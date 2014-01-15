package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

public interface SimpleShader {
	void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
