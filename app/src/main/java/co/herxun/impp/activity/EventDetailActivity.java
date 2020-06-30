package co.herxun.impp.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.controller.EventManager;
import co.herxun.impp.controller.EventManager.LikeCallback;
import co.herxun.impp.controller.EventManager.processEventCallback;
import co.herxun.impp.controller.EventManager.queryEventUsersCallback;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Event;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class EventDetailActivity extends Activity {
    private AppBar appbar;
    private Event event;
    private ImageView ivEventPhoto, ivPostLike, ivUserPhoto;
    private TextView tvEventUsername, tvEventCreateDate, tvSign, tv_startdate, tv_enddate, tvAddress, tvCost, tvUsers,
            tvEventInformation, tvPostPageview, tvLikeCount, tv_event_title;
    private LinearLayout rlLike, ll_contact;
    private RelativeLayout rl_users;
    private EventManager eventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        checkBundle();
        initView();
        initData();

        refresshAttendedUserIds();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        appbar = (AppBar) findViewById(R.id.wall_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_chat);

        ivEventPhoto = (ImageView) findViewById(R.id.iv_event_photo);
        ivUserPhoto = (ImageView) findViewById(R.id.iv_user_photo);
        ivPostLike = (ImageView) findViewById(R.id.iv_post_like);
        rl_users = (RelativeLayout) findViewById(R.id.rl_users);
        tvEventUsername = (TextView) findViewById(R.id.tv_username);
        tvEventCreateDate = (TextView) findViewById(R.id.tv_event_create_date);
        tvSign = (TextView) findViewById(R.id.tv_sign);
        tv_startdate = (TextView) findViewById(R.id.tv_startdate);
        tv_enddate = (TextView) findViewById(R.id.tv_enddate);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvCost = (TextView) findViewById(R.id.tv_cost);
        tvUsers = (TextView) findViewById(R.id.tv_users);
        tvEventInformation = (TextView) findViewById(R.id.tv_event_information);
        tv_event_title = (TextView) findViewById(R.id.tv_event_title);
        tvPostPageview = (TextView) findViewById(R.id.tv_post_pageview);
        tvLikeCount = (TextView) findViewById(R.id.tv_like_count);

        rlLike = (LinearLayout) findViewById(R.id.rl_like);
        ll_contact = (LinearLayout) findViewById(R.id.ll_contact);
    }

    private void checkBundle() {
        String eventId = getIntent().getStringExtra("event_id");
        eventManager = new EventManager(this, 0);
        event = eventManager.getEventByEventId(eventId);
    }

    private void initData() {
        setAttendedUser();
        setTime();
        setOthersInformation();
        setReadAndLike();
    }

    private void setOthersInformation() {
        tv_event_title.setText(event.eventTitle);
        tvEventUsername.setText(UserManager.getInstance(this).getUserByUserId(event.userId).userName);
        ImageLoader.getInstance(this).DisplayImage(
                UserManager.getInstance(this).getUserByUserId(event.userId).userPhotoUrl, ivUserPhoto,
                R.drawable.friend_default, true);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int newHeight = width / 2;
        ivEventPhoto.setLayoutParams(new LinearLayout.LayoutParams(width, newHeight));
        ImageLoader.getInstance(this).DisplayImage(event.eventPhotoUrl, ivEventPhoto, R.drawable.annou_default3, false);

        tvAddress.setText(event.address);
        if (event.eventCost.equals("0")) {
            tvCost.setText(getResources().getString(R.string.annou_event_free));
        } else {
            tvCost.setText(event.eventCost + getResources().getString(R.string.annou_event_unit));
        }

        tvEventInformation.setText(event.eventInformation);
        ll_contact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(EventDetailActivity.this, UserDetailActivity.class);
                User user = UserManager.getInstance(EventDetailActivity.this).getUserByUserId(event.userId);
                i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT, user.clientId);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });

        rl_users.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EventDetailActivity.this, AttendedUserListActivity.class);
                i.putExtra("event_id", event.eventId);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });
    }

    private void setReadAndLike() {
        if (event.isRead) {
            tvPostPageview.setText("" + event.pageview);
        } else {
            event.isRead = true;
            event.pageview = event.pageview + 1;
            event.save();
            tvPostPageview.setText("" + event.pageview);
            eventManager.updateEventPageView(event);
        }

        tvLikeCount = (TextView) findViewById(R.id.tv_like_count);
        tvLikeCount.setText("" + event.likeCount);
        final boolean hasLiked = event.myLike(UserManager.getInstance(this).getCurrentUser()) != null;
        setLikeBtnStatus(hasLiked);
        setLikeBtnClick();
    }

    private void setTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(event.createdAt);
        tvEventCreateDate.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(c.getTime()));
        long starttime = event.startTime;
        Calendar starttimeCalendar = Calendar.getInstance();
        starttimeCalendar.setTimeInMillis(starttime);
        tv_startdate.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(starttimeCalendar.getTime()));
        long endtime = starttime + event.duration;
        Calendar endtimeCalendar = Calendar.getInstance();
        endtimeCalendar.setTimeInMillis(endtime);
        tv_enddate.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(endtimeCalendar.getTime()));

        Calendar nowCalendar = Calendar.getInstance();
        appbar.getTextViewClose().setTextColor(getResources().getColor(R.color.no5));
        if (starttimeCalendar.after(nowCalendar)) {
            tvSign.setBackgroundColor(getResources().getColor(R.color.no1));
            tvSign.setText(getResources().getString(R.string.annou_event_not_start));
        } else if (endtimeCalendar.before(nowCalendar)) {
            tvSign.setBackgroundColor(getResources().getColor(R.color.no15));
            tvSign.setText(getResources().getString(R.string.annou_event_finished));
            appbar.getTextViewClose().setEnabled(false);
            appbar.getTextViewClose().setTextColor(getResources().getColor(R.color.no7));
        } else {
            tvSign.setBackgroundColor(getResources().getColor(R.color.no1));
            tvSign.setText(getResources().getString(R.string.annou_event_ongoing));
        }
    }

    private void setAttendedUser() {
        appbar.getTextViewClose().setVisibility(View.VISIBLE);
        appbar.getTextViewClose().setTextColor(getResources().getColor(R.color.no5));
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56));
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        appbar.getTextViewClose().setLayoutParams(rlp);

        if (event.isAttend(UserManager.getInstance(this).getCurrentUser().userId)) {
            appbar.getTextViewClose().setText(getResources().getString(R.string.annou_event_quit));
            appbar.getTextViewClose().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    quitEvent();
                }
            });
        } else {
            appbar.getTextViewClose().setText(getResources().getString(R.string.annou_event_attend));
            appbar.getTextViewClose().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    attendEvent();
                }
            });
        }

        int attendedUsersLength = 0;
        if (event.attendedUserIds != null && event.attendedUserIds.length() > 0) {
            attendedUsersLength = event.attendedUserIds.split(",").length;
        }
        int userLimit = event.eventUserLimit;
        if (userLimit > 0) {
            if (userLimit > attendedUsersLength) {
                tvUsers.setText(attendedUsersLength + "/" + userLimit);
            } else {
                tvUsers.setText(attendedUsersLength + "/" + userLimit
                        + getResources().getString(R.string.annou_event_full));
                appbar.getTextViewClose().setEnabled(false);
                appbar.getTextViewClose().setTextColor(getResources().getColor(R.color.no7));
            }
        } else {
            tvUsers.setText(attendedUsersLength + "");
        }
    }

    private void setLikeBtnStatus(boolean hasLiked) {
        if (hasLiked) {
            ivPostLike.setImageResource(R.drawable.like_red);
        } else {
            ivPostLike.setImageResource(R.drawable.like_green);
        }
    }

    private void setLikeBtnClick() {
        rlLike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rlLike.setEnabled(false);
                boolean hasLiked = event.myLike(UserManager.getInstance(EventDetailActivity.this).getCurrentUser()) != null;
                setLikeBtnStatus(!hasLiked);
                if (eventManager != null) {
                    eventManager.triggerLikeButton(UserManager.getInstance(EventDetailActivity.this).getCurrentUser(),
                            event, new LikeCallback() {
                                @Override
                                public void onFailure(Event event) {
                                    rlLike.setEnabled(true);
                                    tvLikeCount.setText("" + event.likeCount);
                                    setLikeBtnClick();
                                }

                                @Override
                                public void onSuccess(Event event) {
                                    rlLike.setEnabled(true);
                                    tvLikeCount.setText("" + event.likeCount);
                                    setLikeBtnClick();
                                }
                            });
                }
            }
        });
    }

    private void attendEvent() {
        // showLoading();
        eventManager.attendEvent(event, new processEventCallback() {

            @Override
            public void onFinish(final boolean isOk) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        // dismissLoading();
                        if (isOk) {
                            if (event.attendedUserIds != null && event.attendedUserIds.length() > 0) {
                                event.attendedUserIds = event.attendedUserIds + ","
                                        + UserManager.getInstance(EventDetailActivity.this).getCurrentUser().userId;
                            } else {
                                event.attendedUserIds = UserManager.getInstance(EventDetailActivity.this)
                                        .getCurrentUser().userId;
                            }
                            event.save();
                            initData();
                        } else {
                            Toast.makeText(EventDetailActivity.this,
                                    getResources().getString(R.string.annou_event_operation_failed), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            }
        });

    }

    private void quitEvent() {
        // showLoading();
        eventManager.quitEvent(event, new processEventCallback() {

            @Override
            public void onFinish(final boolean isOk) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        // dismissLoading();
                        if (isOk) {
                            if (event.attendedUserIds.contains(","
                                    + UserManager.getInstance(EventDetailActivity.this).getCurrentUser().userId)) {
                                event.attendedUserIds = event.attendedUserIds
                                        .replace(("," + UserManager.getInstance(EventDetailActivity.this)
                                                .getCurrentUser().userId), "");
                            } else if (event.attendedUserIds.contains(UserManager.getInstance(EventDetailActivity.this)
                                    .getCurrentUser().userId + ",")) {
                                event.attendedUserIds = event.attendedUserIds
                                        .replace(
                                                (UserManager.getInstance(EventDetailActivity.this).getCurrentUser().userId + ","),
                                                "");
                            } else {
                                event.attendedUserIds = event.attendedUserIds
                                        .replace(
                                                (UserManager.getInstance(EventDetailActivity.this).getCurrentUser().userId),
                                                "");
                            }
                            event.save();
                            initData();
                        } else {
                            Toast.makeText(EventDetailActivity.this,
                                    getResources().getString(R.string.annou_event_operation_failed), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            }
        });
    }

    private void refresshAttendedUserIds() {
        eventManager.refresshAttendedUserIds(event, new queryEventUsersCallback() {
            @Override
            public void onFailure() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(EventDetailActivity.this,
                                getResources().getString(R.string.annou_event_operation_failed), Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            @Override
            public void onFinish(final List<User> users) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String userIds = "";
                        for (User user : users) {
                            userIds = userIds + user.userId + ",";
                        }
                        if (userIds.length() > 0) {
                            userIds = userIds.substring(0, userIds.length() - 1);
                        }
                        event.attendedUserIds = userIds;
                        event.save();
                        setAttendedUser();
                        setTime();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
