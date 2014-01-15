package com.pedroedrasousa.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLU;

public class Utils {

	/**
	 * Reads the pixel color from the currently active framebuffer.
	 * @param x		Horizontal screen coordinate.
	 * @param y		Vertical screen coordinate.
	 * @return		The pixel color (R, G, B) ranging from 0 to 1
	 */
	public static Vec3 getPixelColor(int x, int y) {
		ByteBuffer px = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
	    
		// Read the pixel from the framebuffer.
	    GLES20.glReadPixels(x, y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, px);
	    
	    // Convert to normalized values [0, 1].
	    float r = (float)(((int)px.get(0)) & 0xFF) / 255.0f;
	    float g = (float)(((int)px.get(1)) & 0xFF) / 255.0f;
	    float b = (float)(((int)px.get(2)) & 0xFF) / 255.0f;
	    
	    return new Vec3(r, g, b);
	}
	
	/**
	 * Maps the specified window coordinates into view space coordinates.
	 * @param x				Specify the window x coordinate to be mapped.
	 * @param y				Specify the window y coordinate to be mapped.
	 * @param z				Specify the window z coordinate to be mapped.
	 * @param viewMatrix	Specifies the view matrix.
	 * @param projMatrix	Specifies the projection matrix.
	 * @param viewport		Specifies the viewport.
	 * @return				The coordinates projected into view space coordinates.
	 */
	public static Vec3 getProjectCoords(float x, float y, float z, float[] viewMatrix, float[] projMatrix, int[] viewport) {

		final Vec3 objCoordsVec	= new Vec3();
		float[] objCoords		= new float[4];	    

	    int res = GLU.gluUnProject(x, y, z, viewMatrix, 0, projMatrix, 0, viewport, 0, objCoords, 0);
		
	    if (res == GL10.GL_TRUE && objCoords[3] != 0) {
	    	objCoordsVec.scale(1.0f / objCoords[3]);
	    }
	    
	    objCoordsVec.assign(objCoords);

	    return objCoordsVec;
	}
	
	/**
	 * Maps the specified window coordinates into view space coordinates.
	 * Window Z coordinate will be read from the red color component of the active framebuffer.
	 * @param x				Specify the window x coordinate to be mapped.
	 * @param y				Specify the window y coordinate to be mapped.
	 * @param viewMatrix	Specifies the view matrix.
	 * @param projMatrix	Specifies the projection matrix.
	 * @param viewport		Specifies the viewport.
	 * @return				The coordinates projected into view space coordinates.
	 */
	public static Vec3 getProjectCoords(int x, int y, float[] viewMatrix, float[] projMatrix, int[] viewport) {
		final Vec3 color = getPixelColor((int)x, (int)y);
		return getProjectCoords(x, y, color.x, viewMatrix, projMatrix, viewport);
	}
	
	/**
	 * Gets the z buffer value for the given z coordinate in view space.
	 * @param zNear		The near clip plane value used to build the perspective matrix.
	 * @param zFar		The far clip plane value used to build the perspective matrix.
	 * @param viewDistZ The z coordinate in view space.
	 * @return			The z value as stored in the z buffer ranging from 0 to 1.
	 */
	public float getZBufferValue(float zNear, float zFar, float viewDistZ) {
        float a = (-zFar * zNear)/(zFar - zNear);
        float b = (zFar + zNear) / (2.0f * (zFar - zNear)) + 0.5f;        
        return 1 / viewDistZ * a + b;
	}
	
	public static void removeMatrixTranslation(float[] m) {
		m[12] = 0.0f;
		m[13] = 0.0f;
		m[14] = 0.0f;
	}
}
