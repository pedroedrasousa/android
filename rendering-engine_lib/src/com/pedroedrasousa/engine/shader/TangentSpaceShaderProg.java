package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import android.content.Context;


public class TangentSpaceShaderProg extends AbstractShaderProg implements TangentSpaceShader {

	public static final String VCOORDS_ATTRIB_NAME		= "aVertPos";
	public static final String VNORMAL_ATTRIB_NAME		= "aNormal";
	public static final String VTANGENT_ATTRIB_NAME		= "aTangent";
	public static final String VBINORMAL_ATTRIB_NAME	= "aBinormal";
	public static final String VTEXCOORDS_ATTRIB_NAME	= "aTexCoords";
	
	public TangentSpaceShaderProg(Context context, int vertexResourceId, int fragmentResourceId) {
		super(context, vertexResourceId, fragmentResourceId);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(VCOORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setNormalAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(VNORMAL_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setTangentAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(VTANGENT_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(VBINORMAL_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(VTEXCOORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(VCOORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setNormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(VNORMAL_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setTangentAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(VTANGENT_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(VBINORMAL_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(VTEXCOORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(VCOORDS_ATTRIB_NAME);
	    enableVertexAttribArray(VNORMAL_ATTRIB_NAME);
	    enableVertexAttribArray(VTANGENT_ATTRIB_NAME);
	    enableVertexAttribArray(VBINORMAL_ATTRIB_NAME);
	    enableVertexAttribArray(VTEXCOORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(VCOORDS_ATTRIB_NAME);
	    disableVertexAttribArray(VNORMAL_ATTRIB_NAME);
	    disableVertexAttribArray(VTANGENT_ATTRIB_NAME);	    
	    disableVertexAttribArray(VBINORMAL_ATTRIB_NAME);
	    disableVertexAttribArray(VTEXCOORDS_ATTRIB_NAME);
	}
}
