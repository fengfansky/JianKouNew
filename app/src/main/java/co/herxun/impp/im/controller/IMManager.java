package co.herxun.impp.im.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.arrownock.appo.desk.AnDesk;
import com.arrownock.appo.desk.AnDeskMessage;
import com.arrownock.appo.desk.AnDeskMessageType;
import com.arrownock.appo.desk.IAnDeskCallback;
import com.arrownock.appo.desk.IAnDeskGetGroupsCallback;
import com.arrownock.appo.desk.IAnDeskHistoryCallback;
import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.im.AnIMMessage;
import com.arrownock.im.AnIMStatus;
import com.arrownock.im.callback.AnIMAddClientsEventData;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMCallbackAdapter;
import com.arrownock.im.callback.AnIMCreateTopicEventData;
import com.arrownock.im.callback.AnIMGetTopicInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMMessageSentCallbackData;
import com.arrownock.im.callback.AnIMNoticeCallbackData;
import com.arrownock.im.callback.AnIMReadACKCallbackData;
import com.arrownock.im.callback.AnIMReceiveACKCallbackData;
import com.arrownock.im.callback.AnIMRemoveClientsEventData;
import com.arrownock.im.callback.AnIMRemoveTopicEventData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.im.callback.AnIMUpdateTopicEventData;
import com.arrownock.im.callback.IAnIMGetTopicInfoCallback;
import com.arrownock.im.callback.IAnIMGetTopicListCallback;
import com.arrownock.im.callback.IAnIMHistoryCallback;
import com.arrownock.im.callback.IAnIMPushBindingCallback;
import com.arrownock.im.callback.IAnIMTopicCallback;
import com.arrownock.live.AnLive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.activity.BaseActivity;
import co.herxun.impp.activity.LoginActivity;
import co.herxun.impp.controller.MyIAnLiveEventListener;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.FetchSingleUserCallback;
import co.herxun.impp.controller.UserManager.FetchUserlistCallBack;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.DeskGroup;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.im.model.TopicMember;
import co.herxun.impp.model.FriendRequest;
import co.herxun.impp.model.SessionData;
import co.herxun.impp.model.SessionUserData;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.SpfHelper;

public class IMManager extends Observable {
    private static IMManager sIMManager;
    private AnIM anIM;
    private AnDesk anDesk;
    private Handler handler;
    private Context ct;
    private AlertDialog mActionDialog;

    private final static int RECONNECT_RATE = 1000;
    private String currentClientId;

    private boolean retryConnect = false;

    public static String WELCOME_MESSAGE_ID = "100000000";

    public enum UpdateType {
        Topic, Chat, FriendRequest, Like
    }

    private IMManager(Context ct) {
        this.ct = ct;
        handler = new Handler();
        try {
            anIM = new AnIM(ct, ct.getString(R.string.app_key));
            anIM.setCallback(imCallback);
        } catch (ArrownockException e) {

            e.printStackTrace();
        }
    }

    public void initDesk(com.arrownock.appo.desk.User user) {
        try {
            anDesk = new AnDesk(user, ct.getString(R.string.app_key), anIM, ct, deskCallback);
        } catch (ArrownockException e) {
            e.printStackTrace();
            Log.e("impp andesk", "init anDesk failed.");
        }
    }

    public static IMManager getInstance(Context ct) {
        if (sIMManager == null) {
            sIMManager = new IMManager(ct);
        }
        return sIMManager;
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    public AnDesk getAnDesk() {
        return anDesk;
    }

    public AnIM getAnIM() {
        return anIM;
    }

    public void enableRetryConnect(boolean bool) {
        retryConnect = bool;
    }

    public void connect(String clientId) {
        this.currentClientId = clientId;
        retryConnect = true;
        try {
            anIM.connect(clientId);
        } catch (ArrownockException e) {

            e.printStackTrace();
        }
    }

    public void disconnect(boolean logout) {
        retryConnect = false;
        if (logout) {
            currentClientId = null;
        }
        try {
            anIM.disconnect();
        } catch (ArrownockException e) {

            e.printStackTrace();
        }
    }

    private void checkCoonnection() {
        if (!anIM.isOnline() && currentClientId != null) {
            connect(currentClientId);
        }
    }

    public String getCurrentClientId() {
        return currentClientId;
    }

    public void syncHistory() {
        anIM.syncHistory(currentClientId, 100, -1, new IAnIMHistoryCallback() {

            @Override
            public void onError(ArrownockException data) {
                Log.e(IMManager.class.getName(), "Sync history error: " + data);
            }

            @Override
            public void onSuccess(List<AnIMMessage> data, int count) {
                handleOfflineHistory(data);
                if (count > 0) {
                    syncHistory();
                }
            }
        });

    }

    public Message sendMessage(ChatUser user, Message message, byte[] originalData) {
        Chat chat = message.chat;
        String msgId = null;
        Map<String, String> customData = new HashMap<String, String>();
        customData.put("name", user.getUsername());
        customData.put("photoUrl", user.getIconUrl());
        try {
            if (chat.topic != null) {
                if (message.type.equals(Message.TYPE_TEXT)) {
                    customData.put("notification_alert", user.getUsername() + ": " + message.message);
                    msgId = anIM.sendMessageToTopic(chat.topic.topicId, message.message, customData);
                } else if (message.type.equals(Message.TYPE_IMAGE)) {
                    customData.put("notification_alert", user.getUsername() + " " + ct.getString(R.string.noti_image));
                    customData.put("type", Message.TYPE_IMAGE);
                    customData.put("url", message.fileURL);
                    msgId = anIM.sendBinaryToTopic(chat.topic.topicId, message.content, message.type, customData);

                } else if (message.type.equals(Message.TYPE_RECORD)) {
                    customData.put("notification_alert",
                            ct.getString(R.string.noti_record).replace("#", user.getUsername()));
                    msgId = anIM.sendBinaryToTopic(chat.topic.topicId, message.content, message.type, customData);
                }
            } else if (chat.group != null) {
                if (message.type.equals(Message.TYPE_TEXT)) {
                    msgId = anDesk.sendMessage(chat.group.groupSessionId, message.message);
                } else if (message.type.equals(Message.TYPE_IMAGE)) {
                    msgId = anDesk.sendImage(chat.group.groupSessionId, originalData, message.fileURL);
                } else if (message.type.equals(Message.TYPE_RECORD)) {
                    msgId = anDesk.sendAudio(chat.group.groupSessionId, message.content);
                }
            } else {
                if (message.type.equals(Message.TYPE_TEXT)) {
                    customData.put("notification_alert", user.getUsername() + ": " + message.message);
                    msgId = anIM.sendMessage(chat.targetClientId, message.message, customData, true);
                } else if (message.type.equals(Message.TYPE_IMAGE)) {
                    customData.put("notification_alert", user.getUsername() + " " + ct.getString(R.string.noti_image));
                    customData.put("type", Message.TYPE_IMAGE);
                    customData.put("url", message.fileURL);
                    msgId = anIM.sendBinary(chat.targetClientId, message.content, message.type, customData, true);
                } else if (message.type.equals(Message.TYPE_RECORD)) {
                    customData.put("notification_alert",
                            ct.getString(R.string.noti_record).replace("#", user.getUsername()));
                    msgId = anIM.sendBinary(chat.targetClientId, message.content, message.type, customData, true);
                }
            }
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
        message.msgId = msgId;
        message.currentClientId = currentClientId;
        message.fromClient = currentClientId;
        message.status = Message.STATUS_SENDING;
        message.readed = true;

        message.fromUsername = user.getUsername();
        message.fromUserIconUrl = user.getIconUrl();
        message.update();

        setChanged();
        notifyObservers(message);

        return message;
    }

    public void setMessageReaded(Message msg, boolean sendAck) {
        msg.readed = true;
        msg.update();

        if (sendAck) {
            try {
                getAnIM().sendReadACK(msg.fromClient, msg.msgId);
            } catch (ArrownockException e) {
                e.printStackTrace();
            }
        }
    }

    public void getMessageByChat(final Chat chat, final GetMessageCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBug.e("getMessageByChat", (chat.topic == null) + "");
                final List<Message> data = chat.messages();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish(data);
                        }
                    }
                });
            }
        }).start();
    }

    public interface GetMessageCallback {
        public void onFinish(List<Message> data);
    }

    public void getUnReadMessageCount(final GetUnReadedMessageCountCallback callback) {
        IMManager.getInstance(ct).getAllMyChat(new GetChatCallback() {
            @Override
            public void onFinish(final List<Chat> chats) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<Message> unReadMsgs = new ArrayList<Message>();
                        for (Chat chat : chats) {
                            List<Message> msg = new Select()
                                    .from(Message.class)
                                    .where("readed = \"" + 0 + "\" and currentClientId = \"" + currentClientId
                                            + "\" and Chat = \"" + chat.getId() + "\"").execute();
                            unReadMsgs.addAll(msg);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    if (unReadMsgs == null) {
                                        callback.onFinish(0);
                                    } else {
                                        callback.onFinish(unReadMsgs.size());
                                    }
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    public interface GetUnReadedMessageCountCallback {
        public void onFinish(int count);
    }

    public void getAllMyChat(final GetChatCallback callback) {
        final List<Chat> data = new Select().from(Chat.class)
                .where("currentClientId = ? ", SpfHelper.getInstance(ct).getMyClientId()).execute();
        Collections.sort(data, new Comparator<Chat>() {
            @Override
            public int compare(Chat chat1, Chat chat2) {
                if (chat1.lastMessage() == null || chat2.lastMessage() == null) {
                    return 1;
                }

                long time1 = chat1.lastMessage().timestamp;
                long time2 = chat2.lastMessage().timestamp;
                if (time1 > time2) {
                    return -1;
                } else if (time1 < time2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        DBug.e("getAllMyChat", data.size() + "");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFinish(data);
                }
            }
        });
    }

    public Chat addChat(Topic topic) {
        DBug.e("addChat", topic.topicId + "?");
        Chat chat = new Chat();
        chat.currentClientId = currentClientId;
        chat.topic = new Select().from(Topic.class).where("topicId = ?", topic.topicId).executeSingle();
        chat.update();

        return chat.getFromTable();
    }

    public Chat addChat(DeskGroup group) {
        DBug.e("addChat", group.groupId + "?");
        Chat chat = new Chat();
        chat.currentClientId = currentClientId;
        chat.group = new Select().from(DeskGroup.class).where("groupId = ? and currentClientId = ? ", group.groupId, currentClientId).executeSingle();
        chat.update();

        return chat.getFromTable();
    }

    public Chat addChat(String targetClientId) {
        Chat chat = new Chat();
        chat.currentClientId = currentClientId;
        chat.targetClientId = targetClientId;
        chat.update();

        return chat.getFromTable();
    }

    public void deleteChat(Chat chat) {
        chat.getFromTable().delete();

        List<Message> msgs = chat.messages();
        ActiveAndroid.beginTransaction();
        try {
            for (Message msg : msgs) {
                msg.delete();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public void notifyChatUpdated() {
        setChanged();
        notifyObservers(UpdateType.Chat);
    }

    public interface GetChatCallback {
        public void onFinish(List<Chat> data);
    }

    public void createTopic(final String topicName, final Set<String> members) {
        IMManager.getInstance(ct).getAnIM()
                .createTopic(topicName, currentClientId, members, true, currentClientId, new IAnIMTopicCallback() {

                    @Override
                    public void onSuccess(String topicId, long createdTimestamp, long updatedTimestamp) {
                        DBug.d("anIM createTopic", "createTopic successful");
                        Topic topic = new Topic();
                        topic.topicId = topicId;
                        topic.topicName = topicName;
                        topic.ownerClientId = currentClientId;
                        for (String clientId : members) {
                            topic.addMember(clientId);
                        }
                        topic = topic.update();
                        notifyTopicUpdated();
                    }

                    @Override
                    public void onError(ArrownockException arg0) {
                        DBug.e("anIM createTopic", "createTopic failed");
                    }
                });
    }

    public void createRoom(final String topicName, final IAnIMTopicCallback callback) {
        final Set<String> members = new HashSet<String>();
        members.add(currentClientId);
        IMManager.getInstance(ct).getAnIM().createTopic(topicName, null, members, new IAnIMTopicCallback() {

            @Override
            public void onSuccess(String arg0, long arg1, long arg2) {
                DBug.d("anIM createTopic", "createTopic successful");
                Topic topic = new Topic();
                topic.topicId = arg0;
                topic.topicName = topicName;
                topic.ownerClientId = currentClientId;
                for (String clientId : members) {
                    topic.addMember(clientId);
                }
                topic = topic.update();
                notifyTopicUpdated();
                callback.onSuccess(arg0, arg1, arg2);
            }

            @Override
            public void onError(ArrownockException arg0) {
                DBug.e("anIM createTopic", "createTopic failed");
                callback.onError(arg0);
            }
        });
    }

    public void getMyLocalTopic(final FetchLocalTopicCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // final List<Topic> data= new Select().from(Topic.class)
                // .innerJoin(TopicMember.class).on("Topic.topicId = TopicMember.topicId").where("TopicMember.clientId = ?",
                // currentClientId).execute();

                List<Topic> data = new Select().from(Topic.class).execute();
                final List<Topic> filterdata = new ArrayList<Topic>();
                for (Topic topic : data) {
                    // DBug.e("getMyLocalTopic", topic.topicName);
                    List<TopicMember> members = new Select().from(TopicMember.class)
                            .where("topicId = ?", topic.topicId).execute();
                    for (TopicMember member : members) {
                        // DBug.e("getMyLocalTopic", "--"+member.clientId);
                        if (currentClientId.equals(member.clientId)) {
                            filterdata.add(topic);
                            break;
                        }
                    }
                }

                DBug.e("getMyLocalTopic", filterdata.size() + "");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish(filterdata);
                        }
                    }
                });
            }
        }).start();
    }

    public void removeAllTopics() {
        new Delete().from(Topic.class).execute();
    }

    public void fetchAllRemoteTopic() {
        checkCoonnection();
        anIM.getTopicList(currentClientId, new IAnIMGetTopicListCallback() {
            @Override
            public void onSuccess(final AnIMGetTopicListCallbackData data) {
                DBug.d("anIM getTopicList", "getTopicList successful");
                // removeAllTopics();
                getMyLocalTopic(new FetchLocalTopicCallback() {
                    @Override
                    public void onFinish(final List<Topic> localTopicList) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Set<String> filterTopicSet = new HashSet<String>();
                                for (Topic topic : localTopicList) {
                                    filterTopicSet.add(topic.topicId);
                                }
                                List<JSONObject> topicList = data.getTopicList();
                                if (topicList != null && topicList.size() > 0) {
                                    for (JSONObject j : topicList) {
                                        Topic topic = new Topic();
                                        topic.parseJSON(j);
                                        DBug.e("getTopicList", topic.topicName);
                                        topic = IMManager.getInstance(ct).updateTopic(topic);
                                        filterTopicSet.remove(topic.topicId);

                                        try {
                                            JSONArray parties = j.getJSONArray("parties");
                                            for (int i = 0; i < parties.length(); i++) {
                                                String clientId = parties.getString(i);
                                                if (UserManager.getInstance(ct).getUserByClientId(clientId) == null) {
                                                    UserManager.getInstance(ct).fetchUserDataByClientId(clientId);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyTopicUpdated();
                                        }
                                    });
                                } else {
                                    DBug.e("getTopicList", "null");
                                }

                                for (String topicId : filterTopicSet) {
                                    DBug.e("filterTopicSet", topicId);
                                    new Delete().from(Topic.class).where("topicId = ?", topicId).executeSingle();
                                }
                            }
                        }).start();
                    }
                });

            }

            @Override
            public void onError(ArrownockException arg0) {
                DBug.e("anIM getTopicList", "getTopicList failed");
            }
        });
    }

    public interface FetchLocalTopicCallback {
        public void onFinish(List<Topic> data);
    }

    public interface FetchTopicCallback {
        public void onFinish(Topic t);
    }

    private void updateTopic(String topicId, final FetchTopicCallback callback) {
        anIM.getTopicInfo(topicId, new IAnIMGetTopicInfoCallback() {
            @Override
            public void onSuccess(AnIMGetTopicInfoCallbackData data) {
                DBug.d("anIM getTopicInfo", "getTopicInfo successful");
                Topic topic = new Topic();
                topic.topicId = data.getTopicId();
                topic.topicName = data.getTopicName();
                topic.ownerClientId = data.getOwner();
                for (String clientId : data.getParties()) {
                    topic.addMember(clientId);
                }
                IMManager.getInstance(ct).updateTopic(topic);
                notifyTopicUpdated();
                callback.onFinish(topic);
            }

            @Override
            public void onError(ArrownockException arg0) {
                DBug.e("anIM getTopicInfo", "getTopicInfo failed");
            }
        });
    }

    public Topic updateTopic(Topic topic) {
        topic = topic.update();
        if (topic.topicName == null) {
            anIM.getTopicInfo(topic.topicId, new IAnIMGetTopicInfoCallback() {
                @Override
                public void onSuccess(AnIMGetTopicInfoCallbackData data) {
                    DBug.d("anIM getTopicInfo", "getTopicInfo successful");
                    Topic topic = new Topic();
                    topic.topicId = data.getTopicId();
                    topic.topicName = data.getTopicName();
                    topic.ownerClientId = data.getOwner();
                    for (String clientId : data.getParties()) {
                        topic.addMember(clientId);
                    }
                    IMManager.getInstance(ct).updateTopic(topic);
                    notifyTopicUpdated();
                }

                @Override
                public void onError(ArrownockException arg0) {
                    DBug.e("anIM getTopicInfo", "getTopicInfo failed");
                }
            });
        }
        return topic;
    }

    public Topic updateTopicName(String topicName, Topic topic) {
        topic.topicName = topicName;
        topic = topic.update();
        if (topic.topicName != null) {
            anIM.updateTopic(topic.topicId, topic.topicName, topic.ownerClientId, true, currentClientId,
                    new IAnIMTopicCallback() {

                        @Override
                        public void onSuccess(String arg0, long createdTimestamp, long updatedTimestamp) {
                            DBug.d("anIM updateTopic", "updateTopic successful");
                        }

                        @Override
                        public void onError(ArrownockException arg0) {
                            DBug.e("anIM updateTopic", "updateTopic failed");
                        }
                    });
        }
        notifyTopicUpdated();
        return topic;
    }

    public Topic addTopicMembers(Set<String> clients, Topic topic) {
        for (String client : clients) {
            topic.addMember(client);
        }
        if (topic.topicName != null) {
            anIM.addClientsToTopic(topic.topicId, clients, true, currentClientId, new IAnIMTopicCallback() {

                @Override
                public void onSuccess(String arg0, long createdTimestamp, long updatedTimestamp) {
                    DBug.d("anIM addClientsToTopic", "addClientsToTopic successful");
                }

                @Override
                public void onError(ArrownockException arg0) {
                    DBug.e("anIM addClientsToTopic", "addClientsToTopic failed");
                }
            });
        }
        notifyTopicUpdated();
        return topic;
    }

    public void addTopicMembers(final String client, String topicId, final AddTopicCallback callback) {
        Topic t = new Topic();
        t.topicId = topicId;
        Topic topic = t.getFromTable();
        final Set<String> clients = new HashSet<String>();
        clients.add(client);
        if (topic != null) {
            topic.addMember(client);
            if (topic.topicName != null) {
                anIM.addClientsToTopic(topic.topicId, clients, true, client, new IAnIMTopicCallback() {

                    @Override
                    public void onSuccess(String topicId, long createdTimestamp, long updatedTimestamp) {
                        DBug.d("anIM addClientsToTopic", "addClientsToTopic successful");
                        callback.onFinish(topicId);
                    }

                    @Override
                    public void onError(ArrownockException arg0) {
                        DBug.e("anIM addClientsToTopic", "addClientsToTopic failed");
                        callback.onError(arg0);
                    }
                });
            }
            notifyTopicUpdated();
        } else {
            anIM.getTopicInfo(topicId, new IAnIMGetTopicInfoCallback() {

                @Override
                public void onSuccess(AnIMGetTopicInfoCallbackData data) {
                    DBug.d("anIM getTopicInfo", "getTopicInfo successful");
                    Topic topic = new Topic();
                    topic.topicId = data.getTopicId();
                    topic.topicName = data.getTopicName();
                    for (String clientId : data.getParties()) {
                        topic.addMember(clientId);
                    }
                    IMManager.getInstance(ct).updateTopic(topic);
                    topic.addMember(client);
                    if (topic.topicName != null) {
                        anIM.addClientsToTopic(topic.topicId, clients, new IAnIMTopicCallback() {

                            @Override
                            public void onSuccess(String topicId, long createdTimestamp, long updatedTimestamp) {
                                DBug.d("anIM addClientsToTopic", "addClientsToTopic successful");
                                callback.onFinish(topicId);
                            }

                            @Override
                            public void onError(ArrownockException arg0) {
                                DBug.e("anIM addClientsToTopic", "addClientsToTopic failed");
                                callback.onError(arg0);
                            }
                        });
                    }
                    notifyTopicUpdated();
                }

                @Override
                public void onError(ArrownockException arg0) {
                    DBug.e("anIM addClientsToTopic", "addClientsToTopic failed");
                    callback.onError(arg0);
                }
            });
        }
    }

    public Topic removeTopicMembers(String client, Topic topic, final AddTopicCallback callback) {
        topic.removeMember(client);
        Set<String> clients = new HashSet<String>();
        clients.add(client);
        if (topic.topicName != null) {
            anIM.removeClientsFromTopic(topic.topicId, clients, true, client, new IAnIMTopicCallback() {

                @Override
                public void onSuccess(String topicId, long createdTimestamp, long updatedTimestamp) {
                    DBug.d("anIM removeTopicMembers", "removeTopicMembers successful");
                    callback.onFinish(topicId);
                }

                @Override
                public void onError(ArrownockException arg0) {
                    DBug.e("anIM removeTopicMembers", "removeTopicMembers failed");
                    callback.onError(arg0);
                }
            });
        }
        notifyTopicUpdated();
        return topic;
    }

    public interface AddTopicCallback {
        public void onFinish(String topicId);

        public void onError(ArrownockException error);
    }

    public Topic leaveTopic(Chat chat, Topic topic) {
        chat.getFromTable().delete();
        topic.removeMember(currentClientId);

        if (topic.ownerClientId.equals(currentClientId)) {
            anIM.removeTopic(topic.topicId, true, currentClientId, new IAnIMTopicCallback() {
                @Override
                public void onSuccess(String arg0, long createdTimestamp, long updatedTimestamp) {
                    DBug.d("anIM removeTopic", "removeTopic successful");
                }

                @Override
                public void onError(ArrownockException arg0) {
                    DBug.e("anIM removeTopic", "removeTopic failed");
                }
            });
        } else {
            Set<String> client = new HashSet<String>();
            client.add(currentClientId);
            anIM.removeClientsFromTopic(topic.topicId, client, true, currentClientId, new IAnIMTopicCallback() {

                @Override
                public void onSuccess(String arg0, long createdTimestamp, long updatedTimestamp) {
                    DBug.d("anIM removeClientsFromTopic", "removeClientsFromTopic successful");
                }

                @Override
                public void onError(ArrownockException arg0) {
                    DBug.e("anIM removeClientsFromTopic", "removeClientsFromTopic failed");
                }
            });
        }
        notifyTopicUpdated();
        return topic;
    }

    public void notifyTopicUpdated() {
        setChanged();
        notifyObservers(UpdateType.Topic);
    }

    private void handleFriendRequest(final String fromClientId, final Map<String, String> data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String friendRequestType = data.get(Constant.FRIEND_REQUEST_KEY_TYPE);

                if (friendRequestType.equals(Constant.FRIEND_REQUEST_TYPE_APPROVE)) {
                    Toast.makeText(ct, ct.getString(R.string.friend_request_accepted), Toast.LENGTH_LONG).show();
                    User user = new User();
                    user.clientId = fromClientId;
                    UserManager.getInstance(ct).saveUser(user);
                    UserManager.getInstance(ct).addFriendLocal(fromClientId, true);
                } else if (friendRequestType.equals(Constant.FRIEND_REQUEST_TYPE_SEND)) {
                    Toast.makeText(ct, ct.getString(R.string.friend_request_received), Toast.LENGTH_LONG).show();
                    User user = new User();
                    user.clientId = fromClientId;
                    UserManager.getInstance(ct).saveUser(user);
                } else if (friendRequestType.equals(Constant.FRIEND_REQUEST_TYPE_REJECT)) {
                    String requestId = data.get(Constant.FRIEND_REQUEST_ID);
                    FriendRequest request = new FriendRequest();
                    request.friendRequestId = requestId;
                    request.removeRequest();
                    User fromUser = UserManager.getInstance(ct).getUserByClientId(fromClientId);
                    Toast.makeText(ct, fromUser.userName + " " + ct.getString(R.string.friend_request_rejected),
                            Toast.LENGTH_LONG).show();
                    UserManager.getInstance(ct).removeFriendLocal(fromClientId);
                }
                notifyFriendRequest();
            }
        });
    }

    public void notifyFriendRequest() {
        setChanged();
        notifyObservers(UpdateType.FriendRequest);
    }

    private void handleLikeNotice(Object data) {
        if (data instanceof AnIMBinaryCallbackData) {

        } else if (data instanceof AnIMMessage) {

        }
        notifyLike();
    }

    public void notifyLike() {
        setChanged();
        notifyObservers(UpdateType.Like);
    }

    private void handleChatMessage(Object data) {
        final Message msg = new Message();
        if (data instanceof AnIMMessageCallbackData) {
            AnIMMessageCallbackData msgData = (AnIMMessageCallbackData) data;

            Chat chat = IMManager.getInstance(ct).addChat(msgData.getFrom());

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.message = msgData.getMessage();
            msg.msgId = msgData.getMsgId();
            msg.fromClient = msgData.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = Message.TYPE_TEXT;
            msg.readed = false;

            if (msgData.getCustomData() != null) {
                if (msgData.getCustomData().containsKey("name"))
                    msg.fromUsername = msgData.getCustomData().get("name");
                if (msgData.getCustomData().containsKey("photoUrl"))
                    msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
            }

            msg.update();

        } else if (data instanceof AnIMTopicMessageCallbackData) {
            AnIMTopicMessageCallbackData msgData = (AnIMTopicMessageCallbackData) data;

            Topic topic = new Topic();
            topic.topicId = msgData.getTopic();
            topic = IMManager.getInstance(ct).updateTopic(topic);

            Chat chat = IMManager.getInstance(ct).addChat(topic);

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.message = msgData.getMessage();
            msg.msgId = msgData.getMsgId();
            msg.fromClient = msgData.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = Message.TYPE_TEXT;
            msg.readed = false;
            msg.topicId = msgData.getTopic();

            if (msgData.getCustomData() != null) {
                if (msgData.getCustomData().containsKey("name"))
                    msg.fromUsername = msgData.getCustomData().get("name");
                if (msgData.getCustomData().containsKey("photoUrl"))
                    msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
            }
            msg.update();
        } else if (data instanceof AnIMMessage) {
            AnIMMessage msgData = (AnIMMessage) data;
            Chat chat = null;

            if (msgData.getTopicId() != null && msgData.getTopicId().length() > 0) {
                Topic topic = new Topic();
                topic.topicId = msgData.getTopicId();
                topic = IMManager.getInstance(ct).updateTopic(topic);
                msg.topicId = msgData.getTopicId();
                chat = IMManager.getInstance(ct).addChat(topic);
            } else {
                if (IMManager.getInstance(ct).getCurrentClientId().equals(msgData.getFrom())) {
                    // this is a message sent from me on other device
                    // chat =
                    // IMManager.getInstance(ct).addChat(msgData.getTo());
                } else {
                    chat = IMManager.getInstance(ct).addChat(msgData.getFrom());
                }
            }

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.message = msgData.getMessage();
            msg.msgId = msgData.getMsgId();
            msg.fromClient = msgData.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.readed = false;

            DBug.e("***msgData.getFileType()", msgData.getFileType() + "?");
            if (msgData.getFileType() == null) {
                msg.type = Message.TYPE_TEXT;
            } else {
                msg.type = msgData.getFileType();
                msg.content = msgData.getContent();
                if (msgData.getCustomData() != null && msgData.getCustomData().containsKey("url")) {
                    msg.fileURL = msgData.getCustomData().get("url");
                }
            }

            if (msgData.getCustomData() != null) {
                if (msgData.getCustomData().containsKey("name"))
                    msg.fromUsername = msgData.getCustomData().get("name");
                if (msgData.getCustomData().containsKey("photoUrl"))
                    msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
            }

            msg.update();

        } else if (data instanceof AnIMBinaryCallbackData) {
            AnIMBinaryCallbackData msgData = (AnIMBinaryCallbackData) data;

            Chat chat = IMManager.getInstance(ct).addChat(msgData.getFrom());

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.msgId = msgData.getMsgId();
            msg.fromClient = msgData.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = msgData.getFileType();
            msg.readed = false;
            msg.content = msgData.getContent();
            msg.fileURL = msgData.getCustomData().get("url");

            if (msgData.getCustomData() != null) {
                if (msgData.getCustomData().containsKey("name"))
                    msg.fromUsername = msgData.getCustomData().get("name");
                if (msgData.getCustomData().containsKey("photoUrl"))
                    msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
            }
            msg.update();

        } else if (data instanceof AnIMTopicBinaryCallbackData) {
            AnIMTopicBinaryCallbackData msgData = (AnIMTopicBinaryCallbackData) data;

            Topic topic = new Topic();
            topic.topicId = msgData.getTopic();
            topic = IMManager.getInstance(ct).updateTopic(topic);

            Chat chat = IMManager.getInstance(ct).addChat(topic);

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.msgId = msgData.getMsgId();
            msg.fromClient = msgData.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = msgData.getFileType();
            msg.readed = false;
            msg.content = msgData.getContent();
            msg.fileURL = msgData.getCustomData().get("url");
            msg.topicId = msgData.getTopic();

            if (msgData.getCustomData() != null) {
                if (msgData.getCustomData().containsKey("name"))
                    msg.fromUsername = msgData.getCustomData().get("name");
                if (msgData.getCustomData().containsKey("photoUrl"))
                    msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
            }
            msg.update();
        }
        if (msg.topicId == null) {
            setChanged();
            notifyObservers(msg);
        } else {
            updateUserIfDiff(msg, new FetchSingleUserCallback() {

                @Override
                public void onFinish(User user) {
                    Topic topic = new Topic();
                    topic.topicId = msg.topicId;
                    topic = topic.getFromTable();
                    if (!topic.hasMember(user.clientId)) {
                        updateTopic(msg.topicId, new FetchTopicCallback() {
                            @Override
                            public void onFinish(Topic t) {
                                if (ct instanceof Activity) {
                                    Activity act = (Activity) ct;
                                    act.runOnUiThread(new Runnable() {
                                        public void run() {
                                            setChanged();
                                            notifyObservers(msg);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        if (ct instanceof Activity) {
                            Activity act = (Activity) ct;
                            act.runOnUiThread(new Runnable() {
                                public void run() {
                                    setChanged();
                                    notifyObservers(msg);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void updateUserIfDiff(Message msg, FetchSingleUserCallback callback) {
        User u = UserManager.getInstance(ct).getUserByClientId(msg.fromClient);
        if (u != null && u.userPhotoUrl != null && u.userPhotoUrl.equals(msg.fromUserIconUrl)) {
            callback.onFinish(u);
        } else {
            UserManager.getInstance(ct).fetchSIngleUserDataByClientId(msg.fromClient, callback);
        }
    }

    private void updateUserIfName(Message msg, FetchSingleUserCallback callback) {
        User u = UserManager.getInstance(ct).getUserByClientId(msg.fromClient);
        if (u != null && u.userName != null) {
            callback.onFinish(u);
        } else {
            UserManager.getInstance(ct).fetchSIngleUserDataByClientId(msg.fromClient, callback);
        }
    }

    private void getUsers(AnIMAddClientsEventData data, final UserManager.FetchUserlistCallBack callback) {
        /*
         * 1.��������ݿ� ��ȡusers 2.��server��ȡusers 3.���������ȡ��users
         */

        final List<User> userList = new ArrayList<User>();
        // ����parties
        String clientIds = "";
        Set<String> parties = data.getParties();

        Iterator<String> it = parties.iterator();
        while (it.hasNext()) {
            // String client = "";
            String clientId = it.next();
            User user = UserManager.getInstance(ct).getUserByClientId(clientId);
            if (user == null) {
                clientIds = clientId + "," + clientId;
            } else {
                userList.add(user);
            }
        }

        if (clientIds.length() > 0) {
            // valid clientIds
            clientIds.substring(1, clientIds.length());

            UserManager.getInstance(ct).fetchUserlistDataByClientIds(clientIds, new FetchUserlistCallBack() {

                @Override
                public void onFinish(List<User> newUserList) {
                    userList.addAll(newUserList);
                    callback.onFinish(userList);

                }
            });

        } else {
            callback.onFinish(userList);
        }
    }

    private void getRemovedUsers(AnIMRemoveClientsEventData data, final UserManager.FetchUserlistCallBack callback) {

        final List<User> userList = new ArrayList<User>();
        String clientIds = "";
        Set<String> parties = data.getParties();

        Iterator<String> it = parties.iterator();
        while (it.hasNext()) {
            String clientId = it.next();
            User user = UserManager.getInstance(ct).getUserByClientId(clientId);
            if (user == null) {
                clientIds = clientId + "," + clientId;
            } else {
                userList.add(user);
            }
        }

        if (clientIds.length() > 0) {
            clientIds.substring(1, clientIds.length());
            UserManager.getInstance(ct).fetchUserlistDataByClientIds(clientIds, new FetchUserlistCallBack() {

                @Override
                public void onFinish(List<User> newUserList) {
                    userList.addAll(newUserList);
                    callback.onFinish(userList);
                }
            });

        } else {
            callback.onFinish(userList);
        }
    }

    private void handleOfflineHistory(List<AnIMMessage> data) {
        for (int i = data.size() - 1; i >= 0; i--) {
            AnIMMessage msg = data.get(i);
            DBug.e("getOfflineHistory. msg.getMessage()", msg.getMessage() + "?");
            if (msg.getFileType() == null) {
                handleChatMessage(msg);
            } else {
                if (msg.getFileType().equals(Constant.FRIEND_REQUEST_TYPE_SEND)) {
                    String type = msg.getCustomData().get(Constant.FRIEND_REQUEST_KEY_TYPE);
                    if (type.equals(Constant.FRIEND_REQUEST_TYPE_SEND)
                            || type.equals(Constant.FRIEND_REQUEST_TYPE_APPROVE)
                            || type.equals(Constant.FRIEND_REQUEST_TYPE_REJECT)) {
                        handleFriendRequest(msg.getFrom(), msg.getCustomData());
                    } else if (type.equals(Message.TYPE_LIKE)) {
                        handleLikeNotice(msg);
                    }
                } else if (msg.getFileType().equals(Message.TYPE_IMAGE)
                        || msg.getFileType().equals(Message.TYPE_RECORD)) {
                    handleChatMessage(msg);
                }
            }
        }
    }

    public void bindAnPush() {
        IMppApp app = (IMppApp) ct.getApplicationContext();
        anIM.bindAnPushService(app.anPush.getAnID(), ct.getString(R.string.app_key), currentClientId,
                new IAnIMPushBindingCallback() {
                    @Override
                    public void onSuccess() {
                        DBug.d("anIM bindAnPushService", "bindAnPushService successful");
                    }

                    @Override
                    public void onError(ArrownockException arg0) {
                        DBug.e("anIM bindAnPushService", "bindAnPushService failed");
                    }
                });
    }

    public void unbindAnPush() {
        anIM.unbindAnPushService(currentClientId, new IAnIMPushBindingCallback() {
            @Override
            public void onSuccess() {
                DBug.d("AnIM unbindAnPushService", "unbindAnPushService successful");
            }

            @Override
            public void onError(ArrownockException arg0) {
                DBug.d("AnIM unbindAnPushService", "unbindAnPushService failed");
            }
        });
    }

    private AnIMCallbackAdapter imCallback = new AnIMCallbackAdapter() {
        @Override
        public void messageSent(AnIMMessageSentCallbackData data) {
            DBug.e("IMManergerManager anIM", "messageSent" + data.getMsgId());
            Message msg = new Message();
            msg.msgId = data.getMsgId();
            msg.currentClientId = currentClientId;
            msg.readed = true;
            msg.timestamp = data.getTimestamp();

            if (data.isError()) {
                data.getException().printStackTrace();
                msg.status = Message.STATUS_FAILED;
            } else {
                msg.status = Message.STATUS_SENT;
            }

            msg.update();
            setChanged();
            notifyObservers(data);
        }

        public void receivedRemoveClientsFromTopicEvent(final com.arrownock.im.callback.AnIMRemoveClientsEventData data) {
            super.receivedRemoveClientsFromTopicEvent(data);

            final Message msg = new Message();
            Topic topic = new Topic();
            topic.topicId = data.getTopicId();
            topic = IMManager.getInstance(ct).updateTopic(topic);
            Chat chat = IMManager.getInstance(ct).addChat(topic);

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.msgId = data.getEventId();
            msg.fromClient = data.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = Message.TYPE_NOTIFICATION;
            msg.readed = false;
            msg.topicId = data.getTopicId();

            msg.timestamp = data.getTimestamp();

            getRemovedUsers(data, new UserManager.FetchUserlistCallBack() {
                @Override
                public void onFinish(List<User> userList) {
                    Topic topic = new Topic();
                    topic.topicId = data.getTopicId();
                    topic = IMManager.getInstance(ct).updateTopic(topic);
                    String list = "";
                    for (User u : userList) {
                        if (!u.clientId.equals(data.getFrom())) {
                            list = "," + list + u.userName;
                            topic.removeMember(u.clientId);
                        }
                    }

                    String list2 = list.substring(1, list.length());

                    msg.message = list2 + " " + ct.getResources().getString(R.string.notifi_quit_group);
                    msg.update();
                    setChanged();
                    notifyObservers(msg);
                    setChanged();
                    notifyObservers(data);
                }
            });
        }

        public void receivedRemoveTopicEvent(AnIMRemoveTopicEventData data) {
            Topic topic = new Topic();
            topic.topicId = data.getTopicId();
            topic.delete();

        };

        @Override
        public void receivedCreateTopicEvent(final AnIMCreateTopicEventData data) {
            super.receivedCreateTopicEvent(data);

            final Message msg = new Message();
            Topic topic = new Topic();
            topic.topicId = data.getTopicId();
            topic = IMManager.getInstance(ct).updateTopic(topic);
            Chat chat = IMManager.getInstance(ct).addChat(topic);

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.msgId = data.getEventId();
            msg.fromClient = data.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = Message.TYPE_NOTIFICATION;
            msg.readed = false;
            msg.topicId = data.getTopicId();
            msg.timestamp = data.getTimestamp();

            updateUserIfName(msg, new FetchSingleUserCallback() {
                @Override
                public void onFinish(User user) {

                    msg.message = (user.userName + " " + ct.getResources().getString(R.string.notifi_create_group));

                    msg.update();
                    setChanged();
                    notifyObservers(msg);
                    setChanged();
                    notifyObservers(data);

                }
            });

        }

        @Override
        public void receivedUpdateTopicEvent(final AnIMUpdateTopicEventData data) {
            super.receivedUpdateTopicEvent(data);

            final Message msg = new Message();
            Topic topic = new Topic();
            topic.topicId = data.getTopicId();
            topic = IMManager.getInstance(ct).updateTopic(topic);
            topic.topicName = data.getTopicName();
            topic.save();
            Chat chat = IMManager.getInstance(ct).addChat(topic);

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.msgId = data.getEventId();
            msg.fromClient = data.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = Message.TYPE_NOTIFICATION;
            msg.readed = false;
            msg.topicId = data.getTopicId();
            msg.timestamp = data.getTimestamp();

            updateUserIfName(msg, new FetchSingleUserCallback() {
                @Override
                public void onFinish(User user) {

                    msg.message = (user.userName + " " + ct.getResources().getString(R.string.notifi_update_group)
                            + " " + data.getTopicName());
                    msg.update();
                    setChanged();
                    notifyObservers(msg);
                    setChanged();
                    notifyObservers(data);
                }
            });

        }

        @Override
        public void receivedAddClientsToTopicEvent(final AnIMAddClientsEventData data) {
            super.receivedAddClientsToTopicEvent(data);

            final Message msg = new Message();
            Topic topic = new Topic();
            topic.topicId = data.getTopicId();
            topic = IMManager.getInstance(ct).updateTopic(topic);
            Chat chat = IMManager.getInstance(ct).addChat(topic);

            msg.currentClientId = currentClientId;
            msg.chat = chat;
            msg.msgId = data.getEventId();
            msg.fromClient = data.getFrom();
            msg.status = Message.STATUS_SENT;
            msg.type = Message.TYPE_NOTIFICATION;
            msg.readed = false;
            msg.topicId = data.getTopicId();

            msg.timestamp = data.getTimestamp();

            getUsers(data, new UserManager.FetchUserlistCallBack() {
                @Override
                public void onFinish(List<User> userList) {
                    Topic topic = new Topic();
                    topic.topicId = data.getTopicId();
                    topic = IMManager.getInstance(ct).updateTopic(topic);
                    String list = "";
                    for (User u : userList) {
                        if (!u.clientId.equals(data.getFrom())) {
                            list = list + u.userName + ",";
                            topic.addMember(u.clientId);
                        }
                    }

                    list = list.substring(0, list.length() - 1);

                    msg.message = list + " " + ct.getResources().getString(R.string.notifi_add_group);

                    msg.update();
                    setChanged();
                    notifyObservers(msg);
                    setChanged();
                    notifyObservers(data);
                }
            });

        }

        @Override
        public void receivedBinary(AnIMBinaryCallbackData data) {
            DBug.e("IMManergerManager", "receivedBinary");
            if (data.getFileType().equals(Constant.FRIEND_REQUEST_TYPE_SEND)) {
                String type = data.getCustomData().get(Constant.FRIEND_REQUEST_KEY_TYPE);
                DBug.e("receivedBinary", type);
                if (type.equals(Constant.FRIEND_REQUEST_TYPE_SEND) || type.equals(Constant.FRIEND_REQUEST_TYPE_APPROVE)
                        || type.equals(Constant.FRIEND_REQUEST_TYPE_REJECT)) {
                    handleFriendRequest(data.getFrom(), data.getCustomData());
                } else if (type.equals(Message.TYPE_LIKE)) {
                    handleLikeNotice(data);
                }
            } else if (data.getFileType().equals(Message.TYPE_IMAGE) || data.getFileType().equals(Message.TYPE_RECORD)) {
                handleChatMessage(data);
            }

            setChanged();
            notifyObservers(data);
        }

        @Override
        public void receivedMessage(AnIMMessageCallbackData data) {
            handleChatMessage(data);

            setChanged();
            notifyObservers(data);
        }

        @Override
        public void receivedNotice(AnIMNoticeCallbackData data) {

        }

        @Override
        public void receivedReadACK(AnIMReadACKCallbackData data) {
            DBug.e("IMManergerManager", "receivedReadACK");
            Message msg = new Message();
            msg.msgId = data.getMsgId();
            msg.currentClientId = currentClientId;
            msg.readACK = true;
            msg.update();

            setChanged();
            notifyObservers(data);
        }

        @Override
        public void receivedReceiveACK(AnIMReceiveACKCallbackData data) {

        }

        @Override
        public void receivedTopicBinary(AnIMTopicBinaryCallbackData data) {
            if (data.getFileType() != null
                    && (data.getFileType().equals(Message.TYPE_IMAGE) || data.getFileType().equals(Message.TYPE_RECORD))) {
                handleChatMessage(data);
            }

            setChanged();
            notifyObservers(data);
        }

        @Override
        public void receivedTopicMessage(AnIMTopicMessageCallbackData data) {
            handleChatMessage(data);

            setChanged();
            notifyObservers(data);
        }

        /*
         * @Override public void messageSentFromOtherDevice(AnIMMessage message)
         * { if(message != null) { Message msg = new Message();
         * if(AnIMMessageType.AnIMTextMessage == message.getType()) { msg.chat =
         * addChat(message.getTo()); msg.message = message.getMessage();
         * msg.type = Message.TYPE_TEXT; msg.msgId = message.getMsgId();
         * msg.currentClientId = currentClientId; msg.fromClient =
         * currentClientId; msg.status = Message.STATUS_SENT; msg.readed = true;
         * 
         * User mUser = UserManager.getInstance(ct).getCurrentUser();
         * msg.fromUsername = mUser.userName; msg.fromUserIconUrl =
         * mUser.userPhotoUrl; msg.update();
         * 
         * setChanged(); notifyObservers(msg); } } }
         */
        @Override
        public void statusUpdate(final AnIMStatusUpdateCallbackData data) {
            DBug.e("statusUpdate", data.getStatus().name());
            if (data.getStatus() == AnIMStatus.ONLINE) {
                /*
                 * handler.post(new Runnable() {
                 * 
                 * @Override public void run() { Toast.makeText(ct,
                 * ct.getString(R.string.im_connect), Toast.LENGTH_LONG).show();
                 * } });
                 */

                MyIAnLiveEventListener lsr = new MyIAnLiveEventListener(ct);
                IMppApp app = (IMppApp) ct.getApplicationContext();
                try {
                    app.anLive = AnLive.initialize(ct, getAnIM(), lsr);
                } catch (Exception e) {
                    app.anLive = null;
                }

                try {
                    anIM.bindAnPushService(app.anPush.getAnID(), ct.getString(R.string.app_key), currentClientId);
                } catch (ArrownockException e) {
                    e.printStackTrace();
                }

                anIM.getOfflineHistory(currentClientId, 100, new IAnIMHistoryCallback() {
                    @Override
                    public void onError(ArrownockException data) {
                        Log.e(IMManager.class.getName(), "Fetch offline history error: " + data);
                    }

                    @Override
                    public void onSuccess(List<AnIMMessage> data, int count) {
                        handleOfflineHistory(data);
                        if (count > 0) {
                            anIM.getOfflineHistory(currentClientId, 100, this);
                        }
                    }
                });
                anIM.getOfflineTopicHistory(currentClientId, 100, new IAnIMHistoryCallback() {
                    @Override
                    public void onError(ArrownockException data) {
                        Log.e(IMManager.class.getName(), "Fetch offline history error: " + data);
                    }

                    @Override
                    public void onSuccess(List<AnIMMessage> data, int count) {
                        handleOfflineHistory(data);
                        if (count > 0) {
                            anIM.getOfflineTopicHistory(currentClientId, 100, this);
                        }
                    }
                });
                anDesk.getOfflineMessage(currentClientId, 100, new IAnDeskHistoryCallback() {

                    @Override
                    public void onSuccess(List<AnDeskMessage> deskMessageList, int count) {
                        for (int i = deskMessageList.size() - 1; i >= 0; i--) {
                            AnDeskMessage msg = deskMessageList.get(i);
                            handleDeskMessage(msg);
                        }
                        if (count > 0) {
                            anDesk.getOfflineMessage(currentClientId, 100, this);
                        }
                    }

                    @Override
                    public void onError(ArrownockException arg0) {
                        Log.e(IMManager.class.getName(), "anDesk Fetch offline history error: " + data);

                    }
                });
            } else if (data.getStatus() == AnIMStatus.OFFLINE) {
                if (data.getException() != null) {
                    data.getException().printStackTrace();
                    if (data.getException().getErrorCode() == ArrownockException.IM_FAILED_DISCONNECT) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ct, ct.getString(R.string.im_disconnect), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (data.getException().getErrorCode() == ArrownockException.IM_FORCE_CLOSED) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SpfHelper.getInstance(ct).clearUserInfo();
                                currentClientId = null;
                                final IMppApp app = (IMppApp) ct.getApplicationContext();
                                final List<BaseActivity> activeActivityList = app.getActiveActivityList();
                                BaseActivity currentAct = activeActivityList.get(activeActivityList.size() - 1);
                                AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(currentAct);
                                LayoutInflater inflater = LayoutInflater.from(currentAct);
                                View view = inflater.inflate(R.layout.view_kick_off_alert, null);
                                view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        for (BaseActivity act : activeActivityList) {
                                            act.finish();
                                        }
                                        Intent i = new Intent(app, LoginActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        app.startActivity(i);
                                        mActionDialog.dismiss();
                                    }
                                });
                                dialogBuiler.setView(view);
                                mActionDialog = dialogBuiler.create();
                                mActionDialog.setCancelable(false);
                                mActionDialog.show();
                            }
                        });
                    } else {
                        if (currentClientId != null && retryConnect) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    connect(currentClientId);
                                }
                            }, RECONNECT_RATE);
                        }
                    }
                } else {

                }
            }
        }
    };

    private IAnDeskCallback deskCallback = new IAnDeskCallback() {

        @Override
        public void receivedMessage(String groupId, String messageId, String message, long timestamp, String accountId,
                String accountName) {
            AnDeskMessage deskMessage = new AnDeskMessage(AnDeskMessageType.AnDeskText, messageId, groupId, accountId,
                    accountName, message, null, timestamp);
            handleDeskMessage(deskMessage);

        }

        @Override
        public void receivedImage(String groupId, String messageId, byte[] content, long timestamp, String accountId,
                String accountName) {
            AnDeskMessage deskMessage = new AnDeskMessage(AnDeskMessageType.AnDeskImage, messageId, groupId, accountId,
                    accountName, null, content, timestamp);
            handleDeskMessage(deskMessage);
        }

        @Override
        public void messageSent(String messageId, long timestamp, Exception e) {
            Log.e("IMManergerManager Desk", "messageSent" + messageId);
            Message msg = new Message();
            msg.msgId = messageId;
            msg.currentClientId = currentClientId;
            msg.readed = true;
            msg.timestamp = timestamp;

            boolean isError = false;
            ArrownockException error = null;
            if (e != null) {
                error = new ArrownockException(e, 999999);
                isError = true;
                e.printStackTrace();
                msg.status = Message.STATUS_FAILED;
            } else {
                isError = false;
                msg.status = Message.STATUS_SENT;
            }

            msg.update();

            AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(isError, error, messageId, timestamp);
            setChanged();
            notifyObservers(data);
        }

        @Override
        public void sessionClosed(final String groupId, final String sessionId, final long timestamp) {
            SessionData data = new SessionData();
            data.setGroupId(groupId);
            data.setSessionId(sessionId);

            DeskGroup group = new DeskGroup();
            group.groupId = groupId;
            group = group.getFromTable(UserManager.getInstance(ct).getCurrentUser().clientId);
            if (group == null) {
                anDesk.getGroups(new IAnDeskGetGroupsCallback() {
                    @Override
                    public void onSuccess(List<com.arrownock.appo.desk.Group> deskGroupList) {
                        final List<DeskGroup> data = new ArrayList<DeskGroup>();
                        for (com.arrownock.appo.desk.Group deskGroup : deskGroupList) {
                            DeskGroup group = new DeskGroup();
                            group.groupId = deskGroup.getId();
                            group.groupName = deskGroup.getName();
                            group.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                            // group.groupPhotoUrl = deskGroup.g
                            group.update(UserManager.getInstance(ct).getCurrentUser().clientId);
                            data.add(group);
                        }
                        DeskGroup group = new DeskGroup();
                        group.groupId = groupId;
                        group = group.getFromTable(UserManager.getInstance(ct).getCurrentUser().clientId);
                        if (sessionId.equals(group.groupSessionId)) {
                            group.groupAccountId = "";
                            group.groupAccountName = "";
                            group.save();
                        }

                        Message msg = new Message();
                        msg.type = Message.TYPE_NOTIFICATION;
                        msg.message = ct.getResources().getString(R.string.cs_service_finished);
                        msg.fromClient = msg.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                        Chat chat = IMManager.getInstance(ct).addChat(group);
                        msg.chat = chat;
                        msg.update();

                        setChanged();
                        notifyObservers(data);
                        setChanged();
                        notifyObservers(msg);
                    }

                    @Override
                    public void onFailure(ArrownockException e) {
                        Log.e("anDesk getGroup faliure", e.getMessage());
                    }
                });
            } else {
                if (sessionId.equals(group.groupSessionId)) {
                    group.groupAccountId = "";
                    group.groupAccountName = "";
                    group.save();
                }

                Message msg = new Message();
                msg.type = Message.TYPE_NOTIFICATION;
                msg.message = ct.getResources().getString(R.string.cs_service_finished);
                msg.fromClient = msg.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                Chat chat = IMManager.getInstance(ct).addChat(group);
                msg.chat = chat;
                msg.update();

                setChanged();
                notifyObservers(data);
                setChanged();
                notifyObservers(msg);
            }
        }

        @Override
        public void accountAddedToSession(final String groupId, final String sessionId, final String accountId,
                final String accountName, final long timestamp) {
            DeskGroup group = new DeskGroup();
            group.groupId = groupId;
            group = group.getFromTable(UserManager.getInstance(ct).getCurrentUser().clientId);
            if (group != null) {
                if (sessionId.equals(group.groupSessionId)) {
                    group.groupAccountId = accountId;
                    group.groupAccountName = accountName;
                    group.save();
                }

                Message msg = new Message();
                msg.type = Message.TYPE_NOTIFICATION;
                msg.message = ct.getResources().getString(R.string.cs_service_ondoing_prefix) + accountName + "("
                        + ct.getResources().getString(R.string.cs_service_ondoing_id) + accountId + ") "
                        + ct.getResources().getString(R.string.cs_service_ondoing_buffix);
                msg.fromClient = msg.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                msg.readed = true;
                msg.readACK = true;
                Chat chat = IMManager.getInstance(ct).addChat(group);
                msg.chat = chat;
                msg.update();

                setChanged();
                notifyObservers(msg);

                User user = new User();
                user.clientId = accountId;
                user.userId = accountId;
                user.userName = accountName;
                user.update();

                SessionUserData sessionUserData = new SessionUserData();
                sessionUserData.setGroupId(groupId);
                sessionUserData.setSessionId(sessionId);
                sessionUserData.setUser(user);
                setChanged();
                notifyObservers(sessionUserData);
            } else {
                anDesk.getGroups(new IAnDeskGetGroupsCallback() {
                    @Override
                    public void onSuccess(List<com.arrownock.appo.desk.Group> deskGroupList) {
                        final List<DeskGroup> data = new ArrayList<DeskGroup>();
                        for (com.arrownock.appo.desk.Group deskGroup : deskGroupList) {
                            DeskGroup group = new DeskGroup();
                            group.groupId = deskGroup.getId();
                            group.groupName = deskGroup.getName();
                            group.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                            // group.groupPhotoUrl = deskGroup.g
                            group.update(UserManager.getInstance(ct).getCurrentUser().clientId);
                            data.add(group);
                        }
                        DeskGroup group = new DeskGroup();
                        group.groupId = groupId;
                        group = group.getFromTable(UserManager.getInstance(ct).getCurrentUser().clientId);
                        if (sessionId.equals(group.groupSessionId)) {
                            group.groupAccountId = accountId;
                            group.groupAccountName = accountName;
                            group.save();
                        }

                        Message msg = new Message();
                        msg.type = Message.TYPE_NOTIFICATION;
                        msg.message = ct.getResources().getString(R.string.cs_service_ondoing_prefix) + accountName
                                + "(" + ct.getResources().getString(R.string.cs_service_ondoing_id) + accountId + ") "
                                + ct.getResources().getString(R.string.cs_service_ondoing_buffix);
                        msg.fromClient = msg.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                        msg.readed = true;
                        msg.readACK = true;
                        Chat chat = IMManager.getInstance(ct).addChat(group);
                        msg.chat = chat;
                        msg.update();

                        setChanged();
                        notifyObservers(msg);

                        User user = new User();
                        user.clientId = accountId;
                        user.userId = accountId;
                        user.userName = accountName;
                        user.update();

                        SessionUserData sessionUserData = new SessionUserData();
                        sessionUserData.setGroupId(groupId);
                        sessionUserData.setSessionId(sessionId);
                        sessionUserData.setUser(user);
                        setChanged();
                        notifyObservers(sessionUserData);
                    }

                    @Override
                    public void onFailure(ArrownockException e) {
                        Log.e("anDesk getGroup faliure", e.getMessage());
                    }
                });
            }
        }

    };

    private void handleDeskMessage(Object data) {
        if (data instanceof AnDeskMessage) {
            Message msg = new Message();
            AnDeskMessage msgData = (AnDeskMessage) data;
            if (AnDeskMessageType.AnDeskText.equals(msgData.getType())) {
                DeskGroup group = new DeskGroup();
                group.groupId = msgData.getGroupId();
                group.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                group.getFromTable(UserManager.getInstance(ct).getCurrentUser().clientId);

                Chat chat = IMManager.getInstance(ct).addChat(group);

                msg.currentClientId = currentClientId;
                msg.chat = chat;
                msg.message = msgData.getMessage();
                msg.msgId = msgData.getMsgId();
                msg.fromClient = msgData.getAccountId();
                msg.fromUsername = msgData.getAccountName();
                msg.status = Message.STATUS_SENT;
                msg.type = Message.TYPE_TEXT;
                msg.readed = false;
                msg.topicId = msgData.getGroupId();

                msg.update();

            } else if (AnDeskMessageType.AnDeskImage.equals(msgData.getType())) {
                DeskGroup group = new DeskGroup();
                group.groupId = msgData.getGroupId();
                group.currentClientId = UserManager.getInstance(ct).getCurrentUser().clientId;
                group.getFromTable(UserManager.getInstance(ct).getCurrentUser().clientId);

                Chat chat = IMManager.getInstance(ct).addChat(group);

                msg.currentClientId = currentClientId;
                msg.chat = chat;
                msg.msgId = msgData.getMsgId();
                msg.fromClient = msgData.getAccountId();
                msg.fromUsername = msgData.getAccountName();
                msg.status = Message.STATUS_SENT;
                msg.type = Message.TYPE_IMAGE;
                msg.readed = false;
                msg.content = msgData.getData();
                msg.topicId = msgData.getGroupId();

                msg.update();
            }
            setChanged();
            notifyObservers(msg);
        }
    }

}
