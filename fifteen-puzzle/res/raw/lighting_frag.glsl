precision mediump float;

// Hardcoded light properties
#define AMBIENT_COLOR	vec3(0.8, 0.8, 0.8)
#define DIFFUSE_COLOR	vec3(1.0, 1.0, 1.0)
#define SPECULAR_COLOR	vec3(0.5, 0.5, 0.5)
#define SHININESS		100.0

uniform sampler2D	uBaseMap;
uniform sampler2D	uNormalMap;

varying vec2	vTexCoords;
varying vec3	vLightVec;
varying vec3	vHalfVec;

varying vec3	vBinormal;

void main()
{
	vec4 finalColor;
	
	// Normalize vectors passed from vertex shader
	vec3 halfVec = normalize(vHalfVec);
	vec3 lightVec = normalize(vLightVec);

	// Lookup color from texture
	vec4 baseMap = texture2D(uBaseMap, vTexCoords);

	// Lookup normal from texture, move it from [0,1] to [-1,1] range and normalize it.
	vec3 normal = normalize(texture2D(uNormalMap, vTexCoords).rgb * 2.0 - 1.0);

  	// Ambient lighting
	finalColor = vec4(AMBIENT_COLOR, 1.0) * baseMap;

	// Compute difuse lighting factor
	float difuseFactor = max(dot(lightVec, normal), 0.0);
	
	if (difuseFactor > 0.0)
	{
		// Compute specular lighting factor
		float specularFactor = pow(max(dot(halfVec, normal), 0.0), SHININESS);
		
		// Add difuse and specular component
		finalColor = max(vec4(AMBIENT_COLOR, 1.0), vec4(DIFFUSE_COLOR, 1.0) * difuseFactor) * baseMap;
		finalColor += vec4(SPECULAR_COLOR, 1.0) * specularFactor;
	}
	
	gl_FragColor = finalColor;
}
