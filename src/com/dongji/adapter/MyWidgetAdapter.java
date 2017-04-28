package com.dongji.adapter;

import java.util.List;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.launcher.R;

public class MyWidgetAdapter extends BaseAdapter{

	  Context context ;
	  List<AppWidgetProviderInfo>  allAppWidgetInfo;
	  PackageManager p;
	public MyWidgetAdapter(Context context,List<AppWidgetProviderInfo>  allAppWidgetInfo) {
		this.context = context;
		this.allAppWidgetInfo =allAppWidgetInfo;
		p = context.getPackageManager();
	}

	@Override
	public int getCount() {
		return allAppWidgetInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		
		
		AppWidgetProviderInfo  appWidget = allAppWidgetInfo.get(position);
		convertView = LayoutInflater.from(context).inflate(R.layout.add_widget_all_item, null);
        ImageView img = (ImageView)convertView.findViewById(R.id.img);
		
		TextView tv = (TextView)convertView.findViewById(R.id.tv);
		TextView tv_size = (TextView)convertView.findViewById(R.id.tv_size);
		
		if(appWidget.label.equals("动机提醒日历"))
		{
			tv.setText(appWidget.label);
			img.setImageResource(R.drawable.ic_launcher);
			tv_size.setText("4x4");
		}else{
			try {
				img.setBackgroundDrawable(p.getApplicationIcon(appWidget.provider.getPackageName()));
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

//			AppWidgetHost  host = new AppWidgetHost(Launcher.this, 1024);
//			int appWidgetId = host.allocateAppWidgetId();
//			fra.addView(host.createView(context, appWidgetId, appWidget));
//			fra.setClickable(false);
//			fra.setFocusable(false);
			tv.setText(appWidget.label);
			
			final Resources resources = context.getResources();
   	        int actualWidth = resources.getDimensionPixelSize(R.dimen.cell_width);
   	        int actualHeight = resources.getDimensionPixelSize(R.dimen.cell_height);
   	        int smallerSize = Math.min(actualWidth, actualHeight);

   	     // Always round up to next largest cell
   	        int min_width = (appWidget.minWidth  + smallerSize) / smallerSize;
   	        int min_height = (appWidget.minHeight + smallerSize) / smallerSize;
			
			if(min_height ==0)
			{
				min_height =1;
			}
			
			if(min_width ==0)
			{
				min_width = 1;
			}
			
			tv_size.setText(min_width +"x" + min_height);
		}
		
		return convertView;
	}
}