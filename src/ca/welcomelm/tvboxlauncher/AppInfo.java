package ca.welcomelm.tvboxlauncher;

import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppInfo {

	/**
     * The application name.
     */
	protected CharSequence title;

    /**
     * The intent used to start the application.
     */
	protected Intent intent;

    /**
     * The application icon.
     */
	protected Drawable icon;
	
	protected Point dimension;
	
	protected Context context;
    
    protected AppInfo(Context context , CharSequence title, ComponentName componentName, 
    					Drawable icon, Point dimension) {
		super();
		this.title = title;
		this.intent = setLauncherMainActivity(componentName, 
				Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		this.icon = icon;
		this.dimension = dimension;
		this.context = context;
	}
    
	public void scaleIcon(Context context , Point dimen){
		
		if ((icon == null) || !(icon instanceof BitmapDrawable)) {
	        return;
	    }

	    Bitmap b = ((BitmapDrawable)icon).getBitmap();

	    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, dimen.x, dimen.y, false);

	    icon = new BitmapDrawable(context.getResources(), bitmapResized);
	}
    
    /**
     * Creates the application intent based on a component name and various launch flags.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    private Intent setLauncherMainActivity(ComponentName className, int launchFlags) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        return intent;
    }
    
    public void SetMeOnTextView(View ll , int selected){
		ll.getLayoutParams().width = dimension.x;
		ll.getLayoutParams().height = dimension.y;
		TextView tv = (TextView) ll.findViewById(R.id.tvAppTitle);
		
		if (selected == AppAdapter.currentSelected) {
			resizeView(tv, dimension.x - 10, dimension.y - 10);
		} else if (selected == AppAdapter.lastSelected){
			resizeView(tv, dimension.x * 4 / 5, dimension.y * 4 / 5);
		}else{
			tv.setWidth(dimension.x * 4 / 5);
			tv.setHeight(dimension.y * 4 / 5);
		}
		
		tv.setText(title);
		tv.setPadding(0, dimension.x/20, 0, 0);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension.x/10);
		tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
    }
    
    protected void resizeView(View view , int width, int Height){
    	ResizeAnimation anim = new ResizeAnimation(view, width, Height);
		anim.setDuration(250);
		view.startAnimation(anim);
    }

    public CharSequence getTitle() {
		return title;
	}

	public void setTitle(CharSequence title) {
		this.title = title;
	}



	public Intent getIntent() {
		return intent;
	}



	public void setIntent(Intent intent) {
		this.intent = intent;
	}



	public Drawable getIcon() {
		return icon;
	}



	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppInfo)) {
            return false;
        }

        AppInfo that = (AppInfo) o;
        return title.equals(that.title) &&
                intent.getComponent().getClassName().equals(
                        that.intent.getComponent().getClassName());
    }
    
    @Override
    public int hashCode() {
        int result;
        result = (title != null ? title.hashCode() : 0);
        final String name = intent.getComponent().getClassName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

	public static void loadApplications(Context context, AppAdapter<AppInfo> adapter, Point dimension) {
		// TODO Auto-generated method stub
        PackageManager manager = context.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        adapter.clear();

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                
                AppInfo application = new AppInfo(context, info.loadLabel(manager) ,
                		new ComponentName(info.activityInfo.applicationInfo.packageName, 
                				info.activityInfo.name), 
                				loadFullResIcon(info, manager),
                				dimension);
                
                application.scaleIcon(context, new Point(dimension.y / 2, dimension.y / 2));

                adapter.add(application);
            }
        }
	}
	
	protected static Drawable loadFullResIcon(ResolveInfo info, PackageManager manager){
		try {
			ApplicationInfo appInfo= info.activityInfo.applicationInfo;
			Resources res = manager.getResourcesForApplication(appInfo);
			int displayMetrics[] = { DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_XHIGH , 
					DisplayMetrics.DENSITY_HIGH , DisplayMetrics.DENSITY_MEDIUM};

			for(int displayMetric : displayMetrics){
			Drawable d = res.getDrawableForDensity(appInfo.icon , displayMetric);
				if (d!=null) {
					return d;
				}
			}
		} catch (Exception e) {
		}
		
		return info.loadIcon(manager);
	}

	public void excute() {
		// TODO Auto-generated method stub
		context.startActivity(intent);
	}

	public void addMeToFavorite(AppAdapter<FavoriteAppInfo> adapter, Point dimension) {
		// TODO Auto-generated method stub
		FavoriteAppInfo favoriteInfo = new FavoriteAppInfo(context , title , intent.getComponent(), icon , dimension);
		favoriteInfo.addMeToFavorite(adapter , false);
	}
}
