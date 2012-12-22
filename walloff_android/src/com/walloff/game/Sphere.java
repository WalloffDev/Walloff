package com.walloff.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;


/**
 * Sphere class is used to render a playable sphere object as well as the starting lights.
 *
 */
public class Sphere {

	/* buffers that are used to set up the vertexes and colors of the circles */
	private FloatBuffer m_vertex_data;
    
    private float m_Radius = 1.0f;
    private int m_Stacks = 20; 
    private int m_Slices = 20;
    private float m_Scale = m_Radius;
    
    /* colors to draw each of the spheres */
    private float color_red = 0;
    private float color_green = 0;
    private float color_blue = 0;
    
    
	/* constructor for our circle objects */
	public Sphere()
	{
		float[] vertex_data;                                                                                
	   	
        int vIndex=0;				//vertex index                     

        {   		    				
	    	//vertices   		
           vertex_data = new float[ 3*((m_Slices*2+2) * m_Stacks)];	            	
		
           int phiIdx, thetaIdx;
   		
           //latitude   		
           for(phiIdx=0; phiIdx < m_Stacks; phiIdx++)	               		
           {
           	//starts at -90 degrees (-1.57 radians) goes up to +90 degrees (or +1.57 radians)   			
           	//the first circle    	             	             	             	             	             	            	       
           	float phi0 = (float)Math.PI * ((float)(phiIdx+0) * (1.0f/(float)(m_Stacks)) - 0.5f);	
   			
           	//the next, or second one.	             	             	             	             	             	             	      
           	float phi1 = (float)Math.PI * ((float)(phiIdx+1) * (1.0f/(float)(m_Stacks)) - 0.5f);	

           	float cosPhi0 = FloatMath.cos(phi0);	                     
           	float sinPhi0 = FloatMath.sin(phi0);
           	float cosPhi1 = FloatMath.cos(phi1);
           	float sinPhi1 = FloatMath.sin(phi1);
   			
           	float cosTheta, sinTheta;
   			
           	//longitude   			
           	for(thetaIdx=0; thetaIdx < m_Slices; thetaIdx++)               
           	{
	    	         //increment along the longitude circle each "slice"
	    				                                                                   
	    	         float theta = (float) (-2.0f*(float)Math.PI * ((float)thetaIdx) * (1.0/(float)(m_Slices-1)));			
	    	         cosTheta = FloatMath.cos(theta);		
	    	         sinTheta = FloatMath.sin(theta);
	    				
	    	        //we're generating a vertical pair of points, such 
	    	        //as the first point of stack 0 and the first point of stack 1
	    	        //above it. This is how TRIANGLE_STRIPS work, 
	    	        //taking a set of 4 vertices and essentially drawing two triangles
	    	        //at a time. The first is v0-v1-v2 and the next is v2-v1-v3. Etc.
	    				
	    	        //get x-y-z for the first vertex of stack	    				
	    	         vertex_data[vIndex+0] = m_Scale*cosPhi0*cosTheta; 	
	         	     vertex_data[vIndex+1] = m_Scale*(sinPhi0*m_Radius); 
	        	     vertex_data[vIndex+2] = m_Scale*(cosPhi0*sinTheta); 
	    				
	    	         vertex_data[vIndex+3] = m_Scale*cosPhi1*cosTheta;
	    	         vertex_data[vIndex+4] = m_Scale*(sinPhi1*m_Radius); 
	    	         vertex_data[vIndex+5] = m_Scale*(cosPhi1*sinTheta); 
	    	         
	          	     vIndex+=2*3; 				           
	            }
		    			
		    	// create a degenerate triangle to connect stacks and maintain winding order			     	           	             	             	             	             	             
		    	vertex_data[vIndex+0] = vertex_data[vIndex+3] = vertex_data[vIndex-3];
		    	vertex_data[vIndex+1] = vertex_data[vIndex+4] = vertex_data[vIndex-2]; 
		    	vertex_data[vIndex+2] = vertex_data[vIndex+5] = vertex_data[vIndex-1];
		     }
			
		 }
        
        m_vertex_data = makeFloatBuffer(vertex_data);
	}

    public Sphere(int id)
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
		
		float[] vertex_data;                                                                                
   	
        int vIndex=0;				//vertex index                     

        {   		    				
	    	//vertices   		
           vertex_data = new float[ 3*((m_Slices*2+2) * m_Stacks)];	            	
		
           int phiIdx, thetaIdx;
   		
           //latitude   		
           for(phiIdx=0; phiIdx < m_Stacks; phiIdx++)	               		
           {
           	//starts at -90 degrees (-1.57 radians) goes up to +90 degrees (or +1.57 radians)   			
           	//the first circle    	             	             	             	             	             	            	       
           	float phi0 = (float)Math.PI * ((float)(phiIdx+0) * (1.0f/(float)(m_Stacks)) - 0.5f);	
   			
           	//the next, or second one.	             	             	             	             	             	             	      
           	float phi1 = (float)Math.PI * ((float)(phiIdx+1) * (1.0f/(float)(m_Stacks)) - 0.5f);	

           	float cosPhi0 = FloatMath.cos(phi0);	                     
           	float sinPhi0 = FloatMath.sin(phi0);
           	float cosPhi1 = FloatMath.cos(phi1);
           	float sinPhi1 = FloatMath.sin(phi1);
   			
           	float cosTheta, sinTheta;
   			
           	//longitude   			
           	for(thetaIdx=0; thetaIdx < m_Slices; thetaIdx++)               
           	{
	    	         //increment along the longitude circle each "slice"
	    				                                                                   
	    	         float theta = (float) (-2.0f*(float)Math.PI * ((float)thetaIdx) * (1.0/(float)(m_Slices-1)));			
	    	         cosTheta = FloatMath.cos(theta);		
	    	         sinTheta = FloatMath.sin(theta);
	    				
	    	        //we're generating a vertical pair of points, such 
	    	        //as the first point of stack 0 and the first point of stack 1
	    	        //above it. This is how TRIANGLE_STRIPS work, 
	    	        //taking a set of 4 vertices and essentially drawing two triangles
	    	        //at a time. The first is v0-v1-v2 and the next is v2-v1-v3. Etc.
	    				
	    	        //get x-y-z for the first vertex of stack	    				
	    	         vertex_data[vIndex+0] = m_Scale*cosPhi0*cosTheta; 	
	         	     vertex_data[vIndex+1] = m_Scale*(sinPhi0*m_Radius); 
	        	     vertex_data[vIndex+2] = m_Scale*(cosPhi0*sinTheta); 
	    				
	    	         vertex_data[vIndex+3] = m_Scale*cosPhi1*cosTheta;
	    	         vertex_data[vIndex+4] = m_Scale*(sinPhi1*m_Radius); 
	    	         vertex_data[vIndex+5] = m_Scale*(cosPhi1*sinTheta); 
	    	         
	          	     vIndex+=2*3; 				           
	            }
		    			
		    	// create a degenerate triangle to connect stacks and maintain winding order			     	           	             	             	             	             	             
		    	vertex_data[vIndex+0] = vertex_data[vIndex+3] = vertex_data[vIndex-3];
		    	vertex_data[vIndex+1] = vertex_data[vIndex+4] = vertex_data[vIndex-2]; 
		    	vertex_data[vIndex+2] = vertex_data[vIndex+5] = vertex_data[vIndex-1];
		     }
			
		 }
        
        m_vertex_data = makeFloatBuffer(vertex_data);
	}

    public void setColors(float red, float green, float blue)
    {
    	this.color_red = red;
    	this.color_green = green;
    	this.color_blue = blue;
    }
    
	/* function for drawing our circle */
	 public void draw(GL10 gl) 
    {
    	gl.glFrontFace(GL10.GL_CW);					
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_vertex_data);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	
    	gl.glColor4f(color_red, color_green, color_blue, 1);// set the color to draw
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, (m_Slices+1)*2*(m_Stacks-1)+2);
        gl.glColor4f(1, 1, 1, 1);// clear the color afterwards
        
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
	    
    protected static FloatBuffer makeFloatBuffer(float[] arr) 
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    public float getRadius() { return this.m_Radius; }
}
