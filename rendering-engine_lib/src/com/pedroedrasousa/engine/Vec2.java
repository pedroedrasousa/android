package com.pedroedrasousa.engine;


public class Vec2 {
		
	public float x, y;
	
	public Vec2() {
		x = y = 0.0f;
	}
	
	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2(float[] v) {
		this.x = v[0];
		this.y = v[1];
	}
	
	public Vec2(Vec2 a) {
		this.x = a.x;
		this.y = a.y;
	}
	
	public void assign(float x, float y) {
		this.x = x;
		this.y = y;
	} 
	
	public void assign(float[] v) {
		this.x = v[0];
		this.y = v[1];
	}
	
	public void assign(Vec2 a) {
		this.x = a.x;
		this.y = a.y;
	}
	
	public void setZero() {
		this.x = 0.0f;
		this.y = 0.0f;
	}
	
	public void add(Vec2 a) {
		this.x += a.x;
		this.y += a.y;
	}
	
	public void add(float a) {
		this.x += a;
		this.y += a;
	}
	
	public void sub(Vec2 a) {
		this.x -= a.x;
		this.y -= a.y;
	}
	
	public void sub(float a) {
		this.x -= a;
		this.y -= a;
	}
	
	public void scale(float factor) {
		this.x *= factor;
		this.y *= factor;
	}
	
	@Override
	public String toString() {
	    return x + ", " + y;
	}
	
	@Override
	public boolean equals(Object obj) {
		
	   if (obj == null) {
	      return false;
	   }

	   if (this.getClass() != obj.getClass()) {
	      return false;
	   }

	   if (this.x != ((Vec2)obj).x || this.y != ((Vec2)obj).y) {
	      return false;
	   }

	   return true;
	}
	
	public static Vec2 add(Vec2 a, Vec2 b) {
		return new Vec2(a.x + b.x, a.y + b.y);
	}
	
	public static Vec2 sub(Vec2 a, Vec2 b) {
		return new Vec2(a.x - b.x, a.y - b.y);
	}
	
	public static Vec2 mul(Vec2 a, float f) {
		return new Vec2(a.x * f, a.y * f);
	}
}
