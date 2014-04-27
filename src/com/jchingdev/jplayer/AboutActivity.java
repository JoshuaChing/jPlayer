package com.jchingdev.jplayer;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView versionName = (TextView)findViewById(R.id.app_version);
		try {
			versionName.setText("Version "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			versionName.setText("Version 1.0");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
	
	public void songsButtonClicked(View view){
		AboutActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	//When back button is pressed
	@Override
	public void onBackPressed(){
		AboutActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}

}
