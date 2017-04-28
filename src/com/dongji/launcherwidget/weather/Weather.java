package com.dongji.launcherwidget.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.adw.launcher.Launcher;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.dongji.launcher.R;

public class Weather extends AppWidgetProvider {
	private ProgressDialog progressDialog = null;
	private static final String INTENT_CLICK = "com.dongji.launcher.weather.refresh";
	private static final String RETURN_SETTING_DATE_AND_TIME = "com.dongji.launcher.time";
	private static final String SEARCHE_CITY = "com.dongji.launcher.cityname";
	private static RemoteViews rv;
	
	private SimpleDateFormat df = new SimpleDateFormat("HHmmss");

	// 数字图片的ID
	private int[] numberIcon = new int[] { R.drawable.number_0,
			R.drawable.number_1, R.drawable.number_2, R.drawable.number_3,
			R.drawable.number_4, R.drawable.number_5, R.drawable.number_6,
			R.drawable.number_7, R.drawable.number_8, R.drawable.number_9 };
	// 用于显示数字的ImageView的ID
	private int[] numberView = new int[] { R.id.hour01, R.id.hour02,
			R.id.minute01, R.id.minute02 };
	Context context;
	SharedPreferences sp = null;
	Editor editor = null;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.context=context;
		System.out.println("=====onupdate =======");
		
		context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK));
		
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
		
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		System.out.println(action + "   ====  action");
		if ( action.equals(INTENT_CLICK)){
			// TODO Auto-generated method stub
			AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
			int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(context, Weather.class));
			for (int i = 0; i < appIds.length; i++) {
				int appWidgetId = appIds[i];
				updateAppWidget(context, appWidgetManger, appWidgetId);
			}
			Toast.makeText(context, "已更新", Toast.LENGTH_SHORT).show();
		} else if (action.equals(Intent.ACTION_DATE_CHANGED)){
			
			AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
			int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(context, Weather.class));
			
			for (int i = 0; i < appIds.length; i++) {
				int appWidgetId = appIds[i];
				updateAppWidget(context, appWidgetManger, appWidgetId);
			}
			
		} else if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIME_TICK)){
			
			try {
				AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
				
				rv = new RemoteViews(context.getPackageName(), R.layout.weather);
				
				String timeString = df.format(new Date(System.currentTimeMillis()));
				int num = 0;
				for (int i = 0; i < numberView.length; i++) {
					num = timeString.charAt(i) - 48;
					rv.setImageViewResource(numberView[i], numberIcon[num]);
				}
				
				Intent it = new Intent(RETURN_SETTING_DATE_AND_TIME);
				PendingIntent pt = PendingIntent.getBroadcast(context, 0, it, 0);
				rv.setOnClickPendingIntent(R.id.timely, pt);
				
				
				ComponentName component = new ComponentName(context, Weather.class);
				appWidgetManger.updateAppWidget(component, rv);
			}catch (Exception e) {
				// TODO: handle exception
//				e.printStackTrace();
			}
			
		} else if (action.equals(RETURN_SETTING_DATE_AND_TIME)){
			
			Intent setting = new Intent(Settings.ACTION_DATE_SETTINGS);
			setting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(setting);
		} else if (action.equals(SEARCHE_CITY)){
			
			Intent city = new Intent(context, CitySearchDialogActivity.class);
			city.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(city);
			
		}
	}
	
	

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		
		context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK));
	}
	
   
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
	}
	
	
	private void updateAppWidget(Context context,AppWidgetManager appWidgetManager,int appWidgetIds){
		
		rv = new RemoteViews(context.getPackageName(), R.layout.weather);
		
		String timeString = df.format(new Date(System.currentTimeMillis()));
		int num;
		for (int i = 0; i < numberView.length; i++) {
			num = timeString.charAt(i) - 48;
			rv.setImageViewResource(numberView[i], numberIcon[num]);
		}
		
		Intent it = new Intent(RETURN_SETTING_DATE_AND_TIME);
		PendingIntent pt = PendingIntent.getBroadcast(context, 0, it, 0);
		rv.setOnClickPendingIntent(R.id.timely, pt);
		
		sp = context.getSharedPreferences("citycode_sp", 0);
		String citycode = sp.getString("citycode", null);
		System.out.println(" citycode " );
		if (citycode == null) {
			
			rv.setViewVisibility(R.id.searchly, View.VISIBLE);
			rv.setViewVisibility(R.id.weatherinfoly, View.GONE);
			
			Intent click = new Intent(SEARCHE_CITY);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, click, 0);
			rv.setOnClickPendingIntent(R.id.searchly, pendingIntent);
			
		} else {
			rv.setViewVisibility(R.id.searchly, View.GONE);
			rv.setViewVisibility(R.id.weatherinfoly, View.VISIBLE);
			
			WeatherInfo weatherInfo = getWeathInfo(citycode);
			
			if(weatherInfo == null){
				
				Toast.makeText(context, "无法获取最新城市信息,请检查网络连接是否正常！", Toast.LENGTH_SHORT).show();
				
				rv.setViewVisibility(R.id.searchly, View.VISIBLE);
				rv.setViewVisibility(R.id.weatherinfoly, View.GONE);
				
				Intent click = new Intent(SEARCHE_CITY);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, click, 0);
				rv.setOnClickPendingIntent(R.id.searchly, pendingIntent);
				
			}else{
				
				rv.setTextViewText(R.id.countryname, weatherInfo.getCity());
				
				Intent changeCity = new Intent(SEARCHE_CITY);
				PendingIntent changeCityPendingIntent = PendingIntent.getBroadcast(context, 0, changeCity, 0);
				rv.setOnClickPendingIntent(R.id.countryname, changeCityPendingIntent);
				
				rv.setImageViewResource(R.id.weather_img, getWeatherImage(weatherInfo.getImg_title()));
				rv.setTextViewText(R.id.temperature, weatherInfo.getTemp1());
				
				rv.setTextViewText(R.id.weather, weatherInfo.getWeather()+" , "+weatherInfo.getWind1());
				
				getDate(rv);
				Intent click = new Intent(INTENT_CLICK);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, click, 0);
				rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
				
			}
		}
		
		ComponentName component = new ComponentName(context, Weather.class);
		appWidgetManager.updateAppWidget(component, rv);
		
	}
	
	private static WeatherInfo getWeathInfo(String citycode){
		
		WeatherInfo weatherInfo = null ;
			
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = null;
		
		if (!citycode.equals("")){
			
			httpGet = new HttpGet("http://m.weather.com.cn/data/"+citycode+".html");
			
			int res = 0;
			try {
				res = client.execute(httpGet).getStatusLine().getStatusCode();

				if (res == 200) {
					/*
					 * 当返回码为200时，做处理 得到服务器端返回json数据，并做处理
					 */
					HttpResponse httpResponse = client.execute(httpGet);
					StringBuilder builder = new StringBuilder();
					BufferedReader bufferedReader2 = new BufferedReader(
							new InputStreamReader(httpResponse.getEntity()
									.getContent()));
					String str2 = "";
					for (String s = bufferedReader2.readLine(); s != null; s = bufferedReader2.readLine()) {
						builder.append(s);
					}

					JSONObject jsonObject;
					jsonObject = new JSONObject(builder.toString()).getJSONObject("weatherinfo");

					weatherInfo = new WeatherInfo();
					
					weatherInfo.setCity(jsonObject.getString("city"));
					weatherInfo.setDate_y(jsonObject.getString("date_y"));
					weatherInfo.setTemp1(jsonObject.getString("temp1"));
					weatherInfo.setWeather(jsonObject.getString("weather1"));
					weatherInfo.setImg_title(jsonObject.getString("img_title_single"));
					weatherInfo.setWind1(jsonObject.getString("wind1"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println("无法获取最新城市信息,请检查网络连接是否正常！");
		}
		
		return weatherInfo;

	}
	
	public static int getWeatherImage(String description){
		int icon = 0;
		
		if ( !description.equals("")){
			if(description.contains("暴雪")){
				icon = R.drawable.weather_icon_baoxue;
			}else if(description.contains("暴雨")){
				icon = R.drawable.weather_icon_baoyu;
			}else if(description.contains("冰雹")){
				icon = R.drawable.weather_icon_bingbao;
			}else if(description.contains("大雾")){
				icon = R.drawable.weather_icon_dawu;
			}else if(description.contains("大雪")){
				icon = R.drawable.weather_icon_daxue;
			}else if(description.contains("大雨")){
				icon = R.drawable.weather_icon_dayu;
			}else if(description.contains("冻雨")){
				icon = R.drawable.weather_icon_dongyu;
			}else if(description.contains("多云")){
				icon = R.drawable.weather_icon_duoyun;
			}else if(description.contains("多云转晴")){
				icon = R.drawable.weather_icon_duoyunzhuanqing;
			}else if(description.contains("可能有雾")){
				icon = R.drawable.weather_icon_kenengyouyu;
			}else if(description.contains("雷雨")){
				icon = R.drawable.weather_icon_leiyu;
			}else if(description.contains("雷阵雨")){
				icon = R.drawable.weather_icon_leizhenyu;
			}else if(description.contains("mai.png")){
				icon = R.drawable.weather_icon_mai;
			}else if(description.contains("晴")){
				icon = R.drawable.weather_icon_qing;
			}else if(description.contains("晴转多云")){
				icon = R.drawable.weather_icon_qingzhuanduoyun;
			}else if(description.contains("沙尘暴")){
				icon = R.drawable.weather_icon_shachenbao;
			}else if(description.contains("霜冻")){
				icon = R.drawable.weather_icon_shuangdong;
			}else if(description.contains("台风")){
				icon = R.drawable.weather_icon_taifeng;
			}else if(description.contains("雾")){
				icon = R.drawable.weather_icon_wu;
			}else if(description.contains("小雪")){
				icon = R.drawable.weather_icon_xiaoxue;
			}else if(description.contains("小雨")){
				icon = R.drawable.weather_icon_xiaoyu;
			}else if(description.contains("扬沙")){
				icon = R.drawable.weather_icon_yangsha;
			}else if(description.contains("阴天")){
				icon = R.drawable.weather_icon_yintian;
			}else if(description.contains("雨夹雪")){
				icon = R.drawable.weather_icon_yujiaxue;
			}else if(description.contains("阵雨")){
				icon = R.drawable.weather_icon_zhenyu;
			}else if(description.contains("中雪")){
				icon = R.drawable.weather_icon_zhongxue;
			}else if(description.contains("中雨")){
				icon = R.drawable.weather_icon_zhongyu;
			}
		}
		
		if(icon == 0){
			icon = R.drawable.weather_icon_qing;
		}
		
		return icon;
	}

	public String getServerJsonDataWithNoType(String url) {
		int res = 0;
		HttpClient client = new DefaultHttpClient();
		StringBuilder str = new StringBuilder();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpRes = client.execute(httpGet);
			httpRes = client.execute(httpGet);
			res = httpRes.getStatusLine().getStatusCode();
			if (res == 200) {
				BufferedReader buffer = new BufferedReader(new InputStreamReader(httpRes.getEntity().getContent(),"UTF-8"));
				for (String s = buffer.readLine(); s != null; s = buffer.readLine()) {
					str.append(s);
				}
								
			} else {
			}
		} catch (Exception e) {
		}
		return str.toString();
	}
	
	private void getDate(RemoteViews rv ){
		
		NongLi nongli = new NongLi();
		
		Calendar cld = Calendar.getInstance();
		int year = cld.get(Calendar.YEAR);
		int month = cld.get(Calendar.MONTH) + 1;
		int day = cld.get(Calendar.DAY_OF_MONTH);
		
		long[] l = nongli.calElement(year, month, day);

		String n = "";
		switch ((int) (l[1])) {
		case 1:
			n = "一";
			break;
		case 2:
			n = "二";
			break;
		case 3:
			n = "三";
			break;
		case 4:
			n = "四";
			break;
		case 5:
			n = "五";
			break;
		case 6:
			n = "六";
			break;
		case 7:
			n = "七";
			break;
		case 8:
			n = "八";
			break;
		case 9:
			n = "九";
			break;
		case 10:
			n = "十";
			break;
		case 11:
			n = "十一";
			break;
		case 12:
			n = "十二";
			break;
		}
		
		rv.setTextViewText(R.id.date2, "农历:" + n + "月" + nongli.getchina((int) (l[2])));
		rv.setTextViewText(R.id.date1, year + "年" + month + "月" + day + "日");
		
		String path = "http://www.91dongji.com/org/api.php";
		
		String str1 = getServerJsonDataWithNoType(path+"?op=calendar").replace("[", "").replace("]", "");
		String str2 = getServerJsonDataWithNoType(path+"?op=constellation");
		
		String[] dateInfo = str1.replace("\"", "").split(",");
		
		rv.setTextViewText(R.id.yi, dateInfo[0]);
		rv.setTextViewText(R.id.ji, dateInfo[1]);
		
		try {
			
			JSONObject json = new JSONObject(str2.toString());
			String title = json.getString("name");
			rv.setTextViewText(R.id.constellation, "幸运星座："+title);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			try {

				switch (msg.what) {
				case 1: //
					if(progressDialog!=null && progressDialog.isShowing())
					{
						progressDialog.dismiss();
					}
					break;
				case 2: //
					progressDialog=new ProgressDialog(context);
					progressDialog.show(context,  "请稍等...",  "获取数据中...");

					break;

				default:
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

