package com.jchingdev.jplayer;

import android.media.MediaMetadataRetriever;

public class SongItem {
	public String name;
	private String artist;
	private String album;
	private String songPath;
	private String songFile;
	private MediaMetadataRetriever metaData = new MediaMetadataRetriever();
	private byte[] art;
	
	////CONSTRUCTOR////
	
	public SongItem(String sp, String sf){
		this.songPath = sp;
		this.songFile = sf;
		this.name = sf.substring(0,sf.length()-4);
		this.metaData.setDataSource(songPath+songFile);
		//get art meta data
		setArt(metaData.getEmbeddedPicture());
		//check if album text is null
		if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!= null)
			album=metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
		else
			album="Unknown Album";
		//check if artist text is null
		if (metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null)
				artist=metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			else
				artist="Unknown Artist";
	}
	
	////SET METHODS////
	
	public SongItem(SongItem songItem) {
		this.setName (songItem.getName());
		this.setArtist(songItem.getArtist());
		this.setAlbum(songItem.getAlbum());
		this.setArt(songItem.getArt());
	}

	public void setName(String n){
		this.name = n;
	}
	
	public void setArtist(String a){
		this.artist = a;
	}
	
	public void setAlbum(String a){
		this.album = a;
	}
	
	public void setArt(byte[] art) {
		this.art = art;
	}
	
	////GET METHODS////
	
	public String getName(){
		return name;
	}
	
	public String getArtist(){
		return artist;
	}
	
	public String getAlbum(){
		return album;
	}

	public byte[] getArt() {
		return art;
	}

}
