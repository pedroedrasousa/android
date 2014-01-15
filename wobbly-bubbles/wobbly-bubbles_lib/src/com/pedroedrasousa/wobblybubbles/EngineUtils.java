package com.pedroedrasousa.wobblybubbles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.Matrix;

public class EngineUtils {
	
	private float[]		mOrthoMVPMatrix = new float[16];
	private FloatBuffer	mQuadVertexBuffer;
	private FloatBuffer	mQuadTexCoords;
	private FloatBuffer	mQuadUnitTexCoords;
	
	public static float[]	identityMatrix;
	
	static {
        identityMatrix = new float[16];
        Matrix.setIdentityM(identityMatrix, 0);		
	}
	
	public EngineUtils(Context context) {
		Matrix.orthoM(mOrthoMVPMatrix, 0, 0, 1, 1, 0, 0, 100);
		
		ByteBuffer byteBuf;
		
		float[] vertices;
		vertices = new float[8];
		vertices[0] = 0f;	vertices[1] = 1f;
		vertices[2] = 1f;	vertices[3] = 1f;
		vertices[4] = 0f;	vertices[5] = 0f;
		vertices[6] = 1f;	vertices[7] = 0f;

		byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mQuadVertexBuffer = byteBuf.asFloatBuffer();
		mQuadVertexBuffer.put(vertices);
		mQuadVertexBuffer.position(0);
		
		float[] texture;
		texture = new float[8];
		texture[0] = 0f;	texture[1] = 1f;
		texture[2] = 1f;	texture[3] = 1f;
		texture[4] = 0f;	texture[5] = 0f;
		texture[6] = 1f;	texture[7] = 0f;
		
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mQuadTexCoords = byteBuf.asFloatBuffer();
		mQuadTexCoords.put(texture);
		mQuadTexCoords.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mQuadUnitTexCoords = byteBuf.asFloatBuffer();
		mQuadUnitTexCoords.put(texture);
		mQuadUnitTexCoords.position(0);
	}
	
	public FloatBuffer getQuadUnitTexCoords() {
		return mQuadUnitTexCoords;
	}
	
	public FloatBuffer getQuadVertexBuffer() {
		return mQuadVertexBuffer;
	}
	
	public FloatBuffer getQuadTexCoords() {
		return mQuadTexCoords;
	}
	
	public void setQuadTexCoords(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		final float[] texCoords = new float[8];
		
		texCoords[0] = x1;	texCoords[1] = y1;
		texCoords[2] = x2;	texCoords[3] = y2;
		texCoords[4] = x3;	texCoords[5] = y3;
		texCoords[6] = x4;	texCoords[7] = y4;
		
		mQuadTexCoords.position(0);
		mQuadTexCoords.put(texCoords);
		mQuadTexCoords.position(0);
	}
	
	public float[] getOrthoMVPMatrix() {
		return mOrthoMVPMatrix;
	}
}
