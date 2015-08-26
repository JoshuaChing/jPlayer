package com.jchingdev.jplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

public class UtilsHelper {
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void setBackground(View view, Drawable drawable){
		if(Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
	}

    // 0 = grey, 1 = navy, 2 = turquoise 3 = green 4 = black
	public static int getTheme(Context context){
		if (context == null){
			return 0;
		}
		SharedPreferences settings = context.getSharedPreferences("THEME", 0);
		int theme = settings.getInt("theme", 0);
		return theme;
	}

	public static void setTheme(Context context, int theme){
		if (context == null){
			return;
		}
	    SharedPreferences settings = context.getSharedPreferences("THEME", 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt("theme", theme);
	    editor.commit();
	}
}