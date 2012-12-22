package com.walloff.game;


/**
 * Class is used to keep track of any constants that we may need and any general functions such as
 * exit.
 * 
 */
public class WallOffEngine {
	/* variables used for game timing */
	public static final int GAME_THREAD_FPS_SLEEP = (1000/21);
	
	/* Accelerometer properties */
	public static Accelerometer accelerometer; 
	
	/* math properties */
	public static final float PI = 3.141592654f;	
	public static final long NANOSECOND = 1000000000;
	public static final int MAX_NUMBER_PLAYERS = 4;
	public static int player_count;
	public static final float player_speed = .3f;
	
	/* list of available map names */
	public static final String map_Original = "default";
	
	/* map properties */
	public static String map_name;
	public static float map_size;
	public static float map_initial_size;
	public static boolean map_shrinkable;
	public static final float map_size_constant_large = 85f;
	public static final float map_size_constant_normal = 75f;
	public static final float map_size_constant_small = 65f;
	public static final String map_small = "small";
	public static final String map_normal = "normal";
	public static final String map_large = "large";
	public static final int map_shrink_ticks = GAME_THREAD_FPS_SLEEP * 10;
	public static final float map_shrink_length = 5f; //the total length we wish to shrink the map after one shrink iteration
	public static final int map_shrink_count = 15; //used to count how many iterations our wall should project out
	public static final float map_shrink_ammount = map_shrink_length/map_shrink_count; //amount to shrink our map by based on shrink_count
	public static final int map_max_shrink_times = 5; //max number of times the map will shrink
	
	/* random object properties */
	public static boolean obsticles;
	public static int obsticles_number;
	public static boolean obsticles_moving;
	public static int obsticles_move_pattern;
	public static String obsticles_move_pattern_string = "MOVING OBS PATTERN";
	public static int obsticles_init_pattern;
	public static String obsticles_init_pattern_string = "OBS INIT PATTERN";
	
	/* set the constants of our map that the host created */
	public static void setGameConstants(String mapName, String mapSize, boolean shrink, boolean obs, int obsNum, boolean moveObs)
	{
		WallOffEngine.map_name = mapName;
		
		if ( mapSize.equals(map_small) )
			WallOffEngine.map_size = map_size_constant_small;
		else if ( mapSize.equals( map_normal )  )
			WallOffEngine.map_size = map_size_constant_normal;
		else
			WallOffEngine.map_size = map_size_constant_large;
		
		WallOffEngine.map_initial_size = map_size;
		WallOffEngine.map_shrinkable = shrink;
		WallOffEngine.obsticles = obs;
		WallOffEngine.obsticles_number = obsNum;
		WallOffEngine.obsticles_moving = moveObs;
	}
	
}
