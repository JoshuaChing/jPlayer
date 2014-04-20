package com.jchingdev.jplayer;

import java.util.ArrayList;
import com.jchingdev.jplayer.MusicService.LocalBinder;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SongListActivity extends ListActivity {

	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////SONG FILTER VARIABLE////
	private EditText searchFilter;
	
	////NAV DRAWER VARIABLES////
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] mDrawerItems = {"Item A","Item B","Item C","Item D","Item E"};
	private View mDrawerContainer;
	
	////SONG LIST VARIABLES////
	private boolean hasAlreadyBeenUpdated = false;
	private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	private ArrayList<SongItem> songList = new ArrayList<SongItem>();
	private SongItemAdapter songItemAdapter;
	private boolean noSongs;
	
	////NOW PLAYING VARIABLES////
	private Handler handler = new Handler();
	private ImageView playButton;
	private int playButtonResourceID;
	private TextView nowPlayingText;
	private String nowPlayingSong;
	private TextView artistText;
	private MediaMetadataRetriever metaData = new MediaMetadataRetriever();
	
	////OVERRIDE METHODS////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_list);
		//set up play button
		playButton = (ImageView)findViewById(R.id.playButtonImage);
		playButtonResourceID = R.drawable.ic_action_play;
		//set up now playing text
		nowPlayingText = (TextView)findViewById(R.id.nowPlayingText);
		artistText = (TextView)findViewById(R.id.artistText);
		//set up search filter
		searchFilter = (EditText)findViewById(R.id.searchFilter);
		searchFilter.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				songItemAdapter.getFilter().filter(s.toString());
			}
		});
		//set up nav drawer and display
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerContainer = findViewById(R.id.left_drawer);
		mDrawerList = (ListView) findViewById (R.id.left_drawerList);
		ArrayAdapter<String> displayList = new ArrayAdapter<String>(this,R.layout.drawer_item,mDrawerItems);
		mDrawerList.setAdapter(displayList);
		
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
			noSongs = mService.getIsNoSongs();
			//check if songs exist
			if (!noSongs){
				if (!hasAlreadyBeenUpdated)
					updateSongList();
				//set play button image
				setPlayButtonImage();
				//set now playing text
				setNowPlayingText();
				//set now playing song string variable
				nowPlayingSong = mService.getNowPlayingText();
				//start now playing thread
				handler.postDelayed(UpdateNowPlaying, 100);
			}
			else{
				TextView tv = (TextView)findViewById(R.id.noSongs);
				tv.setVisibility(View.VISIBLE);
			}
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
		SongListActivity.this.finish();
	}	
		
	////PROTECTED METHODS////
		
	//method to play selected song
	protected void onListItemClick(ListView list,View view,int position, long id){
		mService.selectSong(((SongItem) list.getItemAtPosition(position)).getListPosition());
		SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}
		
	////PRIVATE METHODS////
	
	private void setNowPlayingText(){
		metaData.setDataSource(mService.getSongPath());
		//check if artist text is null
		if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
			artistText.setText(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		else
			artistText.setText("Unknown Artist");
		nowPlayingText.setText(mService.getNowPlayingText());
	}
	
	//method to get all songs from service's list
	private void updateSongList(){
			
		//go through the list from service
		for (int i=0; i<mService.getSongListSize();i++){
			SongItem songItem = new SongItem(SD_PATH, mService.getOnlySongFile(i),i);
			songList.add(songItem);
		}
		
		songItemAdapter = new SongItemAdapter(this,R.layout.song_item,songList);
		setListAdapter(songItemAdapter);
		getListView().setTextFilterEnabled(true);
		hasAlreadyBeenUpdated = true;
	}
	
	//alert user that no songs exist
	public void alertNoSongs(){
		new AlertDialog.Builder(this)
		.setTitle("Error")
		.setMessage("no mp3 files found in the folder: "+mService.getOnlySongPath())
		.setIcon(R.drawable.ic_action_error)
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//place alert dialog functions here
				}
		})
		.show();
	}
	
	//set play button image
	private void setPlayButtonImage(){
		if (mService.getIsPaused()){
			playButton.setImageResource(R.drawable.ic_action_play);
		}
		else
			playButton.setImageResource(R.drawable.ic_action_pause);
	}
	
	////THREADS////
	
	//update seek bar and song times
	private Runnable UpdateNowPlaying = new Runnable(){
		public void run(){
			//check if play button needs to be changed
			if (mService.getIsPaused()==true && playButton.getId() != playButtonResourceID){
				playButton.setImageResource(R.drawable.ic_action_play);
			}
			//check if new song is playing
			if (!(nowPlayingSong.equals(mService.getNowPlayingText()))){
				setNowPlayingText();
				nowPlayingSong = mService.getNowPlayingText();
			}
			handler.postDelayed(this, 100);
		}
	};
	
	////PUBLIC METHODS////
	
	//back button (goes to now playing song)
	public void backButtonClicked(View view){
		//check if songs exist
		if (noSongs)
			alertNoSongs();
		else
		{
			SongListActivity.this.finish();
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
		}
			
	}
	
	public void songPathClicked(View view){
		new AlertDialog.Builder(this)
		.setTitle("All Songs")
		.setMessage(songList.size()+ "  mp3 files found in the folder: "+mService.getOnlySongPath())
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
			//check if songs exist
			if (noSongs)
				alertNoSongs();
			else{
				mService.playButton();
				setPlayButtonImage();
			}
		}
		
		//rewind button clicked
		public void rewindButtonClicked(View v){
			//check if songs exist
			if (noSongs)
				alertNoSongs();
			else
				mService.previousSong();
		}

		//forward button clicked
		public void forwardButtonClicked(View v){
			//check if songs exist
			if (noSongs)
				alertNoSongs();
			else
				mService.nextSong();
		}
	
		//playlists button clicked PLAYLIST ACTIVITY DISABLED
		/*public void playlistsButtonClicked(View view){
			SongListActivity.this.finish();
			Intent intent = new Intent(this,PlaylistActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
		}*/
		
		//menu button clicked
		public void menuButtonClicked(View view){
			mDrawerLayout.openDrawer(mDrawerContainer);
		}
	
}
