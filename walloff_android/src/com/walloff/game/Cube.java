package com.walloff.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.FloatMath;

/**
 * This class can be used to create cube objects. These objects can be used to randomly
 * place obstacles that the player has to avoid. Note that the move function will have to 
 * create a "random" looking path to the users. This way our game host does not have to 
 * tell all the other players where the moving obstacles can go. I don't believe that the
 * Android wireless will be able to handle much more additional information than player locaation
 * updates.
 * 
 */
public class Cube {
	
	/* location coordinates */
	private float x = 0;
	private float y = 0;
	private float z = 0;
	private float x_init = 0;
	private float y_init = 0;
	private float z_init = 0;
	private float m_scale_x = 1;
	private float m_scale_y = 1;
	private float m_scale_z = 1;
	private float m_move_x = 0;
	private float m_move_z = 0;	
	private float m_mid_to_side = 1.0f;
	private float m_place_theta;
	private float m_move_theta;
	private float m_move_speed = WallOffEngine.player_speed/2; //set it to move at 1/2 the player speed
	int ID = 0;
	
	//texture related items
 	private int[] textures = new int[1];
	private FloatBuffer textureBuffer;
	private float texture[] = {  //Mapping coordinates for our texture
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f, 

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
	};

	/* the vertices for our cube */
	private float vertices[] = {
            //Vertices according to faces
            -1.0f, -1.0f, 1.0f, //Vertex 0
            1.0f, -1.0f, 1.0f,  //v1
            -1.0f, 1.0f, 1.0f,  //v2
            1.0f, 1.0f, 1.0f,   //v3

            1.0f, -1.0f, 1.0f,  //...
            1.0f, -1.0f, -1.0f,         
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,            
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,         
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,

            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,         
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,

            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,           
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
                                };

	// The order we like to connect them.
	private byte indices[] = {
            0,1,3, 0,3,2,           //Face front
            4,5,7, 4,7,6,           //Face right
            8,9,11, 8,11,10,        //... 
            12,13,15, 12,15,14,     
            16,17,19, 16,19,18,     
            20,21,23, 20,23,22,     
     };
	
	// Our vertex buffer.
	private FloatBuffer vertexBuffer;
	
	// Our index buffer.
	private ByteBuffer indexBuffer;
	
	/* Basic constructors for our cube */
	public Cube( )
	{
		this.x = 0;
		this.z = 0;
		
		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		//add the indices to the index buffer.
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
		
		//byte buffer for our texture
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}
	
	public Cube(int id, Player[] players)
	{	
		this.ID = id;
		
		m_place_theta = (float)Math.toRadians( WallOffEngine.obsticles_init_pattern ) + (2*WallOffEngine.PI*id)/WallOffEngine.obsticles_number;
		if (m_place_theta > 2*WallOffEngine.PI)
			m_place_theta -= 2*WallOffEngine.PI;
		
		this.x_init = ( ( WallOffEngine.map_size*FloatMath.sin(m_place_theta) + WallOffEngine.obsticles_init_pattern)*FloatMath.cos(m_place_theta) ) % (WallOffEngine.map_size - this.m_mid_to_side);
		this.z_init = ( ( WallOffEngine.map_size*FloatMath.cos(m_place_theta) + WallOffEngine.obsticles_init_pattern)*FloatMath.sin(m_place_theta) ) % (WallOffEngine.map_size - this.m_mid_to_side);
		
		/* create a safe zone around our initial player location a 5 by 5 box*/	
		for (Player player : players) { // the box is within the 5 x 5 safe zone somewhere
			while ( true )
			{
				if ( player.getX() + player.getCharacter().getRadius() + 15 <= this.x - this.m_mid_to_side && // right side
				     player.getX() - player.getCharacter().getRadius() - 15 >= this.x + this.m_mid_to_side && // left side
				     player.getZ() + player.getCharacter().getRadius() + 15 <= this.z - this.m_mid_to_side && // bottom
				     player.getZ() - player.getCharacter().getRadius() - 15 >= this.z + this.m_mid_to_side ) //top side
				{
					this.x_init =  ( this.x_init * 2 ) % (WallOffEngine.map_size - this.m_mid_to_side);
					this.z_init =  ( this.z_init * 2 ) % (WallOffEngine.map_size - this.m_mid_to_side);
				}
				else 
					break;
			}
				
		}
		
		this.x = this.x_init;
		this.z = this.z_init;
		
		m_move_theta = (float)Math.toRadians( WallOffEngine.obsticles_move_pattern )
				        + (2*WallOffEngine.PI*id)/WallOffEngine.obsticles_number;
		m_move_z = - FloatMath.sin( m_move_theta ) * m_move_speed;
		m_move_x = - FloatMath.cos( m_move_theta ) * m_move_speed;
		
		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		//add the indices to the index buffer.
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
		
		//byte buffer for our texture
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}
	
	/* function used to draw our cube */
	public void draw(GL10 gl)
	{
		gl.glPushMatrix();
			gl.glEnable(GL10.GL_TEXTURE_2D); //Enable Texture Mapping 
			
			//Bind our only previously generated texture in this case
		    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
	
		    //Point to our buffers
		    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	
		    //Set the face rotation
		    gl.glFrontFace(GL10.GL_CCW);
	
		    //Enable the vertex and texture state
		    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
	
		    //Draw the vertices as triangles, based on the Index Buffer information
		    gl.glTranslatef(this.x, this.y, this.z);
		    gl.glScalef(m_scale_x, m_scale_y, m_scale_z);
		    gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
	
		    //Disable the client state before leaving
		    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		    
		    gl.glDisable(GL10.GL_TEXTURE_2D); //disable the GL texture 2d
	    gl.glPopMatrix();
	}
	
	/* function to randomly move our cubes */
	public void randomMove(GL10 gl)
	{ 
		//positive x wall
		if ( x - m_move_x > WallOffEngine.map_size - m_mid_to_side ) 
		{ 
			x = WallOffEngine.map_size - m_mid_to_side;
			m_move_x = - m_move_x;
		}
		//negative x wall
		else if ( x + m_move_x < -WallOffEngine.map_size + m_mid_to_side )
		{
			x = -WallOffEngine.map_size + m_mid_to_side;
			m_move_x = - m_move_x;
		}
		//positive z wall
		else if ( z - m_move_z > WallOffEngine.map_size - m_mid_to_side )
		{
			z = WallOffEngine.map_size - m_mid_to_side;
			m_move_z = - m_move_z;
		}
		//negative z wall
		else if ( z + m_move_z < -WallOffEngine.map_size + m_mid_to_side )
		{
			z = -WallOffEngine.map_size + m_mid_to_side;
			m_move_z = - m_move_z;
		}
		
		x += m_move_x;
		z += m_move_z;
	}

	/* load a texture on the square we wish to draw */
    public void loadGLTexture(GL10 gl, Context context, int texture_id){
		// loading texture
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texture_id);
		// generate one texture pointer
		gl.glGenTextures(1, textures, 0);
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		// Clean up
		bitmap.recycle();
	}

    /* mutator methods */
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
    public void setScaleX(float x) { this.m_scale_x = x; }
    public void setScaleY(float y) { this.m_scale_y = y; }
    public void setScaleZ(float z) { this.m_scale_z = z; }
    public void resetMovement()
    {
    	m_move_theta = (float)Math.toRadians( WallOffEngine.obsticles_move_pattern )
		        + (2*WallOffEngine.PI*this.ID)/WallOffEngine.obsticles_number;
    	m_move_z = - FloatMath.sin( m_move_theta ) * m_move_speed;
    	m_move_x = - FloatMath.cos( m_move_theta ) * m_move_speed;
    }
    
    /* accessor methods */
    public float getX() { return this.x; }
    public float getY() { return this.y; }
    public float getZ() { return this.z; }
    public float getXInit() { return this.x_init; }
    public float getYInit() { return this.y_init; }
    public float getZInit() { return this.z_init; }
    public float getScaleX() { return this.m_scale_x; }
    public float getScaleY() { return this.m_scale_y; }
    public float getScaleZ() { return this.m_scale_z; }
    public float getMoveX() { return this.m_move_x; }
    public float getMoveZ() { return this.m_move_z; }
    public float getMidToSide() { return this.m_mid_to_side; }

}
