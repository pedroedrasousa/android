package com.pedroedrasousa.wobblybubbles;

public class SettingsPreset {

	// Background Properties
	public String	mBgImage;
	public float	mBgBrightness;
	public boolean	mScrollingWallpaper;
	
	// Metaball properties
	public int		mNbrMeta;
	public int		mMetaSize;	
	public int		mColorPalette;
	public float	mReflectionFactor;
	public float	mMetaTransparencyFactor;
	
	// Performance settings
	public int		mFPSLimit;
	public boolean	mShowFPS;	
	public int		mDetail;
	
	// General settings
	public int		mRenderMode;	
	public int		mSpeedFactor;		
	public int		mZoomFactor;
	public float	mTrailFactor;
	public boolean	mReactToTouch;
	public boolean	mReactToSound;
	
	// Lighting properties
	public float	mAmbientLight;
	public float	mSpecularLight;
	public float	mMetaColorFactor;
	
	public String getBgImage() {
		return mBgImage;
	}
	
	public void setBgImage(String bgImage) {
		mBgImage = bgImage;
	}
	
	public float getBgBrightness() {
		return mBgBrightness;
	}
	
	public void setBgBrightness(float bgBrightness) {
		mBgBrightness = bgBrightness;
	}
	
	public boolean isScrollingWallpaper() {
		return mScrollingWallpaper;
	}
	
	public void setScrollingWallpaper(boolean scrollingWallpaper) {
		mScrollingWallpaper = scrollingWallpaper;
	}
	
	public int getNbrMeta() {
		return mNbrMeta;
	}
	
	public void setNbrMeta(int nbrMeta) {
		mNbrMeta = nbrMeta;
	}
	
	public int getMetaSize() {
		return mMetaSize;
	}
	
	public void setMetaSize(int metaSize) {
		mMetaSize = metaSize;
	}
	
	public int getColorPalette() {
		return mColorPalette;
	}
	
	public void setColorPalette(int colorPalette) {
		mColorPalette = colorPalette;
	}
	
	public float getReflectionFactor() {
		return mReflectionFactor;
	}
	
	public void setReflectionFactor(float reflectionFactor) {
		mReflectionFactor = reflectionFactor;
	}
	
	public float getMetaTransparencyFactor() {
		return mMetaTransparencyFactor;
	}
	
	public void setMetaTransparencyFactor(float metaTransparencyFactor) {
		mMetaTransparencyFactor = metaTransparencyFactor;
	}
	
	public int getFPSLimit() {
		return mFPSLimit;
	}
	
	public void setFPSLimit(int FPSLimit) {
		mFPSLimit = FPSLimit;
	}
	
	public boolean isShowFPS() {
		return mShowFPS;
	}
	
	public void setShowFPS(boolean showFPS) {
		mShowFPS = showFPS;
	}
	
	public int getDetail() {
		return mDetail;
	}
	
	public void setDetail(int detail) {
		mDetail = detail;
	}
	
	public int getRenderMode() {
		return mRenderMode;
	}
	
	public void setRenderMode(int renderMode) {
		mRenderMode = renderMode;
	}
	
	public int getSpeedFactor() {
		return mSpeedFactor;
	}
	
	public void setSpeedFactor(int speedFactor) {
		mSpeedFactor = speedFactor;
	}
	
	public int getZoomFactor() {
		return mZoomFactor;
	}
	
	public void setZoomFactor(int zoomFactor) {
		mZoomFactor = zoomFactor;
	}
	
	public float getTrailFactor() {
		return mTrailFactor;
	}
	
	public void setTrailFactor(float trailFactor) {
		mTrailFactor = trailFactor;
	}
	
	public boolean isReactToTouch() {
		return mReactToTouch;
	}
	
	public void setReactToTouch(boolean reactToTouch) {
		mReactToTouch = reactToTouch;
	}
	
	public boolean isReactToSound() {
		return mReactToSound;
	}
	
	public void setReactToSound(boolean reactToSound) {
		mReactToSound = reactToSound;
	}
	
	public float getAmbientLight() {
		return mAmbientLight;
	}
	
	public void setAmbientLight(float ambientLight) {
		mAmbientLight = ambientLight;
	}
	
	public float getSpecularLight() {
		return mSpecularLight;
	}
	
	public void setSpecularLight(float specularLight) {
		mSpecularLight = specularLight;
	}
	
	public float getMetaColorFactor() {
		return mMetaColorFactor;
	}
	
	public void setMetaColorFactor(float metaColorFactor) {
		mMetaColorFactor = metaColorFactor;
	}
}
