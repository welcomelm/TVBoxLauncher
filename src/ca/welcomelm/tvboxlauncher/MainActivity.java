package ca.welcomelm.tvboxlauncher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.support.v4.app.ShareCompat.IntentReader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	
	private final String TIME_FORMAT = "EEEE MMM d, h:mm a";

	private GridView gvApp, gvShowApp, gvAddApp;
	
	private AppAdapter allAppAdapter;
	private AppAdapter favoriteAppAdapter;
	
	private Animation fadeIn;
	
	private BroadcastReceiver timeUpateReceiver, networkUpdateReceiver;
	
	private TextView tvTime;
	
	private DateFormat df;
	
	private ConnectivityManager cm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViews();
		
		updateTime();
		
		updateNetworks();
		
		registerIntentReceivers();
		
		bindListeners();
		
		loadApplications();
		
		loadAnimations();

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
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.grid_entry);
	}

	private void updateNetworks() {
		// TODO Auto-generated method stub
		if (cm == null) {
			cm = (ConnectivityManager) MainActivity.this.getSystemService(CONNECTIVITY_SERVICE);
		}
		
		for(NetworkInfo info : cm.getAllNetworkInfo()){
			System.out.println(info.getTypeName() + "...isConnected... " + info.isConnected());
		}
	}

	private void bindListeners() {
		// TODO Auto-generated method stub
		gvShowApp.setOnItemClickListener(this);
		gvShowApp.setOnItemLongClickListener(this);
//		gvShowApp.setOnItemSelectedListener(this);
	}

	private void findViews() {
		// TODO Auto-generated method stub
		gvApp = (GridView) findViewById(R.id.gvApp);
		gvAddApp = (GridView) findViewById(R.id.gvAddApp);
		gvShowApp = (GridView) findViewById(R.id.gvShowApp);
		
		tvTime = (TextView) findViewById(R.id.tvTime);
		
	}

	private void loadApplications() {
		// TODO Auto-generated method stub
		allAppAdapter = new AppAdapter(this, R.layout.app_cell);
		favoriteAppAdapter = new AppAdapter(this, R.layout.favorites_cell);
		
        allAppAdapter.clear();
        favoriteAppAdapter.clear();
		
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            for (int i = 0; i < count; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.setActivity(new ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(manager);

                allAppAdapter.add(application);
            }
        }
        
        gvShowApp.setAdapter(allAppAdapter);
        gvShowApp.setSelection(0);
        
        gvApp.setAdapter(favoriteAppAdapter);
        gvApp.setSelection(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        }
    }
	
	public void showPopup(View v) {
		openOptionsMenu();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemAddApp:
            	gvShowApp.setVisibility(GridView.INVISIBLE);
            	gvApp.setVisibility(GridView.INVISIBLE);
            	gvAddApp.setVisibility(GridView.VISIBLE);
                return true;
            case R.id.menuItemAllApp:
            	gvAddApp.setVisibility(GridView.INVISIBLE);
            	gvShowApp.setVisibility(GridView.VISIBLE);
            	gvApp.setVisibility(GridView.INVISIBLE);
                return true;
            case R.id.menuItemSettings:
            	return true;
            case R.id.menuItemWallpaper:
            	return true;
        }

        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		final int pos = position;
		final AdapterView<?> av = parent;
		
		fadeIn.setAnimationListener(new AnimationListener() {
			
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
				ApplicationInfo info = (ApplicationInfo)av.getItemAtPosition(pos);
				startActivity(info.intent);					
			}
		});
		view.startAnimation(fadeIn);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		gvApp.setVisibility(GridView.VISIBLE);
		gvAddApp.setVisibility(GridView.INVISIBLE);
		gvShowApp.setVisibility(GridView.INVISIBLE);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		ApplicationInfo info = (ApplicationInfo)parent.getItemAtPosition(position);
		String addStr = getResources().getString(R.string.add_app);
		Toast.makeText(this, info.title + " " + addStr, Toast.LENGTH_SHORT).show();
		favoriteAppAdapter.add(info);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(timeUpateReceiver);
	}
}
