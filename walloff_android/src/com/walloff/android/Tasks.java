package com.walloff.android;

import java.io.DataOutputStream;
import java.net.Socket;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

public class Tasks {

	public static class SendToWalloffServer extends AsyncTask< JSONObject, Void, Void > {

		/* Class members */
		Dialog dialog = null;
		Context activity_context = null;
		Socket soc = null;
		DataOutputStream dos = null;
		
		public SendToWalloffServer( Context activity_context ) {
			super( );
			this.activity_context = activity_context;
		}
		
		public void setDialog( Dialog dialog ) {
			this.dialog = dialog;
		}
		
		@Override
		protected void onPreExecute( ) {
			super.onPreExecute( );
		}

		@Override
		protected Void doInBackground( JSONObject... params ) { 
			
			try {
				
				this.soc = new Socket( Constants.server_url, Constants.server_port );
				this.dos = new DataOutputStream( this.soc.getOutputStream( ) );
				dos.write( params[ 0 ].toString( ).getBytes( ) );
				
			} catch( Exception e ) {
				e.printStackTrace( );
			} finally {
				try {
					this.soc.close( );
				} catch( Exception e ) {
					// Ignore
				}
			}
			
			return null;
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
		protected void onPostExecute( Void result ) {
			super.onPostExecute( result );
			
			if( this.dialog != null )
				this.dialog.dismiss( );
		}
		
	}
}
