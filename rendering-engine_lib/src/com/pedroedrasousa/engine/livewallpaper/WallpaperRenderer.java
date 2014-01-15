package com.pedroedrasousa.engine.livewallpaper;

import com.pedroedrasousa.engine.Renderer;

public interface WallpaperRenderer extends Renderer {
	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset);
	public void setIsPreview(boolean isPreview);
}
