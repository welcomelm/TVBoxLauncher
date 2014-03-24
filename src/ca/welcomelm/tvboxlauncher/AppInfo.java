package ca.welcomelm.tvboxlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
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
    
    public AppInfo(CharSequence title, ComponentName componentName, Drawable icon) {
		super();
		this.title = title;
		this.intent = setLauncherMainActivity(componentName, 
				Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		this.icon = icon;
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
    
    public void SetMeOnTextView(TextView tv , Point dimension){
    	tv.setWidth(dimension.x);
		tv.setHeight(dimension.y);
		tv.setText(title);
		tv.setPadding(0, dimension.x/15, 0, dimension.x/15);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension.x/10);
		tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);	
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
}
