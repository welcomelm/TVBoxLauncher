package ca.welcomelm.tvboxlauncher;

import java.io.FileNotFoundException;

import android.R.bool;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
	
	private static final int USE_DEFAULT_ICON = 0x0;
	private static final int USE_CUSTOM_BACKGROUND = 0x1;
	private static final int USE_CUSTOM_ICON = 0x2;
	
	static private FavoriteDatabase favoriteDb;
	
	private int state;
	private String backgroundUri;
	private String customIconUri;
	
	private Drawable background;
    
	private Drawable customIcon;
	
	static protected Point dimension;
    
	public static Point getDimension() {
		return dimension;
	}

	public static void setDimension(Point dimension) {
		FavoriteAppInfo.dimension = dimension;
	}

	protected FavoriteAppInfo(CharSequence title, ComponentName componentName, Drawable icon) {
		super(title , componentName, icon);
		this.state = USE_DEFAULT_ICON;
		this.backgroundUri = "";
		this.customIconUri = "";
		this.background = null;
		this.customIcon = null;
	}

	@Override
	public void SetMeOnTextView(View ll , int selected) {
		// TODO Auto-generated method stub
		ll.getLayoutParams().width = dimension.x;
		ll.getLayoutParams().height = dimension.y;
		ImageView iv = (ImageView) ll.findViewById(R.id.ivFavorite);

		if (selected == AppAdapter.currentSelected) {
			resizeView(iv, dimension.x - 20, dimension.y - 20);
		} else if (selected == AppAdapter.lastSelected){
			resizeView(iv, dimension.x * 4 / 5, dimension.y * 4 / 5);
		}else{
			iv.getLayoutParams().width = dimension.x * 4 / 5;
			iv.getLayoutParams().height = dimension.y * 4 / 5;
		}
		
		iv.setPadding(0, dimension.x / 30, 0, dimension.x / 30);
		
		switch (state) {
		case USE_DEFAULT_ICON:
			iv.setImageDrawable(icon);
			iv.setBackgroundResource(R.drawable.app_big_background);
			break;
		case USE_CUSTOM_ICON:
			iv.setImageDrawable(null);
			iv.setBackgroundDrawable(customIcon);
			break;
		case USE_CUSTOM_BACKGROUND:
			iv.setImageDrawable(icon);
			iv.setBackgroundDrawable(background);
			break;
		default:
			break;
		}
	}
	
	public void addMeToFavorite(AppAdapter<FavoriteAppInfo> adapter, Boolean ordered) {
		
		if( favoriteDb.addFavorite(this , ordered) && !ordered ){
			//this.scaleIcon(context, new Point(dimension.y * 2 / 3, dimension.y * 2 /3));
			PackageManager manager = context.getPackageManager();
			
			ResolveInfo info = manager.resolveActivity(intent, 0);
			
			icon = loadFullResIcon(info.activityInfo.applicationInfo, manager);
			
			adapter.add(this);
		}
		
	}
	
	public void removeMeFromFavorite(AppAdapter<FavoriteAppInfo> adapter , Boolean ordered){
		
		if (!ordered) {
			adapter.remove(this);
		}
		
		favoriteDb.removeFavorite(this , ordered);
		
	}

	public void changeCustomIcon(Uri uri, AppAdapter<FavoriteAppInfo> adapter) {
		// TODO Auto-generated method stub
		try {
			Drawable customIcon = Drawable.createFromStream(context.getContentResolver().openInputStream(uri), 
															uri.toString());
			
			if (customIcon != null) {
				this.customIcon = customIcon;
				state = USE_CUSTOM_ICON;
				customIconUri = uri.toString();
				removeMeFromFavorite(adapter, true);
				addMeToFavorite(adapter , true);
				adapter.notifyDataSetChanged();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeCustomBackground(Uri uri, AppAdapter<FavoriteAppInfo> adapter) {
		// TODO Auto-generated method stub
		try {
			Drawable background = Drawable.createFromStream(context.getContentResolver().
										openInputStream(uri), uri.toString());
			
			if (background != null) {
				this.background = background;
				state = USE_CUSTOM_BACKGROUND;
				backgroundUri = uri.toString();
				removeMeFromFavorite(adapter, true);
				addMeToFavorite(adapter , true);
				adapter.notifyDataSetChanged();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void loadFavorites(AppAdapter<FavoriteAppInfo> adapter){
		FavoriteDatabase.loadFavorites(adapter);
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
			
			String component = favoriteAppInfo.intent.getComponent().flattenToString();
			
			// TODO Auto-generated method stub
			if (ordered) {
				SQLiteDatabase dbRead = this.getReadableDatabase();
				
				String[] selectionArgs = {component};
				
				Cursor cursor = dbRead.query(tableName, null, names[componentName] + "=?", 
											selectionArgs, null, null, null);
				
				if (cursor.moveToNext()) {
					lastOrder = cursor.getInt(cursor.getColumnIndex(names[order]));
				}
				
				dbRead.close();
			}
			
			SQLiteDatabase dbWrite = this.getWritableDatabase();
			
			String[] whereArgs = {component};
			
			dbWrite.delete(tableName, names[componentName] + "=?", whereArgs);
			
			dbWrite.close();
		}

		public boolean addFavorite(FavoriteAppInfo favoriteAppInfo , Boolean ordered) {
			// TODO Auto-generated method stub
			SQLiteDatabase dbRead = this.getReadableDatabase();
			
			String[] selectionArgs = {favoriteAppInfo.intent.getComponent().flattenToString()};
			
			Cursor cursor = dbRead.query(tableName, null, names[componentName] + "=?", 
										selectionArgs, null, null, null);
			
			if (cursor.getCount() != 0) {
				dbRead.close();
				return false;
			}
			
			SQLiteDatabase dbWrite = this.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			
			values.put(names[componentName], favoriteAppInfo.intent.getComponent().flattenToString());
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
			sqlCommand.append(names[state] + " INTEGER DEFAULT 0,");
			sqlCommand.append(names[customIconUri] + " TEXT DEFAULT NONE,");
			sqlCommand.append(names[backgroundUri] + " TEXT DEFAULT NONE,");
			sqlCommand.append(names[order] + " INTEGER DEFAULT 0)");

			db.execSQL(sqlCommand.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
		
		public static void loadFavorites(AppAdapter<FavoriteAppInfo> adapter) {
			// TODO Auto-generated method stub
			if (favoriteDb == null) {
				favoriteDb = new FavoriteDatabase(context);
			}
			
			SQLiteDatabase dbRead = favoriteDb.getReadableDatabase();
			
			Cursor cursor = dbRead.query(tableName, null, null, null, null, null, "appOrder ASC");
			
			PackageManager manager = context.getPackageManager();
			
			adapter.clear();
			
			while(cursor.moveToNext()){
				
				String component = cursor.getString(cursor.getColumnIndex(names[componentName]));
				
				Intent intent = new Intent(Intent.ACTION_MAIN);
		        intent.addCategory(Intent.CATEGORY_LAUNCHER);
		        intent.setComponent(ComponentName.unflattenFromString(component));
		        
		        ResolveInfo info = manager.resolveActivity(intent, 0);
		        
		        if (info != null) {
	                FavoriteAppInfo application = new FavoriteAppInfo(info.loadLabel(manager) ,
	                		new ComponentName(info.activityInfo.applicationInfo.packageName, 
	                				info.activityInfo.name), 
	                				loadFullResIcon(info.activityInfo.applicationInfo, manager));
	                
	                application.state = cursor.getInt(cursor.getColumnIndex(names[state]));
	                
	                switch (application.state) {
					case USE_CUSTOM_ICON:
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
						break;
					case USE_CUSTOM_BACKGROUND:
	                	try{
	                		application.backgroundUri = cursor.getString(cursor.getColumnIndex(names[backgroundUri]));
		                	Uri uri = Uri.parse(application.backgroundUri);
		                	Drawable background = Drawable.createFromStream(
		                			context.getContentResolver().openInputStream(uri), 
									uri.toString());
		                	if (background != null) {
		                		application.background = background;
		                	} else {
		                		application.state = USE_DEFAULT_ICON;
		                	}
	                	}catch(Exception e){
	                		
	                	}						
						break;

					default:
						break;
					}
	                
	                dbRead.close();
	                
	                //application.scaleIcon(context, new Point(dimension.y * 2 / 3, dimension.y * 2 /3));

	                adapter.add(application);
				}
			}
		}
	}
}
