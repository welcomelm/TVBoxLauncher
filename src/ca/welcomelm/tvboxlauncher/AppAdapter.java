package ca.welcomelm.tvboxlauncher;

import java.util.zip.Inflater;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppAdapter extends ArrayAdapter<ApplicationInfo> {

	private Context context;
	private int resID;
	private Point dimension;

	public AppAdapter(Context context, int resID, Point dimension) {
		super(context, 0);
		this.context = context;
		this.resID = resID;
		this.dimension = dimension;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LinearLayout ll = null;
		
		if (convertView != null) {
			ll = (LinearLayout) convertView;
		}else {
			ll = (LinearLayout) LayoutInflater.from(context).inflate(resID, parent, false);
		}		
		
		ApplicationInfo appInfo = getItem(position);
		
		TextView tv = (TextView)ll.findViewById(R.id.tvAppTitle);
		tv.setWidth(dimension.x);
		tv.setHeight(dimension.y);
		
		switch (resID) {
		case R.layout.app_cell:
			tv.setText(appInfo.title);
			tv.setPadding(0, dimension.x/15, 0, dimension.x/15);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension.x/10);
			tv.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null, null);			
			break;

		case R.layout.favorites_cell:
			tv.setPadding(0, dimension.x/10, 0, dimension.x/10);
			tv.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null, null);
			break;
			
		default:
			break;
		}
		return ll;
		
//		ImageView iv = (ImageView)ll.findViewById(R.id.ivAppIcon);
//		TextView tv = (TextView)ll.findViewById(R.id.tvAppTitle);
//		
//		tv.setWidth(dimension.x);
//		tv.setHeight((int) (dimension.y * 0.3));
//		
//		iv.setScaleType(ScaleType.CENTER);
//		iv.setImageDrawable(appInfo.icon);
//		tv.setText(appInfo.title);
		
//		return ll;
	}
}
