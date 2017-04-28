package com.dongji.tool;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dongji.adapter.TaskMngrAdapter;
import com.dongji.enity.InstalledAppInfo;
import com.dongji.enity.PowerbootRecord;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Debug.MemoryInfo;


public class LauncherUtils {
	/**
	 * 停止单个应用,2.2版本暂时无效
	 * @param context
	 * @param info
	 * @param handler
	 */
	public static void stopApp(Context context, InstalledAppInfo info) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		Method method = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
//		method.setAccessible(true);
//		method.invoke(am, packageName);
		
		/*无效
		am.killBackgroundProcesses(packageName);		//方法一
		 */        
		
		List<RunningAppProcessInfo> list = am.getRunningAppProcesses();	
		for (RunningAppProcessInfo runningAppProcessInfo : list) {
			if (runningAppProcessInfo.uid == info.getUid()) {
//				System.out.println("uid=====>" + info.getUid() + ", pid=====>" + runningAppProcessInfo.pid);
//				android.os.Process.killProcess(runningAppProcessInfo.pid);	//方法二
				am.restartPackage(info.getPkgName());		//方法三，2.2不可用
//				handler.sendEmptyMessage(TaskMngrAdapter.KILL_RUNNING_PROCESS);
				break;
			}
		}
		
	}
	
	public static void stopPowerBootApps(Context context, List<PowerbootRecord> powerbootList) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		List<RunningAppProcessInfo> list = am.getRunningAppProcesses();	
		for (PowerbootRecord record : powerbootList) {
			for (RunningAppProcessInfo runningProcessInfo : list) {
				System.out.println("uid_1====>" + runningProcessInfo.uid + "/uid_2=====>" + record.getUid());
				if (runningProcessInfo.uid == record.getUid()) {
					System.out.println("开机禁止项======>" + record.getPkgName() + ", description======>" + runningProcessInfo.describeContents());
					am.restartPackage(record.getPkgName());
				}
			}
		}
	}
	
	/**
	 * 一键清理除本软件及系统软件之外的所有进程,2.2版本暂时无效
	 * @param context
	 */
	public static void clearAppsOneClick(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningList = am.getRunningAppProcesses();
		List<InstalledAppInfo> installedList = getInstalledApp(context, FILTER_ALL_APP);
		for (RunningAppProcessInfo runningAppProcessInfo : runningList) {
			// 过滤掉系统进程及本应用进程
			if (runningAppProcessInfo.processName.indexOf("android") == -1
					&& runningAppProcessInfo.processName.indexOf(context
							.getPackageName()) == -1) {
				for (InstalledAppInfo installedAppInfo : installedList) {
					if (runningAppProcessInfo.uid == installedAppInfo.getUid()) {
						am.restartPackage(installedAppInfo.getPkgName());
						break;
					}

				}
			}
		}
	}
	
	/**
	 * 一键清除选中进程
	 * @param context
	 * @param chosenList
	 */
	public static void clearChosenApps(Context context, List<InstalledAppInfo> chosenList) {
		if (chosenList != null && chosenList.size() > 0) {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (InstalledAppInfo installedAppInfo : chosenList) {
				System.out.println("killed process =======>" + installedAppInfo.getName());
				am.restartPackage(installedAppInfo.getPkgName());
			}
		}
	}
	
	public static String sizeFormat(long size) {
		if ((float) size / 1024 > 1024) {
			float size_mb = (float) size / 1024 / 1024;
			return String.format("%.2f", size_mb) + "M";
		}
		return size / 1024 + "K";
	}
	
	public static final int FILTER_ALL_APP = 0;			//所有应用
	public static final int FILTER_SYSTEM_APP = 1;		//系统应用
	public static final int FILTER_THIRD_APP = 2;		//第三方应用
	public static final int FILTER_SDCARD_APP = 3;		//安装在sdcard中的应用
	/**
	 * 根据过滤条件筛选我们需要的应用
	 * @param context
	 * @param filter
	 * @return
	 */
	public static List<InstalledAppInfo> getInstalledApp(Context context, int categoryFilter) {
//		System.out.println("context packageName=======>" + context.getPackageName());
		List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> listPackages = pm.getInstalledPackages(0);
//		List<ApplicationInfo> listApplications = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
//		Collections.sort(listApplications, new ApplicationInfo.DisplayNameComparator(pm)); //排序
		switch (categoryFilter) {
		case FILTER_ALL_APP:		//所有应用
			for (PackageInfo pInfo : listPackages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;

				installedAppInfo.setIcon(info.loadIcon(pm));
				installedAppInfo.setUid(info.uid);
				installedAppInfo.setAppInfo(info);
				installedAppInfo.setName(delInvisibleChar(info.loadLabel(pm).toString().trim()));
				installedAppInfo.setVersionName(pInfo.versionName);
				installedAppInfo.setVersionCode(pInfo.versionCode);
				installedAppInfo.setClassName(info.className);
				installedAppInfo.setPkgName(info.packageName);
				installedAppInfo.setProcessName(info.processName);
				// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
				// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
				String dir = info.publicSourceDir;
				int size = Integer.valueOf((int) new File(dir).length());
				installedAppInfo.setSize(sizeFormat(size));
				list.add(installedAppInfo);
			}
			break;
		case FILTER_SYSTEM_APP:			//系统应用
			for (PackageInfo pInfo : listPackages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;

				// 显示系统程序
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					installedAppInfo.setIcon(info.loadIcon(pm));
					installedAppInfo.setUid(info.uid);
					installedAppInfo.setAppInfo(info);
					installedAppInfo.setName(info.loadLabel(pm).toString());
					installedAppInfo.setVersionName(pInfo.versionName);
					installedAppInfo.setVersionCode(pInfo.versionCode);
					installedAppInfo.setClassName(info.className);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.setProcessName(info.processName);
					installedAppInfo.setFlag(1);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					list.add(installedAppInfo);
				}
			}
			break;
		case FILTER_THIRD_APP:			//第三方应用
			for (PackageInfo pInfo : listPackages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;

				// 显示第三方应用程序.如果原来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
				if (((info.flags & ApplicationInfo.FLAG_SYSTEM) <= 0
						|| (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) && !info.packageName.equals("com.dongji.launcher")) {
					installedAppInfo.setIcon(info.loadIcon(pm));
					installedAppInfo.setUid(info.uid);
					installedAppInfo.setAppInfo(info);
					installedAppInfo.setName(info.loadLabel(pm).toString());
					installedAppInfo.setVersionName(pInfo.versionName);
					installedAppInfo.setVersionCode(pInfo.versionCode);
					installedAppInfo.setClassName(info.className);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.setProcessName(info.processName);
					installedAppInfo.setFlag(3);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					list.add(installedAppInfo);
				}
			}
			break;
		case FILTER_SDCARD_APP:			//sdCard中的应用
			for (PackageInfo pInfo : listPackages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;
				
				// 显示用户安装应用，而不显示系统程序
				if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
					installedAppInfo.setIcon(info.loadIcon(pm));
					installedAppInfo.setUid(info.uid);
					installedAppInfo.setAppInfo(info);
					installedAppInfo.setName(info.loadLabel(pm).toString());
					installedAppInfo.setVersionName(pInfo.versionName);
					installedAppInfo.setVersionCode(pInfo.versionCode);
					installedAppInfo.setClassName(info.className);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.setProcessName(info.processName);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					list.add(installedAppInfo);
				}
			}
			break;
		default:
			break;
		}
		return list;
	}
	
	/**
	 * 删除字符串中前面不可见的字符(Unicode编码在区间[128,160]内的字符)
	 * @param str 原始字符串
	 * @return
	 */
	public static String delInvisibleChar(String str) {
		StringBuffer sb = new StringBuffer(str);
		for (int i = 0; i < str.length(); i++) {
			int nuIndex = (int)str.charAt(i);
			if (nuIndex >= 128 && nuIndex <= 160) {
				sb.deleteCharAt(0);
				continue;
			}
			break;
		}
		return sb.toString();
	}
	
	/**
	 * 取得正在运行的应用信息，包括占用内存情况
	 * @param context
	 * @param flag
	 * @return
	 */
	public static List<InstalledAppInfo> getRunningApps(Context context, int flag) {
		ActivityManager actMngr = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppList = actMngr.getRunningAppProcesses();
		PackageManager pkgMngr = context.getPackageManager();
		List<InstalledAppInfo> list = getInstalledApp(context, flag);
		List<InstalledAppInfo> runningList = new ArrayList<InstalledAppInfo>();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppList) {
			for (InstalledAppInfo installedAppInfo : list) {
				if (runningAppProcessInfo.processName.equals(installedAppInfo.getProcessName())) {
					int[] pids = new int[]{runningAppProcessInfo.pid};
					MemoryInfo[] memInfos = actMngr.getProcessMemoryInfo(pids);
					installedAppInfo.setUsageMemory(memInfos[0].dalvikPss + memInfos[0].nativePss + memInfos[0].otherPss);
//					long cacheVal = AndroidUtils.getFileSize2(new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + installedAppInfo.getPkgName() + "/cache/"));
//					System.out.println("packe name====>"+ installedAppInfo.getPkgName() +", cache value ======>" + cacheVal);
//					installedAppInfo.setCacheVal(cacheVal);
					runningList.add(installedAppInfo);
					break;
				}
			}
		}
		return runningList;
	}
	
	public static void getAppsUsageMemory(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningProcessList = am.getRunningAppProcesses();
		int[] pids = new int[runningProcessList.size()];
		for (int i = 0; i < runningProcessList.size(); i++) {
			pids[i] = runningProcessList.get(i).pid;
		}
		MemoryInfo[] memInfos = am.getProcessMemoryInfo(pids);
		int j = 0;
		for (MemoryInfo memoryInfo : memInfos) {
			System.out.println(j + ", totalPss =========>" + memoryInfo.getTotalPss());
			System.out.println(j + ", total private dirty =========>" + memoryInfo.getTotalPrivateDirty());
			System.out.println(j + ", total shared dirty =========>" + memoryInfo.getTotalSharedDirty());
			System.out.println(j + ", dalvikPss =========>" + memoryInfo.dalvikPss);
			System.out.println(j + ", nativePss =========>" + memoryInfo.nativePss);
			System.out.println(j + ", nativeSharedDirty =========>" + memoryInfo.nativeSharedDirty);
			System.out.println(j++ + ", otherPss =========>" + memoryInfo.otherPss);
		}
	}
	
	public static void orderWithName(List<InstalledAppInfo> list) {
		Comparator<InstalledAppInfo> comparator = new Comparator<InstalledAppInfo>() {

			@Override
			public int compare(InstalledAppInfo lhs, InstalledAppInfo rhs) {
				
				Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);
				String str1 = delInvisibleChar(lhs.getName().trim());
				String str2 = delInvisibleChar(rhs.getName().trim());
				if (myCollator.compare(str1, str2) > 0) {
					return 1;
				} else if (myCollator.compare(str1, str2) < 0) {
					return -1;
				} else {
					return 0;
				}
			}

		};
//		for (InstalledAppInfo installedAppInfo : list) {
//			System.out.println("order before====>" + installedAppInfo.getName());
//		}
		Collections.sort(list, comparator);
//		for (InstalledAppInfo installedAppInfo : list) {
//			System.out.println("order after====>" + (int)installedAppInfo.getName().charAt(0));
//		}
	}
}
