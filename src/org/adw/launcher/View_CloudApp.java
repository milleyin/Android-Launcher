package org.adw.launcher;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.AllDrawer.OnPackageListener;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.desktopswitch.ADownloadApkItem;
import com.dongji.desktopswitch.ADownloadService;
import com.dongji.desktopswitch.ApkItem;
import com.dongji.desktopswitch.DataManager;
import com.dongji.desktopswitch.NavigationInfo;
import com.dongji.desktopswitch.PopupWindow_Quick;
import com.dongji.desktopswitch.SettingTools;
import com.dongji.enity.ShowPermissionRecord;
import com.dongji.launcher.R;
import com.dongji.sqlite.DrawerDatabase;
import com.dongji.ui.ScrollLayoutTouchable;

public class View_CloudApp implements OnPackageListener {
	private static int row_int = 6;
	private static int line_int = 5;

	private Launcher mLauncher;
	private View mSuperView;
	private View cloudappView;
	private ProgressBar pb_cloudapp_wait;
	private TextView tv_cloudapp_loading, tv_cloudapp_failed;
	private ScrollLayoutTouchable sl_cloudapp;

	// private ArrayList<ApkItem> apkItems;
	List<ADownloadApkItem> alldownloadApkItems = new ArrayList<ADownloadApkItem>();
	private View[] item_views;
	private LinearLayout[] cell_views;
	private ArrayList<Integer> download_indexs;
	// private List<ApkItem> installedApk;
	private Bitmap mDefaultBitmap;

	// private static CloudAppBroadcastReceiver cloudAppBroadcastReceiver =
	// null;

	// private class CloudAppBroadcastReceiver extends BroadcastReceiver {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	//
	// if (intent.getAction()
	// .equals("android.intent.action.PACKAGE_ADDED")) {
	// String packageName = intent.getDataString();
	// String PACKAGE_STR = "package:";
	// int num = packageName.indexOf(PACKAGE_STR);
	// if (num != -1) {
	// Toast.makeText(context, "安装成功", Toast.LENGTH_SHORT).show();
	// packageName = packageName.substring(PACKAGE_STR.length()
	// + num, packageName.length());
	// SettingTools.deleteFileByApkSaveName(packageName);
	// }
	// } else if (intent.getAction().equals(
	// "android.intent.action.PACKAGE_REMOVED")) {
	// String packageName = intent.getDataString();
	// String PACKAGE_STR = "package:";
	// int num = packageName.indexOf(PACKAGE_STR);
	// if (num != -1) {
	// packageName = packageName.substring(PACKAGE_STR.length()
	// + num, packageName.length());
	// }
	// }
	// }
	// }

	int loadIndex = 0;
	List<MyRunnable> loaders = new ArrayList<MyRunnable>();

	int installed_index;

	public View_CloudApp(Launcher mLauncher, View mSuperView) {
		// ActivityManager manager = (ActivityManager) mLauncher
		// .getSystemService(Context.ACTIVITY_SERVICE);
		// for (RunningServiceInfo service : manager
		// .getRunningServices(Integer.MAX_VALUE)) {
		// if ("com.dongji.desktopswitch.ADownloadService"
		// .equals(service.service.getClassName())) {
		// System.out.println("come in..............");
		// Intent tempIntent = new Intent();
		// tempIntent.setClass(mLauncher, ADownloadService.class);
		// mLauncher.stopService(tempIntent);
		// }
		// }
		SettingTools.deleteTempApk();
		this.mLauncher = mLauncher;
		this.mSuperView = mSuperView;
		mDefaultBitmap = BitmapFactory.decodeResource(mLauncher.getResources(),
				R.drawable.icon);
		download_indexs = new ArrayList<Integer>();

		// IntentFilter intentFilter = new IntentFilter();
		// intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
		// if (null == cloudAppBroadcastReceiver) {
		// cloudAppBroadcastReceiver = new CloudAppBroadcastReceiver();
		// mLauncher.registerReceiver(cloudAppBroadcastReceiver, intentFilter);
		// }
	}

	public View initViews() {
		cloudappView = LayoutInflater.from(mLauncher).inflate(
				R.layout.layout_cloudapp, null);

		pb_cloudapp_wait = (ProgressBar) cloudappView
				.findViewById(R.id.pb_cloudapp_wait);
		tv_cloudapp_loading = (TextView) cloudappView
				.findViewById(R.id.tv_cloudapp_loading);
		tv_cloudapp_failed = (TextView) cloudappView
				.findViewById(R.id.tv_cloudapp_failed);
		sl_cloudapp = (ScrollLayoutTouchable) cloudappView
				.findViewById(R.id.sl_cloudapp);

		HandlerThread mHandlerThread = new HandlerThread("");
		mHandlerThread.start();
		myHandler = new MyHandler(mHandlerThread.getLooper());
		myHandler.sendEmptyMessage(SettingTools.SHOWPROGRESSBAR);

		tv_cloudapp_failed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myHandler.sendEmptyMessage(SettingTools.SHOWPROGRESSBAR);
			}
		});

		return cloudappView;
	}

	private void fillViewData() {
		DataManager dataManager = DataManager.newInstance();
		PackageManager pm = mLauncher.getPackageManager();
		try {
			ArrayList<NavigationInfo> navList = dataManager.getNavigationList();
			if (navList == null) {
				// Toast.makeText(mLauncher, "数据获取失败！",
				// Toast.LENGTH_LONG).show();
				mLauncher.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pb_cloudapp_wait.setVisibility(View.GONE);
						tv_cloudapp_loading.setVisibility(View.GONE);
						tv_cloudapp_failed.setVisibility(View.VISIBLE);
					}
				});
				return;
			}
			List<ApkItem> apkItems = dataManager.getApps(mLauncher,
					navList.get(1), true);
			if (null == apkItems) {
				// Toast.makeText(mLauncher, "数据获取失败！",
				// Toast.LENGTH_LONG).show();
				mLauncher.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pb_cloudapp_wait.setVisibility(View.GONE);
						tv_cloudapp_loading.setVisibility(View.GONE);
						tv_cloudapp_failed.setVisibility(View.VISIBLE);
					}
				});
				return;
			}
			List<ApkItem> gameList = dataManager.getApps(mLauncher,
					navList.get(1), false);
			if (gameList != null) {
				apkItems.addAll(gameList);
			}
			// removeInstallApp(apkItems);

			List<ApkItem> installedApk = new ArrayList<ApkItem>(); // 已安装的应用
			List<ApkItem> unstalledApk = new ArrayList<ApkItem>(); // 未安装的应用

			for (ApkItem apk : apkItems) {
				String pName = apk.packageName;

				boolean isInstall = false;
				try {
					pm.getInstallerPackageName(pName);
					isInstall = true;
				} catch (Exception e) {
				}
				if (isInstall) { // 已安装

					apk.status = ADownloadService.STATUS_OF_INSTALLED;
					installedApk.add(apk);
				} else {
					if (SettingTools.checkApkIsExist(pName)) {// 已下载未安装

						apk.status = ADownloadService.STATUS_OF_DOWNLOADCOMPLETE;
						unstalledApk.add(apk);
					} else { // 未安装
						apk.status = ADownloadService.STATUS_OF_UNINSTALLED;
						unstalledApk.add(apk);
					}
				}
			}

			installed_index = installedApk.size() - 1;
			for (ApkItem a : installedApk) {
				ADownloadApkItem item = new ADownloadApkItem(a);
				alldownloadApkItems.add(item);
			}

			for (ApkItem a : unstalledApk) {
				ADownloadApkItem item = new ADownloadApkItem(a);
				alldownloadApkItems.add(item);
			}

			item_views = new View[apkItems.size()];
			cell_views = new LinearLayout[apkItems.size()];
			int index = 0;

			LayoutParams layoutParams_all = new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			LayoutParams layoutParams_line = new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
			LayoutParams layoutParams_item = new LayoutParams(
					SettingTools.px2dip(mLauncher, 120), SettingTools.px2dip(
							mLauncher, 90), 1);

			LinearLayout linearLayout_all;
			linearLayout_all = new LinearLayout(mLauncher);
			linearLayout_all.setLayoutParams(layoutParams_all);
			linearLayout_all.setPadding(0, 20, 0, 20);
			linearLayout_all.setOrientation(LinearLayout.VERTICAL);

			LinearLayout linearLayout_line = new LinearLayout(mLauncher);
			linearLayout_line.setLayoutParams(layoutParams_line);
			linearLayout_line.setOrientation(LinearLayout.HORIZONTAL);

			for (int i = 0; i < alldownloadApkItems.size(); i++) {
				LinearLayout linearLayout_item = new LinearLayout(mLauncher);
				linearLayout_item.setLayoutParams(layoutParams_item);
				linearLayout_item.setGravity(Gravity.CENTER);
				// ADownloadApkItem aDownloadApkItem = new
				// ADownloadApkItem(apkItems.get(i));

				cell_views[i] = linearLayout_item;

				ADownloadApkItem aDownloadApkItem = alldownloadApkItems.get(i);

				aDownloadApkItem.viewIndex = i;
				linearLayout_item.addView(getItemView(aDownloadApkItem));
				linearLayout_line.addView(linearLayout_item);
				index++;
				if (index % row_int == 0) {
					linearLayout_all.addView(linearLayout_line);
					linearLayout_line = new LinearLayout(mLauncher);
					linearLayout_line.setLayoutParams(layoutParams_line);
					linearLayout_line.setOrientation(LinearLayout.HORIZONTAL);
				}

				if (index == row_int * line_int) {
					final LinearLayout tempLayout = linearLayout_all;
					mLauncher.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sl_cloudapp.addView(tempLayout);
						}
					});
					linearLayout_all = new LinearLayout(mLauncher);
					linearLayout_all.setLayoutParams(layoutParams_all);
					linearLayout_all.setPadding(0, 20, 0, 20);
					linearLayout_all.setOrientation(LinearLayout.VERTICAL);
					index = 0;

				} else if (i == apkItems.size() - 1) {
					for (int temp = linearLayout_line.getChildCount(); temp < row_int; temp++) {
						linearLayout_item = new LinearLayout(mLauncher);
						linearLayout_item.setLayoutParams(layoutParams_item);
						linearLayout_line.addView(linearLayout_item);
					}
					linearLayout_all.addView(linearLayout_line);
					for (int temp = linearLayout_all.getChildCount(); temp < line_int; temp++) {
						linearLayout_item = new LinearLayout(mLauncher);
						linearLayout_item.setLayoutParams(layoutParams_item);
						linearLayout_all.addView(linearLayout_item);
					}
					final LinearLayout tempLayout = linearLayout_all;
					mLauncher.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sl_cloudapp.addView(tempLayout);
						}
					});
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		myHandler.sendEmptyMessage(SettingTools.SHOWVIEW);
	}

	// private void removeInstallApp(ArrayList<ApkItem> apkItems) {
	// List<String> packageNameList = SettingTools
	// .getAllInstallAppPackageName(mLauncher);
	// for (int i = 0; i < apkItems.size(); i++) {
	// ApkItem apkItem = apkItems.get(i);
	// for (int j = 0; j < packageNameList.size(); j++) {
	// if (apkItem.packageName.trim().equals(packageNameList.get(j))) {
	// apkItems.remove(apkItem);
	// break;
	// }
	// }
	// }
	// }

	public static PopupWindow popupWindow_quick;

	private View getItemView(final ADownloadApkItem aDownloadApkItem) {
		int index = aDownloadApkItem.viewIndex;
		item_views[index] = LayoutInflater.from(mLauncher).inflate(
				R.layout.gridview_item_cloudapp, null);
		final ImageView iv_cloudappIcon = (ImageView) item_views[index]
				.findViewById(R.id.iv_cloudappIcon);
		final ImageView iv_cloudappbg = (ImageView) item_views[index]
				.findViewById(R.id.iv_cloudappbg);
		final ProgressBar pbcloudappDownload = (ProgressBar) item_views[index]
				.findViewById(R.id.pbcloudappDownload);
		final TextView tv_waitTiips = (TextView) item_views[index]
				.findViewById(R.id.tv_waitTiips);
		TextView tv_cloudappName = (TextView) item_views[index]
				.findViewById(R.id.tv_cloudappName);

		iv_cloudappIcon.setImageResource(R.drawable.icon);

		switch (aDownloadApkItem.apkStatus) {
		case ADownloadService.STATUS_OF_DOWNLOADCOMPLETE: // 已下载未安装
			tv_waitTiips.setText("点击安装");
			tv_waitTiips.setVisibility(View.VISIBLE);
			break;

		case ADownloadService.STATUS_OF_UNINSTALLED: // 未安装
			tv_waitTiips.setText("点击下载");
			tv_waitTiips.setVisibility(View.VISIBLE);
			break;

		case ADownloadService.STATUS_OF_INSTALLED: // 已安装
			iv_cloudappbg.setVisibility(View.GONE);
			break;
		}

		// try {
		// FileService.loadFileToMap();
		// FileService.getBitmap(aDownloadApkItem.apkIconUrl, iv_cloudappIcon,
		// mDefaultBitmap, 0);
		// // if (SettingTools.getAppIsInstall(mLauncher,
		// // aDownloadApkItem.apkPackageName)) {
		// // // imageToColor(iv_cloudappIcon);
		// // } else {
		// // imageTogray(iv_cloudappIcon);
		// // }
		// } catch (OutOfMemoryError e) {
		// if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
		// mDefaultBitmap.recycle();
		// }
		// }

		loaders.add(new MyRunnable(aDownloadApkItem.apkIconUrl,
				iv_cloudappIcon, index));

		pbcloudappDownload.setMax(100);
		String tempName = aDownloadApkItem.apkName.trim();
		if (tempName.length() > 6) {
			tempName = tempName.substring(0, 5);
		}
		tv_cloudappName.setText(tempName);
		item_views[index].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!SettingTools.getWifiStatus(mLauncher)) {
					Toast.makeText(mLauncher, "无可用的WIFI，请连接后再安装。",
							Toast.LENGTH_LONG).show();
					return;
				}
				if (pbcloudappDownload.getVisibility() == View.VISIBLE) {
					return;
				}
				if (tv_waitTiips.getVisibility() == View.VISIBLE
						&& tv_waitTiips.getText().toString().trim()
								.equals("点击安装")) {
					SettingTools.installApp(mLauncher,
							aDownloadApkItem.apkPackageName);
					return;
				}
				if (!SettingTools.checkIsInstall(mLauncher,
						aDownloadApkItem.apkPackageName)) {
					Log.e("TAG2", "" + aDownloadApkItem.apkName + ","
							+ aDownloadApkItem + ","
							+ aDownloadApkItem.apkStatus);
					if (aDownloadApkItem.apkStatus == ADownloadService.STATUS_OF_PREPAREDOWNLOAD) {
						Toast.makeText(mLauncher, "正在等待下载", Toast.LENGTH_SHORT)
								.show();
						return;
					}
					pbcloudappDownload.setVisibility(View.GONE);
					tv_waitTiips.setText("等待下载");
					tv_waitTiips.setVisibility(View.VISIBLE);
					aDownloadApkItem.apkStatus = ADownloadService.STATUS_OF_PREPAREDOWNLOAD;
					download_indexs.add(aDownloadApkItem.viewIndex);

					Intent serviceIntent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putParcelable(ADownloadService.APKDOWNLOADITEM,
							aDownloadApkItem);
					serviceIntent.putExtra(ADownloadService.APKDOWNLOADITEM,
							bundle);
					serviceIntent.setClass(mLauncher, ADownloadService.class);
					mLauncher.startService(serviceIntent);

					sendRefreshMessage(SettingTools.REFERENSH_SCROLL,
							aDownloadApkItem.viewIndex);

				} else {
					// imageToColor(iv_cloudappIcon);
					PackageManager packageManager = mLauncher
							.getPackageManager();
					Intent intent = new Intent();
					intent = packageManager
							.getLaunchIntentForPackage(aDownloadApkItem.apkPackageName);
					mLauncher.startActivity(intent);

					clickInsertDB(aDownloadApkItem.apkPackageName,
							aDownloadApkItem.apkName);
				}
			}
		});
		item_views[index].setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (SettingTools.checkIsInstall(mLauncher,
						aDownloadApkItem.apkPackageName)) {
					int index = aDownloadApkItem.viewIndex;
					popupWindow_quick = PopupWindow_Quick.getInstance(
							mLauncher, aDownloadApkItem.apkPackageName,
							cell_views[index], ((TextView) item_views[index]
									.findViewById(R.id.tv_cloudappName))
									.getText().toString(),
							((ImageView) item_views[index]
									.findViewById(R.id.iv_cloudappIcon))
									.getDrawable(), mSuperView);

					popupWindow_quick.setFocusable(true);
					popupWindow_quick.setOutsideTouchable(true);
					popupWindow_quick
							.setBackgroundDrawable(new BitmapDrawable());
					WindowManager windowManager = (WindowManager) mLauncher
							.getSystemService(Context.WINDOW_SERVICE);
					DisplayMetrics dm = new DisplayMetrics();
					windowManager.getDefaultDisplay().getMetrics(dm);
					popupWindow_quick
							.setAnimationStyle(R.style.popupAnimation_slow);
					popupWindow_quick.showAsDropDown(v,
							-SettingTools.px2dip(mLauncher, 60),
							-SettingTools.px2dip(mLauncher, 35));
				}
				return false;
			}
		});
		return item_views[index];
	}

	private DrawerDatabase mDrawerDatabase;

	private void clickInsertDB(String packageName, String name) {
		if (mDrawerDatabase == null) {
			mDrawerDatabase = new DrawerDatabase(mLauncher);
		}
		mDrawerDatabase.addOrUpdateOpenData(0,
				LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, packageName,
				name);
	}

	// private void imageTogray(ImageView imageView) {
	// ColorMatrix cm = new ColorMatrix();
	// cm.setSaturation(0.0f);
	// ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
	// imageView.setColorFilter(cf);
	// }
	//
	// private void imageToColor(ImageView imageView) {
	// ColorMatrix cm = new ColorMatrix();
	// cm.setSaturation(1f);
	// ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
	// imageView.setColorFilter(cf);
	// }

	// private void reflushView() {
	// for (int i = 0; i <apkItems.size(); i++) {
	// if(apkItems.)
	// }
	// }

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SettingTools.REFERENSH_SCROLL:
				final int index = msg.arg1;

				// System.out.println("index............" + index +
				// ",size......"
				// + download_indexs.size() + ",size2...."
				// + ADownloadService.downloadingAPKList.size());

				for (int i = 0; i < download_indexs.size(); i++) {
					TextView tv_cloudappName = (TextView) item_views[download_indexs
							.get(i)].findViewById(R.id.tv_cloudappName);
					for (int j = 0; j < ADownloadService.downloadingAPKList
							.size(); j++) {
						final ADownloadApkItem aDownloadApkItem = ADownloadService.downloadingAPKList
								.get(j);
						String tempName = aDownloadApkItem.apkName.trim();
						if (tempName.length() > 6) {
							tempName = tempName.substring(0, 5);
						}
						if (tv_cloudappName.getText().toString().trim()
								.equals(tempName)) {
							// System.out.println(i
							// + ".........i===...................."
							// + download_indexs.size());
							if (i >= download_indexs.size()) {
								return;
							}
							int tempIndex = download_indexs.get(i);
							final ProgressBar pbcloudappDownload = (ProgressBar) item_views[tempIndex]
									.findViewById(R.id.pbcloudappDownload);
							final TextView tv_waitTiips = (TextView) item_views[tempIndex]
									.findViewById(R.id.tv_waitTiips);
							// final ImageView iv_cloudappIcon = (ImageView)
							// item_views[index]
							// .findViewById(R.id.iv_cloudappIcon);
							mLauncher.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (aDownloadApkItem.apkStatus == ADownloadService.STATUS_OF_PREPAREDOWNLOAD) {
										pbcloudappDownload
												.setVisibility(View.GONE);
										tv_waitTiips
												.setVisibility(View.VISIBLE);
									} else {
										pbcloudappDownload
												.setVisibility(View.VISIBLE);
										tv_waitTiips.setVisibility(View.GONE);
									}

									int progress = (int) ((long) aDownloadApkItem.apkDownloadSize * 100 / (long) aDownloadApkItem.apkTotalSize);
									Log.e("TAG", progress + ","
											+ aDownloadApkItem + ","
											+ +aDownloadApkItem.apkDownloadSize
											+ ","
											+ aDownloadApkItem.apkTotalSize);
									pbcloudappDownload.setProgress(progress);
									if (SettingTools
											.checkApkIsExist(aDownloadApkItem.apkPackageName)) {
										// SettingTools
										// .rootInstallApp(aDownloadApkItem.apkPackageName);
										pbcloudappDownload
												.setVisibility(View.GONE);
										tv_waitTiips.setText("点击安装");
										tv_waitTiips
												.setVisibility(View.VISIBLE);
										SettingTools
												.installApp(
														mLauncher,
														aDownloadApkItem.apkPackageName);
										// imageToColor(iv_cloudappIcon);
									}
								}
							});

							if (SettingTools
									.checkApkIsExist(aDownloadApkItem.apkPackageName)) {
								ADownloadService.downloadingAPKList
										.remove(aDownloadApkItem);
								download_indexs
										.remove(aDownloadApkItem.viewIndex);
							}
						}
					}
				}
				if (download_indexs.size() > 0 && !ADownloadService.isStop) {
					sendRefreshMessage(SettingTools.REFERENSH_SCROLL, index);
				} else {
					removeMessage(SettingTools.REFERENSH_SCROLL);
				}
				break;
			case SettingTools.SHOWVIEW:
				mLauncher.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pb_cloudapp_wait.setVisibility(View.GONE);
						tv_cloudapp_loading.setVisibility(View.GONE);
						tv_cloudapp_failed.setVisibility(View.GONE);
						sl_cloudapp.setVisibility(View.VISIBLE);
					}
				});

				if (loaders.size() > 0) {
					startLoad();
				}

				break;
			case SettingTools.SHOWPROGRESSBAR:
				mLauncher.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						pb_cloudapp_wait.setVisibility(View.VISIBLE);
						tv_cloudapp_loading.setVisibility(View.VISIBLE);
						tv_cloudapp_failed.setVisibility(View.GONE);
						sl_cloudapp.setVisibility(View.GONE);
					}
				});
				fillViewData();
				break;
			}
		}
	}

	private MyHandler myHandler;

	public void removeMessage(int flag) {
		if (myHandler != null && myHandler.hasMessages(flag)) {
			myHandler.removeMessages(flag);
		}
	}

	public void sendRefreshMessage(int flag, int index) {
		// removeMessage(flag);
		Message msg = new Message();
		msg.what = flag;
		msg.arg1 = index;
		myHandler.sendMessageDelayed(msg, 500);
	}

	@Override
	public void onPackageAdded(String packageName, boolean replacing) {
		System.out.println("接收到广播。。。。。。。。。。。。。。。");
		// Toast.makeText(mLauncher, "安装成功", Toast.LENGTH_SHORT).show();
		SettingTools.deleteFileByApkSaveName(packageName);

		Integer finded = -1;
		// for(Integer i:download_indexs)
		// {
		// ADownloadApkItem aDownloadApkItem = alldownloadApkItems.get(i);
		//
		// if(aDownloadApkItem.apkName.equals(packageName))
		// {
		// finded = aDownloadApkItem.viewIndex;
		// break;
		// }
		// }

		for (ADownloadApkItem aItem : alldownloadApkItems) {
			if (aItem.apkPackageName.equals(packageName)) {
				finded = aItem.viewIndex;
			}
		}

		// download_indexs.remove(finded);
		swapSiteHandler.sendEmptyMessage(finded);
	}

	@Override
	public void onPackageRemoved(String packageName, boolean replacing) {
		for (int i = 0; i < alldownloadApkItems.size(); i++) {
			if (alldownloadApkItems.get(i).apkPackageName.equals(packageName)) {
				// ((ImageView) item_views[i].findViewById(R.id.iv_cloudappbg))
				// .setVisibility(View.VISIBLE);
				swapSiteHandler_uninstall.sendEmptyMessage(alldownloadApkItems
						.get(i).viewIndex);
				break;
			}
		}
	}

	// public static void unregisterCloudAppBroadcastReceiver(Context context) {
	// if (null != cloudAppBroadcastReceiver) {
	// context.unregisterReceiver(cloudAppBroadcastReceiver);
	// }
	// }

	class MyRunnable implements Runnable {

		String s_url;
		ImageView img;
		int position;

		Bitmap bm;

		public MyRunnable(String s_url, ImageView img, int position) {
			this.s_url = s_url;
			System.out.println(" s_url  --->" + s_url);
			this.img = img;
			this.position = position;
		}

		@Override
		public void run() {

			// try {
			// Thread.sleep(1000);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			URL url = null;
			try {
				url = new URL(s_url);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				System.out
						.println(" !!!!!!!!!!!! =============  load  wrong  --->"
								+ s_url);
			}
			ByteArrayOutputStream bos = null;
			InputStream in = null;
			byte[] result = null;
			try {
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				con.connect();
				in = con.getInputStream();
				bos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int ch = -1;
				while ((ch = in.read(buf)) != -1) {
					bos.write(buf, 0, ch);
				}
				bos.flush();
				bos.close();
				in.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (bos != null) {
				result = bos.toByteArray();
			}

			if (result != null) {
				bm = BitmapFactory.decodeByteArray(result, 0, result.length);
				if (bm != null && img != null) {

					// Bundle bundle = new Bundle();
					loadHandler.sendEmptyMessage(position);
					// img.setImageBitmap(bm);
				}
			}
			loadNext();
		}

		public void update() {
			try {
				img.setImageBitmap(bm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void startLoad() {
		for (int i = 0; i < 5; i++) {
			new Thread(loaders.get(i)).start();
		}
		loadIndex = 4;
	}

	void loadNext() {
		loadIndex++;
		// System.out.println(" loadIndex ---> " + loadIndex +
		// " loaders.size() ---> " + loaders.size());
		if (loadIndex < loaders.size()) {
			new Thread(loaders.get(loadIndex)).start();
		}
	}

	Handler loadHandler = new Handler() {
		public void handleMessage(Message msg) {
			loaders.get(msg.what).update();
		};
	};

	Handler swapSiteHandler_uninstall = new Handler() {
		public void handleMessage(Message msg) {
			int position = msg.what;

			System.out.println("position..............." + position);

			if (position != -1) {
				//
				if (installed_index == position) // 卸载了组后一个安装的应用
				{
					System.out.println("卸载....................");
					((ImageView) item_views[position]
							.findViewById(R.id.iv_cloudappbg))
							.setVisibility(View.VISIBLE);
					TextView tv_waitTiips = (TextView) item_views[position]
							.findViewById(R.id.tv_waitTiips);
					tv_waitTiips.setText("点击下载");
					tv_waitTiips.setVisibility(View.VISIBLE);
					ADownloadApkItem a = alldownloadApkItems.get(position);
					a.apkStatus = ADownloadService.STATUS_OF_UNINSTALLED;
					Log.e("TAG2", a.apkName + "," + a.apkStatus);
				} else {

					ADownloadApkItem a_old = alldownloadApkItems
							.get(installed_index);

					ADownloadApkItem a_new = alldownloadApkItems.get(position);
					a_new.apkStatus = ADownloadService.STATUS_OF_UNINSTALLED;

					//
					// System.out.println(" before ===============");
					// System.out.println("  a_old ---> " + a_old.apkPackageName
					// + " :  " + a_old.viewIndex);
					// System.out.println(" a_new ---> " + a_new.apkPackageName
					// + " :  " + a_new.viewIndex);

					a_old.viewIndex = position;
					a_new.viewIndex = installed_index;
					//
					// System.out.println(" after ===============");
					// System.out.println(" a_old ---> " + a_old.apkPackageName
					// + " :  " + a_old.viewIndex);
					// System.out.println(" a_new ---> " + a_new.apkPackageName
					// + " :  " + a_new.viewIndex);

					LinearLayout ln_old = cell_views[installed_index];
					ln_old.removeAllViews();

					LinearLayout ln_new = cell_views[position];
					ln_new.removeAllViews();

					((ImageView) item_views[position]
							.findViewById(R.id.iv_cloudappbg))
							.setVisibility(View.VISIBLE);
					TextView tv_waitTiips = (TextView) item_views[position]
							.findViewById(R.id.tv_waitTiips);
					tv_waitTiips.setText("点击下载");
					tv_waitTiips.setVisibility(View.VISIBLE);

					View a = item_views[position];
					View c = a;
					View b = item_views[installed_index];
					View d = b;

					item_views[position] = d;
					item_views[installed_index] = c;

					ln_old.addView(c);
					ln_new.addView(d);
				}
			}

			installed_index--;
			// }
		};
	};

	Handler swapSiteHandler = new Handler() {
		public void handleMessage(Message msg) {
			int position = msg.what;

			if (position != -1) {
				installed_index++;

				if (installed_index == position) // 安装了第一个未安装的应用
				{
					((ImageView) item_views[position]
							.findViewById(R.id.iv_cloudappbg))
							.setVisibility(View.GONE);
					((TextView) item_views[position]
							.findViewById(R.id.tv_waitTiips))
							.setVisibility(View.GONE);
				} else {

					ADownloadApkItem a_old = alldownloadApkItems
							.get(installed_index);
					ADownloadApkItem a_new = alldownloadApkItems.get(position);

					// System.out.println(" before ===============");
					// System.out.println("  a_old ---> " + a_old.apkPackageName
					// +" :  " +a_old.viewIndex);
					// System.out.println(" a_new ---> " + a_new.apkPackageName
					// +" :  " +a_new.viewIndex);

					a_old.viewIndex = position;
					a_new.viewIndex = installed_index;

					// System.out.println(" after ===============");
					// System.out.println(" a_old ---> " + a_old.apkPackageName
					// +" :  " +a_old.viewIndex);
					// System.out.println(" a_new ---> " + a_new.apkPackageName
					// +" :  " +a_new.viewIndex);

					LinearLayout ln_old = cell_views[installed_index];
					ln_old.removeAllViews();

					LinearLayout ln_new = cell_views[position];
					ln_new.removeAllViews();

					((ImageView) item_views[position]
							.findViewById(R.id.iv_cloudappbg))
							.setVisibility(View.GONE);
					((TextView) item_views[position]
							.findViewById(R.id.tv_waitTiips))
							.setVisibility(View.GONE);

					View a = item_views[position];
					View c = a;
					View b = item_views[installed_index];
					View d = b;

					item_views[position] = d;
					item_views[installed_index] = c;

					ln_old.addView(c);
					ln_new.addView(d);
					
					ADownloadApkItem tempA = alldownloadApkItems
							.get(installed_index);
					alldownloadApkItems.set(installed_index,
							alldownloadApkItems.get(position));
					alldownloadApkItems.set(position, tempA);
				}
			}
		};
	};
}
