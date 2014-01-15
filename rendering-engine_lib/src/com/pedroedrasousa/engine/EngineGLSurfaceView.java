package com.pedroedrasousa.engine;

import android.util.AttributeSet;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class EngineGLSurfaceView extends GLSurfaceView {
	
	private OnTouchListener mOnTouchListener;

	public EngineGLSurfaceView(Context context)  {
		super(context);	
	}
	
	public EngineGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}

	public void setRenderer(Renderer renderer)  {
		super.setRenderer(renderer);
	}
	
	public void setTouchEventHandler(OnTouchListener touchListener) {
		mOnTouchListener = touchListener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mOnTouchListener != null)
			mOnTouchListener.onTouch(null, event);
		super.onTouchEvent(event);
		return true;
	}
}
