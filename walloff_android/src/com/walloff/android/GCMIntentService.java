package com.walloff.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String PROJECT_ID = "55756868908";
	private static final String TAG = "GCMIntentService";
	
	public GCMIntentService()
	{
		super(PROJECT_ID);
		Log.d(TAG, "Created GCMIntentService");
	}
	
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.d("WE DON FUCKED UP", arg1);
	}


	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Log.d(TAG, "Message Received");
		
		String message = "THIS IS WRONG"; 
		
		message = arg1.getStringExtra("msg");
		
		Log.d("message", message);
		
		sendGCMIntent(arg0, message);
	}
	
	private void sendGCMIntent(Context ctx, String message) {
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("GCM_RECEIVED_ACTION");
		
		broadcastIntent.putExtra("gcm", message);
		
		ctx.sendBroadcast(broadcastIntent);
		
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Toast.makeText(arg0, "registered", Toast.LENGTH_SHORT).show();
		Log.v("reg", "the device was registered.");
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
	}

}
