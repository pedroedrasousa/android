package com.pedroedrasousa.engine.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.Hashtable;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;


public abstract class AbstractShaderProg {
	
	private int		mProgramHandle;	
	private int		mVertexProgramHandle;
	private int		mFragmentProgramHandle;
	
	private String	mVertexShdResName	= new String("N/A");
	private String	mFragmentShdResName	= new String("N/A");
	
	private int		mVertexResourceId;
	private int		mFragmentResourceId;
	
	private Hashtable<String, Integer> mUniformLocations	= new Hashtable<String, Integer>();
	private Hashtable<String, Integer> mAtribLocations		= new Hashtable<String, Integer>();
	
	public AbstractShaderProg(Context context, int vertexResourceId, int fragmentResourceId) {
		 createProgFromFile(context, vertexResourceId, fragmentResourceId);
	}
	
	public void reload(Context context) {
		createProgFromFile(context, mVertexResourceId, mFragmentResourceId);
	}
	
	private String readTextFile(final Context context, final int resourceId) {
		
		final InputStream inputStream				= context.getResources().openRawResource(resourceId);
		final InputStreamReader inputStreamReader	= new InputStreamReader(inputStream);
		final BufferedReader bufferedReader			= new BufferedReader(inputStreamReader);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try {
			while ( (nextLine = bufferedReader.readLine()) != null ) {
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return body.toString();
	}

	public void createProgFromFile(Context context, int vertexResourceId, int fragmentResourceId) {
		
		String source;
		
		mVertexResourceId	= vertexResourceId;
		mFragmentResourceId	= fragmentResourceId;
		
		// Store the resource name where the shader programs where loaded from
		mVertexShdResName	= context.getResources().getResourceEntryName(vertexResourceId);
		mFragmentShdResName	= context.getResources().getResourceEntryName(fragmentResourceId);
		
		source = readTextFile(context, vertexResourceId);
		compileVertexProgram(source);
		
		source = readTextFile(context, fragmentResourceId);
		compileFragmentProgram(source);
		
		link();
	}

	public int compile(String souce, int type) {
		
		String log = null;
		int shaderHandle = GLES20.glCreateShader(type);
		
		if (shaderHandle != 0) {
		    GLES20.glShaderSource(shaderHandle, souce);
		    GLES20.glCompileShader(shaderHandle);
		    final int[] compileStatus = new int[1];
		    GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		    
		    if (compileStatus[0] == 0) {
		    	// The compilation failed
		    	log = GLES20.glGetShaderInfoLog(shaderHandle);
		        GLES20.glDeleteShader(shaderHandle);
		        shaderHandle = 0;
		    }
		}
		 
		if (shaderHandle == 0) {
			String shaderType = new String((type == GLES20.GL_VERTEX_SHADER) ? "vertex" : "fragment");
			throw new RuntimeException("Error compiling " + shaderType + " shader program. Vertex program resource: " + mVertexShdResName + " Vertex program resource: " + mFragmentShdResName + " Log: \n"  + log);
		}
		
		return shaderHandle;
	}
	
	public void compileVertexProgram(String souce) {
		mVertexProgramHandle = compile(souce, GLES20.GL_VERTEX_SHADER);
	}
	
	public void compileFragmentProgram(String souce) {
		mFragmentProgramHandle = compile(souce, GLES20.GL_FRAGMENT_SHADER);
	}
	
	public void link() {
		
		String log = null;
		
		mProgramHandle = GLES20.glCreateProgram();
		
		GLES20.glAttachShader(mProgramHandle, mVertexProgramHandle);
		GLES20.glAttachShader(mProgramHandle, mFragmentProgramHandle);
		GLES20.glLinkProgram(mProgramHandle);

		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

		if (linkStatus[0] == 0) {
			// The linking failed
			log = GLES20.glGetProgramInfoLog(mProgramHandle);
			GLES20.glDeleteProgram(mProgramHandle);
			mProgramHandle = 0;
		}

		if (mProgramHandle == 0) {
			throw new RuntimeException("Error linking shader program. Vertex program resource: " + mVertexShdResName + " Vertex program resource: " + mFragmentShdResName + " Log:"  + log);
		}
	}
	
	public int getProgramHandle() {
		return mProgramHandle;
	}
	
	public int getUniformLocation(String name) {
		
		Integer location;
		
		location = mUniformLocations.get(name);
		
		if (location == null) {
			// Location not been cashed yet
			location = GLES20.glGetUniformLocation(mProgramHandle, name);
			mUniformLocations.put(name, location);
		}
		
		if (location == -1) {
			Log.e("pedroedrasousa", "Error getting shader uniform location. Vertex program resource: " + mVertexShdResName + " Vertex program resource: " + mFragmentShdResName + " Name: " + name);
		}
		
		return location;
	}
	
	public int getAttribLocation(String name) {
		Integer location;
		
		location = mAtribLocations.get(name);
		
		if (location == null) {
			// Location not been cashed yet
			location = GLES20.glGetAttribLocation(mProgramHandle, name);
			mAtribLocations.put(name, location);
		}
		
		if (location == -1) {
			Log.e("pedroedrasousa", "Error getting shader attributte location. Vertex program resource: " + mVertexShdResName + " Vertex program resource: " + mFragmentShdResName + " Name: " + name);
		}
		
		return location;
	}
	
	public void uniform1i(String name, int x) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform1i(location, x);
	}
	
	public void uniform3f(String name, float x, float y, float z) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform3f(location, x, y, z);
	}
	
	public void uniformMatrix4fv(String name, int count, boolean transpose, float[] value, int offset) {
		Integer location = getUniformLocation(name);
		GLES20.glUniformMatrix4fv(location, count, transpose, value, offset);
	}

	public void vertexAttribPointer(String name, int size, int type, boolean normalized, int stride, int offset) {
		Integer location = getAttribLocation(name);
		GLES20.glVertexAttribPointer(location, size, type, normalized, stride, offset);
	}
	
	public void vertexAttribPointer(String name, int size, int type, boolean normalized, int stride, Buffer ptr) {
		Integer location = getAttribLocation(name);
		GLES20.glVertexAttribPointer(location, size, type, normalized, stride, ptr);
	}
	
	public void enableVertexAttribArray(String name) {
		GLES20.glEnableVertexAttribArray(getAttribLocation(name));
	}
	
	public void disableVertexAttribArray(String name) {
		GLES20.glDisableVertexAttribArray(getAttribLocation(name));
	}
	
	public void useProgram() {
		GLES20.glUseProgram(mProgramHandle);
	}
	
	public abstract void enable();
	public abstract void disable();
}
