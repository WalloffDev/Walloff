package com.walloff.game;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * This class will be used to set up the games accelerometer.
 * 
 */
public class Accelerometer implements SensorEventListener {
	/* Coordinate handlers */
	private float x_theta;
	private float y_theta;
	private float z_theta;
	private SensorManager manager;

	/* Basic constructor for the accelerometer */
	public Accelerometer(SensorManager mangr){
		//listener for the SensorManager and accelerometer
		manager = mangr;
		y_theta = 0;
		x_theta = 0;
		z_theta = 0;
		
		//add a listener for the accelerometer
		//make sure there is an accelerometer to play the game
		if(manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
			manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		else
			System.exit(0);
	}
	
	/* return the x coordinate */
	public float getXTheta(){ return x_theta; }
	
	/* return the y coordinate */
	public float getYTheta(){ return y_theta; }
 	
	/* return the z coordinate */
	public float getZTheta(){ return z_theta; }
		
	/* unimplemented function */
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	/* Detect if a player has titled the screen and adjust turn angle accordingly */
	public void onSensorChanged(SensorEvent event) {
		//return if the sensor is not the accelerometer
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			//make the player move in a straight line
			if(event.values[1] > -2.5 && event.values[1] < 2.5) y_theta = 0;
			
			//have the player turn left
			else if(event.values[1] > 5) y_theta = WallOffEngine.PI/32;
			else if(event.values[1] > 2.5) y_theta = WallOffEngine.PI/64;			

			//have the player turn right
			else if(event.values[1] < -5) y_theta = -WallOffEngine.PI/32;
			else if(event.values[1] < -2.5) y_theta = -WallOffEngine.PI/64;
			
		}
		else 
			return;
		
	}

}
