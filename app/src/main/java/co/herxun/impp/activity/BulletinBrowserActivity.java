package co.herxun.impp.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.controller.BulletinManager;
import co.herxun.impp.controller.BulletinManager.LikeCallback;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.model.Bulletin;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class BulletinBrowserActivity extends BaseActivity {
    private AppBar appbar;
    private WebView webview;
    private RelativeLayout rlParentView;
    private LinearLayout rlLike;
    private Bulletin bulletin;
    private TextView tvPostPageview, tvLikeCount;
    private ImageView ivPostLike;
    private BulletinManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_post_browser);

        String bulletinId = getIntent().getStringExtra("bulletinId");
        manager = new BulletinManager(this);
        bulletin = manager.getBulletinById(bulletinId);

        appbar = (AppBar) findViewById(R.id.create_post_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webview.canGoBack()) {
                    // 返回键退回
                    webview.goBack();
                    initClose();
                } else {
                    onBackPressed();
                }
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(bulletin.bulletinTitle);
        appbar.getMenuItemView().setVisibility(View.GONE);

        tvPostPageview = (TextView) findViewById(R.id.tv_post_pageview);
        if (bulletin.isRead) {
            tvPostPageview.setText(""+bulletin.pageview);
        } else {
            bulletin.isRead = true;
            bulletin.pageview = bulletin.pageview + 1;
            bulletin.save();
            tvPostPageview.setText(""+bulletin.pageview);
            manager.updateBulltinPageView(bulletin);
        }

        tvLikeCount = (TextView) findViewById(R.id.tv_like_count);
        tvLikeCount.setText(""+bulletin.likeCount);
        ivPostLike = (ImageView) findViewById(R.id.iv_post_like);

        rlParentView = (RelativeLayout) findViewById(R.id.rlParentView);
        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new MyWebViewClient());
        webview.addJavascriptInterface(new JsObject(), "injectedObject");
        webSettings.setDisplayZoomControls(false);
//         webSettings.setUseWideViewPort(true);
        // webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        if (bulletin.bulletinContent != null) {

            webview.getSettings().setDefaultTextEncodingName("utf-8");
            webview.loadData(getHtmlData(bulletin.bulletinContent), "text/html; charset=UTF-8", "utf-8");
        }

        final boolean hasLiked = bulletin.myLike(UserManager.getInstance(this).getCurrentUser()) != null;
        setLikeBtnStatus(hasLiked);
        rlLike = (LinearLayout) findViewById(R.id.rl_like);
        setLikeBtnClick();
    }
    
    private String getHtmlData(String bodyHTML) {
        String head = "<head><style>img{max-width: 100%; width:auto; height: auto;}</style></head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    private void setLikeBtnClick() {
        rlLike.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                rlLike.setEnabled(false);
                boolean hasLiked = bulletin.myLike(UserManager.getInstance(BulletinBrowserActivity.this)
                        .getCurrentUser()) != null;
                setLikeBtnStatus(!hasLiked);
                if (manager != null) {
                    manager.triggerLikeButton(UserManager.getInstance(BulletinBrowserActivity.this).getCurrentUser(),
                            bulletin, new LikeCallback() {
                                @Override
                                public void onFailure(Bulletin bulletin) {
                                    rlLike.setEnabled(true);
                                    tvLikeCount.setText(""+bulletin.likeCount);
                                    setLikeBtnClick();
                                }

                                @Override
                                public void onSuccess(Bulletin bulletin) {
                                    rlLike.setEnabled(true);
                                    tvLikeCount.setText(""+bulletin.likeCount);
                                    setLikeBtnClick();
                                }
                            });
                }
            }
        });
    }

    private void setLikeBtnStatus(boolean hasLiked) {
        if (hasLiked) {
//            tvLikeCount.setTextColor(getResources().getColor(R.color.no15));
            ivPostLike.setImageResource(R.drawable.like_red);
        } else {
//            tvLikeCount.setTextColor(getResources().getColor(R.color.no1));
            ivPostLike.setImageResource(R.drawable.like_green);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void initClose() {
        appbar.getTextViewClose().setVisibility(View.VISIBLE);
        appbar.getTextViewClose().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * 按键响应，在WebView中查看网页时，按返回键的时候按浏览历史退回,如果不做此项处理则整个WebView返回退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            initClose();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rlParentView.removeView(webview);
        webview.removeAllViews();
        webview.destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }

    class JsObject {
        @JavascriptInterface
        public String toString() {
            return "injectedObject";
        }
    }
}
