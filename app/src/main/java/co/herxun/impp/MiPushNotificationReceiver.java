package co.herxun.impp;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import co.herxun.impp.activity.SplashActivity;

import com.arrownock.push.MiPushReceiver;
import com.xiaomi.mipush.sdk.MiPushMessage;

public class MiPushNotificationReceiver extends MiPushReceiver {

	@Override
	public void onReceiveMessage(Context context, MiPushMessage message) {
		String payload = message.getContent();
		if (payload == null || payload.equals(""))
			return;
		
		JSONObject payloadJson = null;
		try {
			payloadJson = new JSONObject(payload);
		} catch (JSONException ex) {
			// mute
		}
		Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		if(payloadJson != null) {
	        intent.putExtra("payload", payloadJson.toString());
		}
		context.startActivity(intent);
	}
}
