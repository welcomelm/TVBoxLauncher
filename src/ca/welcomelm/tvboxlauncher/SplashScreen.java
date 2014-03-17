package ca.welcomelm.tvboxlauncher;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.ImageView;

public class SplashScreen extends Activity {
	
	boolean timeOut = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_screen);
		
		Timer timer = new Timer();
		
		TimerTask timerTask = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				finish();
			}
		};
		
		timer.schedule(timerTask, 2000);
		
	}

}
