package com.walloff.android;

import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.walloff.android.Tasks.SendToWalloffServer;

public class GameLobbyActivity extends Activity {

	private BroadcastReceiver updater = null, gs = null;
	private SendToWalloffServer send_ws = null;
	private Player[ ] opos = null;
	private NetworkingManager n_man = null;
	
	/* UI elt(s) */
	private TextView lname, p1_name, p2_name, p3_name, p4_name; //countdown, mname, ;
	private View p1, p2, p3, p4;
	
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
		
		/* Setup receivers for GCM */
		this.updater = new BroadcastReceiver( ) {
			@Override
			public void onReceive( Context arg0, Intent arg1 ) {
				updateLobby( ( Bundle )arg1.getExtras( ).get( "payload" ) );
			}
		};
		this.gs = new BroadcastReceiver( ) {
			@Override
			public void onReceive( Context context, Intent intent ) {
				Log.i( "DEBUG", "GS received" );
			}
		};
	}

	public void initUIElts( ) {
		this.lname = ( TextView )findViewById( R.id.lobby_view_lobby_name );
//		this.countdown = ( TextView )findViewById( R.id.lobby_view_countdown );
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
	
	/* Life-cycle func(s) */
	@Override
	protected void onResume( ) {
		super.onResume( );
		
		Constants.in_lobby = true;
		
		/* Register broadcast receiver(s) */
		registerReceiver( this.updater, new IntentFilter( Constants.BROADCAST_LOB_UPDATE ) );
		registerReceiver( this.gs, new IntentFilter( Constants.BROADCAST_GS ) );
	}
	@Override
	protected void onPause( ) {
		super.onPause( );
		
		Constants.in_lobby = false;
		
		/* Unregister broadcast receiver(s) */
		unregisterReceiver( this.updater );
		unregisterReceiver( this.gs );
		
		/* Tell WalloffServer we are leaving */
		try {
		
			SharedPreferences prefs = GameLobbyActivity.this.getSharedPreferences( Constants.PREFS_FILENAME, GameLobbyActivity.MODE_PRIVATE );
			String uname = prefs.getString( Constants.L_USERNAME, "" );
			
			JSONObject to_send = new JSONObject( );
			to_send.put( Constants.M_TAG, Constants.LEAVE );
			to_send.put( Constants.L_USERNAME, uname );
			send_ws = new SendToWalloffServer( GameLobbyActivity.this );
			send_ws.execute( to_send );
			
		} catch( Exception e ) {
			e.printStackTrace( );
		}
	}
}
