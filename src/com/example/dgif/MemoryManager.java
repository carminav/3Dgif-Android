package com.example.dgif;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/* @author Carmina Villaflores
 * Manages the saving and retrieving of data to internal app memory
 */
public class MemoryManager {
	
	private static final String IMG_TAG = "3dGif";
	private static int fileNum = 0;
	
    private Context context;
	
    
    public MemoryManager(Context context) {
    	this.context = context;
    }
	
    /*SAVE IMAGE
     * Saves byte array as image in internal memory
     */
	public void saveImage(byte[] data) {
		
		String filename = IMG_TAG + fileNum;
		try {
			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(data);
			fos.close();
			fileNum++;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
  
	
	

}
