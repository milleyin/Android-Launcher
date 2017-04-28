package com.dongji.desktopswitch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;



/**
 * 
 * @author zhangkai
 *
 */
public class HttpClientApi {
	private static HttpClientApi httpClientApi;
	private static final int CONNECT_TIME_OUT = 10000;
	private static final int SO_TIME_OUT = 10000;
	private static final String DEFAULT_CHARSET = "utf-8";
	
	private HttpParams httpParams;
	
	private HttpClientApi() {
		super();
		httpParams=new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECT_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIME_OUT);
	}
	
	public static synchronized HttpClientApi getInstance() {
		if(httpClientApi==null) {
			httpClientApi=new HttpClientApi();
		}
		return httpClientApi;
	}
	
	private HttpClient getHttpClient() {
		return new DefaultHttpClient(httpParams);
	}
	
	public String getContentFromUrl(String url) throws IOException {
		String result=null;
		HttpClient httpClient=getHttpClient();
		HttpGet httpGet=new HttpGet(url);
		HttpResponse httpResponse=httpClient.execute(httpGet);
		HttpEntity httpEntity=httpResponse.getEntity();
		if(httpEntity!=null) {
			result=EntityUtils.toString(httpEntity, DEFAULT_CHARSET); //
			httpEntity.consumeContent();
		}
		return result;
	}
	
	public String getContentFromUrlByPost(String url, List<String[]> list) throws IOException {
		System.out.println("++++++++++++++++getContentFromUrlByPost++++++++++++++++"+list.size());
		HttpPost httpPost=new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for(int i=0;i<list.size();i++) {
			String[] arr=list.get(i);
//			System.out.println(arr[0]+", "+arr[1]);
			sb.append("{\"apk_versioncode\":\"").append(arr[0]).append("\",\"apk_packagename\":\"").append(arr[1]).append("\"}");
			if(i<list.size()-1) {
				sb.append(",");
			}
		}
		sb.append("]");
		params.add(new BasicNameValuePair("updates", sb.toString()));
		HttpEntity httpentity;
		try {
			httpentity = new UrlEncodedFormEntity(params, DEFAULT_CHARSET);
			httpPost.setEntity(httpentity);  
			// 取得默认的HttpClient  
			HttpClient httpclient = new DefaultHttpClient();  
			// 取得HttpResponse  
			HttpResponse httpResponse = httpclient.execute(httpPost);  
			// HttpStatus.SC_OK表示连接成功  
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
			// 取得返回的字符串  
				String strResult = EntityUtils.toString(httpResponse.getEntity(), DEFAULT_CHARSET);  
				System.out.println("================== update result:"+strResult);
				return strResult;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		return null;
	}
	
	public boolean postCloundBackup(String url, List<String[]> list, String userSessionId, Context context) throws IOException {
		HttpPost httpPost=new HttpPost(url);//+"&userssionid="+userSessionId
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for(int i=0;i<list.size();i++) {
			String[] arr=list.get(i);
//			System.out.println(arr[0]+", "+arr[1]);
			sb.append("{\"apk_versioncode\":\"").append(arr[0]).append("\",\"apk_packagename\":\"").append(arr[1]).append("\"}");
			if(i<list.size()-1) {
				sb.append(",");
			}
		}
		sb.append("]");
		params.add(new BasicNameValuePair("market_username", userSessionId));
		params.add(new BasicNameValuePair("backups", sb.toString()));
		HttpEntity httpentity;
		try {
			httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(httpentity);
			// 取得默认的HttpClient  
			HttpClient httpclient = new DefaultHttpClient();  
			// 取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpPost);
			// HttpStatus.SC_OK表示连接成功  
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// 取得返回的字符串
				HttpEntity httpEntity=httpResponse.getEntity();
				if(httpEntity!=null) {
					String result=EntityUtils.toString(httpEntity, DEFAULT_CHARSET);
					
					System.out.println("cloud backup result:"+result);
					httpEntity.consumeContent();
					int num=0;
					try{
						num=Integer.valueOf(result);
						if(num==1) {
							return true;
						}
					}catch(NumberFormatException e) {
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} 
		return false;
	}
	
	public String postCloundRestore(String url, String value) throws IOException {
		HttpPost httpPost=new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("market_username", value));
		HttpEntity httpentity;
		try {
			httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(httpentity);
			// 取得默认的HttpClient  
			HttpClient httpclient = new DefaultHttpClient();  
			// 取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpPost);
			// HttpStatus.SC_OK表示连接成功  
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// 取得返回的字符串
				HttpEntity httpEntity=httpResponse.getEntity();
				if(httpEntity!=null) {
					String result=EntityUtils.toString(httpEntity, DEFAULT_CHARSET);
					
					System.out.println("cloud restore result:" + result);
					httpEntity.consumeContent();
					return result;
				}
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println(e);
		}
		return null;
	}
}
