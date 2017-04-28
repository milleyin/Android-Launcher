package com.dongji.launcher;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongji.sqlite.DButil;
import com.dongji.sqlite.MyDatabaseUtil;
import com.dongji.tool.TimeCounter;

/**
 * 
 * 提醒弹窗
 * @author Administrator
 *
 */
public class RemindPopActivity extends Activity implements OnClickListener {
	
	long remind_id;
	
	LinearLayout bas_layout;
	
//	ImageView img_head;
	TextView tv_top_title; 
	Button btn_close;
	
	/*TextView tv_name;
	TextView tv_partener;*/
	TextView tv_content;
	
//	Button btn_call;
//	Button btn_sms;
	Button btn_cancle_remind;
	
	Button btn_pick_ok;
	Button btn_pick_cancle;
	
	long start_time = -1;  //提醒的开始时间
	long end_time = -1;    //提醒的结束时间
	
	int remind_type;
	int remind_num;  //提醒数值
	
	int remind_time; //体系次数
	int has_remind_time;
	
	String contact_str = "" ; //联系人:   #id#:name:p,p,p
//	int c_id = -1;
	String partner_str = "";
	int [] p_ids;
	String partner_names = "";
	
	int repeat_type;
	long repeat_start_time = -1;
	long repeat_end_time = -1;
	
	String repeat_condition;
	
	int repeat_freq;
	
	int aciton_type;  //0:打电话  1:发短信
	MyDatabaseUtil myDatabaseUtil;
	private int REMIND_GAP_TIME = 5*60*1000 ; //每次提醒的间隔时间，毫秒 ， 5分钟 
	
	String time_filter ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remind);
		myDatabaseUtil=DButil.getInstance(this);
//		img_head = (ImageView)findViewById(R.id.img_head);
		
		tv_top_title = (TextView)findViewById(R.id.tv_top_title);
		
		btn_close = (Button)findViewById(R.id.btn_close); 
		btn_close.setOnClickListener(this);
		
		tv_content = (TextView)findViewById(R.id.tv_content);
		
		btn_cancle_remind = (Button)findViewById(R.id.btn_cancle_remind);
		btn_cancle_remind.setOnClickListener(this);
		
		remind_id = getIntent().getLongExtra(MyDatabaseUtil.REMIND_ID, -1);
		System.out.println(" RemindPopActivity  remind_id --->" + remind_id);
		query();
	}
	
	void query()
	{
		
		Cursor c = myDatabaseUtil.queryRemind(remind_id);
		c.moveToNext();
		String content = c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTENT));
		tv_content.setText(content);
//		contact_str = c.getString(c.getColumnIndex(MyDatabaseUtil.REMIND_CONTACT));
//		String [] contact_ss = contact_str.split(":");
//		c_id = Integer.valueOf(contact_ss[0].replace("#", ""));
//		String [] pp = contact_ss[2].split(",");
		
		
		start_time = c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_START));
		end_time = c.getLong(c.getColumnIndex(MyDatabaseUtil.REMIND_END));
		
		remind_num = c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_NUM));
		
		remind_time = c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TIME));
		
		has_remind_time = c.getInt(c.getColumnIndex(MyDatabaseUtil.HAS_REMIND_TIME));
		has_remind_time++; //默认为0，提醒一次 + 1
		
		remind_type = c.getInt(c.getColumnIndex(MyDatabaseUtil.REMIND_TYPE));
		
		//重复
		repeat_type = c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_TYPE));
		
		repeat_condition = c.getString(c.getColumnIndex(MyDatabaseUtil.REPEAT_CONDITION));
		
	    repeat_freq = c.getInt(c.getColumnIndex(MyDatabaseUtil.REPEAT_FREQ));
		
		repeat_start_time = c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_START_TIME));
		repeat_end_time =  c.getLong(c.getColumnIndex(MyDatabaseUtil.REPEAT_END_TIME));
		
		time_filter  = c.getString(c.getColumnIndex(MyDatabaseUtil.TIME_FILTER));
		c.close();
		
		
		System.out.println("总提醒次数: " +remind_time);
		System.out.println("已经提醒的次数 :  " + has_remind_time);
		
		myDatabaseUtil.updateHasRemindNum(remind_id, has_remind_time);
		
	}
	
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btn_close:
			go();
			break;
			
		case R.id.btn_cancle_remind:
			tiggletNext();
			break;

		default:
			break;
		}
	}
	
	void go()
	{
		if(has_remind_time<remind_time) //还没完成总提醒次数,五分钟后提醒
		{
			Intent it = new Intent(RemindPopActivity.this, AlarmReceiver.class);
    		it.putExtra(MyDatabaseUtil.REMIND_ID, remind_id);		
    		PendingIntent pit = PendingIntent.getBroadcast(RemindPopActivity.this, (int)remind_id, it, 0);
    		AlarmManager amr = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
    		amr.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis()+ REMIND_GAP_TIME),pit);
    		finish();
		}else{
			tiggletNext();
			
		}
	}
	
	//触发下一次提醒
	void tiggletNext(){
		myDatabaseUtil.updateHasRemindNum(remind_id, 0);
		
		long next_time = TimeCounter.getNextTime(start_time, remind_type, remind_num, repeat_type, repeat_condition,repeat_freq,repeat_start_time, repeat_end_time,time_filter);
	       
		//保留
        if(next_time!=-1)
        {
        	Intent it = new Intent(RemindPopActivity.this, AlarmReceiver.class);
    		it.putExtra(MyDatabaseUtil.REMIND_ID, remind_id);		
    		PendingIntent pit = PendingIntent.getBroadcast(RemindPopActivity.this, (int)remind_id, it, 0);
    		AlarmManager amr = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
    		amr.cancel(pit);//先取消 ？
    		amr.set(AlarmManager.RTC_WAKEUP, next_time ,pit);
        }else{
        	 //没有下一次的提醒了
        }
        
        finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		go();
	}
}
