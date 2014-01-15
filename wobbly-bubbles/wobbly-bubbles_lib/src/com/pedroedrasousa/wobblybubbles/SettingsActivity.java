package com.pedroedrasousa.wobblybubbles;

import yuku.ambilwarna.AmbilWarnaDialog;

import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import com.pedroedrasousa.engine.AppRater;
import com.pedroedrasousa.engine.EngineGLSurfaceView;
import com.pedroedrasousa.engine.Vec3;
import com.pedroedrasousa.wobblybubbleslib.R;
import com.searchboxsdk.android.StartAppSearch;
import com.startapp.android.publish.StartAppAd;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	
	private static int		RESULT_LOAD_IMAGE		= 1;
	private static float	MAX_COLOR_PALETTE_SIZE	= 5;
	
	private Editor					mSharedPrefsEditor;
	private ListPreference			mBgImagePref;
	private ListPreference			mColorPaletteListPref;
	private ListPreference			mPresetListPref;
	private ListPreference			mPreset2ListPref;
	private SharedPreferences		mSharedPrefs;
	
	private Toast					mToast;
	private EngineGLSurfaceView		mGLSurfaceView;
	private WobblyBubbles			mEngineRenderer;
	private boolean					mIsLiveWallpaper;
	
	public static SettingsPreset[]	mSettingsPreset;
	public static Vec3[][]			mColorPalettePresets;
	
	private GoogleAnalytics			mGaInstance;
	private Tracker					mGaTracker;
	
	private StartAppAd				mStartAppAd = new StartAppAd(this);
	
	static {
		/**
		 *  TODO: Presets would be better stored somewhere else
		 */
		mSettingsPreset = new SettingsPreset[17];
		
		// Wobbling in the Green
		mSettingsPreset[0] = new SettingsPreset();
		mSettingsPreset[0].mBgImage					= "leaves.jpg";
		mSettingsPreset[0].mBgBrightness			= 0.0f;
		mSettingsPreset[0].mTrailFactor				= 0.4f;
		mSettingsPreset[0].mNbrMeta					= 3;
		mSettingsPreset[0].mMetaSize				= 75;
		mSettingsPreset[0].mColorPalette			= 0;
		mSettingsPreset[0].mReflectionFactor		= 0.5f;
		mSettingsPreset[0].mMetaTransparencyFactor	= 0.4f;
		mSettingsPreset[0].mRenderMode				= 0;
		mSettingsPreset[0].mFPSLimit				= 30;
		mSettingsPreset[0].mDetail					= 12;
		mSettingsPreset[0].mZoomFactor				= 50;
		mSettingsPreset[0].mSpeedFactor				= 40;
		mSettingsPreset[0].mShowFPS					= false;
		mSettingsPreset[0].mScrollingWallpaper		= true;
		mSettingsPreset[0].mReactToTouch			= true;
		mSettingsPreset[0].mReactToSound			= true;
		mSettingsPreset[0].mAmbientLight			= 0.0f;
		mSettingsPreset[0].mSpecularLight			= 1.0f;
		mSettingsPreset[0].mMetaColorFactor			= 0.5f;

		// Purple &amp; Green in the Sky
		mSettingsPreset[1] = new SettingsPreset();
		mSettingsPreset[1].mBgImage					= "clouds.jpg";
		mSettingsPreset[1].mBgBrightness			= 0.0f;
		mSettingsPreset[1].mTrailFactor				= 0.4f;
		mSettingsPreset[1].mNbrMeta					= 4;
		mSettingsPreset[1].mMetaSize				= 70;
		mSettingsPreset[1].mColorPalette			= 5;
		mSettingsPreset[1].mReflectionFactor		= 0.4f;
		mSettingsPreset[1].mMetaTransparencyFactor	= 0.9f;
		mSettingsPreset[1].mRenderMode				= 0;
		mSettingsPreset[1].mFPSLimit				= 30;
		mSettingsPreset[1].mDetail					= 12;
		mSettingsPreset[1].mZoomFactor				= 50;
		mSettingsPreset[1].mSpeedFactor				= 30;
		mSettingsPreset[1].mShowFPS					= false;
		mSettingsPreset[1].mScrollingWallpaper		= true;
		mSettingsPreset[1].mReactToTouch			= true;
		mSettingsPreset[1].mReactToSound			= true;
		mSettingsPreset[1].mAmbientLight			= 0.5f;
		mSettingsPreset[1].mSpecularLight			= 1.0f;
		mSettingsPreset[1].mMetaColorFactor			= 0.5f;
		
		// Water
		mSettingsPreset[2] = new SettingsPreset();
		mSettingsPreset[2].mBgImage					= "water.jpg";
		mSettingsPreset[2].mBgBrightness			= 0.0f;
		mSettingsPreset[2].mTrailFactor				= 0.0f;
		mSettingsPreset[2].mNbrMeta					= 10;
		mSettingsPreset[2].mMetaSize				= 60;
		mSettingsPreset[2].mColorPalette			= 2;
		mSettingsPreset[2].mReflectionFactor		= 0.5f;
		mSettingsPreset[2].mMetaTransparencyFactor	= 0.8f;
		mSettingsPreset[2].mRenderMode				= 0;
		mSettingsPreset[2].mFPSLimit				= 30;
		mSettingsPreset[2].mDetail					= 12;
		mSettingsPreset[2].mZoomFactor				= 40;
		mSettingsPreset[2].mSpeedFactor				= 60;
		mSettingsPreset[2].mShowFPS					= false;
		mSettingsPreset[2].mScrollingWallpaper		= true;
		mSettingsPreset[2].mReactToTouch			= true;
		mSettingsPreset[2].mReactToSound			= true;
		mSettingsPreset[2].mAmbientLight			= 0.2f;
		mSettingsPreset[2].mSpecularLight			= 1.0f;
		mSettingsPreset[2].mMetaColorFactor			= 0.8f;

		// Two Lonely Bubbles
		mSettingsPreset[3] = new SettingsPreset();
		mSettingsPreset[3].mBgImage					= "droplets.jpg";
		mSettingsPreset[3].mBgBrightness			= 0.0f;
		mSettingsPreset[3].mTrailFactor				= 0.0f;
		mSettingsPreset[3].mNbrMeta					= 2;
		mSettingsPreset[3].mMetaSize				= 60;
		mSettingsPreset[3].mColorPalette			= 0;
		mSettingsPreset[3].mReflectionFactor		= 0.5f;
		mSettingsPreset[3].mMetaTransparencyFactor	= 0.4f;
		mSettingsPreset[3].mRenderMode				= 0;
		mSettingsPreset[3].mFPSLimit				= 30;
		mSettingsPreset[3].mDetail					= 13;
		mSettingsPreset[3].mZoomFactor				= 25;
		mSettingsPreset[3].mSpeedFactor				= 50;
		mSettingsPreset[3].mShowFPS					= false;
		mSettingsPreset[3].mScrollingWallpaper		= true;
		mSettingsPreset[3].mReactToTouch			= true;
		mSettingsPreset[3].mReactToSound			= true;
		mSettingsPreset[3].mAmbientLight			= 0.0f;
		mSettingsPreset[3].mSpecularLight			= 1.0f;
		mSettingsPreset[3].mMetaColorFactor			= 0.5f;
		
		// Green Goo
		mSettingsPreset[4] = new SettingsPreset();
		mSettingsPreset[4].mBgImage					= "leaves.jpg";
		mSettingsPreset[4].mBgBrightness			= 0.0f;
		mSettingsPreset[4].mTrailFactor				= 0.8f;
		mSettingsPreset[4].mNbrMeta					= 5;
		mSettingsPreset[4].mMetaSize				= 70;
		mSettingsPreset[4].mColorPalette			= 9;
		mSettingsPreset[4].mReflectionFactor		= 0.7f;
		mSettingsPreset[4].mMetaTransparencyFactor	= 0.25f;
		mSettingsPreset[4].mRenderMode				= 0;
		mSettingsPreset[4].mFPSLimit				= 30;
		mSettingsPreset[4].mDetail					= 12;
		mSettingsPreset[4].mZoomFactor				= 50;
		mSettingsPreset[4].mSpeedFactor				= 30;
		mSettingsPreset[4].mShowFPS					= false;
		mSettingsPreset[4].mScrollingWallpaper		= true;
		mSettingsPreset[4].mReactToTouch			= true;
		mSettingsPreset[4].mReactToSound			= true;
		mSettingsPreset[4].mAmbientLight			= 0.1f;
		mSettingsPreset[4].mSpecularLight			= 1.0f;
		mSettingsPreset[4].mMetaColorFactor			= 0.15f;
		
		// Cloudy
		mSettingsPreset[5] = new SettingsPreset();
		mSettingsPreset[5].mBgImage					= "clouds.jpg";
		mSettingsPreset[5].mBgBrightness			= 0.0f;
		mSettingsPreset[5].mTrailFactor				= 0.0f;
		mSettingsPreset[5].mNbrMeta					= 4;
		mSettingsPreset[5].mMetaSize				= 70;
		mSettingsPreset[5].mColorPalette			= 10;
		mSettingsPreset[5].mReflectionFactor		= 0.5f;
		mSettingsPreset[5].mMetaTransparencyFactor	= 0.5f;
		mSettingsPreset[5].mRenderMode				= 0;
		mSettingsPreset[5].mFPSLimit				= 30;
		mSettingsPreset[5].mDetail					= 13;
		mSettingsPreset[5].mZoomFactor				= 30;
		mSettingsPreset[5].mSpeedFactor				= 50;
		mSettingsPreset[5].mShowFPS					= false;
		mSettingsPreset[5].mScrollingWallpaper		= true;
		mSettingsPreset[5].mReactToTouch			= true;
		mSettingsPreset[5].mReactToSound			= true;
		mSettingsPreset[5].mAmbientLight			= 0.5f;
		mSettingsPreset[5].mSpecularLight			= 1.0f;
		mSettingsPreset[5].mMetaColorFactor			= 0.5f;

		// Pale Bubbles
		mSettingsPreset[6] = new SettingsPreset();
		mSettingsPreset[6].mBgImage					= "water.jpg";
		mSettingsPreset[6].mBgBrightness			= 0.0f;
		mSettingsPreset[6].mTrailFactor				= 0.4f;
		mSettingsPreset[6].mNbrMeta					= 3;
		mSettingsPreset[6].mMetaSize				= 35;
		mSettingsPreset[6].mColorPalette			= 0;
		mSettingsPreset[6].mReflectionFactor		= 0.8f;
		mSettingsPreset[6].mMetaTransparencyFactor	= 0.9f;
		mSettingsPreset[6].mRenderMode				= 0;
		mSettingsPreset[6].mFPSLimit				= 30;
		mSettingsPreset[6].mDetail					= 13;
		mSettingsPreset[6].mZoomFactor				= 30;
		mSettingsPreset[6].mSpeedFactor				= 50;
		mSettingsPreset[6].mShowFPS					= false;
		mSettingsPreset[6].mScrollingWallpaper		= true;
		mSettingsPreset[6].mReactToTouch			= true;
		mSettingsPreset[6].mReactToSound			= true;
		mSettingsPreset[6].mAmbientLight			= 0.0f;
		mSettingsPreset[6].mSpecularLight			= 1.0f;
		mSettingsPreset[6].mMetaColorFactor			= 0.1f;
		
		// Color Drops
		mSettingsPreset[7] = new SettingsPreset();
		mSettingsPreset[7].mBgImage					= "droplets.jpg";
		mSettingsPreset[7].mBgBrightness			= 0.0f;
		mSettingsPreset[7].mTrailFactor				= 0.9f;
		mSettingsPreset[7].mNbrMeta					= 5;
		mSettingsPreset[7].mMetaSize				= 40;
		mSettingsPreset[7].mColorPalette			= 0;
		mSettingsPreset[7].mReflectionFactor		= 1.0f;
		mSettingsPreset[7].mMetaTransparencyFactor	= 0.1f;
		mSettingsPreset[7].mRenderMode				= 0;
		mSettingsPreset[7].mFPSLimit				= 25;
		mSettingsPreset[7].mDetail					= 13;
		mSettingsPreset[7].mZoomFactor				= 50;
		mSettingsPreset[7].mSpeedFactor				= 30;
		mSettingsPreset[7].mShowFPS					= false;
		mSettingsPreset[7].mScrollingWallpaper		= true;
		mSettingsPreset[7].mReactToTouch			= true;
		mSettingsPreset[7].mReactToSound			= true;
		mSettingsPreset[7].mAmbientLight			= 0.8f;
		mSettingsPreset[7].mSpecularLight			= 0.2f;
		mSettingsPreset[7].mMetaColorFactor			= 0.5f;

		// Dark and Colorful
		mSettingsPreset[8] = new SettingsPreset();
		mSettingsPreset[8].mBgImage					= "water.jpg";
		mSettingsPreset[8].mBgBrightness			= -1.0f;
		mSettingsPreset[8].mTrailFactor				= 0.9f;
		mSettingsPreset[8].mNbrMeta					= 6;
		mSettingsPreset[8].mMetaSize				= 70;
		mSettingsPreset[8].mColorPalette			= 0;
		mSettingsPreset[8].mReflectionFactor		= 1.0f;
		mSettingsPreset[8].mMetaTransparencyFactor	= 0.2f;
		mSettingsPreset[8].mRenderMode				= 0;
		mSettingsPreset[8].mFPSLimit				= 30;
		mSettingsPreset[8].mDetail					= 13;
		mSettingsPreset[8].mZoomFactor				= 50;
		mSettingsPreset[8].mSpeedFactor				= 40;
		mSettingsPreset[8].mShowFPS					= false;
		mSettingsPreset[8].mScrollingWallpaper		= true;
		mSettingsPreset[8].mReactToTouch			= true;
		mSettingsPreset[8].mReactToSound			= true;
		mSettingsPreset[8].mAmbientLight			= 0.5f;
		mSettingsPreset[8].mSpecularLight			= 1.0f;
		mSettingsPreset[8].mMetaColorFactor			= 0.5f;
		
		// Dark Blue
		mSettingsPreset[9] = new SettingsPreset();
		mSettingsPreset[9].mBgImage					= "black.jpg";
		mSettingsPreset[9].mBgBrightness			= 0.0f;
		mSettingsPreset[9].mTrailFactor				= 0.6f;
		mSettingsPreset[9].mNbrMeta					= 5;
		mSettingsPreset[9].mMetaSize				= 60;
		mSettingsPreset[9].mColorPalette			= 10;
		mSettingsPreset[9].mReflectionFactor		= 0.0f;
		mSettingsPreset[9].mMetaTransparencyFactor	= 0.0f;
		mSettingsPreset[9].mRenderMode				= 0;
		mSettingsPreset[9].mFPSLimit				= 25;
		mSettingsPreset[9].mDetail					= 13;
		mSettingsPreset[9].mZoomFactor				= 50;
		mSettingsPreset[9].mSpeedFactor				= 50;
		mSettingsPreset[9].mShowFPS					= false;
		mSettingsPreset[9].mScrollingWallpaper		= true;
		mSettingsPreset[9].mReactToTouch			= true;
		mSettingsPreset[9].mReactToSound			= true;
		mSettingsPreset[9].mAmbientLight			= 0.0f;
		mSettingsPreset[9].mSpecularLight			= 0.1f;
		mSettingsPreset[9].mMetaColorFactor			= 0.15f;
		
		// Wobbly Pixels
		mSettingsPreset[10] = new SettingsPreset();
		mSettingsPreset[10].mBgImage				= "black.jpg";
		mSettingsPreset[10].mBgBrightness			= 0.0f;
		mSettingsPreset[10].mTrailFactor			= 0.0f;
		mSettingsPreset[10].mNbrMeta				= 3;
		mSettingsPreset[10].mMetaSize				= 50;
		mSettingsPreset[10].mColorPalette			= 0;
		mSettingsPreset[10].mReflectionFactor		= 0.0f;
		mSettingsPreset[10].mMetaTransparencyFactor	= 0.0f;
		mSettingsPreset[10].mRenderMode				= 4;
		mSettingsPreset[10].mFPSLimit				= 35;
		mSettingsPreset[10].mDetail					= 12;
		mSettingsPreset[10].mZoomFactor				= 25;
		mSettingsPreset[10].mSpeedFactor			= 40;
		mSettingsPreset[10].mShowFPS				= false;
		mSettingsPreset[10].mScrollingWallpaper		= false;
		mSettingsPreset[10].mReactToTouch			= true;
		mSettingsPreset[10].mReactToSound			= true;
		mSettingsPreset[10].mAmbientLight			= 0.25f;
		mSettingsPreset[10].mSpecularLight			= 1.0f;
		mSettingsPreset[10].mMetaColorFactor		= 0.5f;
		
		// Wobbly Wireframe
		mSettingsPreset[11] = new SettingsPreset();
		mSettingsPreset[11].mBgImage				= "black.jpg";
		mSettingsPreset[11].mBgBrightness			= 0.0f;
		mSettingsPreset[11].mTrailFactor			= 0.5f;
		mSettingsPreset[11].mNbrMeta				= 4;
		mSettingsPreset[11].mMetaSize				= 40;
		mSettingsPreset[11].mColorPalette			= 3;
		mSettingsPreset[11].mReflectionFactor		= 0.0f;
		mSettingsPreset[11].mMetaTransparencyFactor	= 0.0f;
		mSettingsPreset[11].mRenderMode				= 2;
		mSettingsPreset[11].mFPSLimit				= 25;
		mSettingsPreset[11].mDetail					= 14;
		mSettingsPreset[11].mZoomFactor				= 100;
		mSettingsPreset[11].mSpeedFactor			= 40;
		mSettingsPreset[11].mShowFPS				= false;
		mSettingsPreset[11].mScrollingWallpaper		= false;
		mSettingsPreset[11].mReactToTouch			= true;
		mSettingsPreset[11].mReactToSound			= true;
		mSettingsPreset[11].mAmbientLight			= 0.6f;
		mSettingsPreset[11].mSpecularLight			= 0.0f;
		mSettingsPreset[11].mMetaColorFactor			= 0.5f;

		// Poo
		mSettingsPreset[12] = new SettingsPreset();
		mSettingsPreset[12].mBgImage				= "water.jpg";
		mSettingsPreset[12].mBgBrightness			= -0.4f;
		mSettingsPreset[12].mTrailFactor			= 0.9f;
		mSettingsPreset[12].mNbrMeta				= 4;
		mSettingsPreset[12].mMetaSize				= 100;
		mSettingsPreset[12].mColorPalette			= 11;
		mSettingsPreset[12].mReflectionFactor		= 0.3f;
		mSettingsPreset[12].mMetaTransparencyFactor	= 0.1f;
		mSettingsPreset[12].mRenderMode				= 0;
		mSettingsPreset[12].mFPSLimit				= 30;
		mSettingsPreset[12].mDetail					= 12;
		mSettingsPreset[12].mZoomFactor				= 0;
		mSettingsPreset[12].mSpeedFactor			= 20;
		mSettingsPreset[12].mShowFPS				= false;
		mSettingsPreset[12].mScrollingWallpaper		= true;
		mSettingsPreset[12].mReactToTouch			= true;
		mSettingsPreset[12].mReactToSound			= true;
		mSettingsPreset[12].mAmbientLight			= 0.0f;
		mSettingsPreset[12].mSpecularLight			= 0.1f;
		mSettingsPreset[12].mMetaColorFactor		= 0.5f;		
		
		// High Detail
		mSettingsPreset[13] = new SettingsPreset();
		mSettingsPreset[13].mBgImage				= "leaves.jpg";
		mSettingsPreset[13].mBgBrightness			= 0.0f;
		mSettingsPreset[13].mTrailFactor			= 0.0f;
		mSettingsPreset[13].mNbrMeta				= 3;
		mSettingsPreset[13].mMetaSize				= 20;
		mSettingsPreset[13].mColorPalette			= 0;
		mSettingsPreset[13].mReflectionFactor		= 0.5f;
		mSettingsPreset[13].mMetaTransparencyFactor	= 0.4f;
		mSettingsPreset[13].mRenderMode				= 0;
		mSettingsPreset[13].mFPSLimit				= 35;
		mSettingsPreset[13].mDetail					= 25;
		mSettingsPreset[13].mZoomFactor				= 80;
		mSettingsPreset[13].mSpeedFactor			= 25;
		mSettingsPreset[13].mShowFPS				= false;
		mSettingsPreset[13].mScrollingWallpaper		= true;
		mSettingsPreset[13].mReactToTouch			= true;
		mSettingsPreset[13].mReactToSound			= true;
		mSettingsPreset[13].mAmbientLight			= 0.0f;
		mSettingsPreset[13].mSpecularLight			= 1.0f;
		mSettingsPreset[13].mMetaColorFactor		= 0.5f; 
		
		mSettingsPreset[14] = new SettingsPreset();
		mSettingsPreset[14].mBgImage				= "clouds.jpg";
		mSettingsPreset[14].mBgBrightness			= 0.0f;
		mSettingsPreset[14].mTrailFactor			= 0.2f;
		mSettingsPreset[14].mNbrMeta				= 6;
		mSettingsPreset[14].mMetaSize				= 15;
		mSettingsPreset[14].mColorPalette			= 0;
		mSettingsPreset[14].mReflectionFactor		= 0.5f;
		mSettingsPreset[14].mMetaTransparencyFactor	= 1.0f;
		mSettingsPreset[14].mRenderMode				= 0;
		mSettingsPreset[14].mFPSLimit				= 60;
		mSettingsPreset[14].mDetail					= 25;
		mSettingsPreset[14].mZoomFactor				= 100;
		mSettingsPreset[14].mSpeedFactor			= 20;
		mSettingsPreset[14].mShowFPS				= false;
		mSettingsPreset[14].mScrollingWallpaper		= true;
		mSettingsPreset[14].mReactToTouch			= true;
		mSettingsPreset[14].mReactToSound			= true;
		mSettingsPreset[14].mAmbientLight			= 0.6f;
		mSettingsPreset[14].mSpecularLight			= 1.0f;
		mSettingsPreset[14].mMetaColorFactor		= 0.5f;
		
		mSettingsPreset[15] = new SettingsPreset();
		mSettingsPreset[15].mBgImage				= "water.jpg";
		mSettingsPreset[15].mBgBrightness			= 0.0f;
		mSettingsPreset[15].mTrailFactor			= 0.2f;
		mSettingsPreset[15].mNbrMeta				= 4;
		mSettingsPreset[15].mMetaSize				= 15;
		mSettingsPreset[15].mColorPalette			= 0;
		mSettingsPreset[15].mReflectionFactor		= 0.5f;
		mSettingsPreset[15].mMetaTransparencyFactor	= 1.0f;
		mSettingsPreset[15].mRenderMode				= 0;
		mSettingsPreset[15].mFPSLimit				= 60;
		mSettingsPreset[15].mDetail					= 25;
		mSettingsPreset[15].mZoomFactor				= 100;
		mSettingsPreset[15].mSpeedFactor			= 25;
		mSettingsPreset[15].mShowFPS				= false;
		mSettingsPreset[15].mScrollingWallpaper		= true;
		mSettingsPreset[15].mReactToTouch			= true;
		mSettingsPreset[15].mReactToSound			= true;
		mSettingsPreset[15].mAmbientLight			= 0.0f;
		mSettingsPreset[15].mSpecularLight			= 1.0f;
		mSettingsPreset[15].mMetaColorFactor		= 0.1f;
		
		mSettingsPreset[16] = new SettingsPreset();
		mSettingsPreset[16].mBgImage				= "droplets.jpg";
		mSettingsPreset[16].mBgBrightness			= 0.0f;
		mSettingsPreset[16].mTrailFactor			= 0.0f;
		mSettingsPreset[16].mNbrMeta				= 8;
		mSettingsPreset[16].mMetaSize				= 1;
		mSettingsPreset[16].mColorPalette			= 0;
		mSettingsPreset[16].mReflectionFactor		= 0.5f;
		mSettingsPreset[16].mMetaTransparencyFactor	= 0.4f;
		mSettingsPreset[16].mRenderMode				= 0;
		mSettingsPreset[16].mFPSLimit				= 60;
		mSettingsPreset[16].mDetail					= 25;
		mSettingsPreset[16].mZoomFactor				= 100;
		mSettingsPreset[16].mSpeedFactor			= 25;
		mSettingsPreset[16].mShowFPS				= false;
		mSettingsPreset[16].mScrollingWallpaper		= true;
		mSettingsPreset[16].mReactToTouch			= true;
		mSettingsPreset[16].mReactToSound			= true;
		mSettingsPreset[16].mAmbientLight			= 0.0f;
		mSettingsPreset[16].mSpecularLight			= 1.0f;
		mSettingsPreset[16].mMetaColorFactor		= 0.5f;
		
		
		mColorPalettePresets = new Vec3[12][];
		// Red Green and Blue
		mColorPalettePresets[0] = new Vec3[3];
		mColorPalettePresets[0][0] = new Vec3(1.0f, 0.0f, 0.0f);
		mColorPalettePresets[0][1] = new Vec3(0.0f, 1.0f, 0.0f);
		mColorPalettePresets[0][2] = new Vec3(0.0f, 0.0f, 1.0f);
		
		// Red and Green
		mColorPalettePresets[1] = new Vec3[2];
		mColorPalettePresets[1][0] = new Vec3(1.0f, 0.0f, 0.0f);
		mColorPalettePresets[1][1] = new Vec3(0.0f, 1.0f, 0.0f);
		
		// Green and Blue
		mColorPalettePresets[2] = new Vec3[2];
		mColorPalettePresets[2][0] = new Vec3(0.0f, 1.0f, 0.0f);
		mColorPalettePresets[2][1] = new Vec3(0.0f, 0.0f, 1.0f);		
		
		// Blue and Red
		mColorPalettePresets[3] = new Vec3[2];
		mColorPalettePresets[3][0] = new Vec3(0.0f, 0.0f, 1.0f);
		mColorPalettePresets[3][1] = new Vec3(1.0f, 0.0f, 0.0f);
		
		// Yellow and Red
		mColorPalettePresets[4] = new Vec3[2];
		mColorPalettePresets[4][1] = new Vec3(1.0f, 1.0f, 0.0f);
		mColorPalettePresets[4][0] = new Vec3(1.0f, 0.0f, 0.0f);		
		
		// Purple and Green
		mColorPalettePresets[5] = new Vec3[2];
		mColorPalettePresets[5][0] = new Vec3(1.0f, 0.0f, 1.0f);
		mColorPalettePresets[5][1] = new Vec3(0.0f, 1.0f, 0.0f);

		// Orange and Blue
		mColorPalettePresets[6] = new Vec3[2];
		mColorPalettePresets[6][1] = new Vec3(1.0f, 0.5f, 0.0f);
		mColorPalettePresets[6][0] = new Vec3(0.0f, 0.0f, 1.0f);				
		
		// Black & White
		mColorPalettePresets[7] = new Vec3[2];
		mColorPalettePresets[7][0] = new Vec3(0.0f, 0.0f, 0.0f);
		mColorPalettePresets[7][1] = new Vec3(1.0f, 1.0f, 1.0f);
		
		// Red
		mColorPalettePresets[8] = new Vec3[2];
		mColorPalettePresets[8][0] = new Vec3(1.0f, 0.0f, 0.0f);
		mColorPalettePresets[8][1] = new Vec3(1.0f, 1.0f, 1.0f);
		
		// Green
		mColorPalettePresets[9] = new Vec3[2];
		mColorPalettePresets[9][0] = new Vec3(0.0f, 1.0f, 0.0f);
		mColorPalettePresets[9][1] = new Vec3(1.0f, 1.0f, 1.0f);
		
		// Blue
		mColorPalettePresets[10] = new Vec3[2];
		mColorPalettePresets[10][0] = new Vec3(0.0f, 0.0f, 1.0f);
		mColorPalettePresets[10][1] = new Vec3(1.0f, 1.0f, 1.0f);
		
		// Brown
		mColorPalettePresets[11] = new Vec3[2];
		mColorPalettePresets[11][0] = new Vec3(0.5f, 0.25f, 0.0f);
		mColorPalettePresets[11][1] = new Vec3(0.0f, 0.0f, 0.0f);
	}
	
	public static void randomizeProperties(Context context) {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(WobblyBubbles.SHARED_PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		Random rand = new Random();
		
		editor.putInt("specular_light", rand.nextInt(100));
		editor.putInt("trail_factor", rand.nextInt(100));
		
		int brightnessRand = rand.nextInt(10);
		
		if (brightnessRand < 2)
			editor.putInt("bg_brightness", -100 + rand.nextInt(150));
		else if (brightnessRand < 4)
			editor.putInt("bg_brightness", -100);
		else
			editor.putInt("bg_brightness", 0);
		
		editor.putString("nbr_bubbles", Integer.toString(rand.nextInt(10) + 1));
		editor.putInt("size", rand.nextInt(100));

		int paletteIdx = rand.nextInt(10);
		for (int i = 0; i < mColorPalettePresets[paletteIdx].length; i++) {
			editor.putFloat("color_palette_r" + (i + 1), mColorPalettePresets[paletteIdx][i].x);
			editor.putFloat("color_palette_g" + (i + 1), mColorPalettePresets[paletteIdx][i].y);
			editor.putFloat("color_palette_b" + (i + 1), mColorPalettePresets[paletteIdx][i].z);
		}
		editor.putInt("palette_size", mColorPalettePresets[paletteIdx].length);
		
		editor.putInt("dist_offset", rand.nextInt(100));		
		editor.putInt("transparency", rand.nextInt(100));	
		editor.putInt("ambient_light", rand.nextInt(100));
		editor.putInt("specular_light", rand.nextInt(100));
		editor.putInt("color_factor", rand.nextInt(100));				
		editor.commit();
	}
	
	public void setSettingsPreset(SettingsPreset settingsPreset) {
		
		if (settingsPreset == null) {
			return;
		}
		
		mSharedPrefsEditor.putString("bg_image", settingsPreset.mBgImage);
		if (mBgImagePref != null)
			mBgImagePref.setValue(settingsPreset.mBgImage);
		mSharedPrefsEditor.putInt("specular_light", (int)(settingsPreset.mBgBrightness * 100.0f));
		mSharedPrefsEditor.putInt("trail_factor", (int)(settingsPreset.mTrailFactor * 100.0f));
		mSharedPrefsEditor.putInt("bg_brightness", (int)(settingsPreset.mBgBrightness * 100.0f));
		mSharedPrefsEditor.putString("nbr_bubbles", Integer.toString(settingsPreset.mNbrMeta));
		mSharedPrefsEditor.putInt("size", settingsPreset.mMetaSize);
		setColorPalettePreset(settingsPreset.mColorPalette);
		mSharedPrefsEditor.putInt("dist_offset", (int)(settingsPreset.mReflectionFactor * 100.0f));		
		mSharedPrefsEditor.putInt("transparency", (int)(settingsPreset.mMetaTransparencyFactor * 100.0f));		
		mSharedPrefsEditor.putString("render_mode", Integer.toString(settingsPreset.mRenderMode));		
		mSharedPrefsEditor.putString("fps_limit", Integer.toString(settingsPreset.mFPSLimit));
		mSharedPrefsEditor.putInt("detail", settingsPreset.mDetail);
		mSharedPrefsEditor.putInt("zoom", settingsPreset.mZoomFactor);
		mSharedPrefsEditor.putInt("speed", settingsPreset.mSpeedFactor);
		mSharedPrefsEditor.putBoolean("show_fps", settingsPreset.mShowFPS);
		mSharedPrefsEditor.putBoolean("scrolling_wallpaper", settingsPreset.mScrollingWallpaper);
		mSharedPrefsEditor.putBoolean("react_scrolling", settingsPreset.mReactToTouch);
		mSharedPrefsEditor.putBoolean("react_to_sound", settingsPreset.mReactToSound);
		mSharedPrefsEditor.putInt("ambient_light", (int)(settingsPreset.mAmbientLight * 100.0f));
		mSharedPrefsEditor.putInt("specular_light", (int)(settingsPreset.mSpecularLight * 100.0f));
		mSharedPrefsEditor.putInt("color_factor", (int)(settingsPreset.mMetaColorFactor * 100.0f));				
		mSharedPrefsEditor.commit();
	}
	
	public void createSharedPrefsList() {
		setPreferenceScreen(null);
		int id = getResources().getIdentifier("preferences", "xml", getApplicationContext().getPackageName());
		addPreferencesFromResource(id);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		// Hide Open Live Wallpaper preferences if instructed to
		if ( getIntent().getBooleanExtra("isLiveWallpaper", true)) {
			
			Preference auxPref;
			PreferenceCategory notificationsCategory;
			
			notificationsCategory = (PreferenceCategory) findPreference("general");
			if (notificationsCategory != null) {
				auxPref = findPreference("sound_reaction");
				if (auxPref != null)  {
					notificationsCategory.removePreference(auxPref);				
				}
			}
			
			notificationsCategory = (PreferenceCategory) findPreference("other");		
			if (notificationsCategory != null) {
				auxPref = findPreference("lwp");
				if (auxPref != null)  {
					notificationsCategory.removePreference(auxPref);				
				}
			}
			
			notificationsCategory = (PreferenceCategory) findPreference("performance");
			if (notificationsCategory != null) {
				auxPref = findPreference("show_fps");
				if (auxPref != null)  {
					notificationsCategory.removePreference(auxPref);				
				}
			}
			
		    mIsLiveWallpaper = true;
		} else {
			try {
				findPreference("lwp").setOnPreferenceClickListener(this);
			} catch (NullPointerException e) { /* Do nothing */ }
		}
		
		try {
			findPreference("about").setOnPreferenceClickListener(this);
		} catch (NullPointerException e) { /* Do nothing */ }
		
		try {
			findPreference("full").setOnPreferenceClickListener(this);
		} catch (NullPointerException e) { /* Do nothing */ }
		
		try {
			findPreference("randomize").setOnPreferenceClickListener(this);
		} catch (NullPointerException e) { /* Do nothing */ }		
		
		try {
			mPresetListPref = (ListPreference)findPreference("preset");
			try {
				mPresetListPref.setValue( mSharedPrefs.getString("preset", "0") );
			} catch (Exception e) { /* Do nothing */ }
		} catch (NullPointerException e) { /* Do nothing */ }
		
		try {
			mPreset2ListPref = (ListPreference)findPreference("preset2");
			try {
				mPreset2ListPref.setValue( mSharedPrefs.getString("preset2", "0") );
			} catch (Exception e) { /* Do nothing */ }
		} catch (NullPointerException e) { /* Do nothing */ }
		
		try {
			mColorPaletteListPref = (ListPreference)findPreference("color_palette");
			mColorPaletteListPref.setOnPreferenceChangeListener(new ColorPaletteListPreferenceListener());
			if (mColorPaletteListPref.getValue() == null) {
				try {
					mColorPaletteListPref.setValue( mSharedPrefs.getString("color_palette", DefaultValues.COLOR_PALETTE) );
				} catch (Exception e) { /* Do nothing */		}
			}
			
		} catch (NullPointerException e) { /* Do nothing */ }
		
		try {
			findPreference("rate").setOnPreferenceClickListener(this);
		} catch (NullPointerException e) { /* Do nothing */ }
		
		try {
			mBgImagePref = (ListPreference)findPreference("bg_chooser");
			mBgImagePref.setOnPreferenceChangeListener(new BgImageListPreferenceListener());
			if (mBgImagePref.getValue() == null) {
				try {
					mBgImagePref.setValue( mSharedPrefs.getString("bg_image", DefaultValues.BG_IMAGE) );
				} catch (Exception e) { /* Do nothing */		}
			}
		} catch (NullPointerException e) { /* Do nothing */ }
	}
	
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// Hide Open Live Wallpaper preferences if instructed to
		if ( getIntent().getBooleanExtra("isLiveWallpaper", true) == false ) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		super.onCreate(savedInstanceState);
				
		// Must be called before setContentView().
		if (WobblyBubblesUtils.isFree(this)) {
			StartAppAd.init(this, getString(R.string.startapp_developer_id), getString(R.string.startapp_app_id));
			StartAppSearch.init(this, getString(R.string.startapp_developer_id), getString(R.string.startapp_app_id));
		}
		setContentView(R.layout.settings_activity);
		
		getPreferenceManager().setSharedPreferencesName(WobblyBubbles.SHARED_PREFERENCES_NAME);
		int id = getResources().getIdentifier("preferences", "xml", getApplicationContext().getPackageName());
		addPreferencesFromResource(id);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		mSharedPrefs		= getPreferenceManager().getSharedPreferences();
		mSharedPrefsEditor	= mSharedPrefs.edit();
		createSharedPrefsList();

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		
		mGLSurfaceView = (EngineGLSurfaceView)findViewById(R.id.gl_surface_view);
	    
	    // Check if the system supports OpenGL ES 2.0
	    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
	 
	    if (supportsEs2) {
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			
			mEngineRenderer = new WobblyBubbles(this);
			mEngineRenderer.setIsPreview(true);
			mEngineRenderer.setIsLiveWallpaper(mIsLiveWallpaper);
			
	        // Request an OpenGL ES 2.0 compatible context and set the renderer
	        mGLSurfaceView.setEGLContextClientVersion(2);
	        mGLSurfaceView.setRenderer(mEngineRenderer);
	        mGLSurfaceView.setOnTouchListener(mEngineRenderer);
	    }
	    else {
	        // OpenGL ES 1.x compatible
	        return;
	    }

		// Calculate seek bar layout width based on screen width in pixels
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
	    Point size = new Point();
	    size = getDisplaySize(display);
		// Gets linearlayout
		LinearLayout layout = (LinearLayout)findViewById(R.id.settings_layout);
		
	    View view_instance = (View)mGLSurfaceView;
	    LayoutParams params=(LayoutParams) view_instance.getLayoutParams();
		
	    if (size.x < size.y) {
			layout.setOrientation(LinearLayout.VERTICAL);
		    params.height = (int)((double)size.x * 0.55);

	    } else {
	    	layout.setOrientation(LinearLayout.HORIZONTAL);
	    	params.width = (int)((double)size.y * 0.7);
	    }
	    
	    view_instance.setLayoutParams(params);
	}
	
	@Override
	public void onStart() {
		super.onStart();
    	mGaInstance = GoogleAnalytics.getInstance( getApplicationContext() );
    	mGaTracker = mGaInstance.getTracker(WobblyBubblesUtils.getGATrackingId(this));
    	mGaTracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, "Settings").build());
	}
	
	@Override
	protected void onDestroy()	{
		super.onDestroy();
	}

	private void showAboutDialog() {
		String versionName = new String();
		String year = getResources().getString(R.string.year);
		String author = getResources().getString(R.string.author);
		
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		TextView aboutText1 = new TextView(this);
		TextView aboutText2 = new TextView(this);
		TextView aboutText3 = new TextView(this);
		TextView aboutText4 = new TextView(this);
		TextView emailText = new TextView(this);
		aboutText1.setText("Version " + versionName + "\n" + year + " " + author);
		aboutText2.setText(R.string.about);
		aboutText3.setText(R.string.about2);
		aboutText4.setText(R.string.url_apache);
		aboutText4.setMovementMethod(LinkMovementMethod.getInstance());
		emailText.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
		emailText.setText(R.string.email);
		
		LinearLayout aboutLayout = new LinearLayout(this);
		aboutLayout.setOrientation(LinearLayout.VERTICAL);
		aboutLayout.setPadding(10, 5, 0, 10);
		aboutLayout.addView(aboutText1);
		aboutLayout.addView(aboutText2);
		aboutLayout.addView(emailText);
		if (!aboutText3.getText().equals(""))
			aboutLayout.addView(aboutText3);
		if (!aboutText4.getText().equals(""))
			aboutLayout.addView(aboutText4);

		AlertDialog aboutDialog = new AlertDialog.Builder(this).create();
		aboutDialog.setTitle(R.string.app_name);			 
		aboutDialog.setView(aboutLayout);

		aboutDialog.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		aboutDialog.show();
	}

	private void showGetFullVersionDialog() {
	
		TextView aboutText1 = new TextView(this);

		aboutText1.setText("Sorry...\nThis feature is only avaliable in the full version of Wobbly Bubbles.");

		
		LinearLayout aboutLayout = new LinearLayout(this);
		aboutLayout.setOrientation(LinearLayout.VERTICAL);
		aboutLayout.setPadding(10, 5, 0, 10);
		aboutLayout.addView(aboutText1);


		AlertDialog aboutDialog = new AlertDialog.Builder(this).create();
		aboutDialog.setTitle(R.string.app_name);
		aboutDialog.setView(aboutLayout);

		aboutDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Get It!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GetFullVersionIntent();
				dialog.dismiss();
			}
		}); 

		aboutDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

	      public void onClick(DialogInterface dialog, int id) {
	    	  dialog.dismiss();
	    }}); 

		aboutDialog.show();
	}
	
	private void pickColor(final int idx) {

		int initialColor = mSharedPrefs.getInt("color" + idx, 0xffffffff);
				
		// initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
		// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {	    	 
		    	 mSharedPrefsEditor.putFloat("color_palette_r" + idx, (float)Color.red(color) / 255.0f);
		    	 mSharedPrefsEditor.putFloat("color_palette_g" + idx, (float)Color.green(color) / 255.0f);
		    	 mSharedPrefsEditor.putFloat("color_palette_b" + idx, (float)Color.blue(color) / 255.0f);

		    	 if (idx < MAX_COLOR_PALETTE_SIZE) {
		    		 pickColor(idx + 1);
		    	 } else {
		    		 mSharedPrefsEditor.putInt("palette_size", idx);
		    		 mSharedPrefsEditor.commit();
		    		 mToast.setText("Palette has " + idx + " colors");
		    		 mToast.show();
		    	 }
			}
			
			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
				if (idx == 1) {
					mToast.setText("Color palette unchanged");
		    		mToast.show();
		    		return;
				}
				mSharedPrefsEditor.putInt("palette_size", idx - 1);
				mSharedPrefsEditor.commit();
				mToast.setText("Palette has " + (idx - 1) + " colors");
	    		mToast.show();
			}
		});

		mToast.setText("Pick palette color #" + idx + "\nHit Done if no more colors are required");
		mToast.show();
		dialog.show();
	}
	
	private void GetFullVersionIntent() {
		Uri uri = Uri.parse("market://details?id=" + "com.pedroedrasousa.wobblybubbles");
		Intent goToPlayStore = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToPlayStore);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Couldn't launch Google Play Store", Toast.LENGTH_LONG).show();
		}
	}
		@Override
	public boolean onPreferenceClick(Preference preference) {
			
		String key = preference.getKey();
	
		if (key.equals("about")) {
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "about", null).build() );
			showAboutDialog();
			return true;
		}
		
		if (key.equals("full")) {
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "get_full", null).build() );
			GetFullVersionIntent();
			return true;
		}
		
		if (key.equals("randomize")) {
			SettingsActivity.randomizeProperties(getApplicationContext());
			mSharedPrefsEditor.putString("preset1", "-1");
			mSharedPrefsEditor.putString("preset2", "-1");
			mSharedPrefsEditor.commit();
			createSharedPrefsList();
			return true;
		}
		
		if (key.equals("lwp")) {
			try {
				mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "lwp", null).build() );

				Intent intent = new Intent();
				intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
				startActivity(intent);
				mToast.setText("Choose Wobbly Bubbles from the list to set the Live Wallpaper");
				mToast.show();
			} catch (Exception e) {
				e.printStackTrace();
				mToast.setText("Unable to start the Live Wallpaper Picker");
				mToast.show();
			}
			return true;
		}
		
		if (key.equals("rate")) {
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "rate", null).build() );
			
			Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
			Intent goToPlayStore = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(goToPlayStore);
				
				// Set the preferences to rated = true
		        SharedPreferences.Editor editor = this.getSharedPreferences(AppRater.SHARED_PREFS_NAME, 0).edit();
                if (editor != null) {
                    editor.putBoolean("rated", true);
                    editor.commit();
                }
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, "Couldn't launch Google Play Store", Toast.LENGTH_LONG).show();
			}
			
			return true;
		}

		return false;
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
	    	 Uri selectedImage = data.getData();
	    	 String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	    	 Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	    	 cursor.moveToFirst();
	 
	    	 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	    	 String picturePath = cursor.getString(columnIndex);
	    	 cursor.close();

	    	 mSharedPrefsEditor.putString("bg_image", picturePath);
	    	 mSharedPrefsEditor.commit();
	     }
	}
	
	private void setColorPalettePreset(int paletteIdx) {
		
		if (mColorPaletteListPref != null)
			mColorPaletteListPref.setValue(Integer.toString(paletteIdx));
		
		for (int i = 0; i < mColorPalettePresets[paletteIdx].length; i++) {
		   	 mSharedPrefsEditor.putFloat("color_palette_r" + (i + 1), mColorPalettePresets[paletteIdx][i].x);
		   	 mSharedPrefsEditor.putFloat("color_palette_g" + (i + 1), mColorPalettePresets[paletteIdx][i].y);
		   	 mSharedPrefsEditor.putFloat("color_palette_b" + (i + 1), mColorPalettePresets[paletteIdx][i].z);
		}
		
		mSharedPrefsEditor.putInt("palette_size", mColorPalettePresets[paletteIdx].length);
		mSharedPrefsEditor.commit();
	}
	
	private class ColorPaletteListPreferenceListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
        	
			if (((String)newValue).equals("custom")) {
    			pickColor(1);
			} else {
				int paletteIdx = Integer.parseInt((String)newValue);
				setColorPalettePreset(paletteIdx);
			}
			
			return true;
        }
    }
	
	private class BgImageListPreferenceListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
        	
			if (((String)newValue).equals("custom")) {
    			Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    			startActivityForResult(i, RESULT_LOAD_IMAGE);
    			//return false;	// Don't allow the preference to be changed to "custom" string
			} else if (((String)newValue).equals("-1")) {
				mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "custom_image", null).build() );
				showGetFullVersionDialog();
			} else {
		    	 mSharedPrefsEditor.putString("bg_image", (String)newValue);
		    	 mSharedPrefsEditor.commit();
			}
			
			return true;
        }
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		mEngineRenderer.onResume();
		
		if (WobblyBubblesUtils.isFree(this) && mStartAppAd != null) {
			mStartAppAd.onResume();
		}
	}
	
	@Override
	protected void onPause() {
		mEngineRenderer.onPause();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (WobblyBubblesUtils.isFree(this) && mStartAppAd != null && getIntent().getBooleanExtra("isLiveWallpaper", true)) {
			mStartAppAd.onBackPressed();
		}
		super.onBackPressed();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("preset")) {
			int value = Integer.parseInt((String)mSharedPrefs.getString("preset", "0"));
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "preset", (long)value).build() );
			if (value == -1) {
				return;
			} else if (value < 0) {
				GetFullVersionIntent();
				return;
			}
			setSettingsPreset(mSettingsPreset[value]);
			mSharedPrefsEditor.putString("preset2", "-1");
			mSharedPrefsEditor.commit();
			createSharedPrefsList();
		} else if (key.equals("preset2")) {
			int value = Integer.parseInt((String)mSharedPrefs.getString("preset2", "0"));
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "preset2", (long)value).build() );
			if (value == -1) {
				return;
			} else if (value < 0) {
				GetFullVersionIntent();
				return;
			}
			setSettingsPreset(mSettingsPreset[value]);
			mSharedPrefsEditor.putString("preset", "-1");
			mSharedPrefsEditor.commit();
			createSharedPrefsList();
		} else if (key.equals("detail")) {
			int value = mSharedPrefs.getInt("detail", 0);
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "detail", (long)value).build() );
			mToast.setDuration(Toast.LENGTH_LONG);
			if (value > 20) {
				mToast.setText("A level of detail of " + value + " isn't recommended for using in Livewallpaper mode!\nLower it if you experience battery drain.");
				mToast.show();
				mToast.setDuration(Toast.LENGTH_SHORT);
			}
		} else if (key.equals("trail_factor")) {
			int value = mSharedPrefs.getInt("trail_factor", 0);
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "trail_factor", (long)value).build() );
		} else if (key.equals("zoom")) {
			int value = mSharedPrefs.getInt("zoom", 0);
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "zoom", (long)value).build() );
		} else if (key.equals("speed")) {
			int value = mSharedPrefs.getInt("speed", 0);
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "speed", (long)value).build() );
		} else if (key.equals("show_fps")) {
			boolean value = mSharedPrefs.getBoolean("show_fps", false);
			mGaTracker.send( MapBuilder.createEvent("ui_action", "button_press", "show_fps", ((value == false)? 0l : 1l)).build() );
		}
	}
	
	@TargetApi(13)
	private static Point getDisplaySize(final Display display) {
	    final Point point = new Point();
	    try {
	        display.getSize(point);
	    } catch (java.lang.NoSuchMethodError ignore) { // Older device
	        point.x = display.getWidth();
	        point.y = display.getHeight();
	    }
	    return point;
	}
}