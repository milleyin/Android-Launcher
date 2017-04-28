package com.dongji.adapter;

import java.util.ArrayList;

import org.adw.launcher.ApplicationInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dongji.launcher.R;

public class MyGridViewAdapterMuilt extends BaseAdapter{

	Context context ;
	ArrayList<ApplicationInfo> mApplications;
	 
	boolean[] itemStatus ;
		
	public MyGridViewAdapterMuilt(Context context,ArrayList<ApplicationInfo> mApplications) {
		this.context = context;
		this.mApplications =mApplications;
		
		itemStatus = new boolean[mApplications.size()];
	}

	@Override
	public int getCount() {
		return mApplications.size();
	}

	@Override
	public Object getItem(int position) {
		return mApplications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.add_app_all_item_mutil, null);
		
		ImageView img = (ImageView)convertView.findViewById(R.id.img);
		TextView tv = (TextView)convertView.findViewById(R.id.tv);
		
		img.setBackgroundDrawable(mApplications.get(position).icon);
		tv.setText(mApplications.get(position).title);	
		
		CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb);
	    cb.setOnCheckedChangeListener(new MyCheckBoxChangedListener(position));
		
	    if (itemStatus[position] == true) {
			cb.setChecked(true);
		} else {
			cb.setChecked(false);
		}
	    
		return convertView;
	}
	
	
	public int[] getSelectedItemIndexes() {

		if (itemStatus == null || itemStatus.length == 0) {
			return new int[0];
		} else {
			int size = itemStatus.length;
			int counter = 0;
			for (int i = 0; i < size; i++) {
				if (itemStatus[i] == true)
					++counter;
			}
			int[] selectedIndexes = new int[counter];
			int index = 0;
			for (int i = 0; i < size; i++) {
				if (itemStatus[i] == true)
					selectedIndexes[index++] = i;
			}
			return selectedIndexes;
		}
	};
	
	
	class MyCheckBoxChangedListener implements OnCheckedChangeListener {
		int position;

		MyCheckBoxChangedListener(int position) {
			this.position = position;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			System.out.println("" + position + "Checked?:" + isChecked);
			if (isChecked)
				itemStatus[position] = true;
			else
				itemStatus[position] = false;
		}
	}
}