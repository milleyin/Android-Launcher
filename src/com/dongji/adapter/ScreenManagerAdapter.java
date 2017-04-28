package com.dongji.adapter;

import java.util.ArrayList;
import java.util.Collections;

import org.adw.launcher.CellLayout;
import org.adw.launcher.Launcher;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dongji.launcher.R;
import com.dongji.tool.AndroidUtils;

public class ScreenManagerAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<CellLayout> mScreens;
	private Launcher mLauncher;
	private final static int MAX_SCREENS = 9;
	private int mWidth, mHeight;
	private Drawable mBackgroundDrawable;
	
	public ScreenManagerAdapter(Context context, Launcher mLauncher, int[] num, int orientation) {
		this.context=context;
		this.mLauncher=mLauncher;
		int padding=AndroidUtils.dip2px(context, 95.0f);
		mWidth = (num[0] - padding) / 2;
		mHeight = (num[1] - padding) / 2;
		mBackgroundDrawable=WallpaperManager.getInstance(Launcher.mLauncher).getDrawable();
	}
	
	public void addScreen(CellLayout screen){
    	if(mScreens==null)
    		mScreens=new ArrayList<CellLayout>();
    	mScreens.add(screen);
    	notifyDataSetChanged();
    }
    public void addScreen(CellLayout screen, int position){
    	if(mScreens==null)
    		mScreens=new ArrayList<CellLayout>();
    	mScreens.add(position, screen);
    	notifyDataSetChanged();
    }
    public void removeScreen(int position){
    	if(mScreens==null)
    		return;
    	mScreens.remove(position);
    	notifyDataSetChanged();
    }
    public void swapScreens(int a, int b){
    	if(mScreens==null)
    		return;
    	Collections.swap(mScreens, a, b);
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mScreens == null ? 1 : mScreens.size() < MAX_SCREENS ? mScreens
				.size() + 1 : mScreens.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.item_screen, null);
			holder=new ViewHolder();
			holder.mImageView=(ImageView)convertView.findViewById(R.id.imageview);
			holder.mDeleteButton=(Button)convertView.findViewById(R.id.deleteButton);
			holder.mAddButton=(Button)convertView.findViewById(R.id.addButton);
			holder.mLinearLayout=(FrameLayout)convertView.findViewById(R.id.linearlayout);
			FrameLayout.LayoutParams mParams=(FrameLayout.LayoutParams)holder.mLinearLayout.getLayoutParams();
			mParams.width=mWidth;
			mParams.height=mHeight;
			holder.mLinearLayout.setLayoutParams(mParams);
			convertView.setTag(holder);
		}else holder=(ViewHolder)convertView.getTag();
		if(mScreens==null || position>=mScreens.size()) {
			holder.mImageView.setVisibility(View.GONE);
			holder.mDeleteButton.setVisibility(View.GONE);
			holder.mAddButton.setVisibility(View.VISIBLE);
			holder.mAddButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mScreens!=null && mScreens.size()<MAX_SCREENS){
						CellLayout newScreen=mLauncher.mWorkspace.addScreen(position);
						addScreen(newScreen,position);
					}else{
						Toast t=Toast.makeText(context, R.string.message_cannot_add_desktop_screen, Toast.LENGTH_LONG);
						t.show();
					}
				}
			});
		}else {
			mScreens.get(position).setDrawingCacheEnabled(true);
	        Bitmap b=mScreens.get(position).getDrawingCache(true);
	        holder.mImageView.setBackgroundDrawable(mBackgroundDrawable);
			holder.mImageView.setImageBitmap(b);
			holder.mImageView.setVisibility(View.VISIBLE);
			holder.mDeleteButton.setVisibility(View.VISIBLE);
			holder.mAddButton.setVisibility(View.GONE);
			holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final int screenToDelete=position;
					if(mLauncher.mWorkspace.getChildCount()>1){
						if (mLauncher.mWorkspace.getScreenChildCount(screenToDelete) > 0) {
			                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			                alertDialog.setTitle(context.getResources().getString(R.string.prompt));
			                alertDialog.setMessage(context.getResources().getString(R.string.message_delete_desktop_screen));
			                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(android.R.string.ok),
			                    new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {
			                    	mLauncher.mWorkspace.removeScreen(screenToDelete);
			    					removeScreen(screenToDelete);
			                    }
			                });
			                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(android.R.string.cancel),
			                    new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {
			                    }
			                });
			                alertDialog.show();
						}else {
							mLauncher.mWorkspace.removeScreen(screenToDelete);
	    					removeScreen(screenToDelete);
						}
					}else{
						Toast t=Toast.makeText(context, R.string.message_cannot_delete_desktop_screen, Toast.LENGTH_LONG);
						t.show();
					}
				}
			});
		}
		return convertView;
	}

	private static class ViewHolder {
		FrameLayout mLinearLayout;
		ImageView mImageView;
		Button mDeleteButton, mAddButton;
	}
	
	public void destory()
	{
		mBackgroundDrawable.setCallback(null);
		mBackgroundDrawable = null;
		System.gc();
	}
}
