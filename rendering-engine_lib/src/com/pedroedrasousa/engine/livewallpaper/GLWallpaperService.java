
package com.pedroedrasousa.engine.livewallpaper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.PowerManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public abstract class GLWallpaperService extends WallpaperService {

	public class GLEngine extends Engine 
		implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		class WallpaperGLSurfaceView extends GLSurfaceView {

			WallpaperGLSurfaceView(Context context) {
				super(context);
				setPreserveContext(this);
			}

			// Avoid destroying the surface (onSurfaceCreated will not be invoked)
			@TargetApi(11)
			private void setPreserveContext(Object view) {
				int sdkVersion = android.os.Build.VERSION.SDK_INT;
				if (sdkVersion >= 11 && view instanceof GLSurfaceView) {
					try {
						super.setPreserveEGLContextOnPause(true);
					} catch (Exception e) {}
				}
			}
			
			@Override
			public SurfaceHolder getHolder() {
				return getSurfaceHolder();
			}

			public void onDestroy() {			
				super.onDetachedFromWindow();
			}
		}

		private WallpaperGLSurfaceView	mSurfaceView;
		private boolean					mRendererHasBeenSet;	
		private WallpaperRenderer		mRendererEngine;
		private boolean					mIsScreenOn;
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			mSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
			setTouchEventsEnabled(true);
			mIsScreenOn = ((PowerManager) getSystemService(POWER_SERVICE)).isScreenOn();
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {

			super.onVisibilityChanged(visible);

			if (mRendererHasBeenSet) {

				if (visible) {
					mRendererEngine.onResume();
					mSurfaceView.onResume();
				} else {
					mRendererEngine.onPause();
					mSurfaceView.onPause();
				}
				
				PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				if (mIsScreenOn != (mIsScreenOn = powerManager.isScreenOn())) {
					mRendererEngine.onScreenOnOffToggled(mIsScreenOn);
				}
			}
		}

		@Override
		public void onDestroy() {
			mRendererEngine.onSurfaceDestroyed();
			setTouchEventsEnabled(false);
			mSurfaceView.onDestroy();
			super.onDestroy();
			mRendererEngine = null;
		}
		
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			mRendererEngine.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}
		
		public void onTouchEvent(MotionEvent event) {
			mRendererEngine.onTouch(null, event);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals("prefs_being_edited") == false) {
				return;
			}
			// If preferences are being changed, settings are active, resume the surface view.
			boolean preferencesBeingChanged = sharedPreferences.getBoolean("prefs_being_edited", false);
			if (preferencesBeingChanged) {
				mSurfaceView.onResume();
			} else  {
				mSurfaceView.onPause();
			}
		}
		
		protected void setRenderer(WallpaperRenderer renderer) {
			mRendererEngine = renderer;
			mRendererEngine.setIsPreview(isPreview());
			mSurfaceView.setRenderer((GLSurfaceView.Renderer)mRendererEngine);
			mRendererHasBeenSet = true;
		}

		protected void setEGLContextClientVersion(int version) {
			mSurfaceView.setEGLContextClientVersion(version);
		}
	}
}
