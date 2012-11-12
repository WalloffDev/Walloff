package com.walloff.android;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
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
	private int view_itter[] = new int[4];
	private int curr_index = 0;
	private float image_locations[][] = new float[4][2];
	
	/* keep track of where the user pressed on the screen */
	private float touchedX = 0;
	private float touchedY = 0;
	
	/* Constants used for layout math */
	private float radius = 65f;
	private float PI = (float)Math.PI;
	private float boxHeight = ( Constants.window_size_y/5 );
	private float boxWidth = ( Constants.window_size_x/5 );
	private float default_arm_x_size = ( Constants.window_size_x/5 );
	private final float correction_constant = 4.0f;
	
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
		
		int string_itter = 0;
		for( int i = 0; i < 5; i++ ) {
			if( i != curr_index ) {
				views[ string_itter ] = ( String )this.viewFlipper.getChildAt( i ).getTag( );
				Log.v( ( String )this.viewFlipper.getChildAt( i ).getTag( ), String.valueOf(i)  );
				view_itter[ string_itter ] = i;
				string_itter++;
			}
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
					curr_index = view_itter[i];
					viewFlipper.setDisplayedChild( view_itter[i] );
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
		
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		paint.setAntiAlias(true);
		
		canvas.drawCircle(touchedX, touchedY, radius, paint);
		
		paint.setColor(Color.BLACK);
		canvas.drawCircle(touchedX, touchedY, radius-5.0f, paint);
		
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		canvas.drawCircle(touchedX, touchedY, 20.0f, paint);
		
		//set our text size and stroke sizethis.initialize( this.curr_index );
		paint.setTextSize(24f);
		paint.setStrokeWidth(8f);
		
		//we are in the middle of the screen
		if ( touchedX <= ( 3 * Constants.window_size_x )/4  && touchedX >= Constants.window_size_x/4 
			&& touchedY <= (2 * Constants.window_size_y	)/3  && touchedY >= Constants.window_size_y/3)
		{			
			float armX;
			float armY = 0;
			
			if( touchedX + radius + ( boxWidth/2 ) + default_arm_x_size >= Constants.window_size_x )
			{
				armX =  Constants.window_size_x - ( touchedX + radius + ( boxWidth/2 ) );
			}
			else if( touchedX - radius - ( boxWidth/2 ) - default_arm_x_size <= 0 )
			{
				armX = touchedX - radius - ( boxWidth/2 );
			}
			else
			{
				armX = Constants.window_size_x/5.5f;
			}
			
			//top right line
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(PI/4) - correction_constant,
							touchedY - (radius)*FloatMath.sin(PI/4),
							touchedX + (radius)*FloatMath.cos(PI/4) + armX,
						    touchedY - (radius)*FloatMath.sin(PI/4), paint);
			//top left line
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(PI/4) + correction_constant,
							touchedY - (radius)*FloatMath.sin(PI/4),
							touchedX - (radius)*FloatMath.cos(PI/4) - armX,
							touchedY - (radius)*FloatMath.sin(PI/4), paint);
			//bottom right line
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(PI/4) - correction_constant, 
							touchedY + (radius)*FloatMath.sin(PI/4),
							touchedX + (radius)*FloatMath.cos(PI/4) + armX, 
							touchedY + (radius)*FloatMath.sin(PI/4), paint);
			//bottom left line
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(PI/4) + correction_constant,
							touchedY + (radius)*FloatMath.sin(PI/4),
							touchedX - (radius)*FloatMath.cos(PI/4) - armX,
							touchedY + (radius)*FloatMath.sin(PI/4), paint);
			
			//we are in the upper half of the screen for where we touched
			if ( Constants.window_size_y - touchedY >= (Constants.window_size_y/2) )
			{
				/* check the y height we can use */
				armY = touchedY - boxHeight - radius * FloatMath.sin(PI/4);
			}
			//we are in the lower half of the screen
			else if ( Constants.window_size_y - touchedY < (Constants.window_size_y/2) )
			{
				armY = Constants.window_size_y - touchedY - boxHeight - radius * FloatMath.sin(PI/4);
			}
			
			//top right
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(PI/4) + armX - correction_constant, 
							touchedY - (radius)*FloatMath.sin(PI/4),
							touchedX + (radius)*FloatMath.cos(PI/4) + armX - correction_constant, 
							touchedY - (radius)*FloatMath.sin(PI/4) - armY, paint);			
			//top left
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(PI/4) - armX + correction_constant, 
							touchedY - (radius)*FloatMath.sin(PI/4),
							touchedX - (radius)*FloatMath.cos(PI/4) - armX + correction_constant, 
							touchedY - (radius)*FloatMath.sin(PI/4) - armY, paint);
			//bottom right
			canvas.drawLine(touchedX + (radius)*FloatMath.cos(PI/4) + armX - correction_constant, 
							touchedY + (radius)*FloatMath.sin(PI/4),
							touchedX + (radius)*FloatMath.cos(PI/4) + armX - correction_constant, 
							touchedY + (radius)*FloatMath.sin(PI/4) + armY, paint);
			//bottom left
			canvas.drawLine(touchedX - (radius)*FloatMath.cos(PI/4) - armX + correction_constant, 
							touchedY + (radius)*FloatMath.sin(PI/4),
							touchedX - (radius)*FloatMath.cos(PI/4) - armX + correction_constant, 
							touchedY + (radius)*FloatMath.sin(PI/4) + armY, paint);
			
			//top right box
			drawSelectableLocation(canvas, views[0], 
								   touchedX + (radius)*FloatMath.cos(PI/4) + armX - boxWidth/2 - correction_constant, 
								   touchedX + (radius)*FloatMath.cos(PI/4) + armX + boxWidth/2 - correction_constant,
								   touchedY - (radius)*FloatMath.sin(PI/4) - armY - boxHeight,
								   touchedY - (radius)*FloatMath.sin(PI/4) - armY);
			
			image_locations[0][0] = touchedX + (radius)*FloatMath.cos(PI/4) + armX - boxWidth/2 - correction_constant;
			image_locations[0][1] = touchedY - (radius)*FloatMath.sin(PI/4) - armY - boxHeight;
			
			//top left box
			drawSelectableLocation(canvas, views[1], 
								   touchedX - (radius)*FloatMath.cos(PI/4) - armX - boxWidth/2 + correction_constant, 
								   touchedX - (radius)*FloatMath.cos(PI/4) - armX + boxWidth/2 + correction_constant,
								   touchedY - (radius)*FloatMath.sin(PI/4) - armY- boxHeight,
								   touchedY - (radius)*FloatMath.sin(PI/4) - armY);
			
			image_locations[1][0] = touchedX - (radius)*FloatMath.cos(PI/4) - armX - boxWidth/2 + correction_constant;
			image_locations[1][1] = touchedY - (radius)*FloatMath.sin(PI/4) - armY - boxHeight;
			
			
			//bottom right box
			drawSelectableLocation(canvas, views[2],
								   touchedX + (radius)*FloatMath.cos(PI/4) + armX - boxWidth/2 - correction_constant, 
								   touchedX + (radius)*FloatMath.cos(PI/4) + armX + boxWidth/2 - correction_constant,
								   touchedY + (radius)*FloatMath.sin(PI/4) + armY,
								   touchedY + (radius)*FloatMath.sin(PI/4) + armY + boxHeight);
			
			image_locations[2][0] = touchedX + (radius)*FloatMath.cos(PI/4) + armX - boxWidth/2 - correction_constant;
			image_locations[2][1] = touchedY + (radius)*FloatMath.sin(PI/4) + armY;
			
			//bottom left box
			drawSelectableLocation(canvas, views[3],
					   			   touchedX - (radius)*FloatMath.cos(PI/4) - armX - boxWidth/2 + correction_constant, 
					   			   touchedX - (radius)*FloatMath.cos(PI/4) - armX + boxWidth/2 + correction_constant,
					   			   touchedY + (radius)*FloatMath.sin(PI/4) + armY,
					   			   touchedY + (radius)*FloatMath.sin(PI/4) + armY + boxHeight);
			
			image_locations[3][0] = touchedX - (radius)*FloatMath.cos(PI/4) - armX - boxWidth/2 + correction_constant;
			image_locations[3][1] = touchedY + (radius)*FloatMath.sin(PI/4) + armY;
		}
	}
	
	private void drawSelectableLocation(Canvas canvas, String s, float left, float right, float top, float bottom)
	{
		//create an outline for our rectangles
		canvas.drawRect(left, top, right, bottom, paint);
		paint.setColor(Color.BLACK);
		canvas.drawRect(left+3, top+3, right-3, bottom-3, paint);
		
		//reset the paint object to our tron blue
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		
		//set the text within each box
		canvas.drawText(s, left + 10, top+(boxHeight/2), paint);
	}
}