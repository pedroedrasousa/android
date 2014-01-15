uniform mat4	uMVPMatrix;
uniform vec3	aColor;

attribute vec3	aVertPos;


varying vec3 vColor;

void main()
{
	gl_Position = uMVPMatrix * vec4(aVertPos, 1.0);
	
	vColor = aColor;
}
