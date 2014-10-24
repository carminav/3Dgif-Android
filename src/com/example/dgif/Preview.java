package com.example.dgif;



import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

public class Preview extends Activity {
	
	
	private static final String DEBUG_TAG = "Preview";
	
	public final static int VIEW_WIDTH = 720;
	public final static int VIEW_HEIGHT = 1280;
	

	protected static final int LOAD_CAM_PREV = 0;
	
	private Camera mCamera;
	private CamView mPreview;
	private boolean mIsPreviewing;
	private UIHandler mHandler;
	private FrameLayout mPreviewFrame;
	
	private Button mBackButton;
	
	private float mMotionX;
	private float mMotionY;
	private float mMotionZ;
	
	private AutoFocusListener mAutoFocusCallback;
	private Sensor mAccelerometer;
	private SensorManager mSensorManager;
	
	private MemoryManager memoryManager;
	
	private boolean onStartCalled = false;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
		mBackButton = (Button) findViewById(R.id.back_button);
		
		memoryManager = new MemoryManager(this);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAutoFocusCallback = new AutoFocusListener();
		
		mIsPreviewing = false;
		mHandler = new UIHandler();
		
		mMotionX = 0;
		mMotionY = 0;
		mMotionZ = 0;
		
		mBackButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Preview.this, ImageGallery.class);
				startActivity(i);
				
			}
			
		});
		
		// Begin loading camera resource
		new Thread(new LoadCameraAndPrev()).start();
		onStartCalled = true;
		
		
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
		
		//TODO: Camera is null sometimes
        Log.d(DEBUG_TAG, "onStartCalled: " + onStartCalled);
        
        if (!onStartCalled) {
        	new Thread(new LoadCameraAndPrev()).start();
        }
		
		
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		stopPreview();
		releaseCamera();
		
		onStartCalled = false;
	}
	

	/*********************************************************************************************/
	
	 
	/*SETUP VIEW
	 * - Called only after a LoadCameraAndPrev thread is finished
	 * - Adds listener to newly created view view and adds it to the frame
	 */
	public void setupView() {
		mPreview = new CamView(this, mCamera);
		mPreviewFrame.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(DEBUG_TAG, "view touched");

				if (mCamera != null && mIsPreviewing) {
					mCamera.takePicture(null, null, mPictureCallback);
				}

				return false;
			}
		});

		mPreviewFrame.addView(mPreview);

	}
	

	/* START PREVIEW 
	 * - Sets preview display as surface holder
	 * - starts camera's preview
	 */
	private void startPreview(final SurfaceHolder holder) {
		
		if (mCamera != null && !mIsPreviewing) {

			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			mCamera.startPreview();
			mIsPreviewing = true;
			
		} else {
			Log.e(DEBUG_TAG, "mCamera is null and mIsPrev: " + mIsPreviewing);
		}

	}
	
	
	/* STOP PREVIEW
	 * Stops camera preview on surface view (cam view) and unregisters listener
	 */
	private void stopPreview() {
		if (mCamera != null && mIsPreviewing) {
			Log.d(DEBUG_TAG, "stop preview");
			mIsPreviewing = false;
			mCamera.stopPreview();

		}
	}
	
	/* RELEASE CAMERA
	 * Release camera so other applications can use it
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			mSensorManager.unregisterListener(mAutoFocusCallback, mAccelerometer);
			mCamera.release();
			mCamera = null;

		}
	}


	
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			
			mIsPreviewing = false;
			
			//Give user time to view image
			//TODO: Change to new activity preview screen so user can choose to 
			// retake picture or keep
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(DEBUG_TAG, "Thread sleep failure on picture taken");
			}
			
			//save image
			memoryManager.saveImage(data);
			
			
			//restart preview
			startPreview(mPreview.getHolder());
			
		}
		
	};
	
	/*********************************************************************************************/
    /*									 INNER CLASSES                                           */
	/*********************************************************************************************/
	
	/*LOAD CAMERA AND PREV CLASS (THREAD USE)
	 * - Opens camera instance
	 * - Sets parameters for camera
	 * - Attaches sensor listener
	 */
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
			
			Camera.Parameters p = mCamera.getParameters();
			p.setPreviewSize(VIEW_HEIGHT, VIEW_WIDTH);      //Note that height and width are switched 
			mCamera.setParameters(p);
			
			mSensorManager.registerListener(mAutoFocusCallback, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			
			mCamera.setDisplayOrientation(90);
			
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_CAM_PREV));
			
		}
		
	}
	
	/*UI HANDLER CLASS
	 * Responsible for posting tasks to UI's task queue from another thread
	 */
	@SuppressLint("HandlerLeak")
	private class UIHandler extends Handler {
		
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
		
		
	}
	
	
	/* AUTO FOCUS LISTENER CLASS
	 * - Used to auto focus whenever there is a change in movement
	 */
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
	

	
	/*CAM VIEW CLASS
	 * Serves as the preview screen for seeing what the camera sees live
	 */
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
			
			stopPreview();

			startPreview(mHolder);

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			
			//do nothing
		}



	}
	
	


	

		


	
	
}
