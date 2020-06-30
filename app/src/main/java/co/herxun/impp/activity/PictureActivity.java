package co.herxun.impp.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.TouchImageView;

public class PictureActivity extends Activity {
	private AppBar mAppbar;
	private TouchImageView mTouchImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		mAppbar = (AppBar)findViewById(R.id.picture_app_bar);
		mAppbar.getLogoView().setImageResource(R.drawable.menu_back);
		mAppbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		mAppbar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mAppbar.getTextView().setVisibility(View.VISIBLE);
		mAppbar.getTextView().setText(R.string.chat_view_picture);
			
		mTouchImageView = (TouchImageView)findViewById(R.id.picture_touchImageView);
		if (getIntent().hasExtra("url")) {
		    String url = getIntent().getStringExtra("url");
		    ImageLoader.getInstance(this).DisplayImage(url, mTouchImageView, null, false);
		} else if (getIntent().hasExtra("msgId")) {
		    String msgId = getIntent().getStringExtra("msgId");
		    Message msg = new Message();
		    msg.msgId = msgId;
		    msg.currentClientId = IMManager.getInstance(this).getCurrentClientId();
		    msg = msg.getFromTable();
		    if (msg.content != null && msg.content.length > 0) {
		        Bitmap bitmap = BitmapFactory.decodeByteArray(msg.content, 0, msg.content.length);
		        mTouchImageView.setImageBitmap(bitmap);
		    }
		}
		
		
		
	}
	
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,R.anim.push_up_out);
	}
	
}
