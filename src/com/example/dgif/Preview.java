package com.example.dgif;



import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


//TODO: Add Rotation Vector sensor usage and replace accelerometer data in view
public class Preview extends Activity {
	
	
	private static final String DEBUG_TAG = "Preview";
	
	private boolean mOverlayMode = false;
	
	public final static int VIEW_WIDTH = 720;
	public final static int VIEW_HEIGHT = 1280;
	public final static int PIC_WIDTH = 480; //1458;
	public final static int PIC_HEIGHT = 640; //2592;
	public final static int VIEW_PIC_RATIO = VIEW_WIDTH / PIC_WIDTH;
	
	private final static String THEME_COLOR = "#7D0000";

	protected static final int LOAD_CAM_PREV = 0;
	
	private Camera mCamera;
	private CamView mPreview;
	private boolean mIsPreviewing;
	private UIHandler mHandler;
	private FrameLayout mPreviewFrame;
	
	private Button mGalleryButton;
	private Button mMovifyButton;
	
	private MovieEncoder mMovieEncoder;
	
	private float mMotionX;
	private float mMotionY;
	private float mMotionZ;
	
	
	private float mMotionXView;
	private float mMotionYView;
	private float mMotionZView;
	private GridLayout mHeader;
	
	private AutoFocusListener mAutoFocusCallback;
	private Sensor mAccelerometer;
	private SensorManager mSensorManager;
	
	private MemoryManager mMemoryManager;
	
	private boolean onPauseCalled;
	
	private TextView mCoordXView;
	private TextView mCoordYView;
	private TextView mCoordZView;
	
	private ImageView blinkingArrowView;
	private AnimationDrawable blinkingArrow;
	
	private ImageView mOverlayView;
	private Bitmap mLastImage;
	
	private RelativeLayout mFrameWrapper;
	Button mCaptureButton;
	Button mTrashButton;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		mLastImage = null;
		
		mFrameWrapper = (RelativeLayout) findViewById(R.id.camera_wrapper);
		
	    mCaptureButton = (Button) findViewById(R.id.capture_button);
		
		mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
		mGalleryButton = (Button) findViewById(R.id.gallery_button);
		mMovifyButton = (Button) findViewById(R.id.movify_button);
		
		mTrashButton = (Button) findViewById(R.id.trash_button);
		
		mMovieEncoder = new MovieEncoder();
		mMovieEncoder.saveContext(this);
		
	    mCoordXView = (TextView) findViewById(R.id.xcoordView);
	    mCoordYView = (TextView) findViewById(R.id.ycoordView);
	    mCoordZView = (TextView) findViewById(R.id.zcoordView);
	    
	    mHeader = (GridLayout) findViewById(R.id.coord_view);
	    
	    blinkingArrowView = (ImageView) findViewById(R.id.blinking_arrow_view);
	    blinkingArrow = ((AnimationDrawable) blinkingArrowView.getDrawable());
	    
	    mOverlayView = (ImageView) findViewById(R.id.image_overlay_view);
	    
		
		mMemoryManager = new MemoryManager(this);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAutoFocusCallback = new AutoFocusListener();
		
		mIsPreviewing = false;
		mHandler = new UIHandler();
		
		mMotionX = 0;
		mMotionY = 0;
		mMotionZ = 0;
		
		mMotionXView = 0;
		mMotionYView = 0;
		mMotionZView = 0;
		
		mGalleryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Preview.this, ImageGallery.class);
				startActivity(i);
				
			}
			
		});
		
		mMovifyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						Log.d(DEBUG_TAG, "attempt to save movie");
						Bitmap bitmaps[] = mMemoryManager.getAllImages();
						mMovieEncoder.saveMovieFromBitmaps(bitmaps);
					}
					
				}).start();
				
				
			}
			
		});
		
		
		mTrashButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOverlayView.getVisibility() == View.VISIBLE) 
					mOverlayView.setVisibility(View.GONE);
				mTrashButton.setVisibility(View.GONE);
			}
			
		});
		
		// Begin loading camera resource
		new Thread(new LoadCameraAndPrev()).start();
		onPauseCalled = false;
		
		drawHeader();
		
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		View decorView = getWindow().getDecorView();

		// Hide the status bar.
		//int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		//decorView.setSystemUiVisibility(uiOptions);

		// Hide action bar
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
	
        if (onPauseCalled) {
        	new Thread(new LoadCameraAndPrev()).start();
        }
        
        
		
		
	}
	
	
	private void drawHeader() {
		
		int color = Color.parseColor(THEME_COLOR);
	  
		//Get Screen Dimensions
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		Log.d(DEBUG_TAG, "Screen dimensions: " + width + " x " + height);
		
		//int headerHeight = (int) (0.07 * height);
		int headerHeight = 41;
		
		//Draw Header
		LinearLayout header = new LinearLayout(this);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, headerHeight);
		header.setLayoutParams(p);
		header.setOrientation(LinearLayout.HORIZONTAL);
		header.setBackgroundColor(Color.BLACK);
		header.setVisibility(View.VISIBLE);
		
		mFrameWrapper.addView(header);
		
		//Draw Footer
		
		//int footerHeight = (int) (.13 * height);
		int footerHeight = 50;
		RelativeLayout footer = new RelativeLayout(this);
		
		RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, footerHeight);
		//p2.setMargins(0, height - footerHeight - 50, 0, 0);
		p2.setMargins(0, height - footerHeight - 50, 0, 0);
		footer.setLayoutParams(p2);
		footer.setBackgroundColor(Color.BLACK);
		footer.setVisibility(View.VISIBLE);
	//	footer.setTop(height - headerHeight);
		
		mFrameWrapper.addView(footer);
		
		
		
		
	
		
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		stopPreview();
		releaseCamera();
		
		if (blinkingArrow.isRunning()) {
			blinkingArrow.stop();
			blinkingArrowView.setVisibility(View.GONE);
		}
		
		if (mOverlayView.getVisibility() == View.VISIBLE) {
			mOverlayView.setVisibility(View.GONE);
		}
		
		if (mTrashButton.getVisibility() == View.VISIBLE) {
			mTrashButton.setVisibility(View.GONE);
		}
		
		onPauseCalled = true;
	}
	

	/*********************************************************************************************/
	
	 
	/*SETUP VIEW
	 * - Called only after a LoadCameraAndPrev thread is finished
	 * - Adds listener to newly created view view and adds it to the frame
	 */
	public void setupView() {
		mPreview = new CamView(this, mCamera);
		
//		mPreviewFrame.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				Log.i(DEBUG_TAG, "view touched");
//
//				if (mCamera != null && mIsPreviewing) {
//					mCamera.takePicture(null, null, mPictureCallback);
//				}
//
//				return false;
//			}
//		});

		mPreviewFrame.addView(mPreview);
		
		mCaptureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCamera != null && mIsPreviewing) {
    				mCamera.takePicture(null, null, mPictureCallback);
				}
				
			}
			
		});

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
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				Log.e(DEBUG_TAG, "Thread sleep failure on picture taken");
//			}
			
			//save image
			mMemoryManager.saveImage(data);
			
			//restart preview
			startPreview(mPreview.getHolder());
			
			blinkingArrowView.setVisibility(View.VISIBLE);
			blinkingArrow.start();
			
			//TODO: Make counter. When n pictures are taken, create gif. 
			/* Set picture to image overlay */
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			
			//resize
			/***OLD**/
			int cropFromBothSides = 45;
			int newWidth = 480 - (2 * cropFromBothSides);
			double newRatio = (double)720 / newWidth;
			int newHeight = (int) (640 * newRatio);
			
			Bitmap croppedBM = Bitmap.createBitmap(bm, cropFromBothSides - 5, 0, newWidth, 640);
			Log.d(DEBUG_TAG, "cropped bm: " + croppedBM.getWidth() + " x " + croppedBM.getHeight());
			//scale
			//Bitmap scaledBM = Bitmap.createScaledBitmap(croppedBM, 720, 1100, true);
			Bitmap scaledBM = Bitmap.createScaledBitmap(croppedBM, 720, 1050, true);
			
			//set scale type
			mOverlayView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			/********/
			

			//shift
			//Bitmap shiftedBM = Bitmap.createBitmap(scaledBM, x, y, width, height, m, filter)
			
			
			
			//put in imageview
		    mOverlayView.setImageBitmap(scaledBM);
		    mTrashButton.setVisibility(View.VISIBLE);
		    
		    
		    mCaptureButton.bringToFront();
		    mTrashButton.bringToFront();
		    mGalleryButton.bringToFront();
			
		
			
			if (mOverlayView.getVisibility() == View.GONE) {
				mOverlayView.setVisibility(View.VISIBLE);
				mGalleryButton.bringToFront();
				mHeader.bringToFront();
				
			}
			
			
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
			
			p.setRotation(90);
			
			p.setPictureSize(PIC_HEIGHT, PIC_WIDTH);
			List<Size> sizes = p.getSupportedPictureSizes();
			//List<Size> sHeights = p.getSupportedPictureSizes();
			Log.d(DEBUG_TAG, "supported picture sizes:");
			for (int i = 0; i < sizes.size(); i++) {
				Log.d(DEBUG_TAG, sizes.get(i).width + " x " + sizes.get(i).height);
			}
			
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
			//Log.d(DEBUG_TAG, "onAutoFocus");
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			
			if(Math.abs(event.values[0] - mMotionX) > 1 
		            || Math.abs(event.values[1] - mMotionY) > 1 
		            || Math.abs(event.values[2] - mMotionZ) > 1 ) {
		           ;
		            try {
		          
		                mCamera.autoFocus(this);
		            } catch (RuntimeException e) { 
		            	Log.e(DEBUG_TAG, "try autofocus FAIL");
		            }
		            
		            mMotionX = event.values[0];
		            mMotionY = event.values[1];
		            mMotionZ = event.values[2];
		            
		            
		        }
			
			if(Math.abs(event.values[0] - mMotionXView) > 0.05 
		            || Math.abs(event.values[1] - mMotionYView) > 0.05 
		            || Math.abs(event.values[2] - mMotionZView) > 0.05 ) {
				
				mMotionXView = (float) Math.round(event.values[0] * 1000)/1000;
				mMotionYView = (float) Math.round(event.values[1] * 1000)/1000;
				mMotionZView = (float) Math.round(event.values[2] * 1000)/1000;
				
				mCoordXView.setText("" + mMotionXView);
				mCoordYView.setText("" + mMotionYView);
				mCoordZView.setText("" + mMotionZView);
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
