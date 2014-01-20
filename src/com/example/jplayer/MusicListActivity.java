package com.example.jplayer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MusicListActivity extends ListActivity {

	//Filters mp3 files
	class Mp3Filter implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name) {
			// TODO Auto-generated method stub
			return (name.endsWith("mp3"));
		}
	}
	
	//declaring variables
	private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	private List<String> songList = new ArrayList<String>();
	private MediaPlayer mediaPlayer = new MediaPlayer();
	public SensorManager sensorManager;
	public Sensor proximitySensor;
	public SensorEventListener pListener;
	public int songPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_list);
		//update the song list
		updateSongList();
		songPosition = songList.size() -1;
		//set up sensor manager and sensor
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		pListener = new ProximitySensorEventListener(mediaPlayer, SD_PATH, songList, songPosition);
		sensorManager.registerListener(pListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music_list, menu);
		return true;
	}
	
	@Override //stop music when back button is hit
	public void onBackPressed() {
	   mediaPlayer.stop();
	   sensorManager.unregisterListener(pListener);
	   Toast.makeText(this, "Exiting jPlayer", Toast.LENGTH_SHORT).show();
	   MusicListActivity.this.finish();
	}
	

	//method for pause button
	public void pauseButton (View view){
		if (mediaPlayer.isPlaying()){
			Toast.makeText(this, "Music Paused",Toast.LENGTH_SHORT).show();
			mediaPlayer.pause();
		}
		else{
			Toast.makeText(this, "Music unpaused", Toast.LENGTH_SHORT).show();
			mediaPlayer.start();
		}
	}
	
	//method to play music when list is clicked
	protected void onListItemClick(ListView list, View view, int position, long id){
		try{
			songPosition = position;
			mediaPlayer.reset();
			mediaPlayer.setDataSource(SD_PATH + songList.get(position));
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IOException e){
			
		}
	}
	
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
		
		ArrayAdapter<String> displayList = new ArrayAdapter<String>(this, R.layout.song_item,songList);
		setListAdapter(displayList);
	}

}
