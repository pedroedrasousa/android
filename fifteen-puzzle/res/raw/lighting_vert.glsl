uniform mat4	uMVPMatrix;
uniform mat4	uMVMatrix;
uniform mat4	uVMatrix;

uniform vec3	aLightPos;	// Light position in object space

// Vertex atributes
attribute vec3	aVertPos;
attribute vec3	aNormal;
attribute vec3	aTangent;
attribute vec3	aBinormal;
attribute vec2	aTexCoords;

varying vec2	vTexCoords;
varying vec3	vLightVec;
varying vec3	vHalfVec;

void main()
{
	vec3 vecTBN;

	// Calculate tangent space vectors in eye space
	vec3 n = normalize(vec3(uMVMatrix * vec4(aNormal, 0.0)));
	vec3 t = normalize(vec3(uMVMatrix * vec4(aTangent, 0.0)));
	vec3 b = normalize(vec3(uMVMatrix * vec4(aBinormal, 0.0)));

	// Calculate vertex position in eye space
	vec3 vertexPosition = vec3(uMVMatrix * vec4(aVertPos, 1.0));
	
	// Calculate light position in eye space (could be done in CPU)
	vec3 lightPosition = vec3(uVMatrix * vec4(aLightPos, 1.0));

	// Calculate light vector in eye space and then transform it into tangent space
	vec3 lightDir = lightPosition - vertexPosition;
	vecTBN.x = dot(lightDir, t);
	vecTBN.y = dot(lightDir, b);
	vecTBN.z = dot(lightDir, n);
	vLightVec = vecTBN;
	
	// Calculate view vector in eye space and then transform it into tangent space
	// viewVector = eyePosition - vertexPosition
	// eyePosition in eye space is (0, 0, 0), so we get
	vec3 viewVector = -vertexPosition;
	vecTBN.x = dot(viewVector, t);
	vecTBN.y = dot(viewVector, b);
	vecTBN.z = dot(viewVector, n);
	
	// Calculate halfway vector between light and view vectors
	// Vectors are already in tangent space
	vHalfVec = vLightVec + vecTBN;
	
	vTexCoords = aTexCoords;
	gl_Position = uMVPMatrix * vec4(aVertPos, 1.0);
}
