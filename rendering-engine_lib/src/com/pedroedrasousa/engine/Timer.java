package com.pedroedrasousa.engine;

import java.util.concurrent.TimeUnit;

public class Timer {
	
	private static final int STATE_RUNNING	= 0;	
	private static final int STATE_STOPPED	= 1;
	private static final int STATE_PAUSED	= 2;

	private long	mStartTime;
	private long	mStopTime;
	private long	mCumulativeTime;
	private int		mState;
	
	public Timer() {
		reset();
	}
	
	public boolean isRunning() {
		return mState == STATE_RUNNING;
	}
	
	public void reset() {
		mStartTime		= 0;
		mStopTime		= 0;
		mCumulativeTime	= 0;
		mState			= STATE_STOPPED;
	}
	
	public void pause() {
		if (mState != STATE_RUNNING)
			return;
		mState = STATE_PAUSED;
		mCumulativeTime += System.currentTimeMillis() - mStartTime;
	}
	
	public void resume() {
		if (mState != STATE_PAUSED)
			return;
		mState = STATE_RUNNING;
		mStartTime = System.currentTimeMillis();
	}
	
	public void start() {
		mState = STATE_RUNNING;
		mStartTime = System.currentTimeMillis();
	}

	public void stop() {
		mState = STATE_STOPPED;
		mStopTime = System.currentTimeMillis();
	}
	
	public long getElapsedMs() {
		long ms;
		
		switch (mState) {
		case STATE_RUNNING:
			ms = System.currentTimeMillis() - mStartTime + mCumulativeTime;
			break;
		case STATE_PAUSED:
			ms = mCumulativeTime;
			break;
		case STATE_STOPPED:
			ms = mStopTime - mStartTime + mCumulativeTime;
			break;
		default:
			ms = 0;
		}
		
		return ms;
	}
	
	public String getElapsedTimeString() {
		long ms = getElapsedMs();
		
		int seconds = (int) (ms / 1000);
		int minutes = seconds / 60;
		seconds     = seconds % 60;
		
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	public int getElapsedMinutes() {
		return (int)(getElapsedMs() / 1000 / 60);
	}
	
	public int getElapsedSeconds() {
		return (int)(getElapsedMs() / 1000);
	}
	
	public static String msToStringMMSS(int ms) {
		return String.format("%02d:%02d", 
			TimeUnit.MILLISECONDS.toMinutes(ms) - 
			TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
			TimeUnit.MILLISECONDS.toSeconds(ms) - 
			TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}
}
