package com.pedroedrasousa.engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.pedroedrasousa.engine.shader.SimpleShaderProg;
import com.pedroedrasousa.renderingengine.R;

public class Font {
	
	private final float		TAB_FACTOR		= 4.0f;
	private final float		SPACE_FACTOR	= 0.25f;
	
	private final int		NBR_H_CHARS		= 16;
	private final int		NBR_V_CHARS		= 16;
	
	private float			mScaleFactor = 1.0f;

	private int				mHInterval;
	private int				mVInterval;

	private Texture			mTexture;
	
	private int[]			mCharPxStart;	// Pixel where the character starts in its bitmap space.
	private int[]			mCharPxEnd;		// Pixel where the character ends in its bitmap space.
	
	private FloatBuffer		mVertexBuffer;		// Every char will use the same vertex coordinates
	private FloatBuffer[]	mTexCoordBuffer;	// Texture coordinates vary per char

	// Shader and attribute handlers
	private SimpleShaderProg	mShaderProgram;
	private int				mMVPMatrixHdl;
	private int				mMVMatrixHdl;
	private int				mVertexPosHdl;
	private int				mTexMapHdl;
	private int				mTexCoordHdl;

	private float[]			mMVPMatrix = new float[16];
	private float[]			mMVMatrix  = new float[16];
	
	public Font(Context context, int fontResourceID, float scaleFactor) {
		
		setScaleFactor(scaleFactor);
		
		mTexCoordBuffer = new FloatBuffer[NBR_H_CHARS * NBR_V_CHARS];

		mCharPxStart	= new int[NBR_H_CHARS * NBR_V_CHARS];
		mCharPxEnd		= new int[NBR_H_CHARS * NBR_V_CHARS];
		
		// Create the shader program and get the handlers
		mShaderProgram = new SimpleShaderProg(context, R.raw.font_vert, R.raw.font_frag);
		mMVPMatrixHdl	= mShaderProgram.getUniformLocation("uMVPMatrix");
		mMVMatrixHdl	= mShaderProgram.getUniformLocation("uMVMatrix");
        mVertexPosHdl	= mShaderProgram.getAttribLocation("aVertPos");
        mTexMapHdl		= mShaderProgram.getUniformLocation("uTexture");
        mTexCoordHdl	= mShaderProgram.getAttribLocation("aTexCoord");
        
        // Load the font texture        
        mTexture = new Texture();
        mTexture.loadFromResourceId(context, fontResourceID, -1, -1);
		
		InputStream is = context.getResources().openRawResource(fontResourceID);
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			is = null;
		}
		
		// Get the width and height, in pixels, of the chars.
		mHInterval = bitmap.getWidth() / NBR_H_CHARS;
		mVInterval = bitmap.getHeight() / NBR_V_CHARS;
		
		// Build an array with vertex coordinates
		// Every letter will use the same vertex coordinates
		
		float[] vertices = new float[8];
		
		vertices[0] = 0.0f;					vertices[1] = (float)mVInterval * mScaleFactor;
		vertices[2] = (float)mHInterval * mScaleFactor;	vertices[3] = (float)mVInterval * mScaleFactor;
		vertices[4] = 0.0f;					vertices[5] = 0.0f;
		vertices[6] = (float)mHInterval * mScaleFactor;	vertices[7] = 0.0f;
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		
		float[] texture = new float[NBR_H_CHARS * NBR_V_CHARS * 8];	
		
		int index = 0;
		int buffIndex = 0;
		
		for(int y = 0; y < 16; y++) {
			for(int x = 0; x < 16; x++) {
				
				float u1 = (float)x * (float)mHInterval / (float)bitmap.getWidth();
				float v1 = (float)y * (float)mVInterval / (float)bitmap.getHeight();
				
				float u2 = u1 + ((float)mHInterval) / (float)bitmap.getWidth();
				float v2 = v1 + ((float)mVInterval) / (float)bitmap.getHeight();
			
				texture[index    ] = u1;	texture[index + 1] = v2;
				texture[index + 2] = u2;	texture[index + 3] = v2;
				texture[index + 4] = u1;	texture[index + 5] = v1;
				texture[index + 6] = u2;	texture[index + 7] = v1;

				byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
				byteBuf.order(ByteOrder.nativeOrder());
				mTexCoordBuffer[buffIndex] = byteBuf.asFloatBuffer();
				mTexCoordBuffer[buffIndex].put(texture);
				mTexCoordBuffer[buffIndex].position(0);
				
				// Get the character width in pixels
				int maxX = 0;
				int minX = mHInterval;
				
				for (int yDelta = 0; yDelta < mHInterval - 1; yDelta++) {				
					for (int xDelta = 0; xDelta < mHInterval - 1; xDelta++) {
						int color = bitmap.getPixel(x * mHInterval + xDelta, y * mVInterval + yDelta);
						if (Color.alpha(color) != 0) {
							
							//int alpha = Color.alpha(color);
							
							if (xDelta > maxX) {
								maxX = xDelta;
							}
							if (xDelta < minX) {
								minX = xDelta;
							}
						}
					}
				}
				
				mCharPxStart[buffIndex]	= minX;
				mCharPxEnd[buffIndex]	= maxX;
				
				buffIndex++;
			}
		}
	}

	public void enable(float w, float h) {
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		Matrix.orthoM(mMVPMatrix, 0, 0, w, h, 0, 0, 100);		
		
		mShaderProgram.useProgram();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture.getHandle());	     
        GLES20.glUniform1i(mTexMapHdl, 0);
        
        GLES20.glVertexAttribPointer(mVertexPosHdl, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mVertexPosHdl);

        GLES20.glEnableVertexAttribArray(mTexCoordHdl);
        
		GLES20.glUniformMatrix4fv(mMVPMatrixHdl, 1, false, mMVPMatrix, 0);
	}
	
	public void disable() {
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}
	
	public void Print(int x, int y, String strText) {
		if(strText == null)
			return;

		int len = strText.length();

		// Initial positioning.
		Matrix.setIdentityM(mMVMatrix, 0);
		Matrix.translateM(mMVMatrix, 0, x, y, 0);
		
		// Loop through every char in the string.
		for(int i = 0, line = 0; i < len; i++) {
			char c = strText.charAt(i);

			if(c == ' ') {
				Matrix.translateM(mMVMatrix, 0, mHInterval * SPACE_FACTOR * mScaleFactor, 0, 0);
			}
			else if(c == '\n') {
				line++;
				// Reset the Matrix and translate it to the current line.
				Matrix.setIdentityM(mMVMatrix, 0);
				Matrix.translateM(mMVMatrix, 0, x, y + (float)mVInterval * (float)line, 0.0f);
			}
			else if(c == '\t') {
				Matrix.translateM(mMVMatrix, 0, mHInterval * TAB_FACTOR * mScaleFactor, 0, 0);
			}
			else {
				Matrix.translateM(mMVMatrix, 0, -mCharPxStart[c] * mScaleFactor, 0.0f, 0.0f);									// Translate to compensate char left margin.
		        GLES20.glVertexAttribPointer(mTexCoordHdl, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer[c]);	// Set the texture coordinates according to the char.
		        GLES20.glUniformMatrix4fv(mMVMatrixHdl, 1, false, mMVMatrix, 0);								// Set the matrix.
				GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
				Matrix.translateM(mMVMatrix, 0, mCharPxEnd[c] * mScaleFactor, 0.0f, 0.0f);										// Translate matrix to the end of the char.
			}
		}
	}
	
	public float getVInterval() {
		return mVInterval * mScaleFactor * 0.75f;
	}
	
	public float getHInterval() {
		return mHInterval * mScaleFactor;
	}
	
	public float getScaleFactor() {
		return mScaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		mScaleFactor = scaleFactor;
	}
}

