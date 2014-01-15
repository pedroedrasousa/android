package com.pedroedrasousa.engine.object3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.pedroedrasousa.engine.Vec3;

public class VertexData {

	public static final int BYTES_PER_FLOAT = 4;
	public static final int BYTES_PER_SHORT = 2;
	
	protected FloatBuffer	mVertexBuffer;
	protected ShortBuffer	mIndexBuffer;
	protected Boundaries	mBoundaries;

	public VertexData() {
		;
	}
	
	public VertexData(float[] data, short[] indices) {
		setVertexData(data, indices);
	}
	
	public void setVertexData(float[] data, short[] indices) {
		
		ByteBuffer  byteBuf;
		
		// Vertex data buffer.
		byteBuf = ByteBuffer.allocateDirect(data.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(data);
		mVertexBuffer.position(0);
		
		// Index buffer.
		byteBuf = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT);
		byteBuf.order(ByteOrder.nativeOrder());
		mIndexBuffer = byteBuf.asShortBuffer();
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
	
	public FloatBuffer getVertexBuffer() {
		return mVertexBuffer;
	}

	public ShortBuffer getIndexBuffer() {
		return mIndexBuffer;
	}
	
	public Boundaries getBoundaries() {
		return mBoundaries;
	}

	public void setBoundaries(Boundaries boundaries) {
		mBoundaries = boundaries;
	}
	
	public int getNbrIndices() {
		return mIndexBuffer.capacity();
	}
	
	public static class Boundaries {
		
		private Vec3 mMin;
		private Vec3 mMax;
				
		public float getMinX() {	return mMin.x;	}
		public float getMaxX() {	return mMax.x;	}
		public float getMinY() {	return mMin.y;	}
		public float getMaxY() {	return mMax.y;	}
		public float getMinZ() {	return mMin.z;	}
		public float getMaxZ() {	return mMax.z;	}
		public Vec3 getMinVals() {	return mMin;	}
		public Vec3 getMaxVals() {	return mMax;	}		
		
		public void setMinX(float x) {	mMin.x = x;	}
		public void setMaxX(float x) {	mMax.x = x;	}
		public void setMinY(float y) {	mMin.y = y;	}
		public void setMaxY(float y) {	mMax.y = y;	}
		public void setMinZ(float z) {	mMin.z = z;	}
		public void setMaxZ(float z) {	mMax.z = z;	}
		public void setMinVals(Vec3 minVals) {	mMin = minVals;	}
		public void setMaxVals(Vec3 maxVals) {	mMax = maxVals;	}
		
		public float getWidth()		{	return mMax.x - mMin.x;	}
		public float getHeight()	{	return mMax.y - mMin.y;	}
		public float getLength()	{	return mMax.z - mMin.z;	}
	}
}
