package com.pedroedrasousa.wobblybubbles;

import android.content.Context;
import android.util.Log;

import com.tapjoy.TapjoyConnect;

import com.pedroedrasousa.wobblybubbleslib.R;

public class WobblyBubblesUtils {

	public final static String	TAG = "WobblyBubbles";
	
	public static String getGATrackingId(Context context) {
		if (isFree(context)) {
			return context.getString(R.string.ga_tracking_id_free);
		} else {
			return context.getString(R.string.ga_tracking_id);
		}
	}
	
	public static void requestTapjoyConnect(Context context) {
		if (isFree(context)) {
			TapjoyConnect.requestTapjoyConnect(context, context.getString(R.string.tap_joy_app_id_free), context.getString(R.string.tap_joy_secret_key_free)); 
			Log.println(Log.INFO, TAG, "requestTapjoyConnect appID: " + context.getString(R.string.tap_joy_app_id_free));
		} else {
			TapjoyConnect.requestTapjoyConnect(context, context.getString(R.string.tap_joy_app_id), context.getString(R.string.tap_joy_secret_key)); 
			Log.println(Log.INFO, TAG, "requestTapjoyConnect appID: " + context.getString(R.string.tap_joy_app_id));
		}
	}
	
	public static boolean isFree(Context context) {
		return context.getApplicationContext().getPackageName().equals("com.pedroedrasousa.wobblybubbleslite");
	}
}
