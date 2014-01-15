package com.pedroedrasousa.engine;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.opengl.GLES20;

public class FrameBuffer {
	
	private static final int BYTES_PER_FLOAT = 4;
	
	private int[]	mFrameBuffer;
	private int[]	mDepthBuffer;
	private int[]	mTexture;
	private int		mWidth;
	private int		mHeight;
	
	private Renderer mRenderer;
	
	public int getTextureDataHandler() {
		return mTexture[0];
	}

	public FrameBuffer(Renderer renderer, int width, int height) {
		mRenderer = renderer;
		mWidth    = width;
		mHeight   = height;
    
		mFrameBuffer	= new int[1];
		mDepthBuffer	= new int[1];
		mTexture		= new int[1];
	
		// Generate
		GLES20.glGenFramebuffers(1, mFrameBuffer, 0);
		GLES20.glGenRenderbuffers(1, mDepthBuffer, 0);
		GLES20.glGenTextures(1, mTexture, 0);
	
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
	
		// Create an empty buffer
		int[] buf = new int[width * height];
		Buffer pixels = ByteBuffer.allocateDirect(buf.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asIntBuffer();
	
		// Generate the textures
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);
		
		// Create the render buffer and bind a 16-bit depth buffer
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBuffer[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight);
	}
	
	public void bind() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTexture[0], 0);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mDepthBuffer[0]);

		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
			return;
		
		GLES20.glViewport(0, 0, mWidth, mHeight);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);	
	}
	
	public void unbind() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
		GLES20.glViewport(0, 0, mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
	}
	
	public void destroy() {
		GLES20.glDeleteFramebuffers(1, mFrameBuffer, 0);
		GLES20.glDeleteRenderbuffers(1, mDepthBuffer, 0);
	}
	
	public int getFrameBuffer() {
		return mFrameBuffer[0];
	}
	
	
	public int getDepthBuffer() {
		return mDepthBuffer[0];
	}
	
	public int getTexture() {
		return mTexture[0];
	}
}
