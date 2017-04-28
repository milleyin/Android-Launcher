package com.dongji.desktopswitch;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dongji.launcher.R;

public class PopupWindow_ChoosePower {
	private Context context;
	private PopupWindow_Setting popupWindow_Setting;
	private String customePowerName;
	private View layout_popup_choosepower;

	private ListView lv_choose_power;
	private Adapter_lv_item_choosepower adapter_lv_item_choosepower;
	private static ChoosePowerReceiver choosePowerReceiver;
	private ArrayList<HashMap<String, Object>> listitem;

	public PopupWindow_ChoosePower(Context context,
			PopupWindow_Setting popupWindow_Setting, String customePowerName) {
		this.context = context;
		this.popupWindow_Setting = popupWindow_Setting;
		this.customePowerName = customePowerName;

		choosePowerReceiver = new ChoosePowerReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter
				.addAction(PopupWindow_CustomPowerModel.ACTION_SAVE_CUSTOMPOWER);
		context.registerReceiver(choosePowerReceiver, intentFilter);
	}

	public static PopupWindow getInstance(Context context,
			PopupWindow_Setting popupWindow_Setting, String customePowerName) {
		PopupWindow_ChoosePower popupWindow_ChoosePower = new PopupWindow_ChoosePower(
				context, popupWindow_Setting, customePowerName);
		PopupWindow pw_choosepower = new PopupWindow(
				popupWindow_ChoosePower.getChoosePowerPopupwindow(),
				SettingTools.px2dip(context, 314),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		return pw_choosepower;
	}

	public View getChoosePowerPopupwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout_popup_choosepower = layoutInflater.inflate(
				R.layout.layout_popup_power, null);

		ImageView iv_choose_power_close = (ImageView) layout_popup_choosepower
				.findViewById(R.id.iv_choose_power_close);

		lv_choose_power = (ListView) layout_popup_choosepower
				.findViewById(R.id.lv_choose_power);

		iv_choose_power_close.setOnClickListener(new IVClickListener());

		lv_choose_power
				.setOnItemClickListener(new ChoosePowerItemClickListener());

		getChoosePowerListData(lv_choose_power);

		return layout_popup_choosepower;
	}

	private void getChoosePowerListData(ListView listView) {
		listitem = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> tempMap;

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_choosepower.POWER_NAME,
				context.getString(R.string.tv_power_normal));
		tempMap.put(Adapter_lv_item_choosepower.POWER_TIP,
				context.getString(R.string.tv_power_normal_tip));
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_choosepower.POWER_NAME,
				context.getString(R.string.tv_power_meeting));
		tempMap.put(Adapter_lv_item_choosepower.POWER_TIP,
				context.getString(R.string.tv_power_meeting_tip));
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_choosepower.POWER_NAME, customePowerName);
		tempMap.put(Adapter_lv_item_choosepower.POWER_TIP,
				context.getString(R.string.tv_power_custom_tip));
		listitem.add(tempMap);

		adapter_lv_item_choosepower = new Adapter_lv_item_choosepower(context,
				listitem, popupWindow_Setting);

		listView.setAdapter(adapter_lv_item_choosepower);

	}

	private class IVClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_choose_power_close:
				onCloseChoosePower();
				break;
			}
		}
	}

	private class ChoosePowerItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			ImageView iv_power_checkable = (ImageView) arg1
					.findViewById(R.id.iv_power_checkable);
			TextView textView = (TextView) arg1
					.findViewById(R.id.tv_power_name);
			iv_power_checkable.setImageResource(R.drawable.setting_checkon);
			if (textView.getText().equals(
					context.getString(R.string.tv_power_normal))) {
				chooseNormalPower();
				SettingTools
						.setSharepreferences(
								context,
								Adapter_lv_item_choosepower.SHAREPREFERENCES_CHOOSEPOWER,
								0);
			} else if (textView.getText().equals(
					context.getString(R.string.tv_power_meeting))) {
				chooseMeetingPower();
				SettingTools
						.setSharepreferences(
								context,
								Adapter_lv_item_choosepower.SHAREPREFERENCES_CHOOSEPOWER,
								1);
			} else {
				chooseCustomPower();
				SettingTools
						.setSharepreferences(
								context,
								Adapter_lv_item_choosepower.SHAREPREFERENCES_CHOOSEPOWER,
								2);
			}
			popupWindow_Setting.closePopupWindow_ChoosePower();
		}
	}

	private void chooseNormalPower() {
		SettingTools.setWifi(context, true);
		SettingTools.setSync(false);
	}

	private void chooseMeetingPower() {
		SettingTools.setWifi(context, false);
		SettingTools.setSync(false);
		SettingTools.setRingInt(context, 0);
		SettingTools.setMusicInt(context, 0);
	}

	private void chooseCustomPower() {
		CustomPowerDBHelper customPowerDBHelper = new CustomPowerDBHelper(
				context);
		final SettingInt settingInt = customPowerDBHelper.selectAllInt();
		SettingTools.setBrightInt(context, settingInt.bright);
		popupWindow_Setting.onChangeBright();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				if (null != settingInt) {
					SettingTools.setWifi(context, settingInt.wifi == 1 ? true
							: false);
					SettingTools.setFlyModelStatus(context,
							settingInt.flymodel == 1 ? true : false);
					SettingTools.setSync(settingInt.sync == 1 ? true : false);
					SettingTools.setAutoRotation(context,
							settingInt.autoratation == 1 ? true : false);
					SettingTools.setMusicInt(context, settingInt.music);
					SettingTools.setRingInt(context, settingInt.ring);
					SettingTools.setSleepInt(context, settingInt.sleep);
				}
			}
		});
		thread.start();
	}

	private void onCloseChoosePower() {
		popupWindow_Setting.closePopupWindow_ChoosePower();
	}

	private class ChoosePowerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (PopupWindow_CustomPowerModel.ACTION_SAVE_CUSTOMPOWER
					.equals(intent.getAction())) {
				String customPowerName = intent
						.getStringExtra(PopupWindow_CustomPowerModel.ACTION_SAVE_CUSTOMPOWER_NAME);
				if (!customPowerName.trim().equals("")) {

					popupWindow_Setting.closePopupWindow_CustomPower();
					listitem.get(2).put(Adapter_lv_item_choosepower.POWER_NAME,
							customPowerName);
					adapter_lv_item_choosepower.updateData(listitem);
					adapter_lv_item_choosepower.notifyDataSetChanged();
				}
			}
		}
	}

	public static void unregisterchoosePowerReceiver(Context context) {
		if (null != choosePowerReceiver) {
			context.unregisterReceiver(choosePowerReceiver);
		}
	}
}
