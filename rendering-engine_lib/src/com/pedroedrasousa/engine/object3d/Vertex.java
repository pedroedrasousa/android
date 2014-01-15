package com.pedroedrasousa.engine.object3d;

import com.pedroedrasousa.engine.Vec2;
import com.pedroedrasousa.engine.Vec3;

public class Vertex {
	
	private Vec3 mPos;
	private Vec3 mNormal;
	private Vec3 mTangent;
	private Vec3 mBinormal;
	private Vec2 mTexCoords;
	
	public Vertex(Vec3 pos, Vec2 texCoords, Vec3 normal, Vec3 tangent, Vec3 binormal) {
		mPos = pos;
		mTexCoords = texCoords;
		mNormal = normal;	
		mTangent = tangent;	
		mBinormal = binormal;	
	}
	
	public Vec3 getPos() {
		return mPos;
	}
	
	public void setPos(Vec3 pos) {
		mPos = pos;
	}
	
	public Vec3 getNormal() {
		return mNormal;
	}
	
	public void setNormal(Vec3 normal) {
		mNormal = normal;
	}
	
	public Vec3 getTangent() {
		return mTangent;
	}

	public void setTangent(Vec3 tangent) {
		mTangent = tangent;
	}
	
	public Vec3 getBinormal() {
		return mBinormal;
	}

	public void setBinormal(Vec3 binormal) {
		mBinormal = binormal;
	}

	public Vec2 getTexCoords() {
		return mTexCoords;
	}

	public void setTexCoords(Vec2 texCoords) {
		mTexCoords = texCoords;
	}
	
	@Override
	public boolean equals(Object obj)
	{
	   if (obj == null)
	   {
	      return false;
	   }

	   if (this.getClass() != obj.getClass())
	   {
	      return false;
	   }

	   if (!this.mPos.equals(((Vertex)obj).mPos))
	   {
	      return false;
	   }
	   
	   if (!this.mNormal.equals(((Vertex)obj).mNormal))
	   {
	      return false;
	   }
	   
	   if (!this.mTangent.equals(((Vertex)obj).mTangent))
	   {
	      return false;
	   }

	   if (!this.mBinormal.equals(((Vertex)obj).mBinormal))
	   {
	      return false;
	   }
	   
	   if (!this.mTexCoords.equals(((Vertex)obj).mTexCoords))
	   {
	      return false;
	   }   
	   
	   return true;
	}
}
