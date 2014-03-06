package com.jchingdev.jplayer;

import java.io.File;
import java.io.FilenameFilter;

public class Mp3Filter implements FilenameFilter {
	@Override
	public boolean accept(File dir, String name) {
		// TODO Auto-generated method stub
		return (name.endsWith("mp3"));
	}
}
