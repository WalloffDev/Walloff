package com.walloff.android;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class CommandCenter {

	/* Class members */
	private Context activity_context = null;
	private ViewFlipper vf = null;
	private Dialog frame = null;
	private Button[ ] buttons = null;
	private int num_buttons = 0;
	private int curr_index = 0;
	
	public CommandCenter( Context context, int num_buttons, ViewFlipper vf ) {
		super( );
		this.activity_context = context;
		this.num_buttons = num_buttons;
		this.vf = vf;
		
		/* Initialize dialog */
		this.frame = new Dialog( this.activity_context, R.style.WalloffCommand );
		this.frame.setContentView( R.layout.command_center );
		this.frame.setTitle( Constants.COMMAND_TITLE );
		this.frame.setCancelable( true );
		
		LinearLayout parent = ( LinearLayout )this.frame.findViewById( R.id.command_center_parent );
		parent.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
		
		/* Initialize buttons */
		this.buttons = new Button[ this.num_buttons ];
		for( int i = 0; i < this.num_buttons; i++ ) {
			this.buttons[ i ] = new Button( this.activity_context );
			this.buttons[ i ].setBackgroundResource( R.drawable.button_selector );
			this.buttons[ i ].setTextColor( this.activity_context.getResources( ).getColor( R.color.TronBlue ) );
			this.buttons[ i ].setTextSize( (float)25.0 );
			parent.addView( this.buttons[ i ] );
		}
		this.frame.setContentView( parent );
	}
	
	public void show( ) { 
		this.initialize( this.curr_index );
		this.frame.show( ); 
	}
	public void hide( ) { this.frame.dismiss( ); }
	
	public void initialize( int curr_index ) {
		int button_iter = 0;
		for( int i = 0; i < this.vf.getChildCount( ); i++ ) {
			if( i != curr_index ) {
				this.buttons[ button_iter ].setText( ( String )this.vf.getChildAt( i ).getTag( ) );
				this.buttons[ button_iter ].setTag( String.valueOf( i ) );
				this.buttons[ button_iter ].setOnClickListener( new View.OnClickListener( ) {
					
					public void onClick( View v ) {
						hide( );
						CommandCenter.this.curr_index = Integer.parseInt( ( String )v.getTag( ) );
						vf.setDisplayedChild( CommandCenter.this.curr_index );
					}
				} );
				button_iter++;
			}
			
		}
	}
}
