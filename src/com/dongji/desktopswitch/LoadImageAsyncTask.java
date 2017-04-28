package com.dongji.desktopswitch;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class LoadImageAsyncTask extends AsyncTask<String, Integer, Bitmap> {
	private ImageView imageView;

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (null != imageView) {
			imageView.setImageBitmap(bitmap);
		}
	}

	// 设置图片视图实例
	public void setImageView(ImageView image) {
		this.imageView = image;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = ApacheUtility.GetBitmapByUrl(params[0]); // 调用前面
																	// ApacheUtility
																	// 类的方法

		return bitmap;
	}
}
