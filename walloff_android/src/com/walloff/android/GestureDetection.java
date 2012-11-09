package com.walloff.android;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ViewFlipper;

public class GestureDetection implements OnGestureListener {
	
	//view resources used by the system
	private ViewFlipper viewFlipper;
	
	//gesture detector
	GestureDetector gesture;
	
	/* activate the gesture listener */
	public boolean onTouchEvent(MotionEvent me) {
        return gesture.onTouchEvent(me);
    }
	
	public GestureDetection(Context context, ViewFlipper flipper) {
		this.viewFlipper = flipper;
		gesture = new GestureDetector(context, this);
	}
	
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if( e1.getX() < e2.getX() )
		{
			viewFlipper.setDisplayedChild(2);
			return true;
		}
		else if( e1.getX() > e2.getX() )
		{
			viewFlipper.setDisplayedChild(1);
			return true;
		}
		return false;
	}
}
