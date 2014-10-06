package com.example.dgif;



import java.io.IOException;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

public class Preview extends Activity {
	
	
	private static final String DEBUG_TAG = "Preview";

	protected static final int LOAD_CAM_PREV = 0;
	
	private Camera mCamera;
	private CamView mPreview;
	private boolean mIsPreviewing = false;
	private FrameLayout mPreviewFrame;
	
	private float mMotionX = 0;
	private float mMotionY = 0;
	private float mMotionZ = 0;
	
	private AutoFocusListener afListenerCallback;
	private Sensor mAccelerometer;
	SensorManager mSensorManager;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		new Thread(new LoadCameraAndPrev()).start();
		
		
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg != null) {
				switch (msg.what) {
				case LOAD_CAM_PREV:
				
					setupView();
					
				}
			} else {
				Log.e(DEBUG_TAG, "handler msg is null");
			}

		}

	};
	
	public void setupView() {
		mPreview = new CamView(this, mCamera);
		mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
		
		mPreviewFrame.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(DEBUG_TAG, "view touched");
				
				//take picture
			
				
				return false;
			}
		});
		

		mPreviewFrame.addView(mPreview);

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
			
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			afListenerCallback = new AutoFocusListener();
			
			mSensorManager.registerListener(afListenerCallback, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			
			mCamera.setDisplayOrientation(90);
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_CAM_PREV));
			
		}
		
	}
	
	private class AutoFocusListener implements SensorEventListener, AutoFocusCallback {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.d(DEBUG_TAG, "onAutoFocus");
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			
			if(Math.abs(event.values[0] - mMotionX) > 1 
		            || Math.abs(event.values[1] - mMotionY) > 1 
		            || Math.abs(event.values[2] - mMotionZ) > 1 ) {
		            Log.d("Camera System", "Refocus");
		            try {
		            	Log.d(DEBUG_TAG, "try autofocus");
		                mCamera.autoFocus(this);
		            } catch (RuntimeException e) { 
		            	Log.e(DEBUG_TAG, "try autofocus FAIL");
		            }
		            
		            mMotionX = event.values[0];
		            mMotionY = event.values[1];
		            mMotionZ = event.values[2];
		        }
			
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
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
			Log.i(DEBUG_TAG, "surface created");
			startPreview(mHolder);
			
			//TODO: Add Loading Circle before camera shows up 
			
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i(DEBUG_TAG, "surface changed");
			
			if (mHolder.getSurface() == null) {
				Log.e(DEBUG_TAG, "mHolder is null in surfaceChanged");
				return;
			}
			
			
			
			//TODO: surfaceChange causes bug because camera becomes null
			//shut down current preview
			stopPreview();

			
			
			
			//start preview
			startPreview(mHolder);

			
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			
			//do nothing
		}

		// Change camera parameters
		//TODO: Check if camera parameters are correct. Right now it still looks weird
		//TODO: Add autofocus camera functionality
		private void setCameraParameters(int width, int height) {

			// Get camera parameters object
			Camera.Parameters p = mCamera.getParameters();
			

            p.set("orientation", "portrait");
            mCamera.setParameters(p);

			// Find closest supported preview size
			Camera.Size bestSize = findBestSize(p, width, height);

			// FIX - Should lock in landscape mode?

			int tmpWidth = bestSize.width;
			int tmpHeight = bestSize.height;

			if (bestSize.width < bestSize.height) {
				tmpWidth = bestSize.height;
				tmpHeight = bestSize.width;
			}
			
			List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();
			
			
			//p.setPreviewSize(tmpWidth, tmpHeight);
			Log.d(DEBUG_TAG, "width: " + supportedSizes.get(2).width + " height: " + supportedSizes.get(2).height);
			p.setPreviewSize(supportedSizes.get(1).width, supportedSizes.get(1).height);
			
			mCamera.setParameters(p);
		}

		// Determine the largest supported preview size
		//TODO: Delete this method? Right 720 x 1280 is hardcoded
		private Camera.Size findBestSize(Camera.Parameters parameters,
				int width, int height) {

			List<Camera.Size> supportedSizes = parameters
					.getSupportedPreviewSizes();

			Camera.Size bestSize = supportedSizes.remove(0);

			for (Camera.Size size : supportedSizes) {
				
				Log.d(DEBUG_TAG, size.width + " x " + size.height);
				
				if ((size.width * size.height) > (bestSize.width * bestSize.height)) {
					bestSize = size;
				}
			}

			Log.d(DEBUG_TAG, "Best size: " + bestSize.width + " x " + bestSize.height);
			return bestSize;
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
	
	private void startPreview(final SurfaceHolder mHolder) {
		

		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				
				if (mCamera == null) {
					try {
						mCamera = Camera.open();
					} catch (RuntimeException e) {
						Log.d(DEBUG_TAG, "camera wont open. start prev");
					}
				}
				
				if (mCamera != null && !mIsPreviewing) {
					try {
						
						
						Camera.Parameters p = mCamera.getParameters();
						List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();
						
						
						//p.setPreviewSize(tmpWidth, tmpHeight);
						Log.d(DEBUG_TAG, "width: " + supportedSizes.get(2).width + " height: " + supportedSizes.get(2).height);
						p.setPreviewSize(supportedSizes.get(1).width, supportedSizes.get(1).height);
						
						mCamera.setParameters(p);
						
						mSensorManager.registerListener(afListenerCallback, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
						
						mCamera.setDisplayOrientation(90);
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
			mIsPreviewing = false;
			
			mSensorManager.unregisterListener(afListenerCallback, mAccelerometer);
			
		}
	}





	

		


	
	
}
