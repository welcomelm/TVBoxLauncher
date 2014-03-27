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

public class AppAdapter<T extends AppInfo> extends ArrayAdapter<T> {

	private Context context;
	private int resID;

	public AppAdapter(Context context, int resID) {
		super(context, 0);
		this.context = context;
		this.resID = resID;
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
		
		AppInfo appInfo = getItem(position);
		
		View v = ll.findViewById(R.id.tvAppTitle);

		appInfo.SetMeOnTextView(v);
		
		return ll;
	}
}
