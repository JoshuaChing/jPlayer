package com.jchingdev.jplayer;

import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	
	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	private PackageManager PM;
	private boolean hasProximitySensor;
	
	////SETTINGS VARIABLE////
	private CheckBox shuffle;
	private CheckBox looping;
	private CheckBox sensor;
	private TextView folderPath;
	
	////THEME VARIABLE////
	private Spinner spinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		//package manager to check for sensor
		PM = this.getPackageManager();
		hasProximitySensor = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
		//set up checkbox views
		shuffle = (CheckBox)findViewById(R.id.shuffleCheckBox);
		looping = (CheckBox)findViewById(R.id.loopCheckBox);
		sensor = (CheckBox)findViewById(R.id.sensorCheckBox);
		folderPath = (TextView)findViewById(R.id.folderPath);
		spinner = (Spinner)findViewById(R.id.themeSpinner);
		setUpSpinner();
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
			folderPath.setText(mService.getOnlySongPath());
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
			if (mService == null) {
				shuffle.setChecked(!shuffle.isChecked());
				return;
			}
			mService.shuffleButton();
			break;
		case R.id.loopCheckBox:
			if (mService == null) {
				looping.setChecked(!looping.isChecked());
				return;
			}
			mService.loopButton();
			break;
		case R.id.sensorCheckBox:
			if (hasProximitySensor && mService != null)
				mService.lazyButton();
			else{
				sensor.setChecked(false);
				new AlertDialog.Builder(this)
				.setTitle("Device Sensor Not Found")
				.setMessage("Your device does not have a proximity sensor to use this feature.")
				.setIcon(R.drawable.ic_action_error)
				.setPositiveButton("OK", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							//place alert dialog functions here
						}
				})
				.show();
			}
			break;
		default:
			System.out.println("Error");
			break;
		}
	}
	
	//when folder path button clicked
	public void folderPathButtonClicked(View view){
		Intent intent = new Intent(this,FolderBrowse.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}

	private void setUpSpinner(){
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.theme_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		int theme = UtilsHelper.getTheme(getApplicationContext());
		spinner.setSelection(theme);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				System.out.println("@@@ " + position);
				UtilsHelper.setTheme(getApplicationContext(), position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			    // do nothing
			}
			
		});
	}
}
