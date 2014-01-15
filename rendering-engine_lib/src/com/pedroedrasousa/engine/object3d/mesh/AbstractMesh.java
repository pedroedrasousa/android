package com.pedroedrasousa.engine.object3d.mesh;

import com.pedroedrasousa.engine.object3d.VBOData;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.SimpleShader;


public abstract class AbstractMesh {
	
	protected VertexData	mVertexData;
	protected VBOData		mVBOData;		// Will assume null if not supported.
	protected SimpleShader	mShaderProgram;
	protected int			mRenderMode;
	
	public AbstractMesh(VertexData vertexData, SimpleShader shaderProgram, boolean useVBOs) {
		setMesh(vertexData);
		setShaderProgram(shaderProgram);
		if (useVBOs && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			mVBOData = new VBOData(vertexData);
		}
	}
	
	/**
	 * Use VBOs by default if supported and argument useVBOs is omitted.
	 * @param vertexData
	 * @param shaderProgram
	 */	
	public AbstractMesh(VertexData vertexData, SimpleShader shaderProgram) {
		this(vertexData, shaderProgram, true);
	}
	
	public int getRenderMode() {
		return mRenderMode;
	}

	public void setRenderMode(int renderMode) {
		mRenderMode = renderMode;
	}
	
	public VBOData getVBOData() {
		return mVBOData;
	}
	
	public VertexData getMesh() {
		return mVertexData;
	}

	public void setMesh(VertexData vertexData) {
		mVertexData = vertexData;
	}
	
	public SimpleShader getShaderProgram() {
		return mShaderProgram;
	}
	
	public void setShaderProgram(SimpleShader shaderProgram) {
		mShaderProgram = shaderProgram;
	}
	
	public void reload() {
		if (mVBOData != null)
			mVBOData.loadVBOVertexData();
	}

	public abstract void render();
}
