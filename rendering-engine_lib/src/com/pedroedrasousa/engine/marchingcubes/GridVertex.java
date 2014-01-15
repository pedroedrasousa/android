package com.pedroedrasousa.engine.marchingcubes;

public class GridVertex {

	private float mValue;
	
	// Packed buffer: position|normal|color
	public float[] mVertexData;
	
	public GridVertex() {
		mVertexData = new float[9];
	}
	
	public float getValue() {
		return mValue;
	}

	public void setValue(float value) {
		mValue = value;
	}
	
	public void addToValue(float x) {
		mValue += x;
	}
}
