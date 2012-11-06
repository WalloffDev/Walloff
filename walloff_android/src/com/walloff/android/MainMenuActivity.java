package com.walloff.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.json.JSONObject;

import com.google.android.gcm.GCMBroadcastReceiver;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainMenuActivity extends Activity {
	public final static String SENDER_ID = "55756868908";
	private EditText uName = null;
	private Button butn = null;
	Socket s = null;
	JSONObject myJSON = null;
	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
    	super.onCreate( savedInstanceState );
        setContentView( R.layout.cred_store ); 
        
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this); //this can be taken out for production code
    	final String regid = GCMRegistrar.getRegistrationId(this);
    	
    	butn = (Button)findViewById(R.id.cred_store_btn);
    	uName = (EditText)findViewById(R.id.cred_store_username);
    	
    	if( regid.equals("") )
    	{
    		GCMRegistrar.register( this, SENDER_ID );
    	}
    	else
    	{
    		Log.v("temp", "already reg.");
    	}
        
    	
    	butn.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View v) {
				if ( !uName.equals("") )
				{
					Log.v("Reg id:", regid);
					try
					{
						s = new Socket( "walloff.cslabs.clarkson.edu", 8080 );
						s.setSoTimeout(0);
						myJSON = new JSONObject( );
						myJSON.put( "tag", "login");
						myJSON.put( "uname", uName.getText( ).toString( ) );
						myJSON.put("gcmid", GCMRegistrar.getRegistrationId(MainMenuActivity.this) );
						DataOutputStream dos = new DataOutputStream( s.getOutputStream( ) );
						dos.write(myJSON.toString().getBytes());
						
						DataInputStream dis = new DataInputStream( s.getInputStream( ) );
						byte[] rec = new byte[1024];
						dis.read( rec );
						JSONObject recJson = new JSONObject( new String(rec) );
						
						if ( recJson.getString("return").equals("FAIL")  )
						{
							Log.v( "Error: " , recJson.getString( "message" ) );
						}
						
						s.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
    }

    /* Probably don't need this */
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }*/
}
