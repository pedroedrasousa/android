package com.pedroedrasousa.engine.livewallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.view.SurfaceHolder;

import com.pedroedrasousa.engine.livewallpaper.GLWallpaperService;

public abstract class OpenGLES2WallpaperService extends GLWallpaperService {
	
	OpenGLES2Engine mEngine;

    @Override
    public Engine onCreateEngine() {
    	mEngine = new OpenGLES2Engine();
        return mEngine;
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
 
    class OpenGLES2Engine extends GLWallpaperService.GLEngine {
 
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            
            WallpaperRenderer engineRenderer;
 
            // Check if the system supports OpenGL ES 2.0
            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
            
            if (supportsEs2) {
            	engineRenderer = getNewRenderer();
            	// Request an OpenGL ES 2.0 compatible context and set the renderer
                setEGLContextClientVersion(2);
                setRenderer(engineRenderer);
            }
            else {
                // No OpenGL ES 2.0
                return;
            }
        }
    }

    public abstract WallpaperRenderer getNewRenderer();
}