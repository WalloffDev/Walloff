package com.walloff.android;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class GestureDetection implements OnGestureListener {
	
	private Context activity_context = null;
	
	/* View resources used by the system */
	private ViewFlipper viewFlipper;
	
	/* ViewFlipper starts at this index for its children */
	private int view_index = 0;
	
	/* Gesture detector */
	GestureDetector gesture;
	
	/* Activate the gesture listener */
	public boolean onTouchEvent( MotionEvent me ) {
        return this.gesture.onTouchEvent( me );
    }
	
	public GestureDetection( Context context, ViewFlipper flipper ) {
		this.activity_context = context;
		this.viewFlipper = flipper;
		this.gesture = new GestureDetector( context, this );
	}
	
	public boolean onDown( MotionEvent e ) {
		return false;
	}

	public void onLongPress( MotionEvent e ) {	
		this.view_index = 0;
		
		/* Set animations ( fade_in & fade_out )*/
		this.viewFlipper.setInAnimation( this.activity_context, R.anim.fade_in );
		this.viewFlipper.setOutAnimation( this.activity_context, R.anim.fade_out );
		
		this.viewFlipper.setDisplayedChild( this.view_index );
	}

	public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
		return false;
	}

	public void onShowPress( MotionEvent e ) { }

	public boolean onSingleTapUp( MotionEvent e ) {
		return false;
	}

	public void onClick( View arg0 ) { }

	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {

		/* Swipe left->right */
		if( e1.getX( ) < e2.getX( ) ) {
			this.view_index--;
			if( this.view_index < 0 ) this.view_index = Constants.MAX_X_VIEWS;
			
			/* Set animations ( left->right ) */
			this.viewFlipper.setInAnimation( AnimationUtils.loadAnimation( this.activity_context, R.anim.in_from_left ) );
			this.viewFlipper.setOutAnimation( AnimationUtils.loadAnimation( this.activity_context, R.anim.out_to_right ) );
			
			this.viewFlipper.setDisplayedChild( this.view_index );
			return true;
		}
		
		/* Swipe right->left */
		else if( e1.getX( ) > e2.getX( ) ) {
			this.view_index++;
			if( this.view_index > Constants.MAX_X_VIEWS ) this.view_index = 0;

			/* Set animations ( right->left )*/
			this.viewFlipper.setInAnimation( AnimationUtils.loadAnimation( this.activity_context, R.anim.in_from_right ) );
			this.viewFlipper.setOutAnimation( AnimationUtils.loadAnimation( this.activity_context, R.anim.out_to_left ) );
			
			this.viewFlipper.setDisplayedChild( this.view_index );
			return true;
		}
		return false;
	}
}
