package com.dongji.adapter;

import java.util.ArrayList;

import org.adw.launcher.ApplicationInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.launcher.R;

public class MyGridViewAdapter extends BaseAdapter{

	Context context ;
	ArrayList<ApplicationInfo> mApplications;
	  
	public MyGridViewAdapter(Context context,ArrayList<ApplicationInfo> mApplications) {
		this.context = context;
		this.mApplications =mApplications;
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
		convertView = LayoutInflater.from(context).inflate(R.layout.add_app_all_item, null);
		
		ImageView img = (ImageView)convertView.findViewById(R.id.img);
		TextView tv = (TextView)convertView.findViewById(R.id.tv);
		
		img.setBackgroundDrawable(mApplications.get(position).icon);
		tv.setText(mApplications.get(position).title);	
		
		return convertView;
	}
}