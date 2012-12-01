package com.walloff.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ViewFlipper;

public class HUDOptions extends View {	

	private UIEltsHelper ui_helper = null;
	
	/* Used to keep track of views */
	private ViewFlipper viewFlipper = null;
	private String views[]= new String[4];
	private int view_selector[] = new int[4];
	private int curr_index = 0;
	private float image_locations[][] = new float[4][2];
	
	/* Keep track of where the user pressed on the screen */
	private float touchedX = 0;
	private float touchedY = 0;
	private int corner_index[] = new int[2];
	
	/* Constants used for layout math */
	private float radius = Constants.window_size_x/15;
	private float angle = (float)Math.PI/6;
	private float boxHeight = ( Constants.window_size_y/7 );
	private float boxWidth = ( Constants.window_size_x/4.8f );
	private final float correction_constant = 2.0f;
	
	/* Paint object used for drawing */
	private Paint paint;
	private Paint blur;
	private int colors[] = {
			getResources().getColor( R.color.TronRed ),
			getResources().getColor(R.color.TronGreen),
			getResources().getColor(R.color.TronYellow),
			getResources().getColor(R.color.TronPurple)
	};
	
	/* Container for our canvas */
	Dialog dialog = null;
	
	/* Constructor(s) */
	public HUDOptions( Context context ) {
		super( context );
	}
	public HUDOptions( Context context, ViewFlipper vf, Dialog dia ) {
		super(context);

		this.ui_helper = new UIEltsHelper( context, vf );
		
		//ViewFlipper needs these
		this.viewFlipper = vf;
		
		/* create our new paint selector */
		paint = new Paint();
		blur = new Paint();
		
		//obtain the dialog passed in
		this.dialog = dia;
		this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//set the dialog dismiss to remove the in_hud flag
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			public void onDismiss(DialogInterface dialog) {
				Constants.in_HUD = false;
			}
		});
		
		
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			public void onShow(DialogInterface dialog) {
				Constants.in_HUD = true;
				
			}
		});
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
	
	/* Check the location of the image_location. The image location takes the left and bottom locations of the boxes. */
	@Override	
	public boolean onTouchEvent(MotionEvent me) {
		//get the location of where the person selected
		float e_x = me.getX();
		float e_y = me.getY();
		
		int selection = me.getActionMasked();
		
		switch(selection)
		{
			case MotionEvent.ACTION_DOWN:
				//check if the user pressed near one of the 4 menu selections
				for( int i = 0; i < 4; i++ )
				{
					if( ( e_x >= image_locations[i][0] && e_x <= image_locations[i][0] + boxWidth ) &&
							( e_y <= image_locations[i][1] && e_y >= image_locations[i][1] - boxHeight ) ) 
					{
						curr_index = view_selector[i];
						viewFlipper.setDisplayedChild( view_selector[i] );
						dialog.dismiss();
						this.ui_helper.registerUIElts( i );
						return true;
					}
				}
				
				//see if the user pressed the circle icon to close the window
				if( (e_x >= touchedX - radius) && (e_x <= touchedX + radius) && 
					(e_y >= touchedY - radius) && (e_y <= touchedY + radius))
				{
					dialog.dismiss();
					return true;
				}
				
				break;
				
			default:
				break;
		}

		return false;
	} 
	
	/* Draw the main circle where a player has pressed */
	private void drawPressCircle( Canvas canvas )
	{
		paint.setStyle(Paint.Style.STROKE);
		
		blur.set(paint);
		blur.setColor( getResources().getColor( R.color.TronBlue ) );
		blur.setStrokeWidth(15f);
		blur.setMaskFilter(new BlurMaskFilter(7.5f, Blur.NORMAL));
		
		paint.setColor( getResources().getColor( R.color.TronBlue ));
		
		RectF outer_oval = new RectF(touchedX - radius, touchedY - radius, touchedX + radius, touchedY + radius);
		RectF inner_oval = new RectF(touchedX - radius/2, touchedY - radius/2, touchedX + radius/2, touchedY + radius/2);
		
		/* Draw outer ring */
		canvas.drawArc(outer_oval, 180, 360, false, blur);
		canvas.drawArc(outer_oval, 180, 360, false, paint);
		/* Draw innner ring */
		canvas.drawArc(inner_oval, 180, 360, false, blur);
		canvas.drawArc(inner_oval, 180, 360, false, paint);
	}
	
	/* Draw the main menu center selection */
	private void drawCenterSelection( Canvas canvas )
	{
		float arm_1_x = Constants.window_size_x/7;
		float arm_2_y = Constants.window_size_y/9;
		float arm_2_x = Constants.window_size_x/10;

		float i = .1f;
		while (true)
		{
			// we have touched near the right of the screen or the left of the screen
			if(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x + boxWidth > Constants.window_size_x || 
				touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x - boxWidth < 0)
			{
				arm_1_x = Constants.window_size_x/(7 + i);
				arm_2_x = Constants.window_size_x/(10 + i);
				
				//we are in the upper half of the screen for where we touched
				if ( touchedY - (radius)*FloatMath.sin(angle) - arm_2_y - boxHeight + 4*correction_constant < 0 )
				{
					arm_2_y = Constants.window_size_y/(9 + i);
				}
				//we are in the lower half of the screen
				else if ( touchedY + (radius)*FloatMath.sin(angle) + arm_2_y + boxHeight - 4*correction_constant > Constants.window_size_y )
				{
					arm_2_y = Constants.window_size_y/(9 + i);
				}
				
				i += .1f;
			}
			else
			{
				break;
			}
		}
		
		/**********bottom************************* TOP RIGHT IMAGE ********************************************************/
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor( colors[0] );
		blur.set(paint);
		blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
		//top right line
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle),
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x,
					    touchedY - (radius)*FloatMath.sin(angle), blur);
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle),
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x,
					    touchedY - (radius)*FloatMath.sin(angle), paint);		
		//top right 2nd arm
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle) - arm_2_y, blur);	
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle) - arm_2_y, paint);		
		//top right box
		drawSelectableLocation(canvas, views[0], 
							   touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - 4*correction_constant,
							   touchedY - (radius)*FloatMath.sin(angle) - arm_2_y + 4*correction_constant);
		
		image_locations[0][0] = touchedX + (radius)*FloatMath.cos(angle) 
								+ arm_1_x + arm_2_x - correction_constant - 4*correction_constant;
		image_locations[0][1] = touchedY - (radius)*FloatMath.sin(angle) - arm_2_y + 4*correction_constant;
		
		/*********************************** TOP LEFT IMAGE ********************************************************/
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor( colors[1] );
		blur.set(paint);
		blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
		//top left line
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle),
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x,
						touchedY - (radius)*FloatMath.sin(angle), blur);	
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle),
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x,
						touchedY - (radius)*FloatMath.sin(angle), paint);		
		//top left 2nd arm
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle) - arm_1_x + correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle) - arm_2_y, blur);
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle) - arm_1_x + correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + correction_constant, 
						touchedY - (radius)*FloatMath.sin(angle) - arm_2_y, paint);
		//top left box
		drawSelectableLocation(canvas, views[1], 
							   touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x - boxWidth + 4*correction_constant,
							   touchedY - (radius)*FloatMath.sin(angle) - arm_2_y + 4*correction_constant);
		
		image_locations[1][0] = touchedX - (radius)*FloatMath.cos(angle) 
								- arm_1_x - arm_2_x - boxWidth - 4*correction_constant;
		image_locations[1][1] = touchedY - (radius)*FloatMath.sin(angle) - arm_2_y + 4*correction_constant;
				
		/*********************************** BOTTOM RIGHT IMAGE ********************************************************/
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor( colors[2] );
		blur.set(paint);
		blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
		//bottom right line
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle), 
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x, 
						touchedY + (radius)*FloatMath.sin(angle), blur);
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle), 
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x, 
						touchedY + (radius)*FloatMath.sin(angle), paint);
		//bottom right 2nd arm
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle) + arm_2_y, blur);
		canvas.drawLine(touchedX + (radius)*FloatMath.cos(angle) + arm_1_x - correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle) + arm_2_y, paint);
		//bottom right box
		drawSelectableLocation(canvas, views[2], 
							   touchedX + (radius)*FloatMath.cos(angle) + arm_1_x + arm_2_x - 4*correction_constant,
							   touchedY + (radius)*FloatMath.sin(angle) + arm_2_y + boxHeight - 4*correction_constant);
		
		image_locations[2][0] = touchedX + (radius)*FloatMath.cos(angle) 
								+ arm_1_x + arm_2_x - correction_constant - 4*correction_constant;
		image_locations[2][1] = touchedY + (radius)*FloatMath.sin(angle) + arm_2_y + boxHeight - 4*correction_constant;
		
		/*********************************** BOTTOM LEFT IMAGE ********************************************************/
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor( colors[3] );
		blur.set(paint);
		blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
		//bottom left line
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle),
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x,
						touchedY + (radius)*FloatMath.sin(angle), blur);
		
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle),
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x,
						touchedY + (radius)*FloatMath.sin(angle), paint);
		//bottom left 2nd arm
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle) - arm_1_x + correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle) + arm_2_y, blur);		
		
		canvas.drawLine(touchedX - (radius)*FloatMath.cos(angle) - arm_1_x + correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle),
						touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x + correction_constant, 
						touchedY + (radius)*FloatMath.sin(angle) + arm_2_y, paint);		
		//bottom left box
		drawSelectableLocation(canvas, views[3],
				   			   touchedX - (radius)*FloatMath.cos(angle) - arm_1_x - arm_2_x - boxWidth + 4*correction_constant,
				   			   touchedY + (radius)*FloatMath.sin(angle) + arm_2_y + boxHeight - 4*correction_constant);
		
		image_locations[3][0] = touchedX - (radius)*FloatMath.cos(angle) 
								- arm_1_x - arm_2_x - boxWidth - 4*correction_constant;
		image_locations[3][1] = touchedY + (radius)*FloatMath.sin(angle) + arm_2_y + boxHeight - 4*correction_constant;
	}
	
	/* Draw function for corner presses */
	private void drawCornerSelection( Canvas canvas ) {

		float spacer_x = ( ( 2 * Constants.window_size_x ) / 3 - views.length * boxWidth ) / ( views.length + 1 ) ;	// Pads the start and end x values the boxes will accompany
		spacer_x = Math.abs( spacer_x );
		float threshold = 0;	// Pivot x coordinate for connections
		float left, top;
		
		/* Setup params */
		// Controls spacing between boxes vertically and is proportional to the this.boxHeight
		float spacer_y = ( ( Constants.window_size_y - ( Math.abs( ( ( Constants.window_size_y * corner_index[ 1 ] ) 
				         - this.touchedY ) ) + this.radius ) ) - this.views.length * this.boxHeight) / (this.views.length + 1);
		left = ( ( corner_index[ 0 ] * ( Constants.window_size_x / 3 ) ) - ( boxWidth / 2 ) );
		
		if( corner_index[ 1 ] == 0 )
			top = ( ( this.touchedY + this.radius + spacer_y ) ) - ( this.boxHeight + ( spacer_y ) );
		else
			top = -this.boxHeight;
		
		for( int i = 0; i < this.views.length; i++ ) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor( colors[i] );
			blur.set(paint);
			blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
			
			left += ( this.boxWidth / 2 ) + spacer_x;
			top += ( this.boxHeight + spacer_y );
			
			drawSelectableLocation( canvas, views[ i ], left, ( top + this.boxHeight ) );
			
			/* Register new input locations */
			image_locations[ i ][ 0 ] = left;
			image_locations[ i ][ 1 ] = top + this.boxHeight;
		}
		if( corner_index[ 0 ] == 1 )
			threshold = ( Constants.window_size_x / 3 );
		else
			threshold = 2 * ( Constants.window_size_x / 3 );
		
		/* Draw connections */
		for( int i = 0; i < this.views.length; i++ ) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor( colors[i] );
			blur.set(paint);
			blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
			
			if( corner_index[ 0 ] == 1 )
				drawConnection( canvas, paint, threshold, image_locations[ i ][ 0 ], image_locations[ i ][ 1 ] + ( this.boxHeight / 2 ) );
			else
				drawConnection( canvas, paint, threshold, image_locations[ i ][ 0 ] + boxWidth, image_locations[ i ][ 1 ] + ( this.boxHeight / 2 ) );
		}	
	}
	
	/* Draw the main menu side(s) selections */
	/* side's will be 0 for top, 1 for bottom, 2 for left and 3 for right */
	private void drawSideSelection( Canvas canvas, int side )
	{
		// Controls spacing between boxes vertically and is proportional to the this.boxHeight
		float space_x = 0;
		float padding_x = 0;
		float space_y = 0;
		float padding_y = 0;
		float arm_1_x_dist = 0;
		float threshold = 0;
		float extra_length_x = 0;
		float extra_length_y = 0;

		
		//determine how much space we have if we touched the right or left side of the screen
		if( side == 2 || side == 3 )
		{
			padding_y = ( Constants.window_size_y - views.length * boxHeight ) / ( views.length + 1 );
			space_y = padding_y;
			if( side == 2 ) //right side of screen extend to 3/4
				arm_1_x_dist = Constants.window_size_x / 3;
			else //left side of screen threshold is 1/4
				arm_1_x_dist = (2 * Constants.window_size_x ) / 3;
		}
		//determine the screen width if touched the top or bottom of the screen
		if( side == 0 || side == 1)
		{
			padding_x = ( Constants.window_size_x - ( views.length * boxWidth ) ) / ( views.length + 1 );
			space_x = padding_x;
		}
		
		for( int i=0; i < this.views.length; i++ )
		{
			if ( side == 2 || side == 3) //draw boxes along the y
			{
				//reset the extra_length and threshold back to their defaults. (the top and bottom lines use these values )
				if( i == 0 || i == 3)
				{
					extra_length_x = 0;
					threshold = arm_1_x_dist;
					
					if( side == 2)
						image_locations[i][0] = arm_1_x_dist + Constants.window_size_x/6;
					else
						image_locations[i][0] = arm_1_x_dist - Constants.window_size_x/6 - boxWidth;
				}
				//add extra distance for the the middle lines
				else
				{
					if( side == 2 )
					{
						extra_length_x = boxWidth / 2;
						image_locations[i][0] = arm_1_x_dist + Constants.window_size_x/4 + boxWidth/2;
						threshold = arm_1_x_dist + extra_length_x;
						
					}
					else
					{
						extra_length_x = boxWidth / 2;
						image_locations[i][0] = arm_1_x_dist - Constants.window_size_x/4 - boxWidth/2 - boxWidth;
						threshold = arm_1_x_dist - extra_length_x;
					}
				}
				
				//set the location of the box's height
				image_locations[i][1] = space_y + boxHeight;
				
				if( side == 2 )
				{
					paint.setStyle(Paint.Style.STROKE);
					paint.setColor( colors[i] );
					blur.set(paint);
					blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
					
					drawConnection_sides_LR(canvas, paint, threshold, image_locations[i][0], image_locations[i][1] - boxHeight/2);
					drawSelectableLocation(canvas, views[i], image_locations[i][0], image_locations[i][1]);
				}
				else
				{
					paint.setStyle(Paint.Style.STROKE);
					paint.setColor( colors[i] );
					blur.set(paint);
					blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
					
					drawConnection_sides_LR(canvas, paint, threshold, image_locations[i][0] + boxWidth, image_locations[i][1] - boxHeight/2);
					drawSelectableLocation(canvas, views[i], image_locations[i][0], image_locations[i][1]);	
				}
				
				//update the y location of each box line
				space_y += boxHeight + padding_y;
			}
			else //draw boxes along the x
			{
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor( colors[i] );
				blur.set(paint);
				blur.setMaskFilter(new BlurMaskFilter(12f, Blur.NORMAL));
				
				//we are on the top of the screen
				if ( side == 0)
				{
					extra_length_y = ( 3 * ( boxHeight / 4 ) );
					threshold = ( Constants.window_size_y / 3 );
				}
				//we have touched the bottom of the screen
				else
				{
					extra_length_y = -( 3 * ( boxHeight / 4 ) );
					threshold = (2 * Constants.window_size_y) / 3;
				}
				
				 //we are drawing the left and right boxes
				if( i % 3 == 0 ) {
					image_locations[ i ][ 0 ] = ( space_x + ( i * ( boxWidth + space_x ) ) );
					
					if( this.touchedY <= ( Constants.window_size_y / 3 ) ) //top side of the screen 
					{
						image_locations[ i ][ 1 ] = ( Constants.window_size_y / 3 ) + ( 2 * boxHeight );
					}
					else  //bottom side of the screen
					{
						image_locations[ i ][ 1 ] = Constants.window_size_y - ( ( Constants.window_size_y / 3 ) + boxHeight );
					}
					drawConnection_sides_TB( canvas, paint, threshold, image_locations[ i ][ 0 ] + ( boxWidth / 2 ), image_locations[ i ][ 1 ] );
				}
				//we are drawing the 2 middle boxes
				else { 
					image_locations[ i ][ 0 ] = ( space_x + ( i * ( boxWidth + space_x ) ) );
					
					if( this.touchedY < ( Constants.window_size_y / 3 ) ) //top of the screen
					{
						image_locations[ i ][ 1 ] = ( Constants.window_size_y / 3 ) + extra_length_y + ( 2 * boxHeight );
					}					
					else //bottom of the screen
					{
						image_locations[ i ][ 1 ] = ( 2 * Constants.window_size_y / 3 ) + extra_length_y - boxHeight;
					}
					drawConnection_sides_TB(canvas, paint, threshold + extra_length_y, image_locations[i][0] + ( boxWidth / 2 ), image_locations[i][1]);
				}
				drawSelectableLocation( canvas, this.views[ i ], image_locations[ i ][ 0 ], image_locations[ i ][ 1 ] );
			}
		}
	}
	
	private void drawConnection_sides_LR(Canvas canvas, Paint paint, float threshold, float final_x, float final_y) {		
		
		Path path = new Path();
		
		//obtain the theta for the edge coming out of where we pressed
		float theta = (float)Math.acos( (threshold - touchedX)/
				 Math.sqrt( Math.pow( touchedX - threshold, 2) + Math.pow(touchedY - final_y, 2) ) );

		if (final_y >= Constants.window_size_y / 2)
			theta = (float) (2*Math.PI - theta);
		
		//draw paths that auto correct for line spacing
		path.moveTo(this.touchedX  + radius*FloatMath.cos(theta), this.touchedY - radius*FloatMath.sin(theta));
		path.lineTo(threshold, final_y);
		path.lineTo(final_x, final_y);
		canvas.drawPath(path, blur);
		
		path.moveTo(this.touchedX  + radius*FloatMath.cos(theta), this.touchedY - radius*FloatMath.sin(theta));
		path.lineTo(threshold, final_y);
		path.lineTo(final_x, final_y);
		canvas.drawPath(path, paint);
	}

	private void drawConnection_sides_TB( Canvas canvas, Paint paint, float threshold, float final_x, float final_y ) {
		
		Path path = new Path();
		
		//obtain the theta for the edge coming out of where we pressed
		float theta = (float)Math.acos( (threshold - touchedY)/
				 Math.sqrt( Math.pow( touchedY - threshold, 2) + Math.pow(touchedX - final_x, 2) ) );

		if ( (final_x >= Constants.window_size_x / 2) )
			theta = (float) (2*Math.PI - theta);

		//draw paths that auto correct for line spacing
		path.moveTo(this.touchedX - radius*FloatMath.sin(theta), this.touchedY + radius*FloatMath.cos(theta));
		path.lineTo(final_x, threshold);
		path.lineTo(final_x, final_y);
		canvas.drawPath(path, blur);
		
		path.moveTo(this.touchedX - radius*FloatMath.sin(theta), this.touchedY + radius*FloatMath.cos(theta));
		path.lineTo(final_x, threshold);
		path.lineTo(final_x, final_y);
		canvas.drawPath(path, paint);
	}
	
	private void drawConnection( Canvas canvas, Paint paint, float threshold, float dest_x, float dest_y ) {
		dest_y = dest_y - boxHeight;
		
		float theta = (float)Math.acos( (touchedX - threshold)/
				 Math.sqrt( Math.pow( touchedX - threshold, 2) + Math.pow(touchedY - dest_y, 2) ) );
		
		Path path = new Path();
		
		if( corner_index[1] == 1 )
		{
			theta = (float)Math.PI - theta;
			
			//draw paths that auto correct for line spacing
			path.moveTo(this.touchedX + radius*FloatMath.cos(theta), this.touchedY - radius*FloatMath.sin(theta));
			path.lineTo(threshold, dest_y);
			path.lineTo(dest_x, dest_y);
			canvas.drawPath(path, blur);
			
			path.moveTo(this.touchedX + radius*FloatMath.cos(theta), this.touchedY - radius*FloatMath.sin(theta));
			path.lineTo(threshold, dest_y);
			path.lineTo(dest_x, dest_y);
			canvas.drawPath(path, paint);	
		}
		else
		{
			//draw paths that auto correct for line spacing
			path.moveTo(this.touchedX - radius*FloatMath.cos(theta), this.touchedY + radius*FloatMath.sin(theta));
			path.lineTo(threshold, dest_y);
			path.lineTo(dest_x, dest_y);
			canvas.drawPath(path, blur);
			
			path.moveTo(this.touchedX - radius*FloatMath.cos(theta), this.touchedY + radius*FloatMath.sin(theta));
			path.lineTo(threshold, dest_y);
			path.lineTo(dest_x, dest_y);
			canvas.drawPath(path, paint);
		}

	}
	
	/* automatically called when this view is shown */
	@Override
	protected void onDraw(Canvas canvas){		
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		
		// Set our text size and stroke sizethis.initialize( this.curr_index );
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(8f);
		
		// We are in the middle of the screen
		if ( touchedX <= ( 2 * Constants.window_size_x)/3  && touchedX >= Constants.window_size_x/3 
				&& touchedY <= (2 * Constants.window_size_y	)/3  && touchedY >= Constants.window_size_y/3)
		{
			drawCenterSelection(canvas);
		}
		
		// We are in one of the corner regions
		else if( ( touchedX <= ( Constants.window_size_x / 3 ) ) && ( touchedY <= ( Constants.window_size_y / 3 ) ) )// top left-hand corner
		{
			corner_index[ 0 ] = 1;
			corner_index[ 1 ] = 0;
			drawCornerSelection( canvas );
		}
		else if( ( touchedX <= ( Constants.window_size_x / 3 ) ) && ( touchedY >= ( 2 * ( Constants.window_size_y / 3 ) ) ) )// bottom left-hand corner
		{
			corner_index[ 0 ] = 1;
			paint.setStyle(Paint.Style.STROKE);		corner_index[ 1 ] = 1;
			drawCornerSelection( canvas );
		}
		else if( ( touchedX >= ( 2 * ( Constants.window_size_x / 3 ) ) ) && ( touchedY <= ( Constants.window_size_y / 3 ) ) )// top right-hand corner
		{
			corner_index[ 0 ] = 0;
			corner_index[ 1 ] = 0;
			drawCornerSelection( canvas );
		}
		else if( ( touchedX >= ( 2 * ( Constants.window_size_x / 3 ) ) ) && ( touchedY >= ( 2 * ( Constants.window_size_y / 3 ) ) ) )// bottom right-hand corner	
		{
			corner_index[ 0 ] = 0;
			corner_index[ 1 ] = 1;
			drawCornerSelection( canvas );
		}
		
		/* We are in one of the side regions */
		else if( ( ( touchedX > ( Constants.window_size_x / 3 ) ) && ( touchedX < ( 2 * ( Constants.window_size_x / 3 ) ) ) ) //touched the top
				&& ( ( touchedY < ( Constants.window_size_y / 3 )) ) ) 
		{ 
			drawSideSelection(canvas, 0);
		}
		else if( ( ( touchedX > ( Constants.window_size_x / 3 ) ) && ( touchedX < ( 2 * ( Constants.window_size_x / 3 ) ) ) ) //touched the bottom
				&& ( ( touchedY > ( Constants.window_size_y / 3 )) ) ) 
		{ 
			drawSideSelection(canvas, 1);
		}
		else if( touchedX < ( Constants.window_size_x/3 ) ) // touched the left side of the screen
		{
			drawSideSelection(canvas, 2);
		}
		else //the only thing left to press is the right side of the screen
		{
			drawSideSelection(canvas, 3);
		}
		
		drawPressCircle( canvas );
	}
	
	/* Draw the clickable box locations */
	private void drawSelectableLocation( Canvas canvas, String s, float left, float bottom )
	{			
		//save the current paint color, this will be needed for the text color
		Paint text_paint = new Paint();
		text_paint.setColor( paint.getColor() );
		text_paint.setTextSize(24f);
		blur.setMaskFilter(new BlurMaskFilter(7.5f, Blur.NORMAL));
				
		//draw the outer edge of the rectangle blue
		RectF rect = new RectF(left, ( bottom - boxHeight ), ( left + boxWidth ), bottom);		
		canvas.drawRoundRect(rect, 5, 5, blur);
		
		//set the style to fill else the text will get messed up.
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRoundRect(rect, 5, 5, paint);
		
		//draw the inner black portion of our rectangle
		paint.setColor( getResources().getColor( R.color.Black_Transparent ) );	
		RectF rect_2 = new RectF(left + 5, ( bottom - boxHeight ) + 5, ( left + boxWidth ) - 5, bottom - 5);
		canvas.drawRoundRect(rect_2, 5, 5, paint);
		
		//set the text within each box
		canvas.drawText(s, left + 10, ( bottom - boxHeight/2 ) + 5, text_paint);
	}
	
	//@Override
	protected void OnBackPress()
	{
		Constants.in_HUD = false;
	}

}