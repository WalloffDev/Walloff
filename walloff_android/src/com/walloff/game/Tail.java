package com.walloff.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * The tail class will be used to keep track of the player's tail.
 * 
 */
public class Tail {
	
	private FloatBuffer vertexBuffer; //vertex buffer
	private List<Float> vertices2 = new ArrayList<Float>();  //dynamic vertex array
	private float vertices[];
	
	/* colors to draw each of the spheres */
    private float color_red = 0;
    private float color_green = 0;
    private float color_blue = 0;
	
	public Tail(int id)
	{
		switch ( id )
		{
			case 0:
				color_red = 0.8f;
				break;
			case 1:
				color_blue = 0.8f;
				break;
			case 2:
				color_red = 0.8f;
				color_green = 0.8f;
				break;
			case 3:
				color_green = 0.8f;
				break;
		}
	}
	
	public void draw( GL10 gl )
	{
		//set color based on sphere's ID				
		vertices = new float[vertices2.size()];
		
		for(int i = 0; i < vertices2.size(); i++)
			vertices[i] = vertices2.get(i);
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		//simply an arbitrary number for our line width.
    	gl.glLineWidth(50); 
		
		// Enabled our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		// set the color to draw
		gl.glColor4f(color_red, color_green, color_blue, 1);
		
		//pointer to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		//draw the square object
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0,  vertices.length/3);
		
		// clear the color
		gl.glColor4f(1, 1, 1, 1);
		
		// Disable the vertices and color buffer.
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}

	public void addNewPoint(float X, float Z){		
		vertices2.add(X);
		vertices2.add(0f);
		vertices2.add(Z);
	}
	
	/* insert a point into our tail array */
	public void insertPointAt(float X, float Z, int i){
		vertices2.add(i, X);
		vertices2.add(i+1, 0f);
		vertices2.add(i+2, Z);
	}
	
	public int getTailLength(){ return vertices2.size();}
	public float getTailEntry(int i){ return vertices2.get(i);}

	public void removeTail() {
		vertices2.clear();
	}
	
}
