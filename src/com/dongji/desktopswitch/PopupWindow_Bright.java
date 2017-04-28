package com.dongji.desktopswitch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.dongji.launcher.R;

public class PopupWindow_Bright {
	private Context context;
	private PopupWindow_Setting popupWindow_Setting;
	private View layout_popup_bright;

	private ImageView iv_bright_25;
	private ImageView iv_bright_50;
	private ImageView iv_bright_100;

	public PopupWindow_Bright(Context context,
			PopupWindow_Setting popupWindow_Setting) {
		this.context = context;
		this.popupWindow_Setting = popupWindow_Setting;
	}

	public static PopupWindow getInstance(Context context,
			PopupWindow_Setting popupWindow_Setting) {
		PopupWindow_Bright popupWindow_Bright = new PopupWindow_Bright(context,
				popupWindow_Setting);
		PopupWindow popupWindow = new PopupWindow(
				popupWindow_Bright.getBrightPopwindow(), SettingTools.px2dip(
						context, 59), SettingTools.px2dip(
								context, 140));
		return popupWindow;
	}

	public View getBrightPopwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout_popup_bright = layoutInflater.inflate(
				R.layout.layout_popup_bright, null);

		iv_bright_25 = (ImageView) layout_popup_bright
				.findViewById(R.id.iv_bright_0);
		iv_bright_50 = (ImageView) layout_popup_bright
				.findViewById(R.id.iv_bright_50);
		iv_bright_100 = (ImageView) layout_popup_bright
				.findViewById(R.id.iv_bright_100);

		iv_bright_25.setOnClickListener(new IVClickListener());
		iv_bright_50.setOnClickListener(new IVClickListener());
		iv_bright_100.setOnClickListener(new IVClickListener());

		return layout_popup_bright;
	}

	private class IVClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_bright_0:
				onChangeBright(64);
				break;
			case R.id.iv_bright_50:
				onChangeBright(128);
				break;
			case R.id.iv_bright_100:
				onChangeBright(255);
				break;
			}
		}
	}

	private void onChangeBright(int brightInt) {
		SettingTools.setBrightInt(context, brightInt);
		popupWindow_Setting.onChangeBright();
	}
}
