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
	public static final String CREDSTORE_DIA_TITLE = "You only need to enter this once!";
	
	/* Server info */
	public static final String server_url = "walloff.cslabs.clarkson.edu";
	public static final Integer server_port = 8080;
	
	/* Message TAG(s) */
	public static final String M_TAG = "tag";
	public static final String LOGIN = "login";
	public static final String L_USERNAME = "uname";
	public static final String L_PASSWORD = "passwd";
	public static final String L_GCMID = "gcmid";
	
	/* Main Menu Veiws */
	public static final int MAX_X_VIEWS = 2; // Don't include the default ( 0 )
	public static final int MAX_Y_VIEWS = 2; // Don't include the default ( 0 )
	
	/* Generic helper functions */
	public boolean verify_credential_input( Context context, String username, String password, 
																			String password2 ) {
		/* Check username: ( Can't be empty, at most 30 characters ), Django will return error otherwise */
		if( username.equals( "" ) || username.length( ) > 30 ) {
			return false;
		}
		
		return true;
	}
}
