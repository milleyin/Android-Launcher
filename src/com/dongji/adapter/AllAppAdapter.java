package com.dongji.adapter;

import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.ApplicationInfo;

import com.dongji.launcher.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AllAppAdapter extends BaseAdapter {

	List<ApplicationInfo>  list;
	Context context;
	PackageManager pm;
	ArrayList<ApplicationInfo> contents;
	
	public AllAppAdapter(Context context,List<ApplicationInfo> list,PackageManager pm,ArrayList<ApplicationInfo> contents) {
	
		this.context = context;
		this.list = list;
		this.pm = pm;
		this.contents = contents;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		convertView = LayoutInflater.from(context).inflate(R.layout.allappgriditem, null);
		
		final ViewHolder viewHolder = new ViewHolder();
		
		viewHolder.appimage = (ImageView) convertView.findViewById(R.id.appimg);
		viewHolder.tv = (TextView) convertView.findViewById(R.id.appname);
		viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb);
		
		ApplicationInfo app = list.get(position);

		if (contents.size() > 0){
			
			for (int i = 0;i<contents.size();i++){
				
				ApplicationInfo visibilityApp  = contents.get(i);
				
				if (app.getPackageName().equals(visibilityApp.getPackageName())) {
					viewHolder.cb.setChecked(true);
				}
				
			}
			
		} else {
//			viewHolder.cb.setChecked(false);
		}
		
		viewHolder.appimage.setBackgroundDrawable(app.icon);
		viewHolder.appimage.setTag(app);
		
		String label = app.title.toString();
		if ( label.length() > 3){
			label = label.substring(0, 3)+"..";
		}
		
		viewHolder.tv.setText(label);
		
		return convertView;
	}
	
	class ViewHolder{
		CheckBox cb;
		ImageView imageView;
		ImageView appimage;
		TextView tv;
	}

}
