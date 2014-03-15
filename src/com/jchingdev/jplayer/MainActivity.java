package com.jchingdev.jplayer;

import java.util.concurrent.TimeUnit;

import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.R.color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener{

	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////MEDIA PLAYER VARIABLES////
	private SeekBar seekBar;
	private Handler seekBarHandler = new Handler();
	private boolean seekBarTouched = false;
	private TextView currentTimeText;
	private TextView maxTimeText;
	private ImageView albumArt;
	private MediaMetadataRetriever metaData = new MediaMetadataRetriever();
	
	////OVERRIDES////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//set up seek bar
		seekBar = (SeekBar)findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
		//set up time text views
		currentTimeText = (TextView)findViewById(R.id.currentTime);
		maxTimeText = (TextView)findViewById(R.id.maxTime);
		albumArt = (ImageView)findViewById(R.id.albumArt);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
			//set play button text
			setPlayButtonText();
			//set lazy button text
			setLazyButtonText();
			//set now playing text
			setNowPlayingText();
			//set seek bar max duration
			seekBar.setMax(mService.getMaxTime());
			//set max time text
			setMaxTimeText();
			//start seek bar thread
			seekBarHandler.postDelayed(UpdateSongTime, 100);
			//set album art
			//setAlbumArt();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0){
			mBound = false;
		}
	};
	
	//When back button is pressed
	@Override
	public void onBackPressed(){
		mService.stopSong();
		stopService(new Intent(this, MusicService.class));
		MainActivity.this.finish();
	}
	
	//seek bar override methods
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
		//update current time text
		currentTimeText.setText(
				String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress())-
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(seekBar.getProgress())),
				TimeUnit.MILLISECONDS.toSeconds(seekBar.getProgress())-
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress()))		
				));
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar){
		seekBarTouched = true;
		mService.pause();
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar){
		mService.setTime(seekBar.getProgress());
		//check if song is paused from before
		if (!mService.getIsPaused())
			mService.start();
		seekBarTouched = false;
	}
	
	////THREADS////
	
	//update seek bar and song times
	private Runnable UpdateSongTime = new Runnable(){
		public void run(){
			if (!seekBarTouched){
			//set seek bar progress
			seekBar.setProgress(mService.getCurrentTime());
			//set current time text view
			currentTimeText.setText(
				String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(mService.getCurrentTime())-
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mService.getCurrentTime())),
				TimeUnit.MILLISECONDS.toSeconds(mService.getCurrentTime())-
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mService.getCurrentTime()))		
				));
			}
			//check if new song
			if (mService.getCurrentTime() <= 1000){
				setNowPlayingText();
				seekBar.setMax(mService.getMaxTime());
				setMaxTimeText();
				//setAlbumArt();
			}
			seekBarHandler.postDelayed(this, 100);
		}
	};
	
	
	////PRIVATE METHODS////
	
	private void setNowPlayingText(){
		TextView tv = (TextView)findViewById(R.id.nowPlaying);
		tv.setText(mService.getNowPlayingText());
	}
	
	private void setMaxTimeText(){
		maxTimeText.setText(
			String.format("%02d:%02d",
			TimeUnit.MILLISECONDS.toMinutes(mService.getMaxTime())-
			TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mService.getMaxTime())),
			TimeUnit.MILLISECONDS.toSeconds(mService.getMaxTime())-
			TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mService.getMaxTime()))		
		));
	}
	
	private void setAlbumArt(){
		metaData.setDataSource(mService.getSongPath());
		byte[] art = metaData.getEmbeddedPicture();
		//check if art is null
		if (art != null){
			Bitmap songImage = BitmapFactory.decodeByteArray(art,0,art.length);
			albumArt.setImageBitmap(songImage);
		}
		else			
			albumArt.setBackgroundColor(color.white);

	}
	
	private void setPlayButtonText(){
		TextView tv = (TextView)findViewById(R.id.playButton);
		if (mService.getIsPaused()){
			tv.setTextColor(getResources().getColor(R.color.docktext));
		}
		else
			tv.setTextColor(getResources().getColor(R.color.lightGreen));
	}
	
	private void setLazyButtonText(){
		TextView tv = (TextView)findViewById(R.id.lazyButton);
		if(!mService.getIsLazy()){
			tv.setTextColor(getResources().getColor(R.color.docktext));
		}
		else
			tv.setTextColor(getResources().getColor(R.color.lightGreen));
		
	}
	
	////PUBLIC METHODS////
	
	//play button clicked
	public void playButtonClicked(View v){
		mService.playButton();
		setPlayButtonText();
	}
	
	//rewind button clicked
	public void rewindButtonClicked(View v){
		mService.previousSong();
	}

	//forward button clicked
	public void forwardButtonClicked(View v){
		mService.nextSong();
	}
	
	//songs button clicked
	public void songsButtonClicked(View v){
		MainActivity.this.finish();
		Intent intent = new Intent(this,SongListActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}
	
	//lazy button clicked
	public void lazyButtonClicked(View v){
		mService.lazyButton();
		setLazyButtonText();
		if (!mService.getIsLazy()){
			new AlertDialog.Builder(this)
				.setTitle("Lazy Button Deactivated")
				.setMessage("Sensor turned off")
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("OK", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							//place alert dialog functions here
						}
				})
				.show();
		}
		else{
			new AlertDialog.Builder(this)
			.setTitle("Lazy Button Activated")
			.setMessage("Wave your hand over the phone's sensor to change song")
			.setIcon(R.drawable.ic_launcher)
			.setPositiveButton("OK", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						//place alert dialog functions here
					}
			})
			.show();
		}
	}
	
}
