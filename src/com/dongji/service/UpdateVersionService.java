package com.dongji.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.adw.launcher.Launcher;
import com.dongji.launcher.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

public class UpdateVersionService extends Service{
	//标题
	private String update_url ;
	//文件存储
	private File updateDir = null;
	private File updateFile = null;
	File apk_file;
	//通知�?
	private NotificationManager updateNotificationManager = null;
	private Notification updateNotification = null;
	//通知栏跳转Intent
	private Intent updateIntent = null;
	private PendingIntent updatePendingIntent = null;
	private final static int DOWNLOAD_COMPLETE = 0;
	private final static int DOWNLOAD_FAIL = 1;
	public static boolean IS_DOWNLOAD = false;
	private RemoteViews view = null;
	
	String sd_path; //sd卡目录
	String home_path; //parentManage根目录
	String app_name;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!Launcher.isDownLoadding)
		{
			if(intent!=null )
			{
				if(intent.getExtras()!=null)
				{
					//获取传
					update_url = intent.getExtras().getString("update_url");
					System.out.println("  update_url  --->" + update_url);
				    
				    //创建文件
				    sd_path = Environment.getExternalStorageDirectory().getPath();
				    home_path = sd_path+"/DongJi/"+ getPackageName() + ".apk";;
				    
					apk_file = new File(home_path);
					if(!apk_file.exists())
					{
						apk_file.mkdirs();
					}
					if(apk_file.exists())
					{
					  apk_file.delete();
					}
					

				    this.updateNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				    
				    updateNotification = new Notification(R.drawable.dongji_ico, "动机桌面正在下载", System.currentTimeMillis());
				    //设置下载过程中，点击通知栏，回到主界
				     updateIntent = new Intent(this, Launcher.class);
				    updatePendingIntent = PendingIntent.getActivity(this,0,updateIntent,0);
				    //设置通知栏显示内
				    view = new RemoteViews(getPackageName(), R.layout.version_update);
				    view.setImageViewResource(R.id.image, R.drawable.dongji_ico);
				    new Thread(new updateRunnable()).start();//这个是下载的重点，是下载的过
				}
				
			}
			
		}
		
	    return super.onStartCommand(intent, flags, startId);
	}
	private Handler updateHandler = new  Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        switch(msg.what){
	            case DOWNLOAD_FAIL:
	                //下载失败
	            	IS_DOWNLOAD=false;
	            	Launcher.isDownLoadding=false;
	                updateNotification.setLatestEventInfo(UpdateVersionService.this, "动机桌面", "下载失败", updatePendingIntent);
	                updateNotificationManager.notify(0, updateNotification);
	                break;
	            case 2:
	            	int updateTotalSize=msg.getData().getInt("updateTotalSize");
	            	int totalSize=msg.getData().getInt("totalSize");
	            	float result=totalSize*100/updateTotalSize;
	            	int p=(int)result;
	            	view.setProgressBar(R.id.pb, 100, p, false);
    				view.setTextViewText(R.id.tv, "进度" + p + "%");// 关键部分，如果你不重新更新通知，进度条是不会更新的
    				// 设置通知在状态栏显示的图标
    				updateNotification.icon = R.drawable.dongji_ico;
    				// 通知时发出的默认声音
    				updateNotification.defaults = Notification.FLAG_ONLY_ALERT_ONCE;
    				// 通过RemoteViews 设置notification中View 的属性
    				updateNotification.contentView = view;
    				updateNotification.contentIntent = updatePendingIntent;
    				
    				
    				// 这个可以理解为开始执行这个通知
    				Intent installIntent = new Intent(Intent.ACTION_VIEW);
    				// 这个可以理解为开始执行这个通知
    				try {
    					updateNotificationManager.notify(0, updateNotification);
					} catch (Exception e) {
						e.printStackTrace();
					}
    				if(p==100)
    				{
    					    IS_DOWNLOAD=false;
    					    Launcher.isDownLoadding=false;
    					    stopService(updateIntent);
    						Intent intent = new Intent(Intent.ACTION_VIEW);
	    		            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		            intent.setDataAndType(Uri.fromFile((apk_file)),"application/vnd.android.package-archive");
	    		            startActivity(intent);
	    		            
	    		            Uri uri = Uri.fromFile(apk_file);
    						installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
    		                updatePendingIntent = PendingIntent.getActivity(UpdateVersionService.this, 0, installIntent, 0);
    		                updateNotification.defaults = Notification.DEFAULT_SOUND;//铃声提醒 
    		                updateNotification.setLatestEventInfo(UpdateVersionService.this,  "动机桌面", "下载完成,点击安装", updatePendingIntent);
    		                updateNotificationManager.notify(0, updateNotification);
    	                    stopService(updateIntent);
    				}
    				
	            	break;
	            default:
	                stopService(updateIntent);
	                break;
	        }
	    }
	};
	class updateRunnable implements Runnable {
        Message message = updateHandler.obtainMessage();
        public void run() {
        	try {
				downloadUpdateFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				updateHandler.sendEmptyMessage(DOWNLOAD_FAIL);
			}
        }
    }
	public long downloadUpdateFile() throws Exception {
        //这样的下载代码很多，我就不做过多的说�?
        int downloadCount = 0;
        int currentSize = 0;
        int totalSize = 0;
        int updateTotalSize = 0;
        
        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        Launcher.isDownLoadding=true;
        try {
        	
            URL url = new URL(update_url);
            httpConnection = (HttpURLConnection)url.openConnection();
            httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            httpConnection.setRequestProperty("Accept-Language", "zh-CN");
            httpConnection.setRequestProperty("Charset", "UTF-8");
            httpConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            httpConnection.setRequestProperty("Connection", "Keep-Alive");
            if(currentSize > 0) {
                httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
            }
            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(20000);
            updateTotalSize = httpConnection.getContentLength();
            if (httpConnection.getResponseCode() == 404) {
                throw new Exception("fail!");
            }
            is = httpConnection.getInputStream();                   
            fos = new FileOutputStream(apk_file, false);
            byte buffer[] = new byte[1024];
            int readsize = 0;
            Message msg =null;
            while((readsize = is.read(buffer)) !=-1){
            	IS_DOWNLOAD=true;
                fos.write(buffer, 0, readsize);
                totalSize += readsize;
                //为了防止频繁的�?知导致应用吃紧，百分比增�?0才�?知一�?
                if((downloadCount == 0)||(int) (totalSize*100/updateTotalSize)-1>downloadCount){ 
                downloadCount += 1;
               msg = new Message();
                msg.what=2;
                msg.getData().putInt("totalSize", totalSize);
                msg.getData().putInt("updateTotalSize", updateTotalSize);
                updateHandler.sendMessage(msg);
                    
                  try {
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
                }                        
            }
            msg.getData().putInt("totalSize", updateTotalSize);
            
            fos.flush();
            
        } finally {
            if(httpConnection != null) {
                httpConnection.disconnect();
            }
            if(is != null) {
                is.close();
            }
            if(fos != null) {
                fos.close();
            }
        }
        return totalSize;
    }
	
}
