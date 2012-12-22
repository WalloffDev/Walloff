package com.walloff.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Current this class is used to draw the warning line four our map expansion.
 * This will change in the future as it becames part of a more generic map class.
 */
public class Line {

	private float m_scale_amount = WallOffEngine.map_size - WallOffEngine.map_shrink_length;
	
	/* vertex buffer object */
 	private FloatBuffer vertexBuffer;
 	private float vertices[] = 
 	{
 			-1.0f, 0.0f, -1.0f,        // V2 - top left
 			-1.0f, 0.0f,  1.0f,        // V1 - bottom left
 	         1.0f, 0.0f,  1.0f,        // V3 - bottom right
 	         1.0f, 0.0f, -1.0f         // V4 - top right
 	};
 	
 	public Line() {
 		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
	}
 	
 	/* draw our Line object */
    public void draw(GL10 gl)
    {
    	//simply an arbitrary number for our line width.
    	gl.glLineWidth(5); 
    	gl.glScalef(m_scale_amount, 1f, m_scale_amount);
    	
    	// Enabled our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		// set the color to draw
		gl.glColor4f(1f, 0f, .6f, 1);
		
		//pointer to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		//draw the square object
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0,  vertices.length/3);
		
		// clear the color
		gl.glColor4f(1, 1, 1, 1);
		
		// Disable the vertices and color buffer.
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    public void setScaleAmmount(float s) { m_scale_amount = s; }
    
    public float getScaleAmmount( ) { return m_scale_amount; }
}
