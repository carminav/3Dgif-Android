package com.example.dgif;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageGallery extends Activity {
//TODO: Add navigation buttons to navigate from preview screen to image gallery
//TODO: Create custom image view so that each emage has a white background in the gallery
//TODO: Make each view a square and zoomed in 	
private GridView gridview;
	
	private static final int SIZE = 200;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_gallery);
		
		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new ImageAdapter(this));
		

	}
	
	private class ImageAdapter extends BaseAdapter {

		private Context mContext;
		private Bitmap[] images;
		
		public ImageAdapter(Context c) {
			mContext = c;
			MemoryManager m = new MemoryManager(c);
			images = m.getAllImages();
		}
		
		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public Object getItem(int position) {
			return images[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		/*GET VIEW
		 * create new imageView for each item referenced by the adapter
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = null;
			
			//if view is not recycled, initialize attributes
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(SIZE,SIZE));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(20, 20, 20, 20);
			} else {
				imageView = (ImageView) convertView;
			}
			
			imageView.setImageBitmap(images[position]);

			return imageView;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_gallery, menu);
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
