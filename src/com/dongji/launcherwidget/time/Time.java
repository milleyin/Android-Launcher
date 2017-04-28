package com.dongji.launcherwidget.time;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.dongji.launcher.R;

public class Time extends AppWidgetProvider {
	
	private static final String RETURN_SETTING_DATE_AND_TIME = "com.dongji.launcher.time";
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
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
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
		
		if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIME_TICK)){
			
			AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
			int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(context, Time.class));
			
			for (int i = 0; i < appIds.length; i++) {
				int appWidgetId = appIds[i];
				updateAppWidget(context, appWidgetManger, appWidgetId);
			}
		} else if (action.equals(RETURN_SETTING_DATE_AND_TIME)){
			
			Intent setting = new Intent(Settings.ACTION_DATE_SETTINGS);
			setting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(setting);
			
		} 
		
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
	}
	
	
	private void updateAppWidget(Context context,AppWidgetManager appWidgetManager,int appWidgetIds){
		
		rv = new RemoteViews(context.getPackageName(), R.layout.time);
		
			
		String timeString = df.format(new Date(System.currentTimeMillis()));
		int num;
		for (int i = 0; i < numberView.length; i++) {
			num = timeString.charAt(i) - 48;
			rv.setImageViewResource(numberView[i], numberIcon[num]);
		}
		
		Intent it = new Intent(RETURN_SETTING_DATE_AND_TIME);
		PendingIntent pt = PendingIntent.getBroadcast(context, 0, it, 0);
		rv.setOnClickPendingIntent(R.id.timely, pt);
		
		ComponentName component = new ComponentName(context, Time.class);
		appWidgetManager.updateAppWidget(component, rv);
		
	}

}

