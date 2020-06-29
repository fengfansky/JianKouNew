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
import co.herxun.impp.adapter.UserListInRoomAdapter;
import co.herxun.impp.controller.RoomManager;
import co.herxun.impp.controller.RoomManager.GetAllUserInRoomCallback;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class RoomUserListActivity extends BaseActivity {
    private ListView roomUserListView;

    private UserListInRoomAdapter adapter;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_user_list);

        initData();
        initView();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        String roomId = bundle.getString(Constant.ROOM_ID);

        users = new ArrayList<User>();

        RoomManager roomManager = new RoomManager(RoomUserListActivity.this);
        roomManager.FetchAllUserInRoom(roomId, new GetAllUserInRoomCallback() {

            @Override
            public void onFinish(List<User> data) {
                adapter.applyData(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });

    }

    private void initView() {
        AppBar appbar = (AppBar) findViewById(R.id.room_user_list_appbar);
        appbar.getTextView().setText(R.string.room_list_name);
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        roomUserListView = (ListView) findViewById(R.id.room_user_list_listview);
        adapter = new UserListInRoomAdapter(this, users);
        roomUserListView.setAdapter(adapter);
        roomUserListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= roomUserListView.getHeaderViewsCount();

                if (adapter.getItem(position) instanceof User) {
                    User user = (User) adapter.getItem(position);
                    Intent i = new Intent(view.getContext(), UserDetailActivity.class);
                    i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT, user.clientId);
                    view.getContext().startActivity(i);
                    ((Activity) view.getContext())
                            .overridePendingTransition(R.anim.push_up_in, android.R.anim.fade_out);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
