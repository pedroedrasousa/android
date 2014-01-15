uniform mat4	uMVPMatrix;

attribute vec3	aVertPos;

varying float vColor;

void main()
{
	gl_Position = uMVPMatrix * vec4(aVertPos, 1.0);
	vColor = gl_Position.z / gl_Position.w;
}
