package com.walloff.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends Activity {
	
	NetworkingManager n_man = null;
	Button close = null;
	Player players[ ] = null;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.net_test );
        
        players = new Player[ 1 ];
        for( int i = 0; i < players.length; i++ )
        	players[ i ] = new Player( "192.168.2.26" );
        
        close = ( Button )findViewById( R.id.close_btn );
        n_man = new NetworkingManager( this.getApplicationContext( ) );
        n_man.set_players( players );
        n_man.init_conns( );
        close.setOnClickListener( new View.OnClickListener( ) {
			
			public void onClick( View v ) {
				n_man.term_conns( );
			}
		});
    }

    /* Probably don't need this */
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }*/
}
