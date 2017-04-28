package com.dongji.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.dongji.sqlite.MyDatabaseUtil;

public class TimeCounter {

	public static long getNextTime(long start_time,int remind_type,int remind_num,int repeat_type,String repeat_condition,int repeat_freq,long repeat_start_time,long repeat_end_time,String time_filter)
	{
		long next_time=-1;
		
		System.out.println(" start_time ---> " + start_time);
		System.out.println(" start_time_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(start_time));
		
		System.out.println(" remind_type --->" + remind_type);
		System.out.println(" remind_num --->" + remind_num);
		
		System.out.println(" repeat_type --->" + repeat_type);
		System.out.println(" repeat_condition --->" + repeat_condition);
		System.out.println(" repeat_freq  --->" + repeat_freq);
		
		System.out.println(" repeat_start_time  --->" + repeat_start_time);
		System.out.println(" repeat_start_time_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(repeat_start_time));
		
		System.out.println(" repeat_end_time --->" + repeat_end_time);
		System.out.println(" repeat_end_time_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(repeat_end_time));
		
		
		//触发最近一次提醒
        long before_time = 0 ; //提前多久提醒 毫秒
		switch (remind_type) {
		case MyDatabaseUtil.REMIND_TYPE_MIN:
			before_time = remind_num * 60*1000;  //必须为长整型   Long.valueOf(et_remind_num.getText().toString())
			break;
			
		case MyDatabaseUtil.REMIND_TYPE_HOUR:
			before_time = remind_num* 60*60*1000;
			break;
			
		case MyDatabaseUtil.REMIND_TYPE_DAY:
			before_time = remind_num * 24 *60*60*1000;
//			System.out.println("remind_num -->" +remind_num);
			break;
			
		case MyDatabaseUtil.REMIND_TYPE_WEEK:
			before_time = remind_num * 7*24*60*60*1000;
			break;

		default:
			break;
		}
		long temp_time = start_time - before_time;
		
		System.out.println(" before_time --->" + before_time);
		System.out.println(" temp_time  --->" + temp_time);
		System.out.println(" temp_time_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(temp_time));
		
		
		long t = start_time;
		
		Calendar c = Calendar.getInstance();
		Date data = new Date(start_time);
		c.setTime(data);
		
		int day = c.get(Calendar.DATE);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int weekOfMonth = c.get(Calendar.WEEK_OF_MONTH);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		int min = c.get(Calendar.MINUTE);
	    
		System.out.println(year +"-"+month+"-"+day+" "+hour+":"+min);
		System.out.println("weekOfMonth : "+weekOfMonth+"  dayOfWeek:" +dayOfWeek);
		
		
		switch (repeat_type) {
		case MyDatabaseUtil.REPEAT_TYPE_ONE: //一次性
			System.out.println("--------------------一次性------------------------");
			if(temp_time>System.currentTimeMillis())
			{
				System.out.println(" temp_time>System.currentTimeMillis() -----");
				next_time = temp_time;
			}
			break;

        case MyDatabaseUtil.REPEAT_TYPE_DAY: //天重复
        	System.out.println("--------------------天重复------------------------");
			if(temp_time<System.currentTimeMillis())
			{
				if(repeat_end_time>System.currentTimeMillis())
				{
//					System.out.println(" ----- temp_time<System.currentTimeMillis()  &&  repeat_end_time>System.currentTimeMillis() ");
					
					while(t<repeat_end_time)
					{
						t = t +  (repeat_freq * 24 *60*60*1000);
						
						
						System.out.println(" t  --->" + t);
						System.out.println(" t_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(t));
						
						if(t > System.currentTimeMillis() && t<repeat_end_time && t>repeat_start_time && !time_filter.contains(String.valueOf(t)))
						{
							next_time = t - before_time;
							System.out.println(" ----- t > System.currentTimeMillis() && t<repeat_end_time && t>repeat_start_time ");
							break;
						}
					}
				}
			}else{
				next_time = temp_time;
			}
			break;
			
		case MyDatabaseUtil.REPEAT_TYPE_WEEK: //周重复
			
			if(temp_time<System.currentTimeMillis())
			{
				if(repeat_end_time>System.currentTimeMillis())
				{
					
					//当前是周几
					Calendar car = Calendar.getInstance();
					Date dd = new Date(System.currentTimeMillis());
					car.setTime(dd);
					int cur_week_day = car.get(Calendar.DAY_OF_WEEK); //当前是周几
					if(cur_week_day==1)
					{
						cur_week_day = 7;
					}else{
						cur_week_day --;
					}
					
					int index = -1;
					
					String[] w_ss = repeat_condition.split(",");

					if (w_ss.length == 0) {
						w_ss = new String[] { repeat_condition };
					}
					
					for (int i = 0; i < w_ss.length; i++) {
						if (Integer.valueOf(w_ss[i])>cur_week_day) {
							index = i;
							break;
						}
					}
					
					int target_week_day;
					
					if(index==-1) //没找到取第一个
					{
						target_week_day = Integer.valueOf(w_ss[0]);
					}else{
						target_week_day = Integer.valueOf(w_ss[index]);
					}
					
					
					//事件开始时间是周几
					Calendar cc = Calendar.getInstance();
					Date ddd = new Date(start_time);
					cc.setTime(ddd);
					int start_week_day = cc.get(Calendar.DAY_OF_WEEK); 
					if(start_week_day==1)
					{
						start_week_day = 7;
					}else{
						start_week_day --;
					}
					
					int day_carp = target_week_day - start_week_day;
					
					t = start_time+ (day_carp* 24*60*60*1000);
					
					System.out.println("cur_week_day ---> " + cur_week_day + "target_week_day ---> " + target_week_day + " day_carp  --->" +day_carp);
					
					while(t<repeat_end_time)
					{
						System.out.println(" t  --->" + t);
						System.out.println(" t_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(t));
						
						if(t > System.currentTimeMillis() && t<repeat_end_time && t>repeat_start_time && !time_filter.contains(String.valueOf(t)))
						{
							next_time = t - before_time;
							break;
						}
						t = t + (repeat_freq*7* 24*60*60*1000);
					}
					
				}
			}else{
				next_time = temp_time;
			}
			break;
			
		case MyDatabaseUtil.REPEAT_TYPE_MONTH: //月重复
			
			System.out.println("------------月重复----------------");
			if(temp_time<System.currentTimeMillis())
			{
				if(repeat_end_time>System.currentTimeMillis())
				{
					
					if(repeat_condition.equals("1")) //每月的第几天
					{
						while(t<repeat_end_time)
						{
							month = month + repeat_freq;
							
							if(month >11)
							{
								 year = year+(month/11);
					    		 month=(month%11)-1;
							}
							
							System.out.println("  yy ---> " + year + " month---> " + month);
							
							Date date = new Date(Integer.valueOf(year)-1900, Integer.valueOf(month), Integer.valueOf(day), Integer.valueOf(hour), Integer.valueOf(min));
							t =  date.getTime();
							
							System.out.println(" t  --->" + t);
							System.out.println(" t_date  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(t));
							
							if(t >System.currentTimeMillis() && t<repeat_end_time && t>repeat_start_time && !time_filter.contains(String.valueOf(t)))
							{
								next_time = t - before_time;
								System.out.println("find next time !");
								break;
							}
						}
				        
					}else{    //每月的第几个星期的周几
						
						while(t < repeat_end_time)
						{
							month= month + repeat_freq;
							
							System.out.println("  yy ---> " + year + " month---> " + month);
							t =  weekdatetodata(year, month, weekOfMonth, dayOfWeek, hour, min);
							
							
							if(t>System.currentTimeMillis() && t<repeat_end_time &&  t>repeat_start_time && !time_filter.contains(String.valueOf(t)))
							{
								next_time = t - before_time;
								System.out.println("find next time !");
								break;
							}
						}
					}
				}
			}else{
				next_time = temp_time;
			}
			break;
			
		case MyDatabaseUtil.REPEAT_TYPE_YEAR:  //年重复
			
			System.out.println("--------------------年重复------------------------");
			if(temp_time<System.currentTimeMillis())
			{
				if(repeat_end_time>System.currentTimeMillis())
				{
					
					while(t<repeat_end_time)
					{
						//下一年的时间
						year = year + repeat_freq;
						Date date = new Date(Integer.valueOf(year)-1900, Integer.valueOf(month), Integer.valueOf(day), Integer.valueOf(hour), Integer.valueOf(min));
						t =  date.getTime();
						
						
						if(t>System.currentTimeMillis() && t<repeat_end_time && t>repeat_start_time && !time_filter.contains(String.valueOf(t)))
						{
							next_time = t - before_time;
							
							break;
						}
					}
				}
			}else{
				next_time = temp_time;
			}
			break;
			
		default:
			break;
		}
		
		
		if(next_time==-1)
		{
			System.out.println(" 没有下次提醒");
			
		}else{
			 System.out.println(" 下次提醒的时间为  --->" + new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format(next_time));
			 System.out.println("--------------------- end -------------------");
		}
		
		return next_time;
	}
	
	
	/**
     * 
     * @param year          年份
     * @param month         月份
     * @param weekOfMonth   这个月的第几周
     * @param dayOfWeek     星期几
     * @return
     */
    public static long weekdatetodata(int year,int month,int weekOfMonth,int dayOfWeek,int hour,int min){
    	
    	  Calendar c = Calendar.getInstance();
    	  
    	  //计算出 x年 y月 1号 是星期几
    	  if(month>11)  //如果大于12月份 ,则跳到下一年
    	  {
    		  year = year+(month/11);
    		  month=(month%11)-1;
    		  
    		  c.set(year ,month, 1);
    		  
    	  }else{
    		  c.set(year, month, 1);
    	  }
    	  
    	  
    	  //如果i_week_day =1  的话 实际上是周日  
    	  int i_week_day = c.get(Calendar.DAY_OF_WEEK);
//    	  System.out.println("  i_week_day  --->" + i_week_day);
    	  
    	  int sumDay = 0;
    	  //dayOfWeek+1 就是星期几（星期日 为 1）
    	  if(i_week_day == 1){
    	   sumDay = (weekOfMonth-1)*7 + dayOfWeek;
    	  }else{
    	   sumDay = 7-i_week_day+1 +  (weekOfMonth-2)*7 + dayOfWeek ;
    	  }
    	  
    	  //在1号的基础上加上相应的天数
    	  c.set(Calendar.DATE, sumDay);
    	  c.set(Calendar.HOUR, hour);
    	  c.set(Calendar.MINUTE,min);
    	  
    	  int y = c.get(Calendar.YEAR);
    	  int m = c.get(Calendar.MONTH);
    	  int d = c.get(Calendar.DAY_OF_MONTH);
    	  
    	  Date date = new Date(y-1900, m, d, hour, min);
    	  
    	  SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    	  System.out.println(sf2.format(date));
    	  
    	  return date.getTime();  
    }
    
    
    //智能返回时间  :  如果是今年内的则不显示年分 格式 yyMMdd hh:mm
    public static  String getTimeStrYYMMDDHHMM(long time)
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
		
		Date d = new Date(System.currentTimeMillis());
		Calendar car1 = Calendar.getInstance();
		car1.setTime(d);
		int cur_year = car1.get(Calendar.YEAR);
		
		if(year==cur_year)
		{
			return month+"-"+day+" "+hourStr+":"+minuteStr;
		}else{
			return year+"-"+month+"-"+day+" "+hourStr+":"+minuteStr;
		}
		
	}
	
    //智能返回时间  :  如果是今年内的则不显示年分 格式 yyMMdd
	public static String getTimeStrYYMMDD(long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		int year = car.get(Calendar.YEAR);
		int month = car.get(Calendar.MONTH)+1;
		int day = car.get(Calendar.DATE);
		
		Date d = new Date(System.currentTimeMillis());
		Calendar car1 = Calendar.getInstance();
		car1.setTime(d);
		int cur_year = car1.get(Calendar.YEAR);
		
		if(year==cur_year)
		{
			return +month+"-"+day;
		}else{
			return year+"-"+month+"-"+day;
		}
	}
	
	public static String getTimeStrMMDD(long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		int month = car.get(Calendar.MONTH)+1;
		int day = car.get(Calendar.DATE);
		
		if(day<=9)
		{
			return +month+"月"+day+"日  ";
		}
		
		return +month+"月"+day+"日";
	}
	
	public static String getTimeStrMMDD2(long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		int month = car.get(Calendar.MONTH)+1;
		int day = car.get(Calendar.DATE);
		
		if(month<=9 && day<=9)
		{
			return +month+"/"+day+"    ";
		}else 
			
		if(month<=9)
		{
			return +month+"/"+day+"  ";
		}else if(day<=9){
			return +month+"/"+day+"  ";
		}
		
		return +month+"/"+day+"";
	}
	
	public static int getYear (long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		return  car.get(Calendar.YEAR);
	}
	
	public static int getMonth (long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		return  car.get(Calendar.MONTH)+1;
	}
	
	public static int getDay (long time)
	{
		Calendar car = Calendar.getInstance();
		Date date = new Date(time);
		car.setTime(date);
		return  car.get(Calendar.DATE);
	}
	
}
