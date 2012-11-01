package com.walloff.android;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/** This class is a manager for the client-side ( in game ) networking
 * 		requirements including the necessary functionality for in game 
 * 		data flow 
 **/
public class NetworkingManager {

	/** CONSTANT(S) **/
	private static final Integer BASE_R_PORT = 8000;	/* Receiver thread port number(s) increments from here */
	private static final String N_MAN_TAG = "N_MAN";	/* Log tag */
	private static final int MAX_BUFF = 10;				/* Maximum buffer size for send/receive game-play data */
	private static final Integer STAT_ATTEMPT = 0;		/* Indicates socket is attempting connection */
	private static final Integer STAT_CONNECT = 1; 		/* Indicates socket connection has been established */
	private static final Integer STAT_CLOSED  = 2;		/* Indicates socket connection has been closed */
	private static final String DBG_MSG = "HEY THERE";
	
	/** CLASS MEMBER(S) **/
	private Context activity_cont = null;				/* Owner activity's context */
	private Receiver[ ] receivers = null;				/* Keeps track of n number of listener threads */
	private Sender[ ] senders = null;					/* Keeps track of n number of broadcaster threads */
	private Player[ ] players = null;					/* Holds current client's neighbor players */
	
	/** CONSTRUCTOR(S) **/
	public NetworkingManager( Context context ) {
		this.activity_cont = context;
	}
	
	/** MANAGEMENT METHOD(S) **/
	public void set_players( Player[ ] players ) {
		this.players = players;
	}
	public void set_context( Context context ) {
		this.activity_cont = context;
	}
	/* Sets up all necessary game-play background connections */
	public void init_conns( ) {
		
		/* Start receivers */
		this.receivers = new Receiver[ this.players.length ];
		for( int i = 0; i < this.receivers.length; i++ ) {
			this.receivers[ i ] = new Receiver( i );
			this.receivers[ i ].execute( );
		}
		
		/* Then start senders */
		this.senders = new Sender[ this.players.length ];
		for( int i = 0; i < this.senders.length; i++ ) {
			this.senders[ i ] = new Sender( i );
			this.senders[ i ].execute( this.players[ i ] );
		}
		
		/* TODO: alert calling activity that it's connections are set up, safe to proceed, not a TOAST! */
		Toast.makeText( this.activity_cont, "Connections established", Toast.LENGTH_SHORT ).show( );
	}
	/* Cleanup all game-play background connections */
	public void term_conns( ) {
		
		/* Kill sender(s) */
		for( int i = 0; i < this.players.length; i++ ) {
			this.senders[ i ].cancel( true );
		}
		
		/* Kill receiver(s) */
		for( int i = 0; i < this.players.length; i++ ) {
			this.receivers[ i ].cancel( true );
		}
		
		/* TODO: alert calling activity that it's connections are all terminated, safe to proceed, not a TOAST! */
		Toast.makeText( this.activity_cont, "Connections terminated", Toast.LENGTH_SHORT ).show( );
		
	}
	
	/** NETWORK METHOD(S) **/
	/* Receiver Thread: listens for game data from lobby opponents */
	/* TODO: handle exceptions more nicely with UI features based on the exception */
	/* TODO: us onPublishProgress to give UI cues to user as to status of connections */
	private class Receiver extends AsyncTask< Integer, Integer, Void > {
	
		/* CLASS MEMBER(S) */
		private DatagramSocket r_soc = null;
		private DatagramPacket r_pac = null;
		private byte[ ] r_buf = null;
		private int r_id = 0;
		
		/* CONSTRUCTOR(S) */
		public Receiver( int r_id ) {
			super( );
			this.r_id = r_id;
		}
		
		@Override
		protected Void doInBackground( Integer... params ) {
			
			publishProgress( NetworkingManager.STAT_ATTEMPT );
			this.r_buf = new byte[ NetworkingManager.MAX_BUFF ];
			this.r_pac = new DatagramPacket( r_buf, r_buf.length );
			
			try {
				this.r_soc = new DatagramSocket( NetworkingManager.BASE_R_PORT + this.r_id );
				this.r_soc.setSoTimeout( 0 );
				if( this.r_soc.isBound( ) ) {
					publishProgress( NetworkingManager.STAT_CONNECT );
					
					/* Listen for data */
					while( true && !this.r_soc.isClosed( ) ) {
						if( isCancelled( ) ) {
							break;
						}
						this.r_soc.receive( this.r_pac );
						Log.i( NetworkingManager.N_MAN_TAG, "recv: " + this.r_pac.getData( ).toString( ) );
					}
				}
				
			} catch( SocketException e ) {	/* Error in socket creation, bind, or timeout change */
				e.printStackTrace( );
			} catch( Exception e ) {	/* Unknown error, print it out */
				e.printStackTrace( );
			} finally {
				publishProgress( NetworkingManager.STAT_CLOSED );
			}
			return null;
		}
		@Override
		protected void onProgressUpdate( Integer... values ) {
			super.onProgressUpdate( values );
			switch( values[ 0 ] ) {
			case 0:
				Log.i( NetworkingManager.N_MAN_TAG, "Initiating listener: " + this.r_id );
				break;
			case 1:
				Log.i( NetworkingManager.N_MAN_TAG, "Listener:" + this.r_id );
				break;
			case 2:
				break;
			default:
				break;
			}
		}
		@Override
		protected void onCancelled( ) {
			super.onCancelled( );
			if( this.r_soc.isBound( ) ) {
				try {
					this.r_soc.close( );
				} catch( Exception e ) {
					e.printStackTrace( );
				} finally {
					Log.i( N_MAN_TAG, "Listener:" + this.r_id + " terminated" );
				}
			}
		}	
	}
	/* Sender Thread: sends game data to lobby opponents */
	private class Sender extends AsyncTask< Player, Integer, Void > {
				
		/* CLASS MEMBER(S) */
		private DatagramSocket s_soc = null;
		private DatagramPacket s_pac = null;
		private byte[ ] s_buf = null;
		private int s_id = 0;
		
		/* CONSTRUCTOR(S) */
		public Sender( int s_id ) {
			super( );
			this.s_id = s_id;
		}
		
		@Override
		protected Void doInBackground( Player... params ) {
			
			publishProgress( NetworkingManager.STAT_ATTEMPT );
			this.s_buf = new byte[ NetworkingManager.MAX_BUFF ];
			this.s_pac = new DatagramPacket( this.s_buf, this.s_buf.length );
			
			try {
				this.s_soc = new DatagramSocket( );
				this.s_soc.connect( InetAddress.getByName( params[ 0 ].get_IP( ) ), 
						( NetworkingManager.BASE_R_PORT + this.s_id ) );
				this.s_soc.setSoTimeout( 0 );
				publishProgress( NetworkingManager.STAT_CONNECT );
				
				this.s_buf = NetworkingManager.DBG_MSG.getBytes( );
				this.s_pac.setData( this.s_buf );
				//this.s_soc.send( this.s_pac );
				while( true ) {
					if( this.isCancelled( ) )
						break;
					this.s_soc.send( this.s_pac );
				}
			} catch( Exception e ) {
				e.printStackTrace( );
			} finally {
				this.publishProgress( NetworkingManager.STAT_CLOSED );
			}
			
			return null;
		}
		@Override
		protected void onProgressUpdate( Integer... values ) {
			super.onProgressUpdate( values );
			switch( values[ 0 ] ) {
			case 0:
				Log.i( NetworkingManager.N_MAN_TAG, "Initiating sender: " + this.s_id );
				break;
			case 1:
				Log.i( NetworkingManager.N_MAN_TAG, "Sender: " + this.s_id + " ready" );
				break;
			case 2:
				break;
			default: 
				break;
			}
		}
		@Override
		protected void onCancelled( ) {
			super.onCancelled( );
			if( this.s_soc.isConnected( ) ) {
				try {
					this.s_soc.close( );
				} catch( Exception e ) {
					e.printStackTrace( );
				} finally {
					Log.i( NetworkingManager.N_MAN_TAG, "Sender: " + this.s_id + " terminated" );
				}
			}
		}
	}
}