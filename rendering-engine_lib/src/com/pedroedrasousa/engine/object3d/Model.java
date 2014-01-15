package com.pedroedrasousa.engine.object3d;

import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.Vec3;
import com.pedroedrasousa.engine.object3d.mesh.AbstractMesh;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Model {
	
	protected AbstractMesh	mMesh;
	protected Texture		mBaseMap		= new Texture();
	protected Texture		mNormalMap		= new Texture();
	protected float[]		mModelMatrix	= new float[16];

	public Model() {
		Matrix.setIdentityM(mModelMatrix, 0);
	}
	
	public Model(AbstractMesh mesh, Texture baseMap, Texture normalMap) {
		this();
		setMesh(mesh);
		setBaseMap(baseMap);
		setNormalMap(normalMap);
	}
		
	public void setMesh(AbstractMesh mesh) {
		mMesh = mesh;
	}

	public void render() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBaseMap.getHandle());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNormalMap.getHandle());
        mMesh.render();
	}
	
	public void reloadMesh() {
		mMesh.reload();
	}
	
	public void reloadTextures(Context context) {
		mBaseMap.reload(context);
		mNormalMap.reload(context);
	}
	
	public void reload(Context context) {
		reloadMesh();
		reloadTextures(context);
	}
	
	public AbstractMesh getMesh() {
		return mMesh;
	}
	
	public Texture getBaseMap() {
		return mBaseMap;
	}

	public void setBaseMap(Texture baseMap) {
		mBaseMap = baseMap;
	}

	public Texture getNormalMap() {
		return mNormalMap;
	}

	public void setNormalMap(Texture normalMap) {
		mNormalMap = normalMap;
	}
	
	public float[] getModelMatrix() {
		return mModelMatrix;
	}
	
	public Vec3 getPos() {
		return new Vec3(mModelMatrix[12], mModelMatrix[13], mModelMatrix[14]);
	}
}
