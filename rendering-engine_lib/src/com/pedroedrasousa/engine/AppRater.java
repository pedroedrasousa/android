package com.pedroedrasousa.engine;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {

    public final static String SHARED_PREFS_NAME = "apprater";

    public static void app_launched(Context context, int days, int launches) {
    	
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
        
        // Check if app was already rated
        if (prefs.getBoolean("rated", false)) {
        	return ;
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }
        
        // Wait at least n days before opening
        if (launch_count >= launches) {
            if (System.currentTimeMillis() >= date_firstLaunch + (days * 24 * 60 * 60 * 1000)) {
                showRateDialog(context, editor);
            }
        }
        
        editor.commit();
    }
    
    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(context);
        final String appName = getAppName(context);
        
        dialog.setTitle("Rate " + appName);

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        TextView textView = new TextView(context);
        textView.setText("Hi, do you enjoy " + appName + "?\nIf so, please take a moment to rate it.\nThanks for your support!");
        textView.setPadding(10, 10, 10, 10);
        ll.addView(textView);
        
        Button b1 = new Button(context);
        b1.setText("Sure!\nI'd be glad to!");
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                if (editor != null) {
                    editor.putBoolean("rated", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });        
        ll.addView(b1);

        Button b2 = new Button(context);
        b2.setText("Nahhh...\nMaybe later...");
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Reset counters
            	editor.putLong("launch_count", 0);
            	editor.putLong("date_firstlaunch", 0);
            	editor.commit();
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(context);
        b3.setText("Don't you ever\ndare to bother me again!");
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("rated", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);        
        dialog.show();        
    }
    
    private static String getAppName(Context context) {
    	String appName = new String();
        try {
        	final PackageManager pm = context.getPackageManager();
        	ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);
        	appName = (String) pm.getApplicationLabel(ai);
		} catch (Exception e) {}
        
        return appName;
    }
}