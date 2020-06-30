package co.herxun.impp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class ApplySourceCodeActivity extends Activity {
    private AppBar appbar;
    private WebView webview;
    private RelativeLayout rlParentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_apply_source);
        appbar = (AppBar) findViewById(R.id.apply_source_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	onBackPressed();
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(getString(R.string.apply_source));
        appbar.getMenuItemView().setVisibility(View.GONE);

        rlParentView = (RelativeLayout) findViewById(R.id.apply_source_rlParentView);
        webview = (WebView) findViewById(R.id.apply_source_webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new MyWebViewClient());
        webview.getSettings().setDisplayZoomControls(false);
        webview.loadUrl("http://www.arrownock.com/apply");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
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
}
