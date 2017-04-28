package com.dongji.desktopswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.dongji.launcher.R;

public class PopupWindow_Setting {

	private Context context;
	private View view;

	private PopupWindow pw_choosepower;
	private PopupWindow pw_choosewifi;
	private PopupWindow pw_Bright;
	private PopupWindow pw_connectWifi;
	private PopupWindow pw_customPower;
	private View layout_popup_setting;

	private ImageView iv_setting_system;
	private ImageView iv_setting_bright;
	private ImageView iv_setting_flyModel;
	private TextView tv_setting_power_int;
	private ImageView iv_setting_power;
	private ImageView iv_setting_wifi;
	private ImageView iv_setting_sync;

	public static SettingBroadcastReceiver settingBroadcastReceiver;

	public PopupWindow_Setting(Context context, View view) {
		this.context = context;
		this.view = view;
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		settingBroadcastReceiver = new SettingBroadcastReceiver();
		context.registerReceiver(settingBroadcastReceiver, intentFilter);
	}

	public static PopupWindow getInstance(Context context, View view) {
		PopupWindow_Setting popupWindow_Setting = new PopupWindow_Setting(
				context, view);
		PopupWindow pw_Setting = new PopupWindow(
				popupWindow_Setting.getSettingPopwindow(), SettingTools.px2dip(
						context, 314), ViewGroup.LayoutParams.WRAP_CONTENT);
		return pw_Setting;
	}

	public View getSettingPopwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout_popup_setting = layoutInflater.inflate(
				R.layout.layout_popup_setting, null);

		iv_setting_system = (ImageView) layout_popup_setting
				.findViewById(R.id.iv_setting_system);
		iv_setting_bright = (ImageView) layout_popup_setting
				.findViewById(R.id.iv_setting_bright);
		iv_setting_flyModel = (ImageView) layout_popup_setting
				.findViewById(R.id.iv_setting_flyModel);
		tv_setting_power_int = (TextView) layout_popup_setting
				.findViewById(R.id.tv_setting_power_int);
		iv_setting_power = (ImageView) layout_popup_setting
				.findViewById(R.id.iv_setting_power);
		iv_setting_wifi = (ImageView) layout_popup_setting
				.findViewById(R.id.iv_setting_wifi);
		iv_setting_sync = (ImageView) layout_popup_setting
				.findViewById(R.id.iv_setting_sync);

		iv_setting_system.setOnClickListener(new IVClickListener());
		iv_setting_bright.setOnClickListener(new IVClickListener());
		iv_setting_flyModel.setOnClickListener(new IVClickListener());
		iv_setting_power.setOnClickListener(new IVClickListener());
		iv_setting_wifi.setOnClickListener(new IVClickListener());
		iv_setting_sync.setOnClickListener(new IVClickListener());

		changeSettingImage(R.id.iv_setting_flyModel,
				SettingTools.getFlyModelStatus(context) ? SettingTools.FLAG_ON
						: SettingTools.FLAG_OFF);
		changeSettingImage(R.id.iv_setting_sync,
				SettingTools.getSyncStatus() ? SettingTools.FLAG_ON
						: SettingTools.FLAG_OFF);
		changeSettingImage(R.id.iv_setting_wifi,
				SettingTools.getWifiStatus(context) ? SettingTools.FLAG_ON
						: SettingTools.FLAG_OFF);
		changeSettingImage(R.id.iv_setting_bright,
				SettingTools.getBrightInt(context));

		return layout_popup_setting;
		// pw_Setting = new PopupWindow(layout_popup_setting,
		// ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	public void showCustomPowerPopupWindow() {
		pw_customPower = PopupWindow_CustomPowerModel.getInstance(context,
				PopupWindow_Setting.this);

		pw_customPower.setFocusable(true);
		pw_customPower.setOutsideTouchable(true);
		pw_customPower.setBackgroundDrawable(new BitmapDrawable());
		pw_customPower.setAnimationStyle(R.style.popupAnimation_slow);
		pw_customPower.showAsDropDown(view,
				SettingTools.getPopupwindow_x(context),
				SettingTools.getPopupwindow_y());
	}

	public void showConnectWifiPopupWindow(String SSID, int type) {
		pw_connectWifi = PopupWindow_ConnectWifi.getInstance(context, SSID,
				type, PopupWindow_Setting.this);

		pw_connectWifi.setFocusable(true);
		pw_connectWifi.setOutsideTouchable(true);
		pw_connectWifi.setAnimationStyle(R.style.popupAnimation_slow);
		pw_connectWifi.setBackgroundDrawable(new BitmapDrawable());
		pw_connectWifi.showAsDropDown(view,
				SettingTools.getPopupwindow_x(context) + 30,
				SettingTools.getPopupwindow_y() + 50);
	}

	public void showChoosePowerPopupwindow(String customePowerName) {
		pw_choosepower = PopupWindow_ChoosePower.getInstance(context,
				PopupWindow_Setting.this, customePowerName);
		// pw_choosepower = new PopupWindow(layout_popup_choosepower, 260,
		// ViewGroup.LayoutParams.WRAP_CONTENT);

		pw_choosepower.setFocusable(true);
		pw_choosepower.setOutsideTouchable(true);
		pw_choosepower.setBackgroundDrawable(new BitmapDrawable());
		pw_choosepower.setAnimationStyle(R.style.popupAnimation_slow);
		pw_choosepower.showAsDropDown(view,
				SettingTools.getPopupwindow_x(context),
				SettingTools.getPopupwindow_y());
	}

	// public void showChooseWifiPopupWindow() {
	// pw_choosewifi = PopupWindow_ChooseWifi.getInstance(context,
	// PopupWindow_Setting.this);
	//
	// pw_choosewifi.setFocusable(true);
	// pw_choosewifi.setOutsideTouchable(true);
	// pw_choosewifi.setBackgroundDrawable(new BitmapDrawable());
	// WindowManager windowManager = (WindowManager) context
	// .getSystemService(Context.WINDOW_SERVICE);
	// DisplayMetrics dm = new DisplayMetrics();
	// windowManager.getDefaultDisplay().getMetrics(dm);
	// int xPos = dm.widthPixels;
	// pw_choosewifi.showAsDropDown(view, xPos, 50);
	// }

	public void onConnectWifi(String SSID) {
		// pw_choosewifi.dismiss();

	}

	private class IVClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_setting_system:
				onClickSystem();
				break;
			case R.id.iv_setting_bright:
				onClickBright();
				break;
			case R.id.iv_setting_flyModel:
				onClickFlyModel();
				break;
			case R.id.iv_setting_power:
				onClickChoosePower();
				break;
			case R.id.iv_setting_wifi:
				onClickWifi();
				break;
			case R.id.iv_setting_sync:
				onClickSync();
				break;
			}
		}
	}

	private void onClickSystem() {
		Intent openIntent = new Intent();
		openIntent.setAction(Settings.ACTION_SETTINGS);
		openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(openIntent);
	}

	private void onClickBright() {
		pw_Bright = PopupWindow_Bright.getInstance(context,
				PopupWindow_Setting.this);
		pw_Bright.setFocusable(true);
		pw_Bright.setOutsideTouchable(true);
		pw_Bright.setBackgroundDrawable(new BitmapDrawable());
		pw_Bright.setAnimationStyle(R.style.popupAnimation_slow);
		pw_Bright.showAsDropDown(view,
				SettingTools.getPopupwindow_x(context) + 203,
				SettingTools.getPopupwindow_y());
	}

	private void onClickFlyModel() {
		boolean isOn = SettingTools.getFlyModelStatus(context);
		SettingTools.setFlyModelStatus(context, !isOn);
		changeSettingImage(R.id.iv_setting_flyModel,
				isOn ? SettingTools.FLAG_OFF : SettingTools.FLAG_ON);
	}

	private void onClickChoosePower() {
		CustomPowerDBHelper customPowerDBHelper = new CustomPowerDBHelper(
				context);
		String customPowerName;
		if (customPowerDBHelper.selectIsHasData()) {
			customPowerName = customPowerDBHelper
					.selectStringBySettingName(CustomPowerDBHelper.FIELD_POWERNAME);
		} else {
			SettingInt settingInt = new SettingInt();
			settingInt.autoratation = 1;
			settingInt.bright = 80;
			settingInt.flymodel = 0;
			settingInt.music = 85;
			settingInt.powername = "自定义模式";
			settingInt.ring = 70;
			settingInt.sleep = 15000;
			settingInt.sync = 1;
			settingInt.wifi = 1;
			customPowerDBHelper.insertIntoSettingInt(settingInt);
			customPowerName = settingInt.powername;
		}
		showChoosePowerPopupwindow(customPowerName);
	}

	private void onClickWifi() {
		pw_choosewifi = PopupWindow_ChooseWifi.getInstance(context,
				PopupWindow_Setting.this);
		pw_choosewifi.setFocusable(true);
		pw_choosewifi.setOutsideTouchable(true);
		pw_choosewifi.setBackgroundDrawable(new BitmapDrawable());
		pw_choosewifi.setAnimationStyle(R.style.popupAnimation_slow);
		pw_choosewifi.showAsDropDown(view,
				SettingTools.getPopupwindow_x(context),
				SettingTools.getPopupwindow_y());
		pw_choosewifi.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				PopupWindow_ChooseWifi.unregisterChooseWifiBroadcastReceiver(context);
			}
		});
	}

	private void onClickSync() {
		SettingTools.setSync(!SettingTools.getSyncStatus());
		changeSettingImage(R.id.iv_setting_sync,
				SettingTools.getSyncStatus() ? SettingTools.FLAG_ON
						: SettingTools.FLAG_OFF);
	}

	private void changeSettingImage(int ivId, int flag) {
		switch (ivId) {
		case R.id.iv_setting_bright:
			System.out.println("..................flag........" + flag);
			if (flag > 0 && flag < 65) {
				iv_setting_bright
						.setImageResource(R.drawable.setting_bright_0_selector);
			} else if (flag > 64 && flag < 255) {
				iv_setting_bright
						.setImageResource(R.drawable.setting_bright_50_selector);
			} else if (flag == 255) {
				iv_setting_bright
						.setImageResource(R.drawable.setting_bright_100_selector);
			}
			break;
		case R.id.iv_setting_flyModel:
			if (flag == SettingTools.FLAG_ON) {
				iv_setting_flyModel
						.setImageResource(R.drawable.setting_flymodel_on);
			} else {
				iv_setting_flyModel
						.setImageResource(R.drawable.setting_flymodel_off);
			}
			break;
		case R.id.iv_setting_sync:
			if (flag == SettingTools.FLAG_ON) {
				iv_setting_sync.setImageResource(R.drawable.setting_sync_on);
			} else {
				iv_setting_sync.setImageResource(R.drawable.setting_sync_off);
			}
			break;
		case R.id.iv_setting_power:
			if (flag > 0 && flag < 5) {
				iv_setting_power
						.setImageResource(R.drawable.setting_power_0_selector);
			} else if (flag > 6 && flag < 34) {
				iv_setting_power
						.setImageResource(R.drawable.setting_power_33_selector);
			} else if (flag > 33 && flag < 67) {
				iv_setting_power
						.setImageResource(R.drawable.setting_power_66_selector);
			} else {
				iv_setting_power
						.setImageResource(R.drawable.setting_power_100_selector);
			}
			break;
		case R.id.iv_setting_wifi:
			if (flag == SettingTools.FLAG_ON) {
				iv_setting_wifi.setImageResource(R.drawable.setting_wifi_on);
			} else {
				iv_setting_wifi.setImageResource(R.drawable.setting_wifi_off);
			}
			break;
		}
	}

	private class SettingBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.WIFI_STATE_CHANGED_ACTION
					.equals(intent.getAction())) {
				changeSettingImage(
						R.id.iv_setting_wifi,
						SettingTools.getWifiStatus(context) ? SettingTools.FLAG_ON
								: SettingTools.FLAG_OFF);
			} else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int powerInt = intent.getIntExtra("level", 0);
				tv_setting_power_int.setText(powerInt + "%");
				changeSettingImage(R.id.iv_setting_power, powerInt);
			} else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent
					.getAction())) {
				changeSettingImage(
						R.id.iv_setting_flyModel,
						SettingTools.getFlyModelStatus(context) ? SettingTools.FLAG_ON
								: SettingTools.FLAG_OFF);
			}
		}
	}

	// public static void unregisterSettingReceiver() {
	// if (null != settingBroadcastReceiver) {
	// context.unregisterReceiver(settingBroadcastReceiver);
	// }
	// }

	public void closePopupWindow_ChooseWifi() {
		if (null != pw_choosewifi) {
			pw_choosewifi.dismiss();
		}
	}

	public void closePopupWindow_ChoosePower() {
		if (null != pw_choosepower) {
			pw_choosepower.dismiss();
			PopupWindow_ChoosePower.unregisterchoosePowerReceiver(context);
		}
	}

	public void closePopupWindow_CustomPower() {
		if (null != pw_customPower) {
			pw_customPower.dismiss();
		}
	}

	public void closePopupWindow_ConnectWifi() {
		if (null != pw_connectWifi) {
			pw_connectWifi.dismiss();
		}
	}

	public void onChangeBright() {
		changeSettingImage(R.id.iv_setting_bright,
				SettingTools.getBrightInt(context));
		if (null != pw_Bright) {
			pw_Bright.dismiss();
		}
	}

	public static void unregisterSettingBroadcastReceiver(Context context) {
		if (null != settingBroadcastReceiver) {
			context.unregisterReceiver(settingBroadcastReceiver);
			settingBroadcastReceiver=null;
		}
	}
}
