uniform mat4	uMVPMatrix;
uniform mat4	uMVMatrix;
uniform float	uBgBrightness;

attribute vec4	aVertPos;
attribute vec2	aTexCoord;
attribute vec2	aTexBgCoords;

varying vec2	vTexBgCoords;
varying vec2	vTexCoord;
varying float	vBgBrightness;

void main()
{                       
	vTexCoord		= aTexCoord;
	vTexBgCoords	= aTexBgCoords;
	vBgBrightness	= uBgBrightness;
	
	gl_Position = uMVMatrix * aVertPos;
	gl_Position = uMVPMatrix * gl_Position;
}
