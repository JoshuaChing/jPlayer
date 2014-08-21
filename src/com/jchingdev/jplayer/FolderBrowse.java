package com.jchingdev.jplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FolderBrowse extends ListActivity {

	 ////SERVICE VARIABLES////
	 MusicService mService;
	 boolean mBound = false;
	
	 ////FOLDER BROWSE VARIABLES////
	 private List<String> item = null;
	 private List<String> path = null;
	 private String currentMusicFolder = Environment.getExternalStorageDirectory().getPath() +"/Music/";
	 private String root="/";
	 private TextView myPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder_browse);
		myPath = (TextView)findViewById(R.id.path);
        getDir(currentMusicFolder);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.folder_browse, menu);
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
			//get current music folder path
			currentMusicFolder = mService.getOnlySongPath();
			getDir(currentMusicFolder);
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0){
			mBound = false;
		}
	};
	
	private void getDir(String dirPath)

    {
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles();

		if(!dirPath.equals(root))
		{
			//item.add("Go back to current Music Folder");
			//path.add(currentMusicFolder);
			item.add("../");
			path.add(f.getParent());
		}
		

		for(int i=0; i < files.length; i++)
		{
			File file = files[i];
			path.add(file.getPath());
			//filter for folders
			if(file.isDirectory())
				item.add(file.getName() + "/");
			//filter for mp3 files
			else if(file.getName().endsWith(".mp3"))
				item.add(file.getName());
		}
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.folder_browse_item, item);
		setListAdapter(fileList);
    }

	@Override

	protected void onListItemClick(ListView l, View v, int position, long id) {
	
		File file = new File(path.get(position));
		
		//check if file is directory
		if (file.isDirectory())
		{
			if(file.canRead())
				getDir(path.get(position));
			else{
				new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("[" + file.getName() + "] folder can't be read!")
				.setIcon(R.drawable.ic_action_error)
				.setPositiveButton("OK", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							//place alert dialog functions here
						}
				})
				.show();
			}
				
		}
		else{
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage("[" + file.getName() + "] is not a folder!")
			.setIcon(R.drawable.ic_action_error)
			.setPositiveButton("OK", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						//place alert dialog functions here
					}
			})
			.show();
		}
	
	}
	
	//when back button is clicked
	public void backButtonClicked(View view){
		FolderBrowse.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	//When back button is pressed on device
	@Override
	public void onBackPressed(){
		FolderBrowse.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	
	//when home folder button is clicked
	public void homeButtonClicked(View view){
		getDir(currentMusicFolder);
	}
	
	
	//when set folder button is clicked
	public void setFolderButtonClicked(View view){

	}
	
}
