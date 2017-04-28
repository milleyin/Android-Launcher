package com.dongji.adapter;

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
import android.widget.TextView;

import com.dongji.launcher.R;

public class RemindListAdapter extends BaseAdapter{

	Context context;
	OnClickListener onClickListener;
	
	public View menu;
	int margin_bottom;
	int original_x;
	
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
				
			default:
				break;
			}

		};
	};
	
	
	public RemindListAdapter(Context context,OnClickListener onClickListener) {
		
		this.context = context;
		this.onClickListener = onClickListener;
		
	}

	@Override
	public int getCount() {
		return 10;
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
		
		convertView = LayoutInflater.from(context).inflate(R.layout.remind_list_item, null);
		
		ViewHolder viewHolder = new ViewHolder();
		
		viewHolder.tv_event_time = (TextView) convertView.findViewById(R.id.tv_event_time);
		viewHolder.tv_remind_num = (TextView) convertView.findViewById(R.id.tv_remind_num);
		viewHolder.tv_repeat_type = (TextView) convertView.findViewById(R.id.tv_repeat_type);
		
		viewHolder.tv_repeat_freq = (TextView) convertView.findViewById(R.id.tv_repeat_freq);
		viewHolder.t_repeat_codition = (TextView) convertView.findViewById(R.id.t_repeat_codition); 
		viewHolder.t_repeat_time = (TextView) convertView.findViewById(R.id.tv_repeat_time);  
		
		viewHolder.tv_remind_time = (TextView) convertView.findViewById(R.id.tv_remind_time);
		
		viewHolder.tv_partners = (TextView) convertView.findViewById(R.id.tv_partners);
		
		viewHolder.content = (TextView) convertView.findViewById(R.id.content);
		
		viewHolder.menu_edit_remind = (Button)convertView.findViewById(R.id.menu_edit_remind);
		viewHolder.menu_edit_remind.setOnClickListener(onClickListener);
		
		viewHolder.menu_delete_remind = (Button)convertView.findViewById(R.id.menu_delete_remind);
		viewHolder.menu_delete_remind.setOnClickListener(onClickListener);
		
		LinearLayout main_layout = (LinearLayout) convertView.findViewById(R.id.main_layout);
		main_layout.setClickable(true);
		main_layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
							popUpMenu(v);
						break;

					default:
						break;
						
					}
					return false;
				}
			});
		
		return convertView;
	}
	
	class ViewHolder {
		
		TextView tv_event_time; //时间的开始 到 结束
		TextView tv_remind_num; //提醒数值
		
		TextView tv_repeat_type; //重复类型
		TextView tv_repeat_freq; //重复频率
		
		TextView t_repeat_codition;
		TextView t_repeat_time;  //重复的 开始 和 结束
		
		TextView tv_partners;  //参与人
		TextView content;  //事件内容
		 
		TextView tv_remind_time; //提醒次数
		
		
		Button menu_edit_remind;
		Button menu_delete_remind; 
		
		CheckBox checkBox;
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

				if (margin_bottom < 0) {
					while (margin_bottom < 0) {
						margin_bottom += 5;
						try {
							Thread.sleep(10);
						} catch (Exception e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(0);
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

}
