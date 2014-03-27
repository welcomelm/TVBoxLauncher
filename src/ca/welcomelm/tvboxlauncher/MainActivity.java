package ca.welcomelm.tvboxlauncher;

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
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnFocusChangeListener, OnClickListener {
	
	private final String FAVORITE_FILE = "favorites.txt";
	
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
	
	private ImageButton btnMenu;
	
	private Button menuBtnApps, menuBtnWallpaper, menuBtnSettings, menuBtnExcute, menuBtnRemove, menuBtnChangeIcon, menuBtnChangeBackground;
	
	private LinearLayout llBtnMenu , llNetAndTime, llMain, llPopupMenu, llPopupButtons, llAppPopupButtons, llAppPopupMenu;
	
	private Point gvAppCellDimension, gvShowAppCellDimension, gvAppIconDimension, gvShowAppIconDimension;
	
	private PopupWindow mainPopupMenu, appPopupMenu;

	private int appPopIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//startSplash();
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		findViews();
		
		setDimension();
		
		setFonts();
		
		popupInit();
		
		updateTime();
		
		updateNetworks();
		
		registerIntentReceivers();
		
		bindListeners();
		
		loadFavorites();
		
		loadApplications();
		
		loadAnimations();	
	}
	
	private void setFonts() {
		// TODO Auto-generated method stub
		//appTypeface = Typeface.createFromAsset(getAssets(),"fonts/apps.ttf");
	}

	private void popupInit() {
		// TODO Auto-generated method stub
		mainPopupMenu = new PopupWindow(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mainPopupMenu.setContentView(llPopupMenu);
		mainPopupMenu.setFocusable(true);
		mainPopupMenu.setOutsideTouchable(true);
		mainPopupMenu.setBackgroundDrawable(new ColorDrawable(0xb0000000));
		mainPopupMenu.setAnimationStyle(R.style.PopupAnimation);
		
		appPopupMenu = new PopupWindow(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		appPopupMenu.setContentView(llAppPopupMenu);
		appPopupMenu.setFocusable(true);
		appPopupMenu.setOutsideTouchable(true);
		appPopupMenu.setBackgroundDrawable(new ColorDrawable(0xb0000000));
		appPopupMenu.setAnimationStyle(R.style.PopupAnimation);
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
		System.out.println(metrics.toString());

		gvApp.setColumnWidth(metrics.widthPixels / 4);
		
		gvShowApp.setColumnWidth(metrics.widthPixels / 6);
		
		gvAppCellDimension = new Point(metrics.widthPixels / 4, (int) (metrics.heightPixels / 3.5));
		gvShowAppCellDimension = new Point(metrics.widthPixels / 6, metrics.heightPixels / 4);
		
		gvApp.setPadding(metrics.widthPixels/32, metrics.widthPixels/32, metrics.widthPixels/32, metrics.widthPixels/32);
		gvApp.setVerticalSpacing(metrics.widthPixels/16);
		gvApp.setHorizontalSpacing(metrics.widthPixels/32);
		
		gvShowApp.setPadding(metrics.widthPixels/64, metrics.widthPixels/96, metrics.widthPixels/64, metrics.widthPixels/96);
		gvShowApp.setVerticalSpacing(metrics.widthPixels/64);
		
		gvShowAppIconDimension = new Point(gvAppCellDimension.y / 2, gvAppCellDimension.y / 2);
		gvAppIconDimension = new Point(gvShowAppCellDimension.y * 4 / 5 , gvShowAppCellDimension.y * 4 / 5);
		
		tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/20);
		tvTime.setPadding(metrics.widthPixels/96, 0, 0, 0);
		
		llBtnMenu.setPadding(0, 0, metrics.widthPixels/128, 0);
		
		llNetAndTime.setPadding(metrics.widthPixels/128, 0, 0, 0);
		
		menuBtnApps.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/50);
		menuBtnSettings.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/50);
		menuBtnWallpaper.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/50);
		
		menuBtnApps.setPadding(metrics.widthPixels/96, 0, metrics.widthPixels/96, 0);
		menuBtnSettings.setPadding(metrics.widthPixels/96, 0, metrics.widthPixels/96, 0);
		menuBtnWallpaper.setPadding(metrics.widthPixels/96, 0, metrics.widthPixels/96, 0);
		
		llPopupButtons.getLayoutParams().height = metrics.heightPixels * 2 / 3;
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
        				info.removeMeFromFavorite(MainActivity.this , favoriteAppAdapter, false);
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
			System.out.println(info.getTypeName() + "...isConnected... " + info.isConnected());
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
		gvApp.setOnItemClickListener(this);
		gvApp.setOnItemLongClickListener(this);
		btnMenu.setOnFocusChangeListener(this);
		btnMenu.setOnClickListener(this);
		menuBtnApps.setOnClickListener(this);
		menuBtnSettings.setOnClickListener(this);
		menuBtnWallpaper.setOnClickListener(this);
		menuBtnExcute.setOnClickListener(this);
		menuBtnRemove.setOnClickListener(this);
		menuBtnChangeIcon.setOnClickListener(this);
		menuBtnChangeBackground.setOnClickListener(this);
	}

	private void findViews() {
		// TODO Auto-generated method stub
		gvApp = (GridView) findViewById(R.id.gvApp);
		gvShowApp = (GridView) findViewById(R.id.gvShowApp);
		
		tvTime = (TextView) findViewById(R.id.tvTime);
		
		ivNetwork = (ImageView) findViewById(R.id.ivNet);
		
		btnMenu = (ImageButton) findViewById(R.id.btnMenu);
		
		llBtnMenu = (LinearLayout) findViewById(R.id.llBtnMenu);
		
		llNetAndTime = (LinearLayout) findViewById(R.id.llNetAndTime);
		
		llMain = (LinearLayout) findViewById(R.id.llMain);
		
		llPopupMenu = (LinearLayout) LinearLayout.inflate(this, R.layout.main_popup_menu, null);
		
		menuBtnApps = (Button) llPopupMenu.findViewById(R.id.menuBtnApps);
		
		menuBtnSettings = (Button) llPopupMenu.findViewById(R.id.menuBtnSettings);
		
		menuBtnWallpaper = (Button) llPopupMenu.findViewById(R.id.menuBtnWallpaper);
		
		llPopupButtons = (LinearLayout) llPopupMenu.findViewById(R.id.llPopupButtons);
		
		llAppPopupMenu = (LinearLayout) LinearLayout.inflate(this, R.layout.app_popup_menu, null);
		
		llAppPopupButtons = (LinearLayout) llAppPopupMenu.findViewById(R.id.llAppPopupButtons);
		
		menuBtnExcute = (Button) llAppPopupMenu.findViewById(R.id.menuBtnExcute);
		menuBtnRemove = (Button) llAppPopupMenu.findViewById(R.id.menuBtnRemove);
		menuBtnChangeIcon = (Button) llAppPopupMenu.findViewById(R.id.menuBtnChangeIcon);
		menuBtnChangeBackground = (Button) llAppPopupMenu.findViewById(R.id.menuBtnChangeBackground);
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
		gvApp.setVisibility(GridView.VISIBLE);
		gvShowApp.setVisibility(GridView.INVISIBLE);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.gvShowApp:
			AppInfo info = (AppInfo)parent.getItemAtPosition(position);
			FavoriteAppInfo favoriteInfo = FavoriteAppInfo.from(info , gvAppCellDimension);
			favoriteInfo.addMeToFavorite(this , favoriteAppAdapter , false);
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
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
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
        	gvShowApp.setVisibility(GridView.VISIBLE);
        	gvApp.setVisibility(GridView.INVISIBLE);
			break;
		case R.id.menuBtnSettings:
			mainPopupMenu.dismiss();
        	Intent settings = new Intent(Settings.ACTION_SETTINGS);
        	startActivity(settings);
			break;
		case R.id.menuBtnWallpaper:
			mainPopupMenu.dismiss();
            Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
            startActivity(Intent.createChooser(pickWallpaper, getString(R.string.menu_wallpaper)));
			break;
		case R.id.menuBtnTest:
			mainPopupMenu.dismiss();
			break;
		case R.id.menuBtnExcute:
			appPopupMenu.dismiss();
			FavoriteAppInfo info = favoriteAppAdapter.getItem(appPopIndex);
			info.excute(this);
			break;
		case R.id.menuBtnRemove:
			appPopupMenu.dismiss();
			FavoriteAppInfo favoriteInfo = favoriteAppAdapter.getItem(appPopIndex);
			favoriteInfo.removeMeFromFavorite(this , favoriteAppAdapter , false);
			break;
		case R.id.menuBtnChangeIcon:
			appPopupMenu.dismiss();
            Intent pickBackground = new Intent(Intent.ACTION_GET_CONTENT);
            pickBackground.setType("image/*");
            pickBackground.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            PackageManager manager = getPackageManager();
            List<ResolveInfo> infos = manager.queryIntentActivities(pickBackground, 0);
            if (infos.size() > 0) {
            	startActivityForResult(pickBackground, 0xbeef);
			}else {
				Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
			}			
			break;
		case R.id.menuBtnChangeBackground:
			appPopupMenu.dismiss();
			Intent test = new Intent();
			test.setClassName("ca.welcomelm.helloeoe", "ca.welcomelm.helloeoe.MainActivity");
			startActivity(test);
			break;
		default:
			break;
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		FavoriteAppInfo info = favoriteAppAdapter.getItem(appPopIndex);
		
		if (data != null) {
			info.changeCustomIcon(MainActivity.this , data.getData(), favoriteAppAdapter);		
		}	
	}
}
