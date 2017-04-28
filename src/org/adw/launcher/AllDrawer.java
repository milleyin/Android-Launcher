package org.adw.launcher;

import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.CellLayout.CellInfo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.dongji.adapter.AllAppAdapter;
import com.dongji.adapter.MyGridViewAdapterMuilt;
import com.dongji.launcher.R;
import com.dongji.sqlite.DrawerDatabase;
import com.dongji.tool.AndroidUtils;
import com.dongji.ui.ScrollLayoutTouchable;

public class AllDrawer extends LinearLayout implements OnClickListener {
	private LinearLayout mContentLayout;
	private Context context;
	private LinearLayout.LayoutParams mParams;
	private View mAppAppView, mWidgetView, mTaskMngrView, mCloudAppView;
	private AppChannel mAppChannel;
	private WidgetChannel mWidgetChannel;
	private TaskMngrChannel mTaskMngrChannel;
	private View_CloudApp mCloudApp;
	private PopupWindow mSortPopupwindow;
	private PopupWindow mDrawerSettingPopupWindow;
	private View mTopView;
	private Button mSortButton;
	private Button mSettingButton;
	private View mSortLayout;
	private View mSettingLayout;
	private Button mWidgetButton;

	private Launcher mLauncher;
	private DrawerWorkspace drawerWorkspace;
	private List<OnPackageListener> listeners = new ArrayList<OnPackageListener>();

	// 批量添加
	int step; // 步骤 0:选择屏幕 1:选择应用
	PopupWindow batch_add_app_dialog;
	WorkspaceMiniBatch workspaceBath;
	ScrollLayoutTouchable ln_content;
	List<MyGridViewAdapterMuilt> myGridAdapters = new ArrayList<MyGridViewAdapterMuilt>();

	TextView tv_batch_title;
	TextView tv_batch_tip;
	int total; // 一个屏幕的空间总数
	int num; // 已经被使用的空间数
	Button btn_batch_cancle;
	Button btn_batch_ok;

	PopupWindow add_dialog;
	// Dialog dialog_add; // 添加 应用 或 widget
	WorkspaceMini add_workspaceMini;
	Button btn_add_cancle;
	Button btn_add_ok;

	PopupWindow app_popup;
	PopupWindow floder_popup;

	Object tag;
	View tv;

	ApplicationInfo appInfo = null;
	UserFolderInfo userFolderInfo = null;
	Dialog dialog = null;
	CheckBox checkDelete;
	Button submit;
	Button cancel;
	EditText rename_et;
	TextView title;
	EditText pwd;

	View f_v = null;

	private RadioButton mAppRadioButton, mCloudAppRadioButton, mTaskManagerRadioButton;

	float mlastX1,mlastX2,mlastX3;
	float mlastY1,mlastY2,mlastY3;
	
	public AllDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initLayoutParams();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		System.out.println("onClick");
		switch (v.getId()) {
		case R.id.appbutton:
			if (mAppAppView == null) {
				mAppChannel = new AppChannel(mLauncher, this);
				listeners.add(mAppChannel);
				mAppAppView = mAppChannel.initViews();
				mLauncher.startLoaders(3);
			}
			setVisibleTopButton(View.VISIBLE);
			addNewView(mAppAppView);
			break;
		case R.id.widgetbutton:
			if (mWidgetView == null) {
				mWidgetChannel = new WidgetChannel(this, mLauncher);
				listeners.add(mWidgetChannel);
				mWidgetView = mWidgetChannel.initViews();
			}
			addNewView(mWidgetView);
			setVisibleTopButton(View.GONE);
			break;
		case R.id.cloudappbutton:
			if (mCloudAppView == null) {
				mCloudApp = new View_CloudApp(mLauncher, this);
				listeners.add(mCloudApp);
				mCloudAppView = mCloudApp.initViews();
			}
			addNewView(mCloudAppView);
			setVisibleTopButton(View.GONE);
			break;
		case R.id.taskmanagerbutton:
			if (mTaskMngrView == null) {
				mTaskMngrChannel = new TaskMngrChannel(this, mLauncher);
				listeners.add(mTaskMngrChannel);
				mTaskMngrView = mTaskMngrChannel.initViews();
			}
			addNewView(mTaskMngrView);
			setVisibleTopButton(View.GONE);
			break;
		case R.id.sortlayout:
		case R.id.sortbutton:
			showSortPopupwindow();
			break;
		case R.id.settinglayout:
		case R.id.settingbutton:
			showDrawerSettingPopupwindow();
			break;
		case R.id.sort_letter_textview:
			mAppChannel.sortByType(0);
			dismissPopupwindow(mSortPopupwindow);
			break;
		case R.id.sort_time_textview:
			mAppChannel.sortByType(1);
			dismissPopupwindow(mSortPopupwindow);
			break;
		case R.id.sort_usenum_textview:
			mAppChannel.sortByType(2);
			dismissPopupwindow(mSortPopupwindow);
			break;
		case R.id.batch_add_textview: // 批量添加
			dismissPopupwindow(mDrawerSettingPopupWindow);
			showBatchAddAppDialog();
			break;

		case R.id.new_folder_textview:
			mAppChannel.showNewFolder(mSettingButton);
			dismissPopupwindow(mDrawerSettingPopupWindow);
			break;
		case R.id.hide_app_textview:
			mAppChannel.showHideAppPopupWindow(mSettingButton);
			dismissPopupwindow(mDrawerSettingPopupWindow);
			break;

		case R.id.btn_batch_cancle:
			batch_add_app_dialog.dismiss();
			break;

		case R.id.btn_batch_ok:

			if (step == 0) {
				if (num == total) {
					Toast.makeText(context, "屏幕已满", Toast.LENGTH_SHORT).show();
				} else {
					pickApps();
					tv_batch_title.setText("批量添加--请选择应用");
					tv_batch_tip.setVisibility(View.VISIBLE);
					step = 1;
					btn_batch_ok.setText("确定");
				}
			} else {
				addApps();
			}

			break;

		case R.id.btn_add_cancle:
			if (add_dialog != null) {
				add_dialog.dismiss();
				add_workspaceMini.cancleAdded();
			}
			break;

		case R.id.btn_add_ok:
			
			if (add_dialog != null) {
				add_dialog.dismiss();
			}
			break;

		case R.id.add_to:
			showAddDialog(tag, tv);
			app_popup.dismiss();
			break;

		case R.id.uninstall:

			String UninstallPkg = null;
			try {
				ApplicationInfo appInfo = (ApplicationInfo) tag;
				if (appInfo.iconResource != null)
					UninstallPkg = appInfo.iconResource.packageName;
				else {
					PackageManager mgr = context.getPackageManager();
					ResolveInfo res = mgr.resolveActivity(appInfo.intent, 0);
					UninstallPkg = res.activityInfo.packageName;
				}

				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
						Uri.parse("package:" + UninstallPkg));
				context.startActivity(uninstallIntent);

				mLauncher.overridePendingTransition(R.anim.enteralpha,
						R.anim.exitalpha);

			} catch (Exception e) {
				e.printStackTrace();
			}
			app_popup.dismiss();
			break;

		case R.id.app_info:
			try {
				ApplicationInfo appInfo = (ApplicationInfo) tag;
				PackageManager mgr = context.getPackageManager();
				ResolveInfo res = mgr.resolveActivity(appInfo.intent, 0);

				mLauncher.showInstalledAppDetails(context,
						res.activityInfo.packageName);
				mLauncher.overridePendingTransition(R.anim.enteralpha,
						R.anim.exitalpha);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			app_popup.dismiss();
			break;

		case R.id.folderdelete:

			if (mLauncher.myDatabaseUtil.isDeciphering(String
					.valueOf(userFolderInfo.id))) { // 解密

				openFolderPwdValidateDialog("deletefolder");

			} else {

				deleteFolder();

			}
			closeFolderPoPup();
			break;

		case R.id.deciphering:

			mLauncher.decipheringDialog(v);
			closeFolderPoPup();

			break;

		case R.id.encryption:
			mLauncher.encryption(v);
			closeFolderPoPup();
			break;

		case R.id.addapp:

			CellLayout layout = (CellLayout) mAppChannel.mWorkspace
					.getChildAt(mAppChannel.mWorkspace.getCurrentScreen());

			if (mLauncher.myDatabaseUtil.isDeciphering(String
					.valueOf(userFolderInfo.id))) { // 解密

				openFolderPwdValidateDialog("addApp");

			} else {

				addAppToFolder(userFolderInfo);

			}

			layout.requestLayout();
			layout.invalidate();
			closeFolderPoPup();
			break;

		case R.id.folderrename:

			if (mLauncher.myDatabaseUtil.isDeciphering(String
					.valueOf(userFolderInfo.id))) {
				openFolderPwdValidateDialog("rename");

			} else {

				renameDialog();
			}

			closeFolderPoPup();

			break;
			
		case R.id.addwidget:
			// new
			/*int appWidgetId = mLauncher.mAppWidgetHost.allocateAppWidgetId();

			Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
			pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
			ArrayList<Bundle> customExtras = new ArrayList<Bundle>();

			// add Remind calendar widget
			AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
			info1.provider = new ComponentName(mLauncher.getPackageName(), "XXX.YYY");
			info1.label = "动机提醒日历";
			info1.icon = R.drawable.remind_icon;
			customInfo.add(info1);

			Bundle b1 = new Bundle();
			b1.putString(Launcher.EXTRA_CUSTOM_WIDGET, Launcher.REMIND_CALENDAR_WIDGET);
			customExtras.add(b1);

			pickIntent.putParcelableArrayListExtra(
					AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
			pickIntent.putParcelableArrayListExtra(
					AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);

			// start the pick activity
			mLauncher.startActivityForResult(pickIntent, Launcher.REQUEST_PICK_APPWIDGET_3);*/
			
			mLauncher.addCustomWidgetData(Launcher.REQUEST_PICK_APPWIDGET_3);
			break;
		}
	}

	public void openFolderPwdValidateDialog(final String type) {

		dialog = new Dialog(context, R.style.theme_myDialog_activity);
		dialog.setContentView(R.layout.openfolderpwd);
		dialog.getWindow().setLayout(500, 300);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();

		// title = (TextView) dialog.findViewById(R.id.title);
		// title.setText("");

		pwd = (EditText) dialog.findViewById(R.id.pwd);
		submit = (Button) dialog.findViewById(R.id.submit);
		cancel = (Button) dialog.findViewById(R.id.cancel);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (!pwd.getText().toString().equals("")) {

					String id = String.valueOf(userFolderInfo.id);

					int count = mLauncher.myDatabaseUtil.queryEntryption(id,
							pwd.getText().toString());

					if (count > 0) {

						dialog.cancel();

						if (type.equals("openFolder")) {

							// handleFolderClick(folderInfo);

						} else if (type.equals("addApp")) {

							addAppToFolder(userFolderInfo);

						} else if (type.equals("rename")) {

							renameDialog();

						} else if (type.equals("deletefolder")) {

							deleteFolder();
						}

					} else {

						Toast.makeText(context, "密码错误！", Toast.LENGTH_SHORT)
								.show();
					}

				} else {
					Toast.makeText(context, "密码不能为空！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.cancel();

			}
		});

	}

	void deleteFolder() {
		dialog = new Dialog(context, R.style.theme_myDialog_activity);
		dialog.setContentView(R.layout.deletefolderdialog);
		dialog.getWindow().setLayout(500, 300);
		dialog.show();

		checkDelete = (CheckBox) dialog.findViewById(R.id.check_delete);
		submit = (Button) dialog.findViewById(R.id.submit);
		cancel = (Button) dialog.findViewById(R.id.cancel);

		submit.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("static-access")
			@Override
			public void onClick(View v) {

				View view = mAppChannel.mWorkspace
						.getViewForTag(userFolderInfo);

				CellLayout layout = (CellLayout) mAppChannel.mWorkspace
						.getChildAt(mAppChannel.mWorkspace.getCurrentScreen());

				if (!checkDelete.isChecked()) {

					ArrayList<ApplicationInfo> contents = userFolderInfo.contents;
					int contentsCount = contents.size();
					int screen = mAppChannel.mWorkspace.getCurrentScreen();
					int user = layout.countSpace();

					if (contentsCount > 0) {

						for (int k = 0; k < contentsCount; k++) {

							ApplicationInfo info = contents.get(k);

							// 平铺应用

						}
					}

				}

				mLauncher.getModel().deleteUserFolderContentsFromDatabase(
						context, userFolderInfo);

				layout.removeViewInLayout(view);
				layout.requestLayout();
				layout.invalidate();

				dialog.cancel();

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
	}

	void renameDialog() {

		dialog = new Dialog(context, R.style.theme_myDialog_activity);
		dialog.setContentView(R.layout.renamedialog);
		dialog.getWindow().setLayout(500, 300);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();

		title = (TextView) dialog.findViewById(R.id.title);
		rename_et = (EditText) dialog.findViewById(R.id.rename);
		submit = (Button) dialog.findViewById(R.id.submit);
		cancel = (Button) dialog.findViewById(R.id.cancel);

		title.setText("编辑文件夹名称");

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!rename_et.getText().toString().equals("")) {

					Intent mReturnData = new Intent();
					mReturnData.putExtra(Intent.EXTRA_SHORTCUT_NAME, rename_et
							.getText().toString());
					mReturnData.putExtra(mLauncher.EXTRA_APPLICATIONINFO,
							userFolderInfo.id);
					mLauncher.setResult(-1, mReturnData);

					View view = mAppChannel.mWorkspace
							.getViewForTag(userFolderInfo);
					userFolderInfo.title = rename_et.getText().toString();
					((BubbleTextView) view).setText(userFolderInfo.title);
					mLauncher.getModel().updateItemInDatabase(context,
							userFolderInfo);

					dialog.cancel();
				} else {
					Toast.makeText(context, "名称不能为空！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.cancel();

			}
		});

	}

	void addAppToFolder(final UserFolderInfo userFolderInfo) {

		dialog = new Dialog(context, R.style.theme_myDialog_activity);
		dialog.setContentView(R.layout.allappgrid);
		dialog.getWindow().setLayout(650, 300);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();

		submit = (Button) dialog.findViewById(R.id.submit);
		cancel = (Button) dialog.findViewById(R.id.cancel);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.cancel();

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.cancel();

			}
		});

		PackageManager pm = context.getPackageManager();

		List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
		for (int i = 0; i < mLauncher.sModel.mApplications.size(); i++) {
			apps.add(mLauncher.sModel.mApplications.get(i));
		}

		final ArrayList<ApplicationInfo> contents = userFolderInfo.contents;

		AllAppAdapter allAppAdapter = new AllAppAdapter(context, apps, pm,
				contents);
		mLauncher.appGv = (GridView) dialog.findViewById(R.id.gridview);
		mLauncher.appGv.setAdapter(allAppAdapter);
		mLauncher.appGv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				CheckBox cb = (CheckBox) view.findViewById(R.id.cb);
				ImageView appImage = (ImageView) view.findViewById(R.id.appimg);
				ApplicationInfo appInfo = (ApplicationInfo) appImage.getTag();

				if (cb.isChecked()) {
					cb.setChecked(false);
				} else {
					cb.setChecked(true);
				}

				if (cb.isChecked()) {
					userFolderInfo.add(appInfo);
					mLauncher.getModel().addOrMoveItemInDatabase(context,
							appInfo, userFolderInfo.id,
							mAppChannel.mWorkspace.getCurrentScreen(),
							userFolderInfo.cellX, userFolderInfo.cellY);
				} else {
					mLauncher.getModel().removeUserFolderItem(userFolderInfo,
							appInfo);
					mLauncher.getModel().deleteItemFromDatabase(context,
							appInfo);
				}

				View v = mAppChannel.mWorkspace.getViewForTag(userFolderInfo);

				FolderIcon icon = (FolderIcon) v;
				mLauncher.mCloseIcon = new BitmapDrawable(icon
						.creaetCloseIcon(userFolderInfo));
				((BubbleTextView) v).setCompoundDrawablesWithIntrinsicBounds(
						null, mLauncher.mCloseIcon, null, null);
			}
		});

	}

	private void addApps() {

		ArrayList<ApplicationInfo> selectedAppInfo = new ArrayList<ApplicationInfo>();

		for (MyGridViewAdapterMuilt a : myGridAdapters) {
			int[] selected = a.getSelectedItemIndexes();
			for (int i = 0; i < selected.length; i++) {
				selectedAppInfo.add((ApplicationInfo) a.getItem(selected[i]));
				System.out.println(" 应用:  --->"
						+ ((ApplicationInfo) a.getItem(selected[i])).title);
			}
		}

		if (selectedAppInfo.size() == 0) {
			Toast.makeText(context, "请选择应用", Toast.LENGTH_SHORT).show();
		} else {

			ProgressDialog progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("正在添加");
			progressDialog.show();

			final int screen = (Integer) workspaceBath
					.getSelected_mini_screen().getTag();

			CellLayout cell = (CellLayout) workspaceBath
					.getSelected_mini_screen().getChildAt(0);

			for (int i = 0; i < selectedAppInfo.size(); i++) {
				ApplicationInfo appInfo = selectedAppInfo.get(i);

				Point p = cell.vaild_points.get(i);
				int[] mCellCoordinates = new int[2];
				cell.pointToCellExact(p.x, p.y, mCellCoordinates);

				final ApplicationInfo info = new ApplicationInfo();

				if (appInfo.icon instanceof FastBitmapDrawable) {
					info.icon = new BitmapDrawable(
							((FastBitmapDrawable) appInfo.icon).getBitmap());
				} else {
					info.icon = new BitmapDrawable(
							((BitmapDrawable) appInfo.icon).getBitmap());
				}

				info.filtered = true;
				info.title = new String(appInfo.title.toString());
				info.intent = new Intent(appInfo.intent);
				info.customIcon = true;
				info.iconResource = null;
				info.screen = screen;
				info.cellX = mCellCoordinates[0];
				info.cellY = mCellCoordinates[1];
				info.spanX = 1;
				info.spanY = 1;

				mLauncher.batchAddShortcut(info, screen, mCellCoordinates[0],
						mCellCoordinates[1]);
			}

			progressDialog.dismiss();
			batch_add_app_dialog.dismiss();
		}
	}

	private void showBatchAddAppDialog() {

		workspaceBath.setVisibility(View.VISIBLE);
		ln_content.setVisibility(View.GONE);
		step = 0;
		btn_batch_ok.setText("下一步");
		ln_content.removeAllViews();
		workspaceBath.removeAllViews();

		int size = mLauncher.mWorkspace.getChildCount();

		mLauncher.mMiniLauncher.setDrawingCacheEnabled(true);
		mLauncher.mMiniLauncher.destroyDrawingCache();
		mLauncher.mMiniLauncher.buildDrawingCache();
		Bitmap mini_bitmap = mLauncher.mMiniLauncher.getDrawingCache();

		workspaceBath.init(size, 0, mini_bitmap);

		final WorkspaceMiniBatch workspace = workspaceBath;
		final boolean desktopLocked = false;

		int s = mLauncher.mWorkspace.getChildCount();

		int unit_width = getResources().getDimensionPixelSize(
				R.dimen.mini_workspace_cell_item_width);
		int unit_height = getResources().getDimensionPixelSize(
				R.dimen.mini_workspace_cell_item_height);

		for (int j = 0; j < s; j++) {
			CellLayout c = (CellLayout) mLauncher.mWorkspace.getChildAt(j);

			System.out.println(" 第 " + j + " 屏   共有  : --->"
					+ c.getChildCount() + " 个元素");
			for (int k = 0; k < c.getChildCount(); k++) {
				View v = c.getChildAt(k);
				v.invalidate();
				// v.setBackgroundDrawable(getResources().getDrawable(
				// R.drawable.no_bg));
				v.setDrawingCacheEnabled(true);
				v.destroyDrawingCache();
				v.buildDrawingCache();

				Bitmap b = v.getDrawingCache();

				Object o = v.getTag();
				ItemInfo itemInfo = (ItemInfo) o;

				// int src_width = b.getWidth();
				// int src_height = b.getHeight();
				//
				// int target_width = itemInfo.spanX * (int) unit_width;
				// int target_height = itemInfo.spanY * (int) unit_height;

				if (b == null) {
					// int c_width = c.mCellWidth;
					// int c_height = c.mCellHeight;
					// final View view =
					// mLauncher.createShortcut(R.layout.application,(ApplicationInfo)itemInfo);

					// ImageView img = new ImageView(context);
					// img.setPadding(5, 5, 5, 5);
					// img.setBackgroundDrawable(new BitmapDrawable(b));
					// view.setTag(v);
					// view.setLongClickable(true);
					// view.setOnLongClickListener(workspace); // 长按监听

					ApplicationInfo apInfo = (ApplicationInfo) itemInfo;
					LinearLayout c_item = (LinearLayout) LayoutInflater.from(
							context).inflate(R.layout.c_item, null);
					ImageView img = (ImageView) c_item.findViewById(R.id.img);
					TextView tv = (TextView) c_item.findViewById(R.id.tv);
					img.setImageDrawable(apInfo.icon);
					tv.setText(apInfo.title);

					workspace.addInScreen(c_item, itemInfo.screen,
							itemInfo.cellX, itemInfo.cellY, itemInfo.spanX,
							itemInfo.spanY, !desktopLocked);
				} else {

					ImageView img = new ImageView(context);
					img.setPadding(5, 5, 5, 5);
					img.setBackgroundDrawable(new BitmapDrawable(b));
					img.setTag(v);
					// img.setLongClickable(true);
					// img.setOnLongClickListener(workspace); // 长按监听

					workspace.addInScreen(img, itemInfo.screen, itemInfo.cellX,
							itemInfo.cellY, itemInfo.spanX, itemInfo.spanY,
							!desktopLocked);
				}
				// ImageView img = new ImageView(context);
				// img.setPadding(5, 5, 5, 5);
				// img.setBackgroundDrawable(new BitmapDrawable(b));
				// img.setTag(v);
				// img.setLongClickable(true);
				//
				// workspace.addInScreen(img, itemInfo.screen, itemInfo.cellX,
				// itemInfo.cellY, itemInfo.spanX, itemInfo.spanY,
				// !desktopLocked);
			}
		}
		batch_add_app_dialog.showAtLocation(mSettingButton, Gravity.CENTER, 0,
				0);

		// 计算并显示第一个屏幕的剩余空间
		post(new Runnable() {
			@Override
			public void run() {
				final LinearLayout ln = (LinearLayout) workspaceBath
						.getChildAt(0);
				LinearLayout fra = (LinearLayout) ln.getChildAt(0);
				LinearLayout lnn = (LinearLayout) fra.getChildAt(0);
				CellLayout cellLayout = (CellLayout) lnn.getChildAt(0);

				System.out.println("cellLayout.countEmptySpace() ---> "
						+ cellLayout.countSpace());
				updataNum(cellLayout.countSpace(), cellLayout.getTotal());

			}
		});

		tv_batch_tip.setVisibility(View.GONE);
	}

	void pickApps() {
		workspaceBath.setVisibility(View.GONE);
		ln_content.setVisibility(View.VISIBLE);
		myGridAdapters.clear();

		ArrayList<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();

		int index = 0;

		int child_size = 0;
		DisplayMetrics dm = new DisplayMetrics();
		mLauncher.getWindowManager().getDefaultDisplay().getMetrics(dm);

		if (dm.widthPixels > dm.heightPixels) // 横屏
		{
			child_size = 14;
		} else {
			child_size = 12;
			
			if(dm.widthPixels>480 && dm.widthPixels<620)
			{
				child_size = 10;
			}
		}

		for (int i = 0; i < mLauncher.sModel.mApplications.size(); i++) {
			apps.add(mLauncher.sModel.mApplications.get(i));
			index++;

			if (index == child_size) {
				GridView grid = (GridView) LayoutInflater.from(context)
						.inflate(R.layout.app_app_all, null);
				MyGridViewAdapterMuilt myGridViewAdapter = new MyGridViewAdapterMuilt(
						context, apps);
				grid.setAdapter(myGridViewAdapter);

				grid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						CheckBox checkBox = (CheckBox) arg1
								.findViewById(R.id.cb);
						if (checkBox.isChecked()) {
							checkBox.setChecked(false);
							changeNumByType(1);
						} else {
							if (num < total) {
								checkBox.setChecked(true);
								changeNumByType(0);
							} else {
								Toast.makeText(context, "屏幕空间不足，请减少需要添加的应用数目",
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
				ln_content.addView(grid);
				myGridAdapters.add(myGridViewAdapter);

				apps = new ArrayList<ApplicationInfo>();

				index = 0;
			}

			if (index == 14 && i == mLauncher.sModel.mApplications.size() - 1) {
				break;
			}

			if (i == mLauncher.sModel.mApplications.size() - 1) {
				GridView grid = (GridView) LayoutInflater.from(context)
						.inflate(R.layout.app_app_all, null);
				MyGridViewAdapterMuilt myGridViewAdapter = new MyGridViewAdapterMuilt(
						context, apps);
				grid.setAdapter(myGridViewAdapter);
				grid.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						CheckBox checkBox = (CheckBox) arg1
								.findViewById(R.id.cb);
						if (checkBox.isChecked()) {
							checkBox.setChecked(false);
							changeNumByType(1);
						} else {
							if (num < total) {
								checkBox.setChecked(true);
								changeNumByType(0);
							} else {
								Toast.makeText(context, "屏幕空间不足，请减少需要添加的应用数目",
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
				ln_content.addView(grid);
				myGridAdapters.add(myGridViewAdapter);
			}

			ln_content.setToScreen(0);
		}
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initChildViews();
	}

	public void setLauncher(Launcher mLauncher) {
		this.mLauncher = mLauncher;
		HandlerThread mHandlerThread=new HandlerThread("Drawer");
		mHandlerThread.start();
		mHandler=new MyHandler(mHandlerThread.getLooper());
	}

	private void initLayoutParams() {
		mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
	}

	private void initChildViews() {
		mContentLayout = (LinearLayout) findViewById(R.id.drawer_contentlayout);
		mTopView = findViewById(R.id.drawer_toplayout);

		mAppRadioButton = (RadioButton) findViewById(R.id.appbutton);
		RadioButton mWidgetRadioButton = (RadioButton) findViewById(R.id.widgetbutton);
		mCloudAppRadioButton = (RadioButton) findViewById(R.id.cloudappbutton);
		mTaskManagerRadioButton = (RadioButton) findViewById(R.id.taskmanagerbutton);
		mSortButton = (Button) findViewById(R.id.sortbutton);
		mSettingButton = (Button) findViewById(R.id.settingbutton);
		mAppRadioButton.setOnClickListener(this);
		mWidgetRadioButton.setOnClickListener(this);
		mCloudAppRadioButton.setOnClickListener(this);
		mTaskManagerRadioButton.setOnClickListener(this);
		mSortButton.setOnClickListener(this);
		mSettingButton.setOnClickListener(this);
		mSortLayout = findViewById(R.id.sortlayout);
		mSettingLayout = findViewById(R.id.settinglayout);
		mSortLayout.setOnClickListener(this);
		mSettingLayout.setOnClickListener(this);

		//zy:mark
		mWidgetButton=((Button)findViewById(R.id.addwidget));
		mWidgetButton.setOnClickListener(this);
		
		// mAppRadioButton.performClick();

		View v = LayoutInflater.from(context).inflate(R.layout.batch_add_app,
				null);
		workspaceBath = (WorkspaceMiniBatch) v.findViewById(R.id.workspaceMini);
		workspaceBath.setAllDrawer(this);
		workspaceBath.setLauncher(mLauncher);
		ln_content = (ScrollLayoutTouchable) v.findViewById(R.id.ln_content);

		tv_batch_title = (TextView) v.findViewById(R.id.tv_batch_title);
		tv_batch_tip = (TextView) v.findViewById(R.id.tv_batch_tip);
		btn_batch_cancle = (Button) v.findViewById(R.id.btn_batch_cancle);
		btn_batch_cancle.setOnClickListener(this);
		btn_batch_ok = (Button) v.findViewById(R.id.btn_batch_ok);
		btn_batch_ok.setOnClickListener(this);

		batch_add_app_dialog = new PopupWindow(v, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		batch_add_app_dialog.setBackgroundDrawable(new BitmapDrawable());
		batch_add_app_dialog.setAnimationStyle(R.style.PopAnimation);

		((LinearLayout) v.findViewById(R.id.bb_ln))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						batch_add_app_dialog.dismiss();
					}
				});

		// dialog_add = new Dialog(context,R.style.theme_myDialog);
		// dialog_add.setOnDismissListener(new OnDismissListener() {
		//
		// @Override
		// public void onDismiss(DialogInterface dialog) {
		// mLauncher.clearTempBitmap();
		// }
		// });

		View a_view = LayoutInflater.from(context).inflate(
				R.layout.add_app_widget, null);

		add_workspaceMini = (WorkspaceMini) a_view.findViewById(R.id.add_workspaceMini);

		btn_add_cancle = (Button) a_view.findViewById(R.id.btn_add_cancle);
		btn_add_cancle.setOnClickListener(this);
		btn_add_ok = (Button) a_view.findViewById(R.id.btn_add_ok);
		btn_add_ok.setOnClickListener(this);
		// dialog_add.setContentView(a_view);

		add_dialog = new PopupWindow(a_view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		add_dialog.setBackgroundDrawable(new BitmapDrawable());
		add_dialog.setAnimationStyle(R.style.PopAnimation);
		add_dialog.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				mLauncher.clearTempBitmap();
			}
		});

		((LinearLayout) a_view.findViewById(R.id.b_ln))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						add_dialog.dismiss();
					}
				});

		View p_v = LayoutInflater.from(context).inflate(
				R.layout.alldrawerpopup, null);
		app_popup = new PopupWindow(v, LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT, true);
		ImageView appto = (ImageView) p_v.findViewById(R.id.add_to);
		appto.setOnClickListener(this);
		ImageView uninstall = (ImageView) p_v.findViewById(R.id.uninstall);
		uninstall.setOnClickListener(this);
		ImageView app_info = (ImageView) p_v.findViewById(R.id.app_info);
		app_info.setOnClickListener(this);

		app_popup.setBackgroundDrawable(new BitmapDrawable());
		app_popup.setOutsideTouchable(true);
		app_popup.setContentView(p_v);

		f_v = LayoutInflater.from(context).inflate(
				R.layout.alldrawerfloderpopup, null);
		floder_popup = new PopupWindow(v, LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT, true);
		Button folderdelete = (Button) f_v.findViewById(R.id.folderdelete);
		folderdelete.setOnClickListener(this);
		Button deciphering = (Button) f_v.findViewById(R.id.deciphering);
		deciphering.setOnClickListener(this);
		Button encryption = (Button) f_v.findViewById(R.id.encryption);
		encryption.setOnClickListener(this);
		Button addapp = (Button) f_v.findViewById(R.id.addapp);
		addapp.setOnClickListener(this);
		Button folderrename = (Button) f_v.findViewById(R.id.folderrename);
		folderrename.setOnClickListener(this);

		floder_popup.setBackgroundDrawable(new BitmapDrawable());
		floder_popup.setOutsideTouchable(true);
		floder_popup.setContentView(f_v);

	}

	private void addNewView(View v) {
		if (mContentLayout.getChildCount() == 1) {
			View mChildView = mContentLayout.getChildAt(0);
			if (mChildView.equals(v)) {
				return;
			}
		}
		mContentLayout.removeAllViews();
		mContentLayout.addView(v, mParams);
	}

	void performFirst() {
		mAppRadioButton.performClick();
	}
	
	void performByShortcut(int type) {
		switch(type) {
			case 1:
				mCloudAppRadioButton.performClick();
				break;
			case 2:
				mTaskManagerRadioButton.performClick();
				break;
		}
	}

	private boolean isFirst=true;
	void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
		if(isFirst && mAppChannel!=null) {
			isFirst=false;
			mAppChannel.bindItems(shortcuts, start, end);
		}
	}

	void startDrag(CellInfo cellInfo) {
		mAppChannel.startDrag(cellInfo);

		View child = cellInfo.cell;
		Object tag = child.getTag();

		this.tag = tag;
		this.tv = child;
		if (tag instanceof ApplicationInfo) // 长按的是应用
		{
			app_popup.showAsDropDown(child);
			System.out.println(" ======  应用  ==========");
		} else if (tag instanceof UserFolderInfo) // 长按的是文件夹
		{
			userFolderInfo = (UserFolderInfo) tag;
			mLauncher.itemInfo = (ItemInfo) tag;

			String id = String.valueOf(userFolderInfo.id);

			if (mLauncher.myDatabaseUtil.isDeciphering(id)) {

				f_v.findViewById(R.id.deciphering).setVisibility(View.VISIBLE);
				f_v.findViewById(R.id.encryption).setVisibility(View.GONE);
			} else {

				f_v.findViewById(R.id.deciphering).setVisibility(View.GONE);
				f_v.findViewById(R.id.encryption).setVisibility(View.VISIBLE);
			}

			floder_popup.showAsDropDown(child);
		}
	}

	private View initSortPopupwindow() {
		View mSortView = LayoutInflater.from(context).inflate(
				R.layout.popupwindow_sort, null);
		View mSortLetterView = mSortView
				.findViewById(R.id.sort_letter_textview);
		View mSortTimeView = mSortView.findViewById(R.id.sort_time_textview);
		View mSortUseNumView = mSortView
				.findViewById(R.id.sort_usenum_textview);
		mSortLetterView.setOnClickListener(this);
		mSortTimeView.setOnClickListener(this);
		mSortUseNumView.setOnClickListener(this);
		return mSortView;
	}

	private View initDrawerSettingView() {
		View mSettingView = LayoutInflater.from(context).inflate(
				R.layout.popupwindow_drawer_setting, null);
		View mBatchAddView = mSettingView.findViewById(R.id.batch_add_textview);
		View mAddFolderView = mSettingView
				.findViewById(R.id.new_folder_textview);
		View mHideAppNumView = mSettingView
				.findViewById(R.id.hide_app_textview);
		mBatchAddView.setOnClickListener(this);
		mAddFolderView.setOnClickListener(this);
		mHideAppNumView.setOnClickListener(this);
		return mSettingView;
	}

	/**
	 * 显示抽屉排序下拉选项
	 */
	private void showSortPopupwindow() {
		if (mSortPopupwindow == null) {
			mSortPopupwindow = new PopupWindow(initSortPopupwindow(),
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			mSortPopupwindow.setOutsideTouchable(true);
			mSortPopupwindow.setBackgroundDrawable(new BitmapDrawable());
			mSortPopupwindow.setAnimationStyle(R.style.popupAnimation_slow);
		}
		if (!mLauncher.isFinishing() && !mSortPopupwindow.isShowing()) {
			System.out.println("showSortPopupwindow");
			mSortPopupwindow.showAsDropDown(mSortButton, -60, 0);
		}
	}

	/**
	 * 显示抽屉设置下拉选项
	 */
	private void showDrawerSettingPopupwindow() {
		if (mDrawerSettingPopupWindow == null) {
			mDrawerSettingPopupWindow = new PopupWindow(
					initDrawerSettingView(), LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, true);
			mDrawerSettingPopupWindow.setOutsideTouchable(true);
			mDrawerSettingPopupWindow
					.setBackgroundDrawable(new BitmapDrawable());
			mDrawerSettingPopupWindow.setAnimationStyle(R.style.popupAnimation_slow);
		}
		if (!mLauncher.isFinishing() && !mDrawerSettingPopupWindow.isShowing()) {
			// mDrawerSettingPopupWindow.showAsDropDown(mSettingButton, -100,
			// 0);
			int y = AndroidUtils.dip2px(mLauncher, 15.0f);
			int x = AndroidUtils.dip2px(mLauncher, 15.0f);
//			mDrawerSettingPopupWindow.showAtLocation(mSettingButton,
//					Gravity.TOP | Gravity.RIGHT, -150, y);
			mDrawerSettingPopupWindow.showAsDropDown(mSettingButton, -125, 0);
		}
	}

	private void dismissPopupwindow(PopupWindow pop) {
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
	}

	private void setVisibleTopButton(int visible) {
		mSortButton.setVisibility(visible);
		mSettingButton.setVisibility(visible);
		mSettingLayout.setVisibility(visible);
		mSortLayout.setVisibility(visible);
		mWidgetButton.setVisibility(visible);
	}

	void setTopButtonVisibleByType(int type) {
		if (type == 1) {
			setVisibleTopButton(View.VISIBLE);
		} else {
			setVisibleTopButton(View.GONE);
		}
	}

	private ApplicationInfo getApplicationInfo(PackageManager manager,
			Intent intent, Context context) {
		// ADW: Changed the check to avoid bypassing SDcard apps in froyo
		ComponentName componentName = intent.getComponent();
		if (componentName == null) {
			return null;
		}

		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		final ApplicationInfo info = new ApplicationInfo();
		info.intent = intent;
		if (resolveInfo != null) {
			final ActivityInfo activityInfo = resolveInfo.activityInfo;

			info.icon = LauncherModel.getIcon(manager, context, activityInfo);

			if (info.title == null || info.title.length() == 0) {
				info.title = activityInfo.loadLabel(manager);
			}
			if (info.title == null) {
				info.title = "";
			}
		} else {
			// ADW: add default icon for apps on SD
			info.icon = manager.getDefaultActivityIcon();
		}
		info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
		return info;
	}

	private void addToDatabase(String packageName) {
		PackageManager packageManager = mLauncher.getPackageManager();
		Intent allApplicationMainIntent = new Intent(Intent.ACTION_MAIN, null);
		allApplicationMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> all_apps = packageManager.queryIntentActivities(
				allApplicationMainIntent, 0);
		ResolveInfo resolveInfo = null;
		for (ResolveInfo info : all_apps) {
			final ActivityInfo activityInfo = info.activityInfo;
			if (activityInfo.packageName.equals(packageName)) {
				resolveInfo = info;
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		ComponentName cn = new ComponentName(packageName,
				resolveInfo.activityInfo.name);
		String title = null;
		try {
					ActivityInfo aInfo = packageManager.getActivityInfo(cn, 0);
			intent.setComponent(cn);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					title = aInfo.loadLabel(packageManager).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				ApplicationInfo applicationinfo = getApplicationInfo(packageManager, intent,
				mLauncher);
				applicationinfo.title = title;

				LauncherModel.addItemToDatabase(mLauncher, applicationinfo,
						LauncherSettings.Favorites.CONTAINER_DESKTOP, applicationinfo.screen,
						applicationinfo.cellX, applicationinfo.cellY, false, 3);
			}
		}
	}
	private List<ResolveInfo> getResolveInfos(String packageName) {
		PackageManager packageManager=mLauncher.getPackageManager();
		Intent allApplicationMainIntent = new Intent(Intent.ACTION_MAIN, null);
 		allApplicationMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 		List<ResolveInfo> all_apps = packageManager.queryIntentActivities(
 				allApplicationMainIntent, 0);
 		List<ResolveInfo> resolveInfos=new ArrayList<ResolveInfo>();
 		for(int i=0;i<all_apps.size();i++) {
 			ResolveInfo info=all_apps.get(i);
 			if(info.activityInfo.packageName.equals(packageName)) {
 				resolveInfos.add(info);
 			}
 		}
 		return resolveInfos;
	}
	
	private MyHandler mHandler;
	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle bundle=msg.getData();
			String packageName=bundle.getString("packageName");
			boolean replacing=bundle.getBoolean("replacing");
			DrawerDatabase db = new DrawerDatabase(mLauncher);
			switch(msg.what) {
				case EVENT_PACKAGE_ADD:
					List<ResolveInfo> resolveInfos=getResolveInfos(packageName);
					PackageManager pm=mLauncher.getPackageManager();
					for(int i=0;i<resolveInfos.size();i++) {
						ResolveInfo resolveInfo=resolveInfos.get(i);
						ComponentName cn = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
			    		ActivityInfo info;
						try {
							info = pm.getActivityInfo(cn, 0);
				            String name=info.loadLabel(pm).toString();
				            db.addInstallPackageName(packageName, name);
							db.addOrUpdateOpenData(0, LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, packageName, name);
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
					}
					addToDatabase(packageName);
					for (OnPackageListener listener : listeners)
						listener.onPackageAdded(packageName, replacing);
					
					break;
				case EVENT_PACKAGE_REMOVE:
					db.deleteInstallPackageName(packageName);
					db.delete(packageName);
					LauncherModel.removePackageByDrawer(mLauncher, packageName);
					for (OnPackageListener listener : listeners) {
						System.out.println("listener:"+listener);
						listener.onPackageRemoved(packageName, replacing);
					}
					break;
			}
		}
	}
	
	private static final int EVENT_PACKAGE_ADD = 1;
	private static final int EVENT_PACKAGE_REMOVE = 2;

	void onPackageChanged(Intent intent, boolean replacing) {
		String packageName = intent.getData().getSchemeSpecificPart();
		if (!TextUtils.isEmpty(packageName)) {
			if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
				if (!replacing) {
					Message msg=mHandler.obtainMessage();
					msg.what = EVENT_PACKAGE_ADD;
					Bundle bundle=new Bundle();
					bundle.putString("packageName", packageName);
					bundle.putBoolean("replacing", replacing);
					msg.setData(bundle);
					mHandler.sendMessage(msg);
				}
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
				if (!replacing) {
					Message msg=mHandler.obtainMessage();
					msg.what = EVENT_PACKAGE_REMOVE;
					Bundle bundle=new Bundle();
					bundle.putString("packageName", packageName);
					bundle.putBoolean("replacing", replacing);
					msg.setData(bundle);
					mHandler.sendMessage(msg);
				}
			}
		}
	}

	interface OnPackageListener {
		void onPackageAdded(String packageName, boolean replacing);

		void onPackageRemoved(String packageName, boolean replacing);
	}

	public void updataNum(int num, int total) {
		this.total = total;
		this.num = num;
		tv_batch_tip.setText(num + "/" + total);
	}

	/**
	 * 
	 * @param type
	 *            0: 增加 1：减少
	 */
	public void changeNumByType(int type) {
		if (type == 0) {
			num++;
		} else {
			num--;
		}
		updataNum(num, total);
	}

	// 显示添加到屏幕对话框
	public void showAddDialog(Object target, View target_view) {
		add_workspaceMini.removeAllViews();

		int size = mLauncher.mWorkspace.getChildCount();

		mLauncher.mMiniLauncher.setDrawingCacheEnabled(true);
		mLauncher.mMiniLauncher.destroyDrawingCache();
		mLauncher.mMiniLauncher.buildDrawingCache();
		Bitmap mini_bitmap = mLauncher.mMiniLauncher.getDrawingCache();

		add_workspaceMini.changToAddMode(target, target_view);
		add_workspaceMini.init(size, 0, mini_bitmap);

		final WorkspaceMini workspace = add_workspaceMini;
		final boolean desktopLocked = false;

		int s = mLauncher.mWorkspace.getChildCount();

		for (int j = 0; j < s; j++) {
			CellLayout c = (CellLayout) mLauncher.mWorkspace.getChildAt(j);

			for (int k = 0; k < c.getChildCount(); k++) {

				View v = c.getChildAt(k);

				Object o = v.getTag();
				ItemInfo itemInfo = (ItemInfo) o;

				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.no_bg));
				v.setDrawingCacheEnabled(true);
				v.buildDrawingCache();

				Bitmap b = v.getDrawingCache();

				if (b == null) {
					ApplicationInfo apInfo = (ApplicationInfo) itemInfo;
					LinearLayout c_item = (LinearLayout) LayoutInflater.from(
							context).inflate(R.layout.c_item, null);
					ImageView img = (ImageView) c_item.findViewById(R.id.img);
					TextView tv = (TextView) c_item.findViewById(R.id.tv);
					img.setImageDrawable(apInfo.icon);
					tv.setText(apInfo.title);

					workspace.addInScreen(c_item, itemInfo.screen,
							itemInfo.cellX, itemInfo.cellY, itemInfo.spanX,
							itemInfo.spanY, !desktopLocked);

				} else {

					ImageView img = new ImageView(context);
					img.setPadding(5, 5, 5, 5);
					img.setBackgroundDrawable(new BitmapDrawable(b));
					img.setTag(v);
					// img.setLongClickable(true);
					// img.setOnLongClickListener(workspace); // 长按监听

					workspace.addInScreen(img, itemInfo.screen, itemInfo.cellX,
							itemInfo.cellY, itemInfo.spanX, itemInfo.spanY,
							!desktopLocked);
				}
			}
		}
		add_workspaceMini.setLauncher(mLauncher);
		add_dialog.showAtLocation(mContentLayout, Gravity.CENTER, 0, 0);
	}

	public void showAddDialog(Intent intent) {
		add_workspaceMini.removeAllViews();

		int size = mLauncher.mWorkspace.getChildCount();

		mLauncher.mMiniLauncher.setDrawingCacheEnabled(true);
		mLauncher.mMiniLauncher.destroyDrawingCache();
		mLauncher.mMiniLauncher.buildDrawingCache();
		Bitmap mini_bitmap = mLauncher.mMiniLauncher.getDrawingCache();

		add_workspaceMini.changToAddMode(intent);
		add_workspaceMini.init(size, 0, mini_bitmap);

		final WorkspaceMini workspace = add_workspaceMini;
		final boolean desktopLocked = false;

		int s = mLauncher.mWorkspace.getChildCount();

		for (int j = 0; j < s; j++) {
			CellLayout c = (CellLayout) mLauncher.mWorkspace.getChildAt(j);

			for (int k = 0; k < c.getChildCount(); k++) {

				View v = c.getChildAt(k);

				Object o = v.getTag();
				ItemInfo itemInfo = (ItemInfo) o;

				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.no_bg));
				v.setDrawingCacheEnabled(true);
				v.buildDrawingCache();

				Bitmap b = v.getDrawingCache();

				if (b == null) {
					if(itemInfo instanceof ApplicationInfo)
					{
						ApplicationInfo apInfo = (ApplicationInfo) itemInfo;
						
						LinearLayout c_item = (LinearLayout) LayoutInflater.from(
								context).inflate(R.layout.c_item, null);
						ImageView img = (ImageView) c_item.findViewById(R.id.img);
						TextView tv = (TextView) c_item.findViewById(R.id.tv);
						img.setImageDrawable(apInfo.icon);
						tv.setText(apInfo.title);

						workspace.addInScreen(c_item, itemInfo.screen,
								itemInfo.cellX, itemInfo.cellY, itemInfo.spanX,
								itemInfo.spanY, !desktopLocked);
					}

				} else {

					ImageView img = new ImageView(context);
					img.setPadding(5, 5, 5, 5);
					img.setBackgroundDrawable(new BitmapDrawable(b));
					img.setTag(v);
					// img.setLongClickable(true);
					// img.setOnLongClickListener(workspace); // 长按监听

					workspace.addInScreen(img, itemInfo.screen, itemInfo.cellX,
							itemInfo.cellY, itemInfo.spanX, itemInfo.spanY,
							!desktopLocked);
				}
			}
		}
		add_workspaceMini.setLauncher(mLauncher);
		add_dialog.showAtLocation(mContentLayout, Gravity.CENTER, 0, 0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (app_popup != null && app_popup.isShowing()) {
			app_popup.dismiss();
		}

		if (floder_popup != null && floder_popup.isShowing()) {
			floder_popup.dismiss();
		}
		return super.onTouchEvent(event);
	}

	
	void dismiss(){
		System.out.println(" AllDrawer  dismiss  ---->");
		if(app_popup!=null && app_popup.isShowing() )
		{
			app_popup.dismiss();
		}
		
		if(floder_popup !=null && floder_popup.isShowing())
		{
			floder_popup.dismiss();
		}
	}
	void closeFolderPoPup(){
		if(floder_popup !=null && floder_popup.isShowing())
		{
			floder_popup.dismiss();
		}
	}
	
	void handleFolderClick(FolderInfo folderInfo) {
		mAppChannel.handleFolderClick(folderInfo);
	}
	
	void close() {
		if(!mAppChannel.closeFolder()) {
			setVisibility(View.GONE);
		}
	}
}
