package com.dongji.desktopswitch;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ADownloadThread implements Runnable {

	private ADownloadApkItem aDownloadApkItem;
	private Context context;

	public ADownloadThread(ADownloadApkItem aDownloadApkItem, Context context) {
		this.aDownloadApkItem = aDownloadApkItem;
		this.context = context;
	}

	@Override
	public void run() {
		HttpURLConnection conn = null;
		RandomAccessFile raf = null;
		InputStream is = null;
		URL apkUrl;
		try {
			apkUrl = new URL(aDownloadApkItem.apkUrl);

			if (apkUrl != null) {

				conn = (HttpURLConnection) apkUrl.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Range", "bytes="
						+ aDownloadApkItem.apkDownloadSize + "-");

				conn.connect();

				aDownloadApkItem.apkTotalSize = conn.getContentLength()
						+ aDownloadApkItem.apkDownloadSize;

				is = conn.getInputStream();

				if (is == null) {
					throw new IOException("inputStream is null!");
				}
				String filePath = SettingTools.getAbsolutePath(
						aDownloadApkItem.apkPackageName, "apk.temp");
				raf = new RandomAccessFile(filePath, "rw");
				raf.seek(aDownloadApkItem.apkDownloadSize);
				byte buf[] = new byte[4 * 1024];
				int readByte = 0;

				// boolean honeycombNetwork = NetTool.getNetWorkType(context) ==
				// 3;
				aDownloadApkItem.apkStatus = ADownloadService.STATUS_OF_DOWNLOADING;
				while ((readByte = is.read(buf)) != -1
						&& aDownloadApkItem.apkTotalSize > aDownloadApkItem.apkDownloadSize
						&& !ADownloadService.isStop) {
					raf.write(buf, 0, readByte);
					aDownloadApkItem.apkDownloadSize += readByte;
				}

				if (aDownloadApkItem.apkDownloadSize == aDownloadApkItem.apkTotalSize) {
					SettingTools.deleteLastSuffix(SettingTools.getAbsolutePath(
							aDownloadApkItem.apkPackageName, "apk.temp"));
					aDownloadApkItem.apkStatus = ADownloadService.STATUS_OF_DOWNLOADCOMPLETE;
				}
			}
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException:" + e);
		} catch (IOException e) {
			Log.e("important------", e.toString() + ", "
					+ aDownloadApkItem.apkDownloadSize + ", "
					+ aDownloadApkItem.apkTotalSize);
		} catch (Exception e) {
			System.out.println("Exception:" + e);
		} finally {
			try {
				if (null != raf) {
					raf.close();
				}
				if (null != is) {
					is.close();
				}

			} catch (IOException e) {
			}
			if (null != conn) {
				conn.disconnect();
			}
		}
	}
}
