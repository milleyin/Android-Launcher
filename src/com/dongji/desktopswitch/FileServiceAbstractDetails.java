package com.dongji.desktopswitch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

public abstract class FileServiceAbstractDetails implements
		FileServiceInterface {

	private static final String TAG = "FileServiceAbstractDetails";
	private static BitmapDownloaderTask task;
	static final BitmapFactory.Options mOptions = new BitmapFactory.Options();
	static {
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		mOptions.inPurgeable = true;
		mOptions.inInputShareable = true;
	}

	boolean cancelPotentialDownload(String url, ImageView imageview) {

		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageview);
		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	private BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageview) {
		if (imageview != null) {
			Drawable drawable = imageview.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	class DownloadedDrawable extends BitmapDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask,
				Bitmap defaultBitmap) {
			super(defaultBitmap);
			// 能随时取得某对象的信息，但又不想影响此对象的垃圾收集
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	private Bitmap downloadBitmap(String url) {
		Bitmap mBitmap = null;
		InputStream is=null;
		try {
			is=getBitmapInputStream(url);
			mBitmap = BitmapFactory.decodeStream(is, null, mOptions);
		} catch (OutOfMemoryError e) {
			System.out.println("downloadBitmap:"+e+", "+url);
			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
			}
			// QQLiveLog.d(TAG, "OutOfMemoryError:"+e.getMessage());
		} finally {
			if(is!=null) {
				try{
					is.close();
				}catch(IOException e) {
				}
			}
		}

		return mBitmap;
	}

	private InputStream getBitmapInputStream(String url) {
		URL newurl;
		try {
			newurl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) newurl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream(), 8192);
			// InputStream inputStream=conn.getInputStream();
			return bis;
		} catch (Exception e) {
			e.printStackTrace();
//			System.out.println("getBitmapInputStream:"+e);
		}
		return null;
	}
	
	String getHashCode(String url) {
		if (!TextUtils.isEmpty(url)) {
			return String.valueOf(url.hashCode());
		}
		return null;
	}

	void forceDownload(String url, ImageView imageView, Bitmap defaultBitmap) {
		if (url == null) {
			imageView.setImageBitmap(defaultBitmap);
			return;
		}
		
		if (cancelPotentialDownload(url, imageView)) {
			try {
				task = new BitmapDownloaderTask(imageView);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(
						task, defaultBitmap);
				imageView.setImageDrawable(downloadedDrawable);
				task.execute(url);
			} catch (RejectedExecutionException re) {
				imageView.setImageBitmap(defaultBitmap);
			}
		}
	}

	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		String url;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageview) {
			imageViewReference = new WeakReference<ImageView>(imageview);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return downloadBitmap(url);
		}

		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				if (isCancelled()) {
					bitmap = null;
				}
				saveBitmap(url, bitmap);
				if (imageViewReference != null) {
					ImageView imageview = imageViewReference.get();
					BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageview);
					if ((this == bitmapDownloaderTask)
							|| (mode != Mode.CORRECT)) {
						imageview.setImageBitmap(bitmap);
					}
				}
			}
		}
	}
}