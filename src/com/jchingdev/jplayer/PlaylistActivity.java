package com.jchingdev.jplayer;

import java.util.ArrayList;
import java.util.List;

import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.os.Bundle;
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

public class PlaylistActivity extends ListActivity {

	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////PLAYLIST VARIABLES////
	private List<String> playlists = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.playlist, menu);
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
			//update and display playlists
			playlists = mService.getPlaylists();
			updatePlaylists();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0){
			mBound = false;
		}
	};

	@Override
	//When back button is pressed
	public void onBackPressed(){
		//check if songs exists
		if (!mService.getIsNoSongs())
			mService.stopSong();
		stopService(new Intent(this, MusicService.class));
		PlaylistActivity.this.finish();
	}	
	
	////PROTECTED METHODS////
	
	//method to play selected song
	protected void onListItemClick(ListView list,View view,int position, long id){
		if (position==0){
			PlaylistActivity.this.finish();
			Intent intent = new Intent(this,SongListActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
		}
	}
	
	////PRIVATE METHODS////
	
	private void updatePlaylists(){
		ArrayAdapter<String> displayList = new ArrayAdapter<String>(this,R.layout.playlist_item,playlists);
		setListAdapter(displayList);
	}
		
		
}
