package com.example.dgif;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

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
	
	public void saveImage(byte[] data) {
		
		String filename = IMG_TAG + fileNum;
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
  
	
	

}
