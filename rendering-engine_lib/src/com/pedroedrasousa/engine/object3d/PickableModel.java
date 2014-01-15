package com.pedroedrasousa.engine.object3d;

import java.util.Vector;

import android.content.Context;
import android.opengl.GLES20;

import com.pedroedrasousa.engine.Vec3;
import com.pedroedrasousa.engine.shader.ColorShader;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.object3d.mesh.ColorMesh;
import com.pedroedrasousa.engine.object3d.mesh.TangentSpaceMesh;


public class PickableModel extends Model {

	private static final float COLOR_DELTA			= 0.2f;					// Must be factor of 1.0f
	private static final float COLOR_ERROR_DELTA	= COLOR_DELTA * 0.3f;	// Allowed error delta when comparing color values.
	
	private static Vector<Vec3> mColorLookupTable;
	
	
	
	private static int mNextColor;
	
	private ColorMesh mBoundingBoxMesh;
	
	static {
		resetColor();
		mColorLookupTable = new Vector<Vec3>();
		
		// Build color lookup table.
		for (float b = 0.0f; b <= 1.0f; b += COLOR_DELTA)
			for (float g = 0.0f; g <= 1.0f; g += COLOR_DELTA)
				for (float r = 0.0f; r <= 1.0f; r += COLOR_DELTA)
					mColorLookupTable.add(new Vec3(r, g, b));
	}
	
	
	
	public PickableModel() {
		
	}
	
	/*
	public PickableModel(AbstractMesh mesh, Texture baseMap, Texture normalMap) {
		super.(mesh, baseMap, normalMap)
	}*/

	public ColorMesh getBoundingBoxMesh() {
		return mBoundingBoxMesh;
	}

	public void setMesh(VertexData vertexData, TangentSpaceShader shader) {
		mMesh = new TangentSpaceMesh(vertexData, shader);
	}
	
	public void setBoundingBoxMesh(VertexData vertexData, ColorShader shader) {		
		mBoundingBoxMesh = new ColorMesh(vertexData, shader, getColor());
	}
	
	public void loadFromOBJ(Context context, TangentSpaceShader tangentSpaceShader, ColorShader colorShader, String assetName) {
		final MeshLoader ml = new MeshLoader();
		ml.LoadFromObj(context, assetName);
		VertexData vertexData	= ml.getVertexData();
		VertexData bbVertexData	= ml.getBoundingBoxMesh();
		setMesh(vertexData, tangentSpaceShader);
		setBoundingBoxMesh(bbVertexData, colorShader);
	}
	
	private Vec3 getColor() {
		return mColorLookupTable.get(mNextColor++);
	}
	
	public static void resetColor() {
		// Black will be used for the background, start at r = COLOR_DELTA.
		mNextColor = 1;
	}

	public void renderBoundingBox() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBaseMap.getHandle());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNormalMap.getHandle());
        mBoundingBoxMesh.render();
	}
	
	/**
	 * Indicates whether a color matches this models bounding box color or not.
	 * @param pickedColor The picked pixel color.
	 * @return True if the picked pixel color matches the model bounding box color.
	 */
	public boolean isPicked(Vec3 pickedColor) {
		if (mBoundingBoxMesh.getColor().equals(pickedColor, COLOR_ERROR_DELTA)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void reload(Context context) {
		super.reload(context);
		mBoundingBoxMesh.reload();
	}
}
