uniform mat4	uMVPMatrix;
uniform mat4	uMVMatrix;
uniform vec3	uLightPos;
uniform vec3	uViewPosition;
uniform float	uAmbientLight;
uniform float	uSpecFactor;
uniform float	uPointSize;

attribute vec4	aVertPos;
attribute vec3	aColor;
attribute vec3	aNormal;

varying vec3	vColor;
varying vec3	vLightPos;
varying vec3	vVertexModeView;
varying vec3	vNormalModeView;

varying float	vAmbientLight;
varying float	vSpecFactor;

varying vec3	vViewVec;
varying vec3	vLightVec;

void main()
{	
	vNormalModeView = vec3(uMVMatrix * vec4(aNormal, 0.0));	// Normal in eye space
	vVertexModeView = vec3(uMVMatrix * aVertPos);			// Vertex eye space
	
	vLightPos		= uLightPos;
	vColor			= aColor;
	vViewVec		= uViewPosition - aVertPos.xyz;
	vAmbientLight	= uAmbientLight;
	
	vSpecFactor = uSpecFactor;
	
	vLightVec = normalize(vLightPos - vVertexModeView);
	
	gl_PointSize = uPointSize;
	gl_Position = uMVPMatrix * aVertPos;
}
