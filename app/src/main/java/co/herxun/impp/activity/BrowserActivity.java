package co.herxun.impp.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class BrowserActivity extends BaseActivity {
    private AppBar appbar;
    private WebView webview;
    private RelativeLayout rlParentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_browser);

        String browserName = getIntent().getStringExtra("browser_name");
        String browserUrl = getIntent().getStringExtra("browser_url");

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
        appbar.getTextView().setText(browserName);
        appbar.getMenuItemView().setVisibility(View.GONE);

        rlParentView = (RelativeLayout) findViewById(R.id.rlParentView);
        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new MyWebViewClient());
        webview.addJavascriptInterface(new JsObject(), "injectedObject");

        if (browserUrl.startsWith("http")) {
            webview.loadUrl(browserUrl);
        } else if (browserUrl.startsWith("www")) {
            webview.loadUrl("http://" + browserUrl);
        } else {
            webview.loadUrl(browserUrl);
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
