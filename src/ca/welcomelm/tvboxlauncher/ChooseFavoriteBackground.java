package ca.welcomelm.tvboxlauncher;

import java.io.FileNotFoundException;
import java.util.Arrays;

import android.R.integer;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

public class ChooseFavoriteBackground extends Activity implements OnItemClickListener {
	
	static public String SELECTED_BACKGROUND_RESID = "ca.welcomelm.tvboxlauncher.extra.backgroundResId";
	private GridView gvFavoriteBackground;
	private Point cellDemsion;
	private LinearLayout llFavoriteBackground;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.change_favorite_background);
		
		gvFavoriteBackground = (GridView) findViewById(R.id.gvFavoriteBackground);
		llFavoriteBackground = (LinearLayout) findViewById(R.id.llFavoriteBackground);
		
		setDimensions();
		
		ImageAdapter adapter = new ImageAdapter();
		
		for (int i = 0; i < 11; i++) {
			int resId = getResources().getIdentifier("app_background_" + String.valueOf(i), 
					"drawable", getPackageName());
			
			adapter.add(resId);
		}
		
		gvFavoriteBackground.setAdapter(adapter);
		gvFavoriteBackground.setOnItemClickListener(this);
		
		gvFavoriteBackground.setSelector(AppStyle.getCurrentStyle().getImageId(AppStyle.large_selector));
	}

	private void setDimensions() {
		// TODO Auto-generated method stub
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		if (metrics.widthPixels > 1280) {
			metrics.widthPixels = 1920;
		}else {
			metrics.widthPixels = 1280;
		}
		
		if (metrics.heightPixels > 720) {
			metrics.heightPixels = 1080;
		}else{
			metrics.heightPixels = 720;
		}
		
		llFavoriteBackground.getLayoutParams().width = metrics.widthPixels * 4 / 5;
		llFavoriteBackground.getLayoutParams().height = metrics.heightPixels * 4 / 5;
		
		double verticalPercent = ( 4 / 5.0 - 3 / 5.0 ) / 4.0;
		cellDemsion = new Point((int) (metrics.widthPixels / 5), (int) (metrics.heightPixels / 5));
		gvFavoriteBackground.setColumnWidth((int) (metrics.widthPixels * 4 / 15));
		gvFavoriteBackground.setPadding(0, (int)(metrics.heightPixels*verticalPercent), 0, 
				(int)(metrics.heightPixels*verticalPercent));
		gvFavoriteBackground.setVerticalSpacing((int) (metrics.heightPixels*verticalPercent));
	}
	
	private class ImageAdapter extends ArrayAdapter<Integer>{
		
		public ImageAdapter() {
			// TODO Auto-generated constructor stub
			super(ChooseFavoriteBackground.this, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LinearLayout ll = null;
			
			if (convertView != null) {
				ll = (LinearLayout) convertView;
			}else {
				ll = (LinearLayout) LayoutInflater.from(ChooseFavoriteBackground.this).
						inflate(R.layout.favorites_cell, parent, false);
			}
			
			ImageView iv = (ImageView)ll.findViewById(R.id.ivFavorite);
			
			ll.getLayoutParams().width = cellDemsion.x;
			ll.getLayoutParams().height = cellDemsion.y;
			
			iv.getLayoutParams().width = cellDemsion.x - 15;
			iv.getLayoutParams().height = cellDemsion.y - 15;
			
			iv.setBackgroundResource((Integer) gvFavoriteBackground.getAdapter().getItem(position));
				
			return ll;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent();
		
		intent.putExtra(SELECTED_BACKGROUND_RESID, (Integer) gvFavoriteBackground.getAdapter().getItem(position));

		setResult(RESULT_OK, intent);
		finish();
	}

}
