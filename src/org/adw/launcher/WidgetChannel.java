package org.adw.launcher;

import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.AllDrawer.OnPackageListener;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.dongji.adapter.WidgetChannelAdapter;
import com.dongji.desktopswitch.PopupWindow_Quick;
import com.dongji.desktopswitch.SettingTools;
import com.dongji.launcher.R;
import com.dongji.tool.AndroidUtils;
import com.dongji.ui.ScrollLayoutTouchable;

public class WidgetChannel implements OnPackageListener {

	private View mSuperView;
	private Launcher mLauncher;
	private View mWidgetLayout;

	private AppWidgetProviderInfo providerInfo;
	private AppWidgetManager mAppWidgetManager;
	private PopupWindow mMenuPopup;

	private ScrollLayoutTouchable scroller_content;
	private WidgetChannelAdapter adapter;

	public WidgetChannel(View mSuperView, Launcher mLauncher) {
		this.mSuperView = mSuperView;
		this.mLauncher = mLauncher;
	}

	public View initViews() {
		mWidgetLayout = LayoutInflater.from(mLauncher).inflate(
				R.layout.layout_all_widget, null);
		scroller_content = (ScrollLayoutTouchable) mWidgetLayout
				.findViewById(R.id.ln_content);
		initLayoutStyle();
		return mWidgetLayout;
	}

	private List<AppWidgetProviderInfo> getAllWidgetInfos() {
		mAppWidgetManager = AppWidgetManager.getInstance(mLauncher);
		List<AppWidgetProviderInfo> list = mAppWidgetManager
				.getInstalledProviders();
		
		AppWidgetProviderInfo calendar = new AppWidgetProviderInfo();
		calendar.label = "动机提醒日历";
		list.add(0, calendar);
		return list;
	}

	public void initLayoutStyle() {
		if (mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			initLayout(8);
		} else if (mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			initLayout(9);
		}
	}

	private void initLayout(int count) {
		scroller_content.removeAllViews();
		List<AppWidgetProviderInfo> allAppWidgetInfo = getAllWidgetInfos();

		ArrayList<AppWidgetProviderInfo> widgets = new ArrayList<AppWidgetProviderInfo>();

		for (int i = 0; i < allAppWidgetInfo.size(); i++) {
			widgets.add(allAppWidgetInfo.get(i));

			if ((i + 1) % count == 0) {
				autoAdaptToScreen(count, widgets);

				widgets = new ArrayList<AppWidgetProviderInfo>();
			} else if (i == allAppWidgetInfo.size() - 1) {
				autoAdaptToScreen(count, widgets);
			}
		}
	}

	private void autoAdaptToScreen(int count,
			ArrayList<AppWidgetProviderInfo> widgets) {
		GridView grid = (GridView) LayoutInflater.from(mLauncher).inflate(
				R.layout.widget_gridview_layout, null);

		DisplayMetrics metrics = AndroidUtils.getScreenSize(mLauncher);
		if (count == 8) {// 横屏
			grid.setPadding(AndroidUtils.dip2px(mLauncher, 20),
					AndroidUtils.dip2px(mLauncher, 50),
					AndroidUtils.dip2px(mLauncher, 20),
					AndroidUtils.dip2px(mLauncher, 50));
			int horizontalSpacing = (metrics.widthPixels
					- grid.getPaddingLeft() - grid.getPaddingRight() - AndroidUtils
					.dip2px(mLauncher, 240) * 4) / 3;
			int verticalSpacing = (metrics.heightPixels - grid.getPaddingTop()
					- grid.getPaddingBottom() - AndroidUtils.dip2px(mLauncher,
					280) * 2);
			grid.setHorizontalSpacing(horizontalSpacing);
			grid.setVerticalSpacing(verticalSpacing);
		} else if (count == 9) {// 竖屏
			grid.setPadding(AndroidUtils.dip2px(mLauncher, 20),
					AndroidUtils.dip2px(mLauncher, 40),
					AndroidUtils.dip2px(mLauncher, 20),
					AndroidUtils.dip2px(mLauncher, 40));
			int horizontalSpacing = (metrics.widthPixels
					- grid.getPaddingLeft() - grid.getPaddingRight() - AndroidUtils
					.dip2px(mLauncher, 240) * 3) / 2;
			int verticalSpacing = (metrics.heightPixels - grid.getPaddingTop()
					- grid.getPaddingBottom() - AndroidUtils.dip2px(mLauncher,
					270) * 3);
			grid.setHorizontalSpacing(horizontalSpacing);
			grid.setVerticalSpacing(verticalSpacing);
		}
		adapter = new WidgetChannelAdapter(mLauncher, widgets);
		grid.setAdapter(adapter);
		grid.setOnItemLongClickListener(new ItemLongClickListener(widgets));
		scroller_content.addView(grid);
	}

	private void initPopupWindow() {
		View mLayout = LayoutInflater.from(mLauncher).inflate(
				R.layout.widget_popup_menu, null);
		mLayout.setFocusableInTouchMode(true);
		View mAddToDesktop = mLayout.findViewById(R.id.add_to_desktop);
		View mUninstall = mLayout.findViewById(R.id.uninstall);
		View mDetailInfo = mLayout.findViewById(R.id.detail_info);

		// int popupHeight = AndroidUtils.dip2px(this, 220);
		mMenuPopup = new PopupWindow(mLayout, AndroidUtils.dip2px(mLauncher,
				220), AndroidUtils.dip2px(mLauncher, 60), true);
		// mMenuPopup.setAnimationStyle(R.style.popupWindow_style);
		mMenuPopup.setBackgroundDrawable(new ColorDrawable(Color.argb(8, 168,
				168, 168)));
		mMenuPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		mMenuPopup.setOutsideTouchable(true);
		mMenuPopup.setTouchable(true);

		mAddToDesktop.setOnClickListener(menuListener);
		mUninstall.setOnClickListener(menuListener);
		mDetailInfo.setOnClickListener(menuListener);
	}

	private void showOrDismissPopupWindow(View view) {
		if (mMenuPopup == null) {
			initPopupWindow();
		}
		if (this != null && !mLauncher.isFinishing()) {
			if (mMenuPopup.isShowing()) {
				mMenuPopup.dismiss();
			}
			mMenuPopup.showAsDropDown(view);
		}
	}

	OnClickListener menuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.add_to_desktop:
				// selectWidgets();
				((AllDrawer) mSuperView).showAddDialog(providerInfo, v);
				mMenuPopup.dismiss();
				break;
			case R.id.uninstall:
				AndroidUtils.uninstallApp(mLauncher,
						providerInfo.provider.getPackageName());
				mMenuPopup.dismiss();
				break;
			case R.id.detail_info:
				AndroidUtils.showInstalledAppDetails(mLauncher,
						providerInfo.provider.getPackageName());
				mMenuPopup.dismiss();
				break;
			default:
				break;
			}
		}
	};

	private class ItemLongClickListener implements OnItemLongClickListener {
		List<AppWidgetProviderInfo> list;

		public ItemLongClickListener(List<AppWidgetProviderInfo> list) {
			super();
			this.list = list;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			providerInfo = list.get(position);
			showOrDismissPopupWindow(view);
			return false;
		}

	}

	@Override
	public void onPackageAdded(String packageName, boolean replacing) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPackageRemoved(String packageName, boolean replacing) {
		if (!replacing) {
			if (adapter != null) {
				adapter.removeAppDataByPackageName(providerInfo.provider
						.getPackageName());
			}
		}
	}
}
