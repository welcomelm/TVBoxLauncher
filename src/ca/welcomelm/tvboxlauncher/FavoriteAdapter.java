package ca.welcomelm.tvboxlauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class FavoriteAdapter extends ArrayAdapter<ApplicationInfo> {
	private Context context;

	public FavoriteAdapter(Context context) {
		super(context, 0);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LinearLayout ll = null;
		
		if (convertView != null) {
			ll = (LinearLayout) convertView;
		}else {
			ll = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.favorites_cell, parent, false);
		}		
		
		ApplicationInfo appInfo = getItem(position);
		
		ImageView iv = (ImageView)ll.findViewById(R.id.ivFavoriteIcon);
		TextView tv = (TextView)ll.findViewById(R.id.tvFavoriteTitle);
		
		iv.setScaleType(ScaleType.CENTER);
		iv.setImageDrawable(appInfo.icon);
		tv.setText(appInfo.title);
		
		return ll;
	}


}
