package com.dongji.receiver;

import java.util.ArrayList;

import org.adw.launcher.Launcher;

import com.dongji.service.UpdateVersionService;
import com.dongji.tool.AndroidUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


public class VersionUpdateTime extends BroadcastReceiver {
	Context context;
	private MyHandler myHandler = new MyHandler();
	String update_url;
		@Override
		public void onReceive(Context context, Intent intent) {
			this.context=context;
			checkUpdate(context);
			
		}

		public void checkUpdate(final Context context) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
								if (AndroidUtils.isNetworkAvailable(context)) {
									ArrayList<String> strings = AndroidUtils
											.checkAppUpdate(context);
									if (!(strings.size() == 0)) {
										String download_url = strings.get(0);
										if (download_url != null
												&& !"".equals(download_url)) {
											update_url = download_url;
											myHandler.sendEmptyMessage(7);
										}
									}
								}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
		}
		private class MyHandler extends Handler {
			@Override
			public void handleMessage(Message msg) {
				try {

					switch (msg.what) {
					case 7: // 如果有版本更新
						if (!UpdateVersionService.IS_DOWNLOAD) {
							downloadUpdatedApp();
						} else {
							Toast.makeText(context, "动机桌面正在后台下载，请稍后",
									Toast.LENGTH_SHORT).show();
						}
						break;
					default:
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void downloadUpdatedApp() {
			if (AndroidUtils.isSdcardExists()) // sd卡是否可用
			{
				if(!Launcher.isDownLoadding)
				{
					Intent updateIntent = new Intent(context,
							UpdateVersionService.class);
					updateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					updateIntent.putExtra("update_url", update_url);
					context.startService(updateIntent);
				}
			} else {
				Toast.makeText(context, "sd card faild", Toast.LENGTH_SHORT).show();
			}
		}
  
}
