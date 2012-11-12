package com.walloff.android;

import android.content.Context;
import android.widget.Toast;

public final class Constants {
	
	/* GCM */
	public static final String project_id = "55756868908";
	public static final String TAG = "GCM";
	
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

	/* Message TAG(s) */
	public static final String M_TAG = "tag";
	public static final String LOGIN = "login";
	public static final String L_USERNAME = "uname";
	public static final String L_PASSWORD = "passwd";
	public static final String L_GCMID = "gcmid";
	
	/* ViewFlipper  necessities  */
	public static final int I_COMMAND = 5;
	public static final String COMMAND_TITLE = "where do you want to go";
	
	/* Display object is used to get dimensions of the screen */
	public static float window_size_x;
	public static float window_size_y;
	
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
