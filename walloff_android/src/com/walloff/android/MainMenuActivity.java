package com.walloff.android;

import java.io.DataOutputStream;
import java.net.Socket;
import org.json.JSONObject;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainMenuActivity extends Activity {

	//variables that are used to create a server socket
	private static final String url = "walloff.cslabs.clarkson.edu";
	private static final int port = 8080;
	
	// This is the project id generated from the Google console when
	// yToast.makeText(arg0, message, Toast.LENGTH_SHORT).show();ou defined a Google APIs project.
	private static final String PROJECT_ID = "55756868908";

	// This tag is used in Log.x() calls
	private static final String TAG = "MainActivity";

	// This string will hold the lengthy registration id that comes
	// from GCMRegistrar.register()
	private String regId = "";
	private String name = "";

	// These strings are hopefully self-explanatory
	private String registrationStatus = "Not yet registered";
	private String broadcastMessage = "No broadcast message";

	// This intent filter will be set to filter on the string "GCM_RECEIVED_ACTION"
	IntentFilter gcmFilter;

	// button and edittext used by the credstore
	Button sendButton;
	EditText userName;

	// This broadcastreceiver instance will receive messages broadcast
	// with the action "GCM_RECEIVED_ACTION" via the gcmFilter
	BroadcastReceiver gcmReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Broadcast rec. OnRec called", Toast.LENGTH_SHORT).show();
			broadcastMessage = intent.getExtras().getString("gcm");
			if (broadcastMessage != null) {
				// display our received message
				Log.d("message", broadcastMessage);
			}
		}
	};
	
	// Reminder that the onCreate() method is not just called when an app is first opened,
	// but, among other occasions, is called when the device changes orientation.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cred_store);
		
		//Register the cred_store buttons and edit text
		sendButton = (Button)findViewById(R.id.cred_store_btn);
		userName = (EditText)findViewById(R.id.cred_store_username);

		// Create our IntentFilter, which will be used in conjunction with a
		// broadcast receiver.
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		sendButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if ( !userName.getText().toString().equals("") )
				{
					name = userName.getText().toString();
					registerClient();
				}
			}
		});

	}

	// This registerClient() method checks the current device, checks the
	// manifest for the appropriate rights, and then retrieves a registration id
	// from the GCM cloud.  If there is no registration id, GCMRegistrar will
	// register this device for the specified project, which will return a
	// registration id.
	public void registerClient() {

		try {
			// Check that the device supports GCM (should be in a try / catch)
			GCMRegistrar.checkDevice(this);

			// Check the manifest to be sure this app has all the required
			// permissions.
			GCMRegistrar.checkManifest(this);
			// This is an empty placeholder for an asynchronous task to post the
			// registration
			// id and any other identifying information to your server.
			// Get the existmessageing registration id, if it exists.
			regId = GCMRegistrar.getRegistrationId(this);

			if (regId.equals("")) {

				registrationStatus = "Registering...";

				// register this device for this project
				GCMRegistrar.register(getApplicationContext(), PROJECT_ID);
				regId = GCMRegistrar.getRegistrationId(this);

				registrationStatus = "Registration Acquired";
				

			} else {
				GCMRegistrar.setRegisteredOnServer(this, true);
				registrationStatus = "Already registered";
			}
			
			registerReceiver(gcmReceiver, gcmFilter);
			
			sendRegistrationToServer();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			registrationStatus = e.getMessage();	
		}

		Log.d(TAG, registrationStatus);
		
		// This is part of our CHEAGCMIntentServiceT.  For this demo, you'll need to
		// capture this registration id so it can be used in our demo web
		// service.
		Log.d(TAG, regId);

	}

	private void sendRegistrationToServer() {
		Socket s;
		try
		{
			s = new Socket(url, port);
			
			//set up the socket streams
			//InputStream is = s.getInputStream();
			//DataInputStream din = new DataInputStream(is);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			
			//create our json object
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("tag", "login");
			jsonObj.put("uname", name);
			jsonObj.put("gcmid", regId);
			
			
			dout.write(jsonObj.toString().getBytes());
			
			s.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	
	// If the user changes the orientation of his phone, the current activity
	// is destroyed, and then re-created.  This means that our broadcast message
	// will get wiped out during re-orientation.
	// So, we save the broadcastmessage during an onSaveInstanceState
	
	// event, which is called prior to the destruction of the activity.
	//tvBroadcastMessage.setText(broadcastMessage);
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		super.onSaveInstanceState(savedInstanceState);	

		savedInstanceState.putString("BroadcastMessage", broadcastMessage);
	}

	// When an activity is re-created, the os generates an onRestoreInstanceState()
	// event, passing it a bundle that contains any values that you may have put
	// in during onSaveInstanceState()
	// We can use this mechanism to re-display our last broadcast message.
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);

		broadcastMessage = savedInstanceState.getString("BroadcastMessage");
	}

	// If our activity is paused, it is important to UN-register any
	// broadcast receivers.
	@Override
	protected void onPause() {		
		unregisterReceiver(gcmReceiver);
		super.onPause();
	}
	
	// When an activity is resumed, be sure to register any
	// broadcast receivers with the appropriate intent
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);

	}

    // There are no menus for this demo app.  This is just
	// boilerplate code.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
		return true;
	}

	// NOTE the call to GCMRegistrar.onDestroy()
	@Override
	public void onDestroy() {

		GCMRegistrar.onDestroy(this);

		super.onDestroy();
	}

}