package com.pedroedrasousa.engine.object3d;

import java.nio.Buffer;

import android.opengl.GLES20;

public class VBOData {
	
	public static final int BYTES_PER_FLOAT = 4;
	public static final int BYTES_PER_SHORT = 2;
	
	protected VertexData mVertexData;
	protected int mVBO[] = new int[2];
	
	public VBOData(VertexData vertexData) {
		setVertexData(vertexData);
		loadVBOVertexData();
	}
	
	public void setVertexData(VertexData vertexData) {
		mVertexData = vertexData;
	}

	public VertexData getVertexData() {
		return mVertexData;
	}
	
	/**
	 * Must be invoked every time after OpenGL context is destroyed.
	 */
	public void loadVBOVertexData() {
		
		Buffer vertexBuffer	= mVertexData.getVertexBuffer();
		Buffer indexBuffer	= mVertexData.getIndexBuffer();
		
		// Generate two buffer objects, one for the vertex data, other for the indices.
		GLES20.glGenBuffers(2, mVBO, 0);
		
		vertexBuffer.position(0);
		// Create a new data store for the vertex data buffer.
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		indexBuffer.position(0);
		// Create a new data store for the index buffer.
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVBO[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * BYTES_PER_SHORT, indexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void bindVBO() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO[0]);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVBO[1]);
	}
	
	public void unbindVBO() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public int getVerticesVBO()	{ return mVBO[0];	}
	public int getIndicesVBO()	{ return mVBO[1];	}
}
