package com.dongji.desktopswitch;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

/**
 * 下载service
 * 
 * @author 131
 * 
 */
public class ADownloadService extends Service {

	public static final String APKDOWNLOADITEM = "apkDownloadItem";

	/**
	 * 已安装
	 */
	public static final int STATUS_OF_INSTALLED = 0;
	/**
	 * 未安装
	 */
	public static final int STATUS_OF_UNINSTALLED = 1;
	/**
	 * /** 准备下载
	 */
	public static final int STATUS_OF_PREPAREDOWNLOAD = 2;
	/**
	 * 正在下载
	 */
	public static final int STATUS_OF_DOWNLOADING = 3;
	/**
	 * 下载完成
	 */
	public static final int STATUS_OF_DOWNLOADCOMPLETE = 4;

	private static ExecutorService executorService;
	public static ArrayList<ADownloadApkItem> downloadingAPKList = new ArrayList<ADownloadApkItem>();
	// public static ADownloadApkList ignoreAPKList = new ADownloadApkList();
	public static boolean isStop = false;
	private ADownloadThread aDownloadThread = null;
	private Context context;

	@Override
	public void onCreate() {
		context = getApplicationContext();

		// if (null == executorService) {
		//
		// }
		executorService = Executors.newFixedThreadPool(3);
		isStop = false;
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (null == intent) {
			return;
		}
		Bundle apkItemBundle = intent.getBundleExtra(APKDOWNLOADITEM);
		if (null != apkItemBundle) {
			final ADownloadApkItem aDownloadApkItem = (ADownloadApkItem) apkItemBundle
					.getParcelable(APKDOWNLOADITEM);
			if (null != aDownloadApkItem.apkUrl) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						downloading(aDownloadApkItem);
					}
				}).start();
			}
		}
		super.onStart(intent, startId);
	}

	private void initThread(ADownloadApkItem aDownloadApkItem) {
		aDownloadThread = new ADownloadThread(aDownloadApkItem, context);
		executorService.execute(aDownloadThread);
	}

	private void downloading(ADownloadApkItem aDownloadApkItem) {
		downloadingAPKList.add(aDownloadApkItem);
		initThread(aDownloadApkItem);
	}

	@Override
	public void onDestroy() {
		executorService.shutdownNow();
		isStop = true;
		downloadingAPKList.clear();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
