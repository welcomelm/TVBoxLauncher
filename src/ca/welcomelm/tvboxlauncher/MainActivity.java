package ca.welcomelm.tvboxlauncher;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import ca.welcomelm.tvboxlauncher.util.IabHelper;
import ca.welcomelm.tvboxlauncher.util.IabHelper.OnConsumeFinishedListener;
import ca.welcomelm.tvboxlauncher.util.IabHelper.OnIabPurchaseFinishedListener;
import ca.welcomelm.tvboxlauncher.util.IabHelper.OnIabSetupFinishedListener;
import ca.welcomelm.tvboxlauncher.util.IabHelper.QueryInventoryFinishedListener;
import ca.welcomelm.tvboxlauncher.util.IabResult;
import ca.welcomelm.tvboxlauncher.util.Inventory;
import ca.welcomelm.tvboxlauncher.util.Purchase;

import com.google.android.gms.ads.*;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.banner.Banner;

public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, 
									OnClickListener, OnItemSelectedListener, OnFocusChangeListener, 
									OnIabSetupFinishedListener, QueryInventoryFinishedListener, 
									OnIabPurchaseFinishedListener, OnConsumeFinishedListener {
	
	private static final int requestWallpaper = 1;
	private static final int requestFavoriteIcon = 2;
	private static final int requestBackground = 3;
	private static final int requestBuy = 4;
	
	private static final String TAG = "custom";
	
	private static final String SKU_TVLAUNCHER = "ca.welcomelm.ads";
	
	private static final int iabSetupFinished = 0x1 << 0;
	private static final int iabQueryFinished = 0x1 << 1;

	private GridView gvApp, gvShowApp;
	
	private AppAdapter<AppInfo> allAppAdapter;
	private AppAdapter<FavoriteAppInfo> favoriteAppAdapter;
	
	private Animation fade;
	
	private BroadcastReceiver timeUpateReceiver, networkUpdateReceiver, appUpdateReceiver;
	
	private TextView tvTime, tvToast;
	
	private ImageView ivNetwork;
	
	private ImageButton btnMenu;
	
	private LinearLayout llNetAndTime, llMain, llAds; 
	
	private CustomPopupMenu mainPopupMenu, favoritePopupMenu, appPopupMenu;

	private int favoritePopIndex , appPopIndex;
	
	public View currentSelectedGridView , lastSelectedGridView;
	
	private ViewSwitcher vsGridView;
	
	private Boolean isMuted = false;
	
	private StartAppAd startAppAd = new StartAppAd(this);
	Banner banner;
	
	private IabHelper mHelper;
	
	private int iabStatus = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
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
		
		loadStyle();
	}
	
	private void setMis() {
		// TODO Auto-generated method stub
		//appTypeface = Typeface.createFromAsset(getAssets(),"fonts/apps.TTF");
		AppInfo.setContext(this);
		AppStyle.init(this);

		StartAppSDK.init(this, "108561043", "209167028", false);
		
		String base64EncodedPublicKey = 
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgDlcAchKY52u8BZTl0z/G86IfaODUR6KNOvwR8qf60NyckR1DlyycL6cQq+z15sguTNILdEeHTvVNlosiEpJ4A89/D+fvgXiFqPcySwlQYgGg0ubdyOPtdNa8tsOcBh2+T8rc+0MgkDDkenzFLpwDaRaqEPucPLsCn9O+whhsf/3JorY+VKMTNNHPP7Grx/IkrxNDvB9le5xtpOE7rsq877l6odZjf/iiTaCX/LpQeesSdI8oqmht83vHHF1ChCHYJzLoIJsvYnU1mVM1nUuGyb+KWKVuBUKqYDnVYaZPD6yB1/P+Yw6sZYeydxSlOBoR50Xus7KAljCUhdx4GR8CwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey); 
		mHelper.startSetup(this);
	}

	private void popupInit() {
		// TODO Auto-generated method stub
		mainPopupMenu = new CustomPopupMenu(this, R.layout.main_popup_menu);
		favoritePopupMenu = new CustomPopupMenu(this, R.layout.favorite_popup_menu);
		appPopupMenu = new CustomPopupMenu(this, R.layout.app_popup_menu);
		
		mainPopupMenu.setup(R.id.menuBtnApps , R.id.menuBtnSettings , R.id.menuBtnWallpaper , 
							R.id.menuBtnStyles, R.id.menuBtnMute , R.id.menuBtnBuy);
		favoritePopupMenu.setup(R.id.menuBtnExcute , R.id.menuBtnRemove , 
							R.id.menuBtnChangeIcon , R.id.menuBtnChangeBackground);
		appPopupMenu.setup(R.id.menuBtnExcuteApp , R.id.menuBtnUninstall , R.id.menuBtnDetails,
						 	R.id.menuBtnToFavorite);
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
		
		double appCellWidthPercent = 1.0 / 6.5 , appCellHeightPercent = 1.0 / 3.8;
		double favoriteAppCellPercent = 1 / 3.1;
		int gvAppCellsX = 3 , gvAppCellsY = 2 , gvShowAppCellsX = 6 , gvShowAppCellsY = 3;
		double gvFavorVerticalPercent = 8 / 9.5 - 50.0 * metrics.density / metrics.heightPixels;
//		double menuVerticalPercent = (metrics.heightPixels - 50.0 * metrics.density) / metrics.heightPixels * 1.5 / 9.5;
		double gvVerticalPercent = 8 / 9.5;
		double menuVerticalPercent = 1.5 / 9.5;
		
		FavoriteAppInfo.setDimension(new Point((int) (metrics.widthPixels * favoriteAppCellPercent), 
												(int) (metrics.heightPixels * favoriteAppCellPercent)));
		
		AppInfo.setDimension(new Point((int)(metrics.widthPixels * appCellWidthPercent), 
										(int)(metrics.heightPixels * appCellHeightPercent)));
		
		double gvShowAppVerticalPercent = (gvVerticalPercent - gvShowAppCellsY * appCellHeightPercent) / (gvShowAppCellsY + 1);
		double gvAppVerticalPercent = (gvFavorVerticalPercent - gvAppCellsY * favoriteAppCellPercent) / (gvAppCellsY + 1);
		
		gvApp.setColumnWidth((int) (metrics.widthPixels / gvAppCellsX));
		gvShowApp.setColumnWidth(metrics.widthPixels / gvShowAppCellsX);
		gvApp.setPadding(0, (int)(metrics.heightPixels*gvAppVerticalPercent), 0, (int)(metrics.heightPixels*gvAppVerticalPercent));
		gvApp.setVerticalSpacing((int) (metrics.heightPixels*gvAppVerticalPercent));
		gvShowApp.setPadding(0, (int)(metrics.heightPixels * gvShowAppVerticalPercent), 
				0, (int)(metrics.heightPixels * gvShowAppVerticalPercent));
		gvShowApp.setVerticalSpacing((int) (metrics.heightPixels * gvShowAppVerticalPercent));
		
		tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels/18);
		tvTime.setPadding(metrics.widthPixels/96, 0, 0, 0);
		
		llNetAndTime.setPadding(metrics.widthPixels/128, metrics.heightPixels / 60, metrics.widthPixels/128, 0);
		
		tvToast.setTextSize(metrics.widthPixels/110);
		tvToast.setPadding(metrics.widthPixels/256, metrics.widthPixels/256, 
				metrics.widthPixels/256, metrics.widthPixels/256);
		tvToast.getLayoutParams().width = (int) (5 * metrics.heightPixels * menuVerticalPercent);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void loadFavorites() {
		// TODO Auto-generated method stub
		favoriteAppAdapter = new AppAdapter<FavoriteAppInfo>(this, R.layout.favorites_cell);        
        FavoriteAppInfo.loadFavorites(favoriteAppAdapter);
        gvApp.setAdapter(favoriteAppAdapter);
        gvApp.setSelection(0);
	}

	private void updateTime(){
		
		Date date = new Date();
		
		SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
		
		tvTime.setText(dfTime.format(date));
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
                AppInfo.loadApplications(allAppAdapter);
			}
        	
        	if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
        		String pkgName = intent.getData().getEncodedSchemeSpecificPart();
        		
        		AppInfo.removePkg(allAppAdapter , pkgName);
        		FavoriteAppInfo.removePkg(favoriteAppAdapter, pkgName);
			}
        }
    }
	
	private class TimeUpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			updateTime();
			
			if (favoriteAppAdapter.getCount() == 0) {
				setToast(R.string.recommend, 10000);
			}
			
			if (mHelper == null) {
				return;
			}
			
			if ((iabStatus & iabSetupFinished) != iabSetupFinished) {
				mHelper.startSetup(MainActivity.this);
			}else if ((iabStatus & iabQueryFinished) != iabQueryFinished) {
				mHelper.queryInventoryAsync(MainActivity.this);
			}
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
	}

	private void updateNetworks() {
		// TODO Auto-generated method stub
		ConnectivityManager	cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo info = cm.getActiveNetworkInfo();
		
		if(info == null || !info.isConnected()){
			ivNetwork.setImageResource(AppStyle.getCurrentStyle().getImageId(AppStyle.disconnect));
		}else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
			ivNetwork.setImageResource(AppStyle.getCurrentStyle().getImageId(AppStyle.ethernet));
		}else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			ivNetwork.setImageResource(AppStyle.getCurrentStyle().getImageId(AppStyle.wifi));
		}else{
			ivNetwork.setImageResource(AppStyle.getCurrentStyle().getImageId(AppStyle.mobile));
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
		btnMenu.setOnFocusChangeListener(this);
	}

	private void findViews() {
		// TODO Auto-generated method stub
		gvApp = (GridView) findViewById(R.id.gvApp);
		gvShowApp = (GridView) findViewById(R.id.gvShowApp);
		
		tvTime = (TextView) findViewById(R.id.tvTime);
		
		ivNetwork = (ImageView) findViewById(R.id.ivNet);
		
		btnMenu = (ImageButton) findViewById(R.id.btnMenu);
		
		llNetAndTime = (LinearLayout) findViewById(R.id.llNetAndTime);
		
		llMain = (LinearLayout) findViewById(R.id.llMain);
		
		llAds = (LinearLayout) findViewById(R.id.llAds);
		
		vsGridView = (ViewSwitcher) findViewById(R.id.vsGridView);
		
		banner = (com.startapp.android.publish.banner.Banner) findViewById(R.id.startAppBanner);
		
		tvToast = (TextView) findViewById(R.id.tvToast);
	}

	private void loadApplications() {
		// TODO Auto-generated method stub
		allAppAdapter = new AppAdapter<AppInfo>(this, R.layout.app_cell);
        
        AppInfo.loadApplications(allAppAdapter);
        
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
		playSound(AppStyle.pressedSound);

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
				info.excute();					
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
			appPopIndex = position;
			appPopupMenu.showAtLocation(llMain, Gravity.CENTER, 0, 0);
			return true;
		case R.id.gvApp:
			favoritePopIndex = position;
			favoritePopupMenu.showAtLocation(llMain, Gravity.CENTER, 0, 0);
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
		if ((mHelper != null) && 
				((iabStatus & iabSetupFinished) == iabSetupFinished)) {
			mHelper.dispose();
			mHelper = null;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		playSound(AppStyle.pressedSound);
		switch (v.getId()) {
		case R.id.btnMenu:
			mainPopupMenu.showAtLocation(llMain, Gravity.CENTER, 0, 0);
			break;
		case R.id.menuBtnApps:
			mainPopupMenu.dismiss();			
			if (vsGridView.getCurrentView() == llAds) {
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
		case R.id.menuBtnStyles:
			mainPopupMenu.dismiss();
			AppStyle.chooseStyle();
			break;
		case R.id.menuBtnExcute:
			favoritePopupMenu.dismiss();
			FavoriteAppInfo favoriteInfo = favoriteAppAdapter.getItem(favoritePopIndex);
			favoriteInfo.excute();
			break;
		case R.id.menuBtnRemove:
			favoritePopupMenu.dismiss();
			favoriteInfo = favoriteAppAdapter.getItem(favoritePopIndex);
			favoriteInfo.removeMeFromFavorite(favoriteAppAdapter , false);
			break;
		case R.id.menuBtnChangeIcon:
			favoritePopupMenu.dismiss();
			chooseImage(requestFavoriteIcon);
			break;
		case R.id.menuBtnChangeBackground:
			favoritePopupMenu.dismiss();
			Intent chooseFavoriteBackground = new Intent(this, ChooseFavoriteBackground.class);
			startActivityForResult(chooseFavoriteBackground, requestBackground);
			break;
		case R.id.menuBtnMute:
			mainPopupMenu.dismiss();
			isMuted = !isMuted;
			Button button = (Button)v;
			int stringId = isMuted ? R.string.unmute : R.string.mute;
			button.setText(stringId);
			break;
		case R.id.menuBtnBuy:
			mainPopupMenu.dismiss();
			
			if (((iabStatus & iabSetupFinished) != iabSetupFinished) ||
					((iabStatus & iabQueryFinished) != iabQueryFinished)) {
				
				//Toast.makeText(this, R.string.noBillingService, Toast.LENGTH_LONG).show();
				
				setToast(R.string.noBillingService , 4000);
				
				return;
			}
			
			mHelper.launchPurchaseFlow(this, SKU_TVLAUNCHER, requestBuy, this);
			break;
		case R.id.menuBtnExcuteApp:
			appPopupMenu.dismiss();
			AppInfo appInfo = allAppAdapter.getItem(appPopIndex);
			appInfo.excute();
			break;
		case R.id.menuBtnToFavorite:
			appPopupMenu.dismiss();
			appInfo = allAppAdapter.getItem(appPopIndex);
			appInfo.addMeToFavorite(favoriteAppAdapter);
			break;
		case R.id.menuBtnUninstall:
			appPopupMenu.dismiss();
			appInfo = allAppAdapter.getItem(appPopIndex);
			appInfo.uninstall();
			break;
		case R.id.menuBtnDetails:
			appPopupMenu.dismiss();
			appInfo = allAppAdapter.getItem(appPopIndex);
			appInfo.showDetails();
			break;
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
			
			FavoriteAppInfo info = favoriteAppAdapter.getItem(favoritePopIndex);
			if (data != null) {
				info.changeCustomIcon(data.getData(), favoriteAppAdapter);		
			}			
			break;
			
		case requestBackground:
			
			info = favoriteAppAdapter.getItem(favoritePopIndex);
			if (data != null) {
				info.changeCustomBackground(data.getIntExtra(ChooseFavoriteBackground.SELECTED_BACKGROUND_RESID, 
											R.drawable.large_app_background_blue), favoriteAppAdapter);		
			}			
			break;
			
		case requestBuy:

		    // Pass on the activity result to the helper for handling
		    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
		        // not handled, so handle it ourselves (here's where you'd
		        // perform any handling of activity results not related to in-app
		        // billing...
		        super.onActivityResult(requestCode, resultCode, data);
		    }

		default:
			super.onActivityResult(requestCode, resultCode, data);
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
			setToast(R.string.noFileManager, 4000);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		if (v == btnMenu && hasFocus) {
			playSound(AppStyle.selectedSound);
			return;
		}
		
		AppAdapter<?> adapter;
		
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
			playSound(AppStyle.selectedSound);
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
	
	public void loadStyle(){
		AppStyle style = AppStyle.getCurrentStyle();
		
		updateNetworks();
		tvTime.setTextColor(getResources().getColor(style.getTextColor(AppStyle.timeTextColor)));
		gvApp.setSelector(style.getImageId(AppStyle.large_selector));
		gvShowApp.setSelector(style.getImageId(AppStyle.small_selector));
		btnMenu.setImageResource(style.getImageId(AppStyle.menu_button_layer));
		mainPopupMenu.setupBtnStyle(R.id.menuBtnApps , R.id.menuBtnSettings , R.id.menuBtnWallpaper , 
									R.id.menuBtnStyles , R.id.menuBtnMute , R.id.menuBtnBuy);
		favoritePopupMenu.setupBtnStyle(R.id.menuBtnExcute , R.id.menuBtnRemove , 
							R.id.menuBtnChangeIcon , R.id.menuBtnChangeBackground);
		appPopupMenu.setupBtnStyle(R.id.menuBtnExcuteApp , R.id.menuBtnUninstall , R.id.menuBtnDetails, R.id.menuBtnToFavorite);
		favoriteAppAdapter.notifyDataSetChanged();
		allAppAdapter.notifyDataSetChanged();
		
		tvToast.setTextColor(getResources().
				getColor(AppStyle.getCurrentStyle().getTextColor(AppStyle.appTextColor)));
		tvToast.setBackgroundResource(AppStyle.getCurrentStyle().getImageId(AppStyle.toast));
		try {
			WallpaperManager.getInstance(this).setResource(AppStyle.getCurrentStyle().getImageId(AppStyle.wallpaper));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void playSound(int id){
		if (isMuted) {
			return;
		}
		AppStyle.playSound(id);
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		// TODO Auto-generated method stub
		if (result.isFailure()) {
			Log.d(TAG, "onIabSetupFinished failed " + result);
			Toast.makeText(this, String.format("onIabSetupFinished failed " + result), Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "onIabSetupFinished passed");
			Toast.makeText(this, String.format("onIabSetupFinished passed"), Toast.LENGTH_SHORT).show();
			iabStatus |= iabSetupFinished;
			mHelper.queryInventoryAsync(this);
		}
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		// TODO Auto-generated method stub
		if (result.isFailure()) {
			Log.d(TAG, "onQueryInventoryFinished failed " + result);
			Toast.makeText(this, String.format("onQueryInventoryFinished failed " + result), Toast.LENGTH_SHORT).show();
		}else{
			iabStatus |= iabQueryFinished;
			if (inv.hasPurchase(SKU_TVLAUNCHER)) {
				Log.d(TAG, "onQueryInventoryFinished has purchased " + SKU_TVLAUNCHER);
				Toast.makeText(this, String.format("onQueryInventoryFinished has purchased " + SKU_TVLAUNCHER), Toast.LENGTH_SHORT).show();
				banner.hideBanner();
				mainPopupMenu.btnSetEnabled(R.id.menuBtnBuy , false);
				//mHelper.consumeAsync(inv.getPurchase(SKU_TVLAUNCHER), this);
			}else {
				Log.d(TAG, "onQueryInventoryFinished has not purchased " + SKU_TVLAUNCHER);
				Toast.makeText(this, String.format("onQueryInventoryFinished has not purchased " + SKU_TVLAUNCHER), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		// TODO Auto-generated method stub
		if (result.isFailure()) {
			Log.d(TAG, "onIabPurchaseFinished failed " + result);
			Toast.makeText(this, String.format("onIabPurchaseFinished failed " + result), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (info.getSku().equals(SKU_TVLAUNCHER)) {
			Log.d(TAG, "onIabPurchaseFinished passed " + info.getSignature() + " " + info.getOrderId());
			Toast.makeText(this, String.format("onIabPurchaseFinished passed " + info.getSignature() + " " + info.getOrderId()), Toast.LENGTH_SHORT).show();
			mHelper.queryInventoryAsync(this);
		}
	}

	@Override
	public void onConsumeFinished(Purchase purchase, IabResult result) {
		// TODO Auto-generated method stub
		if (result.isFailure()) {
			Log.d(TAG, "onConsumeFinished failed " + result);
			Toast.makeText(this, String.format("onConsumeFinished failed " + result), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (purchase.getSku().equals(SKU_TVLAUNCHER)) {
			Log.d(TAG, "onConsumeFinished passed " + purchase.getSignature() + " " + purchase.getOrderId());
			Toast.makeText(this, String.format("onConsumeFinished passed " + purchase.getSignature() + " " + purchase.getOrderId()), Toast.LENGTH_SHORT).show();
		}		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		startAppAd.onPause();	
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		startAppAd.onResume();
		super.onResume();
	}
	
	private void setToast(int resId , int delayMillis){
		tvToast.setText(resId);
		tvToast.setVisibility(View.VISIBLE);
		tvToast.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				tvToast.setVisibility(View.INVISIBLE);
			}
		}, delayMillis);
	}
	
}
