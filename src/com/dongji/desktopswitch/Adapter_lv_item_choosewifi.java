package com.dongji.desktopswitch;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.launcher.R;

public class Adapter_lv_item_choosewifi extends BaseAdapter {

	public static final String WIFI_NAME = "wifi_name";
	public static final String WIFI_MSG = "wifi_msg";
	public static final String WIFI_LEVEL = "wifi_level";
	public static final String WIFI_PWD = "wifi_pwd";

	private Context context;
	private ArrayList<WifiInfo> wifiInfos;

	// private PopupWindow_Setting popupWindow_Setting;

	public Adapter_lv_item_choosewifi(Context context,
			ArrayList<WifiInfo> wifiInfos) {
		this.context = context;
		this.wifiInfos = wifiInfos;
		// this.popupWindow_Setting = popupWindow_Setting;
	}

	public void updateData(ArrayList<WifiInfo> wifiInfos) {
		this.wifiInfos = wifiInfos;
	}

	@Override
	public int getCount() {
		return null == wifiInfos ? 0 : wifiInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return wifiInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.listview_item_choose_wifi, null);
		ImageView iv_wifi_checkable = (ImageView) convertView
				.findViewById(R.id.iv_wifi_checkable);
		final TextView tv_wifi_name = (TextView) convertView
				.findViewById(R.id.tv_wifi_name);
		TextView tv_wifi_msg = (TextView) convertView
				.findViewById(R.id.tv_wifi_msg);
		ImageView iv_wifi_level = (ImageView) convertView
				.findViewById(R.id.iv_wifi_level);
		ImageView iv_wifi_lock = (ImageView) convertView
				.findViewById(R.id.iv_wifi_lock);

		tv_wifi_name.setText(wifiInfos.get(position).wifiname);
		tv_wifi_msg.setText(wifiInfos.get(position).wifimsg);
		iv_wifi_level.setImageResource(getImageIdByLevel(wifiInfos
				.get(position).wifilevel));

		if (tv_wifi_msg.getText().toString().trim().equals("已连接")) {
			iv_wifi_checkable.setImageResource(R.drawable.setting_checkon);
		}

		if (tv_wifi_msg.getText().toString().trim().equals("")) {
			iv_wifi_lock.setVisibility(View.GONE);
		}
		return convertView;
	}

	private int getImageIdByLevel(int level) {
		int imageId = R.drawable.setting_wifi_level_1;
		switch (level) {
		case 1:
			imageId = R.drawable.setting_wifi_level_1;
			break;
		case 2:
			imageId = R.drawable.setting_wifi_level_2;
			break;
		case 3:
			imageId = R.drawable.setting_wifi_level_3;
			break;
		case 4:
		case 5:
			imageId = R.drawable.setting_wifi_level_4;
			break;
		}
		return imageId;
	}
}
