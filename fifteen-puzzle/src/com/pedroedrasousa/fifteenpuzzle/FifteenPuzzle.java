package com.pedroedrasousa.fifteenpuzzle;

import java.util.Random;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pedroedrasousa.engine.EngineGLSurfaceView;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.Timer;
import com.pedroedrasousa.engine.Utils;
import com.pedroedrasousa.engine.Vec3;
import com.pedroedrasousa.engine.shader.ColorShaderProg;
import com.pedroedrasousa.engine.shader.SimpleShaderProg;
import com.pedroedrasousa.engine.shader.TangentSpaceShaderProg;
import com.pedroedrasousa.fifteenpuzzle.R;
import com.pedroedrasousa.engine.Font;
import com.pedroedrasousa.engine.FrameRateControler;
import com.pedroedrasousa.engine.object3d.MeshLoader;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.object3d.mesh.SimpleMesh;


public class FifteenPuzzle implements FifteenPuzzleBoard, OnTouchListener, SensorEventListener {

	public final static String LEVEL_FILE		= "levels.txt";
	
	private final static int SHUFFLE_ITERATIONS = 60;
	
	public static final int GAMESTATUS_INPROGRESS			= 0;
	public static final int GAMESTATUS_SHUFFLING			= 1;
	public static final int GAMESTATUS_ANIMATION			= 2;
	public static final int GAMESTATUS_WINANIMATION			= 3;
	public static final int GAMESTATUS_STOPPED				= 4;
	public static final int GAMESTATUS_SOLVED				= 5;
	public static final int GAMESTATUS_LIMITEDMOVES			= 6;
	public static final int GAMESTATUS_MAX_MOVES_REACHED	= 7;
	
	private GameActivity		mActivity;
	private EngineGLSurfaceView	mGLSurfaceView;
	private float				mDisplayDensity;
	
	private String				mGameId1;
	private String				mGameId2;
	
	private Thread				mShufflerThread;			// Thread used to randomly shuffle the game tiles.
	private Thread				mAnimationThread;			// Thread used to animate the game tiles.
	
	private Vec3				mPickedColor = new Vec3();	// Picked color in the off-screen buffer.
	
	private VertexData	mTileVertexData;					// Geometry vertex data used in the tiles.
	private VertexData	mTileBBVertexData;					// Bounding box geometry vertex data used in the tiles.

	private SimpleMesh	mPlane;								// Off-screen plane used to get the z-buffer depth value.
	
	private boolean isSurfaceCreated;
	
	// Matrices
	private float[] mProjMatrix	= new float[16];
	private float[] mVMatrix	= new float[16];
	private float[] mMVMatrix	= new float[16];
	private float[] mMVPMatrix	= new float[16];

	// Camera
	private Vec3 mCameraEye		= new Vec3(0.0f, 16.0f, 1.5f);
	private Vec3 mCameraCenter	= new Vec3(0.0f, 0.0f, 0.0f);
	private Vec3 mCameraUp		= new Vec3(0.0f, 1.0f, 0.0f);
	
	private Vec3 mLightPos;
	
	// Shader programs
	private TangentSpaceShaderProg	mLightingShader;
	private SimpleShaderProg		mDepth2ColorShader;
	private ColorShaderProg			mSimpleColorShader;

	private Timer				mTimer;
	private Font				mFont;
	private FrameRateControler	mFramRateCtl;	
	private GameBoard			mBoard;
	
	private Vector<Tile> mTiles = new Vector<Tile>();
	private Tile mSelectedTile;
	private Tile mCubeDown;
	private Tile mCubeUp;
	private Tile mCubeLeft;
	private Tile mCubeRight;
	
	private int mCurrentSize;

	private int[] mViewport = new int[4];	// Viewport information (x0, y0, x1, y1)
	private float mScreenRatio;				// Width / Height

	private float mTouchPrevX;
	private float mTouchPrevY;

	private Vec3 mGyroscopeValues	= new Vec3();
	private Vec3 mCameraRot			= new Vec3();
	private Vec3 mCameraTargetRot	= new Vec3();

	private int mNbrMoves;
	private int mNbrAllowedMoves;

	private int mGameState;

	private Toast mToast;
	
	// World projected screen coordinates.
	private Vec3 mTouchDownCoord	= new Vec3();
	private Vec3 mTouchUpCoord		= new Vec3();	
	
	// Motion event stuff
	private boolean mMotionEventDown;
	private boolean mMotionEventMove;
	private float	mDeltaMoveX;
	private float	mDeltaMoveY;
	
	// Game in progress best score
	private int mBestTime;
	private int mBestMoves;
	
	@SuppressLint("ShowToast")
	public FifteenPuzzle(GameActivity context, EngineGLSurfaceView glSurfaceView, float displayDensity) {
		mActivity			= context;
		mGLSurfaceView		= glSurfaceView;
		mDisplayDensity		= displayDensity;
		mNbrAllowedMoves	= -1;
		
		mTimer			= new Timer();
		mFramRateCtl	= new FrameRateControler();
		mLightPos		= new Vec3(0f, 20.0f, 0f);
		
		mToast = Toast.makeText(mActivity, "", Toast.LENGTH_LONG);
		mToast.setGravity(Gravity.BOTTOM, 0, 0);
		LinearLayout toastLayout = (LinearLayout) mToast.getView();
		TextView toastTV = (TextView) toastLayout.getChildAt(0);
		toastTV.setTextSize(16);
	}

	public void setGameId(String gameId1, String gameId2) {
		mGameId1 = gameId1;
		mGameId2 = gameId2;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		if (!isSurfaceCreated) {
			isSurfaceCreated = true;

			mLightingShader		= new TangentSpaceShaderProg(mActivity,R.raw.lighting_vert, R.raw.lighting_frag);
			mDepth2ColorShader	= new SimpleShaderProg(mActivity, R.raw.depth2color_vert, R.raw.depth2color_frag);
			mSimpleColorShader	= new ColorShaderProg(mActivity, R.raw.simple_color_vert, R.raw.simple_color_frag);

			final MeshLoader ml = new MeshLoader();
			ml.LoadFromObj(mActivity, "tile.obj");
			mTileVertexData		= ml.getVertexData();
			mTileBBVertexData	= ml.getBoundingBoxMesh();

			TileFactory.setMesh(mTileVertexData);
			TileFactory.setBBMesh(mTileBBVertexData);

			TileFactory.setMeshShader(mLightingShader);
			TileFactory.setBBMeshShader(mSimpleColorShader);

			for (int i = 1; i < 16; i++) {
				TileFactory.setBaseMapTexture(i, new Texture(mActivity, "tile" + i + "_d.png"));
				TileFactory.setNormalMapTexture(i, new Texture(mActivity, "tile" + i + "_n.png"));
			}

			restartCurrentGame();

			float boardWidth = mTileVertexData.getBoundaries().getWidth() * mBoard.getWidth();

			// Initialize the buffers.
			float[] planeVertexData = {  boardWidth, -1.0f, -boardWidth,
										-boardWidth, -1.0f, -boardWidth,
										 boardWidth, -1.0f,  boardWidth,
										-boardWidth, -1.0f,  boardWidth };

			VertexData vd = MeshLoader.getVertexDataFromArray(planeVertexData);
			mPlane = new SimpleMesh(vd, mDepth2ColorShader);
			mPlane.setRenderMode(GLES20.GL_TRIANGLE_STRIP);
		} else {
			for (Tile t : mTiles) {
				t.reload(mActivity);
			}
			mPlane.reload();
			mLightingShader.reload(mActivity);
			mDepth2ColorShader.reload(mActivity);
			mSimpleColorShader.reload(mActivity);
		}

		mFont = new Font(mActivity, R.raw.arial, 0.9f * mDisplayDensity);
	}
	
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {

		mViewport[0] = 0;
		mViewport[1] = 0;
		mViewport[2] = width;
		mViewport[3] = height;

		GLES20.glViewport(0, 0, width, height);

		// Create the perspective projection matrix
		// Width will vary as per aspect ratio
		mScreenRatio = (float) width / Math.max(mViewport[3], 1);

		if (mScreenRatio < 1.0f)
			mCameraEye.y = -25.0f * mScreenRatio + 33.0f;

		float near		= 1.0f;
		float far		= 30.0f;
		float fov		= 45.0f;
		float top		= (float) Math.tan((float) (fov * (float) Math.PI / 360.0f)) * near;
		float bottom	= -top;
		float left		= mScreenRatio * bottom;
		float right		= mScreenRatio * top;

		Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);

		Matrix.setLookAtM(mVMatrix, 0,	mCameraEye.x,		mCameraEye.y,		mCameraEye.z,
										mCameraCenter.x,	mCameraCenter.y,	mCameraCenter.z,
										mCameraUp.x,		mCameraUp.y,		mCameraUp.z);
	}
	
	public Tile createTile(int number, int x, int y) {

		int nbrBoardSquaresX = mBoard.getWidth();
		int nbrBoardSquaresY = mBoard.getHeight();

		Tile tile = TileFactory.buildTile(number, nbrBoardSquaresX, nbrBoardSquaresY);
		tile.setPosX(x);
		tile.setPosY(y);

		mBoard.setObj(x, y, tile);
		mTiles.add(tile);

		return tile;
	}

	private boolean isSolved() {
		Tile t;
		for (int x = 0; x < mBoard.getWidth(); x++) {
			for (int y = 0; y < mBoard.getHeight(); y++) {
				t = (Tile) mBoard.getObj(x, y);
				if (t != null && !t.isInPlace()) {
					return false;
				}
			}
		}
		return true;
	}

	public void resetGameBoard() {
		// Remove every tile from the board.
		for (int x = 0; x < mBoard.getWidth(); x++) {
			for (int y = 0; y < mBoard.getHeight(); y++) {
				mBoard.setObj(x, y, null);
			}
		}
		// Place the tiles in the correct position on the board.
		for (Tile t : mTiles) {
			t.setPosX(t.getCorrectPosX());
			t.setPosY(t.getCorrectPosY());
			mBoard.setObj(t.getCorrectPosX(), t.getCorrectPosY(), t);
		}

		getMovableTiles();

		mTimer.stop();
		mNbrMoves = 0;

		flyEveryTileDown();

		mGameState = GAMESTATUS_STOPPED;
	}

	private void flyEveryTileDown() {
		for (Tile t : mTiles) {
			t.flyToPlace();
		}
	}

	private void getMovableTiles() {
		for (int x = 0; x < mBoard.getWidth(); x++) {
			for (int y = 0; y < mBoard.getHeight(); y++) {
				if (mBoard.getObj(x, y) == null) {
					mCubeDown = (Tile) mBoard.getObj(x, y - 1);
					mCubeUp = (Tile) mBoard.getObj(x, y + 1);
					mCubeLeft = (Tile) mBoard.getObj(x + 1, y);
					mCubeRight = (Tile) mBoard.getObj(x - 1, y);
				}
			}
		}
	}

	/**
	 * Musn't run in renderer thread.
	 * 
	 * @param iterations
	 */
	public void shuffle(int iterations) {
		Random rand = new Random();
		int last = -1;
		int n;
		boolean moved = false;

		mNbrMoves = 0;
		mGameState = GAMESTATUS_SHUFFLING;
		Tile.setSpeedFactor(1.3f);

		for (int i = 0; i < iterations; i++) {
			n = -1;
			moved = false;

			// Loop until a tile was moved
			while (!moved) {
				n = rand.nextInt(4);

				// Guarantee that every tile is stopped.
				for (Tile t : mTiles) {
					if (!t.isStopped())
						n = -1;
				}

				// Do not move the same tile
				if (n == 0 && last != 1 || n == 1 && last != 0 || n == 2
						&& last != 3 || n == 3 && last != 2) {
					switch (n) {
					case 0:
						moved = moveTile(mCubeRight, 1, 0);
						break;
					case 1:
						moved = moveTile(mCubeLeft, -1, 0);
						break;
					case 2:
						moved = moveTile(mCubeUp, 0, -1);
						break;
					case 3:
						moved = moveTile(mCubeDown, 0, 1);
						break;
					}
					if (moved)
						last = n;
				}
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Tile.setSpeedFactor(1.0f);
		mGameState = GAMESTATUS_INPROGRESS;
	}

	private boolean moveTile(Tile c, int xOfsset, int yOfsset) {
		if (c == null)
			return false;

		int targetX = c.getPosX() + xOfsset;
		int targetY = c.getPosY() + yOfsset;

		if (targetX < 0 || targetY < 0 || targetX >= mBoard.getWidth()
				|| targetY >= mBoard.getHeight())
			return false;

		Object targetTile = mBoard.getObj(targetX, targetY);

		if (targetTile == null) {
			mBoard.setObj(c.getPosX(), c.getPosY(), null);
			mBoard.setObj(targetX, targetY, c);

			mCubeDown	= (Tile) mBoard.getObj(c.getPosX(), c.getPosY() - 1);
			mCubeUp		= (Tile) mBoard.getObj(c.getPosX(), c.getPosY() + 1);
			mCubeLeft	= (Tile) mBoard.getObj(c.getPosX() + 1, c.getPosY());
			mCubeRight	= (Tile) mBoard.getObj(c.getPosX() - 1, c.getPosY());

			if (targetX > c.getPosX()) {
				c.changeState(Tile.ROTATING_POSX);
			}

			else if (targetX < c.getPosX()) {
				c.changeState(Tile.ROTATING_NEGX);
			}

			if (targetY > c.getPosY()) {
				c.changeState(Tile.ROTATING_POSZ);
			}

			else if (targetY < c.getPosY()) {
				c.changeState(Tile.ROTATING_NEGZ);
			}

			return true;
		}

		return false;
	}

	

	private void createBoard3x3() {
		// Check if there is already a thread shuffling.
		if (mShufflerThread != null && mShufflerThread.isAlive()) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mToast.setText(R.string.new_game_in_progress);
					mToast.show();
				}
			});
			return;
		}
		mGameState = GAMESTATUS_STOPPED;
		if (mCurrentSize != SIZE3X3) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.create3x3(this);
			initGame();
		}

		mShufflerThread = new Thread(new Runnable() {
			public void run() {
				shuffle(SHUFFLE_ITERATIONS);
				mGameState = GAMESTATUS_INPROGRESS;
			}
		});
		mShufflerThread.start();
	}

	private void createBoard3x4() {
		// Check if there is already a thread shuffling.
		if (mShufflerThread != null && mShufflerThread.isAlive()) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mToast.setText(R.string.new_game_in_progress);
					mToast.show();
				}
			});
			return;
		}
		mGameState = GAMESTATUS_STOPPED;
		if (mCurrentSize != SIZE3X4) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.create3x4(this);
			initGame();
		}

		mShufflerThread = new Thread(new Runnable() {
			public void run() {
				shuffle(SHUFFLE_ITERATIONS);
				mGameState = GAMESTATUS_INPROGRESS;
			}
		});
		mShufflerThread.start();
	}

	private void createBoard4x4() {

		// Check if there is already a thread shuffling.
		if (mShufflerThread != null && mShufflerThread.isAlive()) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mToast.setText(R.string.new_game_in_progress);
					mToast.show();
				}
			});
			return;
		}
		mGameState = GAMESTATUS_STOPPED;
		if (mCurrentSize != SIZE4X4) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.create4x4(this);
			initGame();
		}

		mShufflerThread = new Thread(new Runnable() {
			public void run() {
				shuffle(SHUFFLE_ITERATIONS);
				mGameState = GAMESTATUS_INPROGRESS;
			}
		});
		mShufflerThread.start();
	}

	/**
	 * Check if every tile is in the stopped state.
	 * 
	 * @return
	 */
	private boolean isEveryTileStoped() {
		for (Tile t : mTiles) {
			if (!t.isStopped())
				return false;
		}
		return true;
	}

	public void restartCurrentGame() {
		
		mCurrentSize = -1;
		mTiles.clear();
		mTimer.reset();

		// Get the best score.
		mBestTime = Score.getValue(mActivity, mGameId1, mGameId2, "time");
		mBestMoves = Score.getValue(mActivity, mGameId1, mGameId2, "moves");

		showGameStartMsg();

		if (mGameId1.compareTo("classic") == 0) {
			if (mGameId2.compareTo("3x3") == 0)
				createBoard3x3();
			else if (mGameId2.compareTo("3x4") == 0)
				createBoard3x4();
			else if (mGameId2.compareTo("4x4") == 0)
				createBoard4x4();
		} else if (mGameId1.compareTo("challenge") == 0) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.loadBoardFromAsset(mActivity, this, LEVEL_FILE, Integer.parseInt(mGameId2));
			mGameState = GAMESTATUS_LIMITEDMOVES;
			initGame();
		}

		// Create a thread to start the time when the tiles are ready.
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; !mTimer.isRunning() && i < 100; i++) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (isEveryTileStoped() && mGameState != GAMESTATUS_SHUFFLING) {
						mTimer.start();
					}
				}
			}
		}).start();
	}

	private void showGameStartMsg() {

		StringBuilder desc = new StringBuilder();

		if (mGameId1.equals("classic")) {
			// Show best time if there is one.
			if (mBestTime != -1)
				desc.append("Best time: " + Timer.msToStringMMSS(mBestTime * 1000) + " in " + mBestMoves + " moves");
		} else if (mGameId1.equals("challenge")) {
			desc.append(FiffteenPuzzleBoardLoader.getLevelName(mActivity, LEVEL_FILE, Integer.parseInt(mGameId2)));
			desc.append("\n");
			desc.append(FiffteenPuzzleBoardLoader.getLevelDesc(mActivity, LEVEL_FILE, Integer.parseInt(mGameId2)));
			if (mBestTime != -1) {
				desc.append("\nBest time: " + Timer.msToStringMMSS(mBestTime * 1000));
			}
		}

		final String msg = desc.toString();

		if (msg != null && !msg.equals("")) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mToast.setText(msg);
					mToast.show();
				}
			});
		}
	}

	private void makeEveryTileJump() {
		for (Tile t : mTiles) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			t.upAndBack();
		}
	}

	private boolean slideRight(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosY() == mCubeRight.getPosY()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX() + 1, mSelectedTile.getPosY());
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX() + 2, mSelectedTile.getPosY());
			moveTile(c2, 1, 0);
			moveTile(c1, 1, 0);
		}
		return moveTile(t, 1, 0);
	}

	private boolean slideLeft(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosY() == mCubeLeft.getPosY()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX() - 1, mSelectedTile.getPosY());
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX() - 2, mSelectedTile.getPosY());
			moveTile(c2, -1, 0);
			moveTile(c1, -1, 0);
		}
		return moveTile(t, -1, 0);
	}

	private boolean slideDown(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosX() == mCubeDown.getPosX()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() + 1);
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() + 2);
			moveTile(c2, 0, 1);
			moveTile(c1, 0, 1);
		}
		return moveTile(t, 0, 1);
	}

	private boolean slideUp(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosX() == mCubeUp.getPosX()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() - 1);
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() - 2);
			moveTile(c2, 0, -1);
			moveTile(c1, 0, -1);
		}
		return moveTile(t, 0, -1);
	}

	private void checkForMovement() {
		boolean moved = false;

		if (mMotionEventDown) {
			mMotionEventDown = false;
			mTouchDownCoord = Utils.getProjectCoords((int) mTouchPrevX, mViewport[3] - (int) mTouchPrevY, mVMatrix, mProjMatrix, mViewport);

			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

			for (Tile t : mTiles) {

				// Build Model View and Model View Projection Matrices.
				Matrix.multiplyMM(mMVMatrix, 0, mVMatrix, 0, t.getModelMatrix(), 0);
				Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);
				mSimpleColorShader.enable();
				mSimpleColorShader.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);
				t.renderBoundingBox();
				mSimpleColorShader.disable();
			}

			mPickedColor = Utils.getPixelColor((int) mTouchPrevX, mViewport[3] - (int) mTouchPrevY);
			mSelectedTile = null;
			for (Tile t : mTiles) {
				if (t.isPicked(mPickedColor)) {
					mSelectedTile = t;
				}
			}
			if (mGameState == GAMESTATUS_SOLVED && mSelectedTile != null && mSelectedTile.isStopped())
				mSelectedTile.upAndBack();
		} else if (mMotionEventMove) {
			mMotionEventMove = false;

			if (mSelectedTile != null) {
				// Get 3D coordinate.
				mTouchUpCoord = Utils.getProjectCoords((int) mTouchPrevX, mViewport[3] - (int) mTouchPrevY, mVMatrix, mProjMatrix, mViewport);

				mTouchUpCoord.sub(mTouchDownCoord);

				if (mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES || mGameState == GAMESTATUS_STOPPED) {
					if (Math.abs(mTouchUpCoord.x) > Math.abs(mTouchUpCoord.z)) {
						if (mTouchUpCoord.x > 0.0f && mCubeRight != null && mSelectedTile.getPosY() == mCubeRight.getPosY()) {
							moved = slideRight(mSelectedTile);
						} else if (mTouchUpCoord.x < 0.0f && mCubeLeft != null && mSelectedTile.getPosY() == mCubeLeft.getPosY()) {
							moved = slideLeft(mSelectedTile);
						}
					} else {
						if (mTouchUpCoord.z < 0.0f && mCubeUp != null && mSelectedTile.getPosX() == mCubeUp.getPosX()) {
							moved = slideUp(mSelectedTile);
						} else if (mTouchUpCoord.z > 0.0f && mCubeDown != null && mSelectedTile.getPosX() == mCubeDown.getPosX()) {
							moved = slideDown(mSelectedTile);
						}
					}
					if (moved)
						mSelectedTile = null;
				}

				mCameraTargetRot.y -= mDeltaMoveX * mDisplayDensity * mFramRateCtl.getFrameFactor() * 0.1f;
				mCameraTargetRot.x -= mDeltaMoveY * mDisplayDensity * mFramRateCtl.getFrameFactor() * 0.1f;
			}
		}

		if (moved && (mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES))
			mNbrMoves++;
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		mFramRateCtl.FrameStart();

		if (mGameState == GAMESTATUS_WINANIMATION && mAnimationThread != null && !mAnimationThread.isAlive()) {
			if (isEveryTileStoped()) {
				mGameState = GAMESTATUS_SOLVED;
				if (mGameId1.equals("challenge")) {
					mActivity.runOnUiThread(new Runnable() {
						public void run() {
							showChalengeWonDialog();
						}
					});
				} else if (mGameId1.equals("classic")) {
					mActivity.runOnUiThread(new Runnable() {
						public void run() {
							showClassicWonDialog();
						}
					});
				}
			}
		}

		if ((mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES) && isSolved()) {
			mAnimationThread = new Thread(new Runnable() {
				public void run() {
					mGameState = GAMESTATUS_WINANIMATION;
					mTimer.stop();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					makeEveryTileJump();
				}
			});
			mAnimationThread.start();
		} else if (mGameState == GAMESTATUS_LIMITEDMOVES && mNbrAllowedMoves != -1 && mNbrMoves >= mNbrAllowedMoves && isEveryTileStoped()) { // Loosing conditions.
			mGameState = GAMESTATUS_MAX_MOVES_REACHED;
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(1000);
						showMovesExaustedDialog();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}

		for (Tile t : mTiles) {
			t.update(mFramRateCtl.getFrameFactor());
		}

		mCameraTargetRot.add(mGyroscopeValues);
		mCameraTargetRot.lerp(Vec3.ZERO, 0.1f);

		mCameraRot.lerp(mCameraTargetRot, 0.05f);

		Matrix.setLookAtM(mVMatrix, 0,	mCameraEye.x,		mCameraEye.y,		mCameraEye.z,
										mCameraCenter.x,	mCameraCenter.y,	mCameraCenter.z,
										mCameraUp.x,		mCameraUp.y,		mCameraUp.z);

		Matrix.rotateM(mVMatrix, 0, -mCameraRot.x * 2.0f * mFramRateCtl.getFrameFactor(), 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(mVMatrix, 0, mCameraRot.y * 2.0f * mFramRateCtl.getFrameFactor(), 0.0f, 0.0f, 1.0f);


		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

		mDepth2ColorShader.enable();
		mDepth2ColorShader.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);
		mPlane.render();
		mDepth2ColorShader.disable();

		checkForMovement();

		// Clear the color picking stuff and render the main scene
		// Render to main buffer

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		// Enable the shader program
		mLightingShader.enable();
		mLightingShader.uniformMatrix4fv("uVMatrix", 1, false, mVMatrix, 0);
		mLightingShader.uniform3f("aLightPos", mLightPos.x, mLightPos.y, mLightPos.z);
		mLightingShader.uniform1i("uBaseMap", 0);
		mLightingShader.uniform1i("uNormalMap", 1);

		for (Tile t : mTiles) {

			// Build Model View and Model View Projection Matrices
			Matrix.multiplyMM(mMVMatrix, 0, mVMatrix, 0, t.getModelMatrix(), 0);
			Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);

			// Specify matrix information
			mLightingShader.uniformMatrix4fv("uMVMatrix", 1, false, mMVMatrix, 0);
			mLightingShader.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);

			// Render the model using previously specified data
			t.render();
		}

		mLightingShader.disable();

		mFont.enable(getViewportWidth(), getViewportHeight());
		
		//mFont.Print(350, 10, "FPS: " + mFramRateCtl.getFPS());

		if (mGameState == GAMESTATUS_SHUFFLING) {
			mFont.Print(10, (int)mFont.getVInterval() - 10, "Shuffling...");
		} else  {
			if (mGameState == GAMESTATUS_SOLVED) {
				mFont.Print(10, 10 + (int)(mFont.getVInterval() * 2), "- SOLVED -");
			} else if (mGameState == GAMESTATUS_MAX_MOVES_REACHED) {
				mFont.Print(10, 10 + (int)(mFont.getVInterval() * 2), "- MOVES EXAUSTED -");
			}

			mFont.Print(10, 10, "Time: " + mTimer.getElapsedTimeString());
	
			StringBuilder sb = new StringBuilder(); 
			sb.append("Moves: " + mNbrMoves);
			if (mNbrAllowedMoves != -1)
				sb.append("/" + mNbrAllowedMoves);
			
			mFont.Print(10, 10 + (int) (mFont.getVInterval()), sb.toString());
		}
	
		mFont.disable();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

		float x = event.getX();
		float y = event.getY();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mMotionEventDown = true;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			mDeltaMoveX = (x - mTouchPrevX) / mDisplayDensity;
			mDeltaMoveY = (y - mTouchPrevY) / mDisplayDensity;
			mMotionEventMove = true;
		} else {
			mDeltaMoveX = 0.0f;
			mDeltaMoveY = 0.0f;
		}

		mTouchPrevX = x;
		mTouchPrevY = y;

		return true;
	}


	/**
	 * Must run on UI thread.
	 */
	public void showMovesExaustedDialog() {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Runnable r = new Runnable() {
						public void run() {
							restartCurrentGame();
						}
					};
					mGLSurfaceView.queueEvent(r);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					mActivity.terminateWithInterstitial();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(R.string.msg_out_of_moves)
				.setPositiveButton(R.string.btn_restart, dialogClickListener)
				.setNegativeButton(R.string.btn_main_menu, dialogClickListener).show();
	}

	private void updateScore() {
		int time = mTimer.getElapsedSeconds();

		if (mBestTime == -1 || time < mBestTime) {
			Score.updateValue(mActivity, mGameId1, mGameId2, "time", mTimer.getElapsedSeconds());
			Score.updateValue(mActivity, mGameId1, mGameId2, "moves", mNbrMoves);
		}
	}

	private String getGameOverMsg() {
		int time = mTimer.getElapsedSeconds();
		StringBuilder msg = new StringBuilder();

		if (mBestTime == -1 || time < mBestTime) {
			if (mBestTime != -1)
				msg.append("You beat the best time!");
			else
				msg.append("Puzzle solved!");
		} else {
			msg.append("Puzzle solved!");
		}

		msg.append("\nYour time: " + Timer.msToStringMMSS(time * 1000) + " in " + mNbrMoves + " moves");
		if (mBestTime != -1) {
			msg.append("\nBest time: " + Timer.msToStringMMSS(mBestTime * 1000) + " in " + mBestMoves + " moves");
		}

		return msg.toString();
	}

	/**
	 * Must run on UI thread.
	 */
	public void showClassicWonDialog() {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Runnable r = new Runnable() {
						public void run() {
							restartCurrentGame();
						}
					};
					mGLSurfaceView.queueEvent(r);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					mActivity.terminateWithInterstitial();
					break;
				}
			}
		};

		updateScore();

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(getGameOverMsg())
				.setPositiveButton(R.string.btn_restart, dialogClickListener)
				.setNegativeButton(R.string.btn_main_menu, dialogClickListener).show();
	}

	/**
	 * Must run on UI thread.
	 */
	public void showChalengeWonDialog() {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Load the next challenge.
					mGameId2 = String.valueOf((Integer.parseInt(mGameId2) + 1));
					showGameStartMsg();
					Runnable r = new Runnable() {
						public void run() {
							restartCurrentGame();
						}
					};
					mGLSurfaceView.queueEvent(r);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					mActivity.terminateWithInterstitial();
					break;
				}
			}
		};

		updateScore();

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(getGameOverMsg())
				.setPositiveButton(R.string.btn_next, dialogClickListener)
				.setNegativeButton(R.string.btn_main_menu, dialogClickListener).show();
	}
	
	@Override
	public void createBoard(int size) {
		switch (size) {
		case FifteenPuzzleBoard.SIZE3X3:
			mCurrentSize = FifteenPuzzleBoard.SIZE3X3;
			mBoard = new GameBoard(3, 3);
			break;
		case FifteenPuzzleBoard.SIZE3X4:
			mCurrentSize = FifteenPuzzleBoard.SIZE3X4;
			mBoard = new GameBoard(3, 4);
			break;
		case FifteenPuzzleBoard.SIZE4X4:
			mCurrentSize = FifteenPuzzleBoard.SIZE4X4;
			mBoard = new GameBoard(4, 4);
			break;
		default:
			break;
		}
	}
	
	public void cleanGameBoard() {
		mTiles.clear();		
		Tile.resetColor();
	}

	@Override
	public void setNbrAllowedMoves(int nbrAllowedMoves) {
		mNbrAllowedMoves = nbrAllowedMoves;
	}
	
	public void initGame() {
		getMovableTiles();
		flyEveryTileDown();
		mNbrMoves = 0;
	}
	
	@Override
	public void onResume() {
		mTimer.resume();
	}

	@Override
	public void onPause() {
		mTimer.pause();
	}

	@Override
	public int getViewportWidth() {
		return mViewport[2];
	}

	@Override
	public int getViewportHeight() {
		return mViewport[3];
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_GYROSCOPE:
			mGyroscopeValues.assign(event.values);
			break;
		}
	}
	
	@Override
	public void onSurfaceDestroyed() {
		;
	}

	@Override
	public void onScreenOnOffToggled(boolean isScreenOn) {
		;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		;
	}
}
