package com.dongji.ui;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.view.View;

import com.dongji.adapter.NumericWheelAdapter;
import com.dongji.launcher.R;


public class WheelMain {

	private View view;
	private WheelView wv_year;
	private WheelView wv_month;
	private WheelView wv_day;
	private WheelView wv_hours;
	private WheelView wv_mins;
	private static int START_YEAR = 1990, END_YEAR = 2100;

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public static int getSTART_YEAR() {
		return START_YEAR;
	}

	public static void setSTART_YEAR(int sTART_YEAR) {
		START_YEAR = sTART_YEAR;
	}

	public static int getEND_YEAR() {
		return END_YEAR;
	}

	public static void setEND_YEAR(int eND_YEAR) {
		END_YEAR = eND_YEAR;
	}

	public WheelMain(View view) {
		super();

		this.view = view;
		setView(view);
	}

	/**
	 * @Description: TODO 弹出日期时间选择器
	 */
	public void initDateTimePicker() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		// 添加大小月月份并将其转换为list,方便之后的判断
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		START_YEAR = year;
		
		// 年
		wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"年"的显示数据
//		wv_year.setCyclic(true);// 可循环滚动
//		wv_year.setLabel("年");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数据
		

//		wv_month.setLabel("小时");
		
		
		wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setCyclic(true);// 可循环滚动
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCurrentItem(hour);
//		wv_hours.setLabel("小时");
		
		wv_mins = (WheelView) view.findViewById(R.id.minutes);
		wv_mins.setCyclic(true);// 可循环滚动
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59));
		wv_mins.setCurrentItem(minute);
//		wv_mins.setLabel("分钟");
		
		
		// 月
		wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
//		wv_month.setLabel("月");
		wv_month.setCurrentItem(month);

		// 日
		wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"日"的数据
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
//		wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);
        
		// 添加"年"监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"日"的数据
				if (list_big
						.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month
						.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0)
							|| year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// 添加"月"监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"日"的数据
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0 && (wv_year
							.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);

		// 根据屏幕密度来指定选择器字体的大小
		int textSize = 0;

		textSize = 30;

		wv_day.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;

	}
	
	public void setToTime(int year,int month, int day,int hour,int minute)
	{
		wv_year.setCurrentItem(year - START_YEAR);
		wv_month.setCurrentItem(month);
		wv_day.setCurrentItem(day - 1);
		
		wv_hours.setCurrentItem(hour);
		wv_mins.setCurrentItem(minute);
	}
	
	public String getTimemil() {
      StringBuffer sb = new StringBuffer();
		
		String month;
		String day;
		String hour;
		String mini;
		String year=String.valueOf(wv_year.getCurrentItem() + START_YEAR);
		//月
		if(wv_month.getCurrentItem()<9)
		{
			month="0"+wv_month.getCurrentItem();
		}
		else
		{
			month=String.valueOf(wv_month.getCurrentItem()+1);
			
		}
		//日
		if(wv_day.getCurrentItem()<9)
		{
			day="0"+wv_day.getCurrentItem()+1;
		}
		else
		{
			
			day=String.valueOf(wv_day.getCurrentItem()+1);
		}
		
		//小时
		if(wv_hours.getCurrentItem()<9)
		{
			hour="0"+wv_hours.getCurrentItem();
		}
		else
		{
			
			hour=String.valueOf(wv_hours.getCurrentItem());
		}
		//分钟
		if(wv_mins.getCurrentItem()<9)
		{
			mini="0"+wv_mins.getCurrentItem();
		}
		else
		{
			
			mini=String.valueOf(wv_mins.getCurrentItem());
		}
		sb.append(year).append("-").append(month).append("-").append(day).append("-").append(hour).append("-").append(mini);
	
		return sb.toString();
	}
	
	public String getTime() {
		StringBuffer sb = new StringBuffer();
		
		String month;
		String day;
		String hour;
		String mini;
		String year=String.valueOf(wv_year.getCurrentItem() + START_YEAR);
		//月
		if(wv_month.getCurrentItem()<9)
		{
			month="0"+wv_month.getCurrentItem()+1;
		}
		else
		{
			month=String.valueOf(wv_month.getCurrentItem()+1);
		}
		//日
		if(wv_day.getCurrentItem()<9)
		{
			day="0"+wv_day.getCurrentItem()+1;
		}
		else
		{
			
			day=String.valueOf(wv_day.getCurrentItem()+1);
		}
		
		//小时
		if(wv_hours.getCurrentItem()<9)
		{
			hour="0"+wv_hours.getCurrentItem();
		}
		else
		{
			
			hour=String.valueOf(wv_hours.getCurrentItem());
		}
		//分钟
		if(wv_mins.getCurrentItem()<9)
		{
			mini="0"+wv_mins.getCurrentItem();
		}
		else
		{
			
			mini=String.valueOf(wv_mins.getCurrentItem());
		}
		sb.append(year).append(month).append(day).append(hour).append(mini);
	
		return sb.toString();
	}
	
	public String getTimemilInFormat() {
	      StringBuffer sb = new StringBuffer();
			
			String month;
			String day;
			String hour;
			String mini;
			String year=String.valueOf(wv_year.getCurrentItem() + START_YEAR);
			
			month=String.valueOf(wv_month.getCurrentItem()+1);
				
			day=String.valueOf(wv_day.getCurrentItem()+1);
			hour=String.valueOf(wv_hours.getCurrentItem());
			mini=String.valueOf(wv_mins.getCurrentItem());
			
			sb.append(year).append("-").append(month).append("-").append(day).append("-").append(hour).append("-").append(mini);
		
			return sb.toString();
		}
}
