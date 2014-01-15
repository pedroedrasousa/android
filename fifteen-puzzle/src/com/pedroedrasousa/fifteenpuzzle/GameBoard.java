package com.pedroedrasousa.fifteenpuzzle;

public class GameBoard {
	
	private GameBoardObject	mGridObjects[][];
	private Object			mGridTiles[][];
	
	public GameBoard(int width, int height) {
		mGridObjects	= new GameBoardObject[width][height];
		mGridTiles		= new Object[width][height];
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				mGridTiles[i][j] = new Object();
			}			
		}
	}
	
	public Object getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= mGridObjects.length || y >= mGridObjects[0].length)
			return null;
		
		return mGridTiles[x][y];
	}
	
	public Object getObj(int x, int y) {
		if (x < 0 || y < 0 || x >= mGridObjects.length || y >= mGridObjects[0].length)
			return null;
		
		return mGridObjects[x][y];
	}
	
	public void setObj(int x, int y, GameBoardObject obj) {
		mGridObjects[x][y] = obj;
		if (obj != null) {
			obj.setOriginalPosX(x);
			obj.setOriginalPosY(y);	
		}
	}
	
	public int getWidth() {
		return mGridObjects.length;
	}
	
	public int getHeight() {
		return mGridObjects[0].length;
	}
}