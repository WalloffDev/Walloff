package com.walloff.game;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.Window;
import android.view.WindowManager;


/**
 * Main activity where we will set up the accelerometer and the backgrounds
 * threads for managing our networking. We will then be able to call WallOffRender
 * which is used to actually render and play the game.
 *
 */

public class WallOffGameActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WallOffEngine.player_count = 3; // number of players in the lobby
        WallOffEngine.obsticles_init_pattern = 213;
        WallOffEngine.obsticles_move_pattern = 167;
        WallOffEngine.setGameConstants("default", "normal", true, true, 6, true);
        
        WallOffEngine.accelerometer = new Accelerometer( (SensorManager)getSystemService(Context.SENSOR_SERVICE) );
    	
        
        GLSurfaceView view = new GLSurfaceView(this);
        view.setRenderer(new WallOffRenderer(this, 0)); //the 0 is the player id (position in lobby)
        setContentView(view);
    }
}
