package co.herxun.impp.controller;

import java.math.BigDecimal;
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
import co.herxun.impp.controller.PhotoUploader.PhotoUploadCallback;
import co.herxun.impp.model.Event;
import co.herxun.impp.model.Like;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class EventManager extends Observable {
    private ArrayList<Event> eventList;
    private AnSocial anSocial;
    private Handler handler;
    private Context ct;
    private final static int POST_LIMIT = 20;

    private int page = 0;
    private int totalEventCount = 0;
    private int fragType = 0;

    public EventManager(Context ct, int fragType) {
        this.ct = ct;
        handler = new Handler();
        anSocial = ((IMppApp) ct.getApplicationContext()).anSocial;
        this.fragType = fragType;
    }

    public Event getEventByEventId(String eventId) {
        return new Select()
                .from(Event.class)
                .where("eventId = ? and currentUserId = ? ", eventId,
                        UserManager.getInstance(ct).getCurrentUser().userId).executeSingle();
    }

    public List<Event> getLocalEvents() {
        List<Event> eventList = new Select().from(Event.class)
                .where("currentUserId = \"" + UserManager.getInstance(ct).getCurrentUser().userId + "\"")
                .orderBy("createdAt DESC").execute();
        return eventList;
    }

    public List<Event> getMyLocalEvents() {
        List<Event> eventList = new Select()
                .from(Event.class)
                .where("currentUserId = ? and userId = ?", UserManager.getInstance(ct).getCurrentUser().userId,
                        UserManager.getInstance(ct).getCurrentUser().userId).orderBy("createdAt DESC").execute();
        return eventList;
    }

    public List<Event> getJoinLocalEvents() {
        List<Event> eventList = new Select().from(Event.class)
                .where("currentUserId = \"" + UserManager.getInstance(ct).getCurrentUser().userId + "\"")
                .orderBy("attendedAt DESC").execute();
        for (int i = eventList.size() - 1; i >= 0; i--) {
            Event e = eventList.get(i);
            if (e.attendedUserIds == null) {
                eventList.remove(i);
                continue;
            }
            if (e.attendedUserIds != null
                    && !e.attendedUserIds.contains(UserManager.getInstance(ct).getCurrentUser().userId)) {
                eventList.remove(i);
            }
        }
        return eventList;
    }

    public boolean canLoadMore() {
        DBug.e("totalEventCount", eventList.size() + "," + totalEventCount);
        return eventList.size() < totalEventCount;
    }

    public void init(final FetchEventsCallback callback) {
        page = 0;
        eventList = new ArrayList<Event>();
        fetchRemoteEvents(++page, new FetchEventsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                getLocalEvents(callback);
            }

            @Override
            public void onFinish(List<Event> data) {
                eventList.addAll(data);
                if (callback != null) {
                    callback.onFinish(data);
                }
            }
        });
    }

    public void loadMore(final FetchEventsCallback callback) {
        fetchRemoteEvents(++page, new FetchEventsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFinish(List<Event> data) {
                eventList.addAll(data);
                if (callback != null) {
                    callback.onFinish(eventList);
                }
            }
        });
    }

    private void getLocalEvents(final FetchEventsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Event> data = new Select().from(Event.class).orderBy("createdAt DESC").execute();
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

    private void fetchRemoteEvents(final int page, final FetchEventsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("page", page);
                params.put("limit", POST_LIMIT);
                params.put("like_user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                String urlEndPoint = "";
                if (fragType == Constant.VOTE_TYPE_ALL) {
                    urlEndPoint = "events/query.json";
                    params.put("sort", "-created_at");
                } else if (fragType == Constant.VOTE_TYPE_MINE) {
                    urlEndPoint = "events/query.json";
                    params.put("sort", "-created_at");
                    params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                } else if (fragType == Constant.VOTE_TYPE_JOIN) {
                    urlEndPoint = "events/query.json";
                    params.put("sort", "-attended_at");
                    params.put("attended_user_id", UserManager.getInstance(ct).getCurrentUser().userId);
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
                                totalEventCount = arg0.getJSONObject("meta").getInt("total");

                                final List<Event> events = new ArrayList<Event>();
                                JSONArray eventArray = arg0.getJSONObject("response").getJSONArray("events");
                                for (int i = 0; i < eventArray.length(); i++) {
                                    JSONObject eventJson = eventArray.getJSONObject(i);
                                    Event event = new Event();
                                    event.parseJSON(eventJson, UserManager.getInstance(ct).getCurrentUser().userId);
                                    event.update();
                                    if (eventJson.has("like")) {
                                        event.deleteAllLikes(UserManager.getInstance(ct).getCurrentUser().userId);
                                        JSONObject likeJson = eventJson.getJSONObject("like");
                                        Like like = new Like();
                                        like.event = event
                                                .getFromTable(UserManager.getInstance(ct).getCurrentUser().userId);
                                        like.parseJSON(likeJson, UserManager.getInstance(ct).getCurrentUser()
                                                .getFromTable(), UserManager.getInstance(ct).getCurrentUser().userId);
                                        boolean updated = like.update();
                                        DBug.e("like.update", updated + "?");
                                    }
                                    events.add(event);
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onFinish(events);
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

    public interface FetchEventsCallback {
        public void onFailure(String errorMsg);

        public void onFinish(List<Event> data);
    }

    public interface EventCallback {
        public void onFailure(String errorMsg);

        public void onFinish(Event event);
    }

    public interface CreateEventCallback {
        public void onFailure(String exception);

        public void onSuccess(Event event);
    }

    public void createEvent(final List<byte[]> dataList, final String title, final String information,
            final String address, final long duration, final String startTime, final BigDecimal cost,
            final int userLimit, final String userId, final CreateEventCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoUploader mPhotoUploader = new PhotoUploader(ct, userId, dataList, new PhotoUploadCallback() {
                    @Override
                    public void onFailure(final String errorMsg) {
                        DBug.e("createEvent.uploadPhotos.onFailure", errorMsg);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(List<String> urlList) {
                        DBug.e("createEvent.uploadPhotos.onSuccess", "?");
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);
                        params.put("title", title);
                        params.put("information", information);
                        params.put("start_time", startTime);
                        params.put("duration", duration);
                        Map<String, Object> customFields = new HashMap<String, Object>();
                        customFields.put("cost", cost.toString());
                        customFields.put("user_limit", userLimit);
                        customFields.put("address", address);
                        customFields.put("pageview", 0);
                        if (!urlList.isEmpty()) {
                            customFields.put("photo_url", urlList.get(0));
                        }
                        params.put("custom_fields", customFields);
                        try {
                            anSocial.sendRequest("events/create.json", AnSocialMethod.POST, params,
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
                                            JSONObject eventJson;
                                            try {
                                                eventJson = arg0.getJSONObject("response").getJSONObject("event");
                                                final Event event = new Event();
                                                event.parseJSON(eventJson,
                                                        UserManager.getInstance(ct).getCurrentUser().userId);
                                                event.update();
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        callback.onSuccess(event);
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
                            DBug.e("createEvent.uploadPhotos.onFailure", e.getMessage());
                        }
                    }
                });
                mPhotoUploader.startUpload();
            }
        }).start();
    }

    public void updateEventPageView(final Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("event_id", event.eventId);
                Map<String, Object> customFields = new HashMap<String, Object>();
                customFields.put("pageview", event.pageview);
                params.put("custom_fields", customFields);

                try {
                    anSocial.sendRequest("events/update.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            try {
                                JSONObject eventJson = arg0.getJSONObject("response").getJSONObject("event");
                                Event event = new Event();
                                event.parseJSON(eventJson, UserManager.getInstance(ct).getCurrentUser().userId);
                                event.update();
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

    public void attendEvent(final Event event, final processEventCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("event_id", event.eventId);
                params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);

                try {
                    anSocial.sendRequest("events/attend.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {
                            Log.e("events/attend.json", arg0.toString());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFinish(false);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFinish(true);
                                }
                            });
                        }
                    });
                } catch (final ArrownockException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void quitEvent(final Event event, final processEventCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("event_id", event.eventId);
                params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);

                try {
                    anSocial.sendRequest("events/quit.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFinish(false);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFinish(true);
                                }
                            });
                        }
                    });
                } catch (final ArrownockException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void triggerLikeButton(User user, Event event, LikeCallback callback) {
        if (event.myLike(user) == null) {
            createLike(user, event, callback);
        } else {
            deleteLike(event.myLike(user), event, callback);
        }
    }

    private void createLike(User user, final Event event, final LikeCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("object_type", "Event");
        params.put("object_id", event.eventId);
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
                        callback.onFailure(event);
                    }
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    Log.e("createLike", arg0.toString());
                    Like like = new Like();
                    try {
                        like.event = event.getFromTable(UserManager.getInstance(ct).getCurrentUser().userId);
                        like.parseJSON(arg0.getJSONObject("response").getJSONObject("like"), UserManager
                                .getInstance(ct).getCurrentUser().userId);
                        like.update();
                        event.likeCount = event.likeCount + 1;
                        event.save();

                        if (callback != null) {
                            callback.onSuccess(event);
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

    private void deleteLike(final Like like, final Event event, final LikeCallback callback) {
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
                        callback.onFailure(event);
                    }
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    Log.e("deleteLike", arg0.toString());
                    event.likeCount = event.likeCount - 1;
                    event.save();
                    like.delete();

                    if (callback != null) {
                        callback.onSuccess(event);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void refresshAttendedUserIds(final Event event, final queryEventUsersCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("event_id", event.eventId);
                params.put("need_user_detail", true);
                try {
                    anSocial.sendRequest("events/users/query.json", AnSocialMethod.GET, params,
                            new IAnSocialCallback() {
                                @Override
                                public void onFailure(final JSONObject arg0) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onFailure();
                                        }
                                    });
                                }

                                @Override
                                public void onSuccess(JSONObject response) {
                                    final List<User> userList = new ArrayList<User>();
                                    try {
                                        JSONArray users = response.getJSONObject("response").getJSONArray(
                                                "attended_users");
                                        for (int i = 0; i < users.length(); i++) {
                                            JSONObject userJson = users.getJSONObject(i);
                                            User user = new User(userJson);
                                            user.update();
                                            userList.add(user);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onFinish(userList);
                                        }
                                    });
                                }
                            });
                } catch (final ArrownockException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface queryEventUsersCallback {
        public void onFinish(List<User> users);

        public void onFailure();
    }

    public interface processEventCallback {
        public void onFinish(boolean isOk);
    }

    public interface LikeCallback {
        public void onFailure(Event event);

        public void onSuccess(Event event);
    }
}
