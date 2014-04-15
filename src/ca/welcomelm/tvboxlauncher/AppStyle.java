package ca.welcomelm.tvboxlauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import android.R.anim;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class AppStyle {
	
	static final private String[] supportedStyles = {"blue" , "black"};
	static final public int blue = 0;
	static final public int black = 1;
	static final private HashMap<String, Integer[]> textColorMap;
	static final private String[] supportedComponents = {"disconnect" , "ethernet" , "large_app_background" ,
										"large_selector" , "menu_button_layer" , "popup_menu_button_layer" ,
										"mobile" , "small_app_background" , "small_selector" , "wifi"};
	static final public int disconnect = 0;
	static final public int ethernet = 1;
	static final public int large_app_background = 2;
	static final public int large_selector = 3;
	static final public int menu_button_layer = 4;
	static final public int popup_menu_button_layer = 5;
	static final public int mobile = 6;
	static final public int small_app_background = 7;
	static final public int small_selector = 8;
	static final public int wifi = 9;
	
	static final public int timeTextColor = 0;
	static final public int appTextColor = 1;
	
	static private AppStyle currentAppStyle;
	static private String currentStyle = "blue";
	private int[] styleImageIds;
	private Integer textColor[];
	
	static{
		
	}
	
	static{
		textColorMap = new HashMap<String, Integer[]>();
		textColorMap.put(supportedStyles[blue], new Integer[]{android.R.color.holo_blue_dark , android.R.color.black});
		textColorMap.put(supportedStyles[black], new Integer[]{android.R.color.black , android.R.color.white});
	}
	
	private AppStyle(String style , MainActivity context){
		styleImageIds = new int[supportedComponents.length];
		textColor = textColorMap.get(style);
		
		for (int i = 0; i < supportedComponents.length; i++) {
			
			int resId = context.getResources().getIdentifier(supportedComponents[i] + '_' + style, 
															"drawable", context.getPackageName());
			
			styleImageIds[i] = resId;
		}
	}
	
	public static AppStyle getCurrentStyle(MainActivity context){
		
		if (currentAppStyle == null) {
			currentAppStyle = new AppStyle(currentStyle , context); 
		}
		
		return currentAppStyle;
	}
	
	public int getImageId(int id){
		
		if (id < 0 || id >= styleImageIds.length) {
			return 0;
		}
		return styleImageIds[id];
	}

	public int getTextColor(int id) {
		
		if (id < 0 || id >= textColor.length) {
			return 0;
		}
		return textColor[id];
	}
	
	public static void chooseStyle(MainActivity context){
		final MainActivity contextSaved = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
		builder.setTitle("Pick your style color").setItems(supportedStyles, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				currentStyle = supportedStyles[which];
				currentAppStyle = new AppStyle(currentStyle , contextSaved);
				contextSaved.loadStyle();
			}
		}).show();		
	}
	
	static private String loadCurrentStyle(){
		File dir = new File()
	}
}
