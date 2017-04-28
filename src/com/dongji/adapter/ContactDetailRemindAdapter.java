package com.dongji.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dongji.enity.RemindBean;
import com.dongji.launcher.R;
import com.dongji.sqlite.MyDatabaseUtil;
import com.dongji.tool.TimeCounter;

/**
 * 
 * 最近通话 列表 对应的Adapter
 * 
 * @author Administrator
 * 
 */
public class ContactDetailRemindAdapter  extends BaseAdapter {
	
    Context context;
    ListView lv;
    
    String contactId;
    
    OnClickListener onClickListener;
    
	private List<RemindBean> list = new ArrayList<RemindBean>();

	public View menu;
	int margin_bottom;
	int original_x;

//	List<CheckBox> cbs ;
//  boolean[] itemStatus = new boolean[20];
	
//	boolean isEditMode = false; //是否为多选模式
	
//	public static  String SF_NAME = "systemsetting";
//	public static String SF_KEY_COLLOG_SORT = "collog_sort";
//	SharedPreferences sf ;
//	int sort = 1;
	
	int archo_temp = -5; //修复下弹出框，底部未与底边对其的bug
	
	MyDatabaseUtil myDatabaseUtil;
	int po;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 0);

			
			switch (msg.what) {
			case 0:
				lp.setMargins(0, 0, 0, margin_bottom);

				menu.setLayoutParams(lp);
				menu.postInvalidate();
				break;
				
			case 1:
				
				lp.setMargins(0, 0, 0, margin_bottom);
				menu.setLayoutParams(lp);
				menu.postInvalidate();
				
					lv.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							lv.smoothScrollToPosition(po);
							System.out.println("滑动--->");
						}
					}, 100);
				break;
				
			default:
				break;
			}
			
			System.out.println(" margin_bottom  --->" +margin_bottom);

		};
	};
	
	public ContactDetailRemindAdapter(Context context, List<RemindBean> list, OnClickListener onClickListener,ListView lv)
	{
		
		this.context = context;
		this.lv = lv;
		this.list = list;
		this.onClickListener = onClickListener;
		
	}
	
	@Override
	public int getCount() {
		return list.size();
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
		
		final ViewHolder viewHolder = new ViewHolder();
		
		RemindBean rb = list.get(position);
		
		convertView = LayoutInflater.from(context).inflate(R.layout.remind_list_item, null);
		convertView.setTag(position);
		
		viewHolder.tv_event_time = (TextView) convertView.findViewById(R.id.tv_event_time);
		viewHolder.tv_remind_num = (TextView) convertView.findViewById(R.id.tv_remind_num);
		
		viewHolder.tv_repeat_type = (TextView) convertView.findViewById(R.id.tv_repeat_type);
		
		viewHolder.tv_repeat_freq = (TextView) convertView.findViewById(R.id.tv_repeat_freq);
		viewHolder.t_repeat_codition = (TextView) convertView.findViewById(R.id.t_repeat_codition); 
		viewHolder.tv_repeat_time = (TextView) convertView.findViewById(R.id.tv_repeat_time);  
		
		viewHolder.tv_remind_time = (TextView) convertView.findViewById(R.id.tv_remind_time);
		
		viewHolder.tv_partners = (TextView) convertView.findViewById(R.id.tv_partners);
		
		viewHolder.content = (TextView) convertView.findViewById(R.id.content);
		
		viewHolder.menu_edit_remind = (Button)convertView.findViewById(R.id.menu_edit_remind);
		viewHolder.menu_edit_remind.setTag(rb.getId()+","+contactId);
		viewHolder.menu_edit_remind.setOnClickListener(onClickListener);
		
		viewHolder.menu_delete_remind = (Button)convertView.findViewById(R.id.menu_delete_remind);
		viewHolder.menu_delete_remind.setTag(rb.getId()+":"+position);
		viewHolder.menu_delete_remind.setOnClickListener(onClickListener);
		
		LinearLayout main_layout = (LinearLayout) convertView.findViewById(R.id.main_layout);
		main_layout.setClickable(true);
		main_layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
							popUpMenu(v);
							po= (Integer)v.getTag();
						break;

					default:
						break;
						
					}
					return false;
				}
		});
		
		
		viewHolder.tv_event_time.setText(TimeCounter.getTimeStrYYMMDDHHMM(rb.getStart_time())+" 至 " + TimeCounter.getTimeStrYYMMDDHHMM(rb.getEnd_time()));
		String remind_unti = "";
		
		int remind_type = rb.getRemind_type();
		switch (remind_type) {
		case MyDatabaseUtil.REMIND_TYPE_MIN:
			remind_unti="分钟";
			break;
		case MyDatabaseUtil.REMIND_TYPE_HOUR:
			remind_unti="小时";
			break;
		case MyDatabaseUtil.REMIND_TYPE_DAY:
			remind_unti="天";
			break;
		case MyDatabaseUtil.REMIND_TYPE_WEEK:
			remind_unti="星期";
			break;
			
		default:
			break;
		}
		viewHolder.tv_remind_num.setText("提醒:"+rb.getRemind_num() + " " +remind_unti);
		
		String repeat_unti = "";
		int repeat_type = rb.getRepeat_type();
		
		switch (repeat_type) {
		case MyDatabaseUtil.REPEAT_TYPE_ONE:
			repeat_unti="一次性";
			break;
		case MyDatabaseUtil.REPEAT_TYPE_DAY:
			repeat_unti="天";
			break;
		case MyDatabaseUtil.REPEAT_TYPE_WEEK:
			repeat_unti="周";
			break;
		case MyDatabaseUtil.REPEAT_TYPE_MONTH:
			repeat_unti="月";
			break;
			
		case MyDatabaseUtil.REPEAT_TYPE_YEAR:
			repeat_unti="年";
			break;	
		default:
			break;
		}
		int repeat_freq = rb.getRepeat_fre();
		
		if(repeat_unti.equals("一次性"))
		{
			viewHolder.tv_repeat_type.setText("重复： "+repeat_unti);
			viewHolder.tv_repeat_freq.setText("重复频率: 无");
			viewHolder.tv_repeat_time.setVisibility(View.GONE);
		}else{
			viewHolder.tv_repeat_type.setText("重复： 每"+repeat_unti);
			viewHolder.tv_repeat_freq.setText("重复频率: "+repeat_freq+repeat_unti );
			viewHolder.tv_repeat_time.setText("重复开始时间:" + TimeCounter.getTimeStrYYMMDD(rb.getRepeat_start_time())+"  重复结束时间:" + TimeCounter.getTimeStrYYMMDD(rb.getRepeat_end_time()));
		}
	
		
		String repeat_condition = rb.getRepeat_condition();
		
		if(repeat_type == MyDatabaseUtil.REPEAT_TYPE_WEEK)
		{
			StringBuffer sb = new StringBuffer();
			
			if(repeat_condition.contains("1"))
			{
				sb.append("周一 ");
			}
			
			if(repeat_condition.contains("2"))
			{
				sb.append("周二 ");
			}
			
			if(repeat_condition.contains("3"))
			{
				sb.append("周三 ");
			}
			
			if(repeat_condition.contains("4"))
			{
				sb.append("周四 ");
			}
			
			if(repeat_condition.contains("5"))
			{
				sb.append("周五 ");
			}
			
			if(repeat_condition.contains("6"))
			{
				sb.append("周六 ");
			}
			
			if(repeat_condition.contains("7"))
			{
				sb.append("周日 ");
			}
			
			viewHolder.t_repeat_codition.setText("重复时间: "+sb.toString());
		}else if(repeat_type == MyDatabaseUtil.REPEAT_TYPE_MONTH){
			if(repeat_condition.equals("1")){
				Calendar car = Calendar.getInstance();
				Date date = new Date(rb.getStart_time());
				car.setTime(date);
				int day = car.get(Calendar.DATE);
				viewHolder.t_repeat_codition.setText("重复时间: 每月 的 第"+day+"天");
			}else{
				Calendar car = Calendar.getInstance();
				Date date = new Date(rb.getStart_time());
				car.setTime(date);
		        int week_of_month = car.get(Calendar.WEEK_OF_MONTH);
		        int day_of_week = car.get(Calendar.DAY_OF_WEEK);
		        
		        String dd ="";
		        
		        switch (day_of_week) {
				case 1:
					dd="日";
					break;
                case 2:
                	dd="一";
					break;
					
                case 3:
                	dd="二";
                    break;
                case 4:
                	dd="三";
                    break;
                case 5:
                	dd="四";
                    break;

                case 6:
                	dd="五";
                    break;
                case 7:
                	dd="六";
                    break;
				default:
					break;
				}
		        viewHolder.t_repeat_codition.setText("重复时间: 每月 的 第"+week_of_month+"个星期的周"+dd);
			}
		}else{
			viewHolder.t_repeat_codition.setVisibility(View.GONE);
		}
		
		viewHolder.tv_remind_time.setText("提醒次数: " + rb.getRemind_time() + "次");
		
		viewHolder.content.setText(rb.getContent());
		
		return convertView;
}
	
	private void popUpMenu(View view) {
		if (menu != null && menu != view.findViewById(R.id.menu)) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 0);
			lp.setMargins(0, 0, 0, -menu.getHeight());

			menu.setLayoutParams(lp);
		}

		menu = view.findViewById(R.id.menu);
		final int height = menu.getHeight();
		margin_bottom = ((LinearLayout.LayoutParams) menu.getLayoutParams()).bottomMargin;


		new Thread(new Runnable() {

			@Override
			public void run() {

				if (margin_bottom < archo_temp) {
					
					if(po == getCount()-1)
					{
						margin_bottom =  archo_temp;
						handler.sendEmptyMessage(1);
					}else{
						while (margin_bottom < archo_temp) {
							margin_bottom += 5;
							try {
								Thread.sleep(10);
							} catch (Exception e) {
								e.printStackTrace();
							}
							handler.sendEmptyMessage(0);
						}
					}
				} else {

					while (margin_bottom > -height) {
						margin_bottom -= 5;
						try {
							Thread.sleep(10);
						} catch (Exception e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(0);
					}
				}

			}
		}).start();
	}
	
	
	class ViewHolder {
		TextView tv_event_time; //时间的开始 到 结束
		TextView tv_remind_num; //提醒数值
		
		TextView tv_repeat_type; //重复类型
		TextView tv_repeat_freq; //重复频率
		
		TextView t_repeat_codition;
		TextView tv_repeat_time;  //重复的 开始 和 结束
		
		TextView tv_partners;  //参与人
		TextView content;  //事件内容
		 
		TextView tv_remind_time; //提醒次数
		
		Button menu_edit_remind;
		Button menu_delete_remind;
		
		CheckBox checkBox;
	}
	
	public void  remove(int position)
	{
		list.remove(position);
		notifyDataSetChanged();
	}
}
