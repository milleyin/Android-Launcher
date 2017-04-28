package com.dongji.launcher;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dongji.launcher.AddEditRmindLayout.OnFinishEditRemindListener;
import com.dongji.ui.ScrollLayout;

/**
 * 提醒日历
 * @author Administrator
 *
 */
public class RemindCalendar {

	Context context;
	public View v;
	
	public ScrollLayout  sroller;
	
	Button btn_add_remind;
	Button btn_change;
	
	FrameLayout ln1;
	FrameLayout ln2;
	public FrameLayout ln3;
	
	RemindLayout remindLayout;
	
	public LinearLayout ln_tip;
	
	public RelativeLayout ln_bottom;
	
	TextView tv_title;
	
	public RemindCalendar(Context context) {
		
		this.context = context;
		v = LayoutInflater.from(context).inflate(R.layout.widget_remind_calendar, null);
		
		ln_tip = (LinearLayout)v.findViewById(R.id.ln_tip);
		ln_tip.setClickable(true);
		ln_tip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ln_tip.removeAllViews();
				ln_tip.setVisibility(View.GONE);
			}
		});
		
		sroller = (ScrollLayout)v.findViewById(R.id.sroller);
	        
	    ln1 = (FrameLayout)v.findViewById(R.id.ln1);
	    ln2= (FrameLayout)v.findViewById(R.id.ln2);
	    ln3 = (FrameLayout)v.findViewById(R.id.ln3);
	        
	    tv_title = (TextView)v.findViewById(R.id.tv_title);
		tv_title.setClickable(true);
		tv_title.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(sroller.getCurScreen()==1)
				{
					remindLayout.popPickWeek();
				}
			}
		});
		
	    remindLayout = new RemindLayout(context,this,tv_title);
	    ln2.addView(remindLayout.view);
	        
	    btn_add_remind = (Button)v.findViewById(R.id.btn_add_remind);
	    btn_add_remind.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					tv_title.setText("新建提醒");
					ln_bottom.setVisibility(View.GONE);
					showOrHideTop(false);
					
				    AddEditRmindLayout addEditRmindLayout = new AddEditRmindLayout(RemindCalendar.this.context,RemindCalendar.this, -1, -1, new OnFinishEditRemindListener() {
						
						@Override
						public void OnFinishEditRemind() {
							remindLayout.updateAfterDelete();
							sroller.snapToScreen(1);
							ln_bottom.setVisibility(View.VISIBLE);
							showOrHideTop(true);
						}
					});
				    ln1.addView(addEditRmindLayout.v);
					sroller.snapToScreen(0);
				}
			});
	    
	     btn_change = (Button)v.findViewById(R.id.btn_change);
	     btn_change.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int i = remindLayout.change();
					
					if(i == 0)
					{
						v.setBackgroundResource(R.drawable.btn_change_calendar_table);
						ln_bottom.setVisibility(View.VISIBLE);
					}else{
						v.setBackgroundResource(R.drawable.btn_change_calendar_list);
						ln_bottom.setVisibility(View.GONE);
					}
				}
			});
	        
	     
	     Button btn_pre = (Button)v.findViewById(R.id.btn_pre_week);
			btn_pre.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					remindLayout.preWeek();
				}
			});
			Button btn_next = (Button)v.findViewById(R.id.btn_next_week);
			btn_next.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					remindLayout.nextWeek();
				}
			});
	     
			ln_bottom = (RelativeLayout) v.findViewById(R.id.ln_bottom);
			
	     sroller.setToScreen(1);
	}
	
	public void showOrHideTop(boolean b )
	{
		if(b)
		{
			btn_add_remind.setVisibility(View.VISIBLE);
			btn_change.setVisibility(View.VISIBLE);
			remindLayout.reSetTitile();
		}else{
			btn_add_remind.setVisibility(View.GONE);
			btn_change.setVisibility(View.GONE);
		}
	}
	
	public void dismiss()
	{
		System.out.println(" in in ---");
		try {
			if(ln_tip!=null)
			{
				ln_tip.setVisibility(View.GONE);
				ln_tip.removeAllViews();
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
}
