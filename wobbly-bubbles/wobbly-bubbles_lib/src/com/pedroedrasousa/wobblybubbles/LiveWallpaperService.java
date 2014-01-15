package com.pedroedrasousa.wobblybubbles;

import com.pedroedrasousa.engine.livewallpaper.OpenGLES2WallpaperService;
import com.pedroedrasousa.engine.livewallpaper.WallpaperRenderer;

public class LiveWallpaperService extends OpenGLES2WallpaperService {
	
	private WobblyBubbles mRenderingEngine;

    @Override
    public WallpaperRenderer getNewRenderer() {
    	mRenderingEngine = new WobblyBubbles(this);
    	mRenderingEngine.setIsLiveWallpaper(true);
        return mRenderingEngine;
    }
}
