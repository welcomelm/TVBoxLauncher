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
import java.util.Arrays;
import java.util.Comparator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoriteAppInfo extends AppInfo {
	
	private static final String USE_DEFAULT_ICON = "USE_DEFAULT_ICON";
	private static final String USE_CUSTOM_BACKGROUND = "USE_CUSTOM_BACKGROUND";
	private static final String USE_CUSTOM_ICON = "USE_CUSTOM_ICON";
	
	private String state;
	private String backgroundUri;
	private String customIconUri;
	private String componentName;
	private long lastModifiedTime = 0;
	
	private Drawable background = null;
    
	private Drawable customIcon = null;
    
	private FavoriteAppInfo(CharSequence title, ComponentName componentName, Drawable icon, Point dimension) {
		super(title , componentName, icon, dimension);
		this.state = USE_DEFAULT_ICON;
		this.backgroundUri = null;
		this.customIconUri = null;
		this.componentName = componentName.flattenToString();
	}
    
    public static FavoriteAppInfo from (AppInfo info , Point dimension){
    	return new FavoriteAppInfo(info.title , info.intent.getComponent(), info.icon , dimension);
    }

	@Override
	public void SetMeOnTextView(View view) {
		// TODO Auto-generated method stub
		if (view instanceof ImageView) {
			ImageView iv = (ImageView) view;
			iv.getLayoutParams().width = dimension.x;
			iv.getLayoutParams().height = dimension.y;
			if (state.equals(USE_DEFAULT_ICON)) {
				iv.setImageDrawable(icon);
				iv.setBackgroundResource(R.drawable.app_big_background);
			}else if (state.equals(USE_CUSTOM_ICON)) {
				iv.setImageDrawable(null);
				iv.setBackgroundDrawable(customIcon);
			}else if (state.equals(USE_CUSTOM_BACKGROUND)) {
				iv.setImageDrawable(icon);
				iv.setBackgroundDrawable(background);
			}			
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
	
	public void addMeToFavorite(Context context , AppAdapter<FavoriteAppInfo> adapter, Boolean ordered) {
		
		File dir = new File(Environment.getExternalStorageDirectory(), 
				context.getApplicationInfo().packageName);
		
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		File file = new File(dir , intent.getComponent().getClassName());
		
		if (file.exists()){
			return;
		}
		
		if(!ordered){
			this.scaleIcon(context, new Point(dimension.y * 4 / 5, dimension.y * 4 / 5));
			adapter.add(this);
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
			
			if (ordered && lastModifiedTime != 0) {
				file.setLastModified(lastModifiedTime);
				lastModifiedTime = 0;
			}else{
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeMeFromFavorite(Context context , AppAdapter<FavoriteAppInfo> adapter , Boolean ordered){
		
		if(!ordered){
			adapter.remove(this);
		}

		File dir = new File(Environment.getExternalStorageDirectory(), 
				context.getApplicationInfo().packageName);
		
		if (!dir.exists()) {
			return;
		}
		
		File file = new File(dir , intent.getComponent().getClassName());
		
		if (file.exists()){
			lastModifiedTime = file.lastModified();
			file.delete();
		}
	}

	public static void loadFavorites(Context context, AppAdapter<FavoriteAppInfo> adapter, Point dimension) {
		// TODO Auto-generated method stub
		File dir = new File(Environment.getExternalStorageDirectory(), 
				context.getApplicationInfo().packageName);
		
		if (!dir.exists()) {
			return;
		}
		
		File[] files = dir.listFiles();
		
		Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return (int)(file1.lastModified() - file2.lastModified());
            }
        });
		
		PackageManager manager = context.getPackageManager();
		
		adapter.clear();
		
		for(File file : files){
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				
				String componentName = br.readLine();
				
				System.out.println(file.lastModified());
				
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
	                	Uri uri = Uri.parse(application.customIconUri);
	                	Drawable customIcon = Drawable.createFromStream(
	                			context.getContentResolver().openInputStream(uri), 
								uri.toString());
	                	if (customIcon != null) {
	                		application.customIcon = customIcon;
	                	} else {
	                		application.state = USE_DEFAULT_ICON;
	                	}
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

	public void changeCustomIcon(Context context, Uri uri, AppAdapter<FavoriteAppInfo> adapter) {
		// TODO Auto-generated method stub
		try {
			Drawable customIcon = Drawable.createFromStream(context.getContentResolver().openInputStream(uri), 
															uri.toString());
			
			if (customIcon != null) {
				this.customIcon = customIcon;
				state = USE_CUSTOM_ICON;
				customIconUri = uri.toString();
				removeMeFromFavorite(context , adapter, true);
				addMeToFavorite(context , adapter , true);
				adapter.notifyDataSetChanged();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
