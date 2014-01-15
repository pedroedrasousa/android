package com.pedroedrasousa.engine.object3d;

import com.pedroedrasousa.engine.Vec2;
import com.pedroedrasousa.engine.Vec3;

public class Triangle {
	
	// Vertex coordinates (x, y, z)
	private Vec3 mVertex1;
	private Vec3 mVertex2;
	private Vec3 mVertex3;
	
	// Texture coordinates (s, t)
	private Vec2 mTexCoords1;
	private Vec2 mTexCoords2;
	private Vec2 mTexCoords3;
	
	// Triangle tangent space
	private Vec3 mNormal;
	private Vec3 mTangent;
	private Vec3 mBinormal;
	
	private String mSmoothGroup;

	// Calculate triangle normal, tangent and binormal.
	public void calcTangentSpace() {

		Vec3 v21 = Vec3.sub(mVertex2, mVertex1);		// v2 -> v1 vector
		Vec3 v31 = Vec3.sub(mVertex3, mVertex1);		// v3 -> v1 vector

		Vec2 st21 = Vec2.sub(mTexCoords2, mTexCoords1);	// st2 -> st1 vector
		Vec2 st31 = Vec2.sub(mTexCoords3, mTexCoords1);	// st3 -> st1 vector
		
		// Normal can be found by calculating the cross product between v21 and v31.
		mNormal = Vec3.cross(v21, v31);
		
		float r = st21.x * st31.y - st31.x * st21.y;
		r = (r == 0) ? 1 : 1 / r;
		
		// sdir = (v21 * st31.y - v31, st21.y) * r
		Vec3 sdir = Vec3.sub(Vec3.mul(v21, st31.y), Vec3.mul(v31, st21.y));
		sdir.scale(r);
		
		// tdir = (v21 * st31.x - v31, st21.x) * r
		Vec3 tdir = Vec3.sub( Vec3.mul(v21, st31.x), Vec3.mul(v31, st21.x));
		tdir.scale(r);
		
		// Gram-Schmidt orthogonalize
		mTangent = Vec3.sub(sdir, Vec3.mul(mNormal, Vec3.dot(mNormal, sdir)));
		mTangent.normalize();
		
		// Binormal can be found by calculating the cross product between the normal and tangent.
		mBinormal = Vec3.cross(mNormal, mTangent);

		// Binormal must have the same orientation than tdir, check whether it must be inverted or not.
		if (Vec3.dot(mBinormal, tdir) < 0.0f) {
			// Binormal has the wrong direction. Invert it.
			mBinormal.scale(-1.0f);
		}
	}
	
	// Getters and setters
	
	public Vec3 getVertexCoord1() { return mVertex1; }
	public Vec3 getVertexCoord2() { return mVertex2; }
	public Vec3 getVertexCoord3() { return mVertex3; }
	
	public void setVertexCoord1(Vec3 vertexCoord) { mVertex1 = vertexCoord; }
	public void setVertexCoord2(Vec3 vertexCoord) { mVertex2 = vertexCoord; }
	public void setVertexCoord3(Vec3 vertexCoord) { mVertex3 = vertexCoord; }	
	public void setVertexCoord(Vec3 v1, Vec3 v2, Vec3 v3) { mVertex1 = v1; mVertex2 = v2; mVertex3 = v3; }
	
	public Vec2 getTexCoord1() { return mTexCoords1; }
	public Vec2 getTexCoord2() { return mTexCoords2; }
	public Vec2 getTexCoord3() { return mTexCoords3; }
	
	public void setTexCoord1(Vec2 texCoord) { mTexCoords1 = texCoord; }
	public void setTexCoord2(Vec2 texCoord) { mTexCoords2 = texCoord; }
	public void setTexCoord3(Vec2 texCoord) { mTexCoords3 = texCoord; }
	public void setTexCoord(Vec2 uv1, Vec2 uv2, Vec2 uv3) { mTexCoords1 = uv1; mTexCoords2 = uv2; mTexCoords3 = uv3; }
	
	public Vec3 getNormal()		{ return mNormal; }
	public Vec3 getTangent()	{ return mTangent; }
	public Vec3 getBinormal()	{ return mBinormal; }
	
	public void setNormal(Vec3 normal)		{ mNormal	= normal; }
	public void setTangent(Vec3 tangent)	{ mTangent	= tangent;}
	public void setBinormal(Vec3 binormal)	{ mBinormal	= binormal; }
	
	public String getSmoothGroup() { return mSmoothGroup; }
	public void setSmoothGroup(String smoothGroup) { mSmoothGroup = smoothGroup; }
}
