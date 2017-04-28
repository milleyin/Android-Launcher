package com.dongji.adapter;

import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.ApplicationInfo;

import com.dongji.launcher.R;
import com.dongji.launcherwidget.weather.WeatherInfo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CityListAdapter extends BaseAdapter {

	List<WeatherInfo>  list;
	Context context;
	
	public CityListAdapter(Context context,List<WeatherInfo> list) {
	
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return 20;
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
		
		if ( position < 20){

			convertView = LayoutInflater.from(context).inflate(R.layout.citylistitem, null);
			
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.cityName = (TextView) convertView.findViewById(R.id.citynameinfo);
			
			WeatherInfo info = list.get(position);
			
			viewHolder.cityName.setText(info.getCity());
			viewHolder.cityName.setTag(info.getCode());
		
		}

		
		return convertView;
	}
	
	class ViewHolder{
		TextView cityName;
	}

}
