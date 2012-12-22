package com.walloff.game;

import android.util.FloatMath;


/**
 * The camera class will be used to update the camera that follows our player.
 * 
 */
public class Camera {

	private float m_position_x;
	private float m_position_y;
	private float m_position_z;
	private float m_look_at_x;
	private float m_look_at_y;
	private float m_look_at_z;
	private float m_up_x;
	private float m_up_y;
	private float m_up_z;
	
	/* initial constructor for our camera */
	public Camera( float x, float y, float z, float Theta )
	{
		this.m_look_at_x = x;
		this.m_look_at_y = y;
		this.m_look_at_z = z;
		
		this.m_position_x = 5*FloatMath.cos(Theta) + x;
    	this.m_position_y = 2.0f + y;
    	this.m_position_z = 5*FloatMath.sin(Theta) + z;
    	
    	if ( WallOffEngine.map_name.equals(WallOffEngine.map_Original) )
    	{
    		this.m_up_x = 0;
    		this.m_up_y = 1;
    		this.m_up_z = 0;
    	}
	}
	
	/* update the camera location based on where the player is */
	public void updateCamera( float x, float y, float z, float Theta )
	{    	
		this.m_position_x = 5*FloatMath.cos(Theta) + x;
    	this.m_position_y = 2.0f + y;
    	this.m_position_z = 5*FloatMath.sin(Theta) + z;
    	
    	if ( this.m_position_x >= WallOffEngine.map_size )
    		this.m_position_x = WallOffEngine.map_size - .1f;
    	else if ( this.m_position_x <= -WallOffEngine.map_size )
    		this.m_position_x = -WallOffEngine.map_size + .1f;
    	else if ( this.m_position_z >= WallOffEngine.map_size )
    		this.m_position_z = WallOffEngine.map_size - .1f;
    	else if ( this.m_position_z <= -WallOffEngine.map_size )
    		this.m_position_z = -WallOffEngine.map_size + .1f;
	}
	
	public void playerDeadCamera( float player_x, float player_y, float player_z, Player m_player ) 
    {
    	float updatePosition = .2f;
    	float updateTheta = WallOffEngine.PI/128;
    	
		/* set camera X to 0 */
		if ( 0 < this.m_position_x - updatePosition) this.m_position_x -= updatePosition;
    	else if ( 0 > this.m_position_x + updatePosition  ) this.m_position_x += updatePosition;
    	else this.m_position_x = 0;
		/* set the focus from middled of sphere to center of map 0 */
		if ( 0 < player_x - updatePosition) m_player.setX(m_player.getX() - updatePosition);
    	else if ( 0 > player_x + updatePosition  ) m_player.setX(m_player.getX() + updatePosition);
    	else m_player.setX( 0 );
		
    	/* set z to 0 */
    	if ( 0 >= this.m_position_z + updatePosition ) this.m_position_z += updatePosition;
    	else if ( 0 <= this.m_position_z - updatePosition ) this.m_position_z -= updatePosition;
    	else this.m_position_z = 0;
    	/* set the focus from middled of sphere to center of map 0 */
		if ( 0 > m_player.getZ() + updatePosition) m_player.setZ(m_player.getZ() + updatePosition);
    	else if ( 0 < m_player.getZ() - updatePosition  ) m_player.setZ(m_player.getZ() - updatePosition);
    	else m_player.setZ( 0 );
		
    	/* set y to 2.3 above the map height */
		this.m_position_y += updatePosition * 2.3;
		if ( this.m_position_y > WallOffEngine.map_initial_size* 2.3) this.m_position_y = (float)(WallOffEngine.map_initial_size* 2.3);
    	
    	if ( m_player.getTheta() > WallOffEngine.PI/2 + updateTheta && m_player.getTheta() < (3 * WallOffEngine.PI)/2 )
    		m_player.setTheta(m_player.getTheta() - updateTheta);
    	else if (m_player.getTheta() < WallOffEngine.PI/2 - updateTheta && m_player.getTheta() >= (3 * WallOffEngine.PI)/2 )
    		m_player.setTheta(m_player.getTheta() + updateTheta);
    	else
    		m_player.setTheta(WallOffEngine.PI/2);
    	
    	this.m_up_x = -FloatMath.cos( m_player.getTheta() );
    	this.m_up_z =  FloatMath.sin( m_player.getTheta() );
    	
    }
	
	/* accessors for our camera class */
	public float getPositionX( ) { return m_position_x; }
	public float getPositionY( ) { return m_position_y; }
	public float getPositionZ( ) { return m_position_z; }
	public float getLookAtX( ) { return m_look_at_x; }
	public float getLookAtY( ) { return m_look_at_y; }
	public float getLookAtZ( ) { return m_look_at_z; }
	public float getUpX( ) { return m_up_x; }
	public float getUpY( ) { return m_up_y; }
	public float getUpZ( ) { return m_up_z; }
	
	/* mutators for our camera class */
	public void setPositionX( float x ) { this.m_position_x = x; }
	public void setPositionY( float y ) { this.m_position_y = y; }
	public void setPositionZ( float z ) { this.m_position_z = z; }
	public void setLookAtX( float x ) { this.m_position_x = x; }
	public void setLookAtY( float y ) { this.m_position_y = y; }
	public void setLookAtZ( float z ) { this.m_position_z = z; }
	public void setUpX( float x ) { this.m_position_x = x; }
	public void setUpY( float y ) { this.m_position_y = y; }
	public void setUpZ( float z ) { this.m_position_z = z; }
}
