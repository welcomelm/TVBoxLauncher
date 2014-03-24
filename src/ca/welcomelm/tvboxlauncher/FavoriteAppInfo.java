package ca.welcomelm.tvboxlauncher;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class FavoriteAppInfo extends AppInfo {

	public static final int USE_DEFAULT_ICON = 0x1 << 0;
	
	public static final int USE_CUSTOM_BACKGROUND = 0x1 << 1;
	
	public static final int USE_CUSTOM_ICON = 0x1 << 2;
	
	protected Drawable background = null;
    
	protected Drawable customIcon = null;
    
	protected int state = USE_DEFAULT_ICON;
    
    public FavoriteAppInfo(CharSequence title, ComponentName componentName, Drawable icon) {
		super(title , componentName, icon);
	}
    
    @Override
    public void SetMeOnTextView(TextView tv, Point dimension) {
    	// TODO Auto-generated method stub
    	tv.setWidth(dimension.x);
		tv.setHeight(dimension.y);
		tv.setPadding(0, dimension.x/10, 0, dimension.x/10);
		switch (state) {
			case USE_DEFAULT_ICON:
				tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
				break;
				
			case USE_CUSTOM_ICON:
				tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				tv.setBackgroundDrawable(customIcon);
				break;
				
			case USE_CUSTOM_BACKGROUND:
				tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
				tv.setBackgroundDrawable(background);
				break;
	
			default:
				break;
		}
    }

	public Drawable getBackground() {
		return background;
	}

	public void setBackground(Drawable background) {
		this.background = background;
	}

	public Drawable getCustomIcon() {
		return customIcon;
	}

	public void setCustomIcon(Drawable customIcon) {
		this.customIcon = customIcon;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
