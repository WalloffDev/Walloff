package com.walloff.android;

import android.content.Context;
import android.widget.Toast;

public final class Constants {
	
	/* Preferences */
	public static final String PREFS_FILENAME = "walloff_prefs";
	public static final String PREFS_CREDS_KEY = "credentials";
	public static final int MIN_PASS_LENGTH = 5;
	
	/* Server info */
	public static final String server_url = "walloff.cslabs.clarkson.edu";
	public static final Integer server_lobby_port = 8080;
	public static final Integer server_heartbeat_port = 8081;
	
	/* Broadcast Intent(s) */
	public static final String BROADCAST_LOBBY_UPDATE = "com.walloff.android.lobbyupdate";
	public static final String BROADCAST_LOBBY_GS = "com.walloff.android.gs";
	public static final String BROADCAST_GC_INIT = "com.walloff.android.gcinit";
	
	/* Nework Manager Plugin */
	public static final int backdoor_port = 8089;
	public static final int HEARTBEAT_INTERVAL = 5000;
	public static final int BACKDOOR_BUFLEN = 512;
	public static final int INGAME_BUFLEN = 100;
	public static final int GC_INIT_SLEEP = 2000;
	public static final int GC_INGAME_SLEEP = 500;
	public static WalloffThreads.Sender sender;
	public static WalloffThreads.Backdoor backdoor;
	
	/* ? */
	public static final int MAX_LOB_SIZE = 4;

	//////////
	public static final String L_IPID = "ip_id"; //
	
	/* Message intent(s) */
	public static final String REGISTER = "register";
	public static final String CREATE = "create";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String GET_LOBBIES = "get_lobbies";
	public static final String LEAVE = "leave";
	public static final String JOIN = "join";
	public static final String LOBBY_UPDATE = "lobby_update";
	public static final String LOBBY_GS = "lobby_gs";
	public static final String GC_INIT = "gc_init";
	
	/* Message key(s) */
	public static final String M_TAG = "tag";
	public static final String L_USERNAME = "uname";
	public static final String L_PASSWORD = "passwd";
	public static final String MAP_NAME = "mname";
	public static final String MAP_SIZE = "msize";
	public static final String MAP_SHRINK = "mshrinkable";
	public static final String MAP_ONUM = "mnumber_obstacles";
	public static final String MAP_MOVE = "mmoving_obstacles";
	public static final String LOB_NAME = "lname";
	public static final String STATUS = "status";
	public static final String PRI_IP = "priv_ip";
	public static final String PRI_PORT = "priv_port";
	public static final String GC_PRI_PORT = "gc_priv_port";
	public static final String GC_PUB_PORT = "gc_pub_port";
	public static final String PAYLOAD = "payload";
	
	/* Lobby list var(s)  */
	public static final String[ ] def_keys = { "" };
	
	/* Display object is used to get dimensions of the screen */
	public static float window_size_x;
	public static float window_size_y;
	
	/* Gesture listener that will be used to change views */
	static GestureDetection gestureDetector;
	
	/* used to tell if we are in the HUD */
	public static boolean in_HUD = false;
	
	/* signifies host_player is in game */
	public static boolean in_game = false;
	
	/* Error(s) */
	public static final String INVALID_USERNAME = "Username can't be blank and must be less than 30 characters!";
	public static final String INVALID_PASSWORD = "Password can't be blank and must be at least 5 characters!";
	public static final String MISMATCH_PASSWDS = "Passwords don't match, please retype them!";
	
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
