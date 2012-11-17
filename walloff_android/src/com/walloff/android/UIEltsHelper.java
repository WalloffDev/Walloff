package com.walloff.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.walloff.android.Tasks.SendToWalloffServer;

public class UIEltsHelper {
	
	private Context activity_context = null;
	private ViewFlipper view_flipper = null;
	private Dialog progress_dialog = null;
	
	/* Task handle(s) */
	private Tasks.SendToWalloffServer send_ws = null;
	
	/* Main menu View elements */
	// Multiplayer
	public Button multi_host = null, multi_join = null, multi_create = null;
	public EditText multi_lname = null;
	public TextView error_info = null;
	public Spinner multi_mname = null, multi_msize = null, multi_onum = null;
	public ToggleButton multi_mshrink = null, multi_obstacles = null, multi_moving = null;
	public ViewFlipper multi_flipper = null;
	
	public UIEltsHelper( Context context, ViewFlipper view_flipper ) {
		super( );
		
		this.send_ws = new SendToWalloffServer( context );
		this.view_flipper = view_flipper;
		this.activity_context = context;
	}

	/* Register UI elts for associated menu view */
    public void registerUIElts( int index ) {    	
    	switch( index ) {
    	case 0:
    		break;
    		
    	case 1:
    		
    		/* Register elt(s) */
    		this.multi_host = ( Button )this.view_flipper.findViewById( R.id.main_menu_multi_btn_host );
    		this.multi_join = ( Button )this.view_flipper.findViewById( R.id.main_menu_btn_join );
    		this.multi_create = ( Button )this.view_flipper.findViewById( R.id.create_lobby_btn_create );
    		this.multi_flipper = ( ViewFlipper )this.view_flipper.findViewById( R.id.main_menu_multiplayer_flipper );
    		this.multi_lname = ( EditText )this.view_flipper.findViewById( R.id.create_lobby_lobby_name );
    		this.error_info = ( TextView )this.view_flipper.findViewById( R.id.lobby_name_error );
    		this.multi_mname = ( Spinner )this.view_flipper.findViewById( R.id.create_lobby_map_spin );
    		this.multi_msize = ( Spinner )this.view_flipper.findViewById( R.id.create_lobby_size_spin );
    		this.multi_mshrink = ( ToggleButton )this.view_flipper.findViewById( R.id.create_lobby_shrink_tog );
    		this.multi_obstacles = ( ToggleButton )this.view_flipper.findViewById( R.id.create_lobby_obj );
    		this.multi_onum = ( Spinner )this.view_flipper.findViewById( R.id.create_lobby_numobj_spin );
    		this.multi_moving = ( ToggleButton )this.view_flipper.findViewById( R.id.create_lobby_moving_tog );
    		
    		/* Setup necessary listeners and adapters */
    		this.multi_host.setOnClickListener( new View.OnClickListener( ) {
				public void onClick( View arg0 ) {
					multi_flipper.setDisplayedChild( 0 );
				}
			} );
    		this.multi_join.setOnClickListener( new View.OnClickListener( ) {
				
				public void onClick( View arg0 ) {	
					multi_flipper.setDisplayedChild( 1 );
				}
			});
    		this.multi_create.setOnClickListener( new View.OnClickListener( ) {
				
				public void onClick( View view ) {
					
					try {
						
						SharedPreferences prefs = activity_context.getSharedPreferences( Constants.PREFS_FILENAME, MainMenuActivity.MODE_PRIVATE );
						String uname = prefs.getString( Constants.L_USERNAME, "" );
						
						JSONObject to_send = new JSONObject( );
						to_send.put( Constants.M_TAG, Constants.CREATE );
						to_send.put( Constants.HOST, uname );
						to_send.put( Constants.LOB_NAME, multi_lname.getText( ).toString( ) );
						to_send.put( Constants.MAP_NAME, ( String )multi_mname.getSelectedItem( ) );
						to_send.put( Constants.MAP_SIZE, ( String )multi_msize.getSelectedItem( ) );
						to_send.put( Constants.MAP_SHRINK, String.valueOf( multi_mshrink.isChecked( ) ) );
						to_send.put( Constants.MAP_ONUM, ( String )multi_onum.getSelectedItem( ) );
						to_send.put( Constants.MAP_MOVE, String.valueOf( multi_moving.isChecked( ) ) );
						
						/* Set Intent and progress dialog */
						send_ws.setIntent( new Intent( activity_context, GameLobbyActivity.class ) );
						send_ws.execute( to_send );
						
					} catch( JSONException e ) {
						e.printStackTrace( );
					}
				}
			} );
    		
    		break;
    		
    	case 2:
    		break;
    	case 3:
    		break;
    	default:
    		break;
    	}
    }
}
