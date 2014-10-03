package com.example.dgif;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

public class Main extends ActionBarActivity {
	
	
	static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final String DEBUG_TAG = "Main";
	
	private ProgressBar cameraProgressBar;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		View decorView = getWindow().getDecorView();

		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);

		// Hide action bar
		ActionBar actionBar = getActionBar();

		setContentView(R.layout.activity_main);
		
		cameraProgressBar = (ProgressBar) findViewById(R.id.camera_progress_bar);

	}

	  //test

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
}
