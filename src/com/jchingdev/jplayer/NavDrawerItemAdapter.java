package com.jchingdev.jplayer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerItemAdapter extends ArrayAdapter<NavDrawerItem>{

	private ArrayList<NavDrawerItem> displayedObjects;
	
	public NavDrawerItemAdapter(Context context, int resource, ArrayList<NavDrawerItem> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.displayedObjects = objects;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		//check if null
        if (v == null) {
        	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		v = inflater.inflate(R.layout.nav_drawer_item, null);
    	}
            
        NavDrawerItem i = displayedObjects.get(position);
        //set values to display
        if (i != null){
            TextView text = (TextView)v.findViewById(R.id.navDrawerText);
            ImageView icon =  (ImageView)v.findViewById(R.id.navDrawerIcon);
            
            if (text!=null)
            	text.setText(i.getName());
            if (icon!=null)
            	icon.setImageResource(i.getImageResource());
        }
        return v;
    }
	
}
