package com.dongji.desktopswitch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.launcher.R;

public class PopupWindow_ConnectWifi {
	private Context context;
	private String SSID;
	private int type;
	private PopupWindow_Setting popupWindow_Setting;
	ProgressDialog progressDialog;

	// private TextView tv;
	private EditText etWifiPwd;

	// private Button btnWifiContect;
	// private Button btnWifiCancel;

	private PopupWindow_ConnectWifi(Context context, String SSID, int type,
			PopupWindow_Setting popupWindow_Setting) {
		this.context = context;
		this.SSID = SSID;
		this.type = type;
		this.popupWindow_Setting = popupWindow_Setting;
	}

	public static PopupWindow getInstance(Context context, String SSID,
			int type, PopupWindow_Setting popupWindow_Setting) {
		PopupWindow_ConnectWifi popupWindow_ChoosePower = new PopupWindow_ConnectWifi(
				context, SSID, type, popupWindow_Setting);
		PopupWindow pw_connectwifi = new PopupWindow(
				popupWindow_ChoosePower.getConnectWifiPopupwindow(),
				SettingTools.px2dip(context, 260),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		return pw_connectwifi;
	}

	public View getConnectWifiPopupwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View layout_popup_connectwifi = layoutInflater.inflate(
				R.layout.layout_popup_connectwifi, null);

		TextView tv_wifi_SSID = (TextView) layout_popup_connectwifi
				.findViewById(R.id.tv_wifi_SSID);
		etWifiPwd = (EditText) layout_popup_connectwifi
				.findViewById(R.id.etWifiPwd);
		Button btnWifiContect = (Button) layout_popup_connectwifi
				.findViewById(R.id.btnWifiContect);
		Button btnWifiCancel = (Button) layout_popup_connectwifi
				.findViewById(R.id.btnWifiCancel);

		tv_wifi_SSID.setText(SSID);

		btnWifiContect.setOnClickListener(new BtnOnClickListener());
		btnWifiCancel.setOnClickListener(new BtnOnClickListener());

		return layout_popup_connectwifi;
	}

	public static final String ACTION_WIFI_CONNECTED = "action_wifi_connected";
	public static final String ACTION_WIFI_CONNECTED_NAME = "action_wifi_connected_name";
	public static final String CONNECTIVITY_SERVICE = null;

	private class BtnOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnWifiContect:
				String tempPassword = etWifiPwd.getText().toString().trim();
				if (tempPassword.equals("")) {
					Toast.makeText(context, "请输入密码", Toast.LENGTH_SHORT).show();
					return;
				}

				boolean isOnline = SettingTools.WifiConnect(context, SSID,
						etWifiPwd.getText().toString(), type);
//				WifiManager wifiManager = (WifiManager) context
//						.getSystemService(Context.WIFI_SERVICE);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//				
//				WifiManager wifi = (WifiManager)(context.getSystemService(Context.WIFI_SERVICE));
//				WifiInfo id=wifi.getConnectionInfo();
				WifiManager wifi = (WifiManager)(context.getSystemService(Context.WIFI_SERVICE));
			     WifiInfo wifiInfo=	wifi.getConnectionInfo();
		        String ssid=wifiInfo.getSSID();//SSID
				
				if (mWifi.isConnected()) {
					if(ssid!=null)
					{
						if(ssid.equals(SSID))
						{
							Toast.makeText(context, "已连接", Toast.LENGTH_SHORT).show();
							popupWindow_Setting.closePopupWindow_ConnectWifi();
							Intent intent = new Intent();
							intent.setAction(ACTION_WIFI_CONNECTED);
							intent.putExtra(ACTION_WIFI_CONNECTED_NAME, SSID);
							context.sendBroadcast(intent);
							
						}else {
					             Toast.makeText(context, "连接失败，请重新再试", Toast.LENGTH_SHORT).show();
							
				              }
					}
				
				}

				

				popupWindow_Setting.onConnectWifi(SSID);

				break;
			case R.id.btnWifiCancel:
				popupWindow_Setting.closePopupWindow_ConnectWifi();
				break;
			}
		}
	}

	Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				progressDialog = new ProgressDialog(context);
				progressDialog.show(context, "正在连接", "请稍候...").show();
				break;
			case 2:
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				break;
			default:
				break;
			}
		}

	};
}
