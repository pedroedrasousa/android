precision mediump float;

uniform sampler2D	uTexBg;			// Background texture
uniform sampler2D	uTexMeta;		// Metaballs texture
uniform sampler2D	uTexPrevFrame;	// Previous rendered frame

varying vec2		vTexBgCoords;
varying vec2		vTexMetaCoords;
varying float		vRefractOffsetFactor;
varying float		vMetaTransparency;
varying float		vTrailFactor;

void main()
{
	vec4 metaColor;
	vec4 bgColor;
	vec4 bgColorOffset;
	vec4 bgColorFinal;
	vec4 prevFrameColor;
	vec2 bgTexCoords;

	// Get the metaballs fragment color
	metaColor = texture2D(uTexMeta, vTexMetaCoords);
	
	// Apply an offset based on texture w component to the background texture coordinates
	bgTexCoords.x = vTexBgCoords.x + metaColor.w * vRefractOffsetFactor;
	bgTexCoords.y = vTexBgCoords.y + metaColor.w * vRefractOffsetFactor;

	// Get the background fragment color with and without distortion offset and mix them
	bgColor			= texture2D(uTexBg, vTexBgCoords);
	bgColorOffset	= texture2D(uTexBg, bgTexCoords);
	bgColorFinal	= mix(bgColor, bgColorOffset, vRefractOffsetFactor);
	
	// Final metaball color mixed with background
	metaColor = mix(metaColor, bgColorFinal, vMetaTransparency * (1.0 - metaColor.w));
	
	prevFrameColor = texture2D(uTexPrevFrame, vTexMetaCoords);

	// Black background has a w component = 1.0
	// The resulting alpha variable will have a value of zero or one
	float alpha = 0.0;
	if (metaColor.w < 0.95)
		alpha = 1.0;

	// Render current frame metaballs on top of last frame
	gl_FragColor = mix(prevFrameColor, metaColor, alpha);
	
	// Final alpha value
	// Dim the alpha value of previous frame
	gl_FragColor.w = alpha + prevFrameColor.w * vTrailFactor;
}