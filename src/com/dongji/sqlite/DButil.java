package com.dongji.sqlite;

import android.content.Context;

/**
 * 数据库单例模式
 * @author Administrator
 *
 */

public class DButil {

	public static MyDatabaseUtil myDatabaseUtil;
	
	public static MyDatabaseUtil getInstance(Context context)
	{
		if(myDatabaseUtil==null)
		{
			myDatabaseUtil = new MyDatabaseUtil(context);
			myDatabaseUtil.open();
		}
		return myDatabaseUtil;
	}
	
	public static void close()
	{
		try {
			if(myDatabaseUtil != null)
			{
				myDatabaseUtil.close();
				myDatabaseUtil = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
