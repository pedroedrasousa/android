package com.pedroedrasousa.wobblybubbles;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.util.DisplayMetrics;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.ads.*;
import com.appbrain.AppBrain;

import com.pedroedrasousa.engine.AppRater;
import com.pedroedrasousa.engine.EngineGLSurfaceView;
import com.pedroedrasousa.wobblybubbleslib.R;
import com.searchboxsdk.android.StartAppSearch;
import com.startapp.android.publish.StartAppAd;


public class MainActivity extends Activity {
	
	private static final boolean USE_APPBRAIN_ADS	= false;
	private static final boolean USE_ADMOB_ADS		= false;
	
	private WobblyBubbles		mEngineRenderer;

	private FrameLayout			mMainLayout;	
	private EngineGLSurfaceView	mGLSurfaceView;	
	private AdView				mAdView;
	private Button				mBtnSettings;
	private StartAppAd			mStartAppAd = new StartAppAd(this);
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// Must be called before setContentView().
		if (WobblyBubblesUtils.isFree(this)) {
			StartAppAd.init(this, getString(R.string.startapp_developer_id), getString(R.string.startapp_app_id));
			StartAppSearch.init(this, getString(R.string.startapp_developer_id), getString(R.string.startapp_app_id));
		}
		
		setContentView(R.layout.main_activity);
		StartAppSearch.showSearchBox(this);	// Must be called after setContentView().

		mGLSurfaceView = (EngineGLSurfaceView)findViewById(R.id.gl_surface_view);
		
		WobblyBubblesUtils.requestTapjoyConnect(this);

		if (WobblyBubblesUtils.isFree(this)) {
			if (USE_APPBRAIN_ADS)
				AppBrain.initApp(this);
		}
	    
	    // Check if the system supports OpenGL ES 2.0
	    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
	 
	    if (supportsEs2) {
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			
			mEngineRenderer = new WobblyBubbles(this);
			mEngineRenderer.setIsPreview(true);
			mEngineRenderer.setIsLiveWallpaper(false);
			
	        // Request an OpenGL ES 2.0 compatible context and set the renderer
	        mGLSurfaceView.setEGLContextClientVersion(2);
	        mGLSurfaceView.setRenderer(mEngineRenderer);
	        mGLSurfaceView.setOnTouchListener(mEngineRenderer);
	    }
	    else {
	        // OpenGL ES 1.x compatible
	        return;
	    }
	    
	    mBtnSettings = (Button)findViewById(R.id.button_settings);
	    
	    mBtnSettings.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				intent.putExtra("isLiveWallpaper", false);
			    startActivity(intent);
			}
		});
	}

	public void createAdMobView() {
		mAdView = new AdView(this, AdSize.BANNER, getString(R.string.ad_mob_unit_id));
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		mAdView.setLayoutParams(params);		
		mMainLayout = (FrameLayout)findViewById(R.id.main_layout);
		mMainLayout.addView(mAdView);

		final AdRequest mAdRequest = new AdRequest();
	    mAdView.loadAd(mAdRequest);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		try {
			EasyTracker.getInstance(this).activityStart(this);
		} catch (Exception e) {}
	}
	  
	@Override
	public void onStop() {
		super.onStop();
		try {
			EasyTracker.getInstance(this).activityStop(this);
		} catch (Exception e) {}
	}

	@Override
	@SuppressWarnings("unused")	
	protected void onResume() {
		super.onResume();
		
	    AppRater.app_launched(this, 3, 5);

		if (mGLSurfaceView != null) {
			mGLSurfaceView.onResume();
		}
		
		if (mEngineRenderer != null) {
			mEngineRenderer.onResume();
		}
		
		if (USE_ADMOB_ADS && WobblyBubblesUtils.isFree(this) && mAdView == null) {
			createAdMobView();
		}
		
		if (mStartAppAd != null) {
			mStartAppAd.onResume();
		}
	}

	@Override
	@SuppressWarnings("unused")	
	protected void onPause() {
		
		if (USE_ADMOB_ADS && WobblyBubblesUtils.isFree(this)) {
			// Destroy ad stuff
			if (mMainLayout != null) {
				mMainLayout.removeView(mAdView);
			}
			
			if (mAdView != null) {
				mAdView.stopLoading();
				mAdView.removeAllViews();
				mAdView.destroy();
				mAdView = null;
			}
		}
		
		if (mGLSurfaceView != null) {
			mGLSurfaceView.onPause();
		}

		if (mEngineRenderer!= null) {
			mEngineRenderer.onPause();
		}
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		if (WobblyBubblesUtils.isFree(this)) {
			mStartAppAd.showAd();
			if (USE_APPBRAIN_ADS) {
				AppBrain.getAds().maybeShowInterstitial(this);
			}
		}
	    finish();
	}
}