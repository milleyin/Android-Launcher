package com.dongji.adapter;

import java.util.List;

import com.dongji.enity.WallpaperInfo2;
import com.dongji.launcher.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WallpaperMngrAdapter extends BaseAdapter {
	
	private Context context;
	private List<WallpaperInfo2> list;
	private boolean[] chosenArray;
	
	public WallpaperMngrAdapter(Context context, List<WallpaperInfo2> list) {
		super();
		this.context = context;
		this.list = list;
		if (chosenArray == null && list != null) {
			chosenArray = new boolean[list.size()];
		}
	}
	
	public void resetChosen() {
		for (int i = 0 ; i < chosenArray.length ; i++) {
			if (chosenArray[i]) {
				chosenArray[i] = false;
			}
		}
	}
	
	public void refreshChooseItem(int chosenIndex) {
		resetChosen();
		chosenArray[chosenIndex] = true;
		notifyDataSetChanged();
	}

	public int getChosenIndex() {
		for (int i = 0 ; i < chosenArray.length ; i++) {
			if (chosenArray[i]) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.wallpaper_gridview_item, null);
			holder = new ViewHolder();
			holder.mThumbImg = (ImageView) convertView.findViewById(R.id.wallpaper_thumb_img);
			holder.mChosenImg = (ImageView) convertView.findViewById(R.id.chosen_imageview);
			holder.mWallpaperName = (TextView) convertView.findViewById(R.id.wallpaper_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
//		holder.mWallpaperImg.setImageDrawable(list.get(position).getWallpaperImg());
		holder.mThumbImg.setImageResource(list.get(position).getThumbId());
		holder.mWallpaperName.setText(list.get(position).getWallpaperName());
		if (chosenArray[position]) {
			holder.mChosenImg.setVisibility(View.VISIBLE);
		} else {
			holder.mChosenImg.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	class ViewHolder {
		ImageView mThumbImg, mChosenImg;
		TextView mWallpaperName;
	}

}
