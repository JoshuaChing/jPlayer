package com.jchingdev.jplayer;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class SongItemAdapter extends ArrayAdapter<SongItem> implements Filterable{

	private ArrayList<SongItem> objects;
	private ArrayList<SongItem> displayedObjects;
	
	public SongItemAdapter(Context context, int resource, ArrayList<SongItem> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.objects= objects;
		this.displayedObjects = objects;
	}

	@Override
	public int getCount(){
		return displayedObjects.size();
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		//check if null
        if (v == null) {
        	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		v = inflater.inflate(R.layout.song_item, null);
    	}
            
        SongItem i = displayedObjects.get(position);
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
	
	public Filter getFilter(){
		Filter filter = new Filter(){

			@SuppressLint("DefaultLocale")
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				// TODO Auto-generated method stub
				FilterResults results = new FilterResults();
				ArrayList<SongItem> FilteredArrList = new ArrayList<SongItem>();
				if (objects == null){
					objects = new ArrayList<SongItem>(displayedObjects);
				}
				if (constraint == null || constraint.length()<=0){
					results.count = objects.size();
					results.values = objects;
				}
				else{
					constraint = constraint.toString().toLowerCase();
					for (int i = 0; i<objects.size();i++){
						String data = objects.get(i).name;
						if (data.toLowerCase().contains(constraint.toString())){
							FilteredArrList.add(new SongItem (objects.get(i)));
						}
					}
					results.count = FilteredArrList.size();
					results.values = FilteredArrList;
				}
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
				FilterResults results) {
				// TODO Auto-generated method stub
				displayedObjects = (ArrayList<SongItem>) results.values;
				notifyDataSetChanged();
			}
			
		};
		return filter;
	}
}
