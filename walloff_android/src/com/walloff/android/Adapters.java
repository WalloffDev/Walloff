package com.walloff.android;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Adapters {
	
	public static class LLAdapter extends BaseAdapter implements OnItemClickListener {
	
		/* Class members */
		private Context context;
		private JSONArray data = null;
		//private String[ ] keys = null;
		private ProgressDialog progress_dialog;

		/* Constructor(s) */
		public LLAdapter( Context context, JSONArray data, String[ ] keys ) {
			super( );
			this.context = context;
			this.data = data;
			//this.keys = keys;
		}
		
		public int getCount( ) {
			return data.length( );
		}

		public Object getItem( int index ) {
			return index;
		}

		public long getItemId( int index ) {
			return index;
		}
		
		public View getView( int index, View convert_view, ViewGroup parent ) {
			
			if( convert_view == null ) {
				LayoutInflater inflater = ( LayoutInflater )this.context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
				convert_view = inflater.inflate( R.layout.multi_join_lobby_elt, null );
			}
			
			try {
				JSONObject obj = this.data.getJSONObject( index );
				TextView lname = ( TextView )convert_view.findViewById( R.id.join_lobby_elt_lobby_name );
				lname.setText( obj.getString( "pk" ) );
			} catch( Exception e ) {
				e.printStackTrace( );
			}
			
			return convert_view;
		}

		public void onItemClick( AdapterView< ? > arg0, View arg1, int position, long arg3 ) {

			SharedPreferences prefs = context.getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
			String uname = prefs.getString( Constants.L_USERNAME, "" );
			
			try {
				
				/* Start backdoor thread before sending message to server, fixes race condition */
				Constants.backdoor = new WalloffThreads.Backdoor( context );
				Constants.backdoor.reset( );
				if( !Constants.backdoor.initialize( ) ) {
					Log.i( GameLobbyActivity.identity, "error during backdoor initializations" );
					return;
				}
				Constants.backdoor.start( );
				
				JSONObject to_send = new JSONObject( );
				to_send.put( Constants.M_TAG, Constants.JOIN );
				to_send.put( Constants.L_USERNAME, uname );
				to_send.put( Constants.LOB_NAME, ( ( JSONObject )this.data.getJSONObject( position ) ).getString( "pk" ) );
				progress_dialog = new ProgressDialog( context );
				progress_dialog.setCancelable( false );
				Constants.sender.setPDialog( progress_dialog );
				Constants.sender.setIntent( new Intent( context, GameLobbyActivity.class ) );
				Constants.sender.sendMessage( to_send );
				
			} catch( Exception e ) {
				e.printStackTrace( );
			}
		}	
	}
}
