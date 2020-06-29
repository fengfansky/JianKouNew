package co.herxun.impp.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.controller.PhotoUploader.PhotoUploadCallback;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class VoteManager extends Observable {
    public ArrayList<Vote> voteList;
    private AnSocial anSocial;
    private Handler handler;
    private Context ct;
    private final static int POST_LIMIT = 20;

    private int page = 0;
    private int totalVoteCount = 0;
    private int fragType = 0;

    public VoteManager(Context ct, int fragType) {
        this.ct = ct;
        handler = new Handler();
        anSocial = ((IMppApp) ct.getApplicationContext()).anSocial;
        this.fragType = fragType;
    }

    public Vote getVoteByVoteId(String voteId) {
        return new Select()
                .from(Vote.class)
                .where("voteId = ? and currentUserId = ? ", voteId, UserManager.getInstance(ct).getCurrentUser().userId)
                .executeSingle();
    }

    public boolean canLoadMore() {
        DBug.e("totalVoteCount", voteList.size() + "," + totalVoteCount);
        return voteList.size() < totalVoteCount;
    }

    public List<Vote> getLocalVotes() {
        List<Vote> voteList = new Select().from(Vote.class)
                .where("currentUserId = \"" + UserManager.getInstance(ct).getCurrentUser().userId + "\"")
                .orderBy("createdAt DESC").execute();
        return voteList;
    }

    public List<Vote> getMyLocalVotes() {
        List<Vote> voteList = new Select()
                .from(Vote.class)
                .where("currentUserId = ? and userId = ?", UserManager.getInstance(ct).getCurrentUser().userId,
                        UserManager.getInstance(ct).getCurrentUser().userId).orderBy("createdAt DESC").execute();
        return voteList;
    }

    public List<Vote> getJoinLocalVotes() {
        List<Vote> voteList = new Select().from(Vote.class)
                .where("currentUserId = ? and votedAt != 0", UserManager.getInstance(ct).getCurrentUser().userId)
                .orderBy("votedAt DESC").execute();
        return voteList;
    }

    public void init(final FetchVotesCallback callback) {
        page = 0;
        voteList = new ArrayList<Vote>();
        fetchRemoteVotes(++page, new FetchVotesCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                getLocalVotes(callback);
            }

            @Override
            public void onFinish(List<Vote> data) {
                voteList.addAll(data);
                if (callback != null) {
                    callback.onFinish(data);
                }
            }
        });
    }

    public void loadMore(final FetchVotesCallback callback) {
        fetchRemoteVotes(++page, new FetchVotesCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFinish(List<Vote> data) {
                voteList.addAll(data);
                if (callback != null) {
                    callback.onFinish(voteList);
                }
            }
        });
    }

    private void getLocalVotes(final FetchVotesCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Vote> data = new Select().from(Vote.class).orderBy("createdAt DESC").execute();
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

    private void fetchRemoteVotes(final int page, final FetchVotesCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("page", page);
                params.put("limit", POST_LIMIT);
                String urlEndPoint = "";
                if (fragType == Constant.VOTE_TYPE_ALL) {
                    urlEndPoint = "votes/query.json";
                    params.put("sort", "-created_at");
                } else if (fragType == Constant.VOTE_TYPE_MINE) {
                    urlEndPoint = "votes/query.json";
                    params.put("sort", "-created_at");
                    params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                } else if (fragType == Constant.VOTE_TYPE_JOIN) {
                    urlEndPoint = "votes/query.json";
                    params.put("voted_user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                    params.put("sort", "-voted_at");
                }

                try {
                    anSocial.sendRequest(urlEndPoint, AnSocialMethod.GET, params, new IAnSocialCallback() {
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
                                totalVoteCount = arg0.getJSONObject("meta").getInt("total");

                                final List<Vote> votes = new ArrayList<Vote>();
                                JSONArray voteArray = arg0.getJSONObject("response").getJSONArray("votes");
                                for (int i = 0; i < voteArray.length(); i++) {
                                    JSONObject voteJson = voteArray.getJSONObject(i);
                                    Vote vote = new Vote();
                                    vote.parseJSON(voteJson, UserManager.getInstance(ct).getCurrentUser().userId);
                                    vote.update();
                                    votes.add(vote);
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onFinish(votes);
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

    public void vote(final Vote vote, final String choice, final VoteCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("vote_id", vote.voteId);
                params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                params.put("choice", choice);
                params.put("type", "public");
                try {
                    anSocial.sendRequest("votes/vote.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {

                            if (callback != null) {
                                try {
                                    final String message = arg0.getJSONObject("meta").getString("message");
                                    int errorCode = arg0.getJSONObject("meta").getInt("errorCode");
                                    // one use only vote one time
                                    if (errorCode == -110200) {
                                        getUserVote(UserManager.getInstance(ct).getCurrentUser().userId, vote.voteId,
                                                callback);
                                    } else {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onFailure(message);
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            try {
                                JSONObject voteJson = arg0.getJSONObject("response").getJSONObject("vote");
                                final Vote vote = new Vote();
                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                try {
                                    c.setTime(sdf.parse(voteJson.getString("updated_at")));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                vote.voteSelectedChoices = choice;
                                vote.votedAt = c.getTimeInMillis();
                                vote.parseJSON(voteJson, UserManager.getInstance(ct).getCurrentUser().userId);
                                vote.update();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onFinish(vote);
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

    private void getUserVote(String userId, String voteId, final VoteCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        String urlEndPoint = "votes/get.json";
        params.put("vote_ids", voteId);
        params.put("voted_user_id", userId);
        try {
            anSocial.sendRequest(urlEndPoint, AnSocialMethod.GET, params, new IAnSocialCallback() {

                @Override
                public void onSuccess(JSONObject arg0) {
                    try {
                        JSONArray voteArray = arg0.getJSONObject("response").getJSONArray("votes");
                        JSONObject voteJson = voteArray.getJSONObject(0);
                        final Vote vote = new Vote();
                        vote.parseJSON(voteJson, UserManager.getInstance(ct).getCurrentUser().userId);
                        vote.update();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    callback.onFinish(vote);
                                }
                            }
                        });

                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(final JSONObject arg0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                try {
                                    String message = arg0.getJSONObject("meta").getString("message");
                                    callback.onFailure(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public interface FetchVotesCallback {
        public void onFailure(String errorMsg);

        public void onFinish(List<Vote> data);
    }

    public interface VoteCallback {
        public void onFailure(String errorMsg);

        public void onFinish(Vote vote);
    }

    public interface CreateVoteCallback {
        public void onFailure(String exception);

        public void onSuccess(Vote vote);
    }

    public void createVote(final List<byte[]> dataList, final String title, final String desc, final String choices,
            final boolean isSingle, final String userId, final CreateVoteCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoUploader mPhotoUploader = new PhotoUploader(ct, userId, dataList, new PhotoUploadCallback() {
                    @Override
                    public void onFailure(final String errorMsg) {
                        DBug.e("createVote.uploadPhotos.onFailure", errorMsg);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(List<String> urlList) {
                        DBug.e("createVote.uploadPhotos.onSuccess", "?");
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                        params.put("title", title);
                        params.put("content", desc);
                        Map<String, String> choicesMap = new HashMap<String, String>();
                        if (choices.indexOf(",") != -1) {
                            String[] choicesArray = choices.split(",");
                            for (int i = 0; i < choicesArray.length; i++) {
                                choicesMap.put("choice_" + (i + 1), choicesArray[i]);
                            }
                        } else {
                            choicesMap.put("choice_" + "1", choices);
                        }
                        params.put("choices", choicesMap);
                        Map<String, String> customFields = new HashMap<String, String>();
                        customFields
                                .put("voteType", isSingle ? Constant.VOTE_TYPE_SINGLE : Constant.VOTE_TYPE_MULTIPLE);
                        if (!urlList.isEmpty()) {
                            String photoUrls = urlList.get(0);
                            customFields.put("votePhotoUrl", photoUrls);
                        }

                        params.put("custom_fields", customFields);
                        try {
                            anSocial.sendRequest("votes/create.json", AnSocialMethod.POST, params,
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
                                            JSONObject voteJson;
                                            try {
                                                voteJson = arg0.getJSONObject("response").getJSONObject("vote");
                                                final Vote vote = new Vote();
                                                vote.parseJSON(voteJson,
                                                        UserManager.getInstance(ct).getCurrentUser().userId);
                                                vote.update();
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        callback.onSuccess(vote);
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
                            DBug.e("createVote.uploadPhotos.onFailure", e.getMessage());
                        }
                    }
                });
                mPhotoUploader.startUpload();
            }
        }).start();

    }
}
