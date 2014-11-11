package com.example.dgif;

import java.io.File;
import java.io.IOException;

import org.jcodec.api.SequenceEncoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;



//Object used to turn sequence of bitmaps into a movie
public class MovieEncoder {

	private static final String DEBUG_TAG = "Movie Encoder";
	SequenceEncoder mSeqEncoder;
	MemoryManager mMemoryManager;
	Context context;
	
	public void MediaEncoder(Context context) {
		this.context = context;
		mMemoryManager = new MemoryManager(context);
		
	}
	
	public void saveContext(Context context) {
		this.context = context;
		mMemoryManager = new MemoryManager(context);
	}
	
	public void saveMovieFromBitmaps(Bitmap[] bitmaps) {
		Log.d(DEBUG_TAG, "in saveMOvieFromBitmaps");
		
		File outFile = mMemoryManager.getMovieDirectory("blahblah.mp4");

		if (outFile == null) {
			Log.e(DEBUG_TAG, "outfile is null");
			return; 
		}

		try {
			Log.d(DEBUG_TAG, "attempt to create new Sequence Encoder");
			mSeqEncoder = new SequenceEncoder(outFile);
			Log.d(DEBUG_TAG, "Sequence Encoder Success");

			for (int i = 0; i < bitmaps.length; i++) {

				//create pictures out of bitmaps
				Bitmap src = bitmaps[i];
				Picture dst = Picture.create((int) src.getWidth(), src.getHeight(), ColorSpace.RGB);
				fromBitmap(src, dst);

				//encode into movie and save
				mSeqEncoder.encodeNativeFrame(dst);
				

			}
			mSeqEncoder.finish();


		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	// I have no idea what this does
	private void fromBitmap(Bitmap src, Picture dst) {
		int[] dstData = dst.getPlaneData(0);
		int[] packed = new int[src.getWidth() * src.getHeight()];
		
		src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
		
		for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
	            int rgb = packed[srcOff];
	            dstData[dstOff]     = (rgb >> 16) & 0xff;
	            dstData[dstOff + 1] = (rgb >> 8) & 0xff;
	            dstData[dstOff + 2] = rgb & 0xff;
	        }
		}
	}
	
	
	
	
}
