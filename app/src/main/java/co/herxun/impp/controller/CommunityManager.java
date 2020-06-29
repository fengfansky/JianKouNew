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
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.controller.PhotoUploader.PhotoUploadCallback;
import co.herxun.impp.model.Community;
import co.herxun.impp.utils.DBug;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class CommunityManager extends Observable {
    private ArrayList<Community> communityList;
    private AnSocial anSocial;
    private Handler handler;
    private Context ct;
    private final static int POST_LIMIT = 20;

    private int page = 0;
    private int totalCommunityCount = 0;

    public CommunityManager(Context ct) {
        this.ct = ct;
        handler = new Handler();
        anSocial = ((IMppApp) ct.getApplicationContext()).anSocial;
    }

    public boolean canLoadMore() {
        DBug.e("totalCommunityCount", communityList.size() + "," + totalCommunityCount);
        return communityList.size() < totalCommunityCount;
    }

    public List<Community> getLocalCommunities() {
        List<Community> Communities = new Select().from(Community.class).orderBy("createdAt DESC").execute();
        return Communities;
    }

    public void init(final FetchCommunitiesCallback callback) {
        page = 0;
        communityList = new ArrayList<Community>();
        fetchRemoteCommunities(++page, new FetchCommunitiesCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                getLocalCommunities(callback);
            }

            @Override
            public void onFinish(List<Community> data) {
                communityList.addAll(data);
                if (callback != null) {
                    callback.onFinish(data);
                }
            }
        });
    }

    public void loadMore(final FetchCommunitiesCallback callback) {
        fetchRemoteCommunities(++page, new FetchCommunitiesCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFinish(List<Community> data) {
                communityList.addAll(data);
                if (callback != null) {
                    callback.onFinish(communityList);
                }
            }
        });
    }

    private void getLocalCommunities(final FetchCommunitiesCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Community> data = new Select().from(Community.class).orderBy("createdAt DESC").execute();
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

    private void fetchRemoteCommunities(final int page, final FetchCommunitiesCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("page", page);
                params.put("limit", POST_LIMIT);
                params.put("sort", "-created_at");

                try {
                    anSocial.sendRequest("objects/community/query.json", AnSocialMethod.GET, params,
                            new IAnSocialCallback() {
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
                                        totalCommunityCount = arg0.getJSONObject("meta").getInt("total");

                                        final List<Community> communities = new ArrayList<Community>();
                                        JSONArray communityArray = arg0.getJSONObject("response").getJSONArray(
                                                "communities");
                                        for (int i = 0; i < communityArray.length(); i++) {
                                            JSONObject communityJson = communityArray.getJSONObject(i);
                                            Community community = new Community();
                                            community.parseJSON(communityJson);
                                            community.update();
                                            communities.add(community);
                                        }
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (callback != null) {
                                                    callback.onFinish(communities);
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

    public interface FetchCommunitiesCallback {
        public void onFailure(String errorMsg);

        public void onFinish(List<Community> data);
    }

    public interface CreateCommunityCallback {
        public void onFailure(String exception);

        public void onSuccess(Community community);
    }

    public void createCommunity(final List<byte[]> dataList, final String name, final String desc, final String url,
            final String userId, final CreateCommunityCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoUploader mPhotoUploader = new PhotoUploader(ct, userId, dataList, new PhotoUploadCallback() {
                    @Override
                    public void onFailure(final String errorMsg) {
                        DBug.e("createCommunity.uploadPhotos.onFailure", errorMsg);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(errorMsg);
                            }
                        });

                    }

                    @Override
                    public void onSuccess(List<String> urlList) {
                        DBug.e("createCommunity.uploadPhotos.onSuccess", "?");
                        String photoUrls = urlList.get(0);

                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                        params.put("communityName", name);
                        params.put("communityDesc", desc);
                        params.put("communityUrl", url);
                        params.put("communityPhotoUrl", photoUrls);
                        try {
                            anSocial.sendRequest("objects/community/create.json", AnSocialMethod.POST, params,
                                    new IAnSocialCallback() {
                                        @Override
                                        public void onFailure(JSONObject arg0) {
                                            try {
                                                final String message = arg0.getJSONObject("meta").getString("message");
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        callback.onFailure(message);
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onSuccess(JSONObject arg0) {
                                            JSONObject communityJson;
                                            try {
                                                communityJson = arg0.getJSONObject("response").getJSONObject(
                                                        "community");
                                                final Community community = new Community();
                                                community.parseJSON(communityJson);
                                                community.update();
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        callback.onSuccess(community);
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                        } catch (final ArrownockException e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(e.getMessage());
                                }
                            });
                            DBug.e("createCommunity.uploadPhotos.onFailure", e.getMessage());
                        }
                    }
                });
                mPhotoUploader.startUpload();
            }
        }).start();

    }
}
