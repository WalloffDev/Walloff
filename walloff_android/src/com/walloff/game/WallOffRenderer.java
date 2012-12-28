package com.walloff.game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.walloff.android.Constants;
import com.walloff.android.MainMenuActivity;
import com.walloff.android.NetworkingManager;
import com.walloff.android.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.FloatMath;
import android.util.Log;


/**
 * Main class for rendering our actual game.
 *
 */
public class WallOffRenderer implements GLSurfaceView.Renderer 
{
	/* context for the renderer */
	private Context m_context;

	/* broadcast receiver for ourplayer updates */
	private BroadcastReceiver m_player_updates;
	
	/* variables for keeping track of the start of the game */
	private boolean m_countdown = true;
	private long m_countdown_time = 0;
	
	/* map options */
	private int m_shrink_count = WallOffEngine.map_shrink_ticks;
	private int m_shrink_map_count = 0;
	private Sphere[] start_lights = new Sphere[3];
	
	/* list of all our players */
	private Player[] players = null;
	private int playersAlive = 0;
	private Cube[] m_obsticles = null;
	private Player m_player;
	private Line line;
	
	/* walls used for shrinking the game */
	private Cube[] m_wall_shrink_cubes = null;
	private boolean m_wall_shrink_show = false;

	/* loop variables */
	private long loopStart = 0;
	private long loopEnd = 0;
	private long loopRunTime = 0;
	private int game_number = 1;
	
	/* camera locations */
	private Camera m_camera = null;
	
	/* network manager */
	NetworkingManager n_man = null;

    private Square m_square_ground, m_square_wall;
    
    public WallOffRenderer(Context context, NetworkingManager man, BroadcastReceiver rec) 
    {
    	int id = 0;
    	this.m_player_updates = rec;
    	this.m_context = context;
    	this.n_man = man;
    	this.m_square_ground = new Square();
    	this.m_square_wall = new Square();
    	this.line = new Line();
    	
    	SharedPreferences prefs = this.m_context.getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
		String uname = prefs.getString( Constants.L_USERNAME, "" );
    	
    	for (int i=0; i<n_man.getPlayers().length; i++)
    	{
    		if ( n_man.getPlayers()[i] == null )
    			break;
    		else
    		{
    			if ( n_man.getPlayers()[i].equals(uname) )
    			{
    				id = i;
    			}
    			Log.i("CREATING PLAYER", "CREATING PLAYER");
    			WallOffEngine.player_count++;
    		}
    	}
    	
    	players = new Player[WallOffEngine.player_count];
    	
    	//create a player object for the number of players
    	for( int i = 0; i < WallOffEngine.player_count; i++ ) { players[i] = new Player(i); }
    	
    	//if we have obstacles, create an array containing them
    	if ( WallOffEngine.obstacles )
    	{
    		this.m_obsticles = new Cube[WallOffEngine.obstacles_number];
    		for ( int i = 0; i<WallOffEngine.obstacles_number; i++ ) { m_obsticles[i] = new Cube(i, players); }
    	}
    	
    	//create our new wall object array
    	if ( WallOffEngine.map_shrinkable )
    	{
    		this.m_wall_shrink_cubes = new Cube[4];
    		for ( int i = 0; i < this.m_wall_shrink_cubes.length; i++) { m_wall_shrink_cubes[i] = new Cube(); }
    	}
    	
    	m_player = players[id];
    	
    	//initialize our camera
    	m_camera = new Camera( m_player.getX( ), m_player.getY( ), m_player.getZ( ), m_player.getTheta( ) );
    	
    	//initialize our start lights
    	for( int i = 0; i <  start_lights.length; i++ ) { start_lights[i] = new Sphere(); }
    	
    	playersAlive = WallOffEngine.player_count;
    	
    	/* Create player update receiver (this reads in data sent from other players in our game) */
		this.m_player_updates = new BroadcastReceiver( ) {
			@Override
			public void onReceive( Context arg0, Intent arg1 ) {
				int player_index = arg1.getIntExtra(WallOffEngine.tag_player, 1000);
				if( player_index > WallOffEngine.player_count)
					Log.i("MESSAGE ERROR REC FROM OTHER PLAYER", "REC MESSAGE FROM A PLAYER THAT should not exist");
				else
				{
					float x = arg1.getFloatExtra(WallOffEngine.tag_x_pos, 0);
					float z = arg1.getFloatExtra(WallOffEngine.tag_z_pos, 0);
					players[player_index].getTail().insertPointAt( x, z, 
													arg1.getIntExtra(WallOffEngine.tag_tail_index, players[player_index].getTail().getTailLength()));
					if( arg1.getIntExtra( WallOffEngine.tag_tail_index, players[player_index].getTail().getTailLength() ) 
										  == players[player_index].getTail().getTailLength() )
					{
						players[player_index].setX(x);
						players[player_index].setZ(z);
					}
				}
			}
		};
		/* will need another rec for when a player dies */
		this.m_context.registerReceiver( this.m_player_updates, new IntentFilter( WallOffEngine.players_send_position ) );
    }

    /* the main rendering function for our game */
    public void onDrawFrame(GL10 gl) 
    {
    	if( game_number <= 5)
    	{
    		if ( playersAlive > 1 )
    		{
		    	//thread the game to run at 60 fps
				loopStart = System.currentTimeMillis();
				try
				{
					if(loopRunTime < WallOffEngine.GAME_THREAD_FPS_SLEEP)
						Thread.sleep(WallOffEngine.GAME_THREAD_FPS_SLEEP - loopRunTime);
				}
				catch (InterruptedException e){ e.printStackTrace(); }
				
		    	// Clears the screen and depth buffer.
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
				// Replace the current matrix with the identity matrix
				gl.glLoadIdentity();
				
		    	if( m_player.isAlive() )
		    	{
		    		/* make sure that the countdown timer has ended */
		    		if ( !m_countdown )
		    		{
		    			/* update the character location */
		    			m_player.updatePlayer();
		    			n_man.sendToAll(m_player);
						/* update each of the cubes */
						for (Cube cube : m_obsticles) { cube.randomMove(gl); }
					}
					
					/* check collisions */
					checkCollisions();
					
					/* update the camera location */
					m_camera.updateCamera( m_player.getX(), m_player.getY(), m_player.getZ(), m_player.getTheta() );
					GLU.gluLookAt(gl, m_camera.getPositionX(), m_camera.getPositionY(), m_camera.getPositionZ(),
									  m_player.getX(), m_player.getY(), m_player.getZ(),
									  m_camera.getUpX(), m_camera.getUpY(), m_camera.getUpZ() );
					
					/* draw the background images */
					drawMap(gl);
					
					/* draw the background objects if any */
					for (Cube cube : m_obsticles) { cube.draw(gl); }
		    	}
		    	//the player is dead but the game is not over so draw the remaining players from a top down view
		    	else
		    	{			
					/* update each of the cubes */
					for (Cube cube : m_obsticles) { cube.randomMove(gl); }
					
					/* camera for the dead player */
					m_camera.playerDeadCamera( m_player.getX(), m_player.getY(), m_player.getZ(), m_player );
					GLU.gluLookAt(gl, m_camera.getPositionX(), m_camera.getPositionY(), m_camera.getPositionZ(),
							 		  m_player.getX(), m_player.getY(), m_player.getZ(),
							 		  m_camera.getUpX(), m_camera.getUpY(), m_camera.getUpZ() );
					
					/* draw the background images */
					drawMap(gl);
					
					/* draw the background objects if any */
					for (Cube cube : m_obsticles) { cube.draw(gl); }			
					
					gl.glLineWidth(3); //simply an arbitrary number for our line width.
		    	}
		    	
		    	//draw the new map boarders if any
		    	for (Cube cube : m_wall_shrink_cubes) { if ( m_wall_shrink_show ) { cube.draw(gl); } }
		    	
		    	/* draw the character */
		    	for( int i = 0; i < WallOffEngine.player_count; i++ ) { if ( players[i].isAlive() ) { players[i].Draw(gl); } }
		    	
		    	/* shrink the map */
		    	if ( !m_countdown ) { shrinkMap(gl); }
		
		    	/* draw the countdown timers if applicable */
		    	if ( m_countdown ) { countdown(gl); }
		    	
				/* update the end of game loop times */
				loopEnd = System.currentTimeMillis();
				loopRunTime = (loopEnd - loopStart);
    		}
    		else
    		{
    			playersAlive = WallOffEngine.player_count;
    			game_number++;
    			
    			//reset our players
    	    	for( int i = 0; i < WallOffEngine.player_count; i++ )
    	    	{ 
    	    		players[i].setX( players[i].getXInit() );
    	    		players[i].setY( players[i].getYInit() );
    	    		players[i].setZ( players[i].getZInit() );
    	    		players[i].setTheta( players[i].getThetaInit() );
    	    		players[i].getTail().removeTail();
    	    		players[i].setAlive(true);
    	    	}

    	    	m_camera.updateCamera( m_player.getX(), m_player.getY(), m_player.getZ(), m_player.getTheta() );
    	    	
    	    	//reset our obstacles if we have them
    	    	if ( WallOffEngine.obstacles )
    	    	{
    	    		for ( int i = 0; i<WallOffEngine.obstacles_number; i++ )
    	    		{
    	    			m_obsticles[i].setX( m_obsticles[i].getXInit() );
    	    			m_obsticles[i].setY( m_obsticles[i].getYInit() );
    	    			m_obsticles[i].setZ( m_obsticles[i].getZInit() );
    	    			m_obsticles[i].resetMovement();
    	    		}
    	    	}
    	    	
    	    	//reset our new wall object array
    	    	if ( WallOffEngine.map_shrinkable )
    	    	{
    	    		for ( int i = 0; i < this.m_wall_shrink_cubes.length; i++)
    	    		{
    	    			m_wall_shrink_cubes[i].setX(0);
    	    			m_wall_shrink_cubes[i].setY(0);
    	    			m_wall_shrink_cubes[i].setZ(0);
    	    			m_wall_shrink_cubes[i].setScaleX(0);
    	    			m_wall_shrink_cubes[i].setScaleZ(0);
    	    		}
    	    	}
    	    	
    	    	//reset the map size and other options associated
    	    	WallOffEngine.map_size = WallOffEngine.map_initial_size;
    	    	m_countdown_time = System.nanoTime() + 4*WallOffEngine.NANOSECOND;
    	    	m_shrink_map_count = 0;
    	    	m_shrink_count = WallOffEngine.map_shrink_ticks;
    	    	line.setScaleAmmount( WallOffEngine.map_size - WallOffEngine.map_shrink_length );
    	    	m_countdown = true;
    		}
    	}
    	else
    	{
    		//we should leave the game here and go back to the lobby
    	}
    }

    /* draw the countdown timer */
    private void countdown(GL10 gl) {
    	float correction = 1.5f;
    	if ( m_countdown_time - System.nanoTime() >= 3*WallOffEngine.NANOSECOND )
    	{
    		for ( int i = 0; i<start_lights.length; i ++)
    		{    				
    			gl.glPushMatrix();
    				if ( m_player.getTheta() == 0 || m_player.getTheta() == WallOffEngine.PI)
    				{
	    				gl.glTranslatef(m_player.getX() + correction*FloatMath.cos(m_player.getTheta()),
	    						        m_player.getY()+correction,
	    						        m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.cos(m_player.getTheta()) );
	    			}
    				else
    				{
    					gl.glTranslatef(m_player.getX() - i*FloatMath.sin(m_player.getTheta())*correction + correction*FloatMath.sin(m_player.getTheta()),
    									m_player.getY()+correction,
    									m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.sin(m_player.getTheta()) );
    				}
    				gl.glScalef( .3f, .3f, .3f );
    				start_lights[i].setColors(1, 1, 1);
    				start_lights[i].draw(gl);
    			gl.glPopMatrix();
    		}
    	}
    	else if ( m_countdown_time - System.nanoTime() >= 2*WallOffEngine.NANOSECOND )
    	{
    		for ( int i = 0; i<start_lights.length; i ++)
    		{    				
    			gl.glPushMatrix();
    				if ( m_player.getTheta() == 0 || m_player.getTheta() == WallOffEngine.PI)
    				{
    					gl.glTranslatef(m_player.getX() + correction*FloatMath.cos(m_player.getTheta()),
						        		m_player.getY()+correction,
						        		m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.cos(m_player.getTheta()) );
	    			}
    				else
    				{
    					gl.glTranslatef(m_player.getX() + i*FloatMath.sin(m_player.getTheta())*correction - correction*FloatMath.sin(m_player.getTheta()),
    									m_player.getY()+correction,
    									m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.sin(m_player.getTheta()) );
    				}
    				gl.glScalef( .3f, .3f, .3f );
    				if ( i == 0 )
    					start_lights[i].setColors(1, 0, 0);
    				else
    					start_lights[i].setColors(1, 1, 1);
    				start_lights[i].draw(gl);
    			gl.glPopMatrix();
    		}
    	}
    	else if ( m_countdown_time - System.nanoTime() >= 1*WallOffEngine.NANOSECOND )
    	{
    		for ( int i = 0; i<start_lights.length; i ++)
    		{    				
    			gl.glPushMatrix();
    				if ( m_player.getTheta() == 0 || m_player.getTheta() == WallOffEngine.PI)
    				{
    					gl.glTranslatef(m_player.getX() + correction*FloatMath.cos(m_player.getTheta()),
						        		m_player.getY()+correction,
						        		m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.cos(m_player.getTheta()) );
	    			}
    				else
    				{
    					gl.glTranslatef(m_player.getX() + i*FloatMath.sin(m_player.getTheta())*correction - correction*FloatMath.sin(m_player.getTheta()),
    									m_player.getY()+correction,
    									m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.sin(m_player.getTheta()) );
    				}
    				gl.glScalef( .3f, .3f, .3f );
    				if ( i <= 1)
    					start_lights[i].setColors(1, 0, 0);
    				else
    					start_lights[i].setColors(1, 1, 1);
    				start_lights[i].draw(gl);
    			gl.glPopMatrix();
    		}
    	}
    	else if ( m_countdown_time - System.nanoTime() > 0 )
    	{
    		for ( int i = 0; i<start_lights.length; i ++)
    		{    				
    			gl.glPushMatrix();
    				if ( m_player.getTheta() == 0 || m_player.getTheta() == WallOffEngine.PI)
    				{
    					gl.glTranslatef(m_player.getX() + correction*FloatMath.cos(m_player.getTheta()),
						        		m_player.getY()+correction,
						        		m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.cos(m_player.getTheta()) );
	    			}
    				else
    				{
    					gl.glTranslatef(m_player.getX() + i*FloatMath.sin(m_player.getTheta())*correction - correction*FloatMath.sin(m_player.getTheta()),
    									m_player.getY()+correction,
    									m_player.getZ() - i*FloatMath.cos(m_player.getTheta())*correction + correction*FloatMath.sin(m_player.getTheta()) );
    				}
    				gl.glScalef( .3f, .3f, .3f );
    				if ( i <= 1)
    					start_lights[i].setColors(1, 0, 0);
    				else
    					start_lights[i].setColors(0, 1, 0);
    				start_lights[i].draw(gl);
    			gl.glPopMatrix();
    		}
    	}    	
    	else if ( m_countdown_time - System.nanoTime() <= 0 )
    		m_countdown = !m_countdown;
	}

	/* function handles all of the shrink properties */
    private void shrinkMap(GL10 gl) {
    	if ( WallOffEngine.map_shrinkable && m_shrink_map_count < WallOffEngine.map_max_shrink_times)
    	{
    		if ( m_shrink_count <= 0 ) /* reduce the map size and draw the new map edges */
    		{
    			WallOffEngine.map_size = WallOffEngine.map_size - WallOffEngine.map_shrink_ammount;
    			
    			//add new wall objects to draw
    			createMapBounds(gl);    			
    			
    			line.setScaleAmmount( WallOffEngine.map_size - WallOffEngine.map_shrink_length );    			
    			m_shrink_count = WallOffEngine.map_shrink_ticks + 1;
    			
    			m_shrink_map_count++;
    		}
    		else if ( m_shrink_count < WallOffEngine.map_shrink_count ) /* show the map being reduced */
    		{
    			WallOffEngine.map_size = WallOffEngine.map_size - WallOffEngine.map_shrink_ammount;
    			//add new wall objects to draw
    			createMapBounds(gl);
    			m_wall_shrink_show = true;
    			
    		}
    		if ( m_shrink_count <= WallOffEngine.map_shrink_ticks/2 )
    		{    			
	    		gl.glPushMatrix();   	
	        		line.draw(gl);
	    		gl.glPopMatrix();
    		}
    		m_shrink_count -= 1;
    	}
	}

	/* check to make sure each character has not collided with anything */
    private void checkCollisions( )
    {
		/* wall collisions */
		if ( m_player.getX() >= WallOffEngine.map_size - m_player.getCharacter().getRadius() )
			m_player.setAlive( false );
		else if ( m_player.getX() <= -WallOffEngine.map_size + m_player.getCharacter().getRadius() )
			m_player.setAlive( false );
		else if ( m_player.getZ() >= WallOffEngine.map_size - m_player.getCharacter().getRadius() )
			m_player.setAlive( false );
		else if ( m_player.getZ() <= -WallOffEngine.map_size + m_player.getCharacter().getRadius() )			
			m_player.setAlive( false );
		
		/* Collisions for each of our box objects */
		for (Cube cube : m_obsticles) {
			if ( m_player.getX() - m_player.getCharacter().getRadius() <= cube.getX() + cube.getMidToSide() && // right side
			     m_player.getX() + m_player.getCharacter().getRadius() >= cube.getX() - cube.getMidToSide() && // left side
			     m_player.getZ() - m_player.getCharacter().getRadius() <= cube.getZ() + cube.getMidToSide() && // bottom
			     m_player.getZ() + m_player.getCharacter().getRadius() >= cube.getZ() - cube.getMidToSide()) //top side
				m_player.setAlive(false);
		}
		
		/* collisions for each of the other players */
		for (Player player : players) {
			if ( player.isAlive() && !player.equals(m_player) )
			{
				if ( Math.sqrt( Math.pow( m_player.getX() - player.getX(), 2 ) +
					            Math.pow( m_player.getZ() - player.getZ(), 2 ) ) <=
				    2*m_player.getCharacter().getRadius() )
				{
					m_player.setAlive( false );
				}
			}
		}
		
		/* Detection for the players tail */
		float L1, L2, B;
		int player_tail_correction;
		for (Player player : players) 
		{
			//we don't want the player to check the last few positions of their own tail. this will always kill them
			if ( player.isAlive() )
			{
				if ( player.equals(m_player) ) 
					player_tail_correction = 35; 
				else
					player_tail_correction = 0;
					
				for ( int i = 0; i < player.getTail().getTailLength() - player_tail_correction; i += 3 )
				{
					L1 = FloatMath.sqrt( (float)(Math.pow( m_player.getTail().getTailEntry(i) - player.getX() , 2) +
						                         Math.pow( m_player.getTail().getTailEntry(i+2) - player.getZ() , 2)) );
					L2 = FloatMath.sqrt( (float)(Math.pow( m_player.getX() - player.getTail().getTailEntry(i+3), 2) +
		                                         Math.pow( m_player.getZ() - player.getTail().getTailEntry(i+5) , 2)) );
					B =  FloatMath.sqrt( (float)(Math.pow( player.getTail().getTailEntry(i) - player.getTail().getTailEntry(i+3), 2) +
                                                 Math.pow( player.getTail().getTailEntry(i+2) - player.getTail().getTailEntry(i+5) , 2) ) );
					
					// kill the player
					if ( m_player.getCharacter().getRadius() + B >= L1 + L2 ) { m_player.setAlive(false); }
				}
			}
		}
		
		if (!m_player.isAlive()) { playersAlive = playersAlive - 1; }
		
    }
    
    /* the method used to draw the level itself */
    private void drawMap(GL10 gl) {    	
		if ( WallOffEngine.map_name.equals(WallOffEngine.map_Original) )
		{
			//draw the ground square
			gl.glPushMatrix();
				//scale the ground based on the map size
				gl.glScalef(WallOffEngine.map_initial_size, 1f, WallOffEngine.map_initial_size);
				//push the ground down one because the sphere radius will be one
				gl.glTranslatef(0, -1f, 0);
				//rotate the map so it will be along the xz plane
				gl.glRotatef(90, 1, 0, 0);				
				m_square_ground.draw(gl);
			gl.glPopMatrix();
			
			//draw the four walls
			gl.glPushMatrix(); //back wall along negative z
				gl.glTranslatef(0, WallOffEngine.map_initial_size/5 - 1, -WallOffEngine.map_initial_size);			
				gl.glScalef(WallOffEngine.map_initial_size+1, WallOffEngine.map_initial_size/5, 1f);				
				m_square_wall.draw(gl);
			gl.glPopMatrix();			
			gl.glPushMatrix(); //front wall along pos z
				gl.glTranslatef(0, WallOffEngine.map_initial_size/5 - 1, WallOffEngine.map_initial_size);
				gl.glScalef(WallOffEngine.map_initial_size+1, WallOffEngine.map_initial_size/5, 1f);				
				gl.glRotatef(180, 0, 1, 0);
				m_square_wall.draw(gl);
			gl.glPopMatrix();
			gl.glPushMatrix(); //wall along pos x
				gl.glTranslatef(WallOffEngine.map_initial_size, WallOffEngine.map_initial_size/5 - 1, 0);
				gl.glScalef(1f, WallOffEngine.map_initial_size/5, WallOffEngine.map_initial_size+1);								
				gl.glRotatef(90, 0, 1, 0);
				m_square_wall.draw(gl);
			gl.glPopMatrix();
			gl.glPushMatrix(); //wall along neg x
				gl.glTranslatef(-WallOffEngine.map_initial_size, WallOffEngine.map_initial_size/5 - 1, 0);
				gl.glScalef(1f, WallOffEngine.map_initial_size/5, WallOffEngine.map_initial_size+1);			
				gl.glRotatef(270, 0, 1, 0);
				m_square_wall.draw(gl);
			gl.glPopMatrix();
			
		}
	}

    /* if the map is shrinking, draw the new map bounds each pass */
    private void createMapBounds( GL10 gl )
    {
    	//positive x new wall cube
    	m_wall_shrink_cubes[0].setX(WallOffEngine.map_initial_size - (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2);
    	m_wall_shrink_cubes[0].setScaleX( (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2 );
    	m_wall_shrink_cubes[0].setScaleZ( WallOffEngine.map_initial_size );
    	//negative x wall cube
    	m_wall_shrink_cubes[1].setX(-WallOffEngine.map_initial_size + (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2);
    	m_wall_shrink_cubes[1].setScaleX( (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2 );
    	m_wall_shrink_cubes[1].setScaleZ( WallOffEngine.map_initial_size );
    	//positive z wall cube
    	m_wall_shrink_cubes[2].setZ(WallOffEngine.map_initial_size - (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2);
    	m_wall_shrink_cubes[2].setScaleX( WallOffEngine.map_size - WallOffEngine.map_shrink_ammount );
    	m_wall_shrink_cubes[2].setScaleZ( (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2 );
    	//negative z wall cube
    	m_wall_shrink_cubes[3].setZ(-WallOffEngine.map_initial_size + (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2);
    	m_wall_shrink_cubes[3].setScaleX( WallOffEngine.map_size - WallOffEngine.map_shrink_ammount );
    	m_wall_shrink_cubes[3].setScaleZ( (WallOffEngine.map_initial_size - WallOffEngine.map_size)/2 );
    }
    
    /* Set the surface to landscape view */
	public void onSurfaceChanged(GL10 gl, int width, int height) 
    {
		// Select the projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);		
		// Reset the projection matrix
		gl.glLoadIdentity();
		// Calculate the aspect ratio of the window
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 500.0f);
		// Select the modelview matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// Reset the modelview matrix
		gl.glLoadIdentity();
		
		m_countdown_time = System.nanoTime() + 4*WallOffEngine.NANOSECOND;
    }

	/* called when our view is first created */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) 
    {
    	gl.glDisable(GL10.GL_DITHER);				//Disable dithering
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do

		
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		
		/* load the necessary textures */
		this.m_square_ground.loadGLTexture(gl, m_context, R.drawable.floor_new);
		this.m_square_wall.loadGLTexture(gl, m_context, R.drawable.wall_new);
		if ( WallOffEngine.obstacles )
    	{
    		for ( int i = 0; i<WallOffEngine.obstacles_number; i++ )
    		{
    			m_obsticles[i].loadGLTexture(gl, m_context, R.drawable.square_collision_objects_new);
    		}
    	}
		if ( WallOffEngine.map_shrinkable )
		{
			for ( int i = 0; i < this.m_wall_shrink_cubes.length; i++)
			{
				m_wall_shrink_cubes[i].loadGLTexture(gl, m_context, R.drawable.wall_new);
			}
		}

    }
    
} 
