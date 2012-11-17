package com.walloff.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class Tasks {

	public static class SendToWalloffServer extends AsyncTask< JSONObject, Void, JSONObject > {

		/* Class members */
		Dialog dialog = null;
		Context activity_context = null;
		Intent next_intent = null;
		Socket soc = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		byte[ ] read = null;
		JSONObject read_in = null, net_info = null;
		
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
		
		@Override
		protected void onPreExecute( ) {
			super.onPreExecute( );
		}

		@Override
		protected JSONObject doInBackground( JSONObject... params ) { 
			
			try {
				this.soc = new Socket( Constants.server_url, Constants.server_port );

				/* Append priv net info */
				//params[ 0 ].put( Constants.PRI_IP, Constants.getLocalIpAddress( ) );
				params[ 0 ].put( Constants.PRI_IP, this.soc.getLocalAddress( ) );
				params[ 0 ].put( Constants.PRI_PORT, this.soc.getLocalPort( ) );
				
				this.dos = new DataOutputStream( this.soc.getOutputStream( ) );
				this.dis = new DataInputStream( this.soc.getInputStream( ) );
				this.dos.write( params[ 0 ].toString( ).getBytes( ) );
				read = new byte[ Constants.MAX_BUF ];
				read_in = new JSONObject( );
				this.dis.read( read );
				read_in.getJSONObject( new String( read ) );
				
			} catch( Exception e ) {
				e.printStackTrace( );
			} finally {
				try {
					this.soc.close( );
				} catch( Exception e ) {
					// Ignore
				}
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
			
			try {
				if( ( ( String )result.get( Constants.RETURN ) ).equals( Constants.FAIL ) )
					Log.i( Constants.FAIL, ( String )result.get( Constants.MESSAGE) );
				return;
			} catch( Exception e ) {
				e.printStackTrace( );
			}
			
				if( this.dialog != null )
					this.dialog.dismiss( );
				if( this.next_intent != null )
					this.activity_context.startActivity( this.next_intent );
		}
		
	}
}
