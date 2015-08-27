package com.jchingdev.jplayer;

import java.util.ArrayList;
import java.util.List;

import com.jchingdev.jplayer.MusicService.LocalBinder;

//import android.media.MediaMetadataRetriever;
import android.os.Bundle;
//import android.os.Handler;
import android.os.IBinder;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
//import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SongListActivity extends ListActivity {

	////SERVICE VARIABLES////
	MusicService mService;
	boolean mBound = false;
	
	////SONG FILTER VARIABLE////
	private EditText searchFilter;
	
	////NAV DRAWER VARIABLES////
	/*private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ArrayList<NavDrawerItem> drawerList = new ArrayList<NavDrawerItem>();
	private NavDrawerItemAdapter navDrawerItemAdapter;
	private View mDrawerContainer;*/
	
	////SONG LIST VARIABLES////
	private TextView title;
	private TextView subtitle;
	
	private boolean hasAlreadyBeenUpdated = false;
	private ArrayList<SongItem> songList = new ArrayList<SongItem>();
	private SongItemAdapter songItemAdapter;
	private boolean noSongs;
	private ListView alternateList;
	private RelativeLayout mainLayout;
	private List<String> alternateListData; //cache
	
	//logic path
	private boolean initialAlternateListSwitch = false;
	private boolean artistView = false;
	private boolean artistAlbumView = false;
	private boolean artistAlbumSongView = false;
	
	////NOW PLAYING VARIABLES////
	/*private Handler handler = new Handler();
	private boolean handlerPaused = false;
	private ImageView playButton;
	private int playButtonResourceID;*/
	//private TextView nowPlayingText;
	//private String nowPlayingSong;
	//private TextView artistText;
	//private MediaMetadataRetriever metaData = new MediaMetadataRetriever();
	
	////OVERRIDE METHODS////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_list);
		//set up title and subtitle
		title = (TextView)findViewById(R.id.title);
		subtitle = (TextView)findViewById(R.id.subtitle);
		//set up alternate list view
		alternateList = (ListView)findViewById(R.id.alternateList);
		mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
		updateMainLayoutTheme();
		setAlternateListClickHandle();
		//set up play button
		/*playButton = (ImageView)findViewById(R.id.playButtonImage);
		playButtonResourceID = R.drawable.ic_action_play;*/
		//set up now playing text
		//nowPlayingText = (TextView)findViewById(R.id.nowPlayingText);
		//artistText = (TextView)findViewById(R.id.artistText);
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
				if (songItemAdapter != null){
				    songItemAdapter.getFilter().filter(s.toString());
				}
			}
		});
		//set up nav drawer and display
		/*mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout); //entire activity layout
		mDrawerContainer = findViewById(R.id.left_drawer); //only drawer layout
		mDrawerListView = (ListView) findViewById (R.id.left_drawerList); //list view of drawer layout
		
		updateNavDrawerList();
		navDrawerItemAdapter = new NavDrawerItemAdapter(this,R.layout.nav_drawer_item,drawerList);
		mDrawerListView.setAdapter(navDrawerItemAdapter);
		mDrawerListView.setOnItemClickListener(new OnItemClickListener(){
		//handling nav drawer click events
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				switch(position){
				case 0: //now playing
					backButtonClicked(view);
					break;
				case 1://Songs
					displayAllSongs();
					break;
				case 2://artists
					displayArtistsList(true);
					break;
				//case 3://albums
					//break;
				case 3: //settings
					Intent settingsIntent = new Intent(SongListActivity.this, SettingsActivity.class);
					startActivity(settingsIntent);
					overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
					break;
				case 4: //help
					Intent helpIntent = new Intent(SongListActivity.this, HelpActivity.class);
					startActivity(helpIntent);
					overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
					break;
				case 5: //about
					Intent aboutIntent = new Intent(SongListActivity.this, AboutActivity.class);
					startActivity(aboutIntent);
					overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
					break;
				default: //default
					System.out.println("Error");
					break;
				}
				mDrawerLayout.closeDrawer(mDrawerContainer);
			}
			
		});*/
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

	@Override
	protected void onResume(){
		super.onResume();
		updateSongListTheme();
		updateMainLayoutTheme();
		updateAlternateListTheme();
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
				if (!hasAlreadyBeenUpdated||mService.getIsNewPath()){
					updateSongList();
					displayAllSongs();
					mService.setIsNewPath(false);
				}
				//set play button image
				//setPlayButtonImage();
				//set now playing text
				//setNowPlayingText();
				//set now playing song string variable
				//nowPlayingSong = mService.getNowPlayingText();
				//show appropriate lists
				TextView tv = (TextView)findViewById(R.id.noSongs);
				tv.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				//start now playing thread
				//handler.postDelayed(UpdateNowPlaying, 100);
			}
			else{
				displayAllSongs();
				searchFilter.setVisibility(View.GONE);
				searchFilter.getText().clear();
				//setPlayButtonImage();
				TextView tv = (TextView)findViewById(R.id.noSongs);
				tv.setVisibility(View.VISIBLE);
				getListView().setVisibility(View.GONE);
				//nowPlayingText.setText(R.string.nowPlaying);
				//artistText.setText(R.string.artist);
				alertNoSongs();
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
	
	/*@Override
	public void onPause(){
		super.onPause();
		handlerPaused=true;
	}
	
	@Override
	public void onResume(){
		super.onPause();
		handlerPaused=false;
	}*/
	
	////PROTECTED METHODS////
		
	//method to play selected song
	protected void onListItemClick(ListView list,View view,int position, long id){
		mService.selectSong(((SongItem) list.getItemAtPosition(position)).getListPosition());
		mService.setPlaylistSpecifications(false); //no specific artistor album to filter
		//SongListActivity.this.finish();
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}
		
	////PRIVATE METHODS////
	
	/*private void setNowPlayingText(){
		metaData.setDataSource(mService.getSongPath());
		//check if artist text is null
		if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
			artistText.setText(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		else
			artistText.setText("Unknown Artist");
		nowPlayingText.setText(mService.getNowPlayingText());
	}*/
	
	//method to add items to nav drawer list
	/*private void updateNavDrawerList(){
		drawerList.add(new NavDrawerItem("Now Playing",R.drawable.ic_action_play_over_video));
		drawerList.add(new NavDrawerItem("Songs",R.drawable.ic_action_collection));
		drawerList.add(new NavDrawerItem("Artists",R.drawable.ic_action_group));
		//drawerList.add(new NavDrawerItem("Albums",R.drawable.ic_action_collection));
		drawerList.add(new NavDrawerItem("Settings",R.drawable.ic_action_settings));
		drawerList.add(new NavDrawerItem("Help",R.drawable.ic_action_help));
		drawerList.add(new NavDrawerItem("About",R.drawable.ic_action_about));
	}*/
	
	//method to get all songs from service's list
	private void updateSongList(){
		
		songList.clear();
		
		//go through the list from service
		for (int i=0; i<mService.getSongListSize();i++){
			SongItem songItem = new SongItem(mService.getOnlySongPath(), mService.getOnlySongFile(i),i);
			songList.add(songItem);
		}
		
		songItemAdapter = new SongItemAdapter(this,R.layout.song_item,songList);
		setListAdapter(songItemAdapter);
		getListView().setTextFilterEnabled(true);
		hasAlreadyBeenUpdated = true;
	}
	
	private void updateSongListTheme(){
		if (songItemAdapter == null){
			return;
		}
		songItemAdapter = new SongItemAdapter(this,R.layout.song_item,songList);
		setListAdapter(songItemAdapter);
	}
	
	//method to display all songs
	private void displayAllSongs(){
		alternateList.setVisibility(View.GONE);
		//getListView().setVisibility(View.VISIBLE);
		//set title hide subtitle
		title.setText("Songs");
		subtitle.setVisibility(View.GONE);
		
		artistView = false;
		artistAlbumView = false;
		artistAlbumSongView = false;
	}
	
	//method to display artists list
	private void displayArtistsList(boolean ials){
		//getListView().setVisibility(View.GONE);
		newAlternateListAdapterData(mService.getArtistsList());
		alternateList.setVisibility(View.VISIBLE);
		//set title hide subtitle
		title.setText("Artists");
		subtitle.setVisibility(View.GONE);
		
		initialAlternateListSwitch = ials;
		artistView = true;
		artistAlbumView = false;
		artistAlbumSongView = false;
	}
	
	//method to set new single line data to alternate list
	private void newAlternateListAdapterData(List<String> list){
		alternateListData = null;
		alternateListData = list;
		// determine theme colors
		int layout = getAlternateListLayout();
		// set list background
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,layout,list);
		alternateList.setAdapter(arrayAdapter);
	}

	private int getAlternateListLayout(){
		// determine theme colors
		int layout = R.layout.alternate_list_single_item;
		int theme = UtilsHelper.getTheme(getApplicationContext());
		if (theme == 1) {
			layout = R.layout.theme_navy_alternate_list_single_item;
		}else if (theme == 2){
			layout = R.layout.theme_turquoise_alternate_list_single_item;
		}else if (theme == 3){
			layout = R.layout.theme_green_alternate_list_single_item;
		}else if (theme == 4){
			layout = R.layout.theme_black_alternate_list_single_item;
		}else if (theme == 5){
			layout = R.layout.theme_blue_alternate_list_single_item;
		}
		return layout;
	}
	
	private void updateAlternateListTheme(){
		if (alternateList == null || alternateListData == null || alternateListData.isEmpty()){
			return;
		}
		// determine theme colors
		int layout = getAlternateListLayout();
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, layout, alternateListData);
		alternateList.setAdapter(arrayAdapter);
	}
	
	private void updateMainLayoutTheme(){
		int color = UtilsHelper.getThemeColor(getApplicationContext());
		if (mainLayout != null){
		    mainLayout.setBackgroundResource(color);
		}
		if (alternateList != null){
			alternateList.setBackgroundResource(color);
		}
	}

	private void setAlternateListClickHandle(){
		alternateList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				//viewing list of artists
				if (artistView){
					String artistSelected = (alternateList.getItemAtPosition(position).toString());
					newAlternateListAdapterData(mService.getArtistsAlbumsList(artistSelected));
					title.setText(artistSelected);
					
					artistView = false;
					artistAlbumView = true;
					artistAlbumSongView = false;
				}
				//viewing list of albums of an artist
				else if (artistAlbumView){
					 //go back to 'Artists'
					if (position == 0){
						displayArtistsList(false);
					}
					else{
						String albumSelected = (alternateList.getItemAtPosition(position).toString());
						//check whether to get all songs or no
						if (position ==1)
							newAlternateListAdapterData(mService.getArtistsAlbumsSongsList(albumSelected,true));
						else
							newAlternateListAdapterData(mService.getArtistsAlbumsSongsList(albumSelected,false));
						//set title and subtitle
						title.setText(albumSelected);
						subtitle.setText(mService.getViewingArtist());
						subtitle.setVisibility(View.VISIBLE);
					
						artistView = false;
						artistAlbumView = false;
						artistAlbumSongView = true;
					}
				}
				//viewing list of songs of albums of an artist
				else if (artistAlbumSongView){
					//go back to 'Specific Artist'
					if (position == 0){
						String artistSelected = mService.getViewingArtist();
						newAlternateListAdapterData(mService.getArtistsAlbumsList(artistSelected));
						//set title hide subtitle
						title.setText(artistSelected);
						subtitle.setVisibility(View.GONE);
						
						artistView = false;
						artistAlbumView = true;
						artistAlbumSongView = false;
					}
					else if (position == 1){
						mService.setPlaylistSpecifications(true); //filter for artist and album
						if (!mService.getIsShuffle()){ //if shuffle is off play first song on list
							String songSelected = (alternateList.getItemAtPosition(2).toString());
							mService.selectSong(songSelected+".mp3");
						}
						else{// if not just play next song and music service will filter
							mService.setIsPaused(false);
							mService.nextSong();	
						}
						backButtonClicked(view); //goes to now playing (ignore bad naming)
					}
					else{
						//check if it is still under the same artist or album, otherwise turn off filter
						if ((!mService.getViewingArtist().equals(mService.getSpecifiedArtist()))||(!mService.getViewingAlbum().equals(mService.getSpecifiedAlbum()))){
							mService.setPlaylistSpecifications(false);
						}
						String songSelected = (alternateList.getItemAtPosition(position).toString());
						mService.selectSong(songSelected+".mp3");
						backButtonClicked(view); //goes to now playing (ignore bad naming)
					}
				}
			}
			
		});
	}
	//alert user that no songs exist
	public void alertNoSongs(){
		new AlertDialog.Builder(this)
		.setTitle("Error")
		.setMessage("no mp3 files found in the folder: "+mService.getOnlySongPath() +"\n \n" +
				"If this is not where your music is located, please set a new music folder path in settings.")
		.setIcon(R.drawable.ic_action_error)
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//place alert dialog functions here
				}
		})
		.show();
	}
	
	//set play button image
	/*private void setPlayButtonImage(){
		if (mService.getIsPaused()){
			playButton.setImageResource(R.drawable.ic_action_play);
		}
		else
			playButton.setImageResource(R.drawable.ic_action_pause);
	}*/
	
	////THREADS////
	
	//update seek bar and song times
	/*private Runnable UpdateNowPlaying = new Runnable(){
		public void run(){
			if (!handlerPaused){
				//check if play button needs to be changed
				if (mService.getIsPaused()==true && playButton.getId() != playButtonResourceID){
					playButton.setImageResource(R.drawable.ic_action_play);
				}*/
				//check if new song is playing
				/*if (!(nowPlayingSong.equals(mService.getNowPlayingText()))){
					//setNowPlayingText();
					nowPlayingSong = mService.getNowPlayingText();
				}*/
				/*setPlayButtonImage();
				handler.postDelayed(this, 100);
			}
			else{
				handler.removeCallbacks(this);
			}
		}
	};*/
	
	////PUBLIC METHODS////
	
	//back button (goes to now playing song)
	public void backButtonClicked(View view){
		//check if songs exist
		if (noSongs)
			alertNoSongs();
		else
		{
			//SongListActivity.this.finish();
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
		}
			
	}
	
	//SONGPATH BUTTON REPLACED WITH SEARCH BUTTON
	/*public void songPathClicked(View view){
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
	}*/
	
	public void searchButtonClicked(View view){
		if (noSongs)
			alertNoSongs();
		else{
			//check if first time clicking (search filter may be already visible)
			if ((artistView || artistAlbumView || artistAlbumSongView)&&initialAlternateListSwitch){
				searchFilter.setVisibility(View.VISIBLE);
				alternateList.setVisibility(View.GONE);
				initialAlternateListSwitch = false;
			}
			else if (searchFilter.getVisibility()==View.VISIBLE){
			
				InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

				//check if no view has focus:
				View v=this.getCurrentFocus();
				if(v==null)
					return;
			
				//hide keyboard
				inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			
				//hide search filter
				searchFilter.setVisibility(View.GONE);
			
				//clear search filter
				searchFilter.getText().clear();
			
				if (artistView || artistAlbumView || artistAlbumSongView)
					alternateList.setVisibility(View.VISIBLE);
				}
				else{
					//display search filter
					searchFilter.setVisibility(View.VISIBLE);
					alternateList.setVisibility(View.GONE);
				}
		}
	}
	
	//play button clicked
	/*public void playButtonClicked(View v){
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
	}*/
	
	//songs button clicked
	public void songsButtonClicked(View v){
		displayAllSongs();
	}
	
	//artists button clicked
	public void artistsButtonClicked(View v){
		displayArtistsList(true);
	}
	
	//settings button clicked
	public void settingsButtonClicked(View v){
		Intent settingsIntent = new Intent(SongListActivity.this, SettingsActivity.class);
		startActivity(settingsIntent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}
	
	//help button clicked
	public void helpButtonClicked(View v){
		Intent helpIntent = new Intent(SongListActivity.this, HelpActivity.class);
		startActivity(helpIntent);
		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
	}

	//playlists button clicked PLAYLIST ACTIVITY DISABLED
	/*public void playlistsButtonClicked(View view){
		SongListActivity.this.finish();
		Intent intent = new Intent(this,PlaylistActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}*/
	
	//menu button clicked
	/*public void menuButtonClicked(View view){
		mDrawerLayout.openDrawer(mDrawerContainer);
	}*/
	
}
