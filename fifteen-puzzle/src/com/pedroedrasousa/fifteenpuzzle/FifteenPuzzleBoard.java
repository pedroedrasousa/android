package com.pedroedrasousa.fifteenpuzzle;

import com.pedroedrasousa.engine.Renderer;;;

public interface FifteenPuzzleBoard extends Renderer {
	
	public static final int SIZE3X3 = 0;
	public static final int SIZE3X4 = 1;
	public static final int SIZE4X4 = 2;
	
	public void createBoard(int size);
	public Tile createTile(int number, int x, int y);
	public void setNbrAllowedMoves(int nbrAllowedMoves);
}
