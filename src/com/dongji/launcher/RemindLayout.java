package com.dongji.launcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dongji.adapter.ContactDetailRemindAdapter;
import com.dongji.adapter.PickWeekAdapter;
import com.dongji.adapter.RemindListAdapter;
import com.dongji.adapter.RemindWeekAdapter;
import com.dongji.enity.RemindBean;
import com.dongji.enity.RemindWeekBean;
import com.dongji.launcher.AddEditRmindLayout.OnFinishEditRemindListener;
import com.dongji.sqlite.DButil;
import com.dongji.sqlite.MyDatabaseUtil;
import com.dongji.tool.TimeCounter;
import com.dongji.ui.ScrollLayout;
import com.dongji.ui.ScrollLayout.OnScrollerFinish;


/**
 * 
 * @author Administrator
 *
 */
public class RemindLayout implements OnClickListener {

	Context context;
	RemindCalendar remindCalendar;
	
	public View view;
	
	
	//周模式 (默认显示)
	LinearLayout ln_rmind_content;
	ScrollLayout week_scroller;
	
//	PopupWindow week_show_remind;
	View week_show_remind_v ;
	
	//列表模式
	LinearLayout list_layout; 
	ListView remind_info;
	
	RemindListAdapter remindListAdapter;
	
	AddEditRmindLayout addEditRmindLayout;
	
	int change = 0; // 0 按周显示 ， 1列表显示
	
	List<RemindBean> contactList = new ArrayList<RemindBean>();
	List<RemindBean> list = new ArrayList<RemindBean>();
	
	
	ContactDetailRemindAdapter contactDetailRemindAdapter;
	
	int week_gap = 0; //0表示本周， 正表示： 上几周   负表示： 下几周

//	TextView tv_cur_week_title;
//	TextView tv_pre_week_title;
//	TextView tv_next_week_title;
	
	TextView tv_title;
	String tmep_title;
	
	ListView lv_cur_week_remind;
	
//	Button btn_back_to_this_week; //返回本周
	
	long cur_week_start_time;
	long cur_week_end_time;
//	PopupWindow pop_pick_week;
	TextView tv_month_title;
	ListView lv_month_week;
	
	List<Long> times;
	
	ScrollLayout month_scroller;
	
	int selected_remind_id;
	long delete_remind_temp_start_time;
	int selected_remind_repeat_type;
//	Dialog dialog_delete_remind;
	View v_delete_remind;
	
	RadioGroup delete_radioGroup;
	RadioButton rbtn_one;
	RadioButton rbtn_all;   
	Button btn_delete_ok;
	Button btn_delete_cancle;
	
	public RemindLayout(Context context, RemindCalendar remindCalendar,TextView tv) {
		
		
		this.context = context;
		this.remindCalendar = remindCalendar;
		this.tv_title = tv;
		
		view = LayoutInflater.from(context).inflate(R.layout.setting_item_11_remind_list, null);
		
		
		ln_rmind_content = (LinearLayout) view.findViewById(R.id.ln_rmind_content);
		week_scroller = (ScrollLayout) view.findViewById(R.id.week_scroller);
		
		
		list_layout = (LinearLayout) view.findViewById(R.id.list_layout);
//		add_ln = (LinearLayout) view.findViewById(R.id.add_all);
//		contact_data = (LinearLayout) view.findViewById(R.id.contact_data);
//		data_title = (TextView) view.findViewById(R.id.data_title);
//		data_vocation = (TextView) view.findViewById(R.id.data_vocation);
//		phone_list = (ListView) view.findViewById(R.id.phone_list);
		remind_info = (ListView) view.findViewById(R.id.remind_info);

		
//		dialog_delete_remind = new Dialog(context,R.style.theme_myDialog);
//		dialog_delete_remind.setContentView(R.layout.dialog_delete_remind);
		
		v_delete_remind = LayoutInflater.from(context).inflate(R.layout.dialog_delete_remind, null);
		
		delete_radioGroup = (RadioGroup)v_delete_remind.findViewById(R.id.delete_radioGroup);
		rbtn_one = (RadioButton)v_delete_remind.findViewById(R.id.rbtn_one);
		rbtn_all = (RadioButton)v_delete_remind.findViewById(R.id.rbtn_all);
		
		btn_delete_ok  = (Button)v_delete_remind.findViewById(R.id.btn_delete_ok);
		btn_delete_ok.setOnClickListener(this);
		btn_delete_cancle = (Button)v_delete_remind.findViewById(R.id.btn_delete_cancle);
		btn_delete_cancle.setOnClickListener(this);
		
		
		queryAllRemind(); //查询全部提醒
		init();
		count();
	}
	
	
	public void setTv_title(TextView tv_title) {
		this.tv_title = tv_title;
	}


	void queryAllRemind()
	{
		contactList.clear();
		Cursor c = DButil.getInstance(context).queryAllRemind();
		
		while (c.moveToNext()) {
			
			RemindBean rb = new RemindBean();
			
			rb.setId(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_ID)));
			
			rb.setContent(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT)));
			
			rb.setContacts(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTACT)));
			rb.setParticipants(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_PARTICIPANT)));
			
			rb.setStart_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_START)));
			rb.setEnd_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_END)));
			
			rb.setRemind_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TYPE)));
			rb.setRemind_num(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_NUM)));
			rb.setRemind_time(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TIME)));
			
			rb.setRepeat_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_TYPE)));
			rb.setRepeat_fre(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_FREQ)));
			
			rb.setRepeat_condition(c.getString(c.getColumnIndex(MyDatabaseUtil.REPEAT_CONDITION)));
			
			rb.setRepeat_start_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_START_TIME)));
			rb.setRepeat_end_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_END_TIME)));
			
			rb.setTime_filter(c.getString(c.getColumnIndex(MyDatabaseUtil.TIME_FILTER)));
			
			contactList.add(rb);
		}
		
		c.close();
	}
	
	void init()
	{
		List<RemindWeekBean> rwbs_n = new ArrayList<RemindWeekBean>();
		for(int k = 0;k<7;k++)
		{
			String sd = "";
			
			if(k==0)
			{
				sd = "周一";
			}else if(k==1)
			{
				sd = "周二";
			}else if(k==2)
			{
				sd = "周三";
			}else if(k==3)
			{
				sd = "周四";
			}else if(k==4)
			{
				sd = "周五";
			}else if(k==5)
			{
				sd = "周六";
			}else if(k==6)
			{
				sd = "周日";
			}
			RemindWeekBean rwbn = new RemindWeekBean(sd, 1, 1);
			rwbs_n.add(rwbn);
		}
		
		View week_item = LayoutInflater.from(context).inflate(
				R.layout.remind_week_item, null);
//		btn_back_to_this_week = (Button) week_item
//				.findViewById(R.id.btn_back_to_this_week);
//		btn_back_to_this_week.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				
//				Calendar cal = Calendar.getInstance();
//		    	
//		    	//获取今天是星期几
//		    	Date date = new Date(System.currentTimeMillis());
//		    	cal.setTime(date);
//		    	
//		    	int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
//		    	
//		    	if(day_of_week == 1)
//		    	{
//		    		day_of_week = 7;
//		    	}else{
//		    		day_of_week --;
//		    	}
//		    	
//		    	int day_gap = 1 - day_of_week; //与周一差几天
//		    	
//		    	long time = System.currentTimeMillis() + day_gap * 24 * 60 * 60 *1000;
//		    	
//		    	Date dd = new Date(time);
//		    	
//		    	cal.clear();
//		    	cal.setTime(dd);
//		    	
//		    	int year_start = cal.get(Calendar.YEAR);
//		    	int month_start = cal.get(Calendar.MONTH);
//		    	int day_start = cal.get(Calendar.DAY_OF_MONTH);
//		    	
//		    	Date d_start = new Date(year_start-1900, month_start, day_start, 0, 0);
//		    	
//		    	//本周  周一 
//		    	long week_start_time =  d_start.getTime();
//		    	//本周  周日
//		    	long week_end_time = week_start_time + (long)7 * (long)24 * (long)60 * (long)60 *(long)1000 - (long)1;
//		    	
//				if (week_gap < 0) {
//					tv_next_week_title.setText(TimeCounter.getTimeStrYYMMDD(week_start_time)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time));
//					week_scroller.setOnScrollerFinish(new OnScrollerFinish() {
//
//						@Override
//						public void onScrollerFinish() {
//							week_gap = 0;
//							count();
//						}
//					});
//					week_scroller.snapToScreen(2);
//
//				} else {
//					tv_pre_week_title.setText(TimeCounter.getTimeStrYYMMDD(week_start_time)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time));
//					week_scroller.setOnScrollerFinish(new OnScrollerFinish() {
//						@Override
//						public void onScrollerFinish() {
//							week_gap = 0;
//							count();
//						}
//					});
//					week_scroller.snapToScreen(0);
//				}
//			}
//		});
//		tv_cur_week_title = (TextView)week_item.findViewById(R.id.tv_title);
//		tv_cur_week_title.setClickable(true);
//		tv_cur_week_title.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				popPickWeek();
//			}
//		});
		
		lv_cur_week_remind = (ListView)week_item.findViewById(R.id.lv_remind_week);
		
		View week_item_pre = LayoutInflater.from(context).inflate(R.layout.remind_week_item, null);
//		tv_pre_week_title = (TextView)week_item_pre.findViewById(R.id.tv_title);
		ListView lv_remind_week_pre = (ListView)week_item_pre.findViewById(R.id.lv_remind_week);
//		tv_title_pre.setText(TimeCounter.getTimeStrYYMMDD(week_start_time - 7* 24 *60 *60 *1000)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time - 7* 24 *60 *60 *1000));
		lv_remind_week_pre.setAdapter(new RemindWeekAdapter(context, rwbs_n, new WeekItemClickListener()));
		
		
		View week_item_next = LayoutInflater.from(context).inflate(R.layout.remind_week_item, null);
//		tv_next_week_title = (TextView)week_item_next.findViewById(R.id.tv_title);
		ListView lv_remind_week_next = (ListView)week_item_next.findViewById(R.id.lv_remind_week);
//		tv_title_next.setText(TimeCounter.getTimeStrYYMMDD(week_start_time + 7* 24 *60 *60 *1000)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time + 7* 24 *60 *60 *1000));
		lv_remind_week_next.setAdapter(new RemindWeekAdapter(context, rwbs_n, new WeekItemClickListener()));
		
		week_scroller.addView(week_item_pre);
		week_scroller.addView(week_item);
		week_scroller.addView(week_item_next);
		
	}
	
	//切换视图布局
	public int  change() {
		
		switch (change) {
		case 0:
			ln_rmind_content.setVisibility(View.GONE);
			list_layout.setVisibility(View.VISIBLE);
			change = 1;// 切换至列表显示

			if (contactDetailRemindAdapter == null) {
				contactDetailRemindAdapter = new ContactDetailRemindAdapter(
						context, contactList,
						new RemindMenuItemClickListener(),remind_info);
				remind_info.setAdapter(contactDetailRemindAdapter);
			}
			
			break;

		case 1:

			ln_rmind_content.setVisibility(View.VISIBLE);
			list_layout.setVisibility(View.GONE);
			change = 0;// 切换至周显示

			week_gap = 0; // 本周
			count();
			
			break;

		default:
			break;
		}
		
		return change;
	}
	
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
			
			case R.id.btn_delete_ok:
				if(delete_radioGroup.getCheckedRadioButtonId()==R.id.rbtn_one && selected_remind_repeat_type != MyDatabaseUtil.REPEAT_TYPE_ONE) //仅删除此条提醒(排除一次性的提醒)
				{
					 long result =  DButil.getInstance(context).updateRemindTimeFilter(selected_remind_id,delete_remind_temp_start_time);
					
					 System.out.println("  result  ---> " + result);
					 
				}else{ //删除全部提醒
					long result = DButil.getInstance(context).delete(selected_remind_id);
					
					System.out.println("  result  ---> " + result);
					
					//取消闹钟服务
					Intent it = new Intent(context, AlarmReceiver.class);
		    		it.putExtra(MyDatabaseUtil.REMIND_ID, selected_remind_id);		
		    		PendingIntent pit = PendingIntent.getBroadcast(context, (int)selected_remind_id, it, 0);
		    		AlarmManager amr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		    		amr.cancel(pit);
				}
				
				updateAfterDelete();
				
				remindCalendar.ln_tip.removeAllViews();
				remindCalendar.ln_tip.setVisibility(View.GONE);
//				dialog_delete_remind.dismiss();
				
				remindCalendar.ln_tip.removeAllViews();
				remindCalendar.ln_tip.setVisibility(View.GONE);
//				week_show_remind.dismiss();
				
				break;
				
			case R.id.btn_delete_cancle:
				remindCalendar.ln_tip.removeAllViews();
				remindCalendar.ln_tip.setVisibility(View.GONE);
				break;
				
			default:
				break;
		}
	}
	
	
	class WeekItemClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			String [] tag_ss = ((String)v.getTag()).split(":");
			
			final int id = Integer.valueOf(tag_ss[0]);
			delete_remind_temp_start_time = Long.valueOf(tag_ss[1]);
			
			selected_remind_id = id;
			
			RemindBean rb = new RemindBean();
			Cursor c = DButil.getInstance(context).queryRemind(id);
			while (c.moveToNext()) {
				
				rb.setId(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_ID)));
				
				rb.setContent(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT)));
				
				rb.setContacts(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTACT)));
				rb.setParticipants(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_PARTICIPANT)));
				
				rb.setStart_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_START)));
				rb.setEnd_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_END)));
				
				rb.setRemind_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TYPE)));
				rb.setRemind_num(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_NUM)));
				rb.setRemind_time(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TIME)));
				
				rb.setRepeat_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_TYPE)));
				rb.setRepeat_fre(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_FREQ)));
				
				rb.setRepeat_condition(c.getString(c.getColumnIndex(MyDatabaseUtil.REPEAT_CONDITION)));
				
				rb.setRepeat_start_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_START_TIME)));
				rb.setRepeat_end_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_END_TIME)));
			}
			c.close();
			
			
			View view = LayoutInflater.from(context).inflate(R.layout.pop_remind_item, null);
			week_show_remind_v = view;
//			week_show_remind = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, true);
//			week_show_remind.setBackgroundDrawable(new BitmapDrawable());  //按返回键  以及点击  区域外 消失  (神奇的语句)
//			week_show_remind.setOutsideTouchable(true);
			
	    	TextView tv_event_time = (TextView) view.findViewById(R.id.tv_event_time);
			TextView tv_remind_num = (TextView) view.findViewById(R.id.tv_remind_num);
			
			TextView tv_repeat_type = (TextView) view.findViewById(R.id.tv_repeat_type);
			
			TextView tv_repeat_freq = (TextView) view.findViewById(R.id.tv_repeat_freq);
			TextView t_repeat_codition = (TextView) view.findViewById(R.id.t_repeat_codition); 
			TextView tv_repeat_time = (TextView) view.findViewById(R.id.tv_repeat_time);  
			
			TextView tv_remind_time = (TextView) view.findViewById(R.id.tv_remind_time);
			
			TextView content = (TextView) view.findViewById(R.id.content);
	    	
			tv_event_time.setText(TimeCounter.getTimeStrYYMMDDHHMM(rb.getStart_time())+" 至 " + TimeCounter.getTimeStrYYMMDDHHMM(rb.getEnd_time()));
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
			
			tv_remind_num.setText("提醒:"+rb.getRemind_num() + " " +remind_unti);
			
			String repeat_unti = "";
			int repeat_type = rb.getRepeat_type();
			selected_remind_repeat_type = repeat_type;
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
				 tv_repeat_type.setText("重复： "+repeat_unti);
				 tv_repeat_freq.setText("重复频率: 无");
				 tv_repeat_time.setVisibility(View.GONE);
			}else{
				 tv_repeat_type.setText("重复： 每"+repeat_unti);
				 tv_repeat_freq.setText("重复频率: "+repeat_freq+repeat_unti );
				 tv_repeat_time.setText("重复开始时间:" + TimeCounter.getTimeStrYYMMDD(rb.getRepeat_start_time())+"  重复结束时间:" + TimeCounter.getTimeStrYYMMDD(rb.getRepeat_end_time()));
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
				
				 t_repeat_codition.setText("重复时间: "+sb.toString());
			}else if(repeat_type == MyDatabaseUtil.REPEAT_TYPE_MONTH){
				if(repeat_condition.equals("1")){
					Calendar car = Calendar.getInstance();
					Date date = new Date(rb.getStart_time());
					car.setTime(date);
					int day = car.get(Calendar.DATE);
					 t_repeat_codition.setText("重复时间: 每月 的 第"+day+"天");
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
			         t_repeat_codition.setText("重复时间: 每月 的 第"+week_of_month+"个星期的周"+dd);
				}
			}else{
				 t_repeat_codition.setVisibility(View.GONE);
			}
			
			 tv_remind_time.setText("提醒次数: " + rb.getRemind_time() + "次");
			 content.setText(rb.getContent());
			 
			 
			 Button btn_pop_edit = (Button)view.findViewById(R.id.btn_pop_edit);
			 btn_pop_edit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					remindCalendar.ln_tip.removeAllViews();
					remindCalendar.ln_tip.setVisibility(View.GONE);
					remindCalendar.tv_title.setText("编辑提醒");
					
					AddEditRmindLayout addEditRmindLayout = new AddEditRmindLayout(context,remindCalendar, id, -1,new OnFinishEditRemindListener() {
						   
						@Override
						public void OnFinishEditRemind() {
							updateAfterDelete();
							remindCalendar.sroller.snapToScreen(1);
							remindCalendar.ln_bottom.setVisibility(View.VISIBLE);
						}
					});
					remindCalendar.ln3.addView(addEditRmindLayout.v);
					remindCalendar.ln_bottom.setVisibility(View.GONE);
					remindCalendar.sroller.snapToScreen(2);
				}
			});
			 
			Button btn_pop_delete = (Button)view.findViewById(R.id.btn_pop_delete);
			btn_pop_delete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						rbtn_one.setChecked(true);
						remindCalendar.ln_tip.removeAllViews();
						remindCalendar.ln_tip.addView(v_delete_remind);
						remindCalendar.ln_tip.setVisibility(View.VISIBLE);
//						dialog_delete_remind.show();
					}
			});
			
			remindCalendar.ln_tip.addView(view);
			remindCalendar.ln_tip.setVisibility(View.VISIBLE);
//			week_show_remind.showAtLocation(list_layout, Gravity.CENTER, 0, 0);
		}
	}
	
	class RemindMenuItemClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(contactDetailRemindAdapter.menu!=null)
			{
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
				lp.setMargins(0, 0, 0, -contactDetailRemindAdapter.menu.getHeight());
				
				contactDetailRemindAdapter.menu.setLayoutParams(lp);
			}
			
			switch (v.getId()) {
			case R.id.menu_edit_remind:
				
				String tag = v.getTag().toString();
				
				String[] s = tag.split(",");
				
				int remind_id = Integer.parseInt(s[0]);
				
				AddEditRmindLayout addEditRmindLayoutnew =  new AddEditRmindLayout(context, remindCalendar,remind_id, -1,new OnFinishEditRemindListener() {
					@Override
					public void OnFinishEditRemind() {  //修改成功的回调刷新
						
						updateAfterDelete();
						remindCalendar.sroller.snapToScreen(1);
//						remindCalendar.ln_bottom.setVisibility(View.VISIBLE);
						remindCalendar.showOrHideTop(true);
					}
				});
				addEditRmindLayoutnew.setReShow(false);
				
				remindCalendar.showOrHideTop(false);
				remindCalendar.tv_title.setText("编辑提醒");
				remindCalendar.ln3.addView(addEditRmindLayoutnew.v);
				remindCalendar.sroller.snapToScreen(2);
				
				
				break;
				
			case R.id.menu_delete_remind: //删除指定的一条提醒
				
				String[] ss = ((String)v.getTag()).split(":");
				
				final int id = Integer.valueOf(ss[0]);
				final int position = Integer.valueOf(ss[1]);
				
				new AlertDialog.Builder(context).setTitle("提示").setIcon(
						android.R.drawable.ic_dialog_info).setMessage("确定删除选中的提醒?").setPositiveButton("确定", new  DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
									
									long result = DButil.getInstance(context).delete(id);
									
									contactDetailRemindAdapter.remove(position);

									//取消闹钟服务
									Intent it = new Intent(context, AlarmReceiver.class);
						    		it.putExtra(MyDatabaseUtil.REMIND_ID, id);		
						    		PendingIntent pit = PendingIntent.getBroadcast(context, (int)id, it, 0);
						    		AlarmManager amr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
						    		amr.cancel(pit);
						    	
						    		updateAfterDelete();
								}
							}).setNegativeButton("取消", null).show();
				break;

			default:
				break;
			}
		}
	}
	
		//周视图相关    计算当前被算中的联系人 下的哪些提醒  是在  下列周时间范围内
	    public void count()
	    {
	    	Calendar cal = Calendar.getInstance();
	    	
	    	//获取今天是星期几
	    	Date date = new Date(System.currentTimeMillis());
	    	cal.setTime(date);
	    	
	    	int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
	    	
	    	if(day_of_week == 1)
	    	{
	    		day_of_week = 7;
	    	}else{
	    		day_of_week --;
	    	}
	    	
	    	int day_gap = 1 - day_of_week; //与周一差几天
	    	
	    	long time = System.currentTimeMillis() + day_gap * 24 * 60 * 60 *1000;
	    	
	    	Date dd = new Date(time);
	    	
	    	cal.clear();
	    	cal.setTime(dd);
	    	
	    	int year_start = cal.get(Calendar.YEAR);
	    	int month_start = cal.get(Calendar.MONTH);
	    	int day_start = cal.get(Calendar.DAY_OF_MONTH);
	    	
	    	Date d_start = new Date(year_start-1900, month_start, day_start, 0, 0);
	    	
	    	//本周  周一 
	    	long week_start_time =  d_start.getTime();
//	    	System.out.println(" 本周  周一的时间为  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(week_start_time));
	    	
	    	//本周  周日
	    	long week_end_time = week_start_time + (long)7 * (long)24 * (long)60 * (long)60 *(long)1000 - (long)1;
//	    	System.out.println(" 本周  周日的时间为  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(week_end_time));
	    	
	    	
	    	//算出当前的周的  周一  00:00   和  周日的  23:59 
	    	week_start_time = week_start_time + ( (long)week_gap * (long)7 * (long)24 * (long)60 * (long)60 * (long)1000);
	    	cur_week_start_time = week_start_time;
	    	week_end_time =  week_end_time + ((long)week_gap * (long)7 * (long)24 * (long)60 * (long)60 * (long)1000);
	    	cur_week_end_time = week_end_time;
	    	
//	    	System.out.println(" 当前周  周一的时间为  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(week_start_time));
//	    	System.out.println(" 当前周  周日的时间为  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(week_end_time));
	    	
	    	//查询当前被选中的联系人  所有有关的    提醒
//	    	String tag = (String)add_ln.getChildAt(selected_position).findViewById(R.id.button_item).getTag();
//			String[] str = tag.split(":");
//			String contactid = str[3];
//	    	
//			List<RemindBean> rbs = new ArrayList<RemindBean>();
//			Cursor c = DButil.getInstance(mainActivity).queryRemindByContactId(contactid);
//			
//			System.out.println(" c.count()  ---> " + c.getCount());
//			while (c.moveToNext()) {
//				
//				RemindBean rb = new RemindBean();
//				
//				rb.setId(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_ID)));
//				
//				rb.setContent(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT)));
//				
//				rb.setContacts(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT)));
//				rb.setParticipants(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_PARTICIPANT)));
//				
//				rb.setStart_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_START)));
//				rb.setEnd_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_END)));
//				
//				rb.setRemind_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TYPE)));
//				rb.setRemind_num(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_NUM)));
//				rb.setRemind_time(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TIME)));
//				
//				rb.setRepeat_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_TYPE)));
//				rb.setRepeat_fre(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_FREQ)));
//				
//				rb.setRepeat_condition(c.getString(c.getColumnIndex(MyDatabaseUtil.REPEAT_CONDITION)));
//				
//				rb.setRepeat_start_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_START_TIME)));
//				rb.setRepeat_end_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_END_TIME)));
//				
//				rb.setTime_filter(c.getString(c.getColumnIndex(MyDatabaseUtil.TIME_FILTER)));
//				
//				rbs.add(rb);
//			}
//			
//			c.close(); //关闭游标
			
			
			//在本周内的提醒
			List<RemindBean> weekBeans = new ArrayList<RemindBean>(); 
			
			for(int i = 0 ;i <contactList.size();i++)
			{
				RemindBean rb = contactList.get(i);
				
				long event_start_time = rb.getStart_time();  //活动开始时间
				long event_end_time = rb.getEnd_time();      //活动结束时间
				int freq = rb.getRepeat_fre(); //频率
				
				int repeat_type = rb.getRepeat_type(); //重复类型
				
				long repeat_end_time = rb.getRepeat_end_time(); //重复结束时间
				
				boolean isMatch = false;
				
				String time_filter = rb.getTime_filter();
//				System.out.println( " time_filter  ---> " + time_filter);
				if(time_filter==null)
				{
					time_filter = "";
				}
				
//				System.out.println(" event_start_time is --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(event_start_time) );
				
				switch (repeat_type) {
				
				case MyDatabaseUtil.REPEAT_TYPE_ONE: //一次性
					
					 
					  
					   if( event_start_time >week_start_time && event_start_time <week_end_time )
					   {
					      isMatch = true;
					   }

					   if( event_end_time > week_start_time && event_end_time < week_end_time )
					   {
					      isMatch = true;
					   }
					   
					   if(event_start_time < week_start_time && event_end_time > week_end_time) //事件持续包含整周
					   {
						  isMatch = true;
					   }

					   if(time_filter.contains(String.valueOf(event_start_time))) //已被过滤掉
					   {
						   isMatch = false;
					   }
					   
					    if(isMatch) //此活动在本周内
					   {
					    	long temp_start_time = event_start_time;
						    long temp_end_time = event_end_time;
					    	
//					    	if(event_start_time<week_start_time) //超过本周开始时间
//				    	    {
//				    	    	temp_start_time = week_start_time;
//				    	    }else{
//				    	    	temp_start_time = event_start_time;
//				    	    }
//				    	    
//				    	    if(event_end_time>week_end_time)    //超过本周结束时间
//				    	    {
//				    	    	temp_end_time = week_end_time;
//				    	    }else{
//				    	    	temp_end_time = event_end_time;
//				    	    }
				    	    
					    	rb.setTemp_start_time(temp_start_time);
					    	rb.setTemp_end_time(temp_end_time);
					    	weekBeans.add(rb);
					    	
//					    	System.out.println(" 一次性 : from " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_start_time) +" to " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_end_time));
					   }
					break;
					
				case MyDatabaseUtil.REPEAT_TYPE_DAY: //天重复
					
//					System.out.println(" 天重复  ----");
					
					if(repeat_end_time>week_start_time)
					{
						long next_start_time = event_start_time;
					    long next_end_time = event_end_time;
					    
					    while(next_start_time < week_end_time)
					    {
					    
//					    System.out.println(" next_start_time: " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(next_start_time) +" week_end_time:  " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(week_end_time));
					    if(next_start_time > repeat_end_time ) //超过重复结束时间
					    {
					    	break;
					    }
					    	
					     if(next_start_time > week_start_time && next_start_time < week_end_time){
					        isMatch  = true;
					      }
					      
					      if(next_end_time > week_start_time && next_end_time < week_end_time){
					        isMatch  = true;
					      } 
					      
					      if(next_start_time < week_start_time && event_end_time > next_end_time) //事件持续包含整周
							{
								  isMatch = true;
							}
					      
					      if(time_filter.contains(String.valueOf(next_start_time))) //已被过滤掉
						   {
							   isMatch = false;
						   }
					      
					      if(isMatch) //此活动在本周内
						   {
					    	  long temp_start_time = next_start_time;
						      long temp_end_time = next_end_time;
					    	    
//					    	    if(next_start_time < week_start_time) //超过本周开始时间
//					    	    {
//					    	    	temp_start_time = week_start_time;
//					    	    }else{
//					    	    	temp_start_time = next_start_time;
//					    	    }
//					    	    
//					    	    if(next_end_time > week_end_time)    //超过本周结束时间
//					    	    {
//					    	    	temp_end_time = week_end_time;
//					    	    }else{
//					    	    	temp_end_time = next_end_time;
//					    	    }
					    	    RemindBean new_rb = rb.copy();
					    	    new_rb.setTemp_start_time(temp_start_time);
					    	    new_rb.setTemp_end_time(temp_end_time);
						    	
//						    	System.out.println(" 天重复: from " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_start_time) +" to " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_end_time));
						    	
						    	weekBeans.add(new_rb);
						   }
					      
					      next_start_time = next_start_time + (long)freq  * (long)24 * (long)60*(long)60*(long)1000;
					      next_end_time = next_end_time + (long)freq  * (long)24 *(long)60*(long)60*(long)1000;
					      
					      isMatch = false;
					   }
					}
					
					break;
					
				case MyDatabaseUtil.REPEAT_TYPE_WEEK: //周重复
					
//					System.out.println(" 周重复  ----");
					
					if(repeat_end_time>week_start_time)
					{
						int repeat_time =0 ; //重复次数
						
						String repeat_condition = rb.getRepeat_condition();
				        String []ss = repeat_condition.split(",");
				        
				        int [] wds; //周重复  的详情  :    {1,3,5}
				        if(repeat_condition.length()==1)
				        {
				        	wds = new int [1];
				        	wds[0] = Integer.valueOf(repeat_condition);
				        }else{
				        	wds = new int [ss.length];
				        	
				        	for(int j = 0;j<ss.length;j++)
				        	{
				        		wds[j] = Integer.valueOf(ss[j]);
				        	}
				        }
				        
						
						long next_start_time = event_start_time;
					    long next_end_time = event_end_time;
					    
					    
					    while(next_start_time<week_end_time)
					    {
					    	if(next_start_time > repeat_end_time ) //超过重复结束时间
					    	{
					    		break;
					    	}
					    	
//					    	System.out.println(" next_start_time: " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(next_start_time) +" next_end_time:  " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(next_end_time));
					    	
					    	if(next_start_time > week_start_time && next_start_time < week_end_time){
					            isMatch  = true;
					         }
					          
					        if(next_end_time > week_start_time && next_end_time < week_end_time){
					            isMatch  = true;
					         }
					        
					        if(next_start_time < week_start_time && event_end_time > next_end_time) //事件持续包含整周
							  {
								  isMatch = true;
							  }
					        
					        if(time_filter.contains(String.valueOf(next_start_time))) //已被过滤掉
							   {
								   isMatch = false;
							   }
					        
					        if(isMatch) //此活动在本周内
					        {
					        	long temp_start_time = next_start_time;
						    	long temp_end_time = next_end_time;
					        	
//					        	 long temp_start_time;
//						    	 long temp_end_time;
//						    	  
//						    	 if(next_start_time < week_start_time) //超过本周开始时间
//						    	 {
//						    	    temp_start_time = week_start_time;
//						    	 }else{
//						    	    temp_start_time = next_start_time;
//						    	 }
//						    	    
//						    	 if(next_end_time > week_end_time)    //超过本周结束时间
//						    	 {
//						    	    temp_end_time = week_end_time;
//						    	 }else{
//						    	    temp_end_time = next_end_time;
//						    	 }
						    	    
						    	RemindBean new_rb = rb.copy();
					    	    new_rb.setTemp_start_time(temp_start_time);
					    	    new_rb.setTemp_end_time(temp_end_time);
							    	
//							    System.out.println(" 周重复: from " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_start_time) +" to " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_end_time));
							    weekBeans.add(new_rb);
					        }
					        
					        //获取已经重复到星期几
					        Date d = new Date(next_start_time);
					        Calendar calendar = Calendar.getInstance();
					        calendar.setTime(d);
					        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
					        
					        if(weekDay==1) //周日
					        {
					        	weekDay = 7;
					        }else{
					        	weekDay -- ;
					        }
					        
					        
					        int index = -1;
					        for(int k = 0 ; k<wds.length;k++)
					        {
					        	if(weekDay==wds[k])
					        	{
					        		index = k;
					        		break;
					        	}
					        }
					        
					        if(index == wds.length-1) //本周重复结束
					        {
					        	 repeat_time++;
					             next_start_time =  event_start_time + (long)repeat_time*(long)(freq)*(long)7*(long)24*(long)60*(long)60*(long)1000;
					             next_end_time = event_end_time + (long)repeat_time*(long)(freq)*(long)7*(long)24*(long)60*(long)60*(long)1000;
					             
					             Date ddd = new Date(next_start_time);
							     Calendar calendars = Calendar.getInstance();
							     calendars.setTime(ddd);
							     int w = calendars.get(Calendar.DAY_OF_WEEK); 
							     if(w==1) //周日
							     {
							        w = 7;
							     }else{
							        w -- ;
							     }
					             
							     int first_week_day = wds[0];
							     
							     next_start_time = next_start_time + (long)(first_week_day - w) * (long)24 *(long)60 *(long)60 * (long)1000;
							     next_end_time = next_end_time + (long)(first_week_day - w) * (long)24 *(long)60 *(long)60 * (long)1000;
							     
					        }else{
					        	int next_week_day =  wds[index+1];
					            int day_crap =  next_week_day - weekDay; //相差多少天
					            next_start_time = next_start_time + (long)day_crap * (long)24*(long)60*(long)60*(long)1000;
					            next_end_time  = next_end_time + (long)day_crap * (long)24*(long)60*(long)60*(long)1000;
					        }
					        
					        isMatch = false;
					    }
					}
					break;
					
					
				case MyDatabaseUtil.REPEAT_TYPE_MONTH:  //月重复
					if(repeat_end_time>week_start_time)
					{
					   long next_start_time = event_start_time;
					   long next_end_time = event_end_time;
					     
					   Date date1 = new Date(event_start_time);
					   Calendar cc = Calendar.getInstance();
					   cc.setTime(date1);
					   
					   int day = cc.get(Calendar.DATE);
					   int year = cc.get(Calendar.YEAR);
					   int month = cc.get(Calendar.MONTH);
					   int hour = cc.get(Calendar.HOUR_OF_DAY);
				       int weekOfMonth = cc.get(Calendar.WEEK_OF_MONTH);
					   int dayOfWeek = cc.get(Calendar.DAY_OF_WEEK);
					   int min = cc.get(Calendar.MINUTE);
					   
					   while(next_start_time < week_end_time)
					   {
						   if(next_start_time > repeat_end_time )//超过重复结束时间
					    	{
					    		break;
					    	}
						   
						   if(next_start_time > week_start_time && next_start_time < week_end_time){
					            isMatch  = true;
					         }
					          
					        if(next_end_time > week_start_time && next_end_time < week_end_time){
					            isMatch  = true;
					         }
					        
					        if(next_start_time < week_start_time && event_end_time > next_end_time) //事件持续包含整周
							{
								  isMatch = true;
							}
					        
					        if(time_filter.contains(String.valueOf(next_start_time))) //已被过滤掉
							   {
								   isMatch = false;
							   }
					        
					        if(isMatch) //此活动在本周内
					        {
					        	long temp_start_time = next_start_time;
						    	long temp_end_time = next_end_time;
						    	  
//						    	 if(next_start_time < week_start_time) //超过本周开始时间
//						    	 {
//						    	    temp_start_time = week_start_time;
//						    	 }else{
//						    	    temp_start_time = next_start_time;
//						    	 }
//						    	    
//						    	 if(next_end_time > week_end_time)    //超过本周结束时间
//						    	 {
//						    	    temp_end_time = week_end_time;
//						    	 }else{
//						    	    temp_end_time = next_end_time;
//						    	 }
						    	    
						    	RemindBean new_rb = rb.copy();
					    	    new_rb.setTemp_start_time(temp_start_time);
					    	    new_rb.setTemp_end_time(temp_end_time);
							    
//							    System.out.println(" 月重复: from " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_start_time) +" to " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_end_time));
							     
							    weekBeans.add(new_rb);
					        }
					        
					        int repeat_condition = Integer.valueOf(rb.getRepeat_condition());
					        
					        if(repeat_condition==1)   //计算下个月的第几天的开始时间 和结束时间
					        {
					        	month = month + freq;
								
								if(month >11)
								{
									 year = year+(month/11);
						    		 month=(month%11)-1;
								}
								
								Date date2 = new Date(Integer.valueOf(year)-1900, Integer.valueOf(month), Integer.valueOf(day), Integer.valueOf(hour), Integer.valueOf(min));
								next_start_time =  date2.getTime();
								next_end_time = next_start_time +(event_end_time-event_start_time);
								
					        }else{  //计算下个月的第几个星期的周几的 开始时间 和结束时间
					        	month= month + freq;
								
								next_start_time =  TimeCounter.weekdatetodata(year, month, weekOfMonth, dayOfWeek, hour, min);
								next_end_time = next_start_time +(event_end_time-event_start_time);
					        }  
					        
					        isMatch = false;
					   }
					}
					break;
					
				case MyDatabaseUtil.REPEAT_TYPE_YEAR: //年重复
					if(repeat_end_time>week_start_time)
					{
						long next_start_time = event_start_time;
						long next_end_time = event_end_time;
						
						Date date1 = new Date(event_start_time);
					    Calendar cc = Calendar.getInstance();
						cc.setTime(date1);
						   
						int day = cc.get(Calendar.DATE);
						int year = cc.get(Calendar.YEAR);
						int month = cc.get(Calendar.MONTH);
						int hour = cc.get(Calendar.HOUR_OF_DAY);
						int min = cc.get(Calendar.MINUTE);
						   
						while(next_start_time < week_end_time)
						{
							if(next_start_time > repeat_end_time ) //超过重复结束时间
					    	{
					    		break;
					    	}
							
							if(next_start_time > week_start_time && next_start_time < week_end_time){
						         isMatch  = true;
						    }
						          
						    if(next_end_time > week_start_time && next_end_time < week_end_time){
						         isMatch  = true;
						    }
						        
						    if(next_start_time < week_start_time && event_end_time > next_end_time) //事件持续包含整周
							  {
								  isMatch = true;
							  }
						       
						    if(time_filter.contains(String.valueOf(next_start_time))) //已被过滤掉
							   {
								   isMatch = false;
							   }
						    
						    if(isMatch) //此活动在本周内
						    {
						    	long temp_start_time = next_start_time;
						    	long temp_end_time = next_end_time;
							    	  
//							    if(next_start_time < week_start_time) //超过本周开始时间
//							    {
//							    	  temp_start_time = week_start_time;
//							    }else{
//							    	  temp_start_time = next_start_time;
//							    }
//							    	    
//							    if(next_end_time > week_end_time)    //超过本周结束时间
//							    {
//							    	  temp_end_time = week_end_time;
//							    }else{
//							    	 temp_end_time = next_end_time;
//							    }
							    	    
						    	RemindBean new_rb = rb.copy();
					    	    new_rb.setTemp_start_time(temp_start_time);
					    	    new_rb.setTemp_end_time(temp_end_time);
								    	
//								System.out.println(" 年重复: from " +  new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_start_time) +" to " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_end_time));
								
								weekBeans.add(new_rb);
						        }
						        
						        year = year + freq;
								Date date3 = new Date(Integer.valueOf(year)-1900, Integer.valueOf(month), Integer.valueOf(day), Integer.valueOf(hour), Integer.valueOf(min));
								next_start_time =  date3.getTime();
								next_end_time = next_start_time +(event_end_time-event_start_time);
						        
						        isMatch = false;
						}
					}
					break;
				
				default:
					break;
				}
				
			}
			
			//排序
			List<RemindWeekBean> rwbs = new ArrayList<RemindWeekBean>();
			
			for(int k = 0;k<7;k++)
			{
				long s = week_start_time+ (long)k *(long)24 * (long)60 * (long)60 *(long)1000;
				long e = week_start_time+ (long)(k+1) *(long)24 * (long)60 * (long)60 *(long)1000 - (long)1;
				
				String sd = "";
				int day = 0;
				long s_time = 0;
				
				if(k==0)
				{
					sd = "周一";
					day = 1;
					s_time = s;
				}else if(k==1)
				{
					sd = "周二";
					day = 2;
				}else if(k==2)
				{
					sd = "周三";
					day = 3;
				}else if(k==3)
				{
					sd = "周四";
					day = 4;
				}else if(k==4)
				{
					sd = "周五";
					day = 5;
				}else if(k==5)
				{
					sd = "周六";
					day = 6;
				}else if(k==6)
				{
					sd = "周日";
					day = 7;
					s_time = e;
				}
				
//				System.out.println(" k  --->" + k);
//				System.out.println(" s : --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(s));
//				System.out.println(" e : --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(e));
				
				RemindWeekBean rwb =  new RemindWeekBean(sd,s,e);
				rwb.setDay(day);
				rwb.setTime(s_time);
				rwbs.add(rwb);
			}
			
			//标记
			for(int j = 0 ; j<weekBeans.size() ;j++)
			{
				RemindBean rb = weekBeans.get(j);
				long  temp_start_time = rb.getTemp_start_time();
				long  temp_end_time = rb.getTemp_end_time();
				
				for(int k = 0;k<rwbs.size();k++)
				{
					RemindWeekBean rwb = rwbs.get(k);
					long week_day_start_time = rwb.getWeek_day_start_time();
					long week_day_end_time = rwb.getWeek_day_end_time();
					
					if( !(temp_end_time < week_day_start_time)  &&  !(temp_start_time > week_day_end_time))
					{
						rwb.getRbs().add(rb);
					}
				}
			}

//			tv_cur_week_title.setText(TimeCounter.getTimeStrYYMMDD(week_start_time)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time));
			
			tmep_title = " 提醒列表    "+TimeCounter.getTimeStrYYMMDD(week_start_time)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time);
			tv_title.setText(tmep_title);
			
//			tv_next_week_title.setText(TimeCounter.getTimeStrYYMMDD(week_start_time + (long)7* (long)24 *(long)60 *(long)60 *(long)1000)+ " 至   "+ TimeCounter.getTimeStrYYMMDD(week_end_time + 7* 24 *60 *60 *1000));
			
			RemindWeekAdapter remindWeekAdapter = new RemindWeekAdapter(context, rwbs, new WeekItemClickListener());
			lv_cur_week_remind.setAdapter(remindWeekAdapter);
			
			week_scroller.setToScreen(1); //显示中间的屏幕
			
//			if(week_gap==0)
//			{
//				btn_back_to_this_week.setVisibility(View.GONE);
//			}else{
//				btn_back_to_this_week.setVisibility(View.VISIBLE);
//			}
			
			System.out.println(" count  finishe ---- ");
	    }
	    
	    public void preWeek()
	    {
//	    	System.out.println(" preWeek ---- ");
	    	week_scroller.setOnScrollerFinish(new OnScrollerFinish() { 
				
				@Override
				public void onScrollerFinish() {
					System.out.println(" onScrollerFinish  get in ");
					week_gap--;
					
					count();
//					System.out.println(" week_gap --->" + week_gap);
					
				}
			});
	    	week_scroller.snapToScreen(0);
	    	
	    }
	    
	    public void nextWeek(){
                week_scroller.setOnScrollerFinish(new OnScrollerFinish() { 
				
				@Override
				public void onScrollerFinish() {
					week_gap++;
					count();
//					System.out.println(" week_gap --->" + week_gap);
				}
			});
           week_scroller.snapToScreen(2);
	    }
	    
	    
	    void popPickWeek()
	    {
	    	View view = LayoutInflater.from(context).inflate(R.layout.popup_pick_week, null);
//	    	pop_pick_week = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, true);
//	    	pop_pick_week.setBackgroundDrawable(new BitmapDrawable());  //按返回键  以及点击  区域外 消失  (神奇的语句)
//	    	pop_pick_week.setOutsideTouchable(true);
	    	
	    	LinearLayout btn_pre_month = (LinearLayout) view.findViewById(R.id.btn_pre_month);
	    	btn_pre_month.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//先滑动，后加载
					
					month_scroller.setOnScrollerFinish(new OnScrollerFinish() { 
						
						@Override
						public void onScrollerFinish() {
							long t = times.get(0);
							
//							int s_month = TimeCounter.getMonth(t- (long)7* (long)24 *(long)60 *(long)60 *(long)1000);
//							int e_month = TimeCounter.getMonth(t );
							
//							System.out.println("  t :  " + TimeCounter.getTimeStrYYMMDD(t));
//							System.out.println("  t _ s :  " + TimeCounter.getTimeStrYYMMDD(t - (long)14* (long)24 *(long)60 *(long)60 *(long)1000));
//							System.out.println("  t _ e:  " + TimeCounter.getTimeStrYYMMDD(t - (long)7* (long)24 *(long)60 *(long)60 *(long)1000 - (long)1));
							
							countWeekOfMonth(t - (long)14* (long)24 *(long)60 *(long)60 *(long)1000 ,t - (long)7* (long)24 *(long)60 *(long)60 *(long)1000 - (long)1 );
							
							lv_month_week.setAdapter(new PickWeekAdapter(context, times));
					    	month_scroller.setToScreen(1);
						}
					});
					
					month_scroller.snapToScreen(0);
				}
			});
	    	
	    	LinearLayout btn_next_month = (LinearLayout) view.findViewById(R.id.btn_next_month);
	    	btn_next_month.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					month_scroller.setOnScrollerFinish(new OnScrollerFinish() { 
						
						@Override
						public void onScrollerFinish() {
							int last = times.size() - 1;
							long t = times.get(last);
							
//							System.out.println("  t :  " + TimeCounter.getTimeStrYYMMDD(t));
//							System.out.println("  t _ s :  " + TimeCounter.getTimeStrYYMMDD(t + (long)7* (long)24 *(long)60 *(long)60 *(long)1000));
//							System.out.println("  t _ e:  " + TimeCounter.getTimeStrYYMMDD(t + (long)14* (long)24 *(long)60 *(long)60 *(long)1000 - (long)1));
							
							countWeekOfMonth(t + (long)7* (long)24 *(long)60 *(long)60 *(long)1000, t + (long)14* (long)24 *(long)60 *(long)60 *(long)1000 - (long)1 );
							
							lv_month_week.setAdapter(new PickWeekAdapter(context, times));
					    	month_scroller.setToScreen(1);
						}
					});
					month_scroller.snapToScreen(2);
				}
			});
	    	
	    	
	    	tv_month_title = (TextView) view.findViewById(R.id.tv_month_title);
	    	lv_month_week = (ListView)view.findViewById(R.id.lv_month_week);
	    	countWeekOfMonth(cur_week_start_time,cur_week_end_time);
	    	lv_month_week.setAdapter(new PickWeekAdapter(context, times));
	    	
	    	lv_month_week.setOnItemClickListener(new OnItemClickListener() {  //点击跳转到指定的周

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					remindCalendar.ln_tip.removeAllViews();
					remindCalendar.ln_tip.setVisibility(View.GONE);
//					pop_pick_week.dismiss();
					
					long t = times.get(position);
					
					Calendar cal = Calendar.getInstance();
			    	
			    	//获取今天是星期几
			    	Date date = new Date(System.currentTimeMillis());
			    	cal.setTime(date);
			    	
			    	int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
			    	
			    	if(day_of_week == 1)
			    	{
			    		day_of_week = 7;
			    	}else{
			    		day_of_week --;
			    	}
			    	
			    	int day_gap = 1 - day_of_week; //与周一差几天
			    	
			    	long time = System.currentTimeMillis() + day_gap * 24 * 60 * 60 *1000;
			    	
			    	Date dd = new Date(time);
			    	
			    	cal.clear();
			    	cal.setTime(dd);
			    	
			    	int year_start = cal.get(Calendar.YEAR);
			    	int month_start = cal.get(Calendar.MONTH);
			    	int day_start = cal.get(Calendar.DAY_OF_MONTH);
			    	
			    	Date d_start = new Date(year_start-1900, month_start, day_start, 0, 0);
			    	
			    	//本周  周一 
			    	long week_start_time =  d_start.getTime();
			    	
			    	
			    	long t_gap = t - week_start_time;
			    	
			    	week_gap = (int)(t_gap /((long)7* (long)24 *(long)60 *(long)60 *(long)1000));
			    	count();
				}
			});
	    	
	    	month_scroller = (ScrollLayout)view.findViewById(R.id.month_scroller);
	    	month_scroller.setToScreen(1);
	    	
	    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);
	    	remindCalendar.ln_tip.addView(view);
	    	
//	    	pop_pick_week.showAtLocation(list_layout, Gravity.CENTER, 0, 0);
	    }
	    
	    void countWeekOfMonth(long s , long e)
	    {
            times = new ArrayList<Long>();
	    	
	    	long s_time = s;
	    	long e_time = e;
	    	
	    	
	    	//获取当前在哪个月份
	    	
	    	int s_month = TimeCounter.getMonth(s_time);
	    	int e_month = TimeCounter.getMonth(e_time);
	    	
	    	tv_month_title.setText(TimeCounter.getYear(e_time)+"/" + e_month );
	    	
	    	if(s_month!=e_month) // 跨月份    以结束时间月份为准
	    	{
	    		times.add(Long.valueOf(s_time));
	    		
	    		int end_day ;
	    		int month = TimeCounter.getMonth(s_time);
	    		int year = TimeCounter.getYear(s_time);
	    		
	    		String s1 = "1,3,5,7,8,10,12";
	    		
	    		if(month == 2)
	    		{
	    			if ((year % 100 == 0 && year % 400 == 0) || year % 4 == 0) //闰年
	    			{
	    				end_day = 29;
	    			}else{
	    				end_day = 28;
	    			}
	    		}else if(s1.contains(String.valueOf(month))){
	    			end_day = 31;
	    		}else{
	    			end_day = 30;
	    		}
	    		
	    		while(true)
	    		{
	    			s_time = s_time +  (long)7* (long)24 *(long)60 *(long)60 *(long)1000;
	    			e_time = e_time + (long)7* (long)24 *(long)60 *(long)60 *(long)1000;
	    			
	    			times.add(Long.valueOf(s_time));
	    			
	    			if(TimeCounter.getMonth(s_time) != TimeCounter.getMonth(e_time) || TimeCounter.getDay(e_time) == end_day)
	    			{
	    				break;
	    			}
	    		}
	    		
	    	}else{ //没跨月份
	    		
	    		boolean isHave = false;
	    		
	    		long ss_time = s_time;
	    		long ee_time = e_time;
	    		
	    		List<Long> s_times = new ArrayList<Long>();
	    		
	    		while(true) //往前推
	    		{
	    			if(TimeCounter.getDay(ss_time)==1)
	    			{
	    				s_times.add(Long.valueOf(ss_time));
	    				isHave = true;
	    				break;
	    			}
	    			
	    			ss_time =  ss_time  -  (long)7* (long)24 *(long)60 *(long)60 *(long)1000;
	    			ee_time =  ee_time  -  (long)7* (long)24 *(long)60 *(long)60 *(long)1000;
	    			
	    			s_times.add(Long.valueOf(ss_time));
	    			
	    			if(TimeCounter.getDay(ss_time)==1 || TimeCounter.getMonth(ss_time) != TimeCounter.getMonth(ee_time))
	    			{
	    				break;
	    			}
	    		}
	    		
	    		
	    		ss_time = s_time;
	    		ee_time = e_time;
	    		
	    		int end_day ;
	    		int month = TimeCounter.getMonth(ss_time);
	    		int year = TimeCounter.getYear(ss_time);
	    		
	    		String s1 = "1,3,5,7,8,10,12";
	    		
	    		if(month == 2)
	    		{
	    			if ((year % 100 == 0 && year % 400 == 0) || year % 4 == 0) //闰年年
	    			{
	    				end_day = 29;
	    			}else{
	    				end_day = 28;
	    			}
	    		}else if(s1.contains(String.valueOf(month))){
	    			end_day = 31;
	    		}else{
	    			end_day = 30;
	    		}
	    		
	    		List<Long> e_times = new ArrayList<Long>();
	    		while(true) //往后推
	    		{
	    			if(TimeCounter.getDay(ee_time)==end_day)
	    			{
	    				e_times.add(Long.valueOf(ee_time));
	    				isHave = true;
	    				break;
	    			}
	    			
	    			ss_time =  ss_time  + (long)7* (long)24 *(long)60 *(long)60 *(long)1000;
	    			ee_time =  ee_time  +  (long)7* (long)24 *(long)60 *(long)60 *(long)1000;
	    			
	    			e_times.add(Long.valueOf(ss_time));
	    			
	    			if(TimeCounter.getDay(ss_time)==end_day || TimeCounter.getMonth(ss_time) != TimeCounter.getMonth(ee_time))
	    			{
	    				break;
	    			}
	    		}
	    		
	    		for(int i = s_times.size()-1; i>=0 ;i--)
	    		{
	    			times.add(s_times.get(i));
	    		}
	    		if(!isHave)
	    		{
	    			times.add(s_time);
	    		}
	    		
	    		for(Long l : e_times)
	    		{
	    			times.add(l);
	    		}
	    		
	    		for(Long l : times)
	    		{
	    			System.out.println("  月 周 详情:    " + TimeCounter.getTimeStrYYMMDD(l));
	    		}
	    	}
	    }
	    
//	    class MyclickSpan extends ClickableSpan{
//
//			int contactId;
//			String name;
//			String[] phone_list;
//			
//			
//			public MyclickSpan(int contactId, String name, String[] phone_list) {
//				this.contactId = contactId;
//				this.name = name;
//				this.phone_list = phone_list;
//			}
//
//			 @Override
//			    public void updateDrawState(TextPaint ds) {
//			        ds.setColor(ds.linkColor);
//			        ds.setUnderlineText(false);
//			    }
//
//			@Override
//			public void onClick(View widget) {
//				System.out.println(" name ---> " + name);
//			    showPartnerDetail(contactId, name, phone_list);
//			} 
//		}
	    
	    void updatePopRemind(int id)
	    {
	    	RemindBean rb = new RemindBean();
			Cursor c = DButil.getInstance(context).queryRemind(id);
			while (c.moveToNext()) {
				
				rb.setId(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_ID)));
				
				rb.setContent(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT)));
				
				rb.setContacts(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTACT)));
				rb.setParticipants(c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_PARTICIPANT)));
				
				rb.setStart_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_START)));
				rb.setEnd_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_END)));
				
				rb.setRemind_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TYPE)));
				rb.setRemind_num(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_NUM)));
				rb.setRemind_time(c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TIME)));
				
				rb.setRepeat_type(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_TYPE)));
				rb.setRepeat_fre(c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_FREQ)));
				
				rb.setRepeat_condition(c.getString(c.getColumnIndex(MyDatabaseUtil.REPEAT_CONDITION)));
				
				rb.setRepeat_start_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_START_TIME)));
				rb.setRepeat_end_time(c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_END_TIME)));
			}
			c.close();
			
			View view = week_show_remind_v;
			
	    	TextView tv_event_time = (TextView) view.findViewById(R.id.tv_event_time);
			TextView tv_remind_num = (TextView) view.findViewById(R.id.tv_remind_num);
			
			TextView tv_repeat_type = (TextView) view.findViewById(R.id.tv_repeat_type);
			
			TextView tv_repeat_freq = (TextView) view.findViewById(R.id.tv_repeat_freq);
			TextView t_repeat_codition = (TextView) view.findViewById(R.id.t_repeat_codition); 
			TextView tv_repeat_time = (TextView) view.findViewById(R.id.tv_repeat_time);  
			
			TextView tv_remind_time = (TextView) view.findViewById(R.id.tv_remind_time);
			
			TextView content = (TextView) view.findViewById(R.id.content);
	    	
			tv_event_time.setText(TimeCounter.getTimeStrYYMMDDHHMM(rb.getStart_time())+" 至 " + TimeCounter.getTimeStrYYMMDDHHMM(rb.getEnd_time()));
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
			
			tv_remind_num.setText("提醒:"+rb.getRemind_num() + " " +remind_unti);
			
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
				 tv_repeat_type.setText("重复： "+repeat_unti);
				 tv_repeat_freq.setText("重复频率: 无");
				 tv_repeat_time.setVisibility(View.GONE);
			}else{
				 tv_repeat_type.setText("重复： 每"+repeat_unti);
				 tv_repeat_freq.setText("重复频率: "+repeat_freq+repeat_unti );
				 tv_repeat_time.setText("重复开始时间:" + TimeCounter.getTimeStrYYMMDD(rb.getRepeat_start_time())+"  重复结束时间:" + TimeCounter.getTimeStrYYMMDD(rb.getRepeat_end_time()));
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
				
				 t_repeat_codition.setText("重复时间: "+sb.toString());
			}else if(repeat_type == MyDatabaseUtil.REPEAT_TYPE_MONTH){
				if(repeat_condition.equals("1")){
					Calendar car = Calendar.getInstance();
					Date date = new Date(rb.getStart_time());
					car.setTime(date);
					int day = car.get(Calendar.DATE);
					 t_repeat_codition.setText("重复时间: 每月 的 第"+day+"天");
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
			         t_repeat_codition.setText("重复时间: 每月 的 第"+week_of_month+"个星期的周"+dd);
				}
			}else{
				 t_repeat_codition.setVisibility(View.GONE);
			}
			
			 tv_remind_time.setText("提醒次数: " + rb.getRemind_time() + "次");
			 content.setText(rb.getContent());
	    }
	    
	public void updateAfterDelete() {

		queryAllRemind();
		if (change == 0) {
			count();
		} else {
			contactDetailRemindAdapter = new ContactDetailRemindAdapter(context, contactList,new RemindMenuItemClickListener(),remind_info);
			remind_info.setAdapter(contactDetailRemindAdapter);
		}
	}
	
	public void reSetTitile()
	{
		tv_title.setText(tmep_title);
	}

}
