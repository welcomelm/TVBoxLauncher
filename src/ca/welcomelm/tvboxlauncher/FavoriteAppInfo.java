package ca.welcomelm.tvboxlauncher;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.TextView;

public class FavoriteAppInfo extends AppInfo {
	
	private static final String USE_DEFAULT_ICON = "USE_DEFAULT_ICON";
	private static final String USE_CUSTOM_BACKGROUND = "USE_CUSTOM_BACKGROUND";
	private static final String USE_CUSTOM_ICON = "USE_CUSTOM_ICON";
	
	public String state;
	public String backgroundUri;
	public String customIconUri;
	public String componentName;
	
	protected Drawable background = null;
    
	protected Drawable customIcon = null;
    
    protected FavoriteAppInfo(CharSequence title, ComponentName componentName, Drawable icon, Point dimension) {
		super(title , componentName, icon, dimension);
		this.state = USE_DEFAULT_ICON;
		this.backgroundUri = null;
		this.customIconUri = null;
		this.componentName = componentName.flattenToString();
	}
    
    protected FavoriteAppInfo(AppInfo info) {
    	super(info);
		this.state = USE_DEFAULT_ICON;
		this.backgroundUri = null;
		this.customIconUri = null;
		this.componentName = intent.getComponent().flattenToString();
	}
    
    public static FavoriteAppInfo from (AppInfo info){
    	return new FavoriteAppInfo(info);
    }
    
    @Override
    public void SetMeOnTextView(TextView tv) {
    	// TODO Auto-generated method stub
    	tv.setWidth(dimension.x);
		tv.setHeight(dimension.y);
		tv.setPadding(0, dimension.x/10, 0, dimension.x/10);
		
		if (state.equals(USE_DEFAULT_ICON)) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
		}else if (state.equals(USE_CUSTOM_ICON)) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			tv.setBackgroundDrawable(customIcon);
		}else if (state.equals(USE_CUSTOM_BACKGROUND)) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
			tv.setBackgroundDrawable(background);
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
	
	public void addMeToFavorite(Context context) {
		File dir = new File(Environment.getExternalStorageDirectory(), 
				context.getApplicationInfo().packageName);
		
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		File file = new File(dir , intent.getComponent().getClassName());
		
		if (file.exists()){
			return;
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			bw.write(componentName);
			bw.newLine();
			bw.write(state);
			bw.newLine();
			if (state.equals(USE_CUSTOM_ICON)) {
				bw.write(customIconUri);
				bw.newLine();
			}else if (state.equals(USE_CUSTOM_BACKGROUND)) {
				bw.write(backgroundUri);
				bw.newLine();
			}	
			bw.flush();
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeMeFromFavorite(Context context){

		File dir = new File(Environment.getExternalStorageDirectory(), 
				context.getApplicationInfo().packageName);
		
		if (!dir.exists()) {
			return;
		}
		
		File file = new File(dir , intent.getComponent().getClassName());
		
		if (file.exists()){
			file.delete();
		}
	}

	public static void loadFavorites(Context context, AppAdapter adapter, Point dimension) {
		// TODO Auto-generated method stub
		File dir = new File(Environment.getExternalStorageDirectory(), 
				context.getApplicationInfo().packageName);
		
		if (!dir.exists()) {
			return;
		}
		
		File[] files = dir.listFiles();
		
		PackageManager manager = context.getPackageManager();
		
		adapter.clear();
		
		for(File file : files){
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				
				String componentName = br.readLine();
				
				Intent intent = new Intent(Intent.ACTION_MAIN);
		        intent.addCategory(Intent.CATEGORY_LAUNCHER);
		        intent.setComponent(ComponentName.unflattenFromString(componentName));
		        
		        ResolveInfo info = manager.resolveActivity(intent, 0);
		        
		        if (info != null) {
	                FavoriteAppInfo application = new FavoriteAppInfo(info.loadLabel(manager) ,
	                		new ComponentName(info.activityInfo.applicationInfo.packageName, 
	                				info.activityInfo.name), 
	                				info.loadIcon(manager),
	                				dimension);
	                
	                application.state = br.readLine();
	                
	                if (application.state.equals(USE_CUSTOM_ICON)) {
	                	application.customIconUri = br.readLine();
	                	application.changeCustomIcon(context, Uri.parse(application.customIconUri));
					}else if (application.state.equals(USE_CUSTOM_BACKGROUND)) {
						application.backgroundUri = br.readLine();
					}
	                
	                br.close();
	                
	                application.componentName = componentName;
	                
	                application.scaleIcon(context, new Point(dimension.y * 4 / 5, dimension.y * 4 / 5));

	                adapter.add(application);
				}
				
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void changeCustomIcon(Context context, Uri uri) {
		// TODO Auto-generated method stub
		try {
			Drawable customIcon = Drawable.createFromStream(context.getContentResolver().openInputStream(uri), 
															uri.toString());
			
			if (customIcon != null) {
				this.customIcon = customIcon;
				state = USE_CUSTOM_ICON;
				customIconUri = uri.toString();
				this.removeMeFromFavorite(context);
				this.addMeToFavorite(context);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
