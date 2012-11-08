package com.walloff.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import com.walloff.android.Tasks.SendToWalloffServer;

public class MainMenuActivity extends Activity {
	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main_menu );
        
        GCMRegistrar.checkDevice( this );
        GCMRegistrar.checkManifest( this );
        final String regId = GCMRegistrar.getRegistrationId( MainMenuActivity.this );
        if( regId.equals( "" ) ) {
          GCMRegistrar.register( MainMenuActivity.this, Constants.project_id );
        } else {
          Log.i( Constants.TAG, "Already registered" );
        }
        Tasks.SendToWalloffServer send_ws = new SendToWalloffServer( MainMenuActivity.this );
        send_ws.execute(  );
    }

	@Override
	protected void onResume( ) {
		super.onResume( );
		
		/* Check SharedPreferences for existing user credentials */
		
		
	}
    
}