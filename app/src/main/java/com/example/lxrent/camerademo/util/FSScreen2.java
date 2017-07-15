package com.example.lxrent.camerademo.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

/** 
 *  
 * @author : clw 
 */
public class FSScreen2 {	

	private static Context mAppContext;
	
	public static void init(Context context){
		mAppContext = context.getApplicationContext();
	}

	/**
	 * obtain the dpi of screen
	 */
	public static float getScreenDpi() {
		return mAppContext.getResources().getDisplayMetrics().densityDpi;
	}

	/**
	 * obtain the density of screen
	 */
	public static float getScreenDensity() {	
		return mAppContext.getResources().getDisplayMetrics().density;
	}
	
	/**
	 * obtain the scaled density of screen
	 */
	public static float getScreenScaledDensity() {	
		return mAppContext.getResources().getDisplayMetrics().scaledDensity;
	}

	/**
	 * obtain the width of screen
	 */
	public static int getScreenWidth() {	
		return mAppContext.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * obtain the height of screen
	 */
	public static int getScreenHeight() {	
		return mAppContext.getResources().getDisplayMetrics().heightPixels;
	}

	public static int getHasVirtualKey() {
		int dpi = 0;
		WindowManager windowManager = (WindowManager) mAppContext
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		@SuppressWarnings("rawtypes")
		Class c;
		try {
			c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, dm);
			dpi = dm.heightPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dpi;
	}
	public static int getStatusBarHeight(){
		int result = 0;
		int resourceId = mAppContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
		     result = mAppContext.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	
	/**
	 * According to the resolution of the phone from the dp unit will become a px (pixels)
	 */
	public static int dip2px(int dip) {
		float density = getScreenDensity();
		return (int) (dip * density + 0.5f);
	}

	/**
	 * Turn from the units of px (pixels) become dp according to phone resolution
	 */
	public static int px2dip(float px) {
		float density = getScreenDensity();
		return (int) (px / density + 0.5f);
	}

	/**
	 * Turn from the units of px (pixels) become sp according to phone scaledDensity
	 * @param ctx
	 * @param px
	 * @return
	 */
	public static int px2sp(float px) {
		float scale = getScreenScaledDensity();
		return (int) (px / scale + 0.5f);
	}

	/**
	 * According to the scaledDensity of the phone from the sp unit will become a px (pixels)
	 * @param ctx
	 * @param sp
	 * @return
	 */
	public static int sp2px(int sp){
		float scale = getScreenScaledDensity();
		return (int) (sp * scale + 0.5f);
	}	
}
