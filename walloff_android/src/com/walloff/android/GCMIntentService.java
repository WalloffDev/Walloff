package com.walloff.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.v("WE DON FUCKED UP", arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Log.v("DATA BITCH", arg1.getDataString());
			Bundle b = arg1.getExtras();
			if ( b.containsKey("msg") )
			{
				Log.v("NO KEY", "NO KEY");
			}
			else
				Log.v("GCM", arg1.getExtras().getString("msg"));
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.v("reg", "the device was registered.");
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
	}

}
