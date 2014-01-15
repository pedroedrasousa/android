package com.pedroedrasousa.fifteenpuzzle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.pedroedrasousa.engine.EngineGLSurfaceView;
import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.fifteenpuzzle.R;

import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;


public class GameActivity extends Activity {
	
	private EngineGLSurfaceView	mGLSurfaceView;
	private FifteenPuzzle		mFifteenPuzzle;
	
	private SensorManager 		mSensorManager;
	private Sensor				mGyroSensor;
	
	private AdView				mAdView;
	private InterstitialAd		mInterstitial;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		String gameId1;
		String gameId2;

		Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	    	gameId1	= extras.getString("game_id1");
	    	gameId2	= extras.getString("game_id2");
	    } else {
	    	gameId1	= "classic";
	    	gameId2	= "4x4";
	    }
	    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_activity);
		
	    // No ads for <2.3 devices.
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
			// Create the adView.
			mAdView = new AdView(this);
			mAdView.setAdUnitId(getString(R.string.ad_view_unit_id_game_screen));
			mAdView.setAdSize(AdSize.BANNER);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			RelativeLayout layout = (RelativeLayout)findViewById(R.id.main_layout);
			layout.addView(mAdView, layoutParams);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					AdRequest adRequest = new AdRequest.Builder().build();
					mAdView.loadAd(adRequest);
				}
			}, 1000);
			
		    // Create the interstitial.
		    mInterstitial = new InterstitialAd(this);
		    mInterstitial.setAdUnitId(getString(R.string.interstitial_ad_view_unit_id));
		    AdRequest adRequest2 = new AdRequest.Builder().build();
		    mInterstitial.loadAd(adRequest2);
	    }

	    // For some reason devices using early android versions report wrong gyroscope data???
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
		    mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		    mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		}
	    

		mGLSurfaceView = (EngineGLSurfaceView)findViewById(R.id.gl_surface_view);
					    
	    // Check if the system supports OpenGL ES 2.0
	    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
	 
	    if (supportsEs2) {
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			
			mFifteenPuzzle = new FifteenPuzzle(this, mGLSurfaceView, displayMetrics.density);
			mFifteenPuzzle.setGameId(gameId1, gameId2);
			
	        // Request an OpenGL ES 2.0 compatible context and set the renderer
	        mGLSurfaceView.setEGLContextClientVersion(2);
	        mGLSurfaceView.setRenderer((Renderer)mFifteenPuzzle);
	        mGLSurfaceView.setTouchEventHandler(mFifteenPuzzle);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch ( item.getItemId() ) {   
	    case R.id.action_restart:
	    	restartCurrentGameAction();
	    	return true;
		case R.id.action_quit:
			super.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	  
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {

		super.onResume();
		
		if (mGLSurfaceView != null) {
			mGLSurfaceView.onResume();
		}
		
		if (mFifteenPuzzle!= null) {
			mFifteenPuzzle.onResume();
		}
		
		if (mGyroSensor != null) {
			mSensorManager.registerListener(mFifteenPuzzle, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		if (mAdView != null) {
			mAdView.resume();
		}
	}

	@Override
	protected void onPause() {
		
		if (mGLSurfaceView != null) {
			mGLSurfaceView.onPause();
		}

		if (mFifteenPuzzle!= null) {
			mFifteenPuzzle.onPause();
		}
		
		if (mGyroSensor != null) {
			mSensorManager.unregisterListener(mFifteenPuzzle);
		}

		if (mAdView != null) {
			mAdView.pause();
		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();
	}
	
	public void terminateWithInterstitial() {
	    if (mInterstitial != null && mInterstitial.isLoaded()) {
	    	mInterstitial.show();
	    }
		super.onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
	    
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        if( which == DialogInterface.BUTTON_NEGATIVE)
		        	GameActivity.this.terminateWithInterstitial();
		        else if( which == DialogInterface.BUTTON_NEUTRAL)
		        	restartCurrentGameAction();
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_end_game)
			.setNegativeButton(R.string.btn_quit, dialogClickListener)
			.setNeutralButton(R.string.btn_restart, dialogClickListener)			
			.setPositiveButton(R.string.btn_cancel, dialogClickListener).show();
	}
	
	private void restartCurrentGameAction() {
		Runnable r = new Runnable() {
    	    public void run() {
    	    	mFifteenPuzzle.restartCurrentGame();
    	    }
    	};
        mGLSurfaceView.queueEvent(r);
	}
}