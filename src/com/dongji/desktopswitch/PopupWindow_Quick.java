package com.dongji.desktopswitch;

import org.adw.launcher.AllDrawer;
import org.adw.launcher.ApplicationInfo;
import org.adw.launcher.View_CloudApp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.dongji.launcher.R;

public class PopupWindow_Quick {
	private Context context;
	private String packageName;
	private LinearLayout llcloudapp;
	private String appName;
	private Drawable icon;
	private View mSuperView;
	private View layout_popup_quick;

	private ImageView iv_app_quick_add;
	private ImageView iv_app_quick_detail;
	private ImageView iv_app_quick_delete;

	public PopupWindow_Quick(Context context, String packageName,
			LinearLayout llcloudapp, String appName, Drawable icon,
			View mSuperView) {
		this.context = context;
		this.packageName = packageName;
		this.llcloudapp = llcloudapp;
		this.appName = appName;
		this.icon = icon;
		this.mSuperView = mSuperView;
	}

	public static PopupWindow getInstance(Context context, String packageName,
			LinearLayout llcloudapp, String appName, Drawable icon,
			View mSuperView) {
		PopupWindow_Quick popupWindow_Quick = new PopupWindow_Quick(context,
				packageName, llcloudapp, appName, icon, mSuperView);
		PopupWindow popupWindow = new PopupWindow(
				popupWindow_Quick.getBrightPopwindow(),
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		return popupWindow;
	}

	public View getBrightPopwindow() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout_popup_quick = layoutInflater.inflate(
				R.layout.layout_popup_quick, null);

		iv_app_quick_add = (ImageView) layout_popup_quick
				.findViewById(R.id.iv_app_quick_add);
		iv_app_quick_detail = (ImageView) layout_popup_quick
				.findViewById(R.id.iv_app_quick_detail);
		iv_app_quick_delete = (ImageView) layout_popup_quick
				.findViewById(R.id.iv_app_quick_delete);

		iv_app_quick_add.setOnClickListener(new IVClickListener());
		iv_app_quick_detail.setOnClickListener(new IVClickListener());
		iv_app_quick_delete.setOnClickListener(new IVClickListener());

		return layout_popup_quick;
	}

	private class IVClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_app_quick_add:
				
				ApplicationInfo applicationInfo = new ApplicationInfo();
				PackageManager packageManager = context.getPackageManager();
				Intent intent = new Intent();
				intent = packageManager.getLaunchIntentForPackage(packageName);
				applicationInfo.intent = intent;
				applicationInfo.title = appName;
				applicationInfo.icon = icon;
				((AllDrawer) mSuperView).showAddDialog(applicationInfo,
						llcloudapp);
				
				break;
			case R.id.iv_app_quick_detail:
				SettingTools.showInstalledAppDetails(context, packageName);
				break;
			case R.id.iv_app_quick_delete:
				// SettingTools.uninstallApp(context, packageName);
				SettingTools.uninstallApp(context, packageName);
				break;
			}
			if (View_CloudApp.popupWindow_quick != null) {
				View_CloudApp.popupWindow_quick.dismiss();
				View_CloudApp.popupWindow_quick = null;
			}

		}
	}
}
