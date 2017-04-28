package com.dongji.enity;

import android.graphics.drawable.Drawable;

public class WallpaperInfo2 {

	private int wallpaperId, thumbId;
	private Drawable wallpaperImg, thumbImg;
	private String wallpaperName;
	
	public WallpaperInfo2(Drawable wallpaperImg, Drawable thumbImg, String wallpaperName) {
		super();
		this.wallpaperImg = wallpaperImg;
		this.wallpaperName = wallpaperName;
	}
	
	public WallpaperInfo2(int wallpaperId, int thumbId, String wallpaperName) {
		super();
		this.wallpaperId = wallpaperId;
		this.thumbId = thumbId;
		this.wallpaperName = wallpaperName;
	}

	public WallpaperInfo2() {}
	
	public int getWallpaperId() {
		return wallpaperId;
	}

	public void setWallpaperId(int wallpaperId) {
		this.wallpaperId = wallpaperId;
	}

	public int getThumbId() {
		return thumbId;
	}

	public void setThumbId(int thumbId) {
		this.thumbId = thumbId;
	}

	public Drawable getWallpaperImg() {
		return wallpaperImg;
	}
	
	public void setWallpaperImg(Drawable wallpaperImg) {
		this.wallpaperImg = wallpaperImg;
	}
	
	public Drawable getThumbImg() {
		return thumbImg;
	}

	public void setThumbImg(Drawable thumbImg) {
		this.thumbImg = thumbImg;
	}

	public String getWallpaperName() {
		return wallpaperName;
	}
	
	public void setWallpaperName(String wallpaperName) {
		this.wallpaperName = wallpaperName;
	}
	
}
