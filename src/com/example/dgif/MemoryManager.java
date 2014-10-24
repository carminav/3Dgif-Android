package com.example.dgif;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/* @author Carmina Villaflores
 * Manages the saving and retrieving of data to internal app memory
 * TODO: Allow video files to be saved to external memory
 */
public class MemoryManager {

	private static final String IMG_TAG = "3dGif";
	

	private Context context;


	public MemoryManager(Context context) {
		this.context = context;
	}

	/*SAVE IMAGE
	 * Saves byte array as image in internal memory
	 */
	@SuppressLint("SimpleDateFormat")
	public void saveImage(byte[] data) {

		String filename = IMG_TAG + (new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		try {
			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void saveImage(byte[] data) {
//		String root = Environment.getExternalStorageDirectory().toString();
//		File dir = new File(root + "/3dgifData");
//		dir.mkdirs();
//		Random generator = new Random();
//		int n = 10000;
//		n = generator.nextInt(n);
//		String filename = "Img-" + n;
//		File file = new File(dir, filename);
//		if (file.exists()) file.delete();
//		try {
//			FileOutputStream out = new FileOutputStream(file);
//			
//		}
//	}

	/*GET ALL IMAGES
	 * Returns an array of bitmaps located in app's internal memory
	 */
	public Bitmap[] getAllImages() {

		String[] files = context.fileList();
		Bitmap[] images = new Bitmap[files.length];

		for (int i = 0; i < images.length; i++) {

			String filename = files[i];

			try {
				FileInputStream fis = context.openFileInput(filename);
				images[i] = BitmapFactory.decodeStream(fis);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		return images;

	}
	
	/* EXTERNAL STORAGE 
	 * Checks if external storage is available for read and write
	 */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) return true;
		else return false;
	}
	
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) return true;
		else return false;
	}

	
	

}
