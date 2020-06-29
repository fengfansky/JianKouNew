package co.herxun.impp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import co.herxun.impp.R;
import co.herxun.impp.controller.CommunityManager;
import co.herxun.impp.model.Community;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.CommunityView;

public class CommunityListActivity extends BaseActivity {
    private CommunityView communityView;
    private ImageView addBtn;
    private TextView noCommunityLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);
        initView();
        initData();
    }

    private void initView() {
        AppBar appbar = (AppBar) findViewById(R.id.community_app_bar);
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
        appbar.getTextView().setText(R.string.annou_community_list_name);

        communityView = (CommunityView) findViewById(R.id.communityView);
        noCommunityLabel = (TextView) findViewById(R.id.noCommunityLabel);

        addBtn = (ImageView) findViewById(R.id.wall_addBtn);
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateCommunityActivity.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });
    }

    private void initData() {
        CommunityManager communityManager = new CommunityManager(this);
        communityView.setCommunityManager(communityManager);
        List<Community> list = communityManager.getLocalCommunities();
        if (list.isEmpty()) {
            setNoData();
        } else {
            setHasData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            initData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }

    public void setNoData() {
        communityView.setVisibility(View.GONE);
        noCommunityLabel.setVisibility(View.VISIBLE);
    }

    public void setHasData() {
        communityView.setVisibility(View.VISIBLE);
        noCommunityLabel.setVisibility(View.GONE);
    }
}
