package com.example.dgif;



import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class Preview extends Activity {
	
	
	private static final String DEBUG_TAG = "Preview";

	protected static final int LOAD_CAM_PREV = 0;
	
	private Camera mCamera;
	private CamView mPreview;
	private boolean mIsPreviewing = false;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		
		new Thread(new LoadCameraAndPrev()).start();
		
		
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg != null) {
				switch (msg.what) {
				case LOAD_CAM_PREV:
					//setUpViewAndHolders();
					setupView();
					
				}
			} else {
				Log.e(DEBUG_TAG, "handler msg is null");
			}

		}

	};
	
	public void setupView() {
		mPreview = new CamView(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
	}

	private class LoadCameraAndPrev implements Runnable {

		@Override
		public void run() {
			
			//Open Camera
			try {
				mCamera = Camera.open();
				Log.i(DEBUG_TAG, "Camera opened");
			} catch (RuntimeException e) {
				Log.e(DEBUG_TAG, "Camera will not open. onCreate");
				finish();
			}
			
	
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_CAM_PREV));
			
		}
		
	}
	
	
	private class CamView extends SurfaceView implements SurfaceHolder.Callback {

		private SurfaceHolder mHolder;
		private Camera mCamera;
		
		public CamView(Context context, Camera camera) {
			super(context);
			mCamera = camera;
			
			mHolder = getHolder();
			mHolder.addCallback(this);
			
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.i(DEBUG_TAG, "surface created");
			showPreview(mHolder);
			
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i(DEBUG_TAG, "surface changed");
			
			if (mHolder.getSurface() == null) {
				return;
			}
			
			//shut down current preview
			stopPreview();
			
			//start preview
			showPreview(mHolder);
			
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		View decorView = getWindow().getDecorView();

		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);

		// Hide action bar
		ActionBar actionBar = getActionBar();
		actionBar.hide();
	}


	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		stopPreview();
	}
	
	private void showPreview(final SurfaceHolder mHolder) {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mCamera != null && !mIsPreviewing) {
					try {
						mCamera.setPreviewDisplay(mHolder);
						mCamera.startPreview();
						mIsPreviewing = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Log.e(DEBUG_TAG, "mCamera is null and mIsPrev: " + mIsPreviewing);
				}
			}
			
		}).start();
		
		
	}
	
	
	private void stopPreview() {
		if (mCamera != null && mIsPreviewing) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}





	

		


	
	
}
