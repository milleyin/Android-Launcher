package com.dongji.launcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.adw.launcher.Workspace;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.adapter.RemindContactsAdapter;
import com.dongji.adapter.RemindContactsMutilAdapter;
import com.dongji.enity.ContactBean;
import com.dongji.enity.GroupInfo;
import com.dongji.sqlite.DButil;
import com.dongji.sqlite.MyDatabaseUtil;
import com.dongji.tool.TimeCounter;
import com.dongji.ui.WheelMain;
import com.dongji.ui.WheelMainRepeatRange;

//import android.provider.ContactsContract;
//import android.provider.ContactsContract.CommonDataKinds.StructuredName;
//import android.provider.ContactsContract.Contacts;
//import android.provider.ContactsContract.PhoneLookup;
//import android.provider.ContactsContract.RawContacts;
//import android.provider.ContactsContract.RawContactsEntity;

/**
 * 新建  或  编辑提醒
 * @author Administrator
 *
 */
public class AddEditRmindLayout implements OnClickListener {
	
	int id; 
	
	Context context;
	RemindCalendar remindCalendar;
	
	public View v ;
	
//	TextView tv_top_title; //标题栏
	
	EditText et_content;
	
	Button btn_pick_contact; //选择联系人
	CheckBox cb_me;
	
	Button btn_pick_partner; //选择参与人
	
	Button btn_start_time;
	Button btn_end_time;
	CheckBox cb_all_day;
	
	
	Button save;
	Button cancel;
	
	int remind_type = MyDatabaseUtil.REMIND_TYPE_MIN; //新建时，默认提醒类型为分钟
	int tmep_remind_type_id;
	Button btn_remind_type; //提醒类型选择
	EditText et_remind_num; //提醒数值
	EditText et_remind_time;
	
	int repeat_type  = MyDatabaseUtil.REPEAT_TYPE_ONE ;
	int tmep_repeat_type_id ;
	Button btn_repeat_type; //重复类型选择
	LinearLayout repeat_rate; //重复频率
	EditText et_repeat_freq;
	
	TextView repeat_rate_type;
	
	LinearLayout repeat_time; //重复时间
	LinearLayout ln_repeat_time_week;
	LinearLayout ln_repeat_time_month;
	
//	Dialog pic_repeat_range_dialog;
	View v_pic_repeat_range_dialog;
	WheelMainRepeatRange wheelMainRepeatRange;
	LinearLayout ln_repeat_time_range; //重复的开始时间 和 结束时间
	
	int dialog_type = 0;
	LinearLayout ln_remind_type;
	LinearLayout ln_repeat_type;
	RadioGroup remind_type_rg;
	RadioGroup repeat_type_rg;
	
	String repeat_week_condition = ""; //周重复条件
	CheckBox cb_monday;    //周一
	CheckBox cb_tuesday;   //周二
	CheckBox cb_wednesday; //周三
	CheckBox cb_thursday;  //周四
	CheckBox cb_friday;    //周五
	CheckBox cb_saturday;  //周六
	CheckBox cb_sunday;    //周日
	
	int repeat_month_condition = 1;   //月重复条件  :   1: 每月的第几天   ;   2:每月的第几周的周几
	RadioGroup repeat_time_month_rg;
	RadioButton b_day_in_month;
	RadioButton b_day_in_week_in_month;
	
	OnFinishEditRemindListener onFinishEditRemindListener;
	
//	Dialog dialog = null;
	
//	Dialog pic_date_dialog;
	View v_pic_date_dialog;
	WheelMain wheelMain;
	Button buttonsure;
	Button buttoncancle;
	
	int  pick_time_type ;
	long start_time = -1;  //提醒的开始时间
	long end_time = -1;    //提醒的结束时间
	
//	Dialog choose_remind_type;
	View v_choose_remind_type;
//	Dialog choose_repeat_type;
	View v_choose_repeat_type;
	
	Calendar car;
	
	String contact_str = "" ; //联系人:   #id#:name:p,p,p
	int c_id = -1;
	String partner_str = "";
	int [] p_ids;
	String partner_names = "";
	
	List<ContactBean> contacts;
	boolean isQuerying = false;
	
	Dialog contact_dialog;
	ListView lv_contact;
	RemindContactsAdapter remindContactsAdapter;
	
	int partner_type = 0;  //  0:多选联系人     1:分组内选联系人
	Dialog partake_dialog ;
	ListView lv_partake_contact;
	RemindContactsMutilAdapter remindContactsMutilAdapter;
	ExpandableListView group_expandableListView;
	ArrayList<ArrayList<GroupInfo>> childInfos = new ArrayList<ArrayList<GroupInfo>>();
	ArrayList<GroupInfo> aGroupInfos ;
	
	Button btn_repeat_start_time;
	Button btn_repeat_end_time;
	
	int  repeat_range_type = 0;
	long repeat_start_time = -1;
	long repeat_end_time = -1;
	
	ScrollView scrollView;
	
	boolean isReShow =  true;
	
	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg) {
//			switch (msg.what) {
//			case 0:
//				remindContactsAdapter = new RemindContactsAdapter(mainActivity, contacts);
//				lv_contact.setAdapter(remindContactsAdapter);
//				
//				isQuerying = false;
//				break;
//				
//			case 1:
//				remindContactsMutilAdapter = new RemindContactsMutilAdapter(mainActivity, contacts);
//	 			lv_partake_contact.setAdapter(remindContactsMutilAdapter);
//	 			  
//				isQuerying = false;
//				break;
//				
//			case 2:
//				
//				SmsExpandableListAdapter smsExpandableListAdapter = new SmsExpandableListAdapter(mainActivity,aGroupInfos,childInfos);
//        		
//				group_expandableListView.setAdapter(smsExpandableListAdapter);
//				
//				group_expandableListView.setOnGroupClickListener(new OnGroupClickListener() {
//					
//					@Override
//					public boolean onGroupClick(ExpandableListView parent, View v,
//							int groupPosition, long id) {
//						
//						if(childInfos.get(groupPosition).size()==0)
//						{
//							ArrayList<GroupInfo> infos=null;
//							long groupid=aGroupInfos.get(groupPosition).getGroup_id();
//							if(groupid==-1){
//								infos=getNoGroupContactInfo();
//							}else{
//								infos=getContactsByGroupId(groupid);
//							}
//							
//							if(infos.size()!=0){
//								childInfos.remove(groupPosition);
//								childInfos.add(groupPosition, infos);
//								SmsExpandableListAdapter listAdapter=new SmsExpandableListAdapter(mainActivity, aGroupInfos, childInfos);
//								listAdapter.notifyDataSetChanged();
//							}
//						}
//						return false;
//					}
//				});
//				
//				break;
//
//			default:
//				break;
//			}
			
		};
	};
	
	/**
	 * id -1 为新建  ; 反之为编辑
	 * @param mainActivity
	 * @param id
	 * @param contactId 联系人id ： 不为 -1  则表示创建与某某联系人的提醒
	 * @param onFinishEditRemindListener
	 */
	public AddEditRmindLayout(Context context, RemindCalendar remindCalendar,int id ,int contactId ,OnFinishEditRemindListener onFinishEditRemindListener)
	{
		this.context = context;
		this.remindCalendar = remindCalendar;
		this.onFinishEditRemindListener = onFinishEditRemindListener;
		this.id = id;
		
		car  = Calendar.getInstance();
		
		v = LayoutInflater.from(context).inflate(R.layout.add_remind, null);

//		tv_top_title = (TextView)v.findViewById(R.id.tv_top_title);
		
		et_content = (EditText)v.findViewById(R.id.et_content);
		
		scrollView = (ScrollView)v.findViewById(R.id.scrollView);
		scrollView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_DOWN ||  event.getAction() == MotionEvent.ACTION_MOVE)
				{
					Workspace.isInterceptTouchAble = false;
				}

				if (event.getAction() == MotionEvent.ACTION_UP) {
				
					Workspace.isInterceptTouchAble = true;
				}
				return false;
			}
		});
		
		
//		btn_pick_contact = (Button)v.findViewById(R.id.btn_pick_contact);
//		btn_pick_contact.setOnClickListener(this);
//		cb_me = (CheckBox)v.findViewById(R.id.cb_me);
//		cb_me.setOnCheckedChangeListener(new MeCheckBoxOnCheckChangeListener());
		
		btn_pick_partner = (Button)v.findViewById(R.id.btn_pick_partner);
		btn_pick_partner.setOnClickListener(this);
		
		btn_start_time = (Button)v.findViewById(R.id.btn_start_time);
		btn_start_time.setOnClickListener(this);
		btn_end_time = (Button)v.findViewById(R.id.btn_end_time);
		btn_end_time.setOnClickListener(this);
		cb_all_day = (CheckBox)v.findViewById(R.id.cb_all_day);
		cb_all_day.setOnCheckedChangeListener(new AllDayCheckBoxOnCheckChangeListener());
		
		et_remind_num = (EditText)v.findViewById(R.id.et_remind_num);
		et_remind_time = (EditText)v.findViewById(R.id.et_remind_time);
		
		et_repeat_freq = (EditText)v.findViewById(R.id.et_repeat_freq);
		
		save = (Button) v.findViewById(R.id.save);
		cancel = (Button) v.findViewById(R.id.cancel);
		repeat_rate = (LinearLayout) v.findViewById(R.id.repeat_rate);
		repeat_rate_type = (TextView) v.findViewById(R.id.repeat_rate_type);
		repeat_time = (LinearLayout) v.findViewById(R.id.repeat_time); 
		ln_repeat_time_week = (LinearLayout) v.findViewById(R.id.ln_repeat_time_week);
		ln_repeat_time_month = (LinearLayout) v.findViewById(R.id.ln_repeat_time_month);
		
		ln_repeat_time_range = (LinearLayout) v.findViewById(R.id.ln_repeat_time_range);
		
		btn_remind_type = (Button) v.findViewById(R.id.remind_type);
		btn_repeat_type = (Button) v.findViewById(R.id.repeat_type);
		
		WeekOnCheckChangeListener weekOnCheckChangeListener = new WeekOnCheckChangeListener();
		
		cb_monday = (CheckBox )v.findViewById(R.id.cb_monday);
		cb_monday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		cb_tuesday= (CheckBox )v.findViewById(R.id.cb_tuesday);
		cb_tuesday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		cb_wednesday= (CheckBox )v.findViewById(R.id.cb_wednesday);
		cb_wednesday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		cb_thursday= (CheckBox )v.findViewById(R.id.cb_thursday);
		cb_thursday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		cb_friday= (CheckBox )v.findViewById(R.id.cb_friday);
		cb_friday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		cb_saturday= (CheckBox )v.findViewById(R.id.cb_saturday);
		cb_saturday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		cb_sunday= (CheckBox )v.findViewById(R.id.cb_sunday);
		cb_sunday.setOnCheckedChangeListener(weekOnCheckChangeListener);
		
		
		save.setOnClickListener(this);
		cancel.setOnClickListener(this);
		btn_remind_type.setOnClickListener(this);
		btn_repeat_type.setOnClickListener(this);
		
		
		//时间选择框
//		pic_date_dialog = new Dialog(context, R.style.theme_myDialog);
//		pic_date_dialog.setCanceledOnTouchOutside(true);
		
		View layoutss = LayoutInflater.from(context).inflate(R.layout.pick_remind_start_end_time, null);
		View timePicker1 = layoutss.findViewById(R.id.timePicker1);
		wheelMain = new WheelMain(timePicker1);
		wheelMain.initDateTimePicker();
		buttonsure = (Button) layoutss.findViewById(R.id.buttonsure);
		buttonsure.setOnClickListener(new PickDateClickListener());
		buttoncancle = (Button) layoutss.findViewById(R.id.buttoncancle);
		buttoncancle.setOnClickListener(new PickDateClickListener());
		
		v_pic_date_dialog = layoutss;
//		pic_date_dialog.setContentView(layoutss);
		
		
		//重复范围时间选择对话框
//		pic_repeat_range_dialog = new Dialog(context, R.style.theme_myDialog);
//		pic_repeat_range_dialog.setCanceledOnTouchOutside(true);

		v_pic_repeat_range_dialog = LayoutInflater.from(context).inflate(R.layout.pick_remind_repeat_start_end_time, null);
		View timePicker = v_pic_repeat_range_dialog.findViewById(R.id.timePicker1);
		wheelMainRepeatRange = new WheelMainRepeatRange(timePicker);
		wheelMainRepeatRange.initDateTimePicker();
		buttonsure = (Button) v_pic_repeat_range_dialog.findViewById(R.id.buttonsure);
		buttonsure.setOnClickListener(new RepeatRangeClickListener());
		buttoncancle = (Button) v_pic_repeat_range_dialog.findViewById(R.id.buttoncancle);
		buttoncancle.setOnClickListener(new RepeatRangeClickListener());

//		pic_repeat_range_dialog.setContentView(v_pic_repeat_range_dialog);
		
		repeat_time_month_rg = (RadioGroup)v.findViewById(R.id.repeat_time_month_rg);
		repeat_time_month_rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.b_day_in_month){ //每月的第几天
					repeat_month_condition = 1;
				}else{
					repeat_month_condition = 2;
				}
				
				System.out.println("  repeat_month_condition  ----> " + repeat_month_condition);
			}
		});
		
		b_day_in_month = (RadioButton)repeat_time_month_rg.findViewById(R.id.b_day_in_month);
		b_day_in_week_in_month = (RadioButton)repeat_time_month_rg.findViewById(R.id.b_day_in_week_in_month);
		
		
		//提醒类型
//		choose_remind_type = new Dialog(context,R.style.theme_myDialog);
//		choose_remind_type.setContentView(R.layout.remind_dialog);
		
		v_choose_remind_type = LayoutInflater.from(context).inflate(R.layout.remind_dialog, null);
		remind_type_rg = (RadioGroup) v_choose_remind_type.findViewById(R.id.remind_type_rg);
		remind_type_rg.setOnCheckedChangeListener(onChecked);
       ((Button)v_choose_remind_type.findViewById(R.id.btn_remind_type_cancle)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AddEditRmindLayout.this.remindCalendar.ln_tip.removeAllViews();
				AddEditRmindLayout.this.remindCalendar.ln_tip.setVisibility(View.GONE);
				
				switch (remind_type) {
				case MyDatabaseUtil.REMIND_TYPE_MIN:
                     RadioButton rb1 = (RadioButton) remind_type_rg.findViewById(R.id.remind_type_min);
                     rb1.setChecked(true);
					break;
					
               case MyDatabaseUtil.REMIND_TYPE_HOUR:
            	   RadioButton rb2 = (RadioButton) remind_type_rg.findViewById(R.id.remind_type_hour);
                   rb2.setChecked(true);
					break;
					
               case MyDatabaseUtil.REMIND_TYPE_DAY:
            	   RadioButton rb3 = (RadioButton) remind_type_rg.findViewById(R.id.remind_type_day);
                   rb3.setChecked(true);
					break;
					
               case MyDatabaseUtil.REMIND_TYPE_WEEK:
            	   RadioButton rb4 = (RadioButton) remind_type_rg.findViewById(R.id.remind_type_week);
                   rb4.setChecked(true);
					break;
				default:
					break;
				}
			}
		});
		
       ((Button)v_choose_remind_type.findViewById(R.id.btn_remind_type_ok)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setRemindType();
			}
		});
		
		
		
		//重复类型
//		choose_repeat_type = new Dialog(context,R.style.theme_myDialog);
//		choose_repeat_type.setContentView(R.layout.remind_repeat_dialog);
		
		v_choose_repeat_type  = LayoutInflater.from(context).inflate(R.layout.remind_repeat_dialog, null);
		repeat_type_rg = (RadioGroup) v_choose_repeat_type.findViewById(R.id.repeat_type_rg);
		repeat_type_rg.setOnCheckedChangeListener(onChecked);
		((Button)v_choose_repeat_type.findViewById(R.id.btn_repeat_type_cancle)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AddEditRmindLayout.this.remindCalendar.ln_tip.removeAllViews();
				AddEditRmindLayout.this.remindCalendar.ln_tip.setVisibility(View.GONE);
				
				switch (repeat_type) {
				case MyDatabaseUtil.REPEAT_TYPE_ONE:
                     RadioButton rb1 = (RadioButton) repeat_type_rg.findViewById(R.id.once);
                     rb1.setChecked(true);
					break;
					
               case MyDatabaseUtil.REPEAT_TYPE_DAY:
            	   RadioButton rb2 = (RadioButton) repeat_type_rg.findViewById(R.id.day);
                   rb2.setChecked(true);
					break;
					
               case MyDatabaseUtil.REPEAT_TYPE_WEEK:
            	   RadioButton rb3 = (RadioButton) repeat_type_rg.findViewById(R.id.week);
                   rb3.setChecked(true);
					break;
					
               case MyDatabaseUtil.REPEAT_TYPE_MONTH:
            	   RadioButton rb4 = (RadioButton) repeat_type_rg.findViewById(R.id.month);
                   rb4.setChecked(true);
					break;
					
               case MyDatabaseUtil.REPEAT_TYPE_YEAR:
            	   RadioButton rb5 = (RadioButton) repeat_type_rg.findViewById(R.id.year);
                   rb5.setChecked(true);
					break;

				default:
					break;
				}
			}
		});
		
       ((Button)v_choose_repeat_type.findViewById(R.id.btn_repeat_type_ok)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setRepeatType();
			}
		});
		
		btn_repeat_start_time = (Button) v.findViewById(R.id.btn_repeat_start_time);
		btn_repeat_start_time.setOnClickListener(this);
		btn_repeat_end_time = (Button) v.findViewById(R.id.btn_repeat_end_time);
		btn_repeat_end_time.setOnClickListener(this);
		
		if(id==-1) //新建提醒
		{
			
			//初始开始时间
			start_time = System.currentTimeMillis()+(10*60*1000);
			btn_start_time.setText(getTimeStrYYMMDDHHMM(start_time));
			
			//初始结束时间
			end_time = start_time+(24*60*60*1000);
			btn_end_time.setText(getTimeStrYYMMDDHHMM(end_time));
			
//			if(contactId!=-1) //新建与某某人提醒
//			{
//				tv_top_title.setText("新建提醒");
//			}else{
//				tv_top_title.setText("新建提醒");
//			}
			
		}else{  //编辑提醒
			
//			tv_top_title.setText("编辑提醒");
			
			
			Cursor c = DButil.getInstance(context).queryRemind(id);
			c.moveToNext();
			String content = c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT));
//			contact_str = c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTACT));
//			String [] contact_ss = contact_str.split(":");
			
//			c_id = Integer.valueOf(contact_ss[0].replace("#", ""));
			
//			btn_pick_contact.setText(contact_ss[1]);
			
			et_content.setText(content);
			
			start_time = c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_START));
			btn_start_time.setText(getTimeStrYYMMDDHHMM(start_time));
			end_time = c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_END));
			btn_end_time.setText(getTimeStrYYMMDDHHMM(end_time));
			
			
			if(end_time-start_time == 24*60*60*1000) //全天
			{
				cb_all_day.setChecked(true);
			}
			//提醒
//			safsdf
			
			int remind_num = c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_NUM));
			et_remind_num.setText(String.valueOf(remind_num));
			
			RadioButton r ;
			remind_type = c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TYPE));
			switch (remind_type) {
			case MyDatabaseUtil.REMIND_TYPE_MIN:
//				btn_remind_type.setText("分钟");
				r = (RadioButton)remind_type_rg.findViewById(R.id.remind_type_min);
				r.setChecked(true);
				break;

			case MyDatabaseUtil.REMIND_TYPE_HOUR:
//				btn_remind_type.setText("小时");
				r = (RadioButton)remind_type_rg.findViewById(R.id.remind_type_hour);
				r.setChecked(true);
				break;

			case MyDatabaseUtil.REMIND_TYPE_DAY:
//				btn_remind_type.setText("天");
				r = (RadioButton)remind_type_rg.findViewById(R.id.remind_type_day);
				r.setChecked(true);
				break;
			case MyDatabaseUtil.REMIND_TYPE_WEEK:
//				btn_remind_type.setText("周");
				r = (RadioButton)remind_type_rg.findViewById(R.id.remind_type_week);
				r.setChecked(true);
				break;

			default:
				break;
			}
			
			int remind_time = c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TIME));
			et_remind_time.setText(String.valueOf(remind_time));
			
			//重复
			repeat_type = c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_TYPE));
			
			String repeat_condition = c.getString(c.getColumnIndex(MyDatabaseUtil.REPEAT_CONDITION));
			
			RadioButton rr;
			switch (repeat_type) {
			
			case MyDatabaseUtil.REPEAT_TYPE_ONE:
				rr = (RadioButton)repeat_type_rg.findViewById(R.id.once);
				rr.setChecked(true);
				break;

			case MyDatabaseUtil.REPEAT_TYPE_DAY:
				rr = (RadioButton)repeat_type_rg.findViewById(R.id.day);
				rr.setChecked(true);
				break;

			case MyDatabaseUtil.REPEAT_TYPE_WEEK:
				rr = (RadioButton)repeat_type_rg.findViewById(R.id.week);
				rr.setChecked(true);
				
				this.repeat_week_condition = repeat_condition;
				
				if(repeat_condition.contains("1"))
				{
					cb_monday.setChecked(true);
				}
				
				if(repeat_condition.contains("2"))
				{
					cb_tuesday.setChecked(true);
				}
				
				if(repeat_condition.contains("3"))
				{
					cb_wednesday.setChecked(true);
				}
				
				if(repeat_condition.contains("4"))
				{
					cb_thursday.setChecked(true);
				}
				
				if(repeat_condition.contains("5"))
				{
					cb_friday.setChecked(true);
				}
				
				if(repeat_condition.contains("6"))
				{
					cb_saturday.setChecked(true);
				}
				
				if(repeat_condition.contains("7"))
				{
					cb_sunday.setChecked(true);
				}
				
				break;

			case MyDatabaseUtil.REPEAT_TYPE_MONTH:
				rr = (RadioButton)repeat_type_rg.findViewById(R.id.month);
				rr.setChecked(true);
				
				this.repeat_month_condition = Integer.valueOf(repeat_condition);
				if(repeat_month_condition==1)
				{
					b_day_in_month.setChecked(true);
					
				}else{
					b_day_in_week_in_month.setChecked(true);
				}
				
				Calendar car = Calendar.getInstance();
				Date date = new Date(start_time);
				car.setTime(date);
				int day = car.get(Calendar.DATE);
				b_day_in_month.setText("每月 的 第"+day+"天");
				
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
		        b_day_in_week_in_month.setText("每月 的 第"+week_of_month+"个星期的周"+dd);
				
				break;

			case MyDatabaseUtil.REPEAT_TYPE_YEAR:
				rr = (RadioButton)repeat_type_rg.findViewById(R.id.year);
				rr.setChecked(true);
				break;

			default:
				break;
			}
			
			int repeat_freq = c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_FREQ));
			et_repeat_freq.setText(String.valueOf(repeat_freq));
			
			repeat_start_time = c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_START_TIME));
			btn_repeat_start_time.setText(getTimeStrYYMMDD(repeat_start_time));
			repeat_end_time =  c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_END_TIME));
			btn_repeat_end_time.setText(getTimeStrYYMMDD(repeat_end_time));
			
//			System.out.println(" remind id ---> " + id);
//			
//			System.out.println("start_time ---> " + start_time);
//			System.out.println(" end_time  --->" + end_time);
//			System.out.println(" content  --->" + content);
//			System.out.println(" contact_str  --->" + contact_str);
//			System.out.println(" partner_str  --->" + partner_str);
//			
//			System.out.println(" remind_type  --->" + remind_type); 
//			System.out.println(" remind_num  --->" + remind_num);
//			
//			System.out.println(" repeat_type  --->" + repeat_type); 
//			System.out.println(" repeat_freq  --->" + repeat_freq);
//			
//			System.out.println(" repeat_condition  --->" + repeat_condition);
//			
//			System.out.println(" repeat_start_time  --->" + repeat_start_time);
//			System.out.println(" repeat_end_time  --->" + repeat_end_time);
			
			c.close();
		}
	}
	
	public interface OnFinishEditRemindListener{ 
		
		public void OnFinishEditRemind();
		
	}


	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		    case R.id.btn_start_time:
		    	pick_time_type = 0;
		    	if(start_time!=-1)
		    	{
		    		Date date = new Date(start_time);
			    	car.setTime(date);
			    	wheelMain.setToTime(car.get(Calendar.YEAR), car.get(Calendar.MONTH), car.get(Calendar.DATE), car.get(Calendar.HOUR_OF_DAY), car.get(Calendar.MINUTE));
		    	}
		    	
		    	remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.addView(v_pic_date_dialog);
		    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);
//		    	pic_date_dialog.show();
		    	break;
		    	
		    case R.id.btn_end_time:
		    	pick_time_type =1;
		    	if(end_time!=-1)
		    	{
		    		Date date = new Date(end_time);
			    	car.setTime(date);
			    	wheelMain.setToTime(car.get(Calendar.YEAR), car.get(Calendar.MONTH), car.get(Calendar.DATE), car.get(Calendar.HOUR_OF_DAY), car.get(Calendar.MINUTE));
		    	}
		    	remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.addView(v_pic_date_dialog);
		    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);
		    	break;
		    	
			case R.id.save:
				
				if(check())
				{
					if(id==-1) //添加
					{
						save();
					}else{     //修改编辑
						update();
					}
					
					if(onFinishEditRemindListener!=null)
					{
						onFinishEditRemindListener.OnFinishEditRemind();
					}
				}
				
				Workspace.isInterceptTouchAble = true;
				break;
				
			case R.id.cancel:
				
//				dialog.cancel();
				remindCalendar.sroller.snapToScreen(1);
				
				if(isReShow)
				{
					remindCalendar.ln_bottom.setVisibility(View.VISIBLE);
				}
				remindCalendar.showOrHideTop(true);
				
				Workspace.isInterceptTouchAble = true;
				break;
			
			case R.id.remind_type:
				
				remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.addView(v_choose_remind_type);
		    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);

				
				break;
				
			case R.id.repeat_type:
				
				remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.addView(v_choose_repeat_type);
		    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);
		    	
//				choose_repeat_type.show();
				
				break;	
				
			case R.id.btn_repeat_start_time:
				repeat_range_type =0;
		    	if(repeat_start_time!=-1)
		    	{
		    		Date date = new Date(repeat_start_time);
			    	car.setTime(date);
			    	wheelMainRepeatRange.setToTime(car.get(Calendar.YEAR), car.get(Calendar.MONTH), car.get(Calendar.DATE));
		    	}
		    	
		    	remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.addView(v_pic_repeat_range_dialog);
		    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);
//		    	pic_repeat_range_dialog.show();
				break;
				
			case R.id.btn_repeat_end_time:
				repeat_range_type =1;
		    	if(repeat_end_time!=-1)
		    	{
		    		Date date = new Date(repeat_end_time);
			    	car.setTime(date);
			    	wheelMainRepeatRange.setToTime(car.get(Calendar.YEAR), car.get(Calendar.MONTH), car.get(Calendar.DATE));
		    	}
		    	remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.addView(v_pic_repeat_range_dialog);
		    	remindCalendar.ln_tip.setVisibility(View.VISIBLE);
				break;
				
			default:
				break;
		}
	}
	
	void save()
	{
		String repeat_condition = "0";
		
		if(repeat_type==MyDatabaseUtil.REPEAT_TYPE_WEEK)
		{
			repeat_condition = repeat_week_condition;
		}else if (repeat_type==MyDatabaseUtil.REPEAT_TYPE_MONTH)
		{
			repeat_condition = String.valueOf(repeat_month_condition);
		}
		
//		sdfsdf
//		System.out.println("before ---  ----");
//		System.out.println(" start_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(start_time));
//		System.out.println(" end_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(end_time));
		
		if(cb_all_day.isChecked())
		{
			Date start_date = new Date(start_time);
			car.setTime(start_date);
			Date s_date = new Date(car.get(Calendar.YEAR)-1900, car.get(Calendar.MONTH),car.get(Calendar.DATE), 0, 0);
			start_time = s_date.getTime();
			
			Date end_date = new Date(end_time);
			car.setTime(end_date);
			Date e_date = new Date(car.get(Calendar.YEAR)-1900, car.get(Calendar.MONTH),car.get(Calendar.DATE), 0, 0);
			end_time = e_date.getTime();
			
//			System.out.println("after ---  ----");
//			System.out.println(" start_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(start_time));
//			System.out.println(" end_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(end_time));
		}else {
			long time_gap = end_time - start_time;
			if(time_gap == 24*60*60*1000){
				end_time -=10000;
			}
		}
		
		// 周重复的时间判断
		if (repeat_type == MyDatabaseUtil.REPEAT_TYPE_WEEK) {
			Calendar c = Calendar.getInstance();
			Date dd = new Date(start_time);
			c.setTime(dd);

			System.out.println(" 当前设定的事件开始时间  --->"+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(start_time));

			int cur_week_day = c.get(Calendar.DAY_OF_WEEK);
			if (cur_week_day == 1) {
				cur_week_day = 7;
			} else {
				cur_week_day = cur_week_day - 1;
			}

			String[] w_ss = repeat_week_condition.split(",");

			if (w_ss.length == 0) {
				w_ss = new String[] { repeat_week_condition };
			}
			int index = -1;

			for (int i = 0; i < w_ss.length; i++) {
				if (w_ss[i].equals(String.valueOf(cur_week_day))) {
					index = i;
					break;
				}
			}

			if (index == -1) // 说明没找到
			{
				int jj = -1;
				for (int i = 0; i < w_ss.length; i++) {
					if (Integer.valueOf(w_ss[i]) > cur_week_day) {
						jj = Integer.valueOf(w_ss[i]);
						break;
					}
				}

				int time_crap = 0; //时间差
				
				if (jj == -1) // 直接跳到下次的重复开始周几去
				{
					int day_crap = 7 - cur_week_day + Integer.valueOf(w_ss[0]);
                    time_crap = day_crap * 24 * 60 * 60 * 1000;
					// System.out.println(" 下周的第一个重复  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm").format(start_time));
				} else {
					int day_crap = jj - cur_week_day; // 相差几天
					time_crap = day_crap * 24 * 60 * 60 * 1000;
					// System.out.println(" 本周内 ， 非今天重复  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm").format(start_time));
				}
				
				start_time = start_time + time_crap;
				end_time = end_time + time_crap;
				repeat_start_time = repeat_start_time + time_crap;
				repeat_end_time = repeat_end_time+ time_crap;

			} else { // 找到则不管
			       // System.out.println(" 本周内 ， 当天重复  --->" + new SimpleDateFormat("yyyy-MM-dd HH:mm" ).format(start_time));
			}
		}
		
		
		try {
			//保留
			long id = DButil.getInstance(context).insertRemind(et_content.getText().toString(),contact_str, partner_str, start_time, end_time, remind_type, Integer.valueOf(et_remind_num.getText().toString()),Integer.valueOf(et_remind_time.getText().toString()), repeat_type, Integer.valueOf(et_repeat_freq.getText().toString()), repeat_condition,repeat_start_time,repeat_end_time);
//			System.out.println(" new remind_id ---> " + id);
			//保存成功,触发第一次提醒
			triggerFirstRemind(id);
		} catch (Exception e) {
			e.printStackTrace();
//			Toast.makeText(mainActivity, "出错了", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	//触发第一次提醒
	void triggerFirstRemind(long remind_id)
	{
		long before_time = 0 ;
		
		switch (remind_type) {
		case MyDatabaseUtil.REMIND_TYPE_MIN:
			before_time = Long.valueOf(et_remind_num.getText().toString()) * 60*1000;  //必须为长整型   Long.valueOf(et_remind_num.getText().toString())
			break;
			
		case MyDatabaseUtil.REMIND_TYPE_HOUR:
			before_time = Long.valueOf(et_remind_num.getText().toString()) * 60*60*1000;
			break;
			
		case MyDatabaseUtil.REMIND_TYPE_DAY:
			before_time = Long.valueOf(et_remind_num.getText().toString()) * 24 *60*60*1000;
			System.out.println("Long.valueOf(et_remind_num.getText().toString()) -->" +Long.valueOf(et_remind_num.getText().toString()));
			break;
			
		case MyDatabaseUtil.REMIND_TYPE_WEEK:
			before_time = Long.valueOf(et_remind_num.getText().toString()) * 7*24*60*60*1000;
			break;

		default:
			break;
		}
		
//		System.out.println(" start_time --->" + start_time);
//		System.out.println(" before_time --->" + before_time);
		
		long next_time = start_time - before_time;
		
		System.out.println(" next_time  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(next_time));
		
		//保留
		Intent it = new Intent(context, AlarmReceiver.class);
		it.putExtra(MyDatabaseUtil.REMIND_ID, remind_id);
		
		
		PendingIntent pit = PendingIntent.getBroadcast(context, 3, it, 0);
		AlarmManager amr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		amr.set(AlarmManager.RTC_WAKEUP, next_time ,pit);
		
	}
	
	
	//保存修改的提醒
	void update()
	{
		
        String repeat_condition = "0";
		if(repeat_type==MyDatabaseUtil.REPEAT_TYPE_WEEK)
		{
			repeat_condition = repeat_week_condition;
		}else if (repeat_type==MyDatabaseUtil.REPEAT_TYPE_MONTH)
		{
			repeat_condition = String.valueOf(repeat_month_condition);
		}
		
//		System.out.println("before ---  ----");
//		System.out.println(" start_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(start_time));
//		System.out.println(" end_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(end_time));
		
		if(cb_all_day.isChecked())
		{
			Date start_date = new Date(start_time);
			car.setTime(start_date);
			Date s_date = new Date(car.get(Calendar.YEAR)-1900, car.get(Calendar.MONTH),car.get(Calendar.DATE), 0, 0);
			start_time = s_date.getTime();
			
			Date end_date = new Date(end_time);
			car.setTime(end_date);
			Date e_date = new Date(car.get(Calendar.YEAR)-1900, car.get(Calendar.MONTH),car.get(Calendar.DATE), 0, 0);
			end_time = e_date.getTime();
			
//			System.out.println("after ---  ----");
//			System.out.println(" start_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(start_time));
//			System.out.println(" end_time ---> " + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(end_time));
		}else {
			long time_gap = end_time - start_time;
			if(time_gap == 24*60*60*1000){
				end_time -=10000;
			}
		}
		
		
		//周重复判断
	    if (repeat_type == MyDatabaseUtil.REPEAT_TYPE_WEEK) {
				Calendar c = Calendar.getInstance();
				Date dd = new Date(start_time);
				c.setTime(dd);

				System.out.println(" 当前设定的事件开始时间  --->"+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(start_time));

				int cur_week_day = c.get(Calendar.DAY_OF_WEEK);
				if (cur_week_day == 1) {
					cur_week_day = 7;
				} else {
					cur_week_day = cur_week_day - 1;
				}

//				System.out.println(" cur_week_day ---> " + cur_week_day);
//				System.out.println("repeat_week_condition --->" + repeat_week_condition);
				

				String[] w_ss = repeat_week_condition.split(",");

				if (w_ss.length == 0) {
					w_ss = new String[] { repeat_week_condition };
				}
				int index = -1;

				for (int i = 0; i < w_ss.length; i++) {
					if (w_ss[i].equals(String.valueOf(cur_week_day))) {
						index = i;
						break;
					}
				}

//				System.out.println(" index --->" + index);

				if (index == -1) // 说明没找到
				{
					int jj = -1;
					for (int i = 0; i < w_ss.length; i++) {
						if (Integer.valueOf(w_ss[i]) > cur_week_day) {
							jj = Integer.valueOf(w_ss[i]);
							break;
						}
					}

					int time_crap = 0; //时间差
						
					if (jj == -1) // 直接跳到下次的重复开始周几去
					{
						int day_crap = 7 - cur_week_day + Integer.valueOf(w_ss[0]);
		                time_crap = day_crap * 24 * 60 * 60 * 1000;
						// System.out.println(" 下周的第一个重复  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm").format(start_time));
					} else {
						int day_crap = jj - cur_week_day; // 相差几天
						time_crap = day_crap * 24 * 60 * 60 * 1000;
						// System.out.println(" 本周内 ， 非今天重复  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm").format(start_time));
					}
						
					start_time = start_time + time_crap;
					end_time = end_time + time_crap;
					repeat_start_time = repeat_start_time + time_crap;
					repeat_end_time = repeat_end_time+ time_crap;

				} else { // 找到则不管
					     // System.out.println(" 本周内 ， 当天重复  --->" + new SimpleDateFormat("yyyy-MM-dd HH:mm" ).format(start_time));
				}
			}
		
		long upadte_id = DButil.getInstance(context).updateRemind(this.id, et_content.getText().toString(),contact_str, partner_str, start_time, end_time, remind_type, Integer.valueOf(et_remind_num.getText().toString()),Integer.valueOf(et_remind_time.getText().toString()), repeat_type, Integer.valueOf(et_repeat_freq.getText().toString()), repeat_condition,repeat_start_time,repeat_end_time);
		
		
		long next_time = TimeCounter.getNextTime(start_time, remind_type, Integer.valueOf(et_remind_num.getText().toString()), repeat_type, repeat_condition, Integer.valueOf(et_repeat_freq.getText().toString()),repeat_start_time, repeat_end_time,"");
       
		//保留
        if(next_time!=-1)
        {
        	Intent it = new Intent(context, AlarmReceiver.class);
    		it.putExtra(MyDatabaseUtil.REMIND_ID, upadte_id);		
    		PendingIntent pit = PendingIntent.getBroadcast(context, (int)upadte_id, it, 0);
    		AlarmManager amr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
    		amr.cancel(pit);//先取消 ？
    		amr.set(AlarmManager.RTC_WAKEUP, next_time ,pit);
    		
        }else{ //取消提醒
        	Intent it = new Intent(context, AlarmReceiver.class);
    		it.putExtra(MyDatabaseUtil.REMIND_ID, upadte_id);		
    		PendingIntent pit = PendingIntent.getBroadcast(context, (int)upadte_id, it, 0);
    		AlarmManager amr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
    		amr.cancel(pit);
        }
	}
	
	//检查
	public boolean check()
	{
		
		if(et_content.getText().toString().length()==0)
		{
			Toast.makeText(context, "提醒内容不能为空", 	Toast.LENGTH_SHORT).show();
			return false;
		}
//		
//		if(contact_str.equals(""))
//		{
//			Toast.makeText(mainActivity, "请选择联系人", Toast.LENGTH_SHORT).show();
//			return false;
//		}
		
		if(start_time==-1)
		{
			Toast.makeText(context, "请选择开始时间", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(end_time==-1)
		{
			Toast.makeText(context, "请选择结束时间", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(id==-1 && start_time < System.currentTimeMillis())
		{
			Toast.makeText(context, "开始时间必须大于当前系统时间", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(end_time<=start_time)
		{
			Toast.makeText(context, "结束时间必须大于开始时间", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		
		if(et_remind_num.getText().toString().equals("") || et_remind_num.getText().toString().equals("0"))
		{
			Toast.makeText(context, "请输入大于0的提醒数值", Toast.LENGTH_SHORT).show();
			return false;
		}else{
			try {
				int  i = Integer.valueOf(et_remind_num.getText().toString());
				if(i==0)
				{
					Toast.makeText(context, "请输入大于0的提醒数值", Toast.LENGTH_SHORT).show();
					return false;
				}
				
			} catch (Exception e) {
				Toast.makeText(context, "请输入正确的提醒数值", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		
		if(et_remind_time.getText().toString().equals("") || et_remind_time.getText().toString().equals("0"))
		{
			Toast.makeText(context, "请输入大于0的提醒次数", Toast.LENGTH_SHORT).show();
			return false;
		}else{
			try {
				int  i = Integer.valueOf(et_remind_time.getText().toString());
				
				if(i==0)
				{
					Toast.makeText(context, "请输入大于0的提醒次数", Toast.LENGTH_SHORT).show();
					return false;
				}
				
			} catch (Exception e) {
				Toast.makeText(context, "请输入正确的提醒次数", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		
		if(repeat_type != MyDatabaseUtil.REPEAT_TYPE_ONE && (et_repeat_freq.getText().toString().equals("") || et_repeat_freq.getText().toString().equals("0")))
		{
			Toast.makeText(context, "请输入大于0的重复频率", Toast.LENGTH_SHORT).show();
			return false;
		}else{
			try {
				int i = Integer.valueOf(et_repeat_freq.getText().toString());
				if(i == 0)
				{
					Toast.makeText(context, "请输入大于0的重复频率", Toast.LENGTH_SHORT).show();
					return false;
				}
			} catch (Exception e) {
				Toast.makeText(context, "请输入正确的重复频率", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		
		if(repeat_type == MyDatabaseUtil.REPEAT_TYPE_WEEK && repeat_week_condition.equals(""))
		{
			Toast.makeText(context, "请选择重复时间", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		//自动校正重复开始时间
		if( repeat_type != MyDatabaseUtil.REPEAT_TYPE_ONE &&  repeat_start_time<start_time)
		{
			Date d = new Date(start_time);
			car.setTime(d);
			int y = car.get(Calendar.YEAR);
			int m = car.get(Calendar.MONTH)+1;
			int da = car.get(Calendar.DATE);
	
			btn_repeat_start_time.setText(y+"-"+m+"-"+da);
			repeat_start_time = start_time;
			
			Toast.makeText(context, "重复开始时间不得小于事件开始时间,已修正", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		
		//自动校正重复结束时间
		long t = 0;
		String s = (btn_start_time.getText().toString().split(" "))[0];
		String [] ss = s.split("-");
		int year = Integer.valueOf(ss[0]);
		int month = Integer.valueOf(ss[1]);
		int day = Integer.valueOf(ss[2]);
		switch (repeat_type) {
		case 1: //天
			t = 24*60*60*1000;
			if ( repeat_end_time<start_time+t*Long.valueOf(et_repeat_freq.getText().toString()))
			{
				Date d = new Date(start_time+t*Long.valueOf(et_repeat_freq.getText().toString()));
				car.setTime(d);
				int y = car.get(Calendar.YEAR);
				int m = car.get(Calendar.MONTH)+1;
				int da = car.get(Calendar.DATE);
		
				btn_repeat_end_time.setText(y+"-"+m+"-"+da);
				repeat_end_time =  start_time+t*Long.valueOf(et_repeat_freq.getText().toString());
				
				Toast.makeText(context, "重复结束时间不得小于最小重复时间,已修正", Toast.LENGTH_SHORT).show();
				return false;
			}
			break;
			
		case 2://周
			t = 24*60*60*1000*7;
			if ( repeat_end_time<start_time+t*Long.valueOf(et_repeat_freq.getText().toString()))
			{
				Date d = new Date(start_time+t*Long.valueOf(et_repeat_freq.getText().toString()));
				car.setTime(d);
				int y = car.get(Calendar.YEAR);
				int m = car.get(Calendar.MONTH)+1;
				int da = car.get(Calendar.DATE);
		
				btn_repeat_end_time.setText(y+"-"+m+"-"+da);
				repeat_end_time =  start_time+t*Long.valueOf(et_repeat_freq.getText().toString());
				
				Toast.makeText(context, "重复结束时间不得小于最小重复时间,已修正", Toast.LENGTH_SHORT).show();
				return false;
			}
			break;
			
		case 3://月
//			t = 24*60*60*1000*30;
			
			System.out.println("btn_start_time ---> "+year+"-"+month+"-"+day);
			System.out.println("et_repeat_freq.getText().toString() ---> " + et_repeat_freq.getText().toString());
			
			int new_year;
			int new_month;
			if(month+Integer.valueOf(et_repeat_freq.getText().toString())>12)
			{
				new_year = year+(month+Integer.valueOf(et_repeat_freq.getText().toString()))/12;
				new_month = (month+Integer.valueOf(et_repeat_freq.getText().toString()))%12+1;
				
			}else{
				new_year = year;
				new_month = month+Integer.valueOf(et_repeat_freq.getText().toString());
			}
			
			System.out.println("new_year  --->"+new_year+"-"+new_month+"-"+day);
			
			Date date = new Date(Integer.valueOf(new_year)-1900, Integer.valueOf(new_month)-1, Integer.valueOf(day), Integer.valueOf(23), Integer.valueOf(59));
			
			if(repeat_end_time<date.getTime())
			{
				repeat_end_time = date.getTime();
				btn_repeat_end_time.setText(new_year+"-"+new_month+"-"+day);
				Toast.makeText(context, "重复结束时间不得小于最小重复时间,已修正", Toast.LENGTH_SHORT).show();
				return false;
			}
			
			break;
			
		case 4://年
			Date date_year = new Date(Integer.valueOf(year+Integer.valueOf(et_repeat_freq.getText().toString()))-1900, Integer.valueOf(month)-1, Integer.valueOf(day), Integer.valueOf(23), Integer.valueOf(59));
			if(repeat_end_time<date_year.getTime())
			{
				repeat_end_time = date_year.getTime();
				btn_repeat_end_time.setText(year+Integer.valueOf(et_repeat_freq.getText().toString())+"-"+month+"-"+day);
				Toast.makeText(context, "重复结束时间不得小于最小重复时间,已修正", Toast.LENGTH_SHORT).show();
				return false;
			}
			break;

		default:
			break;
		}
		
		return true;
	}
	
	private OnCheckedChangeListener onChecked = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			
			switch (group.getId()) {
			
				case R.id.remind_type_rg:
					
					RadioButton rb1 = (RadioButton) group.findViewById(checkedId);
					
					btn_remind_type.setText(rb1.getText());
					
				switch (checkedId) {
				case R.id.remind_type_min:
					tmep_remind_type_id = R.id.remind_type_min;
					break;
				case R.id.remind_type_hour:
					tmep_remind_type_id = R.id.remind_type_hour;
					break;
				case R.id.remind_type_day:
					tmep_remind_type_id = R.id.remind_type_day;
					break;
				case R.id.remind_type_week:
					tmep_remind_type_id = R.id.remind_type_week;
					break;
				default:
					break;
				}
				
				System.out.println(" remind_type --->" + remind_type);
//				remindCalendar.ln_tip.removeAllViews();
//		    	remindCalendar.ln_tip.setVisibility(View.GONE);
					break;
					
				case R.id.repeat_type_rg:
					
					RadioButton rb2 = (RadioButton) group.findViewById(checkedId);
					
					if(rb2.getId() == R.id.day){
						
						tmep_repeat_type_id = R.id.day;
						
					}else if(rb2.getId() == R.id.week){
						tmep_repeat_type_id = R.id.week;
					}else if(rb2.getId() == R.id.month){
						
						tmep_repeat_type_id = R.id.month;
						
					}else if(rb2.getId() == R.id.year){
						
						tmep_repeat_type_id = R.id.year;
					}else{
						
						tmep_repeat_type_id = R.id.once;
					}
//					choose_repeat_type.dismiss();
					System.out.println(" repeat_type --->" + repeat_type);
					
					break;
					
				default:
					break;
			}
		}
	};
	
	
//	class MeCheckBoxOnCheckChangeListener implements android.widget.CompoundButton.OnCheckedChangeListener{
//
//		@Override
//		public void onCheckedChanged(CompoundButton buttonView,
//				boolean isChecked) {
//			if(isChecked)
//			{
//				SharedPreferences ss = mainActivity.getSharedPreferences("myNumberContactId", 0);
//				long contactId = ss.getLong("myContactId", -1);
//				
//				Cursor phones = mainActivity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//						new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
//						ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + contactId, null, null);
//				String pp = " ";
//				StringBuffer sb_phones = new StringBuffer();
//				while (phones.moveToNext()) { 
//					String ph = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("(", "").replace(") ", "").replace("-", "").replace(" ", "");
//					sb_phones.append(ph+",");
//				}
//		        phones.close();
//				
//				contact_str = "#"+String.valueOf(contactId)+"#:"+"我:"+pp;
//				btn_pick_contact.setText("我");
//			}else{
//				contact_str = "";
//				btn_pick_contact.setText("选择联系人");
//			}
//		}
//	}
	
	
	//全天 checkbox
	class AllDayCheckBoxOnCheckChangeListener implements android.widget.CompoundButton.OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if(isChecked)
			{
				String [] ss = btn_start_time.getText().toString().split(" ");
				btn_start_time.setText(ss[0]);
				
				String [] sss = btn_end_time.getText().toString().split(" ");
				btn_end_time.setText(sss[0]);
				
			}else{
				if(start_time!=-1)
				{
					Date data = new Date(start_time);
					btn_start_time.setText((data.getYear()+1900)+"-"+(data.getMonth()+1)+"-"+data.getDate()+" "+data.getHours()+":"+data.getMinutes());
				}
				
				if(end_time!=-1)
				{
					Date end_data = new Date(end_time);
					btn_end_time.setText((end_data.getYear()+1900)+"-"+(end_data.getMonth()+1)+"-"+end_data.getDate()+" "+end_data.getHours()+":"+end_data.getMinutes());
				}
			}
		}
	}
	
	
	class WeekOnCheckChangeListener implements android.widget.CompoundButton.OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			 
			StringBuffer sb = new StringBuffer();

			if (cb_monday.isChecked()) {
				sb.append("1,");
			}

			if (cb_tuesday.isChecked()) {
				sb.append("2,");
			}

			if (cb_wednesday.isChecked()) {
				sb.append("3,");
			}

			if (cb_thursday.isChecked()) {
				sb.append("4,");
			}

			if (cb_friday.isChecked()) {
				sb.append("5,");
			}

			if (cb_saturday.isChecked()) {
				sb.append("6,");
			}

			if (cb_sunday.isChecked()) {
				sb.append("7,");
			}
			   
			if (sb.toString().equals("")) {  //一个都没选
				repeat_week_condition="";
//				Date date = new Date(start_time);
//		    	car.setTime(date);
//		    	int day_week = car.get(Calendar.DAY_OF_WEEK);
//		    	
//		    	if(day_week==1)
//		    	{
//		    		repeat_week_condition="7";
//		    	}else{
//		    		repeat_week_condition=String.valueOf(day_week-1);
//		    	}
//		    	int day = Integer.valueOf(repeat_week_condition);
//		    	switch (day) {
//				case 1:
//					cb_monday.setChecked(true);
//					break;
//					
//				case 2:
//					cb_tuesday.setChecked(true);
//					break;
//					
//				case 3:
//					cb_wednesday.setChecked(true);
//					break;
//					
//				case 4:
//					cb_thursday.setChecked(true);
//					break;
//					
//				case 5:
//					cb_friday.setChecked(true);
//					break;
//					
//				case 6:
//					cb_saturday.setChecked(true);
//					break;
//					
//				case 7:
//					cb_sunday.setChecked(true);
//					break;
//
//				default:
//					break;
//				}
		    	
			} else {
				repeat_week_condition = sb.substring(0, sb.length() - 1);
			}

			 System.out.println(" repeat_week_condition ---> " + repeat_week_condition);
		}
	}
	
	
	class PickDateClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.buttonsure:
				
				System.out.println( " wheelMain.getTimemil()  ---> " + wheelMain.getTimemilInFormat());
				
				String time = wheelMain.getTimemilInFormat();
				String [] tt = time.split("-");
				String year =tt[0];
				String month = tt[1];
				String day = tt[2];
				String hour = tt[3];
				String minute = tt[4];
				
				String hourStr = hour;
				if(Integer.valueOf(hour)<=9)
				{
					hourStr="0"+hour;
				}
				
				String minuteStr = minute;
				if(Integer.valueOf(minute)<=9)
				{
					minuteStr="0"+minute;
				}
				
				System.out.println("date: " + year +"-"+ month +"-"+day+" "+hour+":"+minute);
				
				Date date = new Date(Integer.valueOf(year)-1900, Integer.valueOf(month)-1, Integer.valueOf(day), Integer.valueOf(hour), Integer.valueOf(minute));
				System.out.println(" date.getTime();  ----> " + date.getTime());
				
				
				switch (pick_time_type) {
				case 0:
					start_time =  date.getTime();
					if(cb_all_day.isChecked())
					{
						btn_start_time.setText(year +"-"+ month +"-"+day);
					}else{
						btn_start_time.setText(year +"-"+ month +"-"+day+" "+hourStr+":"+minuteStr);
					}
					break;
					
				case 1:
					end_time =  date.getTime();
					if(cb_all_day.isChecked())
					{
						btn_end_time.setText(year +"-"+ month +"-"+day);
					}else{
						btn_end_time.setText(year +"-"+ month +"-"+day+" "+hourStr+":"+minuteStr);
					}
					break;
					
				default:
					break;
				}
				
				
				if(repeat_type == MyDatabaseUtil.REPEAT_TYPE_MONTH)
				{
					//刷新
					Calendar car = Calendar.getInstance();
					car.setTime(date);
					int d = car.get(Calendar.DATE);
					b_day_in_month.setText("每月 的 第"+d+"天");
					
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
			        b_day_in_week_in_month.setText("每月 的 第"+week_of_month+"个星期的周"+dd);
				}
				
				remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.setVisibility(View.GONE);
//				pic_date_dialog.dismiss();
				
				break;
				
            case R.id.buttoncancle:
            	
            	remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		}
	}
	
	class RepeatRangeClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.buttonsure:
				
				System.out.println( " wheelMain.getTimemil()  ---> " + wheelMainRepeatRange.getTimemilInFormat());
				
				String time = wheelMainRepeatRange.getTimemilInFormat();
				String [] tt = time.split("-");
				String year =tt[0];
				String month = tt[1];
				String day = tt[2];
				
				
				System.out.println("date: " + year +"-"+ month +"-"+day);
				
				Date date = new Date(Integer.valueOf(year)-1900, Integer.valueOf(month)-1, Integer.valueOf(day), Integer.valueOf(23), Integer.valueOf(59));
				System.out.println(" date.getTime();  ----> " + date.getTime());
				
				switch (repeat_range_type) {
					
				case 0:
//					if(date.getTime()<start_time) //重复开始时间  小于   事件开始时间
//					{
//						
//						Date d = new Date(start_time);
//						car.setTime(d);
//						int y = car.get(Calendar.YEAR);
//						int m = car.get(Calendar.MONTH)+1;
//						int da = car.get(Calendar.DATE);
//				
//						btn_start_time.setText(y+"-"+m+"-"+da);
//						repeat_start_time = start_time;
//						
//						Toast.makeText(mainActivity, "重复开始时间不得小于事件开始时间", Toast.LENGTH_SHORT).show();
//						
//					}else{
						repeat_start_time =  date.getTime();
						btn_repeat_start_time.setText(year +"-"+ month +"-"+day);
//					}
				
					break;
					
				case 1:
					
//					long t = 0;
//					
//					switch (repeat_type) {
//					case 1: //天
//						t = 24*60*60*1000;
//						break;
//						
//					case 2://周
//						t = 24*60*60*1000*7;
//						break;
//						
//					case 3://月
//						t = 24*60*60*1000*30;
//						break;
//						
//					case 4://年
//						t = 24*60*60*1000*365;
//						break;
//
//					default:
//						break;
//					}
//					
//					if(date.getTime()<start_time+t*Long.valueOf(et_repeat_freq.getText().toString()))
//					{
//						Date d = new Date(start_time+t*Long.valueOf(et_repeat_freq.getText().toString()));
//						car.setTime(d);
//						int y = car.get(Calendar.YEAR);
//						int m = car.get(Calendar.MONTH)+1;
//						int da = car.get(Calendar.DATE);
//				
//						btn_repeat_end_time.setText(y+"-"+m+"-"+da);
//						repeat_end_time =  start_time+t*Long.valueOf(et_repeat_freq.getText().toString());
//						
//						Toast.makeText(mainActivity, "重复开始时间不得小于事件开始时间", Toast.LENGTH_SHORT).show();
//					}else{
						repeat_end_time =  date.getTime();
						btn_repeat_end_time.setText(year +"-"+ month +"-"+day);
//					}
					break;

				default:
					break;
				}
				remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.setVisibility(View.GONE);
//				pic_repeat_range_dialog.dismiss();
				
				break;
				
            case R.id.buttoncancle:
            	remindCalendar.ln_tip.removeAllViews();
		    	remindCalendar.ln_tip.setVisibility(View.GONE);
//            	pic_repeat_range_dialog.dismiss();
            	
				break;

			default:
				break;
			}
		}
	}
	
	private String getTimeStrYYMMDDHHMM(long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		int year = car.get(Calendar.YEAR);
		int month = car.get(Calendar.MONTH)+1;
		int day = car.get(Calendar.DATE);
		int hour = car.get(Calendar.HOUR_OF_DAY);
		int min = car.get(Calendar.MINUTE);
		String hourStr = String.valueOf(hour);
		
		if(Integer.valueOf(hour)<=9)
		{
			hourStr="0"+hour;
		}
		String minuteStr = String.valueOf(min);
		if(Integer.valueOf(min)<=9)
		{
			minuteStr="0"+min;
		}
		return year+"-"+month+"-"+day+" "+hourStr+":"+minuteStr;
	}
	
	private String getTimeStrYYMMDD(long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		int year = car.get(Calendar.YEAR);
		int month = car.get(Calendar.MONTH)+1;
		int day = car.get(Calendar.DATE);
		return year+"-"+month+"-"+day;
	}
	
    void setRepeatType()
    {
		
		if(tmep_repeat_type_id == R.id.day){
			
			repeat_type = MyDatabaseUtil.REPEAT_TYPE_DAY;
			
			repeat_rate.setVisibility(View.VISIBLE);
			repeat_rate_type.setText("天");
			repeat_time.setVisibility(View.GONE);
			
			ln_repeat_time_range.setVisibility(View.VISIBLE);
			
			btn_repeat_type.setText("天");
			
		}else if(tmep_repeat_type_id == R.id.week){
			
			repeat_type = MyDatabaseUtil.REPEAT_TYPE_WEEK;
			
			repeat_rate.setVisibility(View.VISIBLE);
			repeat_rate_type.setText("周");
			repeat_time.setVisibility(View.VISIBLE);
			ln_repeat_time_week.setVisibility(View.VISIBLE);
			ln_repeat_time_month.setVisibility(View.GONE);
			ln_repeat_time_range.setVisibility(View.VISIBLE);
			
			btn_repeat_type.setText("周");
		}else if(tmep_repeat_type_id == R.id.month){
			
			repeat_type = MyDatabaseUtil.REPEAT_TYPE_MONTH;
			
			repeat_rate.setVisibility(View.VISIBLE);
			repeat_rate_type.setText("月");
			repeat_time.setVisibility(View.VISIBLE);
			ln_repeat_time_month.setVisibility(View.VISIBLE);
			ln_repeat_time_week.setVisibility(View.GONE);
			
			ln_repeat_time_range.setVisibility(View.VISIBLE);
			
			//刷新
			Calendar car = Calendar.getInstance();
			Date date = new Date(start_time);
			car.setTime(date);
			int day = car.get(Calendar.DATE);
			b_day_in_month.setText("每月 的 第"+day+"天");
			
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
	        b_day_in_week_in_month.setText("每月 的 第"+week_of_month+"个星期的周"+dd);
			
	        btn_repeat_type.setText("月");
		}else if(tmep_repeat_type_id == R.id.year){
			
			repeat_type = MyDatabaseUtil.REPEAT_TYPE_YEAR;
			
			repeat_rate.setVisibility(View.VISIBLE);
			repeat_rate_type.setText("年");
			repeat_time.setVisibility(View.GONE);
			
			ln_repeat_time_range.setVisibility(View.VISIBLE);
			btn_repeat_type.setText("年");
		}else{
			
			repeat_type = MyDatabaseUtil.REPEAT_TYPE_ONE;
			
			repeat_rate.setVisibility(View.GONE);
			repeat_time.setVisibility(View.GONE);
			ln_repeat_time_range.setVisibility(View.GONE);
			btn_repeat_type.setText("一次性");
		}
		
		AddEditRmindLayout.this.remindCalendar.ln_tip.removeAllViews();
		AddEditRmindLayout.this.remindCalendar.ln_tip.setVisibility(View.GONE);
    }
    
	void setRemindType() {

		switch (tmep_remind_type_id) {
		case R.id.remind_type_min:
			remind_type = MyDatabaseUtil.REMIND_TYPE_MIN;
			btn_remind_type.setText("分钟");
			break;
		case R.id.remind_type_hour:
			remind_type = MyDatabaseUtil.REMIND_TYPE_HOUR;
			btn_remind_type.setText("小时");
			break;
		case R.id.remind_type_day:
			remind_type = MyDatabaseUtil.REMIND_TYPE_DAY;
			btn_remind_type.setText("天");
			break;
		case R.id.remind_type_week:
			remind_type = MyDatabaseUtil.REMIND_TYPE_WEEK;
			btn_remind_type.setText("周");
			break;
		default:
			break;
		}
		
		AddEditRmindLayout.this.remindCalendar.ln_tip.removeAllViews();
		AddEditRmindLayout.this.remindCalendar.ln_tip.setVisibility(View.GONE);
	}
	
	
	public void setReShow(boolean isReShow) {
		this.isReShow = isReShow;
	}
}
