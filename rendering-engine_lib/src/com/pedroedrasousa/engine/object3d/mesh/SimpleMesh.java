package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import android.opengl.GLES20;

import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.SimpleShader;


public class SimpleMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;
	
	public SimpleMesh(VertexData vertexData, SimpleShader shaderProgram, boolean useVBOs) {
		super(vertexData, shaderProgram, useVBOs);
	}
	
	public SimpleMesh(VertexData vertexData, SimpleShader shaderProgram) {
		super(vertexData, shaderProgram);
	}
	
	public void render() {
		
		int stride = 3 * BYTES_PER_FLOAT;
		
		if (mVBOData != null) {
			mVBOData.bindVBO();
			mShaderProgram.setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			GLES20.glDrawElements(mRenderMode, mVertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			mVBOData.unbindVBO();
		} else {
			Buffer vertexBuffer	= mVertexData.getVertexBuffer();
			Buffer indexBuffer	= mVertexData.getIndexBuffer();
			vertexBuffer.position(0);
			mShaderProgram.setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			indexBuffer.position(0);
			GLES20.glDrawElements(mRenderMode, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
