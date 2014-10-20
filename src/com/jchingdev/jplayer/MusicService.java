package com.jchingdev.jplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
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
			if (isLazy && !noSongs){	
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
	private String SD_PATH = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	//private static final String SD_PATH = "/storage/sdcard0/Music/"; //internal storage for HTC one x
	private List<String> songList = new ArrayList<String>();
	private Integer[] shuffleList;
	private int songPosition = 0;
	private int shufflePositionIndex = 0;
	private boolean noSongs;
	private boolean isNewPath = false;
	
	////PLAYLIST VARIABLES////
	private List<String> playlists = new ArrayList<String>();
	
	////ALTERNATE LIST VARIABLES////
	private MediaMetadataRetriever metaData = new MediaMetadataRetriever();
	private List<String> artistsList = new ArrayList<String>();
	private String viewingArtist;
	private String viewingAlbum;
	private boolean viewArtistAllSongs = false;
	private boolean playlistSpecifications = false;
	private String specifiedArtist;
	private String specifiedAlbum;
	private boolean specifiedArtistAllSongs = false;
	
	////MUSIC PLAYER VARIABLES////
	private MediaPlayer mp = new MediaPlayer();
	private boolean isPaused = true;
	private boolean isLooping = false;
	private boolean isShuffle = false;
	private boolean isLazy = false;
	private SharedPreferences settings;
	private SharedPreferences.Editor changeSettings;
	
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
		loadSettings();
		startService(new Intent(this, MusicService.class));
		init();
		//set up sensor variables and register
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		l = new ProximitySensorEventListener();
		if (isLazy)//turn on sensor if needed
			sensorManager.registerListener(l, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
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
			System.out.println("loop");
			if (isPaused)
				setSong();
			else{
				nextSong();
			}
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
				metaData.setDataSource(SD_PATH+file.getName());
				String tempArtist;
				//check if artists exists or if its "unknown artist"
				if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
					tempArtist = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				else
					tempArtist = "Unknown Artist";
				//check if artists list contains the value, add it in if it doesn't
				if (!artistsList.contains(tempArtist))
					artistsList.add(tempArtist);
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
		while (shuffleList[i]!=currentSongPosition && i<songList.size()){
			i++;
		}	
		shufflePositionIndex = i;
	}
	
	//update playlists
	private void updatePlaylists(){
		playlists.add("All Songs");
	}
	
	//load settings
	private void loadSettings(){
		settings = getSharedPreferences("SETTINGS",MODE_PRIVATE);
		changeSettings = settings.edit();
		isShuffle = settings.getBoolean("isShuffle",false);
		isLooping = settings.getBoolean("isLooping",false);
		isLazy = settings.getBoolean("isLazy", false);
		SD_PATH = settings.getString("SD_PATH",Environment.getExternalStorageDirectory().getPath() +"/Music/");
	}
	
	//check if specified song is valid
	private boolean checkSpecifiedSongValid(String songFile){
		if (playlistSpecifications){
			metaData.setDataSource(SD_PATH+songFile);
			
			String tempArtist;
			//check if artists exists or if its "unknown artist"
			if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
				tempArtist = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			else
				tempArtist = "Unknown Artist";
			
			//first check if artist is same as viewing artist
			if (tempArtist.equals(specifiedArtist)){
				if (specifiedArtistAllSongs){//check if its play all
					//System.out.println(viewingArtist);
					//System.out.println(viewingAlbum);
					//System.out.println(viewArtistAllSongs);
					//System.out.println(specifiedArtist);
					//System.out.println(specifiedAlbum);
					//System.out.println(specifiedArtistAllSongs);
					return true;
				}
				else{
					String tempAlbum;
					//check if artists exists or if its "unknown artist"
					if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!=null)
						tempAlbum = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
					else
						tempAlbum = "Unknown Album";
					
					//System.out.println(viewingArtist);
					//System.out.println(viewingAlbum);
					//System.out.println(viewArtistAllSongs);
					//System.out.println(specifiedArtist);
					//System.out.println(specifiedAlbum);
					//System.out.println(specifiedArtistAllSongs);
					
					
					//check if album is same as viewing album
					if (tempAlbum.equals(specifiedAlbum)){
						return true;
					}
					else{
						return false;
					}
				}
					
			}
			else
				return false;
		}
		return true;
	}
	
	//initialize playlist
	private void init(){
		//update all song lists
		updateSongList();
		//update playlists
		updatePlaylists();
		//check if songs exist
		if (!noSongs){
			populateShuffleList();
			updateShuffleList();
			setShufflePositionIndex(songPosition);
			//set current song
			try{
				mp.setDataSource(SD_PATH + songList.get(songPosition));
				mp.prepare();
			}catch(IOException e){}
				//set up the looper listener
			mp.setOnCompletionListener(this);
		}
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
	
	public List<String> getArtistsList(){
		return artistsList;
	}
	
	//get a list of albums from the artist given
	public List<String> getArtistsAlbumsList(String artist){
		List<String> albums = new ArrayList<String>();
		albums.add("Go back to 'Artists'");
		albums.add("All Songs");
		for (int i = 0; i < songList.size(); i++){
			metaData.setDataSource(SD_PATH+songList.get(i));
			
			String tempArtist;
			String tempAlbum;
			
			//check if artists exists or if its "unknown artist"
			if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
				tempArtist = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			else
				tempArtist = "Unknown Artist";
			
			//check if artists matches
			if (tempArtist.equals(artist)){
				if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!=null)
					tempAlbum = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				else
					tempAlbum = "Unknown Album";
				//check if tempAlbum exists in list
				if (!albums.contains(tempAlbum))
					albums.add(tempAlbum);
			}
		}
		viewingArtist = artist;
		return albums;
	}
	
	//get a list of songs from the album and viewing artist given
	public List<String> getArtistsAlbumsSongsList(String album, boolean getAll){
		List<String> songs = new ArrayList<String>();
		songs.add("Go back to '"+viewingArtist+"'");
		songs.add("Play all");
		for (int i = 0; i < songList.size(); i++){
			metaData.setDataSource(SD_PATH+songList.get(i));
			
			String tempArtist;
			String tempAlbum;
			
			//check if artists exists or if its "unknown artist"
			if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
				tempArtist = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			else
				tempArtist = "Unknown Artist";
			
			//check if artists matches
			if (tempArtist.equals(viewingArtist)){
				//get all songs if getAll is true
				if (getAll){
					songs.add(songList.get(i).substring(0,songList.get(i).length()-4));
				}
				//otherwise filter by album
				else{
					if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!=null)
						tempAlbum = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
					else
						tempAlbum = "Unknown Album";
					
					//check if album matches
					if (tempAlbum.equals(album)){
						//check if tempAlbum exists in list
						//if (!songs.contains(songList.get(i).substring(0,songList.get(i).length()-4)))
						
						//add song to list
						songs.add(songList.get(i).substring(0,songList.get(i).length()-4));
					}
				}
			}
		}
		viewingAlbum = album;
		viewArtistAllSongs = getAll;
		return songs;
	}
	
	//get viewing artist
	public String getViewingArtist(){
		return viewingArtist;
	}
	
	//get viewing album
	public String getViewingAlbum(){
		return viewingAlbum;
	}
	
	//get specified artist
	public String getSpecifiedArtist(){
		return specifiedArtist;
	}
	
	//get specified album
	public String getSpecifiedAlbum(){
		return specifiedAlbum;
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
	
	//get is new path
	public boolean getIsNewPath(){
		return isNewPath;
	}
	
	//set is new path
	public void setIsNewPath(boolean np){
		isNewPath = np;
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
	
	//set is paused
	public void setIsPaused(boolean b){
		isPaused = b;
	}
	
	//set time of song
	public void setTime(int newTime){
		mp.seekTo(newTime);
	}
	
	//set only song path
	public void setOnlySongPath(String newPath){
		isNewPath = true;
		
		changeSettings.putString("SD_PATH",newPath);
		changeSettings.commit();
		
		mp.stop();
		mp.reset();
		isPaused = true;
		SD_PATH = settings.getString("SD_PATH",Environment.getExternalStorageDirectory().getPath() +"/Music/");
		
		songList.clear();
		artistsList.clear();
		songPosition = 0;
		shufflePositionIndex = 0;
		playlistSpecifications = false;
		specifiedArtistAllSongs = false;
		init();
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
	
	//set playlist specifications (specific artist,album,etc)
	public void setPlaylistSpecifications(boolean b){
		playlistSpecifications = b;
		//define specifications
		if (playlistSpecifications){
			specifiedArtist = viewingArtist;
			specifiedAlbum = viewingAlbum;
			specifiedArtistAllSongs = viewArtistAllSongs;
		}
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
		changeSettings.putBoolean("isLooping",isLooping);
		changeSettings.commit();
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
			changeSettings.putBoolean("isShuffle",isShuffle);
			changeSettings.commit();
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
		//check for song validity and specifications
		do{
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
		}while(playlistSpecifications==true&&!checkSpecifiedSongValid(songList.get(songPosition)));
		
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
		setShufflePositionIndex(newPosition);
		setSong();
		mp.start();
		isPaused = false;
	}
	
	//select song by name
	public void selectSong(String name){
		int i = 0;
		while ((!songList.get(i).equals(name)) && i<songList.size()){
			i++;
		}
		songPosition = i;
		setShufflePositionIndex(i);
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
		changeSettings.putBoolean("isLazy",isLazy);
		changeSettings.commit();
	}
}
