package com.walloff.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private Intent m_intent = null;
	
	public GCMIntentService( ) {
		super( Constants.project_id );
	}
	
	@Override
	protected void onError( Context arg0, String arg1 ) {
		Log.i( Constants.TAG, "GCMIntentService onError() called" );
	}

	@Override
	protected void onMessage( Context arg0, Intent arg1 ) {
		Log.i( Constants.TAG, "GCMIntentService onMessage() called" );

		try {

			Bundle temp = arg1.getExtras( );
			if( temp.containsKey( Constants.M_TAG ) ) {
				if( temp.getString( Constants.M_TAG ).equals( Constants.LOBBY_UPDATE ) ) {
					/* Pass update to GameLobbyActivity */
					this.m_intent = new Intent( Constants.BROADCAST_LOB_UPDATE );
					this.m_intent.putExtra( "payload", arg1.getExtras( ) );
					
					/* Wait till user is in lobby */
					while( !Constants.in_lobby ) { }
				}
				else if ( temp.getString( Constants.M_TAG ).equals( Constants.GS ) ) {
					/* Pass GS to GameLobbyActivity */
					this.m_intent = new Intent( Constants.BROADCAST_GS );
				}
				else {
					return;
				}
				sendBroadcast( m_intent );
			}	
			else
				Log.i( GCMIntentService.TAG, "unrecognized tag for message" );
			
		} catch( Exception e ) { e.printStackTrace( ); }
	}

	@Override
	protected void onRegistered( Context arg0, String arg1 ) {
		Log.i( Constants.TAG, "GCMIntentService onRegistered() called" );
	}

	@Override
	protected void onUnregistered( Context arg0, String arg1 ) {
		Log.i( Constants.TAG, "GCMIntentService onUnregistered() called" );
	}
}
