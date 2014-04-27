package com.jchingdev.jplayer;

import java.util.concurrent.TimeUnit;

import com.jchingdev.jplayer.MusicService.LocalBinder;

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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
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
	private TextView nowPlayingText;
	//private TextView albumText; ALBUM TEXT HAS BEEN REMOVED
	private TextView artistText;
	private ImageView albumArt;
	private ImageView albumArtBackground;
	private ImageView playButton;
	private int playButtonResourceID;
	private MediaMetadataRetriever metaData = new MediaMetadataRetriever();
	private String nowPlayingSong;
	
	////SWIP GESTURE CONSTANTS////
	private static final int SWIPE_MIN_DISTANCE=120;
	private static final int SWIPE_THRESHOLD_VELOCITY=200;
	private GestureDetector gestureDetector;
	
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
		//set up meta data views
		albumArt = (ImageView)findViewById(R.id.albumArt);
		albumArtBackground = (ImageView)findViewById(R.id.albumArtBackground);
		//albumText = (TextView)findViewById(R.id.albumText); ALBUM TEXT HAS BEEN REMOVED
		artistText = (TextView)findViewById(R.id.artistText);
		//set up now playing text
		nowPlayingText = (TextView)findViewById(R.id.nowPlayingText);
		//set up play button
		playButton = (ImageView)findViewById(R.id.playButtonImage);
		playButtonResourceID = R.drawable.ic_action_play;
		//set up gesture detector
		gestureDetector = new GestureDetector(this, new OnSwipeGestureListener());
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
			//set play button image
			setPlayButtonImage();
			//set lazy button text
			setLazyButtonImage();
			//set now playing text
			setNowPlayingText();
			//set seek bar max duration
			seekBar.setMax(mService.getMaxTime());
			//set max time text
			setMaxTimeText();
			//start seek bar thread
			seekBarHandler.postDelayed(UpdateSongTime, 100);
			//set meta data
			setMetaData();
			//set now playing song string variable
			nowPlayingSong = mService.getNowPlayingText();
			//set shuffle button image
			setShuffleButtonImage();
			//set looping button image
			setLoopButtonImage();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0){
			mBound = false;
		}
	};
	
	//When back button is pressed
	@Override
	public void onBackPressed(){
		MainActivity.this.finish();
		//Intent intent = new Intent(this,SongListActivity.class);
		//startActivity(intent);
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		return gestureDetector.onTouchEvent(event);
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
			//check if new song is playing
			if (!(nowPlayingSong.equals(mService.getNowPlayingText()))){
				setNowPlayingText();
				seekBar.setMax(mService.getMaxTime());
				setMaxTimeText();
				setMetaData();
				nowPlayingSong = mService.getNowPlayingText();
			}
			//check if play button needs to be changed
			if (mService.getIsPaused()==true && playButton.getId() != playButtonResourceID){
				playButton.setImageResource(R.drawable.ic_action_play);
			}
			seekBarHandler.postDelayed(this, 100);
		}
	};
	
	////PRIVATE CLASSES////
	
	//swipe gesture handler
	private class OnSwipeGestureListener extends GestureDetector.SimpleOnGestureListener{
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float veloctityY){
			float deltaX = e2.getX() - e1.getX();
			if ((Math.abs(deltaX)<SWIPE_MIN_DISTANCE)||(Math.abs(velocityX)<SWIPE_THRESHOLD_VELOCITY)){
				return false; //not a swipe
			}
			else{
				if(deltaX < 0){
					handleSwipeRightToLeft();
				}
				else{
					handleSwipeLeftToRight();
				}
			}
			return true;
		}	
	}
	
	//swipe right
	private void handleSwipeRightToLeft(){
		mService.nextSong();
	}
	
	//swipe left
	private void handleSwipeLeftToRight(){
		mService.previousSong();
	}
	
	////PRIVATE METHODS////
	
	private void setNowPlayingText(){
		nowPlayingText.setText(mService.getNowPlayingText());
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
	
	private void setMetaData(){
		metaData.setDataSource(mService.getSongPath());
		byte[] art = metaData.getEmbeddedPicture();
		//check if art is null
		if (art != null){
			Bitmap songImage = BitmapFactory.decodeByteArray(art,0,art.length);
			albumArt.setImageBitmap(songImage);
			albumArtBackground.setImageBitmap(songImage);
		}
		else{			
			albumArt.setImageResource(R.drawable.jandroid);
			albumArtBackground.setImageResource(R.drawable.jandroid);
		}
		//check if album text is null ALBUM TEXT HAS BEEN REMOVED
		/**if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!= null)
				albumText.setText(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
		else
			albumText.setText("Unknown Album");**/
		//check if artist text is null
		if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
			artistText.setText(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		else
			artistText.setText("Unknown Artist");
	}
	
	private void setPlayButtonImage(){
		if (mService.getIsPaused()){
			playButton.setImageResource(R.drawable.ic_action_play);
		}
		else
			playButton.setImageResource(R.drawable.ic_action_pause);
	}
	
	private void setLoopButtonImage(){
		ImageView iv = (ImageView)findViewById(R.id.loopButtonImage);
		if (mService.getIsLooping()){
			iv.setImageResource(R.drawable.ic_action_repeat_focused);
		}
		else
			iv.setImageResource(R.drawable.ic_action_repeat);
	}
	
	private void setShuffleButtonImage(){
		ImageView iv = (ImageView)findViewById(R.id.shuffleButtonImage);
		if (mService.getIsShuffle()){
			iv.setImageResource(R.drawable.ic_action_shuffle_focused);
		}
		else
			iv.setImageResource(R.drawable.ic_action_shuffle);
	}
	
	private void setLazyButtonImage(){
		ImageView iv = (ImageView)findViewById(R.id.lazyButtonImage);
		if (mService.getIsLazy()){
			iv.setImageResource(R.drawable.sensor_icon_focused);
		}
		else
			iv.setImageResource(R.drawable.sensor_icon);
		
	}
	
	////PUBLIC METHODS////
	
	//play button clicked
	public void playButtonClicked(View v){
		mService.playButton();
		setPlayButtonImage();
	}
	
	//rewind button clicked
	public void rewindButtonClicked(View v){
		mService.previousSong();
	}

	//forward button clicked
	public void forwardButtonClicked(View v){
		mService.nextSong();
	}
	
	//songs button clicked (goes to song list)
	public void songsButtonClicked(View v){
		MainActivity.this.finish();
		//Intent intent = new Intent(this,SongListActivity.class);
		//startActivity(intent);
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	//looping button clicked
	public void loopButtonClicked(View v){
		mService.loopButton();
		setLoopButtonImage();
	}
	
	//shuffle button clicked
	public void shuffleButtonClicked(View v){
		mService.shuffleButton();
		setShuffleButtonImage();
	}
	
	//now playing text clicked
	public void nowPlayingClicked(View v){
		new AlertDialog.Builder(this)
		.setTitle("Now Playing")
		.setMessage(mService.getNowPlayingFile())
		.setIcon(R.drawable.ic_action_about)
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//place alert dialog functions here
				}
		})
		.show();
	}
	
	//lazy button clicked
	public void lazyButtonClicked(View v){
		mService.lazyButton();
		setLazyButtonImage();
		if (!mService.getIsLazy()){
			new AlertDialog.Builder(this)
				.setTitle("Lazy Button Deactivated")
				.setMessage("Sensor disabled")
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
