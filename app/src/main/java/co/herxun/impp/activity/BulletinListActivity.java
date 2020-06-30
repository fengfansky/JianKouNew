package co.herxun.impp.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.controller.BulletinManager;
import co.herxun.impp.model.Bulletin;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.BulletinView;

public class BulletinListActivity extends Activity {
    private BulletinView bulletinView;
    private TextView noBulletinLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_list);
        initView();
        initData();
    }

    private void initView() {
        AppBar appbar = (AppBar) findViewById(R.id.bulletin_app_bar);
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
        appbar.getTextView().setText(R.string.annou_bulletin_list_name);

        bulletinView = (BulletinView) findViewById(R.id.bulletinView);
        noBulletinLabel = (TextView) findViewById(R.id.noBulletinLabel);
    }

    private void initData() {
        BulletinManager bulletinManager = new BulletinManager(this);
        bulletinView.setBulletinManager(bulletinManager);
        List<Bulletin> list = bulletinManager.getLocalBulletins();
        if (list.isEmpty()) {
            setNoData();
        } else {
            setHasData();
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

    public void setNoData() {
        bulletinView.setVisibility(View.GONE);
        noBulletinLabel.setVisibility(View.VISIBLE);
    }

    public void setHasData() {
        bulletinView.setVisibility(View.VISIBLE);
        noBulletinLabel.setVisibility(View.GONE);
    }
}
