precision mediump float;

// Light values
#define DIFFUSE_COLOR	vec3(0.5, 0.5, 0.5)
#define SPECULAR_COLOR	vec3(5.0, 5.0, 5.0)
#define SHININESS		100.0

varying vec3	vColor;
varying vec3	vLightPos;

varying vec3	vVertexModeView;
varying vec3	vNormalModeView;
varying vec3	vViewVec;
varying vec3	vLightVec;

varying float	vAmbientLight;
varying float	vSpecFactor;

void main()
{
	vec3 N = normalize(vNormalModeView);

	vec3 spec = SPECULAR_COLOR * vSpecFactor;
	vec3 diffuse = DIFFUSE_COLOR * max(dot(N, vLightVec), 0.0);
	
	float shininess = SHININESS * (1.0 - vAmbientLight);
	
	// Compute the specular term
	vec3 V = normalize(-vVertexModeView);	// Normalized vector toward the viewpoint
	vec3 H = normalize(vLightVec + V);		// Normalized vector that is halfway between V and L
	float specularLight;
	
	// Compute the reflection vector
	vec3 vReflect = normalize( 2.0 * dot( N, vLightVec) * N - vLightVec );
	
	specularLight = pow(max(dot(N, H), 0.0), SHININESS);
	vec3 specular = spec * vec3(specularLight);
	
	vec3 finalColor = specular + diffuse * vColor;
	
	finalColor = max(finalColor, vAmbientLight * vColor);
	
	gl_FragColor = vec4(finalColor, 1.1 - dot(normalize(vViewVec), vNormalModeView));
}