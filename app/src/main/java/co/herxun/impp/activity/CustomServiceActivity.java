package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.adapter.MemberListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.DeskGroup;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

import com.arrownock.appo.desk.IAnDeskGetGroupsCallback;
import com.arrownock.exception.ArrownockException;

public class CustomServiceActivity extends BaseActivity {
    private ListView listView;
    private MemberListAdapter adapter;
    private Handler handler;
    private TextView noCSLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_service);

        initView();
        showLoading();
        initData();
    }

    private void initView() {
        noCSLabel = (TextView) findViewById(R.id.noCSLabel);
        AppBar appbar = (AppBar) findViewById(R.id.cs_app_bar);
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
        appbar.getTextView().setText(R.string.cs_vip_pro);

        listView = (ListView) findViewById(R.id.cs_listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chat chat = IMManager.getInstance(CustomServiceActivity.this)
                        .addChat(adapter.getItem(position));
                IMManager.getInstance(CustomServiceActivity.this).notifyChatUpdated();
                Intent i = new Intent(CustomServiceActivity.this, ChatActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
                i.putExtras(b);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });
    }

    private void initData() {
        handler = new Handler();
        adapter = new MemberListAdapter(this);
        listView.setAdapter(adapter);
        listView.setDivider(null);

        fetchGroup();
    }

    private void fetchGroup() {
        IMManager.getInstance(CustomServiceActivity.this).getAnDesk().getGroups(new IAnDeskGetGroupsCallback() {

            @Override
            public void onSuccess(List<com.arrownock.appo.desk.Group> deskGroupList) {
                final List<DeskGroup> data = new ArrayList<DeskGroup>();
                for (com.arrownock.appo.desk.Group deskGroup : deskGroupList) {
                    DeskGroup group = new DeskGroup();
                    group.groupId = deskGroup.getId();
                    group.groupName = deskGroup.getName();
                    group.currentClientId = UserManager.getInstance(CustomServiceActivity.this).getCurrentUser().clientId;
//                    group.groupPhotoUrl = deskGroup.g
                    group.update(UserManager.getInstance(CustomServiceActivity.this).getCurrentUser().clientId);
                    data.add(group);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoading();
                        adapter.applyData(data);
                        if (adapter.getCount() > 0) {
                            noCSLabel.setVisibility(View.GONE);
                        } else {
                            noCSLabel.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onFailure(ArrownockException e) {
                Log.e("anDesk getGroup faliure", e.getMessage());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoading();
                        noCSLabel.setVisibility(View.VISIBLE);
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
