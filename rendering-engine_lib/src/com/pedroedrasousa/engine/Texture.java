package com.pedroedrasousa.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Texture {
	
	private enum ResourceType { ASSET, PATH }
	
	private int		mWidth;
	private int		mHeight;
	private int		mOriginalWidth;
	private int		mOriginalHeight;
	private int[]	mHandle = new int[1];
	
	// Used to store the texture resource. Will be used for reloading.
	private String			mResource;
	private ResourceType	mResourceType;
	private int				mMaxSize;
	
	public Texture() {
		;
	}
	
	public Texture(Context context, String assetName) {
		loadFromAsset(context, assetName);
	}
	
	public void reload(Context context) {
		switch (mResourceType) {
		case ASSET:
			loadFromAsset(context, mResource, mMaxSize);
			break;
		case PATH:
			loadFromPath(context, mResource, mMaxSize);
			break;
		default:
			break;
		}
	}
	
	private void decodeBitmapBounds(InputStream stream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        mOriginalWidth  = options.outWidth;
        mOriginalHeight = options.outHeight;
	}
	
	private Bitmap decodeBitmap(InputStream stream, int maxSize) {
        int scale = 1;
        for (int size = Math.min(mOriginalHeight, mOriginalWidth); (size >> (scale-1)) > maxSize; scale++);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        return BitmapFactory.decodeStream(stream, null, options);
	}
	
	public void loadFromAsset(Context context, String assetName) {
		loadFromAsset(context, assetName, -1);
	}
	
	public void loadFromAsset(Context context, String assetName, int maxSize) {
		
	    Bitmap bitmap = null;
	    InputStream stream1 = null;
	    InputStream stream2 = null;
	    
	    mResource = new String(assetName);
	    mResourceType = ResourceType.ASSET;
	    mMaxSize = maxSize;

	    try {
		    stream1 = context.getAssets().open(assetName);
		    stream2 = context.getAssets().open(assetName);
		    
	        decodeBitmapBounds(stream1);
	        
	        if (maxSize <= 0) {
	        	maxSize = (mOriginalWidth > mOriginalHeight)? mOriginalWidth : mOriginalHeight;
	        }
	        
	        bitmap = decodeBitmap(stream2, maxSize);
	        bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, false);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	        if (stream1 != null) {
	        	try {
					stream1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if (stream2 != null) {
	        	try {
					stream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
   
        loadGLTextureFromBitmap(bitmap);
 
        // Bitmap data isn't needed anymore
        bitmap.recycle();
	}
	
	public void loadFromPath(final Context context, final String pathName, int maxSize) {
		
		File file;
	    InputStream stream1 = null;
	    InputStream stream2 = null;
	    Bitmap bitmap = null;
	    
	    mResource = new String(pathName);
	    mResourceType = ResourceType.PATH;
	    mMaxSize = maxSize;
	    
	    mResource = new String(pathName);

	    try {
		    file = new File(pathName);
		    stream1 = new FileInputStream(file);
		    stream2 = new FileInputStream(file);
	        decodeBitmapBounds(stream1);
	        bitmap = decodeBitmap(stream2, maxSize);
	        bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, false);
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    } finally {
	        if (stream1 != null) {
	        	try {
					stream1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if (stream2 != null) {
	        	try {
					stream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
   
        loadGLTextureFromBitmap(bitmap);
 
        // Bitmap data isn't needed anymore
        bitmap.recycle();
	}

	// Mostly like to cause OutOfMemoryError for big bitmaps
	public void loadFromResourceId(final Context context, final int resourceId, int width, int height) {
			 
	    Bitmap bitmap = null;
	    Resources resources = context.getResources();
	    
	    GLES20.glGenTextures(1, mHandle, 0);

    	Drawable image = resources.getDrawable(resourceId);
        float density = resources.getDisplayMetrics().density;

        mOriginalWidth  = (int)(image.getIntrinsicWidth() / density);
        mOriginalHeight = (int)(image.getIntrinsicHeight() / density);
        
        // Check if dimensions are valid, if not use the original ones
        if (width > 0 && height > 0) {
            mWidth  = width;
            mHeight = height;
        } else {
            mWidth  = mOriginalWidth;
            mHeight = mOriginalHeight;
        }

        image.setBounds(0, 0, mWidth, mHeight);
        
        // Create an empty, mutable bitmap
        bitmap = Bitmap.createBitmap( mWidth, mHeight, Bitmap.Config.ARGB_4444 );
        
        Canvas canvas = new Canvas(bitmap);	// Get a canvas to paint over the bitmap
        bitmap.eraseColor(0);

        image.draw(canvas);					// Draw the image onto the bitmap
    	
        loadGLTextureFromBitmap(bitmap);
 
        // Bitmap data isn't needed anymore
        bitmap.recycle();
	}
	
	public int loadGLTextureFromBitmap(Bitmap bitmap) {
		
	    GLES20.glGenTextures(1, mHandle, 0);

	    GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mHandle[0] );

	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR );
	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR );

	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT );
	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT );

	    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

	    return mHandle[0];
	}
	
	public int getHandle() {
		return mHandle[0];
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getOriginalWidth() {
		return mOriginalWidth;
	}
	
	public int geOriginaltHeight() {
		return mOriginalHeight;
	}
}
