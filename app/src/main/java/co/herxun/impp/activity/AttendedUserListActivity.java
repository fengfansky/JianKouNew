package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.adapter.AttendedUserListAdapter;
import co.herxun.impp.controller.EventManager;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.model.Event;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class AttendedUserListActivity extends Activity {
    private ListView listviewAttendedUsers;
    private AttendedUserListAdapter adapter;
    private EventManager eventManager;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attended_user_list);
        checkBundle();
        initView();
        initData();
    }

    private void checkBundle() {
        String eventId = getIntent().getStringExtra("event_id");
        eventManager = new EventManager(this, 0);
        event = eventManager.getEventByEventId(eventId);
    }

    private void initView() {
        AppBar appbar = (AppBar) findViewById(R.id.toolbar);
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
        appbar.getTextView().setText(R.string.annou_event_attended_user_list_title);

        listviewAttendedUsers = (ListView) findViewById(R.id.listview_attended_users);
        adapter = new AttendedUserListAdapter(this);
        listviewAttendedUsers.setAdapter(adapter);

        listviewAttendedUsers.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(AttendedUserListActivity.this, UserDetailActivity.class);
                i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT, adapter.getList().get(position).clientId);
                startActivity(i);
                overridePendingTransition(R.anim.push_up_in, android.R.anim.fade_out);
            }
        });
    }

    private void initData() {
        List<User> userList = new ArrayList<User>();
        String attendUsersId = event.attendedUserIds;
        if (attendUsersId.length() > 0) {
            String[] attendUsersIdArray = attendUsersId.split(",");
            for (int i = 0; i < attendUsersIdArray.length; i++) {
                String userId = attendUsersIdArray[i];
                User user = UserManager.getInstance(this).getUserByUserId(userId);
                userList.add(user);
            }
            adapter.applyData(userList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            initData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
