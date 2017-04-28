package com.dongji.enity;

import java.util.ArrayList;
import java.util.List;

/**
 * 提醒列表 每周的bean 某一天
 * @author Administrator
 *
 */
public class RemindWeekBean {

	String week_day;
	int day;
	long time;
	
	List<RemindBean> rbs = new ArrayList<RemindBean>();

	long week_day_start_time;
	long week_day_end_time;
	
	
	
	public RemindWeekBean(String week_day, long week_day_start_time,
			long week_day_end_time) {
		this.week_day = week_day;
		this.week_day_start_time = week_day_start_time;
		this.week_day_end_time = week_day_end_time;
	}
	
	public List<RemindBean> getRbs() {
		return rbs;
	}

	public void setRbs(List<RemindBean> rbs){
		this.rbs = rbs;
	}

	public String getWeek_day() {
		return week_day;
	}

	public void setWeek_day(String week_day) {
		this.week_day = week_day;
	}

	public long getWeek_day_start_time() {
		return week_day_start_time;
	}

	public void setWeek_day_start_time(long week_day_start_time) {
		this.week_day_start_time = week_day_start_time;
	}

	public long getWeek_day_end_time() {
		return week_day_end_time;
	}

	public void setWeek_day_end_time(long week_day_end_time) {
		this.week_day_end_time = week_day_end_time;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long start_time) {
		this.time = start_time;
	}
	
	
}
