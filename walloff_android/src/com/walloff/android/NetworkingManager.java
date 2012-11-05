package com.walloff.android;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/** This class is a manager for the client-side networking
 * 		requirements including the necessary functionality for in game 
 * 		data flow 
 **/
public class NetworkingManager {

	/** CONSTANT(S) **/
	private static final Integer BASE_R_PORT = 8000;	/* Receiver thread port number(s) increments from here */
	private static final int MAX_BUFF = 10;				/* Maximum buffer size for send/receive game-play data */
	private static final int STAT_ATTEMPT = 0;			/* Indicates socket is attempting connection */
	private static final int STAT_CONNECT = 1; 			/* Indicates socket connection has been established */
	private static final String N_MAN_TAG = "N_MAN";	/* Log tag */
	
	/** CLASS MEMBER(S) **/
	private Context activity_cont = null;				/* Owner activity's context */
	private Player[ ] players = null;					/* Holds current client's neighbor players */
	private GCManager[ ] gc_mans = null;				/* Holds game connection managers for each opponent */
	
	/** CONSTRUCTOR **/
	public NetworkingManager( Context context ) {
		this.activity_cont = context;
	}
	
	/** HELPER CLASS **/
	private class GCManager {
		
		/* CLASS MEMBER(S) */
		private int gc_id = 0;
		private Player gc_opo = null;
		private DatagramSocket gc_soc = null;
		private Receiver gc_rec = null;
		private Sender gc_sen = null;
		
		/* CONSTRUCTOR(S) */
		public GCManager( Player gc_opo, int gc_id ) {
			super( );
			this.gc_opo = gc_opo;
			this.gc_id = gc_id;
		}

		/* GETTER(S) */
		public Receiver getReceiver( ) { return this.gc_rec; }
		public Sender getSender( ) { return this.gc_sen; }

		/* HELPER(S) */
		public boolean initialize( ) {
			
			try {
				this.gc_soc = new DatagramSocket( NetworkingManager.BASE_R_PORT + this.gc_id );
			} catch( Exception e ) {
				e.printStackTrace( );
				this.gc_soc = null;
				return false;
			}
			
			if( this.gc_soc != null ) {
				this.gc_rec = new Receiver( this.gc_id, this.gc_soc );
				this.gc_rec.execute( );
				this.gc_sen = new Sender( this.gc_id, this.gc_soc );
				this.gc_sen.execute( );
			}
			
			return true;
		}
		public void terminate( ) {
			
		}
		/* Receiver Thread: listens for game data from lobby opponents */
		/* TODO: handle exceptions more nicely with UI features based on the exception */
		/* TODO: use onPublishProgress to give UI cues to user as to status of connections */
		private class Receiver extends AsyncTask< Void, Integer, Void > {

			/* CLASS MEMBER(S) */
			private DatagramSocket gc_soc = null;
			private DatagramPacket r_pac = null;
			private byte[ ] r_buf = null;
			private int r_id = 0;
			
			/* CONSTRUCTOR(S) */
			public Receiver( int r_id, DatagramSocket gc_soc ) {
				super( );
				this.r_id = r_id;
				this.gc_soc = gc_soc;
			}
			
			@Override
			protected Void doInBackground( Void... params ) {
				return null;
			}

			@Override
			protected void onProgressUpdate( Integer... values ) {
				super.onProgressUpdate( values );
				switch( values[ 0 ] ) {
				case NetworkingManager.STAT_ATTEMPT:
					Log.i( NetworkingManager.N_MAN_TAG, "GC: " + this.r_id + " - initializing listener" );
					break;
				case NetworkingManager.STAT_CONNECT:
					Log.i( NetworkingManager.N_MAN_TAG, "GC: " + this.r_id + " - listener waiting" );
					break;
				default:
					break;
				}
			}

			@Override
			protected void onCancelled( ) {
				super.onCancelled( );
			}
			
		}
		/* Sender Thread: sends game data to lobby opponents */
		private class Sender extends AsyncTask< Void, Integer, Void > {

			/* CLASS MEMBER(S) */
			private int s_id = 0;
			private DatagramSocket gc_soc = null;
			
			/* CONSTRUCTOR(S) */
			public Sender( int s_id, DatagramSocket gc_soc ) {
				super( );
				this.s_id = s_id;
				this.gc_soc = gc_soc;
			}
			
			@Override
			protected Void doInBackground( Void... params ) {
				return null;
			}

			@Override
			protected void onProgressUpdate( Integer... values ) {
				super.onProgressUpdate( values );
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

	/** MANAGEMENT METHOD(S) **/
	/* Sets up all necessary game-play background connections */
	public void init_gconns( ) {
		
		/* Assure there are opponents */
		if( this.players == null ) {
			/* TODO: alert calling activity of this error, not a Toast! */
			Toast.makeText( this.activity_cont, "No opponents available", Toast.LENGTH_SHORT ).show( );
			return;
		}
		this.gc_mans = new GCManager[ this.players.length ];
		for( int i = 0; i < this.gc_mans.length; i++ ) {
			this.gc_mans[ i ] = new GCManager( this.players[ i ], i );
			this.gc_mans[ i ].initialize( );
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
			this.gc_mans[ i ].terminate( );
		}
		
		/* TODO: alert calling activity that it's connections are all terminated, safe to proceed, not a TOAST! */
		Toast.makeText( this.activity_cont, "Connections terminated", Toast.LENGTH_SHORT ).show( );
	}
}
