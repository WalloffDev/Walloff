package com.walloff.game;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;

/**
 * The player class will represent a person playing the game.
 * 
 */
public class Player {
	/* keep track if the player is alive */
	private Boolean alive;
	
	/* objects that our player can play as */
	private Sphere m_sphere;
	private Tail m_tail;
	private int m_ID;
	
	/* location of where the player is on the map */
	private float x_pos;
	private float y_pos;
	private float z_pos;
	private float x_pos_init;
	private float y_pos_init;
	private float z_pos_init;
	private float theta;
	private float theta_init;
	
	/* helper constants */
	private float edge_constant = 5f;
	
	public Player( int id )
	{
		this.alive = true;
		this.m_ID = id;
		
		//we should check what the person has save for their character( sphere, plain, ship, etc )
		//but for now we are only implementing a sphere
		this.m_sphere = new Sphere(id);
		this.m_tail = new Tail( id );		
		switch (id)
		{
			case 0:
				if ( WallOffEngine.map_name.equals(WallOffEngine.map_Original) )
				{
					this.x_pos_init = 0;
					this.y_pos_init = 0;
					this.z_pos_init = WallOffEngine.map_size - edge_constant;
					this.theta_init = WallOffEngine.PI / 2;
				}
				break;
			case 1:
				if ( WallOffEngine.map_name.equals(WallOffEngine.map_Original) )
				{
					this.x_pos_init = 0;
					this.y_pos_init = 0;
					this.z_pos_init = -WallOffEngine.map_size + edge_constant;
					this.theta_init = ( 3*WallOffEngine.PI )/2;
				}
				break;
			case 2:
				if ( WallOffEngine.map_name.equals(WallOffEngine.map_Original) )
				{
					this.x_pos_init = WallOffEngine.map_size - edge_constant;
					this.y_pos_init = 0;
					this.z_pos_init = 0;
					this.theta_init = 0;
				}
				break;
			case 3:
				if ( WallOffEngine.map_name.equals(WallOffEngine.map_Original) )
				{
					this.x_pos_init = -WallOffEngine.map_size + edge_constant;
					this.y_pos_init = 0;
					this.z_pos_init = 0;
					this.theta_init = WallOffEngine.PI;
				}
				break;
		}
		
		this.x_pos = this.x_pos_init;
		this.y_pos = this.y_pos_init;
		this.z_pos = this.z_pos_init;
		this.theta = this.theta_init;
	}

	/* update our player position and the camera position */
	public void updatePlayer( )
    {
    	this.theta = this.theta + WallOffEngine.accelerometer.getYTheta(); 
		this.x_pos = this.x_pos - FloatMath.cos( this.theta )*WallOffEngine.player_speed;
		this.z_pos = this.z_pos - FloatMath.sin( this.theta )*WallOffEngine.player_speed;
		
		if ( this.theta >= WallOffEngine.PI * 2  )
			this.theta = this.theta - WallOffEngine.PI * 2;
		else if ( this.theta < 0  )
			this.theta = this.theta + WallOffEngine.PI * 2;
		
		this.m_tail.addNewPoint(this.x_pos, this.z_pos);
    }
	
	/* draw the character and the tail */
	public void Draw(GL10 gl)
	{
		gl.glPushMatrix();
			gl.glTranslatef(x_pos, y_pos, z_pos);
			m_sphere.draw(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
			m_tail.draw(gl);
		gl.glPopMatrix();
	}
	
	/* setter functions */
	public void setX( float x ) { this.x_pos = x; }
	public void setY( float y ) { this.y_pos = y; }
	public void setZ( float z ) { this.z_pos = z; }
	public void setAlive( boolean alive ) { this.alive = alive; }
	public void setTheta( float t) 
	{ 
		this.theta = t;
		if ( this.theta >= 2*WallOffEngine.PI )
			this.theta -= 2*WallOffEngine.PI;
		else if ( this.theta < 0 )
			this.theta += 2*WallOffEngine.PI;
	}
	public void removeTail() {
		m_tail.removeTail();
	}
	
	/* getter functions */
	public float getX(){ return this.x_pos; }
	public float getY(){ return this.y_pos; }
	public float getZ(){ return this.z_pos; }
	public float getXInit(){ return this.x_pos_init; }
	public float getYInit(){ return this.y_pos_init; }
	public float getZInit(){ return this.z_pos_init; }
	public boolean isAlive(){ return this.alive; }
	public float getTheta() { return this.theta; }
	public float getThetaInit() { return this.theta_init; }
	public Sphere getCharacter () { return this.m_sphere; } //this again could be the plane, ship, etc
	public Tail getTail() { return this.m_tail; }
	public int getID() { return this.m_ID; }
	
}
