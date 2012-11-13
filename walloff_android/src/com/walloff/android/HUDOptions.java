package com.walloff.android;

import android.app.Dialog;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ViewFlipper;

public class HUDOptions extends View {	
	/* used to keep track of views */
	private ViewFlipper viewFlipper = null;
	private String views[]= new String[4];
	private int view_selector[] = new int[4];
	private int curr_index = 0;
	private float image_locations[][] = new float[4][2];
	
	/* keep track of where the user pressed on the screen */
	private float touchedX = 0;
	private float touchedY = 0;
	
	/* Constants used for layout math */
	private float radius = Constants.window_size_x/15;
	private float PI = (float)Math.PI;
	private float angle = (float)Math.PI/6;
	private float boxHeight = ( Constants.window_size_y/6 );
	private float boxWidth = ( Constants.window_size_x/4.8f );
	private final float correction_constant = 2.0f;
	
	/* paint object used for drawing */
	Paint paint;
	
	Dialog dialog = null;
	
	public HUDOptions(Context context, ViewFlipper vf, Dialog dia ) {
		super(context);
		
		//ViewFlipper needs these
		this.viewFlipper = vf;
		
		/* create our new paint selector */
		paint = new Paint();		
		
		//obtain the dialog passed in
		this.dialog = dia;
		this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	public void updateTouchLocation( float x, float y )
	{
		/* obtain where the person touched on the screen */
		touchedX = x;
		touchedY = y;
		
		int view_itter = 0;
		for( int i = 0; i < 5; i++ ) {
			if( i != 0 )
			{
				views[ view_itter ] = ( String )this.viewFlipper.getChildAt( i ).getTag( );
				view_selector[ view_itter ] = i;
				view_itter++;
			}
		}
		
		if ( curr_index != 0 )
		{
			views[ curr_index-1 ] = ( String )this.viewFlipper.getChildAt( 0 ).getTag( );
			view_selector[ curr_index-1 ] = 0;
			view_itter++;
		}
		
		
	}
	
	@Override	public boolean onTouchEvent(MotionEvent me) {
		//get the location of where the person selected
		float e_x = me.getX();
		float e_y = me.getY();
		
		if ( me.getAction() == MotionEvent.ACTION_DOWN )
		{
			for( int i = 0; i < 4; i++ )
			{
				if( ( e_x >= image_locations[i][0] && e_x <= image_locations[i][0] + boxWidth ) &&
						( e_y >= image_locations[i][1] && e_y <= image_locations[i][1] + boxHeight ) ) 
				{
					curr_index = view_selector[i];
					viewFlipper.setDisplayedChild( view_selector[i] );
					dialog.dismiss();
					return true;
				}
			}
		}		
		return false;
	} 
	
	@Override
	protected void onDraw(Canvas canvas){		
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		paint.setAntiAlias(true);
		//canvas.drawCircle(touchedX, touchedY, radius, paint);
		
//		paint.setColor(Color.BLACK);
//		canvas.drawCircle(touchedX, touchedY, radius-5.0f, paint);
//		
//		paint.setColor( getResources().getColor( R.color.TronBlue ));
//		canvas.drawCircle(touchedX, touchedY, 20.0f, paint);
		
		//set our text size and stroke sizethis.initialize( this.curr_index );
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		paint.setStrokeWidth(8f);
		paint.setTextSize(24f);
		
		//we are in the middle of the screen
		if ( touchedX >= ( 26 * Constants.window_size_x)/60  && touchedX <= (54 * Constants.window_size_x)/60 
			&& touchedY <= (2 * Constants.window_size_y	)/3  && touchedY >= Constants.window_size_y/3)
		{
			float arm_1_x = Constants.window_size_x/7;
			float arm_2_y = Constants.window_size_y/8;
			float arm_2_x = Constants.window_size_x/10;
			
			// we have touched near the right of the screen
//			if( touchedX + radius + ( boxWidth/2 ) + default_arm_x_size >= Constants.window_size_x )
//			{
//				arm_1_x =  Constants.window_size_x - ( touchedX + radius + ( boxWidth/2 ) );
//			}
//			//we have touched near the left of the screen
//			else if( touchedX - radius - ( boxWidth/2 ) - default_arm_x_size <= 0 )
//			{
//				arm_1_x = touchedX - radius - ( boxWidth/2 );
//			},
//			else
//			{
//				arm_1_x = Constants.window_size_x/5.5f;
//			}
//			
//			//we are in the upper half of the screen for where we touched
//			if ( Constants.window_size_y - touchedY >= (Constants.window_size_y/2) )
//			{
//				/* check the y height we can use */
//				arm_2_y = touchedY - boxHeight - radius * FloatMath.sin(PI/4);
//			}
//			//we are in the lower half of the screen
//			else if ( Constants.window_size_y - touchedY < (Constants.window_size_y/2) )
//			{
//				arm_2_y = Constants.window_size_y - touchedY - boxHeight - radius * FloatMath.sin(PI/4);
//			}
			
			//top right line
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle),
							touchedY - (radius)*FloatMath.sin(angle),
							touchedX + (radius)*FloatMath.cos(angle) + arm_1_x,
						    touchedY - (radius)*FloatMath.sin(angle), paint);
			//top left line
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle),
							touchedY - (radius)*FloatMath.sin(angle),
							touchedX - (radius)*FloatMath.cos(angle) - arm_1_x,
							touchedY - (radius)*FloatMath.sin(angle), paint);
			//bottom right line
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle), 
							touchedY + (radius)*FloatMath.sin(angle),
							touchedX + (radius)*FloatMath.cos(angle) + arm_1_x, 
							touchedY + (radius)*FloatMath.sin(angle), paint);
			//bottom left line
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle),
							touchedY + (radius)*FloatMath.sin(angle),
							touchedX - (radius)*FloatMath.cos(angle) - arm_1_x,
							touchedY + (radius)*FloatMath.sin(angle), paint);
			
			//top right 2nd arm
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - correction_constant, 
							touchedY - (radius)*FloatMath.sin(angle),
							touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - correction_constant, 
							touchedY - (radius)*FloatMath.sin(angle) - arm_2_y, paint);			
			//top left 2nd arm
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle) - arm_1_x + correction_constant, 
							touchedY - (radius)*FloatMath.sin(angle),
							touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + correction_constant, 
							touchedY - (radius)*FloatMath.sin(angle) - arm_2_y, paint);
			//bottom right 2nd arm
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - correction_constant, 
							touchedY + (radius)*FloatMath.sin(angle),
							touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - correction_constant, 
							touchedY + (radius)*FloatMath.sin(angle) + arm_2_y, paint);
			//bottom left 2nd arm
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle) - arm_1_x + correction_constant, 
							touchedY + (radius)*FloatMath.sin(angle),
							touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + correction_constant, 
							touchedY + (radius)*FloatMath.sin(angle) + arm_2_y, paint);
			
			//top right box
			drawSelectableLocation(canvas, views[0], 
								   touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - 4*correction_constant, 
								   touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x + boxWidth - 4*correction_constant,
								   touchedY - (radius)*FloatMath.sin(angle) - arm_2_y - boxHeight + 4*correction_constant,
								   touchedY - (radius)*FloatMath.sin(angle) - arm_2_y + 4*correction_constant);
			
			image_locations[0][0] = touchedX + (radius)*FloatMath.cos(angle) 
									+ arm_1_x + arm_2_x - correction_constant - 4*correction_constant;
			image_locations[0][1] = touchedY - (radius)*FloatMath.sin(angle) - arm_2_y - boxHeight + 4*correction_constant;
			
			//top left box
			drawSelectableLocation(canvas, views[1], 
								   touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x - boxWidth + 4*correction_constant, 
								   touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + 4*correction_constant,
								   touchedY - (radius)*FloatMath.sin(angle) - arm_2_y - boxHeight + 4*correction_constant,
								   touchedY - (radius)*FloatMath.sin(angle) - arm_2_y + 4*correction_constant);
			
			image_locations[1][0] = touchedX - (radius)*FloatMath.cos(angle) 
									- arm_1_x - arm_2_x - boxWidth - 4*correction_constant;
			image_locations[1][1] = touchedY - (radius)*FloatMath.sin(angle) - arm_2_y - boxHeight + 4*correction_constant ;
			
			
			//bottom right box
			drawSelectableLocation(canvas, views[2], 
								   touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - 4*correction_constant, 
								   touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x + boxWidth - 4*correction_constant,
								   touchedY + (radius)*FloatMath.sin(angle) + arm_2_y,
								   touchedY + (radius)*FloatMath.sin(angle) + arm_2_y + boxHeight);
			
			image_locations[2][0] = touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - boxWidth/2 - correction_constant;
			image_locations[2][1] = touchedY + (radius)*FloatMath.sin(angle) + arm_2_y;
			
			//bottom left box
			drawSelectableLocation(canvas, views[3],
					   			   touchedX - (radius)*FloatMath.cos(PI/4) - arm_1_x - boxWidth/2 + correction_constant, 
					   			   touchedX - (radius)*FloatMath.cos(PI/4) - arm_1_x + boxWidth/2 + correction_constant,
					   			   touchedY + (radius)*FloatMath.sin(PI/4) + arm_2_y,
					   			   touchedY + (radius)*FloatMath.sin(PI/4) + arm_2_y + boxHeight);
			
			image_locations[3][0] = touchedX - (radius)*FloatMath.cos(PI/4) - arm_1_x - boxWidth/2 + correction_constant;
			image_locations[3][1] = touchedY + (radius)*FloatMath.sin(PI/4) + arm_2_y;
		}
		
		paint.setStyle(Paint.Style.STROKE);
		
		Paint blur = new Paint();
		blur.set(paint);
		blur.setColor( getResources().getColor( R.color.TronBlue ) );
		blur.setStrokeWidth(15f);
		blur.setMaskFilter(new BlurMaskFilter(7.5f, Blur.NORMAL));
		
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		paint.setAntiAlias(true);	
		paint.setShader( new SweepGradient( touchedX, touchedY, 
				getResources( ).getColor( R.color.TronBlue ), Color.CYAN ) );
		
		RectF outer_oval = new RectF(touchedX - radius, touchedY - radius, touchedX + radius, touchedY + radius);
		RectF inner_oval = new RectF(touchedX - radius/2, touchedY - radius/2, touchedX + radius/2, touchedY + radius/2);
		/*canvas.drawArc(oval, 180, 180, false, blur);
		canvas.drawArc(oval, 180, 180, false, paint);
	
		canvas.drawArc(oval, 0, 180, false, paint);*/
		/* Draw outer ring */
		canvas.drawArc(outer_oval, 180, 360, false, blur);
		canvas.drawArc(outer_oval, 180, 360, false, paint);
		/* Draw innner ring */
		canvas.drawArc(inner_oval, 180, 360, false, blur);
		canvas.drawArc(inner_oval, 180, 360, false, paint);
		
		/* Remove shader */
		paint.setShader( null );
		paint.setStyle(Paint.Style.FILL);
		
	}
	
	private void drawSelectableLocation(Canvas canvas, String s, float left, float right, float top, float bottom)
	{
		//create an outline for our rectangles
		canvas.drawRect(left, top, right, bottom, paint);
		paint.setColor(Color.BLACK);
		canvas.drawRect(left+5, top+5, right-5, bottom-5, paint);
		
		//reset the paint object to our tron blue
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		
		//set the text within each box
		canvas.drawText(s, left + 10, top+(boxHeight/2), paint);
	}
}