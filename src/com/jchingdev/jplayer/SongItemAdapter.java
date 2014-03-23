package com.jchingdev.jplayer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SongItemAdapter extends ArrayAdapter<SongItem>{

	private ArrayList<SongItem> objects;
	
	public SongItemAdapter(Context context, int resource, ArrayList<SongItem> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.objects= objects;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		//check if null
        if (v == null) {
        	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		v = inflater.inflate(R.layout.song_item, null);
    	}
            
        SongItem i = objects.get(position);
        
        //set values to display
        if (i != null){
            TextView name = (TextView)v.findViewById(R.id.nameSL);
            TextView artist = (TextView)v.findViewById(R.id.artistSL);
            TextView album = (TextView)v.findViewById(R.id.albumTextSL);
            ImageView albumArt =  (ImageView)v.findViewById(R.id.albumArtSL);
            
            if (name!=null)
            	name.setText(i.getName());
            if (artist!=null)
            	artist.setText(i.getArtist());
            if (album!=null)
            	album.setText(i.getAlbum());
            if (albumArt!=null){
            	if (i.getArt()!=null){
            		Bitmap songImage = BitmapFactory.decodeByteArray(i.getArt(),0,i.getArt().length);
            		albumArt.setImageBitmap(songImage);
            	}
            	else
            		albumArt.setImageResource(R.drawable.jandroid);
            }
        }
        return v;
    }

}
