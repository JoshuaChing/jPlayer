package com.jchingdev.jplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FolderBrowse extends ListActivity {

	 private List<String> item = null;
	 private List<String> path = null;
	 private String root="/";
	 private TextView myPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder_browse);
		myPath = (TextView)findViewById(R.id.path);
        getDir(Environment.getExternalStorageDirectory().getPath());
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.folder_browse, menu);
		return true;
	}
	
	private void getDir(String dirPath)

    {
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles();

		if(!dirPath.equals(root))
		{
			//item.add(root);
			//path.add(root);
			item.add("../");
			path.add(f.getParent());
		}     

		for(int i=0; i < files.length; i++)
		{
			File file = files[i];
			path.add(file.getPath());
			if(file.isDirectory())
				item.add(file.getName() + "/");
			//else
				//item.add(file.getName());
		}
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.folder_browse_item, item);
		setListAdapter(fileList);
    }

	@Override

	protected void onListItemClick(ListView l, View v, int position, long id) {
	
		File file = new File(path.get(position));
		
		if (file.isDirectory())
		{
			if(file.canRead())
				getDir(path.get(position));
				
		}
	
	}
	
}
