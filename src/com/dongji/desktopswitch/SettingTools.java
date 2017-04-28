package com.dongji.desktopswitch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class SettingTools {
	public static int FLAG_ON = 1;
	public static int FLAG_OFF = 2;
	private static int defaultBrightInt = 100;
	private static int defaultSleepInt = 15000;

	public static String DOWNLOADPATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/.dongji/pad/cache/apk/";

	public static final int REFERENSH_SCREEN = 1;
	public static final int REFERENSH_SCROLL = 2;
	public static final int SHOWPROGRESSBAR = 3;
	public static final int SHOWVIEW = 4;

	public static int getPopupwindow_x(Context context) {
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(dm);
		int xPos = dm.widthPixels;
		return px2dip(context, xPos) - 320;
	}

	public static int getPopupwindow_y() {
		return 39;
	}

	public static boolean getFlyModelStatus(Context context) {
		try {
			return Settings.System.getInt(context.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON) == 1;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void setFlyModelStatus(Context context, boolean isOn) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, isOn ? 1 : 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		context.sendBroadcast(intent);
	}

	public static int getBrightInt(Context context) {
		try {
			return Settings.System.getInt(context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return defaultBrightInt;
	}

	public static void setBrightInt(Context context, int brightInt) {
		WindowManager.LayoutParams lp = ((Activity) context).getWindow()
				.getAttributes();
		lp.screenBrightness = brightInt / 255f;
		((Activity) context).getWindow().setAttributes(lp);

		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, brightInt);
	}

	public static boolean getSyncStatus() {
		return ContentResolver.getMasterSyncAutomatically();
	}

	public static void setSync(boolean isOn) {
		ContentResolver.setMasterSyncAutomatically(isOn);
	}

	public static boolean getAutoRotationStatus(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
	}

	public static void setAutoRotation(Context context, boolean isOn) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, isOn ? 1 : 0);
	}

	public static boolean getWifiStatus(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	public static void setWifi(Context context, boolean isOn) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(isOn);
	}

	public static int getMusicInt(Context context) {
		AudioManager mgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int musicInt = 50;
		musicInt = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		return musicInt;
	}

	public static void setMusicInt(Context context, int musicProgress) {
		AudioManager mgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int tempInt = musicProgress
				* mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100;
		mgr.setStreamVolume(AudioManager.STREAM_MUSIC, tempInt, 0);
	}

	public static int getRingInt(Context context) {
		AudioManager mgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int musicInt = 50;
		musicInt = mgr.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		return musicInt;
	}

	public static void setRingInt(Context context, int musicProgress) {
		AudioManager mgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int tempInt = musicProgress
				* mgr.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
				/ 100;
		mgr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, tempInt, 0);
	}

	public static int getSleepInt(Context context) {
		try {
			return Settings.System.getInt(context.getContentResolver(),
					android.provider.Settings.System.SCREEN_OFF_TIMEOUT);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return defaultSleepInt;
	}

	public static void setSleepInt(Context context, int sleepInt) {
		Settings.System.putInt(context.getContentResolver(),
				android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
				sleepInt * 1000);
	}

	public static int castIntToProgress_Bright(int brightInt) {
		if(brightInt<65) {
			return 0;
		}else {
			float n=(float)brightInt;
			int result=Math.round((100.0f / 191.0f) * (n-64));
			System.out.println("==========result:"+result+", n:"+n);
			return result;
		}
	}

	public static int castProgressToInt_Bright(int progress) {
		float n=(float)progress;
		int result=Math.round(n/(100.0f/191.0f)+64);
		System.out.println("==========save result:"+result+", n:"+n);
		return result;
	}

	public static ArrayList<WifiInfo> getWifiInfoList(Context context) {
		ArrayList<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);

		List<ScanResult> listResult = wifiManager.getScanResults();
		ScanResult myScanResult;
		WifiInfo wifiInfo;

		if (null != listResult) {
			for (int i = 0; i < listResult.size(); i++) {
				myScanResult = listResult.get(i);
				wifiInfo = new WifiInfo();
				wifiInfo.wifiname = myScanResult.SSID;
				wifiInfo.wifimsg = getWifiMsg(myScanResult.capabilities);
				wifiInfo.wifilevel = WifiManager.calculateSignalLevel(
						myScanResult.level, 5);
				wifiInfos.add(wifiInfo);
			}
		}
		return wifiInfos;
	}

	public static final int WIFICIPHER_NOPASS = 1;
	public static final int WIFICIPHER_WEP = 2;
	public static final int WIFICIPHER_WPA = 3;
	public static final int WIFICIPHER_WPA2 = 4;

	public static String getWifiMsg(String capabilities) {
		if (capabilities.contains("WPA") && capabilities.contains("WPA2")) {
			return "通过WPA/WPA2进行保护";// 3
		}
		if (capabilities.contains("WPA")) {
			return "通过WPA进行保护"; // 4
		}
		if (capabilities.contains("WPA2")) {
			return "通过WPA2进行保护"; // 2
		}

		return "";
		// return capabilities;
	}

	public static String getConnectedWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		String ssid = wifiManager.getConnectionInfo().getSSID();
		return ssid;
	}

	public static boolean WifiConnect(Context context, String SSID,
			String password, int type) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}

		WifiConfiguration wifiConfig = CreateWifiInfo(SSID, password, type);
		if (null == wifiConfig) {
			return false;
		}

		WifiConfiguration tempConfig = isWifiConfigureExsits(wifiManager, SSID);
		if (tempConfig != null) {
			wifiManager.removeNetwork(tempConfig.networkId);
		}
		int netID = wifiManager.addNetwork(wifiConfig);
		boolean bRet = wifiManager.enableNetwork(netID, true);
		return bRet;
	}

	// 查看以前是否也配置过这个网络
	public static WifiConfiguration isWifiConfigureExsits(
			WifiManager wifiManager, String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	public static WifiConfiguration CreateWifiInfo(String SSID,
			String Password, int type) {
		WifiConfiguration config = new WifiConfiguration();
		
//		config.allowedAuthAlgorithms.clear();
//		config.allowedGroupCiphers.clear();
//		config.allowedKeyManagement.clear();
//		config.allowedPairwiseCiphers.clear();
//		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		if (type == WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (type == WIFICIPHER_WEP) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (type == WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.status = WifiConfiguration.Status.ENABLED;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		}
		return config;
	}

	public static void deleteLastSuffix(String filePath) {
		File file = new File(filePath);
		String newFilePath = filePath.substring(0, filePath.lastIndexOf("."));
		file.renameTo(new File(newFilePath));
	}

	/**
	 * 
	 * @param rootPath
	 *            后面不要带"/"
	 * @param name
	 * @param suffix
	 * @return
	 */
	public static String getAbsolutePath(String name, String suffix) {
		if (createPath(DOWNLOADPATH)) {
			return DOWNLOADPATH + name + "." + suffix;
		}
		// return "";
		return DOWNLOADPATH + name + "." + suffix;
	}

	public static boolean createPath(String path) {
		File newfolder = new File(path);
		if (!newfolder.exists()) {
			return newfolder.mkdirs();
		}
		return true;
	}

	public static int px2dip(Context context, float pxValue) {
		DisplayMetrics mDisplayMetrics = context.getResources()
				.getDisplayMetrics();
		return (int) (pxValue / mDisplayMetrics.density + 0.5f);
	}

	public static final String PAD_SHAREPREFERENCES_NAME = "pad_sharepreferences_name";

	public static int getSharedPreferences(Context context, String name) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PAD_SHAREPREFERENCES_NAME, 0);
		int shareValue = sharedPreferences.getInt(name, 0);
		return shareValue;
	}

	public static void setSharepreferences(Context context, String name,
			int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PAD_SHAREPREFERENCES_NAME, 0);
		sharedPreferences.edit().putInt(name, value).commit();
	}

	public static boolean checkApkIsExist(String apkSaveName) {
		boolean isExist = false;
		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".apk")) {
					fileName = fileName.substring(0, fileName.length() - 4);
					if (fileName.equals(apkSaveName)) {
						isExist = true;
						break;
					}
				}
			}
		}
		return isExist;
	}

	public static void deleteTempApk() {
		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".temp")) {
					files[i].delete();
				}
			}
		}
	}

	public static List<String> getAllInstallAppPackageName(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<String> packageNameList = new ArrayList<String>();
		for (PackageInfo pInfo : packages) {
			ApplicationInfo info = pInfo.applicationInfo;
			packageNameList.add(info.packageName);
		}
		return packageNameList;
	}

	public static boolean getAppIsInstall(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		for (PackageInfo pInfo : packages) {
			ApplicationInfo info = pInfo.applicationInfo;
			if (info.packageName.trim().equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	public static void showInstalledAppDetails(Context context,
			String packageName) {
		String scheme = "package";
		String app_pkg_name_21 = "com.android.settings.ApplicationPkgName"; // 调用系统InstalledAppDetails界面所需的Extra名称(用于Android
																			// 2.1及之前版本)
		String app_pkg_name_22 = "pkg"; // 调用系统InstalledAppDetails界面所需的Extra名称(用于Android
										// 2.2)
		String app_detail_pkg_name = "com.android.settings"; // InstalledAppDetails所在包名
		String app_detail_class_name = "com.android.settings.InstalledAppDetails"; // InstalledAppDetails类名
		// String app_detail_class_name = "com.android.settings.SubSettings";

		Intent intent = new Intent();
		int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3以上版本，直接调用接口
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			Uri uri = Uri.fromParts(scheme, packageName, null);
			intent.setData(uri);
		} else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）,2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			String appPkgName = apiLevel == 8 ? app_pkg_name_22
					: app_pkg_name_21;
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(app_detail_pkg_name, app_detail_class_name);
			intent.putExtra(appPkgName, packageName);
		}
		context.startActivity(intent);
	}

	public static void uninstallApp(Context context, String packageName) {
		Uri packageUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		context.startActivity(uninstallIntent);
	}

	public static List<ApplicationInfo> getAllInstallAppInfo(Context context) {

		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<ApplicationInfo> list = new ArrayList<ApplicationInfo>();
		for (PackageInfo pInfo : packages) {
			ApplicationInfo info = pInfo.applicationInfo;
			list.add(info);
		}

		return list;
	}

	public static boolean checkIsInstall(Context context, String apkPackageName) {
		List<ApplicationInfo> applicationInfos = getAllInstallAppInfo(context);
		for (int i = 0; i < applicationInfos.size(); i++) {
			if (applicationInfos.get(i).packageName.equals(apkPackageName)) {
				return true;
			}
		}
		return false;
	}

	public static ApplicationInfo getApplicationInfoByPackage(Context context,
			String apkPackageName) {
		List<ApplicationInfo> applicationInfos = getAllInstallAppInfo(context);
		ApplicationInfo applicationInfo = new ApplicationInfo();
		for (int i = 0; i < applicationInfos.size(); i++) {
			if (applicationInfos.get(i).packageName.equals(apkPackageName)) {
				applicationInfo = applicationInfos.get(i);
			}
		}
		return applicationInfo;
	}

	public static boolean rootInstallApp(String apkSaveName) {
		String apkPath = DOWNLOADPATH + apkSaveName + ".apk";

		try {
			Runtime.getRuntime().exec("pm install " + apkPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Process process = null;
		// OutputStream out = null;
		// try {
		// process = Runtime.getRuntime().exec("su"); // 得到root 权限
		// out = process.getOutputStream();
		// out.write(("pm install -r " + apkPath + "\n").getBytes());// 调用安装
		// out.flush();
		// return true;
		// } catch (IOException e) {
		// System.out.println("root install:" + e);
		// } finally {
		// if (out != null) {
		// try {
		// out.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		return false;
	}

	public static void rootUninstallApp(String apkSaveName) {
		String apkPath = DOWNLOADPATH + apkSaveName + ".apk";

		try {
			Runtime.getRuntime().exec("pm uninstall " + apkPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Process process = null;
		// OutputStream out = null;
		// try {
		// process = Runtime.getRuntime().exec("su"); // 得到root 权限
		// out = process.getOutputStream();
		// out.write(("pm uninstall " + packageName + "\n").getBytes());// 调用安装
		// out.flush();
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		// if (out != null) {
		// try {
		// out.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
	}

	public static void deleteFileByApkSaveName(String apkSaveName) {
		File file = new File(DOWNLOADPATH + apkSaveName + ".apk");
		file.delete();
	}

	public static void installApp(Context context, String apkSaveName) {
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setDataAndType(
				Uri.fromFile(new File(DOWNLOADPATH + apkSaveName + ".apk")),
				"application/vnd.android.package-archive");
		context.startActivity(installIntent);
	}
}
