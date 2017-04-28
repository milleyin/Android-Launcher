package com.dongji.desktopswitch;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.dongji.desktopswitch.SlipSwitch.OnSwitchListener;
import com.dongji.launcher.R;

public class PopupWindow_ChooseWifi {
	public static final int REFERENSH_SCREEN = 1;

	private Context context;
	private PopupWindow_Setting popupWindow_Setting;
	private View layout_popup_choosewifi;

	// private ListView lv_choose_wifi;
	// private TextView tv_wificlose_tip;
	private ArrayList<WifiInfo> wifiInfos;
	private Adapter_lv_item_choosewifi adapter_lv_item_choosewifi;
	// public MyHandler myHandler;

	private Button btn_opensystem;

	public static ChooseWifiReceiver chooseWifiReceiver;

	private SlipSwitch slipSwitch;

	public PopupWindow_ChooseWifi(Context context,
			PopupWindow_Setting popupWindow_Setting) {
		this.context = context;
		this.popupWindow_Setting = popupWindow_Setting;

		chooseWifiReceiver = new ChooseWifiReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		context.registerReceiver(chooseWifiReceiver, intentFilter);
	}

	public static PopupWindow getInstance(Context context,
			PopupWindow_Setting popupWindow_Setting) {
		PopupWindow_ChooseWifi popupWindow_ChooseWifi = new PopupWindow_ChooseWifi(
				context, popupWindow_Setting);
		PopupWindow pw_ChooseWifi = new PopupWindow(
				popupWindow_ChooseWifi.getChooseWifiPopupwindow(),
				SettingTools.px2dip(context, 314),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		return pw_ChooseWifi;
	}

	public View getChooseWifiPopupwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout_popup_choosewifi = layoutInflater.inflate(
				R.layout.layout_popup_choosewifi, null);

		slipSwitch = (SlipSwitch) layout_popup_choosewifi
				.findViewById(R.id.slipswitch_wifi);
		ImageView iv_choose_wifi_close = (ImageView) layout_popup_choosewifi
				.findViewById(R.id.iv_choose_wifi_close);
		btn_opensystem = (Button) layout_popup_choosewifi
				.findViewById(R.id.btn_opensystem);

		btn_opensystem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});

		// lv_choose_wifi = (ListView) layout_popup_choosewifi
		// .findViewById(R.id.lv_choose_wifi);
		// tv_wificlose_tip = (TextView) layout_popup_choosewifi
		// .findViewById(R.id.tv_wificlose_tip);

		slipSwitch.setOnSwitchListener(new SwitchBtnListener());
		iv_choose_wifi_close.setOnClickListener(new IVClickListener());

		boolean switchState = SettingTools.getWifiStatus(context);
		slipSwitch.setSwitchState(switchState);
		if (switchState) {
			// lv_choose_wifi.setVisibility(View.VISIBLE);
			// tv_wificlose_tip.setVisibility(View.GONE);
			// lv_choose_wifi
			// .setOnItemClickListener(new ChoosePowerItemClickListener());
			//
			// getChooseWifiListData(lv_choose_wifi);

			// HandlerThread mHandlerThread = new HandlerThread("");
			// mHandlerThread.start();
			// myHandler = new MyHandler(mHandlerThread.getLooper());
			// myHandler.sendEmptyMessage(REFERENSH_SCREEN);
		}
		// else {
		// lv_choose_wifi.setVisibility(View.GONE);
		// tv_wificlose_tip.setVisibility(View.VISIBLE);
		// }

		// slipSwitch.setChecked(SettingTools.getWifiStatus(context));
		// slipSwitch.setOnCheckedChangeListener(new ToggleButtonListener());

		return layout_popup_choosewifi;
	}

	// private void getChooseWifiListData(ListView listView) {
	// wifiInfos = SettingTools.getWifiInfoList(context);
	//
	// adapter_lv_item_choosewifi = new Adapter_lv_item_choosewifi(context,
	// wifiInfos);
	//
	// listView.setAdapter(adapter_lv_item_choosewifi);
	//
	// }

	private class IVClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_choose_wifi_close:
				popupWindow_Setting.closePopupWindow_ChooseWifi();
				break;
			}
		}
	}

	private class SwitchBtnListener implements OnSwitchListener {

		@Override
		public void onSwitched(boolean switchState) {
			System.out.println("switch.............." + switchState);
			SettingTools.setWifi(context, switchState);
			// if (switchState) {
			// //TODO 打开开关
			// // lv_choose_wifi.setVisibility(View.VISIBLE);
			// // tv_wificlose_tip.setVisibility(View.GONE);
			// } else {
			// //TODO 关闭开关
			// // lv_choose_wifi.setVisibility(View.GONE);
			// // tv_wificlose_tip.setVisibility(View.VISIBLE);
			// }
		}
	}

	// private class ChoosePowerItemClickListener implements OnItemClickListener
	// {
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// TextView tv_wifi_name = (TextView) arg1
	// .findViewById(R.id.tv_wifi_name);
	// TextView tv_wifi_msg = (TextView) arg1
	// .findViewById(R.id.tv_wifi_msg);
	// ImageView iv_wifi_lock = (ImageView) arg1
	// .findViewById(R.id.iv_wifi_lock);
	//
	// if (tv_wifi_msg.getText().toString().trim()
	// .equals("通过WPA/WPA2进行保护")) {
	// popupWindow_Setting.showConnectWifiPopupWindow(tv_wifi_name
	// .getText().toString(), SettingTools.WIFICIPHER_WPA);
	// } else if (tv_wifi_msg.getText().toString().trim()
	// .equals("通过WPA进行保护")) {
	// popupWindow_Setting.showConnectWifiPopupWindow(tv_wifi_name
	// .getText().toString(), SettingTools.WIFICIPHER_WPA2);
	// } else if (tv_wifi_msg.getText().toString().trim()
	// .equals("通过WPA2进行保护")) {
	// popupWindow_Setting.showConnectWifiPopupWindow(tv_wifi_name
	// .getText().toString(), SettingTools.WIFICIPHER_WEP);
	// } else {
	// SettingTools.WifiConnect(context, tv_wifi_name.getText()
	// .toString(), "", SettingTools.WIFICIPHER_NOPASS);
	// }
	//
	// // if (iv_wifi_lock.getVisibility() == View.VISIBLE
	// // && !tv_wifi_msg.getText().toString().trim().equals("已连接")) {
	// // popupWindow_Setting.showConnectWifiPopupWindow(tv_wifi_name
	// // .getText().toString(), SettingTools.WIFICIPHER_WPA);
	// // System.out.println(AndroidUtils.px2sp(context, 14)
	// // + "...................."
	// // + AndroidUtils.px2dip(context, 12));
	// // } else {
	// // SettingTools.WifiConnect(context, tv_wifi_name.getText()
	// // .toString(), "", SettingTools.WIFICIPHER_NOPASS);
	// // }
	// }
	// }

	// private class MyHandler extends Handler {
	// public MyHandler(Looper looper) {
	// super(looper);
	// }
	//
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case REFERENSH_SCREEN:
	// for (int i = 0; i < wifiInfos.size(); i++) {
	// if (wifiInfos.get(i).wifiname.equals(SettingTools
	// .getConnectedWifi(context))) {
	// wifiInfos.get(i).wifimsg = "已连接";
	// }
	// }
	// adapter_lv_item_choosewifi.updateData(wifiInfos);
	// adapter_lv_item_choosewifi.notifyDataSetChanged();
	// // myHandler.sendEmptyMessageDelayed(REFERENSH_SCREEN, 1000);
	// break;
	// }
	// // sendEmptyMessageDelayed(REFERENSH_SCREEN, 1000);
	// }
	// }

	private class ChooseWifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.WIFI_STATE_CHANGED_ACTION
					.equals(intent.getAction())) {
				boolean switchState = SettingTools.getWifiStatus(context);
				slipSwitch.setSwitchState(switchState);
			}
		}
	}
	
	public static void unregisterChooseWifiBroadcastReceiver(Context context) {
		if (null != chooseWifiReceiver) {
			context.unregisterReceiver(chooseWifiReceiver);
			chooseWifiReceiver=null;
		}
	}
}
