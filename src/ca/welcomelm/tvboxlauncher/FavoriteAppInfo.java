package ca.welcomelm.tvboxlauncher;

import java.io.FileNotFoundException;

import android.R.bool;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FavoriteAppInfo extends AppInfo {
	
	private static final String USE_DEFAULT_ICON = "USE_DEFAULT_ICON";
	private static final String USE_CUSTOM_BACKGROUND = "USE_CUSTOM_BACKGROUND";
	private static final String USE_CUSTOM_ICON = "USE_CUSTOM_ICON";
	
	private String state;
	private String backgroundUri;
	private String customIconUri;
	private String componentName;
	
	private Drawable background;
    
	private Drawable customIcon;
	
	static private FavoriteDatabase favoriteDb;
    
	private FavoriteAppInfo(CharSequence title, ComponentName componentName, Drawable icon, 
			Point dimension) {
		super(title , componentName, icon, dimension);
		this.state = USE_DEFAULT_ICON;
		this.backgroundUri = "";
		this.customIconUri = "";
		this.background = null;
		this.customIcon = null;
		this.componentName = componentName.flattenToString();
	}
    
    public static FavoriteAppInfo from (AppInfo info , Point dimension){
    	return new FavoriteAppInfo(info.title , info.intent.getComponent(), info.icon , dimension);
    }
    
    public static void setDb(FavoriteDatabase db){
    	favoriteDb = db;
    }

	@Override
	public void SetMeOnTextView(View ll , Boolean selected) {
		// TODO Auto-generated method stub
		ll.getLayoutParams().width = dimension.x;
		ll.getLayoutParams().height = dimension.y;
		ImageView iv = (ImageView) ll.findViewById(R.id.tvAppTitle);
		ResizeAnimation anim;
		if (selected) {
//	    	tv.setWidth(dimension.x - 5);
//			tv.setHeight(dimension.y - 5);
			anim = new ResizeAnimation(iv, dimension.x - 20, dimension.y - 20);
		}else{
//	    	iv.getLayoutParams().width = (dimension.x * 4 / 5);
//	    	iv.getLayoutParams().height =(dimension.y * 4 / 5);
			anim = new ResizeAnimation(iv, dimension.x *4 / 5, dimension.y * 4 / 5);
		}
		anim.setDuration(500);
		iv.startAnimation(anim);
		
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
	
	public void addMeToFavorite(Context context , AppAdapter<FavoriteAppInfo> adapter, Boolean ordered) {
		
		if( favoriteDb.addFavorite(this , ordered) && !ordered ){
			this.scaleIcon(context, new Point(dimension.y * 2 / 3, dimension.y * 2 /3));
			adapter.add(this);
		}
		
	}
	
	public void removeMeFromFavorite(AppAdapter<FavoriteAppInfo> adapter , Boolean ordered){
		
		if (!ordered) {
			adapter.remove(this);
		}
		
		favoriteDb.removeFavorite(this , ordered);
		
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
				removeMeFromFavorite(adapter, true);
				addMeToFavorite(context , adapter , true);
				adapter.notifyDataSetChanged();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadFavorites(Context context, AppAdapter<FavoriteAppInfo> adapter, Point dimension){
		FavoriteDatabase.loadFavorites(context, adapter, dimension);
	}
	
	public static class FavoriteDatabase extends SQLiteOpenHelper {
		
		static final private String tableName = "favorites";
		static final private String names[] = {"componentName" , "state" , "customIconUri" , 
												"backgroundUri" , "appOrder"};
		static final private int componentName = 0;
		static final private int state = 1;
		static final private int customIconUri = 2;
		static final private int backgroundUri = 3;
		static final private int order = 4;
		
		private int lastOrder;

		public FavoriteDatabase(Context context) {
			super(context, context.getPackageName(), null, 1);
		}
		
		public void removeFavorite(FavoriteAppInfo favoriteAppInfo , Boolean ordered) {
			// TODO Auto-generated method stub
			if (ordered) {
				SQLiteDatabase dbRead = this.getReadableDatabase();
				
				String[] selectionArgs = {favoriteAppInfo.componentName};
				
				Cursor cursor = dbRead.query(tableName, null, names[componentName] + "=?", 
											selectionArgs, null, null, null);
				
				if (cursor.moveToNext()) {
					lastOrder = cursor.getInt(cursor.getColumnIndex(names[order]));
				}
				
				dbRead.close();
			}
			
			SQLiteDatabase dbWrite = this.getWritableDatabase();
			
			String[] whereArgs = {favoriteAppInfo.componentName};
			
			dbWrite.delete(tableName, names[componentName] + "=?", whereArgs);
			
			dbWrite.close();
		}

		public boolean addFavorite(FavoriteAppInfo favoriteAppInfo , Boolean ordered) {
			// TODO Auto-generated method stub
			SQLiteDatabase dbRead = this.getReadableDatabase();
			
			String[] selectionArgs = {favoriteAppInfo.componentName};
			
			Cursor cursor = dbRead.query(tableName, null, names[componentName] + "=?", 
										selectionArgs, null, null, null);
			
			if (cursor.getCount() != 0) {
				dbRead.close();
				return false;
			}
			
			SQLiteDatabase dbWrite = this.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			
			values.put(names[componentName], favoriteAppInfo.componentName);
			values.put(names[state],         favoriteAppInfo.state);
			values.put(names[customIconUri], favoriteAppInfo.customIconUri);
			values.put(names[backgroundUri], favoriteAppInfo.backgroundUri);
			if (ordered && lastOrder != 0) {
				values.put(names[order], lastOrder);
			}else{
				cursor = dbRead.query(tableName, null, null, null, null, null, null);
				values.put(names[order], cursor.getCount() + 1);
			}
			
			dbWrite.insert(tableName, null, values);
			
			dbWrite.close();
			dbRead.close();
			
			return true;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			StringBuilder sqlCommand = new StringBuilder();
			sqlCommand.append("CREATE TABLE " + tableName + "(");
			
			sqlCommand.append(names[componentName] + " TEXT DEFAULT NONE,");
			sqlCommand.append(names[state] + " TEXT DEFAULT NONE,");
			sqlCommand.append(names[customIconUri] + " TEXT DEFAULT NONE,");
			sqlCommand.append(names[backgroundUri] + " TEXT DEFAULT NONE,");
			sqlCommand.append(names[order] + " INTEGER DEFAULT 0)");

			db.execSQL(sqlCommand.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
		
		public static void loadFavorites(Context context, AppAdapter<FavoriteAppInfo> adapter, Point dimension) {
			// TODO Auto-generated method stub
			SQLiteDatabase dbRead = favoriteDb.getReadableDatabase();
			
			Cursor cursor = dbRead.query(tableName, null, null, null, null, null, "appOrder ASC");
			
			PackageManager manager = context.getPackageManager();
			
			adapter.clear();
			
			while(cursor.moveToNext()){
				
				String name = cursor.getString(cursor.getColumnIndex(names[componentName]));
				
				Intent intent = new Intent(Intent.ACTION_MAIN);
		        intent.addCategory(Intent.CATEGORY_LAUNCHER);
		        intent.setComponent(ComponentName.unflattenFromString(name));
		        
		        ResolveInfo info = manager.resolveActivity(intent, 0);
		        
		        if (info != null) {
	                FavoriteAppInfo application = new FavoriteAppInfo(info.loadLabel(manager) ,
	                		new ComponentName(info.activityInfo.applicationInfo.packageName, 
	                				info.activityInfo.name), 
	                				loadFullResIcon(info, manager),
	                				dimension);
	                
	                application.state = cursor.getString(cursor.getColumnIndex(names[state]));
	                
	                if (application.state.equals(USE_CUSTOM_ICON)) {
	                	try{
		                	application.customIconUri = cursor.getString(cursor.getColumnIndex(names[customIconUri]));
		                	Uri uri = Uri.parse(application.customIconUri);
		                	Drawable customIcon = Drawable.createFromStream(
		                			context.getContentResolver().openInputStream(uri), 
									uri.toString());
		                	if (customIcon != null) {
		                		application.customIcon = customIcon;
		                	} else {
		                		application.state = USE_DEFAULT_ICON;
		                	}
	                	}catch(Exception e){
	                		
	                	}
					}else if (application.state.equals(USE_CUSTOM_BACKGROUND)) {
						application.backgroundUri = cursor.getString(cursor.getColumnIndex(names[backgroundUri]));
					}
	                
	                dbRead.close();
	                
	                application.componentName = name;
	                
	                application.scaleIcon(context, new Point(dimension.y * 2 / 3, dimension.y * 2 /3));

	                adapter.add(application);
				}
			}
		}
	}
}
