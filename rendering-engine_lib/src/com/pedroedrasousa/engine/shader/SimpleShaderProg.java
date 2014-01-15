package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import android.content.Context;


public class SimpleShaderProg extends AbstractShaderProg implements SimpleShader {
	
	public static final String VCOORDS_ATTRIB_NAME = "aVertPos";
	
	public SimpleShaderProg(Context context, int vertexResourceId, int fragmentResourceId) {
		super(context, vertexResourceId, fragmentResourceId);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(VCOORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(VCOORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(VCOORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(VCOORDS_ATTRIB_NAME);
	}
}
