
uniform mat4	uMVPMatrix;
uniform float	uRefractOffsetFactor;
uniform float	uMetaTransparency;
uniform float	uTrailFactor;

attribute vec4	aVertexCoords;
attribute vec2	aTexBgCoords;
attribute vec2	aTexMetaCoords;

varying vec2	vTexBgCoords;
varying vec2	vTexMetaCoords;
varying float	vRefractOffsetFactor;
varying float	vMetaTransparency;
varying float	vTrailFactor;

void main()
{
	vTexBgCoords			= aTexBgCoords; 
	vTexMetaCoords			= aTexMetaCoords;
	vRefractOffsetFactor	= uRefractOffsetFactor;
	vMetaTransparency		= uMetaTransparency;
	vTrailFactor			= uTrailFactor;
	
	gl_Position = uMVPMatrix * aVertexCoords;
	gl_Position.y = -gl_Position.y;
}
