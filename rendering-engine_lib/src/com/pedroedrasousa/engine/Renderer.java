package com.pedroedrasousa.engine;

import android.view.MotionEvent;
import android.view.View;
import android.opengl.*;

public interface Renderer extends GLSurfaceView.Renderer {
	public void onResume();
	public void onPause();
	public void onSurfaceDestroyed();
	public boolean onTouch(View view, MotionEvent event);
	public int getViewportWidth();
	public int getViewportHeight();
	public void onScreenOnOffToggled(boolean isScreenOn);
}
