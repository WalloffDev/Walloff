package com.walloff.android;

import org.json.JSONObject;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class GameLobbyActivity extends Activity {
	
	private Player[ ] opos = null;
	private NetworkingManager n_man = null;
	
	/* UI elt(s) */
	private TextView lname, p1_name, p2_name, p3_name, p4_name, countdown;//mname;
	private View p1, p2, p3, p4;	
	
	/* handler for heartbeates */
	private WalloffThreads.Backdoor backdoor_task = null;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.lobby_view );
		
		/* Keep screen on during lobbying and gameplay */
		getWindow( ).addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		
		/* Initialize UI elt(s) */
		initUIElts( );
		
		/* Setup NetworkingManager */
		this.n_man = new NetworkingManager( this );
		
		/** TODO: implement lobby countdown clock functionality and establish game conns with n_man, ** disable hardware back button **/
	}

	public void initUIElts( ) {
		this.lname = ( TextView )findViewById( R.id.lobby_view_lobby_name );
		this.countdown = ( TextView )findViewById( R.id.lobby_view_countdown );
//		this.mname = ( TextView )findViewById( R.id.lobby_view_map_name );
		this.p1 = ( View )findViewById( R.id.lobby_view_p1 );
		this.p1_name = ( TextView )this.p1.findViewById( R.id.lobby_view_pname );
		this.p2 = ( View )findViewById( R.id.lobby_view_p2 );
		this.p2_name = ( TextView )this.p2.findViewById( R.id.lobby_view_pname );
		this.p3 = ( View )findViewById( R.id.lobby_view_p3 );
		this.p3_name = ( TextView )this.p3.findViewById( R.id.lobby_view_pname );
		this.p4 = ( View )findViewById( R.id.lobby_view_p4 );
		this.p4_name = ( TextView )this.p4.findViewById( R.id.lobby_view_pname );
	}
	
	public void updateLobby( Bundle payload ) {
		JSONObject temp = null;
		this.opos = new Player[ Constants.MAX_LOB_SIZE ];
		
		for( int i = 0; i < this.opos.length; i++ )
			this.opos[ i ] = null;
		
		try {
			if( payload.containsKey( "lname" ) )
				this.lname.setText( ( String )payload.getString( "lname" ) );
			if( payload.containsKey( "0" ) ) {
				temp = new JSONObject( payload.getString( "0" ) );
				this.p1_name.setText( temp.getString( "uname" ) );
				this.opos[ 0 ] = new Player( temp.getString( "pub_ip" ), 
										Integer.parseInt( temp.getString( "pub_port" ) ), 
										temp.getString( "priv_ip" ), 
										Integer.parseInt( temp.getString( "priv_port" ) ) );	
			}
			else {
				this.p1_name.setText( "" );
			}
			if( payload.containsKey( "1" ) ) {
				temp = new JSONObject( payload.getString( "1" ) );
				this.p2_name.setText( temp.getString( "uname" ) );
				this.opos[ 1 ] = new Player( temp.getString( "pub_ip" ), 
						Integer.parseInt( temp.getString( "pub_port" ) ), 
						temp.getString( "priv_ip" ), 
						Integer.parseInt( temp.getString( "priv_port" ) ) );
			}
			else {
				this.p2_name.setText( "" );
			}
			if( payload.containsKey( "2" ) ) {
				temp = new JSONObject( payload.getString( "2" ) );
				this.p3_name.setText( temp.getString( "uname" ) );
				this.opos[ 2 ] = new Player( temp.getString( "pub_ip" ), 
						Integer.parseInt( temp.getString( "pub_port" ) ), 
						temp.getString( "priv_ip" ), 
						Integer.parseInt( temp.getString( "priv_port" ) ) );
			}
			else {
				this.p3_name.setText( "" );
			}
			if( payload.containsKey( "3" ) ) {
				temp = new JSONObject( payload.getString( "3" ) );
				this.p4_name.setText( temp.getString( "uname" ) );
				this.opos[ 3 ] = new Player( temp.getString( "pub_ip" ), 
						Integer.parseInt( temp.getString( "pub_port" ) ), 
						temp.getString( "priv_ip" ), 
						Integer.parseInt( temp.getString( "priv_port" ) ) );
			}
			else {
				this.p4_name.setText( "" );
			}
			
			/* Set n_man player array */
			this.n_man.set_players( this.opos );	
			
		} catch( Exception e ) { e.printStackTrace( ); }
	}
	
	private class countdown extends AsyncTask< Long, Long, Void > {

		@Override
		protected Void doInBackground( Long... params ) {
			
			/* If message arrived late, start game immediately */
			if( params[ 0 ] < ( System.currentTimeMillis( ) / 1000 ) ) 
				return null;
			
			Long curr_time;
			while( ( curr_time = ( System.currentTimeMillis( ) / 1000 ) ) < params[ 0 ] ) {
				publishProgress( params[ 0 ] - curr_time );
				try { 
					Thread.sleep( 500 ); 
				} 
				catch( InterruptedException e ) {
					e.printStackTrace( );
				}
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate( Long... values ) {
			super.onProgressUpdate( values );
			countdown.setText( "0:" + String.valueOf( values[ 0 ] ) );
		}
		
		@Override
		protected void onPostExecute( Void result ) {
			super.onPostExecute( result );
			Toast.makeText( GameLobbyActivity.this, "Game starting", Toast.LENGTH_SHORT ).show( );
		}		
	}
	
	/* Life-cycle func(s) */
	@Override
	protected void onResume( ) {
		super.onResume( );
		Constants.in_lobby = true;
		
		/* Start WalloffBackdoorTask */
		backdoor_task = new WalloffThreads.Backdoor( this );
		backdoor_task.start();
	}
	
	@Override
	public void onBackPressed( ) {
		if( Constants.in_game ) 
			return;
		else
			super.onBackPressed( );
	}

	@Override
	protected void onPause( ) {
		super.onPause( );
		
		Constants.in_lobby = false;
		
		/* Tell WalloffServer we are leaving */
		try {
			// kill the heartbeats
			this.backdoor_task.interrupt();
			
			SharedPreferences prefs = GameLobbyActivity.this.getSharedPreferences( Constants.PREFS_FILENAME, GameLobbyActivity.MODE_PRIVATE );
			String uname = prefs.getString( Constants.L_USERNAME, "" );
			
			JSONObject to_send = new JSONObject( );
			to_send.put( Constants.M_TAG, Constants.LEAVE );
			to_send.put( Constants.L_USERNAME, uname );
			Constants.sender.sendMessage( to_send );
			
		} catch( Exception e ) {
			e.printStackTrace( );
		}
	}
}
