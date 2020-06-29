package co.herxun.impp.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.arrownock.appo.desk.IAnDeskCreateSessionCallback;
import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMAddClientsEventData;
import com.arrownock.im.callback.AnIMRemoveClientsEventData;
import com.arrownock.im.callback.AnIMUpdateTopicEventData;
import com.arrownock.live.IStartCallCallback;
import com.arrownock.social.IAnSocialCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.controller.RoomManager;
import co.herxun.impp.controller.SocialManager;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.DeskGroup;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.model.TopicMember;
import co.herxun.impp.im.view.ChatView;
import co.herxun.impp.model.Room;
import co.herxun.impp.model.SessionData;
import co.herxun.impp.model.SessionUserData;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class ChatActivity extends BaseActivity implements Observer {
    private ChatView mChatView;
    private AlertDialog mActionDialog, mTopicActionDialog;
    private AppBar mAppBar;
    private Chat mChat;
    private IMppApp mApp;

    private View btnVideoCall, btnVoiceCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        IMManager.getInstance(this).addObserver(this);
        mApp = (IMppApp) getApplicationContext();

        initView();
        checkBundle();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkBundle();
    }

    private void initView() {
        mChatView = (ChatView) findViewById(R.id.chatView);
        mChatView.imgAttachment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.show();
            }
        });

        mAppBar = (AppBar) findViewById(R.id.chat_app_bar);
        mAppBar.getLogoView().setImageResource(R.drawable.menu_back);
        mAppBar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        mAppBar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.view_chat_alert, null);
        view.findViewById(R.id.action_dialog_chat_take_photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    Uri mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(ChatActivity.this));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, Constant.REQUESTCODE_PHOTO_TAKE);
                } catch (ActivityNotFoundException e) {
                    Log.d("", "cannot take picture", e);
                }
            }
        });
        view.findViewById(R.id.action_dialog_chat_pick_photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();

                // Intent intent = new Intent();
                // intent.setType("image/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(Intent.createChooser(intent, ""),
                // Constant.REQUESTCODE_PHOTO_PICK);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Constant.REQUESTCODE_PHOTO_PICK);
            }
        });
        view.findViewById(R.id.action_dialog_chat_record).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();
                mChatView.showRecordBox(true);
            }
        });
        btnVoiceCall = view.findViewById(R.id.action_dialog_chat_voice_chat);
        btnVoiceCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                audioCall(mChat.targetClientId);
                mActionDialog.dismiss();
            }
        });
        btnVideoCall = view.findViewById(R.id.action_dialog_chat_video_chat);
        btnVideoCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                videoCall(mChat.targetClientId);
                mActionDialog.dismiss();
            }
        });

        dialogBuiler.setView(view);
        mActionDialog = dialogBuiler.create();

    }

    private void checkBundle() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Constant.INTENT_EXTRA_KEY_CHAT)) {
            mChat = (Chat) getIntent().getExtras().getSerializable(Constant.INTENT_EXTRA_KEY_CHAT);

            mChatView.initListView(mChat.getFromTable());
            Map<String, ChatUser> userMap = new HashMap<String, ChatUser>();

            mAppBar.getTextView().setVisibility(View.VISIBLE);
            if (mChat.topic != null) {
                for (TopicMember topicMember : mChat.topic.members()) {
                    User user = UserManager.getInstance(this).getUserByClientId(topicMember.clientId);
                    if (user != null)
                        userMap.put(user.clientId, new ChatUser(user.clientId, user.userName, user.userPhotoUrl));
                }
                List<Message> data = mChat.getFromTable().messages();
                for (Message msg : data) {
                    if (!(userMap.containsKey(msg.fromClient))) {
                        User user = UserManager.getInstance(this).getUserByClientId(msg.fromClient);
                        userMap.put(user.clientId, new ChatUser(user.clientId, user.userName, user.userPhotoUrl));
                    }
                }

                mAppBar.getTextView().setText(mChat.topic.topicName + "(" + mChat.topic.members().size() + ")");
                mAppBar.getMenuItemView().setVisibility(View.VISIBLE);
                mAppBar.getMenuItemView().setImageResource(R.drawable.menu_edit);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(
                        this, 56));
                rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                mAppBar.getMenuItemView().setLayoutParams(rlp);
                mAppBar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTopicActionDialog.show();
                    }
                });

                initTopicDialog();

                btnVoiceCall.setVisibility(View.GONE);
                btnVideoCall.setVisibility(View.GONE);

                RoomManager roomManager = new RoomManager(this);
                final Room room = roomManager.isRoomExists(mChat.topic.topicId);
                if (room != null && room.topicId != null) {

                    mAppBar.getMenuItemView().setVisibility(View.VISIBLE);
                    mAppBar.getMenuItemView().setImageResource(R.drawable.menu_group);
                    mAppBar.getMenuItemView().setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(ChatActivity.this, RoomUserListActivity.class);
                            Bundle b = new Bundle();
                            b.putSerializable(Constant.ROOM_ID, room.roomId);
                            i.putExtras(b);
                            // mAppBar.getMenuItemView().setVisibility(View.GONE);
                            startActivity(i);
                        }
                    });
                }
            } else if (mChat.group != null) {
                mAppBar.getTextView().setText(mChat.group.groupName);
                btnVoiceCall.setVisibility(View.GONE);
                btnVideoCall.setVisibility(View.GONE);
            } else if (mChat.targetClientId != null) {
                User user = UserManager.getInstance(this).getUserByClientId(mChat.targetClientId);
                if (user != null) {
                    userMap.put(user.clientId, new ChatUser(user.clientId, user.userName, user.userPhotoUrl));
                    mAppBar.getTextView().setText(
                            UserManager.getInstance(this).getUserByClientId(mChat.targetClientId).userName);
                }
            }
            mChatView.setUserMap(userMap);
        }
    }

    private void initData() {
        User currentUser = UserManager.getInstance(this).getCurrentUser();
        // 和客服聊天
        if (mChat.group != null) {
            String oriSessionId = mChat.group.groupSessionId;
            // String oriAccountId = mChat.group.groupAccountId;
            // String oriAccountName = mChat.group.groupAccountName;

            String currentSessionId = "";
            try {
                currentSessionId = IMManager.getInstance(this).getAnDesk()
                        .getCurrentSessionId(mChat.group.groupId, currentUser.clientId);
            } catch (ArrownockException e) {
                e.printStackTrace();
            }

            if (oriSessionId != null && oriSessionId.length() > 0) {
                if (oriSessionId.equals(currentSessionId)) {
                    // 客服没有断开，继续和老客服聊天
                    chatWithOldCustomService();
                } else {
                    // 之前的客服已经断开，需要寻找新客服
                    needChatWithNewCustomService(currentSessionId, currentUser);
                }
            } else {
                // 之前没有客服，直接寻找新客服聊天
                chatWithNewCustomService(currentUser);
            }
        }

        ChatUser cUser = new ChatUser(currentUser.clientId, currentUser.userName, currentUser.userPhotoUrl);
        mChatView.setChat(cUser, mChat.getFromTable());
    }

    private void needChatWithNewCustomService(final String currentSessionId, final User currentUser) {
        mChatView.showReConnectCSBox(true);
        mChatView.getReconnectBox().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                chatWithNewCustomService(currentUser);
            }
        });
    }

    private void chatWithOldCustomService() {
        mChatView.showReConnectCSBox(false);
        // 设置客服专员聊天
        Map<String, ChatUser> userMap = new HashMap<String, ChatUser>();
        userMap.put(mChat.group.groupAccountId, new ChatUser(mChat.group.groupAccountId, mChat.group.groupAccountName,
                null));
        mChatView.setUserMap(userMap);
    }

    private void chatWithNewCustomService(final User currentUser) {
        mChatView.showReConnectCSBox(false);
        // 新建立客服
        Message msg = new Message();
        msg.type = Message.TYPE_NOTIFICATION;
        msg.currentClientId = currentUser.clientId;
        msg.message = getResources().getString(R.string.cs_find_custom_service);
        msg.chat = mChat.getFromTable();
        msg.readed = true;
        msg.readACK = true;
        msg.update();
        mChatView.appendMessage(msg);
        showLoading();
        IMManager.getInstance(this).getAnDesk()
                .createSession(mChat.group.groupId, currentUser.clientId, new IAnDeskCreateSessionCallback() {

                    @Override
                    public void onSuccess(final String sessionId, final String accountId, final String accountName) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissLoading();
                                // 保存聊天group
                                DeskGroup g = mChat.group;
                                g.currentClientId = UserManager.getInstance(ChatActivity.this).getCurrentUser().clientId;
                                g.groupAccountId = accountId;
                                g.groupAccountName = accountName;
                                g.groupSessionId = sessionId;
                                g.update(UserManager.getInstance(ChatActivity.this).getCurrentUser().clientId);
                                if (accountId == null || accountId.length() == 0) {
                                    // 显示未找到在线客服信息，留言
                                    Message msg = new Message();
                                    msg.type = Message.TYPE_NOTIFICATION;
                                    msg.currentClientId = currentUser.clientId;
                                    msg.message = getResources().getString(R.string.cs_service_leave_msg);
                                    msg.chat = mChat.getFromTable();
                                    msg.readed = true;
                                    msg.readACK = true;
                                    msg.update();
                                    mChatView.appendMessage(msg);
                                } else {
                                    // 显示找到客服信息
                                    Message msg = new Message();
                                    msg.type = Message.TYPE_NOTIFICATION;
                                    msg.currentClientId = currentUser.clientId;
                                    msg.message = getResources().getString(R.string.cs_service_ondoing_prefix)
                                            + accountName + "("
                                            + getResources().getString(R.string.cs_service_ondoing_id) + accountId
                                            + ") " + getResources().getString(R.string.cs_service_ondoing_buffix);
                                    msg.chat = mChat.getFromTable();
                                    msg.readed = true;
                                    msg.readACK = true;
                                    msg.update();
                                    mChatView.appendMessage(msg);
                                    // 设置客服专员聊天
                                    User user = new User();
                                    user.clientId = accountId;
                                    user.userId = accountId;
                                    user.userName = accountName;
                                    user.update();
                                    Map<String, ChatUser> userMap = new HashMap<String, ChatUser>();
                                    userMap.put(accountId, new ChatUser(accountId, accountName, null));
                                    mChatView.setUserMap(userMap);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(ArrownockException arg0) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(ChatActivity.this, getString(R.string.no_cs), Toast.LENGTH_LONG).show();
                                ChatActivity.this.finish();
                            }
                        });
                    }
                });
    }

    private void audioCall(String clientId) {
        Intent i = new Intent(ChatActivity.this, VideoActivity.class);
        i.putExtra("mode", VideoActivity.INVITATION_SEND);
        i.putExtra("type", VideoActivity.TYPE_VOICE);
        i.putExtra("clientId", clientId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.push_up_in, android.R.anim.fade_out);

        if (mApp.anLive != null) {
            Map<String, String> notiData = new HashMap<String, String>();
            notiData.put("username", UserManager.getInstance(this).getCurrentUser().userName);
            mApp.anLive.voiceCall(clientId, notiData, new IStartCallCallback() {
                @Override
                public void onFailure(ArrownockException e) {
                    Log.e("videoCall-Audio", e.getMessage() + "=" + e.getErrorCode());
                }

                @Override
                public void onReady(String arg0) {
                }
            });
        } else {

        }
    }

    private void videoCall(String clientId) {
        if (mApp.anLive != null) {
            Map<String, String> notiData = new HashMap<String, String>();
            notiData.put("username", UserManager.getInstance(this).getCurrentUser().userName);
            mApp.anLive.videoCall(clientId, true, notiData, new IStartCallCallback() {
                @Override
                public void onFailure(ArrownockException e) {
                    Log.e("videoCall-Video", e.getMessage() + "=" + e.getErrorCode());
                }

                @Override
                public void onReady(String arg0) {
                }
            });

            Intent i = new Intent(ChatActivity.this, VideoActivity.class);
            i.putExtra("type", VideoActivity.TYPE_VIDEO);
            i.putExtra("mode", VideoActivity.INVITATION_SEND);
            i.putExtra("clientId", clientId);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(i);
            overridePendingTransition(R.anim.push_up_in, android.R.anim.fade_out);
        } else {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }

    private void initTopicDialog() {
        AlertDialog.Builder topicDialogBuiler = new AlertDialog.Builder(this);
        View viewTopicDialog = LayoutInflater.from(this).inflate(R.layout.view_topic_alert, null);
        viewTopicDialog.findViewById(R.id.action_dialog_topic_edit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatActivity.this, EditTopicActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(Constant.INTENT_EXTRA_KEY_TOPIC, mChat.topic);
                i.putExtras(b);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                mTopicActionDialog.dismiss();
            }
        });
        viewTopicDialog.findViewById(R.id.action_dialog_topic_invite).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatActivity.this, CreateTopicActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(Constant.INTENT_EXTRA_KEY_TOPIC, mChat.topic);
                b.putString(Constant.INTENT_EXTRA_KEY_TOPIC_EDIT_TYPE, CreateTopicActivity.TYPE_INVITE);
                List<TopicMember> members = mChat.topic.members();
                String[] filterUsers = new String[mChat.topic.members().size()];
                for (int j = 0; j < filterUsers.length; j++) {
                    TopicMember topicMember = members.get(j);
                    filterUsers[j] = topicMember.clientId;
                }
                b.putStringArray(Constant.INTENT_EXTRA_KEY_TOPIC_EDIT_FILTER_MEMBERS, filterUsers);
                i.putExtras(b);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                mTopicActionDialog.dismiss();
            }
        });

        TextView textLeaveTopic = (TextView) viewTopicDialog.findViewById(R.id.action_dialog_topic_leave_text);
        DBug.e("mChat", (mChat == null) + "?");
        DBug.e("mChat.topic", (mChat.topic == null) + "?");
        DBug.e("mChat.topic.ownerClientId", (mChat.topic.ownerClientId == null) + "?");
        if (IMManager.getInstance(this).getCurrentClientId().equals(mChat.topic.ownerClientId)) {
            textLeaveTopic.setText(R.string.chat_topic_leave_owner);
        } else {
            textLeaveTopic.setText(R.string.chat_topic_leave);
        }
        viewTopicDialog.findViewById(R.id.action_dialog_topic_leave).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DBug.e("viewTopicDialog", "leaveTopic");
                IMManager.getInstance(v.getContext()).leaveTopic(mChat, mChat.topic);
                mTopicActionDialog.dismiss();
                onBackPressed();
            }
        });

        topicDialogBuiler.setView(viewTopicDialog);
        mTopicActionDialog = topicDialogBuiler.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMManager.getInstance(this).connect(IMManager.getInstance(this).getCurrentClientId());
        if (requestCode == Constant.REQUESTCODE_PHOTO_PICK) {
            if (data != null) {
                String imageFilePath = ImageUtility.getFilePathFromGallery(this, data);
                if (imageFilePath != null) {
                    uploadPhoto(imageFilePath);
                }
            }
        } else if (requestCode == Constant.REQUESTCODE_PHOTO_TAKE) {
            if (resultCode == RESULT_OK) {
                String imageFilePath = ImageUtility.getFileTemp(this).getPath();
                if (imageFilePath != null) {
                    uploadPhoto(imageFilePath);
                }
            }
        }
    }

    private void uploadPhoto(String filePath) {
        byte[] fileToUpload, fileToSend;

        File file = new File(filePath);
        int size = (int) file.length();
        fileToUpload = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(fileToUpload, 0, fileToUpload.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap original = BitmapFactory.decodeByteArray(fileToUpload, 0, fileToUpload.length);
        final double MAX_SIZE = 100;
        double imgViewW = 0;
        double imgViewH;
        if (original.getWidth() > original.getHeight()) {
            imgViewW = MAX_SIZE;
            imgViewH = original.getHeight() * (MAX_SIZE / original.getWidth());
        } else {
            imgViewH = MAX_SIZE;
            imgViewW = original.getWidth() * (MAX_SIZE / original.getHeight());
        }
        Bitmap resized = Bitmap.createScaledBitmap(original, (int) imgViewW, (int) imgViewH, true);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, blob);
        fileToSend = blob.toByteArray();

        final byte[] fileData = fileToSend;
        final byte[] originalData = fileToUpload;
        SocialManager.createPhoto(this, UserManager.getInstance(this).getCurrentUser().userId, fileToUpload,
                new IAnSocialCallback() {
                    @Override
                    public void onFailure(JSONObject json) {
                        DBug.e("createPhoto.onFailure", json.toString());
                    }

                    @Override
                    public void onSuccess(JSONObject json) {
                        DBug.e("createPhoto.onSuccess", json.toString());
                        try {
                            String url = json.getJSONObject("response").getJSONObject("photo").getString("url");
                            Message msg = new Message();
                            msg.type = Message.TYPE_IMAGE;
                            msg.content = fileData;
                            msg.fileURL = url;
                            mChatView.sendMessage(msg, originalData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void update(Observable observable, final Object data) {
        if (data instanceof IMManager.UpdateType && ((IMManager.UpdateType) data).equals(IMManager.UpdateType.Topic)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (mChat.topic != null && mAppBar.getTextView() != null) {
                        mAppBar.getTextView().setText(
                                mChat.topic.getFromTable().topicName + "(" + mChat.topic.members().size() + ")");
                    }
                }
            });
        } else if (data instanceof AnIMUpdateTopicEventData) {
            runOnUiThread(new Runnable() {
                public void run() {
                    AnIMUpdateTopicEventData d = (AnIMUpdateTopicEventData) data;
                    if (mChat.topic != null && mAppBar.getTextView() != null) {
                        mAppBar.getTextView().setText(d.getTopicName() + "(" + mChat.topic.members().size() + ")");
                    }
                }
            });
        } else if (data instanceof AnIMRemoveClientsEventData || data instanceof AnIMAddClientsEventData) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (mChat.topic != null && mAppBar.getTextView() != null) {
                        mAppBar.getTextView().setText(
                                mChat.topic.getFromTable().topicName + "(" + mChat.topic.members().size() + ")");
                    }
                }
            });
        } else if (data instanceof SessionData) {
            runOnUiThread(new Runnable() {
                public void run() {
                    final SessionData d = (SessionData) data;
                    Log.e("sessionClosed notify groupId", d.getGroupId());

                    if (mChat.group != null && d.getGroupId().equals(mChat.group.groupId)) {
                        // 禁用聊天
                        mChatView.showReConnectCSBox(true);
                        mChatView.getReconnectBox().setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatWithNewCustomService(UserManager.getInstance(ChatActivity.this).getCurrentUser());
                            }
                        });
                    }
                }
            });
        } else if (data instanceof SessionUserData) {
            runOnUiThread(new Runnable() {
                public void run() {
                    final SessionUserData d = (SessionUserData) data;

                    if (mChat.group != null && d.getGroupId().equals(mChat.group.groupId)) {
                        Map<String, ChatUser> userMap = new HashMap<String, ChatUser>();
                        userMap.put(d.getUser().clientId,
                                new ChatUser(d.getUser().clientId, d.getUser().userName, null));
                        mChatView.setUserMap(userMap);
                    }
                }
            });
        }
    }
}
