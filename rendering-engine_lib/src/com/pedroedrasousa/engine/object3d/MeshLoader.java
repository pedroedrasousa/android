package com.pedroedrasousa.engine.object3d;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.pedroedrasousa.engine.Vec3;
import com.pedroedrasousa.engine.object3d.VertexData.Boundaries;

import android.content.Context;


public class MeshLoader {
	
	private Vector<Vertex>	mVertices;
	private Vector<Short>	mIndices;
	
	private VertexData mVertexData;
	
	public MeshLoader() {
		mVertexData	= new VertexData();
		mVertices	= new Vector<Vertex>();
		mIndices	= new Vector<Short>();
	}
		
	public void LoadFromObj(Context context, String assetName) {
		
		ObjLoader objLoader = new ObjLoader();
		
    	InputStream inputStream;
		try {
			inputStream = context.getAssets().open(assetName);
			objLoader.load(context, inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mVertices	= objLoader.getVertices();
		mIndices	= objLoader.getIndices();
		
		mVertexData.setBoundaries( getBoundaries() );
	}
	
	public void AddVertices(MeshLoader meshLoader) {

		short offset = (short)(mVertices.size());
		
		Vector<Short> newIndices = new Vector<Short>();
		
		for (Short s : meshLoader.mIndices)
			newIndices.add((short)(s.shortValue() + offset));
		
		mVertices.addAll(meshLoader.mVertices);
		mIndices.addAll(newIndices);
	}
	
	public void transform(float[] m) {
	    for (Vertex v: mVertices) {
	    	Vec3 vertexPos = new Vec3(v.getPos());
	    	vertexPos.transform(m);
	    	v.setPos(vertexPos);
	    }
	}
	
	public VertexData getVertexData() {
				
	    // Pack everything into one final buffer.
	    float[] packedData = new float[mVertices.size() * 14];
	    
		int idx = 0;

		for(Vertex v: mVertices) {
	    	
	    	// Vertex position
	    	packedData[idx*14+0]   = v.getPos().x;
	    	packedData[idx*14+1]   = v.getPos().y;
	    	packedData[idx*14+2]   = v.getPos().z;
	    	// Normal
	    	packedData[idx*14+3]   = v.getNormal().x;
	    	packedData[idx*14+4]   = v.getNormal().y;
	    	packedData[idx*14+5]   = v.getNormal().z;
	    	// Tangent
	    	packedData[idx*14+6]   = v.getTangent().x;
	    	packedData[idx*14+7]   = v.getTangent().y;
	    	packedData[idx*14+8]   = v.getTangent().z;
	    	// Binormal
	    	packedData[idx*14+9]   = v.getBinormal().x;
	    	packedData[idx*14+10]  = v.getBinormal().y;
	    	packedData[idx*14+11]  = v.getBinormal().z;
	    	// Texture coordinates
	    	packedData[idx*14+12]  = v.getTexCoords().x;
	    	packedData[idx*14+13]  = v.getTexCoords().y;
	    	
	    	idx++;
	    }

		idx = 0;
		short indices[] = new short[mIndices.size()];
		for(short i: mIndices)
			indices[idx++] = i;

		mVertexData.setVertexData(packedData, indices);
		return mVertexData;
	}
	
	private Boundaries getBoundaries() {
	    Vec3 min = new Vec3();
	    Vec3 max = new Vec3();
	    
	    for (Vertex vertex : mVertices) {
	    	Vec3 v = vertex.getPos();
	    	
	    	if (v.x < min.x) {
	    		min.x = v.x;
	    	} else if (v.x > max.x) {
	    		max.x = v.x;
	    	}
	    	
	    	if (v.y < min.y) {
	    		min.y = v.y;
	    	} else if (v.y > max.y) {
	    		max.y = v.y;
	    	}
	    	
	    	if (v.z < min.z) {
	    		min.z = v.z;
	    	} else if (v.z > max.z) {
	    		max.z = v.z;
	    	}
	    }
	    
	    Boundaries boundaries = new Boundaries();
	    boundaries.setMinVals(min);
	    boundaries.setMaxVals(max);
	    
	    return boundaries;
	}
	
	public VertexData getBoundingBoxMesh() {

		
		getBoundaries().getWidth();
		getBoundaries().getHeight();
		getBoundaries().getLength();
		
	    float [] packedData = getCubeCoords(getBoundaries().getMinVals(), getBoundaries().getMaxVals());
	    
	    short[] indices = new short[packedData.length / 3];
	    for (short i = 0; i < packedData.length / 3; i++) {
	    	indices[i] = i;
	    }
	    
	    VertexData m = new VertexData(packedData, indices);
	    m.setBoundaries( getBoundaries() );
	    
	    return m;
	}
	
	/**
	 * NOTE: Boundaries aren't calculated.
	 * @param vertexData
	 * @return
	 */
	public static VertexData getVertexDataFromArray(float[] vertexData) {
	    
		// Build the index array.
	    short[] indices = new short[vertexData.length / 3];
	    for (short i = 0; i < vertexData.length / 3; i++) {
	    	indices[i] = i;
	    }
	    
	    // Build the VertexData object.
	    VertexData vd = new VertexData(vertexData, indices);
	    
	    return vd;
	}
	
	
	private float [] getCubeCoords(Vec3 min, Vec3 max) {
		return new float [] {
			// Front face
			min.x, max.y, max.z,                                
			min.x, min.y, max.z,
			max.x, max.y, max.z, 
			min.x, min.y, max.z,                                 
			max.x, min.y, max.z,
			max.x, max.y, max.z,
			// Right face
			max.x, max.y, max.z,                                
			max.x, min.y, max.z,
			max.x, max.y, min.z,
			max.x, min.y, max.z,                                
			max.x, min.y, min.z,
			max.x, max.y, min.z,
			// Back face
			max.x, max.y, min.z,                                
			max.x, min.y, min.z,
			min.x, max.y, min.z,
			max.x, min.y, min.z,                                
			min.x, min.y, min.z,
			min.x, max.y, min.z,
			// Left face
			min.x, max.y, min.z,                                
			min.x, min.y, min.z,
			min.x, max.y, max.z, 
			min.x, min.y, min.z,                                
			min.x, min.y, max.z, 
			min.x, max.y, max.z, 
			// Top face
			min.x, max.y, min.z,                                
			min.x, max.y, max.z, 
			max.x, max.y, min.z, 
			min.x, max.y, max.z,                                 
			max.x, max.y, max.z, 
			max.x, max.y, min.z,
			// Bottom face
			max.x, min.y, min.z,                                
			max.x, min.y, max.z, 
			min.x, min.y, min.z,
			max.x, min.y, max.z,                                 
			min.x, min.y, max.z,
			min.x, min.y, min.z,
		};
	}

}
