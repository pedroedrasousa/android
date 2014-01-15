package com.pedroedrasousa.fifteenpuzzle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Score {

	public static int getValue(Activity activity, String gameId1, String gameId2, String metric) {
		SharedPreferences sharedPref = activity.getSharedPreferences("fifteen_shared", Context.MODE_PRIVATE);
		int defaultValue = activity.getResources().getInteger(R.integer.default_score_time);
		int value = sharedPref.getInt(gameId1 + "_" + gameId2 + "_" + metric, defaultValue);
		return value;
	}
	
	public static void updateValue(Activity activity, String gameId1, String gameId2, String metric, int value) {
		SharedPreferences sharedPref = activity.getSharedPreferences("fifteen_shared", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(gameId1 + "_" + gameId2 + "_" + metric, value);
		editor.commit();
	}
}
