package com.pedroedrasousa.fifteenpuzzle;

import java.util.Random;
import android.opengl.Matrix;
import com.pedroedrasousa.engine.object3d.PickableModel;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;


public class Tile extends PickableModel implements GameBoardObject {

	private static final float TRANSLATION_SPEED = 1.0f;
	
	private float mSpeed = TRANSLATION_SPEED;
	private static float mSpeedFactor = 1.0f;
	protected float mHalfSize;
	protected float mSize;
	
	protected int	mState = 0;

	public static final int STOPPED = 0;
	public static final int ROTATING_POSX = 1;
	public static final int ROTATING_NEGX = 2;
	public static final int ROTATING_POSZ = 3;
	public static final int ROTATING_NEGZ = 4;
	public static final int GOING_INTO_PLACE = 5;
	public static final int UP_AND_BACK = 6;

	private int mCorrectPosX;
	private int mCorrectPosY;
	
	
	protected int	mGridTargetPosX;
	protected int	mGridTargetPosY;
	protected int	mGridPosX;
	protected int	mGridPosY;
	
	protected int	mNbrBoardSquaresX;
	protected int	mNbrBoardSquaresY;
	
	private float	mRotationAngle;
	private float	mTranslationZ;
	private float	mTranslationFactor = 1.0f;
	
	private float	mOffset;
	
	public boolean isStopped() {
		return mState == STOPPED;
	}
	
	public boolean isInPlace() {
		return mCorrectPosX == getPosX() && mCorrectPosY == getPosY();
	}
	
	public Tile(int originalPosX, int originalPosY, int nbrBoardSquaresX, int nbrBoardSquaresY) {
		mNbrBoardSquaresX = nbrBoardSquaresX;
		mNbrBoardSquaresY = nbrBoardSquaresY;
		mCorrectPosX = originalPosX;
		mCorrectPosY = originalPosY;
		setPosX(originalPosX);
		setPosY(originalPosY);
	}
	
	public void changeState(int state) {
		if (mState != STOPPED)
			return;
		
		mState = state;
	}
	
	protected void buildModelMatrix() {
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, (mHalfSize + mSize * mGridPosX - mNbrBoardSquaresX*0.5f * mSize) * mTranslationFactor, mHalfSize + mTranslationZ, (mHalfSize + mSize * mGridPosY - mNbrBoardSquaresY * 0.5f * mSize) * mTranslationFactor);
		Matrix.rotateM(getModelMatrix(), 0, mRotationAngle, 1.0f, 0.0f, 1.0f);
	}
	
	private void stopAndBuildModelMatrix() {
		mSpeed = TRANSLATION_SPEED;
		mState = STOPPED;
		mOffset = 0.0f;
		mGridPosX = mGridTargetPosX;
		mGridPosY = mGridTargetPosY;
		buildModelMatrix();
	}
	
	public void flyToPlace() {
		Random rand = new Random();
		mTranslationZ = 10.0f + (float)rand.nextInt(20);
		mState = GOING_INTO_PLACE;
	}
	
	public void upAndBack() {
		t = 0.0f;
		mState = UP_AND_BACK;
	}
	
	float t = 0.0f;
	

	public void update(float factor) {
		
		switch (mState) {
		case ROTATING_POSX:
			slidePosX(factor);
			break;
		case ROTATING_NEGX:
			slideNegX(factor);
			break;
		case ROTATING_POSZ:
			slidePosZ(factor);
			break;
		case ROTATING_NEGZ:
			slideNegZ(factor);
			break;
		case GOING_INTO_PLACE:

			mTranslationZ -= 0.3f * factor;
			mRotationAngle = 400.0f * (float) Math.sin(mTranslationZ / 20.0f);
			mTranslationFactor = ((float) Math.sin(mTranslationZ / 20.0f) + 2.0f) * 0.5f;
			
			if (mTranslationZ < 0) {
				mTranslationFactor = 1.0f;
				mRotationAngle = 0.0f;
				mTranslationZ = 0.0f;
				mState = STOPPED;
			}
			buildModelMatrix();
			break;
			
		case UP_AND_BACK:

			mTranslationZ = (float) Math.cos(t * 2.0f + (float)Math.PI) + 1.0f;
			mTranslationFactor = 0.1f * ((float) Math.cos(t * 2.0f + (float)Math.PI) + 2.0f) + 0.9f;
			mRotationAngle = 180.0f * ((float) Math.cos(t + (float)Math.PI) + 1.0f);
			t+=0.05f * factor;
			
			if (t >  Math.PI) {
				mTranslationFactor = 1.0f;
				mRotationAngle = 0.0f;
				mTranslationZ = 0.0f;
				mState = STOPPED;
			}
			buildModelMatrix();
			break;
			
		default:
			buildModelMatrix();
			break;
		}
	}
		
	private void updateSpeed() {
		mSpeed = (0.5f - (Math.abs(mHalfSize - Math.abs(mOffset))) / mSize) * TRANSLATION_SPEED + 0.01f;
	}
	
	public void slidePosX(float factor) {
		if (mOffset < mSize) {
			mOffset += mSpeed * mSpeedFactor * factor;
			buildModelMatrix();
			translate(mOffset, 0.0f);
			updateSpeed();
		}
		else {
			stopAndBuildModelMatrix();
		}
	}
	
	public void slideNegX(float factor) {
		if (mOffset > -mSize) {
			mOffset -= mSpeed * mSpeedFactor * factor;
			buildModelMatrix();
			translate(mOffset, 0.0f);
			updateSpeed();
		}
		else {
			stopAndBuildModelMatrix();
		}
	}
	
	public void slidePosZ(float factor) {
		
		if (mOffset < mSize) {
			mOffset += mSpeed * mSpeedFactor * factor;
			buildModelMatrix();
			translate(0.0f, mOffset);
			updateSpeed();
		}
		else {
			stopAndBuildModelMatrix();
		}
	}
	
	public void slideNegZ(float factor) {
		if (mOffset > -mSize) {
			mOffset -= mSpeed * mSpeedFactor * factor;
			buildModelMatrix();
			translate(0.0f, mOffset);
			updateSpeed();
		}
		else {
			stopAndBuildModelMatrix();
		}
	}
	
	private void translate(float x, float z) {
		Matrix.translateM(mModelMatrix, 0, x, 0.0f, z);
	}
	
	public int getCorrectPosX() {
		return mCorrectPosX;
	}

	public int getCorrectPosY() {
		return mCorrectPosY;
	}
	
	public void setMesh(VertexData vertexData, TangentSpaceShader shader) {
		super.setMesh(vertexData, shader);
		mHalfSize	= vertexData.getBoundaries().getMaxX();
		mSize		= vertexData.getBoundaries().getMaxX() * 2.0f;
	}

	public int getPosX() {
		return mGridPosX;
	}
	
	public int getPosY() {
		return mGridPosY;
	}
	
	public void setPosX(int x) {
		mGridPosX = x;
	}
	
	public void setPosY(int y) {
		mGridPosY = y;
	}
	
	public void setOriginalPosX(int x) {
		mGridTargetPosX = x;
	}
	
	public void setOriginalPosY(int y) {
		mGridTargetPosY = y;
	}
	
	public static void setSpeedFactor(float speed) {
		mSpeedFactor = speed;
	}
}
