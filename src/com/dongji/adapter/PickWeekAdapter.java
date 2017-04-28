package com.dongji.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dongji.launcher.R;
import com.dongji.tool.TimeCounter;

public class PickWeekAdapter extends BaseAdapter {

	Context context;
	List<Long> times = new ArrayList<Long>();
	Calendar cal;
	
	public PickWeekAdapter(Context context , List<Long> times )
	{
		this.context = context;
		this.times = times;
		
		cal = Calendar.getInstance();
	}
	
	@Override
	public int getCount() {
		return times.size();
	}

	@Override
	public Object getItem(int position) {
		return times.get(position);
		
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		convertView = LayoutInflater.from(context).inflate(R.layout.pick_week_list_item, null);
		
		TextView tv = (TextView) convertView.findViewById(R.id.tv);
		
		
		cal.clear();
		Date date = new Date(times.get(position));
		cal.setTime(date);
		
		int week_of_year = cal.get(Calendar.WEEK_OF_YEAR);
		
		tv.setText(TimeCounter.getTimeStrMMDD2(times.get(position))+" - "+TimeCounter.getTimeStrMMDD2(times.get(position) + (long)7* (long)24 *(long)60 *(long)60 *(long)1000 - (long)1) + " ( 第"+week_of_year+"周 ) ");
		
		System.out.println(" --- -");
		
		return convertView;
	}
}
