package com.dongji.desktopswitch;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.dongji.launcher.R;

public class PopupWindow_CustomPowerModel {
	public static final String ACTION_SAVE_CUSTOMPOWER = "action_save_custompower";
	public static final String ACTION_SAVE_CUSTOMPOWER_NAME = "action_save_custompower_name";

	public static String powerName;

	private Context context;
	private PopupWindow_Setting popupWindow_Setting;

	private View layout_popup_custompower;

	private EditText etCustomPowerName;
	private ImageView ivCustomPowerClose;
	private GridView gvCustomPowerQuickSetting;
	private ListView lvCustomPowerScrollSetting;
	private Button btnCustomPowerSave, btnCustomPowerCancel;

	private Adapter_gv_item_quickSetting adapter_gv_item_quickSetting;
	private Adapter_lv_item_scrollSetting adapter_lv_item_scrollSetting;

	public PopupWindow_CustomPowerModel(Context context,
			PopupWindow_Setting popupWindow_Setting) {
		this.context = context;
		this.popupWindow_Setting = popupWindow_Setting;
	}

	public static PopupWindow getInstance(Context context,
			PopupWindow_Setting popupWindow_Setting) {
		PopupWindow_CustomPowerModel popupWindow_CustomPowerModel = new PopupWindow_CustomPowerModel(
				context, popupWindow_Setting);
		PopupWindow popupWindow = new PopupWindow(
				popupWindow_CustomPowerModel.getCustomPowerModelPopwindow(),
				SettingTools.px2dip(context, 314),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		return popupWindow;
	}

	public View getCustomPowerModelPopwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout_popup_custompower = layoutInflater.inflate(
				R.layout.layout_popup_custompower, null);

		etCustomPowerName = (EditText) layout_popup_custompower
				.findViewById(R.id.etCustomPowerName);
		ivCustomPowerClose = (ImageView) layout_popup_custompower
				.findViewById(R.id.ivCustomPowerClose);
		gvCustomPowerQuickSetting = (GridView) layout_popup_custompower
				.findViewById(R.id.gvCustomPowerQuickSetting);
		lvCustomPowerScrollSetting = (ListView) layout_popup_custompower
				.findViewById(R.id.lvCustomPowerScrollSetting);
		btnCustomPowerSave = (Button) layout_popup_custompower
				.findViewById(R.id.btnCustomPowerSave);
		btnCustomPowerCancel = (Button) layout_popup_custompower
				.findViewById(R.id.btnCustomPowerCancel);

		CustomPowerDBHelper customPowerDBHelper = new CustomPowerDBHelper(
				context);
		if (customPowerDBHelper.selectIsHasData()) {
			SettingInt settingInt = customPowerDBHelper.selectAllInt();
			powerName = settingInt.powername;
			// System.out.println("wifi......1......" + settingInt.wifi);
			// System.out.println("flag_flyModel......1......"
			// + settingInt.flymodel);
			// System.out.println("flag_sync........1...." + settingInt.sync);
			// System.out.println("autoratation.....1......."
			// + settingInt.autoratation);
			init_gvCustomPowerQuickSetting(settingInt);
			init_lvCustomPowerScrollSetting(settingInt);
		}

		ivCustomPowerClose.setOnClickListener(new OnBtnClickListener());
		btnCustomPowerSave.setOnClickListener(new OnBtnClickListener());
		btnCustomPowerCancel.setOnClickListener(new OnBtnClickListener());

		return layout_popup_custompower;

		// pw_choosepower = new PopupWindow(layout_popup_choosepower, 260,
		// ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	private void init_gvCustomPowerQuickSetting(SettingInt settingInt) {
		ArrayList<HashMap<String, Object>> listitem = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> tempMap;

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_NAME,
				context.getString(R.string.tv_Wifi));
		if (settingInt.wifi == 1) {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_wifi_on);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, true);
		} else {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_wifi_off);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, false);
		}
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_NAME,
				context.getString(R.string.tv_Flymodel));
		if (settingInt.flymodel == 1) {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_flymodel_on);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, true);
		} else {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_flymodel_off);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, false);
		}
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_NAME,
				context.getString(R.string.tv_Sync));
		if (settingInt.sync == 1) {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_sync_on);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, true);
		} else {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_sync_off);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, false);
		}
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_NAME,
				context.getString(R.string.tv_Rotate));
		if (settingInt.autoratation == 1) {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_rotation_on);
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, true);
		} else {
			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_IMAGE,
					R.drawable.setting_power_rotation_off);

			tempMap.put(Adapter_gv_item_quickSetting.QUICKSETTING_FLAG, false);
		}
		listitem.add(tempMap);

		adapter_gv_item_quickSetting = new Adapter_gv_item_quickSetting(
				context, listitem);

		gvCustomPowerQuickSetting.setAdapter(adapter_gv_item_quickSetting);
	}

	private void init_lvCustomPowerScrollSetting(SettingInt settingInt) {
		ArrayList<HashMap<String, Object>> listitem = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> tempMap;

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_IMAGE,
				R.drawable.setting_power_bright);
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_SEEK,
				SettingTools.castIntToProgress_Bright(settingInt.bright));
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_IMAGE,
				R.drawable.setting_power_music);
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_SEEK,
				settingInt.music);
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_IMAGE,
				R.drawable.setting_power_ring);
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_SEEK,
				settingInt.ring);
		listitem.add(tempMap);

		tempMap = new HashMap<String, Object>();
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_IMAGE,
				R.drawable.setting_power_sleep);
		tempMap.put(Adapter_lv_item_scrollSetting.SCROLLSETTING_SEEK,
				settingInt.sleep);
		listitem.add(tempMap);

		adapter_lv_item_scrollSetting = new Adapter_lv_item_scrollSetting(
				context, listitem);

		lvCustomPowerScrollSetting.setAdapter(adapter_lv_item_scrollSetting);
	}

	private class OnBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ivCustomPowerClose:
				break;
			case R.id.btnCustomPowerSave:
				String customPowerName = etCustomPowerName.getText().toString()
						.trim();
				CustomPowerDBHelper customPowerDBHelper = new CustomPowerDBHelper(
						context);
				final SettingInt settingInt = new SettingInt();
				settingInt.autoratation = Adapter_gv_item_quickSetting.flag_autorotation ? 1
						: 0;
				settingInt.flymodel = Adapter_gv_item_quickSetting.flag_flyModel ? 1
						: 0;
				settingInt.sync = Adapter_gv_item_quickSetting.flag_sync ? 1
						: 0;
				settingInt.wifi = Adapter_gv_item_quickSetting.flag_wifi ? 1
						: 0;

				// System.out.println("wifi............" + settingInt.wifi);
				// System.out.println("flag_flyModel............"
				// + settingInt.flymodel);
				// System.out.println("flag_sync............" +
				// settingInt.sync);
				// System.out.println("autoratation............"
				// + settingInt.autoratation);

				settingInt.bright = SettingTools
						.castProgressToInt_Bright(Adapter_lv_item_scrollSetting.progress_bright);
				settingInt.music = Adapter_lv_item_scrollSetting.progress_music;
				settingInt.ring = Adapter_lv_item_scrollSetting.progress_ring;
				settingInt.sleep = Adapter_lv_item_scrollSetting.int_sleep;
				System.out.println("......sleep..........." + settingInt.sleep);
				if (!customPowerName.equals("")) {
					settingInt.powername = customPowerName;
					Intent intent = new Intent();
					intent.setAction(ACTION_SAVE_CUSTOMPOWER);
					intent.putExtra(ACTION_SAVE_CUSTOMPOWER_NAME,
							customPowerName);
					context.sendBroadcast(intent);
				} else {
					settingInt.powername = powerName;
				}
				if (customPowerDBHelper.selectIsHasData()) {
					customPowerDBHelper.updateAllSettingInt(settingInt);
				} else {
					settingInt.powername = "自定义模式";
					customPowerDBHelper.insertIntoSettingInt(settingInt);
				}
				if (SettingTools
						.getSharedPreferences(
								context,
								Adapter_lv_item_choosepower.SHAREPREFERENCES_CHOOSEPOWER) == 2) {
					SettingTools.setBrightInt(context, settingInt.bright);
					popupWindow_Setting.onChangeBright();
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {

							if (null != settingInt) {
								SettingTools.setWifi(context,
										settingInt.wifi == 1 ? true : false);
								SettingTools
										.setFlyModelStatus(context,
												settingInt.flymodel == 1 ? true
														: false);
								SettingTools
										.setSync(settingInt.sync == 1 ? true
												: false);
								SettingTools.setAutoRotation(context,
										settingInt.autoratation == 1 ? true
												: false);
								SettingTools.setMusicInt(context,
										settingInt.music);
								SettingTools.setRingInt(context,
										settingInt.ring);
								SettingTools.setSleepInt(context,
										settingInt.sleep);
							}
						}
					});
					thread.start();
				}

				break;
			case R.id.btnCustomPowerCancel:
				break;
			}
			popupWindow_Setting.closePopupWindow_CustomPower();
		}
	}
}
