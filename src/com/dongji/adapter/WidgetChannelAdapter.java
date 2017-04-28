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
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongji.launcher.R;


public class WidgetChannelAdapter extends BaseAdapter {

	private Context context;
	private List<AppWidgetProviderInfo> data;
	private PackageManager pm;
	private AppWidgetProviderInfo providerInfo;
	
	public WidgetChannelAdapter(Context context,
			List<AppWidgetProviderInfo> data) {
		super();
		this.context = context;
		this.data = data;
		pm = context.getPackageManager();
	}

	@Override
	public int getCount() {
		return data != null ? data.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void reset() {
		if (data != null && data.size() > 0) {
			data.clear();
		}
	}
	
	public void addData(List<AppWidgetProviderInfo> list) {
		if (list != null && list.size() > 0) {
			data.addAll(list);
			notifyDataSetChanged();
		}
	}
	
	/**
	 * 根据包名删除卸载列表对应的数据，并刷新列表
	 * @param packageName
	 */
	public void removeAppDataByPackageName(String packageName) {
		if(data!=null && data.size()>0) {
			for(int i=0;i<data.size();i++) {
				AppWidgetProviderInfo info=data.get(i);
				if(packageName.equals(info.provider.getPackageName())) {
					data.remove(i--);
				}
			}
			notifyDataSetChanged();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.widget_gridview_item, null);
			convertView.setLayoutParams(new AbsListView.LayoutParams(220, 220));
			holder = new ViewHolder();
			holder.mWidgetNameTV = (TextView) convertView.findViewById(R.id.widget_name);
			holder.mWidgetSizeTV = (TextView) convertView.findViewById(R.id.widget_size);
			holder.mWidgetIconIV = (ImageView) convertView.findViewById(R.id.widget_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		providerInfo = data.get(position);
		
		
		if(providerInfo.label.equals("动机提醒日历"))
		{
			
			holder.mWidgetNameTV.setText(providerInfo.label);
			holder.mWidgetSizeTV.setText("4x4");
			holder.mWidgetIconIV.setImageResource(R.drawable.ic_launcher);
			holder.mWidgetIconIV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			
		}else{
			holder.mWidgetNameTV.setText(providerInfo.label);
			holder.mWidgetSizeTV.setText(getWidgetSize(providerInfo.minWidth, providerInfo.minHeight));
			try {
				holder.mWidgetIconIV.setImageDrawable(pm.getApplicationIcon(providerInfo.provider.getPackageName()));
				holder.mWidgetIconIV.setLayoutParams(new LinearLayout.LayoutParams(convertIconSize(providerInfo.minWidth), convertIconSize(providerInfo.minHeight)));
				System.out.println(providerInfo.label + " : minWidth=====>" + providerInfo.minWidth + ", minHeight=====>" + providerInfo.minHeight);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return convertView;
	}
	
	/**
	 * 计算widget放置到桌面上时占据的单元格数
	 * @param width
	 * @param height
	 * @return
	 */
	private String getWidgetSize(int width, int height) {
//		int mWidth, mHeight;
//		mWidth = width < 72 ? 1 : width/72;
//		mHeight = height < 72 ? 1 : height/72;
//		return mWidth + "x" + mHeight;
		
		final Resources resources = context.getResources();
	    int actualWidth = resources.getDimensionPixelSize(R.dimen.cell_width);
	    int actualHeight = resources.getDimensionPixelSize(R.dimen.cell_height);
	    int smallerSize = Math.min(actualWidth, actualHeight);

	    // Always round up to next largest cell
	    int min_width = (width  + smallerSize) / smallerSize;
	    int min_height = (height + smallerSize) / smallerSize;
	        
    	return min_width + "x" + min_height;
	}
	
	private int convertIconSize(int size) {
		int temp = size;
		if (size > 100) {
			temp = size/3;
		}
		return temp;
	}
	
	class ViewHolder {
		TextView mWidgetNameTV, mWidgetSizeTV;
		ImageView mWidgetIconIV;
	}

}
