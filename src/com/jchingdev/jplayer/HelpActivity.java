package com.jchingdev.jplayer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;

public class HelpActivity extends Activity {

	private RelativeLayout mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
		updateBackground();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	public void songsButtonClicked(View view){
		HelpActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	
	//When back button is pressed
	@Override
	public void onBackPressed(){
		HelpActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}

	private void updateBackground(){
		int color = UtilsHelper.getThemeColor(getApplicationContext());
		if (mainLayout != null){
		    mainLayout.setBackgroundResource(color);
		}
	}
}
