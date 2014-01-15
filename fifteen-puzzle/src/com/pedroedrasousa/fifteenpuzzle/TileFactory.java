package com.pedroedrasousa.fifteenpuzzle;

import java.util.Vector;

import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.ColorShaderProg;
import com.pedroedrasousa.engine.shader.TangentSpaceShaderProg;


public class TileFactory {
	
	private static VertexData				mMeshVertexData;
	private static VertexData				mBBMeshVertexData;
	
	private static Vector<Texture>			mBaseMapTexture;
	private static Vector<Texture>			mNormalMapTexture;
	
    private static TangentSpaceShaderProg	mMeshShader;
    private static ColorShaderProg			mBBMeshShader;
	
	static {
		mBaseMapTexture		= new Vector<Texture>();
		mNormalMapTexture	= new Vector<Texture>();
	}
		
	public static Tile buildTile(int number, int nbrBoardSquaresX, int nbrBoardSquaresY) {
		
		int posX = (number - 1) % nbrBoardSquaresX;
		int posY = (number - 1) / nbrBoardSquaresX;

		Tile t = new Tile(posX, posY, nbrBoardSquaresX, nbrBoardSquaresY);
		
		t.setMesh(mMeshVertexData, mMeshShader);
		t.setBoundingBoxMesh(mBBMeshVertexData, mBBMeshShader);
		
		t.setBaseMap(mBaseMapTexture.get(number - 1));
		t.setNormalMap(mNormalMapTexture.get(number - 1));
    	
    	return t;
	}
	
	
	public static VertexData getMesh() {
		return mMeshVertexData;
	}

	public static VertexData getBbmesh() {
		return mBBMeshVertexData;
	}

	public static void setMesh(VertexData mesh) {
		mMeshVertexData = mesh;
	}

	public static void setBBMesh(VertexData bbmesh) {
		mBBMeshVertexData = bbmesh;
	}

	public static void setMeshShader(TangentSpaceShaderProg shader) {
		mMeshShader = shader;
	}

	public static void setBBMeshShader(ColorShaderProg shader) {
		mBBMeshShader = shader;
	}

	public static TangentSpaceShaderProg getmMeshShader() {
		return mMeshShader;
	}

	public static ColorShaderProg getmBBMeshShader() {
		return mBBMeshShader;
	}

	public static void setBaseMapTexture(int number, Texture texture) {
		mBaseMapTexture.add(number - 1, texture);
	}

	public static void setNormalMapTexture(int number, Texture texture) {
		mNormalMapTexture.add(number - 1, texture);
	}
}
