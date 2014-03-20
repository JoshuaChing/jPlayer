package com.jchingdev.jplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SongListActivity extends ListActivity {

	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////SONG LIST VARIABLES////
	private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	private List<String> songList = new ArrayList<String>();
	private TextView songPath;
	
	
	////OVERRIDE METHODS////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_list);
		updateSongList();
		songPath = (TextView)findViewById(R.id.songPath);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
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
			songPath.setText("Song Folder: "+mService.getOnlySongPath());
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0){
			mBound = false;
		}
	};
	
	@Override
	//When back button is pressed
	public void onBackPressed(){
		SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}	
		
	////PROTECTED METHODS////
		
	//method to play selected song
	protected void onListItemClick(ListView list,View view,int position, long id){
		mService.selectSong(position);
		SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
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
				songList.add((file.getName()).substring(0,(file.getName()).length()-4));
			}
		}
		
		ArrayAdapter<String> displayList = new ArrayAdapter<String>(this, R.layout.song_item,songList);
		setListAdapter(displayList);
	}
	
	////PUBLIC METHODS////
	
	//back button
	public void backButtonClicked(View view){
		SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
}
