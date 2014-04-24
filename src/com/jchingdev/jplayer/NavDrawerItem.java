package com.jchingdev.jplayer;

public class NavDrawerItem {
	private String name;
	private int imageResource;
	
	
	////CONSTRUCTOR////
	
	public NavDrawerItem(String n, int ir){
		this.name = n;
		this.imageResource = ir;
	}
	
	////SET METHODS////

	public void setName(String n){
		this.name = n;
	}
	
	public void setImageResource(int ir){
		this.imageResource = ir;
	}
	
	////GET METHODS////
	
	public String getName(){
		return name;
	}
	
	public int getImageResource(){
		return imageResource;
	}

}
