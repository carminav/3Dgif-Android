package com.example.dgif;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CamPreview extends ViewGroup implements SurfaceHolder.Callback {

	
	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	
	
	CamPreview(Context context) {
		super(context);
		
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}
