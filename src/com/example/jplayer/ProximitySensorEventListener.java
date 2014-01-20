package com.example.jplayer;

import java.io.IOException;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;

public class ProximitySensorEventListener implements SensorEventListener{

	MediaPlayer mediaPlayer;
	String SD_PATH;
	List<String> songList;
	int songPosition;
	boolean close = false;
	
	public ProximitySensorEventListener (MediaPlayer mp, String sdpath, List<String> sl, int sp){
		mediaPlayer = mp;
		SD_PATH = sdpath;
		songList = sl;
		songPosition = sp;
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		if (se.values[0] < 5){ //check if the new proximity is close
			if (close == false){ //check if it was far before
				if (songPosition + 1>= songList.size() ) //check if the song is at the end of the list, then play
					songPosition = 0;
				else
					songPosition ++;
				try{
					mediaPlayer.reset();
					mediaPlayer.setDataSource(SD_PATH + songList.get(songPosition));
					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IOException e){	}
				close = true; //set the new proximity to be close
			}
		} 
		else
			close = false; //otherwise set new proximity to be far
		
	}

}
