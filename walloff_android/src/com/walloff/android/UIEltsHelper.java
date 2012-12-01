package com.walloff.android;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.walloff.android.Tasks.SendToWalloffServer;

public class UIEltsHelper {
	
	private Context activity_context = null;
	private ViewFlipper view_flipper = null;
	private ProgressDialog progress_dialog = null;
	private MotionEvent m_event = null;
	
	/* Task handle(s) */
	private Tasks.SendToWalloffServer send_ws = null;
	
	/* long press event listener */
	private View.OnLongClickListener HoldListener = new OnLongClickListener() {
		public boolean onLongClick( View v ) {
			Constants.gestureDetector.onLongPress(m_event);
			m_event = null;
			return true;
		}
	};
	 
	//create our on click listener
	public android.view.View.OnTouchListener OnTouchListener = new OnTouchListener( ) {
		
		public boolean onTouch( View v, MotionEvent event ) {
			/* save the motion event. This can be used by the long click listener */			
			m_event = event;
			
			/* switch on the ID of any clickable object for any of our views */
			int id = v.getId();
			switch ( id ) {
			
				/* multiplayer host button ( tab ) */
				case  R.id.main_menu_multi_btn_host:
					if ( event.getAction() == MotionEvent.ACTION_UP && Constants.in_HUD == false )
						multi_flipper.setDisplayedChild( 0 );
					return true;

				/* multiplayer join button ( tab ) */
				case R.id.main_menu_btn_join:
					if ( event.getAction() == MotionEvent.ACTION_UP && Constants.in_HUD == false ) {
						multi_flipper.setDisplayedChild( 1 );
						avail_lobs = ( ListView )multi_flipper.getCurrentView( );
						
						try {
							JSONObject to_send = new JSONObject( );
							to_send.put( Constants.M_TAG, Constants.GET_LOBBIES );
							progress_dialog = new ProgressDialog( activity_context );
							//progress_dialog.setCancelable( false );
							send_ws = new SendToWalloffServer( activity_context );
							send_ws.setListView( avail_lobs );
							send_ws.setPDialog( progress_dialog );
							send_ws.execute( to_send );
						} catch( JSONException e ) { e.printStackTrace( ); }
					}
					return true;
					
				/* case for the create button */
				case R.id.create_lobby_btn_create:
					if ( event.getAction( ) == MotionEvent.ACTION_UP && Constants.in_HUD == false ) {
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
							
							/* Set Intent and progress dialog and start task */
							progress_dialog = new ProgressDialog( activity_context );
							progress_dialog.setCancelable( false );
							send_ws = new SendToWalloffServer( activity_context );
							send_ws.setPDialog( progress_dialog );
							send_ws.setIntent( new Intent( activity_context, GameLobbyActivity.class ) );
							send_ws.execute( to_send );
							
						} catch( JSONException e ) { 
							e.printStackTrace( ); 
						} catch( Exception e ) {
							  e.printStackTrace( );
						}
					}
					return true;
				default:
					return false;
			}
		}
	};
	
	/* Main menu View elements */
	// Multiplayer
	public Button multi_host = null, multi_join = null, multi_create = null;
	public EditText multi_lname = null;
	public TextView error_info = null;
	public Spinner multi_mname = null, multi_msize = null, multi_onum = null;
	public ToggleButton multi_mshrink = null, multi_obstacles = null, multi_moving = null;
	public ViewFlipper multi_flipper = null; //
	public ListView avail_lobs = null;
	
	
	public UIEltsHelper( Context context, ViewFlipper view_flipper ) {
		super( );
		
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
    		this.multi_host.setOnLongClickListener(HoldListener);
    		this.multi_host.setOnTouchListener(OnTouchListener);
    		
    		this.multi_join = ( Button )this.view_flipper.findViewById( R.id.main_menu_btn_join );
    		this.multi_join.setOnLongClickListener(HoldListener);
    		this.multi_join.setOnTouchListener(OnTouchListener);
    		
    		this.multi_create = ( Button )this.view_flipper.findViewById( R.id.create_lobby_btn_create );
    		this.multi_create.setOnLongClickListener(HoldListener);
    		this.multi_create.setOnTouchListener(OnTouchListener);
    		
    		this.multi_flipper = ( ViewFlipper )this.view_flipper.findViewById( R.id.main_menu_multiplayer_flipper );
    		this.error_info = ( TextView )this.view_flipper.findViewById( R.id.lobby_name_error );
    		
    		this.multi_lname = ( EditText )this.view_flipper.findViewById( R.id.create_lobby_lobby_name );
    		this.multi_lname.setOnLongClickListener(HoldListener);
    		this.multi_lname.setOnTouchListener(OnTouchListener);
    		
    		this.multi_mname = ( Spinner )this.view_flipper.findViewById( R.id.create_lobby_map_spin );
    		this.multi_mname.setOnLongClickListener(HoldListener);
    		this.multi_mname.setOnTouchListener(OnTouchListener);
    		
    		this.multi_msize = ( Spinner )this.view_flipper.findViewById( R.id.create_lobby_size_spin );
    		this.multi_msize.setOnLongClickListener(HoldListener);
    		this.multi_msize.setOnTouchListener(OnTouchListener);
    		
    		this.multi_mshrink = ( ToggleButton )this.view_flipper.findViewById( R.id.create_lobby_shrink_tog );
    		this.multi_mshrink.setOnLongClickListener(HoldListener);
    		this.multi_mshrink.setOnTouchListener(OnTouchListener);
    		
    		this.multi_obstacles = ( ToggleButton )this.view_flipper.findViewById( R.id.create_lobby_obj );
    		this.multi_obstacles.setOnLongClickListener(HoldListener);
    		this.multi_obstacles.setOnTouchListener(OnTouchListener);
    		
    		this.multi_onum = ( Spinner )this.view_flipper.findViewById( R.id.create_lobby_numobj_spin );
    		this.multi_onum.setOnLongClickListener(HoldListener);
    		this.multi_onum.setOnTouchListener(OnTouchListener);
    		
    		this.multi_moving = ( ToggleButton )this.view_flipper.findViewById( R.id.create_lobby_moving_tog );
    		this.multi_moving.setOnLongClickListener(HoldListener);
    		this.multi_moving.setOnTouchListener(OnTouchListener);
    		
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
