package com.jchingdev.jplayer;

import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {
	
	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////SETTINGS VARIABLE////
	private CheckBox shuffle;
	private CheckBox looping;
	private CheckBox sensor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		//set up checkbox views
		shuffle = (CheckBox)findViewById(R.id.shuffleCheckBox);
		looping = (CheckBox)findViewById(R.id.loopCheckBox);
		sensor = (CheckBox)findViewById(R.id.sensorCheckBox);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		//bind to music service
		Intent intent = new Intent(this,MusicService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		//remove bind from the music service
		if (mBound){
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	//defines callback for service binding, passed to bindService
		private ServiceConnection mConnection = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName className, IBinder service){
				LocalBinder binder =(LocalBinder)service;
				mService = binder.getService();
				mBound = true;
				//set checkbox values
				shuffle.setChecked(mService.getIsShuffle());
				looping.setChecked(mService.getIsLooping());
				sensor.setChecked(mService.getIsLazy());
			}
			@Override
			public void onServiceDisconnected(ComponentName arg0){
				mBound = false;
			}
		};
	
	//When back button is pressed
	@Override
	public void onBackPressed(){
		SettingsActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	//goes back to song list
	public void songsButtonClicked(View view){
		SettingsActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	//when check boxes are clicked
	public void checkBoxClicked(View view){
		switch (view.getId()){
		case R.id.shuffleCheckBox:
			mService.shuffleButton();
			break;
		case R.id.loopCheckBox:
			mService.loopButton();
			break;
		case R.id.sensorCheckBox:
			mService.lazyButton();
			break;
		default:
			System.out.println("Error");
			break;
		}
	}

}
