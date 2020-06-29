package co.herxun.impp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.model.Bulletin;
import co.herxun.impp.model.Like;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class BulletinManager extends Observable {
    private ArrayList<Bulletin> bulletinList;
    private AnSocial anSocial;
    private Handler handler;
    private Context ct;
    private final static int POST_LIMIT = 20;
    private int page = 0;
    private int totalBulletinCount = 0;

    public BulletinManager(Context ct) {
        this.ct = ct;
        handler = new Handler();
        anSocial = ((IMppApp) ct.getApplicationContext()).anSocial;
    }
    
    public List<Bulletin> getLocalBulletins() {
        List<Bulletin> BulletinList = new Select().from(Bulletin.class)
                .where("currentUserId = \"" + UserManager.getInstance(ct).getCurrentUser().userId + "\"")
                .orderBy("createdAt DESC").execute();
        return BulletinList;
    }

    public boolean canLoadMore() {
        DBug.e("totalBulletinCount", bulletinList.size() + "," + totalBulletinCount);
        return bulletinList.size() < totalBulletinCount;
    }

    public void init(final FetchBulletinsCallback callback) {
        page = 0;
        bulletinList = new ArrayList<Bulletin>();
        fetchRemoteBulletins(++page, new FetchBulletinsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                getLocalBulletins(callback);
            }

            @Override
            public void onFinish(List<Bulletin> data) {
                bulletinList.addAll(data);
                if (callback != null) {
                    callback.onFinish(data);
                }
            }
        });
    }

    public void loadMore(final FetchBulletinsCallback callback) {
        fetchRemoteBulletins(++page, new FetchBulletinsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFinish(List<Bulletin> data) {
                bulletinList.addAll(data);
                if (callback != null) {
                    callback.onFinish(bulletinList);
                }
            }
        });
    }

    public Bulletin getBulletinById(String bulletinId) {
        return new Select()
                .from(Bulletin.class)
                .where("bulletinId = ? and currentUserId = ? ", bulletinId,
                        UserManager.getInstance(ct).getCurrentUser().userId).executeSingle();

    }

    private void getLocalBulletins(final FetchBulletinsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Bulletin> data = new Select().from(Bulletin.class)
                        .where("currentUserId = ? and state = 1", UserManager.getInstance(ct).getCurrentUser().userId)
                        .orderBy("createdAt DESC").execute();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            if (data.size() == 0) {
                                // callback.onFailure(ct.getResources().getString(R.string.general_no_data_error));
                            } else {
                                callback.onFinish(data);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void fetchRemoteBulletins(final int page, final FetchBulletinsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("page", page);
                params.put("limit", POST_LIMIT);
                params.put("sort", "-created_at");
                params.put("type", Constant.BULLETIN_TYPE);
                Map<String, String> customFields = new HashMap<String, String>();
                customFields.put("state", "1");
                params.put("custom_fields", customFields);
                params.put("like_user_id", UserManager.getInstance(ct).getCurrentUser().userId);

                try {
                    anSocial.sendRequest("posts/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {
                            try {
                                String message = arg0.getJSONObject("meta").getString("message");
                                Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callback != null) {
                                        callback.onFailure(arg0.toString());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            try {
                                totalBulletinCount = arg0.getJSONObject("meta").getInt("total");

                                final List<Bulletin> bulletins = new ArrayList<Bulletin>();
                                JSONArray bulletinArray = arg0.getJSONObject("response").getJSONArray("posts");
                                for (int i = 0; i < bulletinArray.length(); i++) {
                                    JSONObject bulletinJson = bulletinArray.getJSONObject(i);
                                    Bulletin bulletin = new Bulletin();
                                    bulletin.parseJSON(bulletinJson,
                                            UserManager.getInstance(ct).getCurrentUser().userId);
                                    bulletin.update();
                                    bulletins.add(bulletin);

                                    if (bulletinJson.has("like")) {
                                        bulletin.deleteAllLikes(UserManager.getInstance(ct).getCurrentUser().userId);
                                        JSONObject likeJson = bulletinJson.getJSONObject("like");
                                        Like like = new Like();
                                        like.bulletin = bulletin.getFromTable(UserManager.getInstance(ct)
                                                .getCurrentUser().userId);
                                        like.parseJSON(likeJson, UserManager.getInstance(ct).getCurrentUser()
                                                .getFromTable(), UserManager.getInstance(ct).getCurrentUser().userId);
                                        boolean updated = like.update();
                                        DBug.e("like.update", updated + "?");
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onFinish(bulletins);
                                        }
                                    }
                                });

                            } catch (final JSONException e) {
                                e.printStackTrace();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onFailure(e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (final ArrownockException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onFailure(e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public void updateBulltinPageView(final Bulletin bulletin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post_id", bulletin.bulletinId);
                Map<String, Object> customFields = new HashMap<String, Object>();
                customFields.put("pageview", bulletin.pageview);
                params.put("custom_fields", customFields);

                try {
                    anSocial.sendRequest("posts/update.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            try {
                                JSONObject bulletinJson = arg0.getJSONObject("response").getJSONObject("post");
                                Bulletin bulletin = new Bulletin();
                                bulletin.parseJSON(bulletinJson, UserManager.getInstance(ct).getCurrentUser().userId);
                                bulletin.update();
                            } catch (final JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (final ArrownockException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void triggerLikeButton(User user, Bulletin bulletin, LikeCallback callback) {
        if (bulletin.myLike(user) == null) {
            createLike(user, bulletin, callback);
        } else {
            deleteLike(bulletin.myLike(user), bulletin, callback);
        }
    }

    private void createLike(User user, final Bulletin bulletin, final LikeCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("object_type", "Post");
        params.put("object_id", bulletin.bulletinId);
        params.put("like", "true");
        params.put("user_id", user.userId);

        try {
            anSocial.sendRequest("likes/create.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    Log.e("createLike", arg0.toString());
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.onFailure(bulletin);
                    }
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    Log.e("createLike", arg0.toString());
                    Like like = new Like();
                    try {
                        like.bulletin = bulletin.getFromTable(UserManager.getInstance(ct).getCurrentUser().userId);
                        like.parseJSON(arg0.getJSONObject("response").getJSONObject("like"), UserManager
                                .getInstance(ct).getCurrentUser().userId);
                        like.update();
                        bulletin.likeCount = bulletin.likeCount + 1;
                        bulletin.save();

                        if (callback != null) {
                            callback.onSuccess(bulletin);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    private void deleteLike(final Like like, final Bulletin bulletin, final LikeCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("like_id", like.likeId);

        try {
            anSocial.sendRequest("likes/delete.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    Log.e("deleteLike", arg0.toString());
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.onFailure(bulletin);
                    }
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    Log.e("deleteLike", arg0.toString());
                    bulletin.likeCount = bulletin.likeCount - 1;
                    bulletin.save();
                    like.delete();

                    if (callback != null) {
                        callback.onSuccess(bulletin);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public interface LikeCallback {
        public void onFailure(Bulletin bulletin);

        public void onSuccess(Bulletin bulletin);
    }

    public interface FetchBulletinsCallback {
        public void onFailure(String errorMsg);

        public void onFinish(List<Bulletin> data);
    }
}
