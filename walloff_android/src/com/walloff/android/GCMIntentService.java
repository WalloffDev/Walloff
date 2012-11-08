package com.walloff.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

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
