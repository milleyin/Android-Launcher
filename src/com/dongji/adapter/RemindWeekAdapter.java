package com.dongji.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongji.enity.RemindBean;
import com.dongji.enity.RemindWeekBean;
import com.dongji.launcher.R;
import com.dongji.tool.TimeCounter;


public class RemindWeekAdapter extends BaseAdapter {

	Context c ;
	
	List<RemindWeekBean> rwbs = new ArrayList<RemindWeekBean>();

	OnClickListener onClickListener;

	int cur_week_day; //当前是周几
	
	int type; // 0:过去周　, 1:当前周  , 2:未来周
	
	int item_height;
//	LinearLayout.LayoutParams lp ;
	
	boolean isBig = false;
	
	public RemindWeekAdapter(Context c, List<RemindWeekBean> rwbs,
			OnClickListener onClickListener) {
		this.c = c;
		this.rwbs = rwbs;
		this.onClickListener = onClickListener;
		
		Calendar cal = Calendar.getInstance();
		Date date = new Date(System.currentTimeMillis());
    	cal.setTime(date);
    	
    	int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
    	
    	if(day_of_week == 1)
    	{
    		day_of_week = 7;
    	}else{
    		day_of_week --;
    	}
    	
    	cur_week_day = day_of_week;

    	long s_time = rwbs.get(0).getTime();
    	long e_time = rwbs.get(rwbs.size()-1).getTime();
    	
    	long time = System.currentTimeMillis();
    	
    	if(time >s_time && time<e_time)
    	{
    		type = 1;
    	}else if(time>s_time)
    	{
    		type = 0;
    	}else{
    		type =2;
    	}
    	
    	Display currDisplay = ((WindowManager)c.getSystemService(c.WINDOW_SERVICE)).getDefaultDisplay();
        int displayWidth = currDisplay.getWidth();
        int displayHeight = currDisplay.getHeight();

        DisplayMetrics dm = new DisplayMetrics();   
        ((WindowManager)c.getSystemService(c.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);   
      
        
//        System.out.println(" displayHeight --->" + displayHeight + "   dm.density ---->" +   dm.density);
        if(displayHeight>displayWidth && displayHeight > 960)
        {
        	isBig = true;
        }
        
        isBig = false;
//        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        
//        if(displayWidth<displayHeight)
//        {
//        	int columns = AlmostNexusSettingsHelper.getDesktopColumns(c);
//        	int rows = AlmostNexusSettingsHelper.getDesktopRows(c);
//        	
//        	System.out.println(" columns ---> " + columns + "  rows  --->" +rows);
//        	System.out.println(" displayHeight  --->" + displayHeight);
//        	
//        	item_height = ((displayHeight-c.getResources().getDimensionPixelSize(R.dimen.naviagtion_bar_height)-c.getResources().getDimensionPixelSize(R.dimen.mini_launcher_h)-120)/rows);
//        
//        	lp.height = item_height;
//        	System.out.println(" item_height  ---> " + item_height);
//        }else{ 
//        	lp.height = c.getResources().getDimensionPixelSize(R.dimen.remindweek_hight_land);
//        }
	}

	
	@Override
	public int getCount() {
		return rwbs.size();
	}

	
	@Override
	public Object getItem(int position) {
		return null;
	}

	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = new ViewHolder();
		convertView = LayoutInflater.from(c).inflate(R.layout.remind_week_item_list_item, null);
		holder.tv_week_day = (TextView)convertView.findViewById(R.id.tv_week_day);
		holder.ln_item_contanier = (LinearLayout)convertView.findViewById(R.id.ln_item_contanier);
		
		RemindWeekBean rwb = rwbs.get(position);
		
		boolean isEnableDay = true;
		
		if(rwb.getDay()<cur_week_day)
		{
			isEnableDay = false;
		}
		
		holder.tv_week_day.setText(rwb.getWeek_day());
		
		
		if(type == 0)
		{
			holder.tv_week_day.setTextColor(c.getResources().getColor(R.color.remind_text_color_disable));
		}else if(type ==2 ){
			holder.tv_week_day.setTextColor(c.getResources().getColor(R.color.text_color_base));
		}else{
			
			if(!isEnableDay)
			{
				holder.tv_week_day.setTextColor(c.getResources().getColor(R.color.remind_text_color_disable));
			}
		}
		
		
		List<RemindBean> rbs = rwb.getRbs();
		for(int i =0;i<rbs.size();i++)
		{
			RemindBean rb = rbs.get(i);
			View v;
			if(!isBig)
			{
				v  = LayoutInflater.from(c).inflate(R.layout.remind_week_item_list_item_item, null);
			}else{
				v  = LayoutInflater.from(c).inflate(R.layout.remind_week_item_list_item_item_big, null);
			}
			
			LinearLayout l = ((LinearLayout)v.findViewById(R.id.item));
			l.setTag(rb.getId()+":"+rb.getTemp_start_time());
			l.setOnClickListener(onClickListener);
//			l.setLayoutParams(lp);
			
			TextView tv_date = ((TextView)v.findViewById(R.id.tv_date));
			tv_date.setText(TimeCounter.getTimeStrYYMMDDHHMM(rb.getTemp_start_time())+"至" + TimeCounter.getTimeStrYYMMDDHHMM(rb.getTemp_end_time()));
			
			TextView tv_thing = ((TextView)v.findViewById(R.id.tv_thing));
			tv_thing.setText(rb.getContent());
			
			if(type == 0)
			{
				tv_date.setTextColor(c.getResources().getColor(R.color.remind_text_color_disable));
				tv_thing.setTextColor(c.getResources().getColor(R.color.remind_text_color_disable));
			}else if(type ==2 ){
				tv_date.setTextColor(c.getResources().getColor(R.color.text_color_base));
				tv_thing.setTextColor(c.getResources().getColor(R.color.text_color_base));
			}else{
				if(!isEnableDay)
				{
					tv_date.setTextColor(c.getResources().getColor(R.color.remind_text_color_disable));
					tv_thing.setTextColor(c.getResources().getColor(R.color.remind_text_color_disable));
				}
			}
			
			holder.ln_item_contanier.addView(v);
			
			if(i!=rbs.size()-1)
			{
				TextView tv = new TextView(c);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lp.height = 1;
				tv.setLayoutParams(lp);
				tv.setBackgroundResource(R.color.remind_line_color);
				holder.ln_item_contanier.addView(tv);
			}
		}
		
		if(rbs.size()==0)
		{
			View v;
			if(!isBig)
			{
				v  = LayoutInflater.from(c).inflate(R.layout.remind_week_item_list_item_item, null);
			}else{
				v  = LayoutInflater.from(c).inflate(R.layout.remind_week_item_list_item_item_big, null);
			}
			
			LinearLayout l = ((LinearLayout)v.findViewById(R.id.item));
			l.setTag(position);
//			l.setOnClickListener(onClickListener);
			l.setClickable(false);
			l.setFocusable(true);
//			l.setLayoutParams(lp);
			
			
			((TextView)v.findViewById(R.id.tv_date)).setText("");
			((TextView)v.findViewById(R.id.tv_thing)).setText("");
			
			holder.ln_item_contanier.addView(v);
		}
		
		return convertView;
	}
	
	class ViewHolder {
		TextView tv_week_day;
		LinearLayout ln_item_contanier;
		
	}

}
