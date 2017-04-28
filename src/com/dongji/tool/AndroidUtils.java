package com.dongji.tool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.net.HttpClientApi;


/**
 * @author zhangkai
 */
public class AndroidUtils {
	private static Toast mToast;
	private static TextView toast_txt;
	public static String cachePath;
	private static DisplayMetrics mDisplayMetrics;

	static {
		cachePath = Environment.getExternalStorageDirectory().getPath()
				+ "/.dongji/dongjiMarket/cache/";
	}

	private static ZipInputStream getZipInputStream(Context context, int rawId)
			throws IOException {
		ZipInputStream zis = new ZipInputStream(context.getResources()
				.openRawResource(rawId));
		ZipEntry zipEntry = zis.getNextEntry();
		if (zipEntry != null) {
			return zis;
		}
		return null;
	}

	public static void copyFile(Context context, int rawId, String path)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		ZipInputStream zis = getZipInputStream(context, rawId);
		byte[] b = new byte[2048];
		int num = 0;
		while ((num = zis.read(b)) != -1) {
			fos.write(b, 0, num);
		}
		zis.close();
		fos.flush();
		fos.close();
	}

	public static void copyFile(Context context, String... str)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(str[0]);
		ZipInputStream zis = getZipInputStream(context, str[1]);
		byte[] b = new byte[2048];
		int num = 0;
		while ((num = zis.read(b)) != -1) {
			fos.write(b, 0, num);
		}
		zis.close();
		fos.flush();
		fos.close();
	}

	private static ZipInputStream getZipInputStream(Context context,
			String filePath) throws IOException {
		ZipInputStream zis = new ZipInputStream(context.getAssets().open(
				filePath));
		ZipEntry zipEntry = zis.getNextEntry();
		if (zipEntry != null) {
			return zis;
		}
		return null;
	}


	/**
	 * 复制 drawable 到指定的目录
	 * 
	 * @param context
	 * @param rid
	 * @param savePath
	 * @return
	 */
	private static boolean copyImage(Context context, int rid, String savePath) {
		Bitmap bitmap = null;
		try {
			if (new File(savePath).exists()) {
				return true;
			}
			bitmap = BitmapFactory.decodeResource(context.getResources(), rid);
			if (bitmap != null) {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(savePath);
					boolean flag = bitmap.compress(CompressFormat.JPEG, 100,
							fos);
					fos.flush();
					return flag;
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (OutOfMemoryError e) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
		return false;
	}

	/**
	 * 检查文件夹是否存在，如不存在则直接创建
	 * 
	 * @param path
	 */
	private static void checkFileAndmkdirs(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 获取 SD 卡
	 * 
	 * @return
	 */
	public static File getSdcardFile() {
		return Environment.getExternalStorageDirectory();
	}

	/**
	 * 检查文件是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkFileExists(String path) {
		File file = new File(path);
		return file.exists();
	}

	/**
	 * 判断 SD 卡是否存在
	 * 
	 * @return
	 */
	public static boolean isSdcardExists() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取 SD 卡剩余大小
	 * 
	 * @return
	 */
	public static long getSdcardAvalilaleSize() {
		File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
		if (path != null && path.exists()) {
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}
		return 0;
	}

	/**
	 * 根据路径删除此路径下所有文件
	 * 
	 * @param filePath
	 */
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteFile(files[i].getPath());
					} else {
						files[i].delete();
					}
				}
			}
			file.delete();
		}
	}
	
	public static void deleteFile(File file) {
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				if (files != null) {
					for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
						deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
					}
				}
			}
			file.delete();
		}
	}
	
	/**
	 * 计算文件大小
	 * @param f
	 * @return
	 */
	public static long getFileSize(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		if (flist != null) {
			for (int i = 0; i < flist.length; i++) {
//				System.out.println("file path======>" + flist[i].getAbsolutePath());
				if (flist[i].isDirectory()) {
					size = size + getFileSize(flist[i]);
				} else {
					size = size + flist.length;
				}
			}
		}
		return size;
	}
	public static long getFileSize2(File f) {
		if (f.exists()) {
//			System.out.println("file path======>" + f.getAbsolutePath());
			if (f.isDirectory()) {
				File[] flist = f.listFiles();
				long size = 0;
				if (flist != null) {
					for (File file : flist) {
						size += getFileSize2(file);
					}
				}
				return size;
			} else {
				long size = f.length();
				return size;
			}
		} else {
			return 0;
		}
	}

	/**
	 * 判断网络是否有效
	 * 
	 * @param context
	 *            上下文对象
	 * @return boolean -- TRUE 有效 -- FALSE 无效
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (context != null) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getApplicationContext().getSystemService(
							Context.CONNECTIVITY_SERVICE);
			if (manager == null) {
				return false;
			}
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			if (networkinfo == null || !networkinfo.isAvailable()) {
				return false;
			}

			if (networkinfo.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 验证当前wifi状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiAvailable(Context context) {
		int type = ConnectivityManager.TYPE_WIFI;
		return isAvailableByType(context, type);
	}

	/**
	 * 验证当前mobile状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileAvailable(Context context) {
		int type = ConnectivityManager.TYPE_MOBILE;
		return isAvailableByType(context, type);
	}

	/**
	 * 根据状态验证网络
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	private static boolean isAvailableByType(Context context, int type) {
		if (context != null) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getApplicationContext().getSystemService(
							Context.CONNECTIVITY_SERVICE);
			if (manager != null) {
				NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
				for (int i = 0; i < networkInfos.length; i++) {
					if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
						if (networkInfos[i].getType() == type) {
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	/**
	 * 获取版本大小
	 * 
	 * @param cntext
	 * @return
	 */
	public static String getAppVersionName(Context cntext) {
		try {

			PackageManager pm = cntext.getPackageManager();
			String pkgName = cntext.getPackageName();
			PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
			String ver = pkgInfo.versionName;
			return ver;
		} catch (NameNotFoundException e) {
			return "0";
		}
	}

	/**
	 * 获取屏幕尺寸
	 * 
	 * @param context
	 * @return
	 */
	public static DisplayMetrics getScreenSize(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm;
	}

	/**
	 * 验证邮箱格式是否正确
	 * 
	 * @param emailStr
	 * @return
	 */
	public static boolean isEmail(String emailStr) {
		String patternStr = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(emailStr);
		return m.matches();
	}

	/**
	 * 验证密码长度是否在6－18位之间
	 * 
	 * @param passwordStr
	 * @return
	 */
	public static boolean passwdFormat(String passwordStr) {
		int len = passwordStr.length();
		if (len >= 6 && len <= 18) {
			return true;
		}
		return false;
	}

	/**
	 * 判断手机是否root
	 * 
	 * @return
	 */
	public static boolean isRoot() {
		Process process = null;
		DataOutputStream dos = null;
		try {
			process = Runtime.getRuntime().exec("su");
			dos = new DataOutputStream(process.getOutputStream());
			dos.writeBytes("exit\n");
			dos.flush();
			int exitValue = process.waitFor();
			if (exitValue == 0) {
				return true;
			}
		} catch (IOException e) {

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
				}
			}
			/*
			 * if (process != null) { process.destroy(); }
			 */
		}
		return false;
	}

	/**
	 * 判断设备有无 SIM 卡
	 * 
	 * @return
	 */
	public static boolean checkSIM(Context context) {
		TelephonyManager tManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int simState = tManager.getSimState();
		if (simState == TelephonyManager.SIM_STATE_ABSENT
				|| simState == TelephonyManager.SIM_STATE_UNKNOWN) {
			return false;
		}
		return true;
	}

	/**
	 * root 权限静默安装 apk
	 * 
	 * @param apkPath
	 */
	public static boolean rootInstallApp(String apkPath) {
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su"); // 得到root 权限
			out = process.getOutputStream();
			out.write(("pm install -r " + apkPath + "\n").getBytes());// 调用安装
			out.flush();
			return true;
		} catch (IOException e) {
			System.out.println("root install:" + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/*
			 * if(process!=null) { process.destroy(); }
			 */
		}
		return false;
	}
	public static void appUpdate(Context context, String url) {
		HttpURLConnection httpURLConnection = null;
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			URL mURL = new URL(url);
			httpURLConnection = (HttpURLConnection) mURL.openConnection();
			httpURLConnection.setConnectTimeout(10000);
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			is = httpURLConnection.getInputStream();
			String path = AndroidUtils.getSdcardFile() + "/"
					+ context.getPackageName() + ".apk";
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);
			int i = 0;
			byte[] data = new byte[1024];
			while ((i = is.read(data)) != -1) {
				fos.write(data, 0, i);
			}
			fos.flush();
			if (context != null && !((Activity) context).isFinishing()) {
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				installIntent.setDataAndType(Uri.fromFile(file),
						"applicationnd.android.package-archive");
				context.startActivity(installIntent);
			}
		} catch (IOException e) {
			System.out.println("app update:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
			}
		}
		// getInstalledAppInfoByPackageName(context, packageName);

	}
	public static ArrayList<String> checkAppUpdate(Context context) {
		ArrayList<String> strings=new ArrayList<String>();
		String DOMAIN_NAME="http://www.91dongji.com";
		String packageName = context.getPackageName();
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			int versionCode = packageInfo.versionCode == 0 ? 1 : packageInfo.versionCode;
			String url = DOMAIN_NAME
					+ "/index.php?g=Api&m=Soft&a=upgrade&versioncode="
					+ versionCode + "&packagename=" + packageName;
//			String url="http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=upgrade&versioncode=1&packagename=cn.com.wali.walisms";
			HttpClientApi httpClientApi=HttpClientApi.getInstance();
			String result=httpClientApi.getContentFromUrl(url);
			if(!TextUtils.isEmpty(result)) {
				JSONObject jsonObject=new JSONObject(result);
				String downloadUrl=jsonObject.getString("down_url");
				String apk_versioncode=jsonObject.getString("apk_versioncode");
				String apk_date=jsonObject.getString("time");
				String website="http://www.91dongji.com/d/file/";
				if(!TextUtils.isEmpty(downloadUrl)) {
					strings.add(website+downloadUrl);
					strings.add(apk_versioncode);
					strings.add(apk_date);
				}
			}
			
		} catch (NameNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (JSONException e) {
			System.out.println(e);
		}
		return strings;
	}

	/**
	 * root 权限静默卸载应用
	 * 
	 * @param packageName
	 */
	public static void rootUninstallApp(String packageName) {
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su"); // 得到root 权限
			out = process.getOutputStream();
			out.write(("pm uninstall " + packageName + "\n").getBytes());// 调用安装
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/*
			 * if (process != null) { process.destroy(); }
			 */
		}
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (dpValue * mDisplayMetrics.density + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (pxValue / mDisplayMetrics.density + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (pxValue / mDisplayMetrics.scaledDensity);
	}

	/**
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int sp2px(Context context, float pxValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (pxValue * mDisplayMetrics.scaledDensity);
	}

	/**
	 * 根据应用名称判断该应用是否创建桌面快捷方式
	 * 
	 * @param context
	 * @param appName
	 * @return
	 */
	public static boolean hasShortCut(Context context, String appName) {
		int systemVersion = Build.VERSION.SDK_INT;
		boolean flag = false;
		String queryUrl = "";
		if (systemVersion < 8) {
			queryUrl = "content://com.android.launcher.settings/favorites?notify=true";
		} else {
			queryUrl = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(Uri.parse(queryUrl), null, "title=?",
				new String[] { appName }, null);
		if (cursor != null && cursor.moveToFirst()) {
			flag = true;
		}
		if (cursor != null) {
			cursor.close();
		}
		return flag;
	}

	/**
	 * 获取已安装软件列表
	 * 
	 * @param context
	 * @return
	 */
	public static List<PackageInfo> getInstalledPackages(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> infos = pm.getInstalledPackages(0);
		/*
		 * for(PackageInfo info : infos) { ApplicationInfo
		 * appInfo=info.applicationInfo; int n=ApplicationInfo.FLAG_SYSTEM;
		 * System
		 * .out.println(appInfo.loadLabel(pm)+", "+info.versionCode+", "+appInfo
		 * .flags); }
		 */
		return infos;
	}
	
	/**
	 * 获取安装包信息
	 * @param packageName
	 * @return
	 */
	public static PackageInfo getPackageInfo(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			return pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取状态栏信息
	 * 
	 * @param context
	 * @return
	 */
	public static Rect getStatusBarInfo(Activity context) {
		Rect frame = new Rect();
		context.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return frame;
	}

	/**
	 * 获取控件在屏幕上的坐标值,x、y保存在int[2]中
	 * 
	 * @param view
	 * @return
	 */
	public static int[] getViewLocation(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location;
	}

	/**
	 * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息 对于Android 2.3（Api Level
	 * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码
	 * 
	 * @param context
	 * @param packageName
	 */
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
	
	/**
	 * 获取手机应用上行与下行数据流量
	 * @param uid
	 * @return
	 */
	public static long getRxAndTxBytes(int uid) {
		long appRxBytes = TrafficStats.getUidRxBytes(uid);
		long appTxBytes = TrafficStats.getUidTxBytes(uid);
		return appRxBytes + appTxBytes;
	}
	
	/**
	 * 卸载软件方法
	 * 
	 * @param packageName
	 */
	public static void uninstallApp(Context context, String packageName) {
		Uri packageUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
	}
	
	/**
	 * 判断是否开机启动
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean canPowerboot(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED, null);
		List<ResolveInfo> list = pm.queryBroadcastReceivers(intent, 0);
		for (ResolveInfo resolveInfo : list) {
			if(packageName.equals(resolveInfo.activityInfo.packageName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取应用权限列表
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String[] getPermissionList(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		String[] permissionList = null;
		try {
			permissionList = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return permissionList;
	}
	
	/**
	 * 将Drawable转换为Bitmap
	 * @param res
	 * @return
	 */
	public static Bitmap drawable2Bitmap(Drawable drawable) {
		 // 取 drawable 的长宽
		 int w = drawable.getIntrinsicWidth();
		 int h = drawable.getIntrinsicHeight();
		 
		 // 取 drawable 的颜色格式
		 Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565;
		 // 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}
	
	/***
	 * 获取安装所有应用
	 * @param context
	 * @return
	 */
	 public static  ArrayList<ResolveInfo>  returnAllResolveInfo(Context context) {
  		Intent allApplicationMainIntent = new Intent(Intent.ACTION_MAIN, null);
  		allApplicationMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
  		PackageManager packageManager=context.getPackageManager();
  		List<ResolveInfo> all_apps = packageManager.queryIntentActivities(
  				allApplicationMainIntent, 0);
  		ArrayList<ResolveInfo> appInfoss=new ArrayList<ResolveInfo>();
  		for(int i=0;i<all_apps.size();i++)
  		{
  			ResolveInfo resolveInfo=all_apps.get(i);
  			appInfoss.add(resolveInfo);
  		}
  		
  		return appInfoss;
  	}
	
	/**
	 * 获取系统可用内存
	 * @param context
	 */
	public static long getAvailMemory(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		System.out.println("availMemory============>" + Formatter.formatFileSize(context, mi.availMem));// Byte转换为KB或者MB，内存大小规格化,并输出
		return mi.availMem;
	}
	
	/**
	 * 获取系统总内存
	 * @param context
	 */
	public static long getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";//系统内存文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;
		
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();//读取meminfo第一行，系统总内存大小
			
			arrayOfString = str2.split("\\s+");
//			for (String string : arrayOfString) {
//				System.out.print("num===>" + string + "\t");
//			}
			
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;//获得系统总内存，单位是kb,乘以1024转换成byte
			localBufferedReader.close();
			System.out.println("totalMemory========>" + Formatter.formatFileSize(context, initial_memory));// Byte转换为KB或者MB，内存大小规格化，并输出
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initial_memory;
	}
}
