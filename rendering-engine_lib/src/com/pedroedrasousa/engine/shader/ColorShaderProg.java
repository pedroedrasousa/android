package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import android.content.Context;

import com.pedroedrasousa.engine.Vec3;


public class ColorShaderProg extends AbstractShaderProg implements ColorShader {
	
	public static final String VCOORDS_ATTRIB_NAME	= "aVertPos";
	public static final String COLOR_UNIFORM_NAME	= "aColor";
	
	public ColorShaderProg(Context context, int vertexResourceId, int fragmentResourceId) {
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
	
	@Override
	public void setColor(Vec3 color) {
		uniform3f("aColor", color.x, color.y, color.z);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(VCOORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(VCOORDS_ATTRIB_NAME);
	}
}
