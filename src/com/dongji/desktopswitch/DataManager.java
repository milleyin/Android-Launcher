package com.dongji.desktopswitch;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

public class DataManager {
	private static DataManager dataManager;
	
//	private String bannerUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/banner.json";
//	private String appsUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/apps.json";
//	private String gemasUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/games.json";
//	private String searchUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/search.json";
//	private String updateUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/update.json";
//	private String searchDropUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/search_drop.json";
//	private String cloudRecoverUrl="http://apptest.uni.me:8002/MarketDataService/WebRoot/data/cloudrecover.json";
	
	private static final String DOMAIN_NAME = "http://www.91dongji.com/";//http://192.168.0.106/
	private static final String NAV_URL = "json/nav_pad.txt";
	private static final String APP_CHANNEL_URL = "json/app.txt";
	private static final String UPDATE_URL = "index.php?g=Api&m=Soft&a=softUpdate"; //http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=softUpdate
	private static final String SEARCH_RESULT_URL = "index.php?g=Api&m=Soft&a=softSearch&param=";  //http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=softSearch&param=
	private static final String STATISTICS_INSTALL = "index.php?g=Api&m=AppCount&a=writeIn&catid=";
	private static final String BANNER_URL="index.php?g=Api&m=Soft&a=bannerShow"; //http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=bannerShow
	private static final String CLOUND_BACKUP_URL = "index.php?g=Api&m=Soft&a=userBackup";  //http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=userBackup
	private static final String CLOUND_RECOVER_URL = "index.php?g=Api&m=Soft&a=userRestoration";  //http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=userRestoration 
	private static final String HOTWORD_URL="index.php?g=Api&m=Soft&a=displayhotword";  //http://192.168.0.101/wuxiuwu/index.php?g=Api&m=Soft&a=displayhotword
	
	private static final String ONLINE_DOMAIN_NAME="http://dl.91dongji.com/file/"; //http://www.91dongji.com/app/d/file/
	
	private static final String ONLINE_STATIC_DOMAIN_NAME = "http://www.91dongji.com/";  //http://tools.mille.us:8888/
	
	private static final String NAVIGATION_NAME = "navigation";
	
	
	private DataManager() {
		super();
	}
	
	public static DataManager newInstance() {
		if(dataManager==null) {
			dataManager=new DataManager();
		}
		return dataManager;
	}
	
	/**
	 * 获取导航数据
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public ArrayList<NavigationInfo> getNavigationList() throws JSONException {
		ArrayList<NavigationInfo> list=null;
		HttpClientApi httpClientApi=HttpClientApi.getInstance();
		String result = null;
		boolean isLocal=false;
		try{
			result = httpClientApi.getContentFromUrl(DOMAIN_NAME + NAV_URL);//DOMAIN_NAME
		}catch(IOException e) {
			System.out.println("getnavigation "+e);
			result = FsCache.getCacheString(NAVIGATION_NAME);
			isLocal=true;
		}
		if(!TextUtils.isEmpty(result)) {
			JSONArray jsonArray=new JSONArray(result);
			int length=jsonArray.length();
			if(jsonArray!=null && length>0) {
				list=new ArrayList<NavigationInfo>();
				for(int i=0;i<length;i++) {
					NavigationInfo info=new NavigationInfo();
					JSONObject jsonObject=jsonArray.getJSONObject(i);
					info.id=jsonObject.getInt("catid");
					info.name=jsonObject.getString("catname");
					JSONObject childJsonObject=jsonObject.getJSONObject("datalist");
					info.staticAddress=new StaticAddress[2];
					StaticAddress gameStaticAddress=new StaticAddress();
					gameStaticAddress.url=childJsonObject.getString("game_url");
					gameStaticAddress.md5Value=childJsonObject.getString("game_md5");
					StaticAddress appStaticAddress=new StaticAddress();
					appStaticAddress.url=childJsonObject.getString("soft_url");
					appStaticAddress.md5Value=childJsonObject.getString("soft_md5");
					
					info.staticAddress[0]=appStaticAddress;
					info.staticAddress[1]=gameStaticAddress;
					list.add(info);
				}
			}
			if(!isLocal) {
				FsCache.cacheFileByMd5(result, NAVIGATION_NAME);
			}
		}
		return list;
	}
	
	public ArrayList<ApkItem> getApps(Context context, NavigationInfo currentInfo, boolean isApp) throws JSONException {
		ArrayList<ApkItem> list=null;
		int position=isApp?0:1;
		StaticAddress currentSd=(StaticAddress)currentInfo.staticAddress[position];
		String md5Value=currentSd.md5Value;
		String suffixUrl=currentSd.url;
		String result=FsCache.getCacheString(md5Value);
		boolean isLocal=false;
		if(TextUtils.isEmpty(result)) {
			HttpClientApi httpClientApi=HttpClientApi.getInstance();
			try{
				result = httpClientApi.getContentFromUrl(DOMAIN_NAME
						+ suffixUrl);
			} catch (IOException e) {
				isLocal=true;
//				result=FsCache.getCacheString(md5Value);
				System.out.println("getApps:"+e);
			}
		}else {
			isLocal=true;
		}
		if(!TextUtils.isEmpty(result)) {
			JSONArray jsonArray=new JSONArray(result);
			if(jsonArray!=null && jsonArray.length()>0) {
				list=new ArrayList<ApkItem>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject=jsonArray.getJSONObject(i);
					ApkItem item=new ApkItem();
					item.appId=jsonObject.getInt("id");
					item.category=jsonObject.getInt("catcid");
//					item.classx=jsonObject.getInt("catpid");
					String language=jsonObject.getString("language");
					if(TextUtils.isEmpty(language)) {
						item.language=1;
					}else {
						item.language=Integer.parseInt(language);
					}
					item.company=jsonObject.getString("developer");
					String apkUrl=jsonObject.getString("down_url");
					if(!TextUtils.isEmpty(apkUrl)) {
//						item.apkUrl = ONLINE_DOMAIN_NAME + apkUrl;
						item.apkUrl = ONLINE_STATIC_DOMAIN_NAME + apkUrl;
					}
					item.downloadNum=jsonObject.getLong("down_count");
					String iconUrl=jsonObject.getString("apk_icon");
					
					if(!TextUtils.isEmpty(iconUrl)) {
						item.appIconUrl = ONLINE_DOMAIN_NAME + iconUrl;
					}
					item.appName=jsonObject.getString("apk_name");
					item.fileSize=jsonObject.getLong("apk_size");
					item.versionCode=jsonObject.getInt("apk_versioncode");
					item.version=jsonObject.getString("apk_versionname");
					item.packageName=jsonObject.getString("apk_packagename");
					list.add(item);
				}
				
//				MarketDatabase db=new MarketDatabase(context);
//				NavigationInfo saveInfo = db.getNavigationByNavId(currentInfo.id);
//				if(saveInfo==null) {
//					FsCache.cacheFileByMd5(result, md5Value);
//					db.addNavigation(currentInfo);
//				}else {
//					StaticAddress saveSd=(StaticAddress)saveInfo.staticAddress[position];
//					if(!currentSd.md5Value.equals(saveSd.md5Value)) {
//						FsCache.deleteCacheFileByMd5Value(saveSd.md5Value);
//						FsCache.cacheFileByMd5(result, md5Value);
//						db.updateNavigation(currentInfo, isApp);
//						System.out.println("cache File change!");
//					}
//				}
			}
			if(!isLocal) {
				FsCache.cacheFileByMd5(result, md5Value);
			}
		}
		return list;
	}
}
