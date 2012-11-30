package com.walloff.android;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.walloff.android.Adapters.LLAdapter;

public class Tasks {

	public static class SendToWalloffServer extends AsyncTask< JSONObject, Void, JSONObject > {

		/* Class members */
		ProgressDialog p_dialog = null;
		Dialog dialog = null;
		ListView list = null;
		Context activity_context = null;
		Intent next_intent = null;
		Socket soc = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		BufferedReader in = null;
		String read = null;
		JSONObject read_in = null, net_info = null;
		JSONArray payload = null;
		
		public SendToWalloffServer( Context activity_context ) {
			super( );
			this.activity_context = activity_context;
		}
		
		public void setIntent( Intent next_intent ) {
			this.next_intent = new Intent( next_intent );
		}
		
		public void setDialog( Dialog dialog ) {
			this.dialog = dialog;
		}
		
		public void setPDialog( ProgressDialog pdialog ) {
			this.p_dialog = pdialog;
		}
		
		public void setListView( ListView list ) {
			this.list = list;
		}
		
		public JSONObject parse_json( String feed ) {
			JSONObject jobj = null;
			
			try {
				feed = feed.trim( );
				jobj = new JSONObject( feed );
			} catch( Exception e ) {
				e.printStackTrace( );
			}
			return jobj;
		}
		
		@Override
		protected void onPreExecute( ) {
			super.onPreExecute( );
			
			if( this.dialog != null )
				this.dialog.show( );
			else if( this.p_dialog != null )
				this.p_dialog.show( );
		}

		@Override
		protected JSONObject doInBackground( JSONObject... params ) { 
			
			try {
				this.soc = new Socket( Constants.server_url, Constants.server_port );

				/* Append private net info */
				params[ 0 ].put( Constants.PRI_IP, this.soc.getLocalAddress( ).getHostAddress( ) );
				params[ 0 ].put( Constants.PRI_PORT, this.soc.getLocalPort( ) );
				
				/* Store private ip address as host player id */
				SharedPreferences prefs = this.activity_context.getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
				SharedPreferences.Editor editor = prefs.edit( );
				editor.putString( Constants.L_IPID, this.soc.getLocalAddress( ).getHostAddress( ) );
				editor.commit( );
				
				/* Send data to server */
				this.dos = new DataOutputStream( this.soc.getOutputStream( ) );
				this.dos.write( params[ 0 ].toString( ).getBytes( ) );
				this.dos.flush( );
				
				this.dis = new DataInputStream( this.soc.getInputStream( ) );
				this.in = new BufferedReader( new InputStreamReader(dis));
				this.read = this.in.readLine( );
				
				this.read_in = parse_json( this.read );
				
			} catch( Exception e ) {
				e.printStackTrace( );
			} finally {
				try {
					this.soc.close( );
				} catch( Exception e ) { }
			}
			
			return read_in;
		}

		@Override
		protected void onProgressUpdate( Void... values ) {
			super.onProgressUpdate( values );
		}

		@Override
		protected void onCancelled( ) {
			super.onCancelled( );
		}

		@Override
		protected void onPostExecute( JSONObject result ) {
			super.onPostExecute( result );
			
			if( result == null )
				return;
			
			try {
				if( result.has( Constants.RETURN ) ) {
					if( ( ( String )result.get( Constants.RETURN ) ).equals( Constants.FAIL ) ) {
						Log.i( Constants.FAIL, ( String )result.get( Constants.MESSAGE) );
						cleanup( false );
						return;
					}
					else if( ( ( String )result.get( Constants.RETURN ) ).equals( Constants.SUCCESS ) ) {
						Log.i( "SUCCESS", "SUCCESS" );
						if( result.has( Constants.PAYLOAD ) ) {
							this.payload = new JSONArray( result.get( Constants.PAYLOAD ).toString( ) );
						}
					}
					else {
						Log.i( "DEBUG", "Unrecognized RETURN value" );
					}
				}
			} catch( Exception e ) {
				
				e.printStackTrace( );
			}
			cleanup( true );
		}
	
		public void cleanup( boolean result ) {
			
			/* Take care of necessary UI elt(s) */
			if( this.list != null && this.payload != null ) {
				LLAdapter adapter = new Adapters.LLAdapter( this.activity_context, this.payload, null );
				this.list.setAdapter( adapter );
				this.list.setOnItemClickListener( adapter );
			}
			if( this.p_dialog != null )
				this.p_dialog.dismiss( );
			if( this.dialog != null )
				this.dialog.dismiss( );
			if( this.next_intent != null && result != false )
				this.activity_context.startActivity( this.next_intent );
		}
	}
}
