package com.walloff.android;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import org.json.JSONException;
import org.json.JSONObject;

import com.walloff.game.WallOffEngine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/** This class is a manager for the client-side networking requirements **/
public class NetworkingManager {	
	
	/** CONSTANT(S) **/
	public static final String N_MAN_TAG = "N_MAN";	/* Log tag */
	
	/** CLASS MEMBER(S) **/
	private Context activity_cont = null;				/* Owner activity's context */
	private Player[ ] players = null;					/* Holds current client's neighbor players */
	private GCManager[ ] gc_mans = null;				/* Holds game connection managers for each opponent */
	private com.walloff.game.Player game_player = null;
	
	/** CONSTRUCTOR **/
	public NetworkingManager( Context context ) {
		this.activity_cont = context;
	}
	
	/** HELPER CLASS **/
	public class GCManager {
		
		/* CLASS MEMBER(S) */
		private Player gc_opo = null;
		private DatagramSocket gc_soc = null;
		private Receiver gc_rec = null;
		private Sender gc_sen_priv = null, gc_sen_pub = null;
		private Boolean ready_to_send = null;
		
		/* CONSTRUCTOR(S) */
		public GCManager( Player gc_opo ) {
			super( );
			this.gc_opo = gc_opo;
		}

		/* HELPER(S) */
		public boolean initialize( ) {
			
			try {
				this.gc_soc = new DatagramSocket( );
			} catch( Exception e ) {
				e.printStackTrace( );
				this.gc_soc = null;
				return false;
			}
			
			if( this.gc_soc != null ) {
				this.gc_rec = new Receiver( this.gc_soc );
				this.gc_rec.execute( );
				this.gc_sen_priv = new Sender( this.gc_soc, 0 );
				this.gc_sen_pub = new Sender( this.gc_soc, 1 );
				this.gc_sen_priv.execute( );
				this.gc_sen_pub.execute( );
			}
			
			return true;
		}
		public void terminate( ) {
			this.gc_rec.cancel( true );
			this.gc_sen_priv.cancel( true );
			this.gc_sen_pub.cancel( true );
		}
		public Player getOpponent( ) {
			return this.gc_opo;
		}
		public void setReadyToSend ( boolean b ) { ready_to_send = b; }
		
		/* Receiver Thread: listens for game data from a lobby opponent */
		private class Receiver extends AsyncTask< Void, Integer, Void > {

			/* CLASS MEMBER(S) */
			private DatagramSocket soc = null;
			private DatagramPacket r_pac = null;
			private byte[ ] r_buf = null;
			
			/* CONSTRUCTOR(S) */
			public Receiver( DatagramSocket gc_soc ) {
				super( );
				this.soc = gc_soc;
				this.r_buf = new byte[ Constants.INGAME_BUFLEN ];
				this.r_pac = new DatagramPacket( this.r_buf, this.r_buf.length );
			}
			
			@Override
			protected Void doInBackground( Void... params ) {
				
				
				while( true ) {
					if( isCancelled( ) ) break;
					try {
						this.soc.receive( this.r_pac );
						Log.i( "GC_CON", "RECV: " + this.r_pac.getData( ).toString( ).trim( ) );
						/* TODO: pass received player positions to game engine, or write to array directly depending on how Dan is doing that */
						String payload = new String( this.r_pac.getData( ) ).trim( );
						JSONObject temp = new JSONObject( payload );
						String m_intent = temp.getString( Constants.M_TAG );
						
						//send the player update position to our WallOffRender
						if( m_intent.equals( WallOffEngine.players_send_position ) ) {
							Intent intent = new Intent( WallOffEngine.players_send_position );
							intent.putExtra( Constants.PAYLOAD, payload );
							activity_cont.sendBroadcast( intent );
						}
						
					} catch( InterruptedIOException iioe ) {
						continue;
					} catch( Exception e ) {
						e.printStackTrace( );
					}
				}
				
				return null;
			}
			
			@Override
			protected void onCancelled( ) {
				super.onCancelled( );
				
				try {
					this.soc.close( );
				} catch( Exception e ) { e.printStackTrace( ); }
				Log.i( NetworkingManager.N_MAN_TAG, "GC: - terminated" );
			}
		}
		/* Sender Thread: sends game data to lobby opponents */
		private class Sender extends AsyncTask< Void, Integer, Void > {

			/* CLASS MEMBER(S) */
			private DatagramSocket soc = null;
			private DatagramPacket s_pac = null;
			private byte[ ] s_buf = null;
			private int target;							/* if 0: send to private net info, else: send to public net info */
			
			/* CONSTRUCTOR(S) */
			public Sender( DatagramSocket gc_soc, int target ) {
				super( );
				this.soc = gc_soc;
				this.target = target;
			}
			
			@Override
			protected Void doInBackground( Void... params ) {
				
				/* Send game socket net info to pre-existing backdoor of opponent until we are notified by our backdoor to start sending to opponent's new game socket
				 * should help with difference in initialization times between clients */
				JSONObject init = new JSONObject( );
				String uname = null;
				try {
					init.put( Constants.M_TAG, Constants.GC_INIT );
					SharedPreferences prefs = activity_cont.getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
					uname = prefs.getString( Constants.L_USERNAME, "" );
					init.put( Constants.L_USERNAME, uname );
					init.put( Constants.GC_PRI_PORT, this.soc.getLocalPort( ) );
				} catch( Exception e ) {
					e.printStackTrace( );
				}
				while( !this.isCancelled( ) && !Constants.in_game ) {
					try {
						this.s_buf = init.toString( ).getBytes( );
						if( this.target != 0 ) {
							this.s_pac = new DatagramPacket( this.s_buf, this.s_buf.length, 
									new InetSocketAddress( gc_opo.get_PubIP( ), gc_opo.get_PubPort( ) ) );
						}
						else {
							this.s_pac = new DatagramPacket( this.s_buf, this.s_buf.length,
									new InetSocketAddress( gc_opo.get_PrivIP( ), gc_opo.get_PrivPort( ) ) );
						}
						this.soc.send( this.s_pac );
						if( this.target != 0 )
							Log.i( NetworkingManager.N_MAN_TAG, "sending public player position" );
						else
							Log.i( NetworkingManager.N_MAN_TAG, "sending private player position" );
						Thread.sleep( Constants.GC_INGAME_SLEEP );
					} catch( Exception e ) {
						e.printStackTrace( );
					}
				}
				
				init = new JSONObject( );
				try {
					init.put( Constants.L_USERNAME, uname );
				} catch( JSONException e1 ) {
					e1.printStackTrace( );
				}
				
				/* TODO: will need to put position updates in somehow */
				
				while( !this.isCancelled( ) ) {
					/* We now have the newly established hole net info, can start sending position updates */
					try {
						if( ready_to_send )
						{
							init.put( Constants.M_TAG, WallOffEngine.players_send_position );
							init.put( WallOffEngine.tag_player, game_player.getID() );
							init.put( WallOffEngine.tag_x_pos, game_player.getX() );
							init.put( WallOffEngine.tag_z_pos, game_player.getZ() );
							init.put( WallOffEngine.tag_tail_index, game_player.getTail().getTailLength() ); 
							ready_to_send = false;
						}
						this.s_buf = init.toString( ).getBytes( );
						if( this.target != 0 ) {
							this.s_pac = new DatagramPacket( this.s_buf, this.s_buf.length,
									new InetSocketAddress( gc_opo.get_PubIP( ), gc_opo.get_GC_PubPort( ) ) );
						}
						else {
							this.s_pac = new DatagramPacket( this.s_buf, this.s_buf.length,
									new InetSocketAddress( gc_opo.get_PrivIP( ), gc_opo.get_GC_PrivPort( ) ) );
						}
						this.soc.send( this.s_pac );
//						if( this.target != 0 )
//							Log.i( NetworkingManager.N_MAN_TAG, "sending POST public player position" );
//						else
//							Log.i( NetworkingManager.N_MAN_TAG, "sending POST private player position" );
						Thread.sleep( WallOffEngine.GAME_THREAD_FPS_SLEEP);
					} catch( Exception e ) {
						e.printStackTrace( );
					}
				}
				
				return null;
			}

			@Override
			protected void onCancelled( ) {
				super.onCancelled( );
			}
		}
	}
	
	/** MODIFIER(S) **/
	public void set_players( Player[ ] players ) {
		this.players = players;
	}
	public void set_context( Context context ) {
		this.activity_cont = context;
	}
	public void sendToAll(com.walloff.game.Player g_player )
	{
		this.game_player = g_player;
		for (GCManager g : gc_mans) {
			g.setReadyToSend(true);
		}
	}
	
	/** GETTER(S) **/
	public GCManager[ ] getGCMans( ) {
		return this.gc_mans;
	}
	public Player[ ] getPlayers( )
	{
		return players;
	}
	
	/** MANAGEMENT METHOD(S) **/
	/* Sets up all necessary game-play background connections */
	public void init_gconns( ) {
		
		/* Assure there are opponents */
		if( this.players == null || this.players[ 0 ] == null ) {
			/* TODO: alert calling activity of this error, not a Toast! */
			Toast.makeText( this.activity_cont, "No opponents available", Toast.LENGTH_SHORT ).show( );
			return;
		}
		this.gc_mans = new GCManager[ this.players.length - 1 ];
		for( int i = 0; i < this.gc_mans.length; i++ )
			this.gc_mans[ i ] = null;
		
		SharedPreferences prefs = this.activity_cont.getSharedPreferences( Constants.PREFS_FILENAME, Context.MODE_PRIVATE );
		String uname = prefs.getString( Constants.L_USERNAME, "" );
		
		int a = 0;
		for( int i = 0; i < this.players.length; i++ ) {
			if( this.players[ i ] == null ) break;
			else if( this.players[ i ].get_Uname( ).equals( uname ) ) continue;
			this.gc_mans[ a ] = new GCManager( this.players[ i ] );
			this.gc_mans[ a ].initialize( );
			a++;
		}
		
		/* TODO: alert calling activity that it's connections are set up, safe to proceed, not a TOAST! */
		Toast.makeText( this.activity_cont, "Connections established", Toast.LENGTH_SHORT ).show( );
	}
	/* Cleanup all game-play background connections */
	public void term_gconns( ) {
		
		/* Assure gc_mans != null */
		if( this.gc_mans == null ) {
			/* TODO: alert calling activity, don't TOAST! */
			Toast.makeText( this.activity_cont, "No current connections", Toast.LENGTH_SHORT ).show( );
			return;
		}
		for( int i = 0; i < this.gc_mans.length; i++ ) {
			if( this.gc_mans[ i ] == null ) continue;
			this.gc_mans[ i ].terminate( );
		}
		
		/* TODO: alert calling activity that it's connections are all terminated, safe to proceed, not a TOAST! */
		Toast.makeText( this.activity_cont, "Connections terminated", Toast.LENGTH_SHORT ).show( );
	}
	
	/* Helper func(s) */
	public void summarize_players( ) {
		String summary = "";
		for( int i = 0; i < this.players.length; i++ ) {
			if( this.players[ i ] == null ) break;
			summary += this.players[ i ].pretty_print( );
		}
		Log.i( N_MAN_TAG, summary );
	}
}