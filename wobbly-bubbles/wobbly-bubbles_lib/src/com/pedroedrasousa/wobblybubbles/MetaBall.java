package com.pedroedrasousa.wobblybubbles;

import com.pedroedrasousa.engine.Vec3;

public class MetaBall {

	private Vec3	mPos			= new Vec3();
	private Vec3	mColor			= new Vec3();
	private float	mRadius			= 1.0f;
	private float	mSquaredRadius	= 1.0f;
	private float	mMaxOffsetX;
	private float	mMaxOffsetY;
	private float	mDeltaX;
	private float	mDeltaY;
	private float	mVelX;
	private float	mVelY;
	
	public MetaBall(float radius, Vec3 pos) {
		mPos			= pos;
		mRadius			= radius;
		mSquaredRadius	= radius * radius;
	}
	
	public void validatePosBounds() {
		mPos.x = Math.max(mPos.x, -mMaxOffsetX);
		mPos.x = Math.min(mPos.x, mMaxOffsetX);
		mPos.y = Math.max(mPos.y, -mMaxOffsetY);
		mPos.y = Math.min(mPos.y, mMaxOffsetY);
	}
	
	public void refreshPosition(float delta) {
		mDeltaX += delta;
		mDeltaY += delta;
		mPos.x = (float)Math.sin(mDeltaX * mVelX) * mMaxOffsetX;
		mPos.y = (float)Math.sin(mDeltaY * mVelY) * mMaxOffsetY;
	}
	
	public void setMaxOffsetX(float maxOffsetX) {
		mMaxOffsetX = maxOffsetX;
	}
	
	public void setMaxOffsetY(float maxOffsetY) {
		mMaxOffsetY = maxOffsetY;
	}
	
	public void setDeltaX(float deltaX) {
		mDeltaX = deltaX;
	}
	
	public void setDeltaY(float deltaY) {
		mDeltaY = deltaY;
	}
	
	public void setVelX(float velX) {
		mVelX = velX;
	}
	
	public void setVelY(float velY) {
		mVelY = velY;
	}
	
	public Vec3 getPos() {
		return mPos;
	}
	
	public Vec3 getColor() {
		return mColor;
	}

	public void setColor(Vec3 color) {
		mColor = color;
	}

	public float getRadius() {
		return mRadius;
	}
	
	public void setRadius(float radius) {
		mRadius			= radius;
		mSquaredRadius	= radius * radius;
	}
	
	public float getSquaredRadius() {
		return mSquaredRadius;
	}
}
