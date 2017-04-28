package com.dongji.launcherwidget.weather;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mobi.intuitit.android.content.LauncherIntent.Action;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.adapter.CityListAdapter;
import com.dongji.launcher.R;

public class CitySearchDialogActivity extends Activity implements OnClickListener{

	LinearLayout cityly;
	Button searchbtn;
	AutoCompleteTextView searchinfo;
	GridView citynamegv;
	SharedPreferences sp = null;
	SharedPreferences.Editor editor = null;
	List<WeatherInfo> lstImageItem = new ArrayList<WeatherInfo>();
	InputStream is = null;
	String[] arrayStr = null;
	String[] keyStr = null;
//	HashMap<String, Object> map = null;
	ConnectivityManager connManager = null;
	State state =  null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.citylist);
		
		 connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); // 获取网络连接状态

		sp = getSharedPreferences("citycode_sp", 0);
    	editor = sp.edit();
		
    	cityly = (LinearLayout) findViewById(R.id.citily);
		searchbtn = (Button) findViewById(R.id.searchbtn);
		searchinfo = (AutoCompleteTextView) findViewById(R.id.searchinfo);
		citynamegv = (GridView) findViewById(R.id.citynamegv); 
		
		searchbtn.setOnClickListener(this);
		cityly.requestFocus();
		cityly.setFocusable(true);
		cityly.setClickable(true);
		cityly.setOnClickListener(this);
		
		getAllData();
		
		CityListAdapter cityListAdapter = new CityListAdapter(this, lstImageItem);
		citynamegv.setAdapter(cityListAdapter);
		citynamegv.setOnItemClickListener(new ItemClickListener());
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, keyStr);
        searchinfo.setAdapter(arrayAdapter);

	}

	private void getAllData() {
		try {
			
			is = getResources().openRawResource(R.raw.citycode);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);

			String str = new String(buffer, "utf-8");
			str = str.replace("{", "").replace("}", "");
			arrayStr = str.split(",");
			
//			map = new HashMap<String, Object>();
			keyStr = new String[arrayStr.length];
			
			for (int s = 0; s < arrayStr.length; s++) {
				
				WeatherInfo weatherinfo = new WeatherInfo();

				String[] str1 = arrayStr[s].split(":");
				String key = str1[0].replace(" ", "").replace("\"", "");
				
				keyStr[s] = key;
//				map.put(key, str1[1].replace("\"", ""));
				
				weatherinfo.setCity(key);
				weatherinfo.setCode(str1[1].replace("\"", ""));
				
				lstImageItem.add(weatherinfo);
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	
	 class  ItemClickListener implements OnItemClickListener
	    {
			public void onItemClick(AdapterView<?> arg0,View view,int arg2,long arg3) {
				
				TextView cityname = (TextView) view.findViewById(R.id.citynameinfo);
				
				String citycode = cityname.getTag().toString();
				try{
		            state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); // 获取网络连接状态
		            if (State.CONNECTED == state) { // 判断是否正在使用WIFI网络
		                
		            	editor.putString("citycode", citycode);
		            	editor.commit();
		            	
		            	Intent widget = new Intent();
						widget.setAction("com.dongji.launcher.weather.refresh");
						sendBroadcast(widget);
						
						CitySearchDialogActivity.this.finish();
		            	
		            } else {
		            	 Toast.makeText(CitySearchDialogActivity.this, "您的网络未连接！", Toast.LENGTH_LONG).show();
		            }
				}catch (Exception ex) {
	    			ex.printStackTrace();
	    		}
				
			}
	    	
	    }

	 
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.searchbtn:
			
			 String cityName = searchinfo.getText().toString();
			
			 boolean isExist = false;
			 
			for (int i =0;i<lstImageItem.size();i++) {
				
				WeatherInfo weatherinfo = (WeatherInfo) lstImageItem.get(i);
				
				if(weatherinfo.getCity().equals(cityName)) {
					
					isExist = true;
					try{
						state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); // 获取网络连接状态
		 	            if (State.CONNECTED == state) { // 判断是否正在使用WIFI网络
		 	            	
		 	            	editor.putString("citycode", weatherinfo.getCode());
			            	editor.commit();
			            	
			            	Intent widget = new Intent();
							widget.setAction("com.dongji.launcher.weather.refresh");
							sendBroadcast(widget);
							
							CitySearchDialogActivity.this.finish();
		 	            	
		 	            } else {
			            	Toast.makeText(CitySearchDialogActivity.this, "您的网络未连接！", Toast.LENGTH_LONG).show();
			            }
					}catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}
				
			}
			
			if (!isExist) {
				Toast.makeText(CitySearchDialogActivity.this, "无法查找到该城市信息！", Toast.LENGTH_LONG).show();
			}
			
			break;
			
		default:
			break;
		}
	}


}
