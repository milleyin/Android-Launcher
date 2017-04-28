package com.dongji.enity;


public class RemindBean {

	int id ; 
	
	String content;  //事件 
	
	String contacts; //发起人
	String participants; //参与人
	
	
	long start_time; //开始时间
	long end_time;  //结束时间
	
	
	int remind_type; //提醒类型
	int remind_num;  //提醒数值       数值  + 类型  = 提前多久提醒
	int remind_time; //提醒次数
	
	
	int repeat_type; //重复类型 :  一次性，天 ， 周， 月 ， 年
	int repeat_fre ; //重复频率
	
	
	String repeat_condition; //重复时间:  按周:周一至周日    ,   按月：一月中的某天(每月 在第30天)   一周的某天(每月 最后一个星期二)   ,    按年:一年

	long repeat_start_time ;
	long repeat_end_time;
	
	String count ;
	
	long temp_start_time; //存储提醒活动的开始时间 (不一定为实际的开始时间和结束时间，可能是重复的时候，具体的某次时间)
	long temp_end_time ;  //存储提醒活动的结束时间 
	
	String time_filter;   //时间过滤： text类型   long,long,long ;  在此时间内的提醒将不被触发
	
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContacts() {
		return contacts;
	}
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	public String getParticipants() {
		return participants;
	}
	public void setParticipants(String participants) {
		this.participants = participants;
	}
	public long getStart_time() {
		return start_time;
	}
	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}
	public long getEnd_time() {
		return end_time;
	}
	public void setEnd_time(long end_time) {
		this.end_time = end_time;
	}
	public int getRemind_type() {
		return remind_type;
	}
	public void setRemind_type(int remind_type) {
		this.remind_type = remind_type;
	}
	public int getRemind_num() {
		return remind_num;
	}
	public void setRemind_num(int remind_num) {
		this.remind_num = remind_num;
	}
	public int getRemind_time() {
		return remind_time;
	}
	public void setRemind_time(int remind_time) {
		this.remind_time = remind_time;
	}
	public int getRepeat_type() {
		return repeat_type;
	}
	public void setRepeat_type(int repeat_type) {
		this.repeat_type = repeat_type;
	}
	public int getRepeat_fre() {
		return repeat_fre;
	}
	public void setRepeat_fre(int repeat_fre) {
		this.repeat_fre = repeat_fre;
	}
	public String getRepeat_condition() {
		return repeat_condition;
	}
	public void setRepeat_condition(String repeat_condition) {
		this.repeat_condition = repeat_condition;
	}
	public long getRepeat_start_time() {
		return repeat_start_time;
	}
	public void setRepeat_start_time(long repeat_start_time) {
		this.repeat_start_time = repeat_start_time;
	}
	public long getRepeat_end_time() {
		return repeat_end_time;
	}
	public void setRepeat_end_time(long repeat_end_time) {
		this.repeat_end_time = repeat_end_time;
	}
	public long getTemp_start_time() {
		return temp_start_time;
	}
	public void setTemp_start_time(long temp_start_time) {
		this.temp_start_time = temp_start_time;
	}
	public long getTemp_end_time() {
		return temp_end_time;
	}
	public void setTemp_end_time(long temp_end_time) {
		this.temp_end_time = temp_end_time;
	}
	
	public String getTime_filter() {
		return time_filter;
	}
	public void setTime_filter(String time_filter) {
		this.time_filter = time_filter;
	}
	
	public RemindBean copy()
	{
		RemindBean rb = new RemindBean();
		
		rb.setId(id);
		
		rb.setContent(content);
		
		rb.setContacts(contacts);
		rb.setParticipants(participants);
		
		rb.setStart_time(start_time);
		rb.setEnd_time(end_time);
		
		rb.setRemind_type(remind_type);
		rb.setRemind_num(remind_num);
		rb.setRemind_time(remind_time);
		
		rb.setRepeat_type(remind_type);
		rb.setRepeat_fre(repeat_fre);
		
		rb.setRepeat_condition(repeat_condition);
		
		rb.setRepeat_start_time(repeat_start_time);
		rb.setRepeat_end_time(repeat_end_time);
		return rb;
	}
}
