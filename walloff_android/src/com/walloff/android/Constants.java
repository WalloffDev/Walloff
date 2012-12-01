package com.walloff.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public final class Constants {
	
	/* GCM */
	public static final String project_id = "55756868908";
	public static final String TAG = "GCM";
	public static final String BROADCAST_LOB_UPDATE = "com.walloff.android.lobbyupdate";
	public static final String BROADCAST_GS = "com.walloff.android.gs";
	public static final String LOBBY_UPDATE = "lobby_update";
	public static final String GS = "gs";
	public static final String GS_DELAY = "gs_delay";
	public static WifiManager.WifiLock w_lock = null;
	
	/* Preferences */
	public static final String PREFS_FILENAME = "walloff_prefs";
	public static final String PREFS_CREDS_KEY = "credentials";
	public static final String INVALID_USERNAME = "Username can't be blank and must be less than 30 characters!";
	public static final String INVALID_PASSWORD = "Password can't be blank and must be at least 5 characters!";
	public static final String MISMATCH_PASSWDS = "Passwords don't match, please retype them!";
	public static final int MIN_PASS_LENGTH = 5;
	
	/* Server info */
	public static final String server_url = "walloff.cslabs.clarkson.edu";
	public static final Integer server_port = 8080;
	public static final Integer MAX_BUF = 1024;
	public static final int MAX_LOB_SIZE = 4;

	/* Message TAG(s) */
	public static final String M_TAG = "tag";
	public static final String LOGIN = "login";
	public static final String CREATE = "create";
	public static final String HOST = "host";
	public static final String L_USERNAME = "uname";
	public static final String L_PASSWORD = "passwd";
	public static final String L_IPID = "ip_id";
	public static final String L_GCMID = "gcmid";
	public static final String MAP_NAME = "map";
	public static final String MAP_SIZE = "size";
	public static final String MAP_SHRINK = "shrinkable";
	public static final String MAP_ONUM = "number_obstacles";
	public static final String MAP_MOVE = "moving_obstacles";
	public static final String LOB_NAME = "lname";
	public static final String RETURN = "return";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAIL = "FAIL";
	public static final String MESSAGE = "message";
	public static final String PRI_IP = "priv_ip";
	public static final String PRI_PORT = "priv_port";
	public static final String GET_LOBBIES = "get_lobbies";
	public static final String PAYLOAD = "payload";
	public static final String LEAVE = "leave";
	public static final String JOIN = "join";
	
	/* Lobby list var(s)  */
	public static final String[ ] def_keys = { "" };
	
	/* Display object is used to get dimensions of the screen */
	public static float window_size_x;
	public static float window_size_y;
	
	/* Gesture listener that will be used to change views */
	static GestureDetection gestureDetector;
	
	/* used to tell if we are in the HUD */
	public static boolean in_HUD = false;
	
	/* used to delay lobby intent broadcast until gamelobbyactivity's oncreate */
	public static boolean in_lobby = false;
	
	/* signifies host_player is in game */
	public static boolean in_game = false;
	
	/* Generic helper functions */
	public static boolean verify_credential_input( Context context, String username, String password, 
																			String password2 ) {
		/* Check username: ( Can't be empty, at most 30 characters ), Django will return error if anything else invalid */
		if( username.equals( "" ) || username.length( ) > 30 ) {
			Toast.makeText( context, Constants.INVALID_USERNAME, Toast.LENGTH_SHORT ).show( );
			return false;
		}
		
		/* Check password: ( Can't be empty, at least 5 characters ), Django will return error if anything else invalid */
		if( password.equals( "" ) || password.length( ) < Constants.MIN_PASS_LENGTH ) {
			Toast.makeText( context, Constants.INVALID_PASSWORD, Toast.LENGTH_SHORT ).show( );
			return false;
		}
		else if ( !( password.equals( password2 ) ) ) { // Check that passwords match
			Toast.makeText( context, Constants.MISMATCH_PASSWDS, Toast.LENGTH_SHORT ).show( );
			return false;
		}
		
		return true;
	}
}
