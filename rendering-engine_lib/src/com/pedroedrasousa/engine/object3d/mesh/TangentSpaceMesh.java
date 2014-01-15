package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;

import android.opengl.GLES20;


public class TangentSpaceMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;

	public TangentSpaceMesh(VertexData vertexData, TangentSpaceShader shaderProgram, boolean useVBOs) {
		super(vertexData, shaderProgram, useVBOs);
	}
	
	public TangentSpaceMesh(VertexData vertexData, TangentSpaceShader shaderProgram) {
		super(vertexData, shaderProgram, true);
	}
	
	public void render() {
		
		int stride = 14 * BYTES_PER_FLOAT;
			
		if (mVBOData != null) {
			mVBOData.bindVBO();
			((TangentSpaceShader)mShaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			((TangentSpaceShader)mShaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 3*BYTES_PER_FLOAT);
			((TangentSpaceShader)mShaderProgram).setTangentAttribPointer(3, GLES20.GL_FLOAT, false, stride, 6*BYTES_PER_FLOAT);
			((TangentSpaceShader)mShaderProgram).setBinormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 9*BYTES_PER_FLOAT);
			((TangentSpaceShader)mShaderProgram).setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, stride, 12*BYTES_PER_FLOAT);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, mVertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			mVBOData.unbindVBO();
		} else {
			Buffer vertexBuffer	= mVertexData.getVertexBuffer();
			Buffer indexBuffer	= mVertexData.getIndexBuffer();

			vertexBuffer.position(0);
			((TangentSpaceShader)mShaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(3);
			((TangentSpaceShader)mShaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(6);
			((TangentSpaceShader)mShaderProgram).setTangentAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(9);
			((TangentSpaceShader)mShaderProgram).setBinormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(12);
			((TangentSpaceShader)mShaderProgram).setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, stride, vertexBuffer);		
	
			indexBuffer.position(0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
