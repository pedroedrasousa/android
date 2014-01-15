package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import android.opengl.GLES20;

import com.pedroedrasousa.engine.Vec3;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.ColorShader;


public class ColorMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;
	
	private Vec3 mColor;
	
	public ColorMesh(VertexData vertexData, ColorShader shaderProgram, boolean useVBOs, Vec3 color) {
		super(vertexData, shaderProgram, useVBOs);
		setColor(color);
	}

	public ColorMesh(VertexData vertexData, ColorShader shaderProgram, Vec3 color) {
		this(vertexData, shaderProgram, true, color);
	}

	public Vec3 getColor() {
		return mColor;
	}

	public void setColor(Vec3 color) {
		mColor = new Vec3(color);
	}

	public void render() {
		
		int stride = 3 * BYTES_PER_FLOAT;
		
		if (mVBOData != null) {
			mVBOData.bindVBO();
			((ColorShader)mShaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			((ColorShader)mShaderProgram).setColor(mColor);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, mVertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			mVBOData.unbindVBO();
		} else {
			Buffer vertexBuffer	= mVertexData.getVertexBuffer();
			Buffer indexBuffer	= mVertexData.getIndexBuffer();
			vertexBuffer.position(0);
			((ColorShader)mShaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			((ColorShader)mShaderProgram).setColor(mColor);
			indexBuffer.position(0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
