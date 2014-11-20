package com.example.dgif;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TestGifView extends Activity {

	ImageView mView;
	AnimationDrawable gif = null;
	Bitmap[] images;
	boolean[] mPicsSelected;
	ArrayList<BitmapDrawable> mBitmaps;
	
	SeekBar mSpeedBar;
	TextView mSpeedView;

	
	//TODO: fix out of memory error
	private static final int DEFAULT_DURATION = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_gif_view);
		
		MemoryManager m = new MemoryManager(this);
		
		mSpeedBar = (SeekBar) findViewById(R.id.speedBar);
		mSpeedView = (TextView) findViewById(R.id.speedView);
		
		
		
		mSpeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mSpeedView.setText(""+progress);
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
				setDrawable(seekBar.getProgress());
				
			}
			
		}); 
				
		
		images = m.getAllImages();
		
		mView = (ImageView) findViewById(R.id.testGifView);
		
		mPicsSelected = getIntent().getExtras().getBooleanArray("picsSelected");
		mBitmaps = getSelectedImages();
		setDrawable(DEFAULT_DURATION);
		
	}

	
	
	private ArrayList<BitmapDrawable> getSelectedImages() {
		ArrayList<BitmapDrawable> list = new ArrayList<BitmapDrawable>();
		for (int i = 0; i < mPicsSelected.length; i++) {
			if (mPicsSelected[i]) {
				list.add(new BitmapDrawable(getResources(), images[i]));
			}
		}
		return list;
	}
	
	
	private AnimationDrawable createGif(ArrayList<BitmapDrawable> frames, int duration) {
		
		AnimationDrawable anim = new AnimationDrawable();
		int count = frames.size();
		
		//forward
		for (int i = 0; i < count; i++) {
			anim.addFrame(frames.get(i), duration);
		}
		
		//reverse
		for (int i = count - 1; i >= 0; i--) {
			anim.addFrame(frames.get(i), duration);
		}
		
		return anim;

	} 
	
	
	private void setDrawable(int speed) {
		
		if (gif != null && gif.isRunning()) {
			gif.stop();
			mView.setBackground(null);
		}
		
		gif = createGif(mBitmaps, speed);
		gif.setOneShot(false);
		
		mView.setBackground(gif);
		
		gif.start();
	}

	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		if (gif.isRunning()) {
			gif.stop();
		}
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (!gif.isRunning()) {
			gif.start();
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_gif_view, menu);
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
