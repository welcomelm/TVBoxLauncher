package ca.welcomelm.tvboxlauncher;

import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
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
import android.net.Uri;
import android.provider.Settings;
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

    public CharSequence getTitle() {
		return title;
	}

	/**
     * The application icon.
     */
	protected Drawable icon;
	
	static protected Point dimension;
	
	static protected MainActivity context;
	
	static double zoomNormal = 4.0 / 5;
	static int zoomSeleted = 10;
    
    protected AppInfo(CharSequence title, ComponentName componentName, 
    					Drawable icon) {
		super();
		this.title = title;
		this.intent = setLauncherMainActivity(componentName, 
				Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		this.icon = icon;
	}
    
	public void scaleIcon(Point dimen){
		
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
		
		TextView tv = (TextView) ll.getTag();
		
		if (tv == null) {
			tv = (TextView) ll.findViewById(R.id.tvAppTitle);
			ll.setTag(tv);
		}
		
		if (selected == AppAdapter.currentSelected) {
			resizeView(tv, (int)(dimension.x - zoomSeleted), (int)(dimension.y - zoomSeleted));
		} else if (selected == AppAdapter.lastSelected){
			resizeView(tv, (int)(dimension.x * zoomNormal), (int)(dimension.y * zoomNormal));
		}else{
			tv.getLayoutParams().width = (int)(dimension.x * zoomNormal);
			tv.getLayoutParams().height = (int)(dimension.y * zoomNormal);
		}
		
		tv.setText(title);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension.x/10);
		tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
		tv.setBackgroundResource(AppStyle.getCurrentStyle().getImageId(AppStyle.small_app_background));
		tv.setTextColor(context.getResources().getColor(
				AppStyle.getCurrentStyle().getTextColor(AppStyle.appTextColor)));
		tv.setPadding(dimension.x/20, dimension.x/20, dimension.x/20, dimension.x/20);
		tv.getPaint().setFakeBoldText(true);
    }
    
    protected void resizeView(View view , int width, int Height){
    	ResizeAnimation anim = new ResizeAnimation(view, width, Height);
		anim.setDuration(250);
		view.startAnimation(anim);
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

	public static void loadApplications(AppAdapter<AppInfo> adapter) {
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
                
                AppInfo application = new AppInfo(info.loadLabel(manager) ,
                		new ComponentName(info.activityInfo.applicationInfo.packageName, 
                				info.activityInfo.name), 
                				loadFullResIcon(info.activityInfo.applicationInfo, manager));
                
                application.scaleIcon(new Point(dimension.y / 2, dimension.y / 2));

                adapter.add(application);
            }
        }
	}
	
	protected static Drawable loadFullResIcon(ApplicationInfo appInfo, PackageManager manager){
		try {
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
		
		return appInfo.loadIcon(manager);
	}

	public void excute() {
		// TODO Auto-generated method stub
		context.startActivity(intent);
	}

	public void addMeToFavorite(AppAdapter<FavoriteAppInfo> adapter) {
		// TODO Auto-generated method stub
		FavoriteAppInfo favoriteInfo = new FavoriteAppInfo(title , intent.getComponent(), icon);
		favoriteInfo.addMeToFavorite(adapter , false);
	}

	public static void setDimension(Point dimension) {
		AppInfo.dimension = dimension;
	}

	public static void setContext(MainActivity context) {
		AppInfo.context = context;
	}

	public static void removePkg(AppAdapter adapter,String pkgName) {
		// TODO Auto-generated method stub
		for (int pos = 0; pos < adapter.getCount(); pos++) {
			AppInfo info = (AppInfo)adapter.getItem(pos);
			if (info.intent.getComponent().getPackageName().equals(pkgName)) {
				adapter.remove(info);
			}					
		}
	}
	
	public void uninstall(){
		Intent uninstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
		uninstall.setData(Uri.parse("package:"+intent.getComponent().getPackageName()));
		context.startActivity(uninstall);
	}
	
	public void showDetails(){
		Intent details = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		details.setData(Uri.parse("package:"+intent.getComponent().getPackageName()));
		context.startActivity(details);
	}
}
