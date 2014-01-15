package com.pedroedrasousa.engine.marchingcubes;

public class GridCell {

	private GridVertex vertices[];
	
	GridCell() {
		vertices = new GridVertex[8];
	}
	
	public GridVertex[] getVertices() {
		return vertices;
	}
}
