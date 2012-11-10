package com.walloff.android;

import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.google.android.gcm.GCMRegistrar;
import com.walloff.android.Tasks.SendToWalloffServer;

public class MainMenuActivity extends Activity {
	
	/* Gesture listener that will be used to change views */
	private GestureDetection gestureDetector;
	
	/* AsyncTask(s) */
	private Tasks.SendToWalloffServer send_ws = null;
	
	/* Credential Store layout */
	private Dialog cred_store_dialog = null;
	private Button cred_store_save = null;
	private EditText username = null, password = null, password_again = null;
	
	/* Register out custom gesture detector */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
        return gestureDetector.onTouchEvent(me);
    }
	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main_menu );
        
        /* Register AsyncTask(s) */
        send_ws = new SendToWalloffServer( MainMenuActivity.this );

        /* Register our gesture listener */
		gestureDetector = new GestureDetection( this, (ViewFlipper)findViewById(R.id.main_menu_parent) );
    }

	@Override
	protected void onResume( ) {
		super.onResume( );
		
		/* Check SharedPreferences for existing user credentials */
		SharedPreferences prefs = getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
		String creds = prefs.getString( Constants.PREFS_CREDS_KEY, "" );
		
		if( creds.equals( "" ) ) {
			
			/* Setup cred_store dialog */
			this.cred_store_dialog = new Dialog( this );
			this.cred_store_dialog.setContentView( R.layout.cred_store );
			this.cred_store_dialog.setTitle( Constants.CREDSTORE_DIA_TITLE );
			this.cred_store_dialog.setCancelable( false );
			this.username = ( EditText )this.cred_store_dialog.findViewById( R.id.cred_store_username );
			this.password = ( EditText )this.cred_store_dialog.findViewById( R.id.cred_store_password );
			this.password_again = ( EditText )this.cred_store_dialog.findViewById( R.id.cred_store_password2 );
			this.cred_store_save = ( Button )this.cred_store_dialog.findViewById( R.id.cred_store_btn );
			
			/* Prompt user for credentials */
			this.cred_store_dialog.show( );
			
			/* Register with GCM */
			GCMRegistrar.checkDevice( this );
	        GCMRegistrar.checkManifest( this );
	        final String regId = GCMRegistrar.getRegistrationId( MainMenuActivity.this );
	        if( regId.equals( "" ) ) {
	          GCMRegistrar.register( MainMenuActivity.this, Constants.project_id );
	        } else {
	          Log.i( Constants.TAG, "Already registered" );
	        }
	        
	        this.cred_store_save.setOnClickListener( new View.OnClickListener( ) {
				public void onClick( View arg0 ) {
					
					/* TODO: check that passwords match before committing and check length at least 5 characters*/
					if( username.getText().toString( ).equals( "" ) ) {
						
					}
					
					/* Save info to preferences */
					SharedPreferences prefs = getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
					SharedPreferences.Editor editor = prefs.edit( );
					editor.putString( Constants.PREFS_CREDS_KEY, "STORED" );
					editor.putString( Constants.L_USERNAME, username.getText( ).toString( ) );
					editor.putString( Constants.L_PASSWORD, password.getText( ).toString( ) );
					editor.putString( Constants.L_GCMID, new String( regId ) );
					editor.commit( );
					
					/* Send info to Walloff Server */
					try {
						
						JSONObject to_send = new JSONObject( );
						to_send.put( Constants.M_TAG, Constants.LOGIN );
						to_send.put( Constants.L_USERNAME, username.getText( ).toString( ) );
						to_send.put( Constants.L_PASSWORD, password.getText( ).toString( ) );
						to_send.put( Constants.L_GCMID, new String( regId ) );
						
						send_ws.setDialog( cred_store_dialog );
				        send_ws.execute( to_send );
						
					} catch( Exception e ) {
						e.printStackTrace( );
					}
				}
			});
		}
		else {
			/* Do nothing */
		}
	}
}