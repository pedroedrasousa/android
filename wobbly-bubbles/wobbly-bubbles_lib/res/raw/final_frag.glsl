precision mediump float;

uniform sampler2D	uTexBg;
uniform sampler2D	uTexture;

varying vec2		vTexCoord;
varying vec2		vTexBgCoords;
varying float		vBgBrightness;

void main()
{
	vec4 bgColor	= texture2D(uTexBg, vTexBgCoords) * vBgBrightness;
	vec4 metaColor	= texture2D(uTexture, vTexCoord);

	gl_FragColor = mix(bgColor, metaColor, metaColor.w);
}
