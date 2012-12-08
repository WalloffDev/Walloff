package com.walloff.android;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.walloff.android.Tasks.SendToWalloffServer;

public class MainMenuActivity extends Activity {
	
	/* AsyncTask(s) */
	private Tasks.SendToWalloffServer send_ws = null;
	
	/* Credential Store layout */
	private Dialog cred_store_dialog = null;
	private Button cred_store_save = null;
	private EditText username = null, password = null, password_again = null;
			
	/* Register out custom gesture detector */
	@Override
	public boolean onTouchEvent( MotionEvent me ) {
        return Constants.gestureDetector.onTouchEvent( me );
    }
	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );        
        setContentView( R.layout.main_menu );
        
        /* get the height and width of our window */
		Display display = getWindowManager().getDefaultDisplay();
		Constants.window_size_y = (float) display.getHeight();
		Constants.window_size_x = (float) display.getWidth();

        /* Register our gesture listener */
		Constants.gestureDetector = new GestureDetection( this, ( ViewFlipper )findViewById( R.id.main_menu_parent ) );	
    }
  
    
    /** Life Cycle func(s) **/
	@Override
	protected void onResume( ) {
		super.onResume( );
		
		/* Check SharedPreferences for existing user credentials */
		SharedPreferences prefs = getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
		String creds = prefs.getString( Constants.PREFS_CREDS_KEY, "" );
		
		if( creds.equals( "" ) ) {
			
			/* Setup cred_store dialog and dialog elements */
			this.cred_store_dialog = new Dialog( MainMenuActivity.this );
			this.cred_store_dialog.setTitle( R.string.cred_store_title_content );
			this.cred_store_dialog.setContentView( R.layout.cred_store );
			this.cred_store_dialog.setCancelable( false );
			this.username = ( EditText )this.cred_store_dialog.findViewById( R.id.cred_store_username );
			this.password = ( EditText )this.cred_store_dialog.findViewById( R.id.cred_store_password );
			this.password_again = ( EditText )this.cred_store_dialog.findViewById( R.id.cred_store_password2 );
			this.cred_store_save = ( Button )this.cred_store_dialog.findViewById( R.id.cred_store_btn );
			
			/* Prompt user for credentials */
			this.cred_store_dialog.show( );
	        
	        this.cred_store_save.setOnClickListener( new View.OnClickListener( ) {
				public void onClick( View arg0 ) {
					
					/* Check user input for credentials before committing to preferences */
					if( !Constants.verify_credential_input( MainMenuActivity.this, username.getText( ).toString( ), 
							password.getText( ).toString( ), password_again.getText( ).toString( ) ) ) {
						return;
					}
					
					/* Save info to preferences */
					SharedPreferences prefs = getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
					SharedPreferences.Editor editor = prefs.edit( );
					editor.putString( Constants.PREFS_CREDS_KEY, "STORED" );
					editor.putString( Constants.L_USERNAME, username.getText( ).toString( ) );
					editor.putString( Constants.L_PASSWORD, password.getText( ).toString( ) );
					editor.commit( );
					
					/* Send info to Walloff Server */
					try {
						
						JSONObject to_send = new JSONObject( );
						to_send.put( Constants.M_TAG, Constants.LOGIN );
						to_send.put( Constants.L_USERNAME, username.getText( ).toString( ) );
						to_send.put( Constants.L_PASSWORD, password.getText( ).toString( ) );
						
						send_ws = new SendToWalloffServer( MainMenuActivity.this );
						send_ws.setDialog( cred_store_dialog );
						send_ws.setPDialog( new ProgressDialog( MainMenuActivity.this ) );
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

	protected void onPause( ) {
		super.onPause( ); 
	}
}