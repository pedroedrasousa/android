uniform mat4	uMVPMatrix;
uniform mat4	uMVMatrix;

attribute vec4	aVertPos;
attribute vec2	aTexCoord;
varying vec2	vTexCoord;

void main()
{                       
	vTexCoord = aTexCoord;
	gl_Position = uMVMatrix * aVertPos;
	gl_Position = uMVPMatrix * gl_Position;
}
