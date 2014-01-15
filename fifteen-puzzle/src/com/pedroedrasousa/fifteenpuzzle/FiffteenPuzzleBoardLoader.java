package com.pedroedrasousa.fifteenpuzzle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.content.Context;
import android.content.res.AssetManager;


public class FiffteenPuzzleBoardLoader {
	
	public static void loadBoardFromAsset(Context context, FifteenPuzzleBoard puzzleBoard, String filename, int level) {
		String line = null;
		
		int nbrAlowedMoves = -1;

		AssetManager am = context.getAssets();
		try {
			LineNumberReader input = new LineNumberReader(new InputStreamReader(am.open(filename)));

			for (line = input.readLine(); line != null; line = input.readLine())
				if (line.startsWith("level" + String.format("%03d", level)))
					break;

			for (line = input.readLine(); line != null && !line.startsWith("level"); line = input.readLine()) {
				if (line.startsWith("moves ")) {
					nbrAlowedMoves = Integer.parseInt(line.substring(6));
					break;
				}
			}

			for (line = input.readLine(); line != null && !line.startsWith("level"); line = input.readLine()) {
				if (line.startsWith("board ")) {
					String boardType = line.substring(6);

					if (boardType.equals("3x3")) {
						puzzleBoard.createBoard(FifteenPuzzleBoard.SIZE3X3);
					} else if (boardType.equals("3x4")) {
						puzzleBoard.createBoard(FifteenPuzzleBoard.SIZE3X4);
					} else if (boardType.equals("4x4")) {
						puzzleBoard.createBoard(FifteenPuzzleBoard.SIZE4X4);
					}
					break;
				}
			}

			int y = 0;
			for (line = input.readLine(); line != null && !line.trim().equals(""); line = input.readLine()) {
				for (int x = 0; x < line.length() / 3; x++) {
					int nbr = Integer.parseInt(line.substring(x * 3, x * 3 + 2));
					if (nbr > 0)
						puzzleBoard.createTile(nbr, x, y);
				}
				y++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		puzzleBoard.setNbrAllowedMoves(nbrAlowedMoves);
	}
	
	public static String getLevelName(Context context, String filename, int level) {

		String name = new String();

		String line;
		AssetManager am = context.getAssets();
		try {
			LineNumberReader input = new LineNumberReader(new InputStreamReader(am.open(filename)));
			// Find the level start line.
			for (line = input.readLine(); line != null; line = input.readLine())
				if (line.startsWith("level" + String.format("%03d", level))) {
					name = line.substring(9);
					break;
				}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return name;
	}
	
	public static String getLevelDesc(Context context, String filename, int level) {

		String desc = new String();

		String line;
		AssetManager am = context.getAssets();
		try {
			LineNumberReader input = new LineNumberReader(new InputStreamReader(am.open(filename)));
			// Find the level start line.
			for (line = input.readLine(); line != null; line = input.readLine())
				if (line.startsWith("level" + String.format("%03d", level))) {
					break;
				}
			// Get the description line.
			for (line = input.readLine(); line != null && !line.startsWith("level"); line = input.readLine()) {
				if (line.startsWith("desc ")) {
					desc = line.substring(5);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return desc;
	}
	
	public static void create3x3(FifteenPuzzleBoard puzzleBoard) {

		puzzleBoard.createBoard(FifteenPuzzleBoard.SIZE3X3);

		puzzleBoard.createTile(1, 0, 0);
		puzzleBoard.createTile(2, 1, 0);
		puzzleBoard.createTile(3, 2, 0);

		puzzleBoard.createTile(4, 0, 1);
		puzzleBoard.createTile(5, 1, 1);
		puzzleBoard.createTile(6, 2, 1);

		puzzleBoard.createTile(7, 0, 2);
		puzzleBoard.createTile(8, 1, 2);
	}
	
	public static void create3x4(FifteenPuzzleBoard puzzleBoard) {

		puzzleBoard.createBoard(FifteenPuzzleBoard.SIZE3X4);

		puzzleBoard.createTile(1, 0, 0);
		puzzleBoard.createTile(2, 1, 0);
		puzzleBoard.createTile(3, 2, 0);

		puzzleBoard.createTile(4, 0, 1);
		puzzleBoard.createTile(5, 1, 1);
		puzzleBoard.createTile(6, 2, 1);

		puzzleBoard.createTile(7, 0, 2);
		puzzleBoard.createTile(8, 1, 2);
		puzzleBoard.createTile(9, 2, 2);

		puzzleBoard.createTile(10, 0, 3);
		puzzleBoard.createTile(11, 1, 3);
	}
	
	public static void create4x4(FifteenPuzzleBoard puzzleBoard) {

		puzzleBoard.createBoard(FifteenPuzzleBoard.SIZE4X4);

		puzzleBoard.createTile(1, 0, 0);
		puzzleBoard.createTile(2, 1, 0);
		puzzleBoard.createTile(3, 2, 0);
		puzzleBoard.createTile(4, 3, 0);

		puzzleBoard.createTile(5, 0, 1);
		puzzleBoard.createTile(6, 1, 1);
		puzzleBoard.createTile(7, 2, 1);
		puzzleBoard.createTile(8, 3, 1);

		puzzleBoard.createTile(9, 0, 2);
		puzzleBoard.createTile(10, 1, 2);
		puzzleBoard.createTile(11, 2, 2);
		puzzleBoard.createTile(12, 3, 2);

		puzzleBoard.createTile(13, 0, 3);
		puzzleBoard.createTile(14, 1, 3);
		puzzleBoard.createTile(15, 2, 3);
	}
}
