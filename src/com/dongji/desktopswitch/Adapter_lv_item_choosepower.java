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
import android.widget.Toast;

import com.dongji.launcher.R;

public class Adapter_lv_item_choosepower extends BaseAdapter {

	public static final String POWER_NAME = "power_name";
	public static final String POWER_TIP = "power_tip";
	public static final String SHAREPREFERENCES_CHOOSEPOWER = "share_choosepower";

	private Context context;
	private ArrayList<HashMap<String, Object>> data;
	private PopupWindow_Setting popupWindow_Setting;

	public Adapter_lv_item_choosepower(Context context,
			ArrayList<HashMap<String, Object>> data,
			PopupWindow_Setting popupWindow_Setting) {
		this.context = context;
		this.data = data;
		this.popupWindow_Setting = popupWindow_Setting;
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

	public void updateData(ArrayList<HashMap<String, Object>> data) {
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.listview_item_choose_power, null);
		ImageView iv_power_checkable = (ImageView) convertView
				.findViewById(R.id.iv_power_checkable);
		TextView tv_power_name = (TextView) convertView
				.findViewById(R.id.tv_power_name);
		TextView tv_power_tip = (TextView) convertView
				.findViewById(R.id.tv_power_tip);
		ImageView iv_choose_power_setting = (ImageView) convertView
				.findViewById(R.id.iv_choose_power_setting);

		int choosepower = SettingTools.getSharedPreferences(context,
				SHAREPREFERENCES_CHOOSEPOWER);
		if (position == choosepower) {
			iv_power_checkable.setImageResource(R.drawable.setting_checkon);
		} else {
			iv_power_checkable.setImageResource(R.drawable.setting_checkoff);
		}
		tv_power_name.setText(data.get(position).get(POWER_NAME).toString());
		tv_power_tip.setText(data.get(position).get(POWER_TIP).toString());

		if (position == 2) {
			iv_choose_power_setting.setVisibility(View.VISIBLE);
			iv_choose_power_setting.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popupWindow_Setting.showCustomPowerPopupWindow();
				}
			});
		}

		return convertView;
	}
}
