package com.pedroedrasousa.engine.object3d;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.pedroedrasousa.engine.Vec2;
import com.pedroedrasousa.engine.Vec3;

import android.content.Context;

public class ObjLoader {
	
	private class Face {
		public int[]	mVertexIdx;
		public int[]	mTexCoordsIdx;
		private String	mSmoothGroup;
		
		public int[] getVertexIdx() {
			return mVertexIdx;
		}
		public void setmVertexIdx(int[] vertexIdx) {
			this.mVertexIdx = vertexIdx;
		}
		public int[] getTexCoordsIdx() {
			return mTexCoordsIdx;
		}
		public void setmTexCoordsIdx(int[] texCoordsIdx) {
			mTexCoordsIdx = texCoordsIdx;
		}
		public String getmSmoothGroup() {
			return mSmoothGroup;
		}
		public void setSmoothGroup(String mSmoothGroup) {
			this.mSmoothGroup = mSmoothGroup;
		}
	}
	
	@SuppressWarnings("unused")
	public class Material {
		
		private String	mAmbientMap;			// map_Ka
		private String	mDifuseMap;				// map_Kd
		private String	mSpecularMap;			// map_Ks
		private String	mSpecularHighlightMap;	// map_Ns
		private String	mAlphaMap;				// map_d
		private String	mBumpMap;				// map_bump
		
		private Vec3	mDiffuse;				// Kd
		private Vec3	mAmbient;				// Ka
		private Vec3	mSpecular;				// Ks
		private Vec3	mEmissive;				// Ke
		private float	mSpecularCoefficient;	// Ns - Ranges between 0 and 1000
		
		public void load(InputStream in) {
			
		    LineNumberReader input = new LineNumberReader(new InputStreamReader(in));           
		    String line = null;
		    try {
		        for (line = input.readLine(); line != null; line = input.readLine()) {
		            if (line.length() > 0) {
		                if (line.startsWith("Ka ")) {		
		                    float[] f = new float[3];
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    f[0] = Float.parseFloat( tok.nextToken() );
		                    f[1] = Float.parseFloat( tok.nextToken() );
		                    f[2] = Float.parseFloat( tok.nextToken() );
		                    mAmbient = new Vec3(f);
		                }
		                else if (line.startsWith("Kd ")) {
		                    float[] f = new float[3];
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    f[0] = Float.parseFloat( tok.nextToken() );
		                    f[1] = Float.parseFloat( tok.nextToken() );
		                    f[2] = Float.parseFloat( tok.nextToken() );
		                    mDiffuse = new Vec3(f);
		                }
		                else if (line.startsWith("Ks ")) {
		                    float[] f = new float[3];
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    f[0] = Float.parseFloat( tok.nextToken() );
		                    f[1] = Float.parseFloat( tok.nextToken() );
		                    f[2] = Float.parseFloat( tok.nextToken() );
		                    mSpecular = new Vec3(f);
		                } else if (line.startsWith("Ke ")) {
		                    float[] f = new float[3];
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    f[0] = Float.parseFloat( tok.nextToken() );
		                    f[1] = Float.parseFloat( tok.nextToken() );
		                    f[2] = Float.parseFloat( tok.nextToken() );
		                    mEmissive = new Vec3(f);
		                } else if (line.startsWith("Ke ")) {
		                	StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mSpecularCoefficient = Float.parseFloat( tok.nextToken() );
		                } else if (line.startsWith("map_Ka ")) {
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mAmbientMap = tok.nextToken();
		                } else if (line.startsWith("map_Kd ")) {
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mDifuseMap = tok.nextToken();
		                } else if (line.startsWith("map_Ks ")) {
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mSpecularMap = tok.nextToken();
		                } else if (line.startsWith("map_Ns ")) {
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mSpecularHighlightMap = tok.nextToken();
		                } else if (line.startsWith("map_d ")) {
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mAlphaMap = tok.nextToken();
		                } else if (line.startsWith("map_bump ")) {
		                    StringTokenizer tok = new StringTokenizer(line);
		                    tok.nextToken();
		                    mBumpMap = tok.nextToken();
		                }
		            }
		        }
		    }
		    catch (Exception e) {
		    	e.printStackTrace();
		    }
		}
	}
	
	private Vector<Vec3>		mVertices;
	private Vector<Vec2>		mTexCoords;
	private Vector<Face>		mFaces;
	private Vector<Material>	mMaterials;
	
	private Vector<Vertex>		mFinalVertices;
	private Vector<Short>		mIndices;
	
	public Vector<Vertex> getVertices() {
		return mFinalVertices;
	}
	
	public Vector<Short> getIndices() {
		return mIndices;
	}
	
	public ObjLoader() {
		mVertices	= new Vector<Vec3>();
		mTexCoords	= new Vector<Vec2>();
		mMaterials	= new Vector<Material>();
		mFaces		= new Vector<Face>();
	}
	
	public void load(Context context, InputStream in) {
		
	    String currentSmoothGroup = new String("off");
	    Vector<Triangle> triangles = new Vector<Triangle>();
	
	    LineNumberReader input = new LineNumberReader(new InputStreamReader(in));           
	    String line = null;
	    try {
	        for (line = input.readLine(); 
	        line != null; 
	        line = input.readLine()) {
	            if (line.length() > 0) {
	                if (line.startsWith("s ")) {			// Smooth group
	                    StringTokenizer tok = new StringTokenizer(line);
	                    tok.nextToken();
	                    currentSmoothGroup = tok.nextToken();
	                }
	                if (line.startsWith("v ")) {			// Vertex coordinates
	                	float x, y, z;
	                    StringTokenizer tok = new StringTokenizer(line);
	                    tok.nextToken();
	                    x = Float.parseFloat( tok.nextToken() );
	                    y = Float.parseFloat( tok.nextToken() );
	                    z = Float.parseFloat( tok.nextToken() );
	                    mVertices.add(new Vec3(x, y, z));
	                }
	                else if (line.startsWith("vt ")) {		// Vertex texture coordinates
	                	float s, t;
	                    StringTokenizer tok = new StringTokenizer(line);
	                    tok.nextToken();
	                    s = Float.parseFloat( tok.nextToken() );
	                    t = Float.parseFloat( tok.nextToken() );
	                    mTexCoords.add(new Vec2(s, 1.0f - t));
	                }
	                else if (line.startsWith("f ")) {		// Face
	                    int[] v1, v2, v3;
	                    StringTokenizer tok = new StringTokenizer(line);
	                    tok.nextToken();
	                    v1 = parseFaceToken(tok.nextToken());
	                    v2 = parseFaceToken(tok.nextToken());
	                    v3 = parseFaceToken(tok.nextToken());
	                    
	                    Face f = new Face();
	                    
	                    f.setmVertexIdx(new int[] {v1[0], v2[0], v3[0]});
	                    f.setmTexCoordsIdx(new int[] {v1[1], v2[1], v3[1]});
	                    f.setSmoothGroup(currentSmoothGroup);
	                    mFaces.add(f);
	                }
	                else if (line.startsWith("mtllib ")) {
	                	String filename;
	                	Material material = new Material();
	                    StringTokenizer tok = new StringTokenizer(line);
	                    tok.nextToken();
	                    filename = tok.nextToken();
	                    
	                    try {
	                    	InputStream io = context.getAssets().open(filename);
	                    	material.load(io);
	            		} catch (IOException e) {
	            			e.printStackTrace();
	            		}
	                	
	                	mMaterials.add(material);
	                }
	            }
	        }
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    // Compute each triangle tangent space normal, tangent and binormal vectors.
	    Iterator<Face> itr = mFaces.iterator();
	    while(itr.hasNext()) {
	    	Face f = itr.next();
	    	
	    	// Get the this triangle vertices.
	    	Vec3 v1 = mVertices.get( f.getVertexIdx()[0] );
			Vec3 v2 = mVertices.get( f.getVertexIdx()[1]);
			Vec3 v3 = mVertices.get( f.getVertexIdx()[2]);
			
			// Get this triangle texture coordinates.
	    	Vec2 uv1 = mTexCoords.get( f.getTexCoordsIdx()[0]);
			Vec2 uv2 = mTexCoords.get( f.getTexCoordsIdx()[1]);
			Vec2 uv3 = mTexCoords.get( f.getTexCoordsIdx()[2]);

	    	Triangle t = new Triangle();
	    	t.setVertexCoord(v1, v2, v3);
	    	t.setTexCoord(uv1, uv2, uv3);
	    	t.setSmoothGroup(f.getmSmoothGroup());
	    	t.calcTangentSpace();
	    	triangles.add(t);
	    }

	    //mIndices = new short[triangles.size() * 3];
	    mIndices		 = new Vector<Short>();
	    mFinalVertices	= new Vector<Vertex>();
	    //int idx = 0;
	    
	    // Compute resulting vertex data according to smooth groups.
	    Iterator<Triangle> TriangleItr = triangles.iterator();
	    while(TriangleItr.hasNext()) {
	    	Triangle t = TriangleItr.next();
	    	Vec3 n1 = new Vec3();
	    	Vec3 n2 = new Vec3();
	    	Vec3 n3 = new Vec3();
	    	
	    	Vec3 t1 = new Vec3();
	    	Vec3 t2 = new Vec3();
	    	Vec3 t3 = new Vec3();
	    	
	    	Vec3 b1 = new Vec3();
	    	Vec3 b2 = new Vec3();
	    	Vec3 b3 = new Vec3();
	    	
	    	Vec3 v1 = t.getVertexCoord1();
	    	Vec3 v2 = t.getVertexCoord2();
	    	Vec3 v3 = t.getVertexCoord3();
	    	
	    	Vec2 uv1 = t.getTexCoord1();
	    	Vec2 uv2 = t.getTexCoord2();
	    	Vec2 uv3 = t.getTexCoord3();
	    	
	    	if (t.getSmoothGroup().equals("off")) {
	    		// No smoothing, vertices normals will be the same of the triangle.
	    		
				n1 = t.getNormal();
				t1 = t.getTangent();
				b1 = t.getBinormal();
				
				n2 = t.getNormal();
				t2 = t.getTangent();
				b2 = t.getBinormal();	
				
				n3 = t.getNormal();
				t3 = t.getTangent();
				b3 = t.getBinormal();					
	    	} else {
	    		// Loop through every triangle to find adjacent vertices
			    Iterator<Triangle> itr2 = triangles.iterator();
			    while(itr2.hasNext()) {
			    	Triangle tri2 = itr2.next();
			    	
			    	Vec3 v1_ = tri2.getVertexCoord1();
					Vec3 v2_ = tri2.getVertexCoord2();
					Vec3 v3_ = tri2.getVertexCoord3();
					
					// Check if triangles belong to the same smooth group
		    		if (!t.getSmoothGroup().equals(tri2.getSmoothGroup()))
		    			continue;
					
					if (v1.equals(v1_) || v1.equals(v2_) || v1.equals(v3_)) 
					{
						n1.add(tri2.getNormal());
						t1.add(tri2.getTangent());
						b1.add(tri2.getBinormal());
					}
					
					if (v2.equals(v1_) || v2.equals(v2_) || v2.equals(v3_)) 
					{
						n2.add(tri2.getNormal());
						t2.add(tri2.getTangent());
						b2.add(tri2.getBinormal());
					}
	
					if (v3.equals(v1_) || v3.equals(v2_) || v3.equals(v3_)) 
					{
						n3.add(tri2.getNormal());
						t3.add(tri2.getTangent());
						b3.add(tri2.getBinormal());
					}
			    }
	    	}
		    
	    	// Normalize everything.
	    	n1.normalize();
	    	n2.normalize();
	    	n3.normalize();
	    	t1.normalize();
	    	t2.normalize();
	    	t3.normalize();
	    	b1.normalize();
	    	b2.normalize();
	    	b3.normalize();
	    	
	    	// The resulting vertices of this triangle.
	    	Vertex vertex1 = new Vertex(v1, uv1, n1, t1, b1);
	    	Vertex vertex2 = new Vertex(v2, uv2, n2, t2, b2);
	    	Vertex vertex3 = new Vertex(v3, uv3, n3, t3, b3);
	    	
	    	int index;
	    	
	    	//
	    	// Check, for the three resulting vertices, if they were already added to the final vertices vector.
	    	// If not, add them and finally update the indices vector.
	    	
	    	index = mFinalVertices.indexOf(vertex1);
	    	if (index == -1) {
	    		mFinalVertices.add(vertex1);
	    		mIndices.add((short)(mFinalVertices.size() - 1));
	    	} else {
	    		mIndices.add((short)index);
	    	}

	    	index = mFinalVertices.indexOf(vertex2);
	    	if (index == -1) {
	    		mFinalVertices.add(vertex2);
	    		mIndices.add((short)(mFinalVertices.size() - 1));
	    	} else {
	    		mIndices.add((short)index);
	    	}
	    	
	    	index = mFinalVertices.indexOf(vertex3);
	    	if (index == -1) {
	    		mFinalVertices.add(vertex3);
	    		mIndices.add((short)(mFinalVertices.size() - 1));
	    	} else {
	    		mIndices.add((short)index);
	    	}
	    }
	}
	
	// Assume there's only one material.
	public String getDifuseMapFilename() {
		
		if (mMaterials.size() > 0)
			return mMaterials.get(0).mDifuseMap;
		
		return null;
	}
	
	// Assume there's only one material.
	public String getNormalMapFilename() {
		
		if (mMaterials.size() > 0)
			return mMaterials.get(0).mBumpMap;
		
		return null;
	}
	
    protected static int parseInt(String val) {
		if (val.length() == 0) {
		        return -1;
		}
		return Integer.parseInt(val);
    }
	        
	private static int[] parseFaceToken(String face) {
		
		int i = face.indexOf("/");
		if (i == -1)
			return new int[] {Integer.parseInt(face) - 1, -1, -1};	// Only vertex index
        else {
        	int j = face.indexOf("/", i + 1);
        	if (j == -1) {
        		return new int[] {	Integer.parseInt(face.substring(0, i)) - 1,
        							Integer.parseInt(face.substring(i + 1)) - 1,
        							-1
        						};
        	}
        	else {
        		return new int[] {	parseInt(face.substring(0, i)) - 1,
        							parseInt(face.substring(i+1, j)) - 1,
        							parseInt(face.substring(j+1)) - 1
        						};
        	}
        }
	}
}
