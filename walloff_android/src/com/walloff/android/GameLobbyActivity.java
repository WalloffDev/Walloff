package com.walloff.android;

import org.json.JSONObject;

import com.walloff.android.Tasks.SendToWalloffServer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class GameLobbyActivity extends Activity {

	private SendToWalloffServer send_ws = null;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.lobby_view );
	}

	@Override
	protected void onPause( ) {
		super.onPause( );
		
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
