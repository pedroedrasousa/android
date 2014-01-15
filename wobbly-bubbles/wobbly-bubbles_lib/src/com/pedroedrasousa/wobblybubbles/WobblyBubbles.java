package com.pedroedrasousa.wobblybubbles;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import android.opengl.GLES20;

import com.google.android.noisealert.SoundMeter;
import com.pedroedrasousa.engine.*;
import com.pedroedrasousa.engine.shader.*;
import com.pedroedrasousa.engine.livewallpaper.*;
import com.pedroedrasousa.engine.marchingcubes.*;

import com.pedroedrasousa.wobblybubbleslib.R;


public class WobblyBubbles implements WallpaperRenderer, OnTouchListener, OnSharedPreferenceChangeListener {

	public static final int		BYTES_PER_FLOAT			= 4;
	public static final String	SHARED_PREFERENCES_NAME	= "wbsettings";
	public final static float	ERROR_DELTA				= 1.0f;
	public final static int		CFG_VERSION				= 1;
	
	private static final float	BG_SCROLL_FACTOR			= 0.005f;
	private static final float	CAMERA_TRANSLATION_FACTOR	= 1.2f;
	private static final float	GRID_ROTATION_FACTOR		= 30.0f;
	private static final float	META_MOTION_FACTOR			= 0.0008f;
	private static final float	MAX_CURRENT_SPEED   		= 4.0f;
	private static final float	MAX_ROTATION_FACTOR 		= 50.0f;
	private static final float	MAX_TRANSLATION_Z 			= 100.0f;
	private static final float	MAX_TRANSLATION_XY 			= 20.0f;
	private static final float	SOUND_REACTION_FACTOR		= 0.1f;
	
	
	private Context				mContext;
	private SharedPreferences	mSharedPreferences;
	
	// Lock used to control IsoSurface mesh array access
	private final ReentrantLock mIsoSurfaceLock = new ReentrantLock();
	
    // Matrices
	private float[] mProjectionMatrix	= new float[16];
	private float[] mViewMatrix			= new float[16];
	private float[] mModelMatrix		= new float[16];
	private float[] mModelViewMatrix	= new float[16];
	private float[] mMVPMatrix			= new float[16];
	
	// Shader programs
	private SimpleShaderProg mMetaShaderProg;
	private SimpleShaderProg mRefractionShaderProg;
	private SimpleShaderProg mFinalShaderProg;
	
	// Shader handlers
	private int mmShdHdlRefractMVPMatrix;
	private int mmShdHdlRefractVertexCoord;
	private int mmShdHdlRefractTexBg;
	private int mmShdHdlRefractTexMeta;
	private int mmShdHdlRefractPrevFrame;
	private int mmShdHdlRefractTexBgCoords;
	private int mmShdHdlRefractTexMetaCoords;
	private int mmShdHdlRefractRefractFactor;
	private int mShdHdlRefractTrailFactor;
	
	// More shader handlers
	private int mShdHdlMetaMVPMatrix;
	private int mShdHdlMetaMVMatrix;
	private int mShdHdlMetaVertPos;
	private int mShdHdlMetaLightPos;
	private int mShdHdlMetaVertNormal;
	private int mShdHdlMetaVertColor;
	private int mShdHdlMetaViewPos;
	private int mShdHdlMetaAmbLight;
	private int mShdHdlMetaSpecLight;
    private int mShdHdlRefractMetaTransparency;
    private int mShdHdlMetaPointSize;    
    
	// Even more shader handlers
    private int mShdHdlFinalMVPMatrix;
	private int mShdHdlFinalMVMatrix;
	private int mShdHdlFinalVertexPos;
	private int mShdHdlFinalMetaTex;
	private int mShdHdlFinalBgTex;
	private int mShdHdlSimpleTextureCoord;
	private int mShdHdlFinalTexBgCoords;
	private int mShdHdlFinalBgBrightness;
	
	// Frame rate stuff
	private float	mFPS;				// Number of frames per second
	private float	mFrameDelta;		// Delta between frames
	private float   mOneOverFpsLimit;	// In milliseconds, used to limit frame rate
	private long	mLastFPSUpdate;		// Last time FPS counter was updated
	private long	mFrameStartTime;
	private long	mFrameEndTime;
	
	// Camera
	private boolean mIsCameraRefreshNeeded;		// Indicates if view matrix needs to be recomputed
	private Vec3	mCameraEye    		= new Vec3(0.0f, 0.0f, 0.0f);
	private Vec3	mCameraCenter 		= new Vec3(0.0f, 0.0f, 0.0f);
	private Vec3	mCameraUp     		= new Vec3(0.0f, 1.0f, 0.0f);
	private float	mCameraTargetPosX	= 0.0f;
	private float	mCameraTargetPosY	= 0.0f;
	private float	mCameraTargetPosZ	= 0.0f;
	private float	mCameraTargetPosZ2	= 0.0f;
	private float	mCameraBaseZoffset;
	// Viewport information (x0, y0, x1, y1)
    private int[]	mViewport			= new int[4];
	
	private int		mGLES20RenderMode	= GLES20.GL_TRIANGLES;
	private float	mPointSize;
	private float	mLineWidth;
	
	private boolean mIsLiveWallpaper			= false;
	private boolean mIsPreview					= false;	// true if this engine is running in preview mode -- that is, it is being shown to the user before they select it as the actual wallpaper.
	private boolean mOnOffsetsChangedSupported	= false;	// Assume onOffsetsChanged will not be called by launcher
	private boolean mUseAlternateScrolling		= false;	// Don't use onOffsetsChanged
	private boolean	mIsBgRefreshNeeded			= true;
	private float	mBgScrollOffset;
	private float	mBgTargetScrollOffset;
	
	private float	mGridRotationFactor;
	private float	mCameraTranslationFactor;
	private float	mMetaMotionFactor;
	private float	mMaxCurrentSpeed;
	
	private EngineUtils	mEngineUtils;
	private Font		mFont;
	
	private IsoSurface	mIsoSurface;
	private final Vec3	mGridSize = new Vec3(20.0f, 20.0f, 8.0f);	// IsoSurface cubes grid size
	
	final int			mMetaballsVBO[] = new int[3];	// Vertex buffer object to store metaballs mesh info
	
	private int			mNbrMeta;	
	private MetaBall[]	mMetaBall;
	private Vec3[]		mMetaColorPalette;
	private String		mBgImage;
	private float		mZoom;
	private float		mRefractFactor;
	private int			mRenderMode;
	private int			mMetaDetail;
	private float		mMetaSize;
	private boolean 	mReactToSound;
	private float		mBgBrightness;

	private FrameBuffer	mAuxFrameBuffer;
	private FrameBuffer	mAuxFrameBuffer2[];

	// Lighting
	private final float[]	mlightPos	= new float[] {50.0f, -50.0f, 10.0f, 1.0f};
	private float mAmbientLightFactor;
	private float mSpecularLightFactor;

	private float	mScreenRatio;			// Ratio screen width and height
	private float	mBgScreenRatio;			// Ratio between background and viewport
	private float	mbgScrollOffset = 0.5f;	// Between 0 and 1
	
	private boolean mBgScrollingEnabled;
	private boolean mReactToSwipe;
	private float	mMetaTransparency;
    private float	mMetaColorFactor;	
	private boolean mShowFPS;
	private float	mTrailFactor;
	
	private float	mTargetSpeed;
	private float	mCurrentSpeed = 0.0f;

	// IsoSurface grid rotation
	private Vec3	mIsoSurfaceRotation			= new Vec3();
	private float	mIsoSurfaceRotationFactor	= 1.0f;
	private float	mTargetRotationFactor		= 1.0f;
	
    private Texture mBgTexture			= new Texture();
    private Texture mTexBlack			= new Texture();

	// Metaballs movement radius
	private float		mMetaOffsetX		= 5.0f;
	private float		mMetaOffsetY		= 5.0f;
	
	private boolean		mIsSurfaceCreated = false;
	private boolean		mReloadNeeded = false;	// Used to instruct the thread which has the OpenGl context to do the reload.
	

	public WobblyBubbles(Context context) {
		mContext = context;
		initSharedPreferences();
		initMetaballs();
		initIsoSurface();
		mFrameStartTime  = SystemClock.uptimeMillis();
    	WobblyBubblesUtils.requestTapjoyConnect(mContext);
	}
	
	private SharedPreferences getSharedPreferences() {
		if (mSharedPreferences == null) {
			mSharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
			mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		}
		return mSharedPreferences;
	}
	
	private void setMetaColor(Vec3[] colorPalette) {
		
		if (mMetaBall == null || colorPalette == null) {
			return;
		}
		
		for (int i = 0; i < mMetaBall.length; i++) {
			// Assign a color 
			mMetaBall[i].setColor(colorPalette[i % colorPalette.length]);
		}
	}
	
	public void initMetaballs() {

		mIsoSurfaceLock.lock();

		try {
			mMetaBall = new MetaBall[mNbrMeta];
			for (int i = 0; i < mMetaBall.length; i++) {
				mMetaBall[i] = new MetaBall(mMetaSize, new Vec3(0.0f, 0.0f, 0.0f));
				
				// Set positioning bounds
				mMetaBall[i].setMaxOffsetX(mMetaOffsetX);
				mMetaBall[i].setMaxOffsetY(mMetaOffsetY);
				
				mMetaBall[i].setDeltaX((float)(Math.random() * Math.PI - Math.PI * 0.5));
				mMetaBall[i].setDeltaY((float)(Math.random() * Math.PI - Math.PI * 0.5));
				
				mMetaBall[i].setVelX(1.0f + (float)(Math.random() * 0.5));
				mMetaBall[i].setVelY(1.0f + (float)(Math.random() * 0.5));
			}
			
			// Assign a colors
			setMetaColor(mMetaColorPalette);

		} finally {
			mIsoSurfaceLock.unlock();
		}
	}
	
	private void initIsoSurface() {
		
		mIsoSurfaceLock.lock();

		try {
			Vec3 offset	= new Vec3(0.0f, 0.0f, 5.0f);
			mIsoSurface = new IsoSurface(mGridSize, offset, mMetaDetail, mMetaDetail, (int)((float)mMetaDetail / 2.0f));
		} finally {
			mIsoSurfaceLock.unlock();
		}
	}
	
	private void initSharedPreferences() {
		
		getSharedPreferences();
				
		if (mSharedPreferences.contains("cfg_version") == false) {
			// No configuration, reset all preferences.
			resetCfg();
		}
		
		// Load the values from shared preferences
		loadSharedPreferences(mSharedPreferences);
	}
	
	public void initShaders() {
		
		/**
		 * TODO: Use the AbstractShaderProg ability to cache uniform and attribute locations
		 * instead of storing them here.
		 */
		
		mMetaShaderProg = new SimpleShaderProg(mContext, R.raw.metaball_vert, R.raw.metaball_frag);
		mShdHdlMetaMVPMatrix	= mMetaShaderProg.getUniformLocation("uMVPMatrix");
		mShdHdlMetaMVMatrix		= mMetaShaderProg.getUniformLocation("uMVMatrix");
		mShdHdlMetaVertPos		= mMetaShaderProg.getAttribLocation("aVertPos");
		mShdHdlMetaVertColor	= mMetaShaderProg.getAttribLocation("aColor");
		mShdHdlMetaVertNormal	= mMetaShaderProg.getAttribLocation("aNormal"); 
		mShdHdlMetaLightPos		= mMetaShaderProg.getUniformLocation("uLightPos");
		mShdHdlMetaViewPos		= mMetaShaderProg.getUniformLocation("uViewPosition");
		mShdHdlMetaAmbLight		= mMetaShaderProg.getUniformLocation("uAmbientLight");
		mShdHdlMetaSpecLight	= mMetaShaderProg.getUniformLocation("uSpecFactor");
		mShdHdlMetaPointSize	= mMetaShaderProg.getUniformLocation("uPointSize");
		
		mRefractionShaderProg = new SimpleShaderProg(mContext, R.raw.refraction_vert, R.raw.refraction_frag);
		mmShdHdlRefractMVPMatrix 		= mRefractionShaderProg.getUniformLocation("uMVPMatrix");
        mmShdHdlRefractRefractFactor	= mRefractionShaderProg.getUniformLocation("uRefractOffsetFactor");
        mmShdHdlRefractTexBg   			= mRefractionShaderProg.getUniformLocation("uTexBg");
        mmShdHdlRefractTexMeta  		= mRefractionShaderProg.getUniformLocation("uTexMeta");
        mmShdHdlRefractPrevFrame		= mRefractionShaderProg.getUniformLocation("uTexPrevFrame");
        mmShdHdlRefractVertexCoord    	= mRefractionShaderProg.getAttribLocation("aVertexCoords");
        mmShdHdlRefractTexBgCoords		= mRefractionShaderProg.getAttribLocation("aTexBgCoords");
        mmShdHdlRefractTexMetaCoords	= mRefractionShaderProg.getAttribLocation("aTexMetaCoords");
        mShdHdlRefractMetaTransparency	= mRefractionShaderProg.getUniformLocation("uMetaTransparency");
        mShdHdlRefractTrailFactor		= mRefractionShaderProg.getUniformLocation("uTrailFactor");
        
		mFinalShaderProg = new SimpleShaderProg(mContext, R.raw.final_vert, R.raw.final_frag);
		mShdHdlFinalMVPMatrix		= mFinalShaderProg.getUniformLocation("uMVPMatrix");
		mShdHdlFinalMVMatrix		= mFinalShaderProg.getUniformLocation("uMVMatrix");
		mShdHdlFinalVertexPos		= mFinalShaderProg.getAttribLocation("aVertPos");
		mShdHdlFinalMetaTex			= mFinalShaderProg.getUniformLocation("uTexture");
		mShdHdlFinalBgTex			= mFinalShaderProg.getUniformLocation("uTexBg");
		mShdHdlSimpleTextureCoord	= mFinalShaderProg.getAttribLocation("aTexCoord");
		mShdHdlFinalTexBgCoords		= mFinalShaderProg.getAttribLocation("aTexBgCoords");
		mShdHdlFinalBgBrightness	= mFinalShaderProg.getUniformLocation("uBgBrightness");
	}
	
	// Background coordinates with corrected aspect ratio
	private float mBgCoord1;
	private float mBgCoord2;

	private void calcBgCoords() {
	    float bgRatio = (float) mBgTexture.getOriginalWidth() / mBgTexture.geOriginaltHeight();
	    // Get the resulting ratio
	    mBgScreenRatio = mScreenRatio / bgRatio;
	    
	    // If background scrolling is enabled add an offset
	    if (mBgScrollingEnabled == true)
	    	mBgScreenRatio += mbgScrollOffset;
	    
	    if (mBgScreenRatio < 1.0f) {
		    mBgCoord1 = (1.0f - mBgScreenRatio) * 0.5f;
		    mBgCoord2 = mBgScreenRatio + (1.0f - mBgScreenRatio) * 0.5f;
	    } else {
		    mBgCoord1 = (1.0f - 1.0f / mBgScreenRatio) * 0.5f;
		    mBgCoord2 = 1.0f / mBgScreenRatio + (1.0f - 1.0f / mBgScreenRatio) * 0.5f;
	    }
	}
	
	private void loadBgTexture() {
		try {
			if (mBgImage.contains("/")) {
				// Load image from sdcard
				mBgTexture.loadFromPath(mContext, mBgImage, 1024);
			} else {
				// Load image from assets
				if (mBgImage.equals("black.jpg")) {
					// Image is black, 1x1 px is enough.
					mBgTexture.loadFromAsset(mContext, mBgImage, 1);
				} else {
					mBgTexture.loadFromAsset(mContext, mBgImage, 1024);
				}
			}
		} catch(Exception e) {
			// Something wrong occurred, load the default image
			mBgTexture.loadFromAsset(mContext, DefaultValues.BG_IMAGE, 1024);
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			
		mIsSurfaceCreated = true;
	
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClearDepthf(1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		
		initShaders();
        
        mEngineUtils = new EngineUtils(mContext);

		mFont = new Font(this.mContext, R.drawable.arial, 1.0f);
		
		// Create a new data store for the buffer object that will contain the metaballs mesh vertex data
		GLES20.glGenBuffers(3, mMetaballsVBO, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mMetaballsVBO[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, IsoSurface.ARRAY_SIZE, mIsoSurface.getVertexBuffer(), GLES20.GL_STREAM_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		// Destroy the framebuffers if they exist. They will have to be recreated.
		if (mAuxFrameBuffer != null) {
			mAuxFrameBuffer.destroy();
			mAuxFrameBuffer = null;
		}
		if (mAuxFrameBuffer2 != null) {
			if (mAuxFrameBuffer2[0] != null) {
				mAuxFrameBuffer2[0].destroy();
				mAuxFrameBuffer2[0] = null;
			}
			if (mAuxFrameBuffer2[1] != null) {
				mAuxFrameBuffer2[1].destroy();
				mAuxFrameBuffer2[1] = null;
			}
			mAuxFrameBuffer2 = null;
		}
		
		loadBgTexture();
		mTexBlack.loadFromAsset(mContext, "black.jpg", 1);
		
	    // If in preview mode center the view
		if (mIsSurfaceCreated == false) {
			mBgScrollOffset = mBgTargetScrollOffset = (mIsPreview == true) ? 0.5f : mBgScrollOffset;
		}
		
		// If application is resuming and surface is recreated start from zero speed to avoid noticeable frame jumps
		mCurrentSpeed = 0.0f;
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
	    mViewport[0] = 0;
	    mViewport[1] = 0;
	    mViewport[2] = width;
	    mViewport[3] = height;

		GLES20.glViewport(0, 0, width, height);
		
	    // Create the perspective projection matrix
	    // Width will vary as per aspect ratio
		mScreenRatio  = (float) width /  Math.max(mViewport[3], 1);
		
	    final float near	= 1.0f;
	    final float far		= 100.0f;
	    final float fov		= 90.0f;
	    final float top		= (float)Math.tan((float)(fov * (float)Math.PI / 360.0f)) * near;
	    final float bottom	= -top;
	    final float left	= mScreenRatio * bottom;
	    final float right	= mScreenRatio * top;

	    Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	    	    
	    if (mAuxFrameBuffer == null) {
		    mAuxFrameBuffer = new FrameBuffer(this, width, height);
	    }
	    
	    if (mAuxFrameBuffer2 == null) {
		    mAuxFrameBuffer2 = new FrameBuffer[2];
		    mAuxFrameBuffer2[0] = new FrameBuffer(this, width, height);
		    mAuxFrameBuffer2[1] = new FrameBuffer(this, width, height);
	    }
	    
	    // Dirty way to keep objects size with different screen ratios
	    mCameraBaseZoffset = 35.0f - 5.0f * mScreenRatio;
	    
	    mCameraEye.z = mCameraTargetPosZ = mCameraTargetPosZ2 = mCameraBaseZoffset - mZoom;
	    
		calcBgCoords();
		mIsBgRefreshNeeded = true;
	    mIsCameraRefreshNeeded = true;
	}
	
	private void update() {
		
		float normalScale;
		float squaredDistance;
		float length;
		final Vec3 ballToPoint = new Vec3();
		
		if (mReactToSound && mSoundAmpDiff > 0.1f) {
			mCurrentSpeed += (float)mSoundAmpDiff * SOUND_REACTION_FACTOR;
			mIsoSurfaceRotationFactor += (float)mSoundAmpDiff * SOUND_REACTION_FACTOR;
		}

		mCurrentSpeed = Math.min(mCurrentSpeed, mMaxCurrentSpeed);
			
		mIsoSurfaceRotationFactor	= Math.min(mIsoSurfaceRotationFactor, MAX_ROTATION_FACTOR);
		
		if (mIsCameraRefreshNeeded) {
			mIsCameraRefreshNeeded = refreshCameraPosition();
		}
		
		if (mIsBgRefreshNeeded) {
			mIsBgRefreshNeeded = refreshBgTexCoords();
		}
				
		mIsoSurfaceRotation.z += mIsoSurfaceRotationFactor * mCurrentSpeed * mFrameDelta;
		
		mIsoSurfaceRotationFactor = mIsoSurfaceRotationFactor * 0.99f + (mTargetRotationFactor * 0.01f) * mFrameDelta;
		
		// Avoid changing the speed abruptly so we can have a smooth animation
		if (mCurrentSpeed < mTargetSpeed - 0.001f) {
			mCurrentSpeed += Math.abs(mCurrentSpeed - mTargetSpeed) * mFrameDelta;
		} else if (mCurrentSpeed > mTargetSpeed + 0.001f) {
			mCurrentSpeed -= Math.abs(mCurrentSpeed - mTargetSpeed) * mFrameDelta;
		}
		
		// Pull everything out into local variables, avoiding the lookups in the loop.
		MetaBall[] metaBall = mMetaBall;
	    int nbrMetaBalls = metaBall.length;
	    for (int i = 0; i < nbrMetaBalls; i++) {
			
	    	metaBall[i].refreshPosition(mCurrentSpeed * mFrameDelta);
			
			//for (int j = 0; j < mMarchingCubes.mVertices.length; j++) {
	    	for (GridVertex gridVertex : mIsoSurface.mVertices) {
				if (i == 0) {
					// Reset the vertices values
					// Coordinates
					gridVertex.setValue(0.0f);
					// Normals
					gridVertex.mVertexData[3] = 0;
					gridVertex.mVertexData[4] = 0;
					gridVertex.mVertexData[5] = 0;
					// Color
					gridVertex.mVertexData[6] = 0;
					gridVertex.mVertexData[7] = 0;
					gridVertex.mVertexData[8] = 0;
				}
				
				ballToPoint.x = gridVertex.mVertexData[0] - metaBall[i].getPos().x;
				ballToPoint.y = gridVertex.mVertexData[1] - metaBall[i].getPos().y;
				ballToPoint.z = gridVertex.mVertexData[2] - metaBall[i].getPos().z;
				
				// Get squared distance from ball to point
				squaredDistance = (ballToPoint.x * ballToPoint.x) + (ballToPoint.y * ballToPoint.y) + (ballToPoint.z * ballToPoint.z);
				if(squaredDistance == 0.0f)
					squaredDistance = 0.0001f;

				// value = r^2/d^2
				gridVertex.addToValue(metaBall[i].getSquaredRadius() / squaredDistance);
				
				// normal = (r^2 * v) / d^4
				normalScale = metaBall[i].getSquaredRadius() / (squaredDistance * squaredDistance);
				gridVertex.mVertexData[3] += ballToPoint.x * normalScale;
				gridVertex.mVertexData[4] += ballToPoint.y * normalScale;
				gridVertex.mVertexData[5] += ballToPoint.z * normalScale;
				
				// color
				gridVertex.mVertexData[6] += metaBall[i].getColor().x * metaBall[i].getSquaredRadius() / squaredDistance * mMetaColorFactor;
				gridVertex.mVertexData[7] += metaBall[i].getColor().y * metaBall[i].getSquaredRadius() / squaredDistance * mMetaColorFactor;
				gridVertex.mVertexData[8] += metaBall[i].getColor().z * metaBall[i].getSquaredRadius() / squaredDistance * mMetaColorFactor;

				// Normalize normals
				if (i == nbrMetaBalls - 1) {

					length = (float)Math.sqrt(  gridVertex.mVertexData[3] * gridVertex.mVertexData[3]
											  + gridVertex.mVertexData[4] * gridVertex.mVertexData[4]
											  + gridVertex.mVertexData[5] * gridVertex.mVertexData[5]);
					
					if (length != 0) {
						length = 1.0f / length;
					}
					
					gridVertex.mVertexData[3] *= length;
					gridVertex.mVertexData[4] *= length;
					gridVertex.mVertexData[5] *= length;
				}
			}
		}
	}
	
	private FloatBuffer mVertexData = null;	// Used to reference the float buffer of the stuff being rendered
	private int mFbIdx = 0;					// Frame buffer to render index

	private void renderFrame(GL10 gl) {
		
        int vertexNbr = 0;				// Metaballs mesh won't have a constant number of vertices, use this variable to store it
        
		GLES20.glLineWidth(mLineWidth);
		
		// Build model matrix
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mIsoSurfaceRotation.z, 0.0f, 0.0f, 1.0f);
        
	    // Build Model View and Model View Projection Matrices
	    Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
	    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);

        // Render to an auxiliary frame buffer
	    // Metaballs with color and lighting, no background.
		mAuxFrameBuffer.bind();

        mIsoSurface.refreshSurfaceVertexBuffer(1.0f);	// Calculate metaballs mesh vertex position, normals and color.
        mVertexData	= mIsoSurface.getVertexBuffer();	// Get the generated data
        vertexNbr	= mIsoSurface.getNbrVertices();
        
        mMetaShaderProg.useProgram();
        
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mMetaballsVBO[0]);
		mVertexData.position(0);
		// Using glBufferSubData rather than glBufferData avoids the cost of reallocating the data store
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexNbr * 9 * BYTES_PER_FLOAT, mVertexData);
        
        // Assign the vertex data to the shader program attributes.
	    GLES20.glVertexAttribPointer(mShdHdlMetaVertPos, 3, GLES20.GL_FLOAT, false, 9*BYTES_PER_FLOAT, 0);
	    GLES20.glEnableVertexAttribArray(mShdHdlMetaVertPos);
	    GLES20.glVertexAttribPointer(mShdHdlMetaVertNormal, 3, GLES20.GL_FLOAT, false, 9*BYTES_PER_FLOAT, 3*BYTES_PER_FLOAT);
	    GLES20.glEnableVertexAttribArray(mShdHdlMetaVertNormal);
		GLES20.glVertexAttribPointer(mShdHdlMetaVertColor, 3, GLES20.GL_FLOAT, false, 9*BYTES_PER_FLOAT, 6*BYTES_PER_FLOAT);
		GLES20.glEnableVertexAttribArray(mShdHdlMetaVertColor);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
        GLES20.glUniformMatrix4fv(mShdHdlMetaMVMatrix, 1, false, mModelViewMatrix, 0);
	    GLES20.glUniformMatrix4fv(mShdHdlMetaMVPMatrix, 1, false, mMVPMatrix, 0);
	    GLES20.glUniform3f(mShdHdlMetaLightPos, mlightPos[0], mlightPos[1], mlightPos[2]);
	    GLES20.glUniform3f(mShdHdlMetaViewPos, mCameraEye.x, mCameraEye.y, mCameraEye.z);	    
	    GLES20.glUniform1f(mShdHdlMetaAmbLight, mAmbientLightFactor);
	    GLES20.glUniform1f(mShdHdlMetaSpecLight, mSpecularLightFactor);
	    GLES20.glUniform1f(mShdHdlMetaPointSize, mPointSize);	    
	    
	    GLES20.glDrawArrays(mGLES20RenderMode, 0, vertexNbr);	

	    mAuxFrameBuffer.unbind();
	    
	    
	    //******************** Render metaballs with refraction ********************//
	    
	    mAuxFrameBuffer2[mFbIdx].bind();
	    
		mRefractionShaderProg.useProgram();
		
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBgTexture.getHandle());
        GLES20.glUniform1i(mmShdHdlRefractTexBg, 0);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mAuxFrameBuffer.getTextureDataHandler());
        GLES20.glUniform1i(mmShdHdlRefractTexMeta, 1);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        if (mTrailFactor < 0.01f) {
        	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexBlack.getHandle());
        }
        else {
        	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mAuxFrameBuffer2[(mFbIdx == 0)? 1 : 0].getTextureDataHandler());
        }
        GLES20.glUniform1i(mmShdHdlRefractPrevFrame, 2);
        
        
        GLES20.glVertexAttribPointer(mmShdHdlRefractVertexCoord, 2, GLES20.GL_FLOAT, false, 0, mEngineUtils.getQuadVertexBuffer());
        GLES20.glEnableVertexAttribArray(mmShdHdlRefractVertexCoord);

        GLES20.glVertexAttribPointer(mmShdHdlRefractTexBgCoords, 2, GLES20.GL_FLOAT, false, 0, mEngineUtils.getQuadTexCoords());
        GLES20.glEnableVertexAttribArray(mmShdHdlRefractTexBgCoords);
        
        GLES20.glVertexAttribPointer(mmShdHdlRefractTexMetaCoords, 2, GLES20.GL_FLOAT, false, 0, mEngineUtils.getQuadUnitTexCoords());
        GLES20.glEnableVertexAttribArray(mmShdHdlRefractTexMetaCoords);
        
		GLES20.glUniformMatrix4fv(mmShdHdlRefractMVPMatrix, 1, false, mEngineUtils.getOrthoMVPMatrix(), 0);
		GLES20.glUniform1f(mmShdHdlRefractRefractFactor, mRefractFactor);
        GLES20.glUniform1f(mShdHdlRefractMetaTransparency, mMetaTransparency);
        GLES20.glUniform1f(mShdHdlRefractTrailFactor, mTrailFactor);
        
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		mAuxFrameBuffer.unbind();
		
		//******************** Render final scene ********************//
		
		mFinalShaderProg.useProgram();
		
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mAuxFrameBuffer2[mFbIdx].getTextureDataHandler());
        GLES20.glUniform1i(mShdHdlFinalMetaTex, 0);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBgTexture.getHandle());
        GLES20.glUniform1i(mShdHdlFinalBgTex, 1);
                
        GLES20.glUniform1f(mShdHdlFinalBgBrightness, mBgBrightness);
        
        mFbIdx = (mFbIdx == 0)? 1 : 0;	// Switch framebuffer
        
        GLES20.glVertexAttribPointer(mShdHdlFinalVertexPos, 2, GLES20.GL_FLOAT, false, 0, mEngineUtils.getQuadVertexBuffer());
        GLES20.glEnableVertexAttribArray(mShdHdlFinalVertexPos);
        
        GLES20.glVertexAttribPointer(mShdHdlSimpleTextureCoord, 2, GLES20.GL_FLOAT, false, 0, mEngineUtils.getQuadUnitTexCoords());
        GLES20.glEnableVertexAttribArray(mShdHdlSimpleTextureCoord);
        
        GLES20.glVertexAttribPointer(mShdHdlFinalTexBgCoords, 2, GLES20.GL_FLOAT, false, 0, mEngineUtils.getQuadTexCoords());
        GLES20.glEnableVertexAttribArray(mShdHdlFinalTexBgCoords);
        
        GLES20.glUniformMatrix4fv(mShdHdlFinalMVPMatrix, 1, false, mEngineUtils.getOrthoMVPMatrix(), 0);
        GLES20.glUniformMatrix4fv(mShdHdlFinalMVMatrix, 1, false, EngineUtils.identityMatrix, 0);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
		if (mShowFPS && !mIsLiveWallpaper) {
		    mFont.enable(getViewportWidth(), getViewportHeight());
		    mFont.Print(5, 5, "FPS: " + Math.round(mFPS));
		    
		    mFont.disable();
		}
	}

	public void onDrawFrame(GL10 gl) {
		
		mFrameEndTime	= SystemClock.uptimeMillis();
		mFrameDelta		= mFrameEndTime - mFrameStartTime;
		mFrameStartTime = SystemClock.uptimeMillis();
		
		// Limit frame rate in Live Wallpaper mode
		if (mIsLiveWallpaper && mFrameDelta < mOneOverFpsLimit) {
			try {
				Thread.sleep((int)mOneOverFpsLimit - (int)mFrameDelta);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Refresh frame rate counter once every second
		if (mFrameEndTime - mLastFPSUpdate > 1000.0f) {
			mLastFPSUpdate = mFrameEndTime;
			mFPS = 1000.0f / mFrameDelta;
		}
		
		// If the delta between frames is to long set it to zero to avoid frame skipping
		if (mFrameDelta > 500.0f) {
			mFrameDelta		= 0.0f;
		}
		
		// Convert delta to seconds
		mFrameDelta /= 1000.0f;
		
		// Reload background texture if needed
		if (mReloadNeeded == true) {
			loadBgTexture();
			calcBgCoords();
			refreshBgTexCoords();
			mReloadNeeded = false;
		}

		// To prevent configurations from changing while rendering frame
		mIsoSurfaceLock.lock();
		
		try {
			update();
			renderFrame(gl);
		} finally {
			mIsoSurfaceLock.unlock();
		}
	}
	
	private boolean refreshBgTexCoords() {
		
		boolean altered = false;
    	float x1, x2, y1, y2;
		
		if (mEngineUtils == null) {
			return altered;
		}
		
		if (mOnOffsetsChangedSupported == false) {
			// Background
			if (mBgScrollOffset < mBgTargetScrollOffset - 0.001f) {
				mBgScrollOffset += Math.abs(mBgScrollOffset - mBgTargetScrollOffset) * mFrameDelta;
				altered = true;
			} else if (mBgScrollOffset > mBgTargetScrollOffset + 0.001f) {
				mBgScrollOffset -= Math.abs(mBgScrollOffset - mBgTargetScrollOffset) * mFrameDelta;
				altered = true;
			}
		}

	    // Calculate texture coordinates maintaining aspect ratio and centering the image
	    if (mBgScreenRatio > 1.0f) {
	    	// Height is larger than width
	    	if (mBgScrollingEnabled == true) {
		    	x1 = 0.0f + (mbgScrollOffset * mBgScrollOffset) / mBgScreenRatio;
		    	x2 = 1.0f - (mbgScrollOffset - (mbgScrollOffset * mBgScrollOffset)) / mBgScreenRatio;
	    	} else {
		    	x1 = 0.0f;
		    	x2 = 1.0f;
	    	}
	    	
	    	y1 = mBgCoord1;
	    	y2 = mBgCoord2;
	    }
	    else {
	    	// Width is larger than height
	    	if (mBgScrollingEnabled == true) {
	    		x1 = mBgCoord1 + (mbgScrollOffset * mBgScrollOffset);
	    		x2 = mBgCoord2 - (mbgScrollOffset - (mbgScrollOffset * mBgScrollOffset));
	    	} else {
	    		x1 = mBgCoord1;
	    		x2 = mBgCoord2;
	    	}
	    	y1 = 0.0f;
	    	y2 = 1.0f;
	    }
	    
		mEngineUtils.setQuadTexCoords(x1, y2, x2, y2, x1, y1, x2, y1);
		
		return altered;
	}
	
	private boolean refreshCameraPosition() {

		boolean altered = false;
		
		// Limit movement factor
		float factor = Math.min(mFrameDelta, 0.1f);
		
		// Zoom
		mCameraTargetPosZ = mCameraBaseZoffset - mZoom;
		
		/**
		 *  TODO: Use the linear interpolation method of Vec3
		 *  instead of having all this mess in here.
		 */
		
		if (mCameraEye.x < mCameraTargetPosX - ERROR_DELTA) {
			mCameraEye.x += Math.abs(mCameraTargetPosX - mCameraEye.x) * 1.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		else if (mCameraEye.x > mCameraTargetPosX + ERROR_DELTA) {
			mCameraEye.x -= Math.abs(mCameraEye.x - mCameraTargetPosX) * 1.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		
		if (mCameraEye.y < mCameraTargetPosY - ERROR_DELTA) {
			mCameraEye.y += Math.abs(mCameraTargetPosY - mCameraEye.y) * 1.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		else if (mCameraEye.y > mCameraTargetPosY + ERROR_DELTA) {
			mCameraEye.y -= Math.abs(mCameraEye.y - mCameraTargetPosY) * 1.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		
		//Center target
		if (mCameraTargetPosX < 0.0f - ERROR_DELTA) {
			mCameraTargetPosX += Math.abs(0.0f - mCameraTargetPosX) * 15.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		else if (mCameraEye.x > 0.0f + ERROR_DELTA) {
			mCameraTargetPosX -= Math.abs(mCameraTargetPosX - 0.0f) * 15.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		
		if (mCameraTargetPosY < 0.0f - ERROR_DELTA) {
			mCameraTargetPosY += Math.abs(0.0f - mCameraTargetPosY) * 15.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		else if (mCameraTargetPosY > 0.0f + ERROR_DELTA) {
			mCameraTargetPosY -= Math.abs(mCameraTargetPosY - 0.0f) * 15.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}


		if (mCameraEye.z < mCameraTargetPosZ - ERROR_DELTA) {
			mCameraEye.z += Math.abs(mCameraTargetPosZ - mCameraEye.z) * 4.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		else if (mCameraEye.z > mCameraTargetPosZ + ERROR_DELTA) {
			mCameraEye.z -= Math.abs(mCameraEye.z -mCameraTargetPosZ) * 4.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		
		if (mCameraEye.z < mCameraTargetPosZ2 - ERROR_DELTA) {
			mCameraEye.z += Math.abs(mCameraTargetPosZ2 - mCameraEye.z) * 4.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		else if (mCameraEye.z > mCameraTargetPosZ2 + ERROR_DELTA) {
			mCameraEye.z -= Math.abs(mCameraEye.z - mCameraTargetPosZ2) * 4.0f * CAMERA_TRANSLATION_FACTOR * factor;
			altered = true;
		}
		
		
		if (mCameraTargetPosZ2 < mCameraTargetPosZ - ERROR_DELTA) {
			mCameraTargetPosZ2 += Math.abs(mCameraTargetPosZ - mCameraTargetPosZ2) * 5.5f * factor;
			altered = true;
		}
			
		if (mCameraTargetPosZ2 > mCameraTargetPosZ + ERROR_DELTA) {
			mCameraTargetPosZ2 -= Math.abs(mCameraTargetPosZ2 -mCameraTargetPosZ) * 5.5f * factor;
			altered = true;
		}
		
		// Check if camera target position is inside boundaries
		mCameraCenter.x = Math.min(mCameraCenter.x, MAX_TRANSLATION_XY);
		mCameraCenter.x = Math.max(mCameraCenter.x, -MAX_TRANSLATION_XY);			
		mCameraCenter.y = Math.min(mCameraCenter.y, MAX_TRANSLATION_XY);
		mCameraCenter.y = Math.max(mCameraCenter.y, -MAX_TRANSLATION_XY);
		
		// Set the same position for the camera eye
		mCameraCenter.x = mCameraEye.x;
		mCameraCenter.y = mCameraEye.y;

		Matrix.setLookAtM(mViewMatrix, 0, mCameraEye.x,    mCameraEye.y,    mCameraEye.z,
										  mCameraCenter.x, mCameraCenter.y, mCameraCenter.z,
										  mCameraUp.x,     mCameraUp.y,     mCameraUp.z);
		
		return altered;
	}
	
	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
				
		if (mUseAlternateScrolling) {
			mOnOffsetsChangedSupported = false;
			return;
		}
		
		if (mOnOffsetsChangedSupported == false && xOffset != 0.0f && xOffset != 0.5f) {
			mOnOffsetsChangedSupported = true;			
		}
		
		if (mBgScrollingEnabled) {
			mBgScrollOffset = xOffset;
			refreshBgTexCoords();
		}
	}

	private Vec2 mPrevTouchCoords = new Vec2();
	
	@SuppressWarnings("unused")
	private boolean mIsTouchActionDown = false;

	private long		mTouchDownTime;	// Instant when a MotionEvent.ACTION_DOWN was received
	
	public boolean onTouch(View view, MotionEvent event) {
		
		// Limit movement factor
		float factor = Math.min(mFrameDelta, 0.1f);
		
		if (event == null)
			return false;
		
		float x = event.getX();
		float y = event.getY();
		long tapDuration;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mIsTouchActionDown = true;
			mTouchDownTime = SystemClock.uptimeMillis();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mIsTouchActionDown = false;
			tapDuration = SystemClock.uptimeMillis() - mTouchDownTime;
			// Was the screen tapped?
			if(tapDuration < 500) {
				if (mReactToSwipe) {
					mCurrentSpeed += 1000000.0f / (float)tapDuration * mMetaMotionFactor;
					if (Float.isNaN(mCurrentSpeed)) {
						mCurrentSpeed = 0.0f;
					} else {
						mCurrentSpeed = Math.min(mCurrentSpeed, mMaxCurrentSpeed);
					}
					mIsoSurfaceRotationFactor = 1000000.0f / (float)tapDuration * mGridRotationFactor;
					mCameraTargetPosZ2 += 2000.0f / (float)tapDuration * mCameraTranslationFactor;
					mCameraTargetPosZ2 = Math.min(mCameraTargetPosZ2, MAX_TRANSLATION_Z);
					mIsCameraRefreshNeeded = true;
				}
			}
		}
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			final Vec2 delta = new Vec2();
			delta.x = (x - mPrevTouchCoords.x) / factor * 0.01f;
			delta.y = (y - mPrevTouchCoords.y) / factor * 0.01f;
			// Clamp offset values
			delta.x = Math.min(delta.x, 100.0f);
			delta.x = Math.max(delta.x, -100.0f);
			delta.y = Math.min(delta.y, 100.0f);
			delta.y = Math.max(delta.y, -100.0f);
			// Swipe vector length
			float offset = (float)Math.sqrt(delta.x * delta.x + delta.y * delta.y) / mFrameDelta;
			
			if (mReactToSwipe) {
				mIsoSurfaceRotationFactor = offset * mGridRotationFactor;
				
				mCameraTargetPosX -= delta.x * mCameraTranslationFactor;
				mCameraTargetPosY -= delta.y * mCameraTranslationFactor;
		
				//
				// Validate and imit the target position
				
				if (Float.isNaN(mCameraTargetPosX)) {
					mCameraTargetPosX = 0.0f;
				} else {		
					mCameraTargetPosX = Math.min(mCameraTargetPosX, MAX_TRANSLATION_XY * 5.0f);
					mCameraTargetPosX = Math.max(mCameraTargetPosX, -MAX_TRANSLATION_XY * 5.0f);						
				}
				
				if (Float.isNaN(mCameraTargetPosY)) {
					mCameraTargetPosY = 0.0f;
				} else {
					mCameraTargetPosY = Math.min(mCameraTargetPosY, MAX_TRANSLATION_XY * 5.0f);
					mCameraTargetPosY = Math.max(mCameraTargetPosY, -MAX_TRANSLATION_XY * 5.0f);
				}
				
				mIsCameraRefreshNeeded = true;
			
				mCurrentSpeed += offset * mMetaMotionFactor;
				
				if (Float.isNaN(mCurrentSpeed)) {
					mCurrentSpeed = 0.0f;
				} else {
					mCurrentSpeed = Math.min(mCurrentSpeed, mMaxCurrentSpeed);
				}
			}

			if (mOnOffsetsChangedSupported == false) {
				if (mBgScrollingEnabled) {
					mBgTargetScrollOffset -= delta.x * BG_SCROLL_FACTOR;
					// Clamp values to [0,1]
					mBgTargetScrollOffset = Math.max(mBgTargetScrollOffset, 0.0f);
					mBgTargetScrollOffset = Math.min(mBgTargetScrollOffset, 1.0f);
					mIsBgRefreshNeeded = true;
				}
			}
		}
		
		mPrevTouchCoords.x = x;
		mPrevTouchCoords.y = y;
		
		return true;
	}
	
	/**
	 * TODO: Create some configuration manager class.
	 */
	private void resetCfg() {
		
		SharedPreferences.Editor editor =  mSharedPreferences.edit();

		editor.putInt("cfg_version", CFG_VERSION);
		editor.putString("bg_image", SettingsActivity.mSettingsPreset[0].getBgImage());
		editor.putInt("bg_brightness", (int)(SettingsActivity.mSettingsPreset[0].getBgBrightness() * 100.0f));
		editor.putString("fps_limit", Integer.toString(SettingsActivity.mSettingsPreset[0].mFPSLimit));
		editor.putInt("detail", SettingsActivity.mSettingsPreset[0].mDetail);
		editor.putInt("size", SettingsActivity.mSettingsPreset[0].mMetaSize);
		editor.putInt("zoom", SettingsActivity.mSettingsPreset[0].mZoomFactor);
		editor.putString("render_mode", Integer.toString(SettingsActivity.mSettingsPreset[0].mRenderMode));
		editor.putInt("speed", SettingsActivity.mSettingsPreset[0].mSpeedFactor);
		editor.putInt("dist_offset", (int)(SettingsActivity.mSettingsPreset[0].mReflectionFactor * 100.0f));
		editor.putInt("color_factor", (int)(SettingsActivity.mSettingsPreset[0].mMetaColorFactor * 100.0f));
		editor.putInt("transparency", (int)(SettingsActivity.mSettingsPreset[0].mMetaTransparencyFactor * 100.0f));
		editor.putBoolean("react_scrolling", SettingsActivity.mSettingsPreset[0].mReactToTouch);
		editor.putBoolean("show_fps", SettingsActivity.mSettingsPreset[0].mShowFPS);
		editor.putString("nbr_bubbles", Integer.toString(SettingsActivity.mSettingsPreset[0].mNbrMeta));
		editor.putBoolean("scrolling_wallpaper", SettingsActivity.mSettingsPreset[0].isScrollingWallpaper());
		editor.putBoolean("sound_reaction", SettingsActivity.mSettingsPreset[0].mReactToSound);
		editor.putInt("ambient_light", (int)(SettingsActivity.mSettingsPreset[0].mAmbientLight * 100.0f));
		editor.putInt("specular_light", (int)(SettingsActivity.mSettingsPreset[0].mSpecularLight * 100.0f));
		editor.putInt("trail_factor", (int)(SettingsActivity.mSettingsPreset[0].mTrailFactor * 100.0f));
	
		for (int i = 0; i < SettingsActivity.mColorPalettePresets[0].length; i++) {
			editor.putFloat("color_palette_r" + (i + 1), SettingsActivity.mColorPalettePresets[0][i].x);
			editor.putFloat("color_palette_g" + (i + 1), SettingsActivity.mColorPalettePresets[0][i].y);
			editor.putFloat("color_palette_b" + (i + 1), SettingsActivity.mColorPalettePresets[0][i].z);
		}
			
		editor.putInt("palette_size", SettingsActivity.mColorPalettePresets[0].length);
		
		editor.commit();
		
		// Load the values from shared preferences
		loadSharedPreferences(mSharedPreferences);
	}
	
	public void loadSharedPreferences(SharedPreferences prefs) {

		mShowFPS				= prefs.getBoolean("show_fps", DefaultValues.SHOW_FPS);
		mReactToSwipe			= prefs.getBoolean("react_scrolling", DefaultValues.REACT_SCROLLING);
		mMetaColorFactor		= (float)prefs.getInt("color_factor", DefaultValues.COLOR_FACTOR) * 0.1f;
		mMetaTransparency		= (float)prefs.getInt("transparency", DefaultValues.META_TRANSPARENCY) * 0.01f;
		mBgScrollingEnabled		= prefs.getBoolean("scrolling_wallpaper", DefaultValues.SCROLLING_WALLPAPER);		
		mRefractFactor			= (float)prefs.getInt("dist_offset", DefaultValues.REFRACT_OFFSET) * 0.01f;
		mAmbientLightFactor		= (float)prefs.getInt("ambient_light", 50) * 0.003f;
		mSpecularLightFactor	= (float)prefs.getInt("specular_light", 50) * 0.003f;
		mBgBrightness			= ((float)prefs.getInt("bg_brightness", 50) + 100.0f) * 0.01f;
		mTrailFactor			= ((float)prefs.getInt("trail_factor", 0) + 10.0f) * 0.085f;
		mTrailFactor			= (float) Math.log10(mTrailFactor);
		mOneOverFpsLimit		= 1000.0f / (float)Integer.parseInt( prefs.getString("fps_limit", DefaultValues.FPS_LIMIT) );
		int renderMode			= Integer.parseInt( prefs.getString("render_mode", DefaultValues.RENDER_MODE) );
		setRenderMode(renderMode);
		mReactToSound			= prefs.getBoolean("sound_reaction", DefaultValues.REACT_TO_SOUND);		
		float motionFactor		= (float)prefs.getInt("speed", DefaultValues.SPEED);
		setMotionFactor(motionFactor);
		mMetaDetail				= prefs.getInt("detail", DefaultValues.DETAIL);
		mZoom					= prefs.getInt("zoom", DefaultValues.ZOOM) * 0.15f;
		mNbrMeta				= Integer.parseInt( prefs.getString("nbr_bubbles", Integer.toString(DefaultValues.NBR_BUBBLES)));
		mMetaSize				= prefs.getInt("size", DefaultValues.SIZE) * 0.1f / (float)Math.max(mNbrMeta, 3) + 1.5f;
		mBgImage				= prefs.getString("bg_image", DefaultValues.BG_IMAGE);
		mBgBrightness			= ((float)prefs.getInt("bg_brightness", 50) + 100.0f) * 0.01f;

		loadColorPalette(prefs);
	}
	
	private void loadColorPalette(SharedPreferences prefs) {
		
		int paletteSize = prefs.getInt("palette_size", 0);
		
		mMetaColorPalette = new Vec3[paletteSize];
			
		for (int i = 0; i < paletteSize; i++) {
			mMetaColorPalette[i] = new Vec3();
			mMetaColorPalette[i].x = prefs.getFloat("color_palette_r" + (i + 1), 0.0f);
			mMetaColorPalette[i].y = prefs.getFloat("color_palette_g" + (i + 1), 0.0f);
			mMetaColorPalette[i].z = prefs.getFloat("color_palette_b" + (i + 1), 0.0f);
		}
			
		setMetaColor(mMetaColorPalette);		
	}

	/*
	 * Note: this method isn't called in the thread which has the OpenGL context
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			
		if (key != null) {
			if (key.equals("show_fps")) {
				mShowFPS = prefs.getBoolean("show_fps", DefaultValues.SHOW_FPS);
			} else if (key.equals("react_scrolling")) {
				mReactToSwipe = prefs.getBoolean("react_scrolling", DefaultValues.REACT_SCROLLING);
			} else if (key.equals("color_factor")) {
				mMetaColorFactor	= (float)prefs.getInt("color_factor", DefaultValues.COLOR_FACTOR) * 0.1f;
			} else if (key.equals("transparency")) {
				mMetaTransparency	= (float)prefs.getInt("transparency", DefaultValues.META_TRANSPARENCY) * 0.01f;
			} else if (key.equals("scrolling_wallpaper")) {
				mBgScrollingEnabled	= prefs.getBoolean("scrolling_wallpaper", DefaultValues.SCROLLING_WALLPAPER);		
			} else if (key.equals("dist_offset")) {
				mRefractFactor		= (float)prefs.getInt("dist_offset", DefaultValues.REFRACT_OFFSET) * 0.01f;
			} else if (key.equals("ambient_light")) {
				mAmbientLightFactor		= (float)prefs.getInt("ambient_light", 50) * 0.003f;
			} else if (key.equals("specular_light")) {
				mSpecularLightFactor		= (float)prefs.getInt("specular_light", 50) * 0.003f;
			} else if (key.equals("bg_brightness")) {
				mBgBrightness		= ((float)prefs.getInt("bg_brightness", 50) + 100.0f) * 0.01f;
			} else if (key.equals("trail_factor")) {
				mTrailFactor		= ((float)prefs.getInt("trail_factor", 0) + 10.0f) * 0.085f;
				mTrailFactor = (float) Math.log10(mTrailFactor);
			} else if (key.equals("fps_limit")) {
				mOneOverFpsLimit	= 1000.0f / (float)Integer.parseInt( prefs.getString("fps_limit", DefaultValues.FPS_LIMIT) );
			} else if (key.equals("render_mode")) {
				int renderMode = Integer.parseInt( prefs.getString("render_mode", DefaultValues.RENDER_MODE) );
				setRenderMode(renderMode);
			} else if (key.equals("sound_reaction")) {
				mReactToSound		= prefs.getBoolean("sound_reaction", DefaultValues.REACT_TO_SOUND);
				if (mActivityIsPaused == false) {
					if (mReactToSound) {
						initSoundMeter();
					} else {
						destroySoundMeter();
					}
				}
			} else if (key.equals("speed")) {
				float motionFactor = (float)prefs.getInt("speed", DefaultValues.SPEED);
				setMotionFactor(motionFactor);
			} else if (key.contains("palette")) {
				loadColorPalette(prefs);
			} else if (key.equals("nbr_bubbles")) {
				mNbrMeta	= Integer.parseInt( prefs.getString("nbr_bubbles", Integer.toString(DefaultValues.NBR_BUBBLES)));
				mMetaSize	= prefs.getInt("size", DefaultValues.SIZE) * 0.1f / (float)Math.max(mNbrMeta, 3) + 1.5f;
				initMetaballs();
			} else if (key.equals("detail")) {	
				mMetaDetail = prefs.getInt("detail", DefaultValues.DETAIL);
				initIsoSurface();
			} else if (key.equals("zoom")) {	
				mZoom = prefs.getInt("zoom", DefaultValues.ZOOM) * 0.15f;
				mIsCameraRefreshNeeded = true;
			} else if (key.equals("size")) {	
				mMetaSize = prefs.getInt("size", DefaultValues.SIZE) * 0.1f / (float)Math.max(mNbrMeta, 3) + 1.5f;
				if (mMetaBall != null) {
					for (int i = 0; i < mMetaBall.length; i++) {
						mMetaBall[i].setRadius(mMetaSize);
					}
				}
			} else if (key.equals("bg_image")) {	
				mBgImage = prefs.getString("bg_image", DefaultValues.BG_IMAGE);
				mReloadNeeded = true;	// Instruct the thread which has the opengl context to do the reload
			}
		}
	}

	public void setMotionFactor(float motionFactor) {
		mTargetSpeed				= (float)motionFactor * (float)motionFactor * 0.0002f;
		mGridRotationFactor			= GRID_ROTATION_FACTOR * (float)motionFactor * 0.03f;
		mCameraTranslationFactor	= CAMERA_TRANSLATION_FACTOR * (float)motionFactor * 0.03f;
		mMetaMotionFactor			= META_MOTION_FACTOR * (float)motionFactor * 0.03f;
		mMaxCurrentSpeed			= MAX_CURRENT_SPEED * (float)motionFactor * 0.03f;
	}
		
	public void setRenderMode(int renderMode) {
		
		mRenderMode = renderMode;
		
		if (mRenderMode == 1) {
			mLineWidth = 1.0f;
		} else if (mRenderMode == 2) {
			mLineWidth = 5.0f;
		} else if (mRenderMode == 3) {
			mPointSize = 10.0f;
		} else if (mRenderMode == 4) {
			mPointSize = 40.0f;
		} else if (mRenderMode == 5) {
			mPointSize = 80.0f;
		}
		
	    if (mRenderMode == 0) {
	    	mGLES20RenderMode = GLES20.GL_TRIANGLES;	
	    } else if (mRenderMode == 1 || mRenderMode == 2) {
	    	mGLES20RenderMode =GLES20.GL_LINES;
	    } else
	    	mGLES20RenderMode = GLES20.GL_POINTS;
	}
	
	public int getViewportWidth() {
		return mViewport[2];
	}
	
	public int getViewportHeight() {
		return mViewport[3];
	}
	
	public void setIsPreview(boolean isPreview) {
		mIsPreview = isPreview;
	}
	
	private boolean mActivityIsPaused = false;
		
	public void onScreenOnOffToggled(boolean isScreenOn) {
		if (isScreenOn == false) {
			// If screen was turned off set metaballs speed to zero
			// When the screen is turned back On they will gain speed gradually
			mCurrentSpeed = 0.0f;
		}
	}

	private Handler	mSoundMeterRunnableHdl;
	private double	mSoundAmp;
	private double	mSoundAmpDiff;
	private double	mBackgroundNoise;
	
	public void setIsLiveWallpaper(boolean isLiveWallpaper) {
		mIsLiveWallpaper = isLiveWallpaper;
	}
	
	private boolean initSoundMeter() {
		
		boolean res;
		
		res = SoundMeter.start();
		
		if (res == false) {
			Toast toast;
			toast = Toast.makeText(mContext, "Unable to start Sound Meter", Toast.LENGTH_LONG);
			toast.show();
		}
			
		if (mSoundMeterRunnableHdl == null) {
			mSoundMeterRunnableHdl = new Handler();
			mSoundMeterRunnableHdl.postDelayed(mPollTask, 500);
		}
		
		return true;
	}
	
	private boolean destroySoundMeter() {
		
		SoundMeter.stop();
		
		if (mSoundMeterRunnableHdl != null) {
			mSoundMeterRunnableHdl.removeCallbacks(mPollTask);
			mSoundMeterRunnableHdl = null;
		}
		
		// Reset values
	    mSoundAmp			= 0.0f;		    
	    mSoundAmpDiff		= 0.0f;	
	    mBackgroundNoise	= 0.0f;	
		
		return true;
	}
	
	public void onResume() {
		mFrameDelta = 0.01f;		// Set frame delta close to zero avoiding frame skips
		if (!mIsLiveWallpaper && mReactToSound) {
			initSoundMeter();
		}
		
		mActivityIsPaused = false;
	}
	
	public void onPause() {
		if (!mIsLiveWallpaper && mReactToSound) {
			destroySoundMeter();
		}
		
		mActivityIsPaused = true;
	}
	
	private Runnable mPollTask = new Runnable() {
        public void run() {
        	
            mSoundAmp = SoundMeter.getAmplitude();
            
            if (mBackgroundNoise == 0.0) {
            	mBackgroundNoise = mSoundAmp;
            } else {
            	mBackgroundNoise = mBackgroundNoise * 0.95 + mSoundAmp * 0.05;
            }
            
            mSoundAmpDiff = mSoundAmp - mBackgroundNoise;
            
            mSoundMeterRunnableHdl.postDelayed(mPollTask, 100);
        }
	};

	@Override
	public void onSurfaceDestroyed() {

	}
}
