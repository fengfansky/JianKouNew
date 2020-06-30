package co.herxun.impp.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.EventDetailActivity;
import co.herxun.impp.controller.EventManager;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Event;
import co.herxun.impp.model.EventItem;
import co.herxun.impp.utils.Utils;

public class EventListAdapter extends BaseAdapter {
    private List<EventItem> eventList;
    private Context ct;

    public EventListAdapter(Context ct, EventManager eventManager) {
        this.ct = ct;
        eventList = new ArrayList<EventItem>();
    }

    public List<EventItem> getList() {
        return eventList;
    }

    public void applyData(List<EventItem> communities) {
        eventList.clear();
        eventList.addAll(communities);

        notifyDataSetChanged();
    }

    public void updateItem(int index, EventItem event) {
        eventList.remove(index);
        eventList.add(index, event);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public EventItem getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EventListItem view = (EventListItem) convertView;
        if (convertView == null) {
            view = new EventListItem(parent.getContext());
        }

        view.setData(eventList.get(position));

        return view;
    }

    public class EventListItem extends RelativeLayout {
        private CardView cardViewLeft, cardViewRight;
        private ImageView iv_event_left, iv_user_left, iv_event_right, iv_user_right;
        private TextView tv_event_title_left, tv_event_createdat_left, tv_username_left, tv_sign_left;
        private TextView tv_event_title_right, tv_event_createdat_right, tv_username_right, tv_sign_right;
        private RelativeLayout rl_left, rl_right;

        public EventListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_event_item, this);
            cardViewLeft = (CardView) findViewById(R.id.left_cardview);
            cardViewRight = (CardView) findViewById(R.id.right_cardview);
            iv_event_left = (ImageView) findViewById(R.id.iv_event_left);
            iv_user_left = (ImageView) findViewById(R.id.iv_user_left);
            iv_user_right = (ImageView) findViewById(R.id.iv_user_right);
            iv_event_right = (ImageView) findViewById(R.id.iv_event_right);
            iv_event_right = (ImageView) findViewById(R.id.iv_event_right);
            tv_event_title_left = (TextView) findViewById(R.id.tv_event_title_left);
            tv_event_createdat_left = (TextView) findViewById(R.id.tv_event_createdat_left);
            tv_username_left = (TextView) findViewById(R.id.tv_username_left);
            tv_sign_left = (TextView) findViewById(R.id.tv_sign_left);
            tv_event_title_right = (TextView) findViewById(R.id.tv_event_title_right);
            tv_event_createdat_right = (TextView) findViewById(R.id.tv_event_createdat_right);
            tv_username_right = (TextView) findViewById(R.id.tv_username_right);
            tv_sign_right = (TextView) findViewById(R.id.tv_sign_right);
            rl_left = (RelativeLayout) findViewById(R.id.rl_left);
            rl_right = (RelativeLayout) findViewById(R.id.rl_right);
        }

        public void setData(final EventItem eventItem) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int newWidth = width - Utils.px2Dp(ct, 20);
            int newHeight = newWidth / 2;
            if (eventItem.getLeftEvent() != null) {
                final Event event = eventItem.getLeftEvent();
                ImageLoader.getInstance(ct).DisplayImage(UserManager.getInstance(ct).getUserByUserId(event.userId).userPhotoUrl, iv_user_left, R.drawable.friend_default,
                        true);
                ImageLoader.getInstance(ct).DisplayImage(event.eventPhotoUrl, iv_event_left, R.drawable.annou_default3,
                        false);
                rl_left.setLayoutParams(new LinearLayout.LayoutParams(newHeight, newHeight));
                iv_event_left.setLayoutParams(new LayoutParams(newHeight, newHeight));
                tv_event_title_left.setText(event.eventTitle);
                tv_username_left.setText(UserManager.getInstance(ct).getUserByUserId(event.userId).userName);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(event.createdAt);
                tv_event_createdat_left.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(c.getTime()));
                long starttime = event.startTime;
                Calendar starttimeCalendar = Calendar.getInstance();
                starttimeCalendar.setTimeInMillis(starttime);
                long endtime = starttime + event.duration;
                Calendar endtimeCalendar = Calendar.getInstance();
                endtimeCalendar.setTimeInMillis(endtime);
                Calendar nowCalendar = Calendar.getInstance();
                if (starttimeCalendar.after(nowCalendar)) {
                    tv_sign_left.setBackgroundColor(ct.getResources().getColor(R.color.no1));
                    tv_sign_left.setText(getResources().getString(R.string.annou_event_not_start));
                } else if (endtimeCalendar.before(nowCalendar)) {
                    tv_sign_left.setBackgroundColor(ct.getResources().getColor(R.color.no15));
                    tv_sign_left.setText(getResources().getString(R.string.annou_event_finished));
                } else {
                    tv_sign_left.setBackgroundColor(ct.getResources().getColor(R.color.no1));
                    tv_sign_left.setText(getResources().getString(R.string.annou_event_ongoing));
                }
                cardViewLeft.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ct, EventDetailActivity.class);
                        intent.putExtra("event_id", event.eventId);
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    }
                });
            }
            if (eventItem.getRightEvent() != null) {
                cardViewRight.setVisibility(View.VISIBLE);
                final Event event = eventItem.getRightEvent();
                ImageLoader.getInstance(ct).DisplayImage(UserManager.getInstance(ct).getUserByUserId(event.userId).userPhotoUrl, iv_user_right, R.drawable.friend_default,
                        true);
                ImageLoader.getInstance(ct).DisplayImage(event.eventPhotoUrl, iv_event_right,
                        R.drawable.annou_default3, false);
                rl_right.setLayoutParams(new LinearLayout.LayoutParams(newHeight, newHeight));
                iv_event_right.setLayoutParams(new LayoutParams(newHeight, newHeight));
                tv_event_title_right.setText(event.eventTitle);
                tv_username_right.setText(UserManager.getInstance(ct).getUserByUserId(event.userId).userName);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(event.createdAt);
                tv_event_createdat_right.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(c.getTime()));
                long starttime = event.startTime;
                Calendar starttimeCalendar = Calendar.getInstance();
                starttimeCalendar.setTimeInMillis(starttime);
                long endtime = starttime + event.duration;
                Calendar endtimeCalendar = Calendar.getInstance();
                endtimeCalendar.setTimeInMillis(endtime);
                Calendar nowCalendar = Calendar.getInstance();
                if (starttimeCalendar.after(nowCalendar)) {
                    tv_sign_right.setBackgroundColor(ct.getResources().getColor(R.color.no1));
                    tv_sign_right.setText(getResources().getString(R.string.annou_event_not_start));
                } else if (endtimeCalendar.before(nowCalendar)) {
                    tv_sign_right.setBackgroundColor(ct.getResources().getColor(R.color.no15));
                    tv_sign_right.setText(getResources().getString(R.string.annou_event_finished));
                } else {
                    tv_sign_right.setBackgroundColor(ct.getResources().getColor(R.color.no1));
                    tv_sign_right.setText(getResources().getString(R.string.annou_event_ongoing));
                }
                cardViewRight.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ct, EventDetailActivity.class);
                        intent.putExtra("event_id", event.eventId);
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    }
                });
            } else {
                cardViewRight.setVisibility(View.INVISIBLE);
            }

        }
    }
}
