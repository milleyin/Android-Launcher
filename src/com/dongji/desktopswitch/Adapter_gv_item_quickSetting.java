package com.dongji.desktopswitch;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.launcher.R;

public class Adapter_gv_item_quickSetting extends BaseAdapter {
	public static final String QUICKSETTING_IMAGE = "quicksetting_image";
	public static final String QUICKSETTING_NAME = "quicksetting_name";
	public static final String QUICKSETTING_FLAG = "quicksetting_flag";

	public static boolean flag_wifi = false;
	public static boolean flag_flyModel = false;
	public static boolean flag_sync = false;
	public static boolean flag_autorotation = false;

	private Context context;
	private ArrayList<HashMap<String, Object>> data;

	public Adapter_gv_item_quickSetting(Context context,
			ArrayList<HashMap<String, Object>> data) {
		this.context = context;
		this.data = data;
		flag_wifi = Boolean.valueOf(data.get(0).get(QUICKSETTING_FLAG)
				.toString());
		flag_flyModel = Boolean.valueOf(data.get(1).get(QUICKSETTING_FLAG)
				.toString());
		flag_sync = Boolean.valueOf(data.get(2).get(QUICKSETTING_FLAG)
				.toString());
		flag_autorotation = Boolean.valueOf(data.get(3).get(QUICKSETTING_FLAG)
				.toString());
	}

	@Override
	public int getCount() {
		return null == data ? 0 : data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(context).inflate(
				R.layout.gridview_item_quicksetting, null);
		ImageView iv_quickSetting = (ImageView) convertView
				.findViewById(R.id.iv_quickSetting);
		TextView tv_quickSetting = (TextView) convertView
				.findViewById(R.id.tv_quickSetting);

		iv_quickSetting.setImageResource(Integer.valueOf(data.get(position)
				.get(QUICKSETTING_IMAGE).toString()));
		tv_quickSetting.setText(data.get(position).get(QUICKSETTING_NAME)
				.toString());

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tvString = ((TextView) v
						.findViewById(R.id.tv_quickSetting)).getText()
						.toString();
				ImageView imageView = (ImageView) v
						.findViewById(R.id.iv_quickSetting);
				if (tvString.equals(context.getText(R.string.tv_Flymodel))) {
					// boolean isOn = SettingTools.getFlyModelStatus(context);
					flag_flyModel = !flag_flyModel;
					changeSettingImage(R.string.tv_Flymodel, imageView);
					return;
				}
				if (tvString.equals(context.getText(R.string.tv_Wifi))) {
					flag_wifi = !flag_wifi;
					changeSettingImage(R.string.tv_Wifi, imageView);
					return;
				}
				if (tvString.equals(context.getText(R.string.tv_Sync))) {
					flag_sync = !flag_sync;
					changeSettingImage(R.string.tv_Sync, imageView);
					return;
				}
				if (tvString.equals(context.getText(R.string.tv_Rotate))) {
					flag_autorotation = !flag_autorotation;
					changeSettingImage(R.string.tv_Rotate, imageView);
					return;
				}
			}
		});

		return convertView;
	}

	private void changeSettingImage(int ivId, ImageView imageView) {
		switch (ivId) {
		case R.string.tv_Wifi:
			if (flag_wifi) {
				imageView.setImageResource(R.drawable.setting_power_wifi_on);
			} else {
				imageView.setImageResource(R.drawable.setting_power_wifi_off);
			}
			break;
		case R.string.tv_Flymodel:
			if (flag_flyModel) {
				imageView
						.setImageResource(R.drawable.setting_power_flymodel_on);
			} else {
				imageView
						.setImageResource(R.drawable.setting_power_flymodel_off);
			}
			break;
		case R.string.tv_Sync:
			if (flag_sync) {
				imageView.setImageResource(R.drawable.setting_power_sync_on);
			} else {
				imageView.setImageResource(R.drawable.setting_power_sync_off);
			}
			break;
		case R.string.tv_Rotate:
			if (flag_autorotation) {
				imageView
						.setImageResource(R.drawable.setting_power_rotation_on);
			} else {
				imageView
						.setImageResource(R.drawable.setting_power_rotation_off);
			}
			break;
		}
	}
}
