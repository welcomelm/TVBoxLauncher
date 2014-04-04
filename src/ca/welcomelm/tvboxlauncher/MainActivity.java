package ca.welcomelm.tvboxlauncher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, 
OnClickListener, OnItemSelectedListener, OnFocusChangeListener {
	
	private static final int requestWallpaper = 1;
	private static final int requestFavoriteIcon = 2;
	private static final int requestBackground = 3;
	
    private final String TIME_FORMAT = "h:mma";

	private GridView gvApp, gvShowApp;
	
	private AppAdapter<AppInfo> allAppAdapter;
	private AppAdapter<FavoriteAppInfo> favoriteAppAdapter;
	
	private Animation fade, scale;
	
	private BroadcastReceiver timeUpateReceiver, networkUpdateReceiver, appUpdateReceiver;
	
	private TextView tvTime;
	
	private DateFormat df;
	
	private ConnectivityManager cm;
	
	private ImageView ivNetwork;
	
	private Button btnMenu;
	
	private LinearLayout llBtnMenu , llNetAndTime, llMain; 
	
	private Point gvAppCellDimension, gvShowAppCellDimension;
	
	private CustomPopupMenu mainPopupMenu, appPopupMenu;

	private int appPopIndex;
	
	private FavoriteAppInfo.FavoriteDatabase favoriteDatabase;
	private Typeface appTypeface;
	
	public static View currentSelectedGridView , lastSelectedGridView;
	
	private ViewSwitcher vsGridView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//startSplash();
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		findViews();
		
		setDimension();
		
		setMis();
		
		popupInit();
		
		updateTime();
		
		updateNetworks();
		
		registerIntentReceivers();
		
		bindListeners();
		
		loadFavorites();
		
		loadApplications();
		
		loadAnimations();	
	}
	
	private void setMis() {
		// TODO Auto-generated method stub
		//appTypeface = Typeface.createFromAsset(getAssets(),"fonts/apps.TTF");
		favoriteDatabase = new FavoriteAppInfo.FavoriteDatabase(this);
		FavoriteAppInfo.setDb(favoriteDatabase);
		AppInfo.setFont(appTypeface);
	}

	private void popupInit() {
		// TODO Auto-generated method stub
		mainPopupMenu = new CustomPopupMenu(this, R.layout.main_popup_menu);
		appPopupMenu = new CustomPopupMenu(this, R.layout.app_popup_menu);
		
		mainPopupMenu.setup(R.id.menuBtnApps , R.id.menuBtnSettings , R.id.menuBtnWallpaper);
		appPopupMenu.setup(R.id.menuBtnExcute , R.id.menuBtnRemove , 
							R.id.menuBtnChangeIcon , R.id.menuBtnChangeBackground);
	}

	private void startSplash() {
		// TODO Auto-generated method stub
		Thread splashThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(), SplashScreen.class));
			}
		});
		
		splashThread.setName("SplashScreen");
		splashThread.start();
	}

	private void setDimension() {
		// TODO Auto-generated method stub
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
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
		
		WallpaperManager.getInstance(this).suggestDesiredDimensions(metrics.widthPixels, metrics.heightPixels);
		
		gvAppCellDimension = new Point((int) (metrics.widthPixels / 3.5), (int) (metrics.heightPixels / 3.5));
		gvShowAppCellDimension = new Point(metrics.widthPixels / 6, metrics.heightPixels / 4);
		
		double gvShowAppVerticalPercent = (8 / 9.3 - 3.0 / 4) / 4;
		double gvAppVerticalPercent = (8 / 9.3 - 2 / 3.5) / 3;
		
		gvApp.setColumnWidth((int) (metrics.widthPixels / 3));
		gvShowApp.setColumnWidth(metrics.widthPixels / 5);
		gvApp.setPadding(0, (int)(metrics.heightPixels*gvAppVerticalPercent), 0, (int)(metrics.heightPixels*gvAppVerticalPercent));
		gvApp.setVerticalSpacing((int) (metrics.heightPixels*gvAppVerticalPercent));
		gvShowApp.setPadding(0, (int)(metrics.heightPixels * gvShowAppVerticalPercent), 
				0, (int)(metrics.heightPixels * gvShowAppVerticalPercent));
		gvShowApp.setVerticalSpacing((int) (metrics.heightPixels * gvShowAppVerticalPercent));
		
		tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/20);
		tvTime.setPadding(metrics.widthPixels/96, 0, 0, 0);
		llBtnMenu.setPadding(0 , 0, metrics.widthPixels/128, 0);
		btnMenu.getLayoutParams().width = (int) (metrics.heightPixels * 2 / 9.3);
		llNetAndTime.setPadding(metrics.widthPixels/128, 0, 0, 0);
	}

	private void loadFavorites() {
		// TODO Auto-generated method stub
		favoriteAppAdapter = new AppAdapter<FavoriteAppInfo>(this, R.layout.favorites_cell);        
        FavoriteAppInfo.loadFavorites(this, favoriteAppAdapter, gvAppCellDimension);
        gvApp.setAdapter(favoriteAppAdapter);
        gvApp.setSelection(0);
	}

	private void updateTime(){
		
		Date date = new Date();
		
		if (df == null) {
			df = new SimpleDateFormat(TIME_FORMAT);
		}
		
		tvTime.setText(df.format(date));
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		timeUpateReceiver = new TimeUpdateReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(timeUpateReceiver, filter);
        
        networkUpdateReceiver = new NetworkUpdateReceiver();
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkUpdateReceiver, filter);
        
        appUpdateReceiver = new AppUpdateReceiver();
        filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(appUpdateReceiver, filter);
	}
	
    /**
     * Receives notifications when applications are added/removed.
     */
    private class AppUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
        			intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {              
                AppInfo.loadApplications(MainActivity.this, allAppAdapter, gvShowAppCellDimension);
			}
        	
        	if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
        		String pkgName = intent.getData().getEncodedSchemeSpecificPart();
        		
        		for (int pos = 0; pos < allAppAdapter.getCount(); pos++) {
        			AppInfo info = allAppAdapter.getItem(pos);
        			if (info.getIntent().getComponent().getPackageName().equals(pkgName)) {
        				allAppAdapter.remove(info);
					}					
				}

        		for (int pos = 0; pos < favoriteAppAdapter.getCount(); pos++) {
        			FavoriteAppInfo info = favoriteAppAdapter.getItem(pos);
        			if (info.getIntent().getComponent().getPackageName().equals(pkgName)) {
        				info.removeMeFromFavorite(favoriteAppAdapter, false);
					}					
				}
			}
        }
    }
	
	private class TimeUpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			updateTime();
		}
		
		
	}
	
	private class NetworkUpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			updateNetworks();
		}
		
		
	}

	private void loadAnimations() {
		// TODO Auto-generated method stub
		fade = AnimationUtils.loadAnimation(this, R.anim.fade);
		scale = AnimationUtils.loadAnimation(this, R.anim.scale);
	}

	private void updateNetworks() {
		// TODO Auto-generated method stub
		if (cm == null) {
			cm = (ConnectivityManager) MainActivity.this.getSystemService(CONNECTIVITY_SERVICE);
		}
		
		NetworkInfo info = cm.getActiveNetworkInfo();
		
		if(info == null || !info.isConnected()){
			ivNetwork.setImageResource(R.drawable.disconnect);
		}else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
			ivNetwork.setImageResource(R.drawable.ethernet);
		}else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			ivNetwork.setImageResource(R.drawable.wifi);
		}else{
			ivNetwork.setImageResource(R.drawable.ethernet);
		}
	}

	private void bindListeners() {
		// TODO Auto-generated method stub
		gvShowApp.setOnItemClickListener(this);
		gvShowApp.setOnItemLongClickListener(this);
		gvShowApp.setOnItemSelectedListener(this);
		gvShowApp.setOnFocusChangeListener(this);
		
		gvApp.setOnItemClickListener(this);
		gvApp.setOnItemLongClickListener(this);
		gvApp.setOnItemSelectedListener(this);
		gvApp.setOnFocusChangeListener(this);
		
		btnMenu.setOnClickListener(this);
	}

	private void findViews() {
		// TODO Auto-generated method stub
		gvApp = (GridView) findViewById(R.id.gvApp);
		gvShowApp = (GridView) findViewById(R.id.gvShowApp);
		
		tvTime = (TextView) findViewById(R.id.tvTime);
		
		ivNetwork = (ImageView) findViewById(R.id.ivNet);
		
		btnMenu = (Button) findViewById(R.id.btnMenu);
		
		llBtnMenu = (LinearLayout) findViewById(R.id.llBtnMenu);
		
		llNetAndTime = (LinearLayout) findViewById(R.id.llNetAndTime);
		
		llMain = (LinearLayout) findViewById(R.id.llMain);
		
		vsGridView = (ViewSwitcher) findViewById(R.id.vsGridView);
	}

	private void loadApplications() {
		// TODO Auto-generated method stub
		allAppAdapter = new AppAdapter<AppInfo>(this, R.layout.app_cell);
        
        AppInfo.loadApplications(this, allAppAdapter, gvShowAppCellDimension);
        
        gvShowApp.setAdapter(allAppAdapter);
        gvShowApp.setSelection(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		mainPopupMenu.showAtLocation(llMain, Gravity.CENTER, 0, 0);
		return false;
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        }
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		final int pos = position;
		final AdapterView<?> av = parent;
		Animation anim = fade;

		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				AppInfo info = (AppInfo)av.getItemAtPosition(pos);
				info.excute(MainActivity.this);					
			}
		});
		view.startAnimation(anim);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (vsGridView.getCurrentView() == gvShowApp) {
			vsGridView.setDisplayedChild(0);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.gvShowApp:
			AppInfo info = (AppInfo)parent.getItemAtPosition(position);
			info.addMeToFavorite(this , favoriteAppAdapter , gvAppCellDimension);
			return true;
		case R.id.gvApp:
			appPopIndex = position;
			appPopupMenu.showAtLocation(llMain, Gravity.CENTER, 0, 0);
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(timeUpateReceiver);
		unregisterReceiver(networkUpdateReceiver);
		unregisterReceiver(appUpdateReceiver);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnMenu:
			mainPopupMenu.showAtLocation(llMain, Gravity.CENTER, 0, 0);
			break;
		case R.id.menuBtnApps:
			mainPopupMenu.dismiss();			
			if (vsGridView.getCurrentView() == gvApp) {
				vsGridView.setDisplayedChild(1);
			}
			break;
		case R.id.menuBtnSettings:
			mainPopupMenu.dismiss();
        	Intent settings = new Intent(Settings.ACTION_SETTINGS);
        	startActivity(settings);
			break;
		case R.id.menuBtnWallpaper:
			mainPopupMenu.dismiss();
			chooseImage(requestWallpaper);
			break;
		case R.id.menuBtnExcute:
			appPopupMenu.dismiss();
			FavoriteAppInfo info = favoriteAppAdapter.getItem(appPopIndex);
			info.excute(this);
			break;
		case R.id.menuBtnRemove:
			appPopupMenu.dismiss();
			FavoriteAppInfo favoriteInfo = favoriteAppAdapter.getItem(appPopIndex);
			favoriteInfo.removeMeFromFavorite(favoriteAppAdapter , false);
			break;
		case R.id.menuBtnChangeIcon:
			appPopupMenu.dismiss();
			chooseImage(requestFavoriteIcon);
			break;
		case R.id.menuBtnChangeBackground:
			appPopupMenu.dismiss();
			Intent chooseFavoriteBackground = new Intent(MainActivity.this, ChooseFavoriteBackground.class);
			startActivityForResult(chooseFavoriteBackground, requestBackground);
		default:
			break;
		}	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case requestWallpaper:
			try {
				if (data != null) {
					WallpaperManager.getInstance(this).setStream(this.getContentResolver().openInputStream(data.getData()));
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case requestFavoriteIcon:
			
			FavoriteAppInfo info = favoriteAppAdapter.getItem(appPopIndex);
			if (data != null) {
				info.changeCustomIcon(MainActivity.this , data.getData(), favoriteAppAdapter);		
			}			
			break;
			
		case requestBackground:
			
			info = favoriteAppAdapter.getItem(appPopIndex);
			if (data != null) {
				info.changeCustomBackground(MainActivity.this , data.getData(), favoriteAppAdapter);		
			}			
			break;

		default:
			break;
		}	
	}
	
	private void chooseImage(int requestCode){
		Intent pickBackground = new Intent(Intent.ACTION_GET_CONTENT);
        pickBackground.setType("image/*");
        pickBackground.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        PackageManager manager = getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(pickBackground, 0);
        if (infos.size() > 0) {
        	startActivityForResult(pickBackground, requestCode);
		}else {
			Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		AppAdapter adapter;
		
		if (v == gvApp) {
			adapter = favoriteAppAdapter;
		}else if (v == gvShowApp) {
			adapter = allAppAdapter;
		} else {
			return;
		}
		
		lastSelectedGridView = currentSelectedGridView;
		if (!hasFocus) {
			currentSelectedGridView = null;
			adapter.notifyDataSetChanged();
		}else{
			currentSelectedGridView = ((GridView)v).getSelectedView();
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		if (view != null) {
			lastSelectedGridView = currentSelectedGridView;
			currentSelectedGridView = view;
			if (parent == gvApp) {
				favoriteAppAdapter.notifyDataSetChanged();
			}else if (parent == gvShowApp) {
				allAppAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		lastSelectedGridView = currentSelectedGridView;
		currentSelectedGridView = null;
		if (parent == gvApp) {
			favoriteAppAdapter.notifyDataSetChanged();
		}else if (parent == gvShowApp) {
			allAppAdapter.notifyDataSetChanged();
		}
	}
}
