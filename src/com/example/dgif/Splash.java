package com.example.dgif;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

/* @author Carmina Villaflores
 * Splash screen which appears upon starting up 3dGif APP 
 */
public class Splash extends ActionBarActivity {

	private static int SPLASH_TIME_OUT = 3000;
	private static final String DEBUG_TAG = "Splash";
	
	/*Necessary so that the setSystemUiVisibility() call compiles */
	@SuppressLint("NewApi") 
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent i = new Intent(Splash.this, Main.class);
				startActivity(i);
				finish();
			}
        	
        }, SPLASH_TIME_OUT);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }
    
   

}
