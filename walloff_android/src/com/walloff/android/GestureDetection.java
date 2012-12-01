package com.walloff.android;

import android.app.Dialog;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewFlipper;

public class GestureDetection implements OnGestureListener {
	
	private Context activity_context = null;
	Dialog selectionDial = null;
	
	/* View resources used by the system */
	private ViewFlipper viewFlipper;
	
	/* Gesture detector */
	GestureDetector gesture;
	
	/* HUDOptions object */
	HUDOptions selectionDisplay;
	
	/* Activate the gesture listener */
	public boolean onTouchEvent( MotionEvent me ) {
        return this.gesture.onTouchEvent( me );
    }
	
	public void setViewFlipper( ViewFlipper flipper ) {
		this.viewFlipper = flipper;
	}
	
	public GestureDetection( Context context, ViewFlipper flipper ) {
		this.activity_context = context;
		this.viewFlipper = flipper;
		this.viewFlipper.setInAnimation( this.activity_context, R.anim.fade_in );
		this.viewFlipper.setOutAnimation( this.activity_context, R.anim.fade_out );
		this.gesture = new GestureDetector( context, this );
		
		selectionDial = new Dialog(context,  R.style.WalloffCommand);
		selectionDisplay = new HUDOptions(context, viewFlipper, selectionDial);
		
	}
	
	public boolean onDown( MotionEvent e ) {
		return false;
	}

	public void onLongPress( MotionEvent e ) {		
		selectionDisplay.updateTouchLocation( e.getRawX(), e.getRawY() );		
		selectionDial.setContentView(selectionDisplay);
		selectionDial.show();
	}

	public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) { return false;	}

	public void onShowPress( MotionEvent e ) { }

	public boolean onSingleTapUp( MotionEvent e ) {	return false; }

	public void onClick( View arg0 ) { }

	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) { return false; }
}
