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

	/**
	 * TODO: if host_player is in game lobby_updates should be cached and applied when game_finished intent is broadcast by gamelobbyactivity
	 * TODO: implement handling of gs_delay parameters in GS messages pass info to gamelobbyactivity's n_man ** ignore in_game rcvs
	 */
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
					
					/* Send sticky broadcast to alleviate race condition of switching to lobby and GCM receive */
					if( !Constants.in_lobby )
						sendStickyBroadcast( m_intent );
					else
						sendBroadcast( m_intent );
				}
				else if( temp.getString( Constants.M_TAG ).equals( Constants.GS ) ) {
					if( Constants.in_game || !Constants.in_lobby ) return;
					/* Pass GS info to GameLobbyActivity */
					this.m_intent = new Intent( Constants.BROADCAST_GS );
					Long gs_delay = Long.parseLong( temp.getString( Constants.GS_DELAY ) );
					this.m_intent.putExtra( Constants.GS_DELAY, gs_delay );
					sendBroadcast( m_intent );
				}
				else {
					return;
				}
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
