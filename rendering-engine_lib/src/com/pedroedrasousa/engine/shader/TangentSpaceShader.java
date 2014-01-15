package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

public interface TangentSpaceShader extends SimpleShader {
	void setNormalAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setTangentAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, int offset);

	void setNormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
	void setTangentAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
	void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
	void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
