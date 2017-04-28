package com.dongji.launcher;

import java.util.List;

import org.adw.launcher.CellLayout;
import org.adw.launcher.Launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.widget.GridView;

import com.dongji.adapter.ScreenManagerAdapter;

/**
 * 
 * @author zhangkai
 */
public class ScreenManagerActivity extends Activity {
	
	ScreenManagerAdapter screens; 
	Drawable mBackgroundDrawable;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screenmanager);
		setupViews();
	}
	
	private void setupViews() {
		GridView mGridView=(GridView)findViewById(R.id.gridview);
		int[] num=new int[2];
		Display mDisplay=getWindowManager().getDefaultDisplay();
		num[0]=mDisplay.getWidth();
		num[1]=mDisplay.getHeight();
		int orientation=getResources().getConfiguration().orientation;;
		screens = new ScreenManagerAdapter(this, Launcher.mLauncher, num, orientation);
		mBackgroundDrawable=WallpaperManager.getInstance(Launcher.mLauncher).getDrawable();
		for (int i = 0; i < Launcher.mLauncher.mWorkspace.getChildCount(); i++) {
			CellLayout cellLayout = (CellLayout) Launcher.mLauncher.mWorkspace
					.getChildAt(i);
			screens.addScreen(cellLayout);
		}
		mGridView.setAdapter(screens);
		findViewById(R.id.contentlayout).setBackgroundDrawable(mBackgroundDrawable);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		screens.destory();
		
		mBackgroundDrawable.setCallback(null);
		mBackgroundDrawable = null;
	}
}
