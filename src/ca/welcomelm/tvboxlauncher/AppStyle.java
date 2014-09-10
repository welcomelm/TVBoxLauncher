package ca.welcomelm.tvboxlauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import android.R.anim;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;

public class AppStyle {
	
	static private String PROP_NAME = "properties.xml";
	
	static final private String[] supportedStyles;
	static final public int blue = 0;
	static final public int black = 1;
	static final public int white = 2;
	static final public int orange = 3;
	static final public int green = 4;
	static final private HashMap<String, Integer[]> textColorMap, soundsMap;
	static final private String[] supportedComponents = {"disconnect" , "ethernet" , "large_app_background" ,
										"large_selector" , "menu_button_layer" , "popup_menu_button_layer" ,
										"mobile" , "small_app_background" , "small_selector" , "wifi" , "toast" ,
										"wallpaper"};
	
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
	static final public int toast = 10;
	static final public int wallpaper = 11;
	
	static final public int timeTextColor = 0;
	static final public int appTextColor = 1;
	
	static final public int selectedSound = 0;
	static final public int pressedSound = 1;
	
	static private MainActivity context;
	static private SoundPool soundPool;
	
	static private AppStyle currentAppStyle;
	static private String currentStyle;
	private int[] styleImageIds;
	private Integer textColor[];
	private int sounds[];
	
	static{
		supportedStyles = new String[]{"blue" , "black" , "white" , "orange" , "green"};
		currentStyle = "blue";
		textColorMap = new HashMap<String, Integer[]>();
		textColorMap.put(supportedStyles[blue], new Integer[]{android.R.color.holo_blue_dark , android.R.color.black});
		textColorMap.put(supportedStyles[black], new Integer[]{android.R.color.black , android.R.color.white});
		textColorMap.put(supportedStyles[white], new Integer[]{android.R.color.white , android.R.color.black});
		textColorMap.put(supportedStyles[orange], new Integer[]{android.R.color.holo_orange_dark , android.R.color.black});
		textColorMap.put(supportedStyles[green], new Integer[]{R.color.lawngreen , android.R.color.black});
		
		soundsMap = new HashMap<String, Integer[]>();
		soundsMap.put(supportedStyles[blue], new Integer[]{R.raw.button_selected_blue , R.raw.button_pressed_blue});
		soundsMap.put(supportedStyles[black], new Integer[]{R.raw.button_selected_black , R.raw.button_pressed_black});
		soundsMap.put(supportedStyles[white], new Integer[]{R.raw.button_selected_white , R.raw.button_pressed_white});
		soundsMap.put(supportedStyles[orange], new Integer[]{R.raw.button_selected_orange , R.raw.button_pressed_orange});
		soundsMap.put(supportedStyles[green], new Integer[]{R.raw.button_selected_green , R.raw.button_pressed_green});
	}
	
	private AppStyle(String style){
		styleImageIds = new int[supportedComponents.length];
		textColor = textColorMap.get(style);
		
		Integer[] soundIds = soundsMap.get(style);
		sounds = new int[soundIds.length];
		soundPool = new SoundPool(soundIds.length, AudioManager.STREAM_MUSIC, 0);
		
		for (int i = 0; i < soundIds.length; i++) {
			sounds[i] = soundPool.load(context, soundIds[i], 1);
		}
		
		for (int i = 0; i < supportedComponents.length; i++) {
			
			int resId = context.getResources().getIdentifier(supportedComponents[i] + '_' + style, 
															"drawable", context.getPackageName());
			
			styleImageIds[i] = resId;
		}
	}
	
	public static AppStyle getCurrentStyle(){
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
	
	static public void playSound(int id) {
		
		if (id < 0 || id >= currentAppStyle.sounds.length) {
			return;
		}
		
		soundPool.play(currentAppStyle.sounds[id], 1, 1, 0, 0, 1f);
	}
	
	public static void chooseStyle(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
		builder.setTitle("Pick your style color").setItems(supportedStyles, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (supportedStyles[which].equals(currentStyle)) {
					return;
				}
				currentStyle = supportedStyles[which];
				soundPool.release();
				currentAppStyle = new AppStyle(supportedStyles[which]);
				context.loadStyle();
				saveStyle(supportedStyles[which]);
			}
		}).show();		
	}
	
	static public void init(MainActivity context){
		
		AppStyle.context = context;
		
		File propXMLFile = new File(context.getFilesDir(), PROP_NAME);

		Properties prop = new Properties();
		try {
			prop.loadFromXML(new FileInputStream(propXMLFile));
			currentStyle = prop.getProperty("style");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		currentAppStyle = new AppStyle(currentStyle);
	}
	
	static private void saveStyle(String style){
		File propXMLFile = new File(context.getFilesDir(), PROP_NAME);
		
		Properties prop = new Properties();

		try {
			prop.loadFromXML(new FileInputStream(propXMLFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		
		prop.setProperty("style", style);
		
		try {
			prop.storeToXML(new FileOutputStream(propXMLFile), null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
