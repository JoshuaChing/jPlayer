package com.jchingdev.jplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
				if (se.values[0] < proximitySensor.getMaximumRange()){
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
	//private static final String SD_PATH = "/storage/sdcard0/Music/"; //internal storage for HTC one x
	private List<String> songList = new ArrayList<String>();
	private Integer[] shuffleList;
	private int songPosition = 0;
	private int shufflePositionIndex = 0;
	private boolean noSongs;
	
	////PLAYLIST VARIABLES////
	private List<String> playlists = new ArrayList<String>();
	
	////MUSIC PLAYER VARIABLES////
	private MediaPlayer mp = new MediaPlayer();
	private boolean isPaused = true;
	private boolean isLooping = false;
	private boolean isShuffle = false;
	private boolean isLazy = false;
	
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
		//update all song lists
		updateSongList();
		//update playlists
		updatePlaylists();
		//check if songs exist
		if (!noSongs){
			populateShuffleList();
			updateShuffleList();
			//set current song
			try{
				mp.setDataSource(SD_PATH + songList.get(songPosition));
				mp.prepare();
			}catch(IOException e){}
			//set up the looper listener
			mp.setOnCompletionListener(this);
		}
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
		
		//check if songs exist
		if (songList.isEmpty())
			noSongs = true;
		else
			noSongs = false;
	}
	
	//populate the shuffle list
	private void populateShuffleList(){
		shuffleList = new Integer[songList.size()];
		for (int i=0; i<songList.size();i++){
			shuffleList[i]=i;
		}
	}
	
	//shuffle the shuffle list
	private void updateShuffleList(){
		Collections.shuffle(Arrays.asList(shuffleList));
	}
	
	//set index of shuffle position
	private void setShufflePositionIndex(int currentSongPosition){
		int i = 0;
		while (shuffleList[i]!=currentSongPosition){
			i++;
		}	
		shufflePositionIndex = i;
	}
	
	//update playlists
	private void updatePlaylists(){
		playlists.add("All Songs");
	}
	
	////PUBLIC METHODS////
	
	//destroy music
	public void stopSong(){
		mp.stop();
	}
	
	//get playlists
	public List<String> getPlaylists(){
		return playlists;
	}
	
	//get song list size
	public int getSongListSize(){
		return songList.size();
	}
	
	//get if song list is empty
	public boolean getIsNoSongs(){
		return noSongs;
	}
	
	//get complete song path
	public String getSongPath(){
		return SD_PATH + songList.get(songPosition);
	}
	
	//get only song path
	public String getOnlySongPath(){
		return SD_PATH;
	}
	
	//get only song file
	public String getOnlySongFile(int i){
		return songList.get(i);
	}
	
	//get song file name
	public String getNowPlayingFile(){
		return songList.get(songPosition);
	}
	
	//get now playing song text
	public String getNowPlayingText(){
		return (songList.get(songPosition)).substring(0,songList.get(songPosition).length()-4);
	}
	
	//get is looping
	public boolean getIsLooping(){
		return isLooping;
	}
	
	//get is shuffle
	public boolean getIsShuffle(){
		return isShuffle;
	}
	
	//get is paused
	public boolean getIsPaused(){
		return isPaused;
	}
	
	//get is lazy
	public boolean getIsLazy(){
		return isLazy;
	}
	
	//set is shuffle
	public void setIsShuffle(boolean s){
		isShuffle = s;
	}
	
	//set is looping
	public void setIsLooping(boolean l){
		isLooping = l;
	}
	
	//set is lazy
	public void setIsLazy(boolean l){
		isLazy = l;
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
	
	//loop button
		public void shuffleButton(){
			if (isShuffle){
				isShuffle = false;
				//re-shuffle list
				updateShuffleList();
			}
			else{
				isShuffle = true;
				//find index of current song in shuffle list
				setShufflePositionIndex(songPosition);
			}
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
		//check if shuffling
		if (isShuffle){
			//check if last song on list
			if (shufflePositionIndex >= shuffleList.length-1)
				shufflePositionIndex = 0;
			else
				shufflePositionIndex++;
			songPosition = shuffleList[shufflePositionIndex];
		}
		else{
			//check if last song on list
			if (songPosition >= songList.size()-1)
				songPosition = 0;
			else
				songPosition++;
		}
		setSong();
		if (!isPaused){
			mp.start();
		}
	}
	
	//previous song
	public void previousSong(){
		//check if shuffling
		if (isShuffle){
			if (shufflePositionIndex <= 0)
				shufflePositionIndex = shuffleList.length-1;
			else
				shufflePositionIndex--;
			songPosition = shuffleList[shufflePositionIndex];
		}
		else
		{
			//check if first song on list
			if (songPosition <= 0)
				songPosition = songList.size()-1;
			else
				songPosition--;
		}
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
