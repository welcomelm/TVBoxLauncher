package ca.welcomelm.tvboxlauncher;

import ca.welcomelm.tvboxlauncher.R.style;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

public class CustomPopupMenu extends PopupWindow {
	
	private MainActivity context;
	private View mainView;
	
	public CustomPopupMenu(MainActivity context , int layoutId){
		super(LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT);
		View view = LayoutInflater.from(context).inflate(layoutId, null);
		setContentView(view);
		setFocusable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new ColorDrawable(0xb0000000));
		setAnimationStyle(R.style.PopupAnimation);
		this.mainView = view;
		this.context = context;
	}
	
	public void setup(Integer... idArray){
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		if (metrics.widthPixels > 1280) {
			metrics.widthPixels = 1920;
		}else {
			metrics.widthPixels = 1280;
		}
		
		if (metrics.heightPixels > 720) {
			metrics.heightPixels = 1080;
		}else{
			metrics.heightPixels = 720;
		}
		
		mainView.setPadding(metrics.widthPixels * 7 / 18, metrics.heightPixels / 4, 
							metrics.widthPixels * 7 / 18, metrics.heightPixels / 4);
		
		for (int i = 0; i < idArray.length; i++) {
			Button btn = (Button) mainView.findViewById(idArray[i]);
			btn.setPadding(metrics.widthPixels/96, 0, metrics.widthPixels/96, 0);
			btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/50);
			btn.setOnClickListener(context);
		}
	}
	
	public void setupBtnBackground(Integer... idArray){
		for (int i = 0; i < idArray.length; i++) {
			Button btn = (Button) mainView.findViewById(idArray[i]);
			btn.setBackgroundResource(AppStyle.getCurrentStyle(context).getImageId(AppStyle.popup_menu_button_layer));
		}		
	}
}
