package com.jchingdev.jplayer;

import java.util.ArrayList;
import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class SongListActivity extends ListActivity {

	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////SONG LIST VARIABLES////
	private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	private ArrayList<SongItem> songList = new ArrayList<SongItem>();
	private SongItemAdapter songItemAdapter;
	private TextView songPath;
	
	////OVERRIDE METHODS////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_list);
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
			updateSongList();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0){
			mBound = false;
		}
	};
	
	@Override
	//When back button is pressed
	public void onBackPressed(){
		mService.stopSong();
		stopService(new Intent(this, MusicService.class));
		SongListActivity.this.finish();
	}	
		
	////PROTECTED METHODS////
		
	//method to play selected song
	protected void onListItemClick(ListView list,View view,int position, long id){
		mService.selectSong(position);
		SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}
		
	////PRIVATE METHODS////
	
	//method to get all songs from service's list
	private void updateSongList(){
			
		//go through the list from service
		for (int i=0; i<mService.getSongListSize();i++){
			SongItem songItem = new SongItem(SD_PATH, mService.getOnlySongFile(i));
			songList.add(songItem);
		}
		
		songItemAdapter = new SongItemAdapter(this,R.layout.song_item,songList);
		setListAdapter(songItemAdapter);
	}
	
	////PUBLIC METHODS////
	
	//back button
	public void backButtonClicked(View view){
		SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}
	
	public void songPathClicked(View view){
		new AlertDialog.Builder(this)
		.setTitle("Song List")
		.setMessage(songList.size()+ "  mp3 files found")
		.setIcon(R.drawable.ic_action_about)
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//place alert dialog functions here
				}
		})
		.show();
	}
	
	//play button clicked
		public void playButtonClicked(View v){
			mService.playButton();
			//setPlayButtonImage();
		}
		
		//rewind button clicked
		public void rewindButtonClicked(View v){
			mService.previousSong();
		}

		//forward button clicked
		public void forwardButtonClicked(View v){
			mService.nextSong();
		}
	
	
}
