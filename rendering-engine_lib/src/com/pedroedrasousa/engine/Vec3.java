package com.pedroedrasousa.engine;

import android.opengl.Matrix;


public class Vec3 {
	
	public final static Vec3 ZERO	= new Vec3(0.0f, 0.0f, 0.0f);
	public final static Vec3 ONE	= new Vec3(1.0f, 1.0f, 1.0f);
		
	public float x, y, z;
	
	public Vec3() {
		x = y = z = 0.0f;
	}
	
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(float[] v) {
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
	}
	
	public Vec3(Vec3 a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
	}
	
	public void assign(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	} 
	
	public void assign(float[] v) {
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
	}
	
	public void assign(Vec3 a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
	}
	
	public void setZero() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
	}
	
	public void add(Vec3 a) {
		this.x += a.x;
		this.y += a.y;
		this.z += a.z;
	}
	
	public void add(float a) {
		this.x += a;
		this.y += a;
		this.z += a;
	}
	
	public void sub(Vec3 a) {
		this.x -= a.x;
		this.y -= a.y;
		this.z -= a.z;
	}
	
	public void sub(float a) {
		this.x -= a;
		this.y -= a;
		this.z -= a;
	}
	
	public void scale(float factor) {
		this.x *= factor;
		this.y *= factor;
		this.z *= factor;
	}
	
	public void transform(float[] m) {
		float[] rhsVec		= new float[] {x, y, z, 1.0f};
		float[] resultVec	= new float[4];
		Matrix.multiplyMV(resultVec, 0, m, 0, rhsVec, 0);
		assign(resultVec);
	}
	
	public void rotate(float[] m) {
		float[] rhsVec		= new float[] {x, y, z, 0.0f};
		float[] resultVec	= new float[4];
		Matrix.multiplyMV(resultVec, 0, m, 0, rhsVec, 0);
		assign(resultVec);
	}
	
	public void normalize() {
		float length = (float)Math.sqrt(x * x + y * y + z * z);
		
		if (length != 0) {
			float invLength = 1.0f / length;
			x *= invLength;
			y *= invLength;
			z *= invLength;
		}
	}
	
	/**
	 * Calculate unit normal vector to the triangle given by points a, b and c.
	 * a->b->c goes right-handed.
	 * @param a
	 * @param b
	 * @param c
	 */
	public void normal(Vec3 a, Vec3 b, Vec3 c) {
		x = (b.y - a.y) * (c.z - a.z) - (b.z - a.z) * (c.y - a.y);
		y = (b.z - a.z) * (c.x - a.x) - (b.x - a.x) * (c.z - a.z);
		z = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
	
		this.normalize();  
	}
	
	public float length() {
		return (float)Math.sqrt(x * x + y * y + z * z);
	}
	
	/**
	 * Linear interpolation.
	 * @param v
	 * @param alpha
	 */
	public void lerp(Vec3 v, float alpha) {
		x = x + (v.x - x) * alpha;
		y = y + (v.y - y) * alpha;
		z = z + (v.z - z) * alpha;
	}
	
	@Override
	public String toString() {
	    return x + ", " + y + ", " + z;
	}
	
	@Override
	public boolean equals(Object obj) {
	   if (obj == null) {
	      return false;
	   }

	   if (this.getClass() != obj.getClass()) {
	      return false;
	   }

	   if (this.x != ((Vec3)obj).x || this.y != ((Vec3)obj).y || this.z != ((Vec3)obj).z) {
	      return false;
	   }

	   return true;
	}
	
	public boolean equals(Object obj, float delta) {
	   if (obj == null) {
	      return false;
	   }

	   if (this.getClass() != obj.getClass()) {
	      return false;
	   }

	   if (	!(x < ((Vec3)obj).x + delta && x > ((Vec3)obj).x - delta) ||
			!(y < ((Vec3)obj).y + delta && y > ((Vec3)obj).y - delta) ||
			!(z < ((Vec3)obj).z + delta && z > ((Vec3)obj).z - delta) ) {
	      return false;
	   }

	   return true;
	}
	
	public static Vec3 add(Vec3 a, Vec3 b) {
		return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
	
	public static Vec3 sub(Vec3 a, Vec3 b) {
		return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z);
	}
	
	public static Vec3 mul(Vec3 a, float f) {
		return new Vec3(a.x * f, a.y * f, a.z * f);
	}
	
	/**
	 * Dot product.
	 * @param a
	 * @param b
	 * @return
	 */
	public static float dot(Vec3 a, Vec3 b) {
		return(a.x * b.x + a.y * b.y + a.z * b.z);
	}
	
	/**
	 * Cross product.
	 * @param a
	 * @param b
	 * @return
	 */
	public static Vec3 cross(Vec3 a, Vec3 b) {
		Vec3 res = new Vec3();
		
		res.x = a.y * b.z - a.z * b.y;
		res.y = a.z * b.x - a.x * b.z;
		res.z = a.x * b.y - a.y * b.x;

		return res;
	}
	
	/**
	 * Linearly interpolate between two vectors.
	 * @param v1
	 * @param v2
	 * @param alpha
	 * @return
	 */
	public static Vec3 lerp(Vec3 v1, Vec3 v2, float alpha) {
		Vec3 res = new Vec3();
		
		res.x = v1.x + (v2.x - v1.x) * alpha;
		res.y = v1.y + (v2.y - v1.y) * alpha;
		res.z = v1.z + (v2.z - v1.z) * alpha;

		return res;
	}
}
