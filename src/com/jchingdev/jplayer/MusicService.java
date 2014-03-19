package com.jchingdev.jplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

public class MusicService extends Service implements OnCompletionListener {
	
	////SENSOR VARIABLES AND CLASS////
	public SensorManager sensorManager;
	public Sensor proximitySensor;
	public SensorEventListener l;
	private boolean close = false;
	
	public class ProximitySensorEventListener implements SensorEventListener{

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent se) {
			// TODO Auto-generated method stub
			if (isLazy){	
				if (se.values[0] < 5){
					if (!close){
						close = true;
						nextSong();
					}
				}
				else
					close = false;
			}
		}
			
	}
	
	
	////SONG LIST VARIABLES////
	private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	private List<String> songList = new ArrayList<String>();
	private int songPosition = 0;
	
	////MUSIC PLAYER VARIABLES////
	private MediaPlayer mp = new MediaPlayer();
	private boolean isPaused = true;
	private boolean isLooping = false;
	private boolean isShuffle = false;
	private boolean isLazy = false;
	private boolean isLoopingOnComplete = false;
	
	////BINDER SET UP////
	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder{
		MusicService getService(){
			return MusicService.this;
		}
	}
	
	////OVERRIDE METHODS////
	
	public MusicService(){
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate(){
		//Toast.makeText(this,"Welcome to jPlayer", Toast.LENGTH_LONG).show();
		startService(new Intent(this, MusicService.class));
		updateSongList();
		//set current song
		try{
			mp.setDataSource(SD_PATH + songList.get(songPosition));
			mp.prepare();
		}catch(IOException e){}
		//set up the looper listener
		mp.setOnCompletionListener(this);
		//set up sensor variables and register
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		l = new ProximitySensorEventListener();
	}
	
	
	@Override
	public void onStart(Intent intent, int startId){
		//Toast.makeText(this, "Music Service Started", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDestroy(){
		//Toast.makeText(this,"Goodbye!", Toast.LENGTH_LONG).show();
		sensorManager.unregisterListener(l);
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		if (isLooping){
			if (isPaused)
				setSong();
			else
				nextSong();
		}
		else{
			setSong();
			isPaused = true;
			isLoopingOnComplete=true;
		}
	}
		
	////PRIVATE METHODS////
	
	//method to find all .mp3 files and add them to list
	private void updateSongList(){
			
		File home = new File(SD_PATH);
			
		//check if path exists and create one if it doesn't
		if (!home.exists()){
			home.mkdirs();
		}
			
		//filter out mp3 files of the directory and add them to list
		if (home.listFiles(new Mp3Filter()).length > 0){
			for (File file: home.listFiles(new Mp3Filter())){
				songList.add(file.getName());
			}
		}			
	}
	
	////PUBLIC METHODS////
	
	//destroy music
	public void stopSong(){
		mp.stop();
	}
	
	//get complete song path
	public String getSongPath(){
		return SD_PATH + songList.get(songPosition);
	}
	
	//get now playing song text
	public String getNowPlayingText(){
		return (songList.get(songPosition)).substring(0,songList.get(songPosition).length()-4);
	}
	
	//get is looping
	public boolean getIsLooping(){
		return isLooping;
	}
	
	//get is looping on complete
	public boolean getIsLoopingOnComplete(){
		return isLoopingOnComplete;
	}
	
	//reset is looping on complete
	public void resetIsLoopingOnComplete(){
		isLoopingOnComplete=false;
	}
	
	//get is paused
	public boolean getIsPaused(){
		return isPaused;
	}
	
	//get is lazy
	public boolean getIsLazy(){
		return isLazy;
	}
	
	//pause
	public void pause(){
		mp.pause();
	}
	
	//start
	public void start(){
		mp.start();
	}
	
	//set time of song
	public void setTime(int newTime){
		mp.seekTo(newTime);
	}
	
	//start music player
	public void setSong(){
		try{
			mp.reset();
			mp.setDataSource(SD_PATH + songList.get(songPosition));
			mp.prepare();
			//reset time
			setTime(0);
		}catch(IOException e){}
	}
	
	//get current song time
	public int getCurrentTime(){
		return mp.getCurrentPosition();
	}
	
	//get song max song time
	public int getMaxTime(){
		return  mp.getDuration();
	}
	
	//loop button
	public void loopButton(){
		if (isLooping){
			isLooping = false;
		}
		else
			isLooping = true;
	}
	
	//play button
	public void playButton(){
		if (isPaused){
			mp.start();
			isPaused = false;
		}
		else{
			mp.pause();
			isPaused = true;
		}
	}
	
	//nextSong
	public void nextSong(){
		//check if last song on list
		if (songPosition >= songList.size()-1)
			songPosition = 0;
		else
			songPosition++;
		setSong();
		if (!isPaused){
			mp.start();
		}
	}
	
	//previous song
	public void previousSong(){
		//check if first song on list
		if (songPosition <= 0)
			songPosition = songList.size()-1;
		else
			songPosition--;
		setSong();
		if (!isPaused){
			mp.start();
		}
	}
	
	//select song
	public void selectSong(int newPosition){
		songPosition = newPosition;
		setSong();
		mp.start();
		isPaused = false;
	}
	
	//lazy button
	public void lazyButton(){
		if (isLazy){
			isLazy=false;
			sensorManager.unregisterListener(l);
		}
		else{
			isLazy=true;
			sensorManager.registerListener(l, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
}
