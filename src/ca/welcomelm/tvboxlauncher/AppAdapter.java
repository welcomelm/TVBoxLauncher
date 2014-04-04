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
	public static int currentSelected = 0x1;
	public static int lastSelected = 0x2;
	public static int neither = 0x3;
	public static int count = 0;

	public AppAdapter(Context context, int resID) {
		super(context, 0);
		this.context = context;
		this.resID = resID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int selected = neither;
		// TODO Auto-generated method stub
		LinearLayout ll = null;
		
		if (convertView != null) {
			ll = (LinearLayout) convertView;
		}else {
			ll = (LinearLayout) LayoutInflater.from(context).inflate(resID, parent, false);
		}
		
		AppInfo appInfo = getItem(position);
		
		if (MainActivity.currentSelectedGridView == ll) {
			selected = currentSelected;
		}else if (MainActivity.lastSelectedGridView == ll) {
			selected = lastSelected;
		}
		
		appInfo.SetMeOnTextView(ll , selected);
		
		return ll;
	}
}
