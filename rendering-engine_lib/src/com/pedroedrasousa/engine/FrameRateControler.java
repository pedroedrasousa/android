package com.pedroedrasousa.engine;

import android.os.SystemClock;

public class FrameRateControler {

	private static final int DEFAULT_MAX_FPS = 60;
	
	private long	mFrameStartTime;
	private long	mFrameEndTime;
	private float	mFrameDelta;		// Time between frames, in milliseconds.
	private long	mLastFPSUpdate;		// Last time FPS was updated
	private float   mOneOverFpsLimit;	// In milliseconds, used to limit frame rate.
	private int		mFPSCounter;		// Frames Per Second
	private float	mFrameFactor;
	
	public FrameRateControler() {
		setFrameRateLimiter(DEFAULT_MAX_FPS);
		mFrameFactor = 1.0f;
	}
	
	public float getFrameFactor() {
		return mFrameFactor;
	}
	
	public int getFPS() {
		return mFPSCounter;
	}
	
	public void setFrameRateLimiter(int maxFPS) {
		assert maxFPS > 0;
		if (maxFPS > 0)
			mOneOverFpsLimit = 1000.0f / maxFPS;
	}
	
	public void FrameStart() {
		mFrameEndTime	= SystemClock.uptimeMillis();
		mFrameDelta		= mFrameEndTime - mFrameStartTime;
		mFrameStartTime = SystemClock.uptimeMillis();
		
		// Frame rate limiter
		if (mFrameDelta < mOneOverFpsLimit) {
			try {
				Thread.sleep((int)mOneOverFpsLimit - (int)mFrameDelta);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Refresh once every second.
		if (mFrameEndTime - mLastFPSUpdate > 1000.0f) {
			mLastFPSUpdate = mFrameEndTime;
			mFPSCounter = (int)(1000.0f / mFrameDelta);
			
			// Get current frame factor.
			float currentFrameFactor = mFrameDelta / mOneOverFpsLimit;
			// Limit the frame factor value.
			currentFrameFactor = Math.min(currentFrameFactor, 1.5f);
			currentFrameFactor = Math.max(currentFrameFactor, 0.5f);
			
			// Gradually adjust frame factor.
			mFrameFactor = 0.75f * mFrameFactor + 0.25f * currentFrameFactor;
		}
	}
}
