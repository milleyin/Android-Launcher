package com.dongji.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.dongji.enity.SortEntity;

public class DrawerDatabase {
	private static final String DB_NAME="drawer_db.db";
	private static final int DB_VERSION = 1;
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase sqLiteDatabase;
	
	
	private static final String SORT_TABLE="sortTable";  // 排序表
	private static final String _ID="sortId";
	private static final String ITEM_TYPE="item_type";  // 类型，用于区分应用和文件夹
	private static final String PACKAGE_NAME="package_name";  // 包名
	private static final String OPEN_TIME="open_time";   // 最近打开时间
	private static final String OPEN_NUM="open_num";   // 打开次数
	private static final String VISIBLE="visible";  // 是否可见
	private static final String APP_NAME="app_name";  // 应用名称
	
	private static final String CREATE_SORT_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ SORT_TABLE
			+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ _ID
			+ " LONG, "
			+ ITEM_TYPE
			+ " INTEGER, "
			+ PACKAGE_NAME
			+ " TEXT, "
			+ OPEN_TIME
			+ " LONG, "
			+ OPEN_NUM
			+ " LONG, "
			+ VISIBLE
			+ " INTEGER, " + APP_NAME + " TEXT);";
	
	private static final String INSTALL_LIMIT = "20";
	private static final String INSTALL_TABLE="installTable";  // 安装表
	private static final String CREATE_INSTALL_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ INSTALL_TABLE
			+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ PACKAGE_NAME + " TEXT, " + APP_NAME + " TEXT);";
	
	private static class DatabaseHelper extends SQLiteOpenHelper{
		private Context context;
		
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.context=context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_SORT_TABLE);
			db.execSQL(CREATE_INSTALL_TABLE);
			loadData(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+SORT_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+CREATE_INSTALL_TABLE);
			onCreate(db);
		}
		
		private void loadData(SQLiteDatabase db) {
			PackageManager pm=context.getPackageManager();
			Intent allApplicationMainIntent = new Intent(Intent.ACTION_MAIN, null);
     		allApplicationMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
     		List<ResolveInfo> all_apps = pm.queryIntentActivities(
     				allApplicationMainIntent, 0);
     		ContentValues values=new ContentValues();
     		try{
	     		for(ResolveInfo info : all_apps) {
	     			values.clear();
					values.put(ITEM_TYPE, 0);
					values.put(PACKAGE_NAME, info.activityInfo.packageName);
					ComponentName cn = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
					ActivityInfo aInfo = pm.getActivityInfo(cn, 0);
					values.put(APP_NAME, aInfo.loadLabel(pm).toString());
					db.insert(SORT_TABLE, null, values);
				}
     		}catch(Exception e) {
     			System.out.println("--------------------"+e);
     		}finally {
     			if(db!=null && db.isOpen()) {
//     				db.close();
     			}
     		}
		}
	}
	
	public DrawerDatabase(Context context) {
		dbHelper=new DatabaseHelper(context);
	}
	
	public void addOrUpdateOpenData(long id, int itemType, String packageName, String name) {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			if(TextUtils.isEmpty(packageName)) {
				cursor=sqLiteDatabase.query(SORT_TABLE, new String[]{OPEN_NUM}, _ID+"=?", new String[]{String.valueOf(id)}, null, null, null);
			}else {
				cursor=sqLiteDatabase.query(SORT_TABLE, new String[]{OPEN_NUM}, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName, name}, null, null, null);
			}
			ContentValues values=new ContentValues();
			if(cursor.getCount()>0) {
				cursor.moveToFirst();
				long openNumber=cursor.getLong(cursor.getColumnIndex(OPEN_NUM));
				values.put(OPEN_NUM, ++openNumber);
				values.put(OPEN_TIME, System.currentTimeMillis());
				if(TextUtils.isEmpty(packageName)) {
					sqLiteDatabase.update(SORT_TABLE, values, _ID+"=?", new String[]{String.valueOf(id)});
				}else {
					sqLiteDatabase.update(SORT_TABLE, values, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName, name});
				}
			}else {
				values.put(_ID, id);
				values.put(ITEM_TYPE, itemType);
//				values.put(OPEN_TIME, System.currentTimeMillis());
//				values.put(OPEN_NUM, 1);
				if(!TextUtils.isEmpty(packageName)) {
					values.put(PACKAGE_NAME, packageName);
					values.put(APP_NAME, name);
				}
				sqLiteDatabase.insert(SORT_TABLE, null, values);
			}
		}catch(SQLiteException e) {
		}finally {
			release(cursor, sqLiteDatabase);
		}
	}
	
	public void updateSortById(long id) {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			cursor=sqLiteDatabase.query(SORT_TABLE, new String[]{OPEN_NUM}, _ID+"=?", new String[]{String.valueOf(id)}, null, null, null);
			if(cursor.getCount()>0) {
				cursor.moveToFirst();
				long openNumber=cursor.getLong(cursor.getColumnIndex(OPEN_NUM));
				ContentValues values=new ContentValues();
				values.put(OPEN_NUM, ++openNumber);
				values.put(OPEN_TIME, System.currentTimeMillis());
				sqLiteDatabase.update(SORT_TABLE, values, _ID+"=?", new String[]{String.valueOf(id)});
			}
		}catch(SQLiteException e) {
		} finally {
			release(cursor, sqLiteDatabase);
		}
	}
	
	public void updateSortById(String packageName, String name) {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			cursor=sqLiteDatabase.query(SORT_TABLE, new String[]{OPEN_NUM}, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName, name}, null, null, null);
			if(cursor.getCount()>0) {
				cursor.moveToFirst();
				long openNumber=cursor.getLong(cursor.getColumnIndex(OPEN_NUM));
				ContentValues values=new ContentValues();
				values.put(OPEN_NUM, ++openNumber);
				values.put(OPEN_TIME, System.currentTimeMillis());
				sqLiteDatabase.update(SORT_TABLE, values, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName});
			}
		}catch(SQLiteException e) {
		} finally {
			release(cursor, sqLiteDatabase);
		}
	}
	
	public List<SortEntity> getSortList(boolean isOpenNumOrderBy) {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getReadableDatabase();
			String orderBy=null;
			if(isOpenNumOrderBy) {
				orderBy=OPEN_NUM+" desc";
			}else {
				orderBy=OPEN_TIME+" desc";
			}
			cursor=sqLiteDatabase.query(SORT_TABLE, null, null, null, null, null, orderBy);
			if(cursor.getCount()>0) {
				cursor.moveToFirst();
				List<SortEntity> list=new ArrayList<SortEntity>();
				while(!cursor.isAfterLast()) {
					SortEntity entity=new SortEntity();
					entity.id=cursor.getLong(cursor.getColumnIndex(_ID));
					entity.itemType=cursor.getInt(cursor.getColumnIndex(ITEM_TYPE));
					entity.packageName=cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
					entity.openTime=cursor.getLong(cursor.getColumnIndex(OPEN_TIME));
					
					entity.openNumber=cursor.getLong(cursor.getColumnIndex(OPEN_NUM));
					entity.visible=cursor.getInt(cursor.getColumnIndex(VISIBLE));
					entity.appName=cursor.getString(cursor.getColumnIndex(APP_NAME));
					if(isOpenNumOrderBy) {
						if(entity.openNumber!=0) {
							list.add(entity);
						}
					}else {
						if(entity.openTime!=0) {
							list.add(entity);
						}
					}
					cursor.moveToNext();
				}
				return list;
			}
		}catch(SQLiteException e) {
			
		}finally {
			release(cursor, sqLiteDatabase);
		}
		return null;
	}
	
	public SortEntity getSortEntityByPackageName(String packageName, String name) {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getReadableDatabase();
			cursor=sqLiteDatabase.query(SORT_TABLE, null, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName, name}, null, null, null);
			if(cursor.getCount()>0) {
				cursor.moveToFirst();
				SortEntity entity=new SortEntity();
				entity.id=cursor.getLong(cursor.getColumnIndex(_ID));
				entity.itemType=cursor.getInt(cursor.getColumnIndex(ITEM_TYPE));
				entity.packageName=cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
				entity.appName=cursor.getString(cursor.getColumnIndex(APP_NAME));
				entity.openTime=cursor.getLong(cursor.getColumnIndex(OPEN_TIME));
				entity.openNumber=cursor.getLong(cursor.getColumnIndex(OPEN_NUM));
				entity.visible=cursor.getInt(cursor.getColumnIndex(VISIBLE));
				return entity;
			}
		}catch(SQLiteException e) {
		}finally {
			release(cursor, sqLiteDatabase);
		}
		return null;
	}
	
	public List<SortEntity> getSortPackageList() {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getReadableDatabase();
			String orderBy=OPEN_TIME+" desc";
			cursor=sqLiteDatabase.query(SORT_TABLE, null, PACKAGE_NAME+" NOT NULL", null, null, null, orderBy);
			if(cursor.getCount()>0) {
				cursor.moveToFirst();
				List<SortEntity> list=new ArrayList<SortEntity>();
				while(!cursor.isAfterLast()) {
					SortEntity entity=new SortEntity();
					entity.id=cursor.getLong(cursor.getColumnIndex(_ID));
					entity.itemType=cursor.getInt(cursor.getColumnIndex(ITEM_TYPE));
					entity.packageName=cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
					entity.appName=cursor.getString(cursor.getColumnIndex(APP_NAME));
					entity.openTime=cursor.getLong(cursor.getColumnIndex(OPEN_TIME));
					entity.openNumber=cursor.getLong(cursor.getColumnIndex(OPEN_NUM));
					entity.visible=cursor.getInt(cursor.getColumnIndex(VISIBLE));
					list.add(entity);
					cursor.moveToNext();
				}
				return list;
			}
		}catch(SQLiteException e) {
			
		}finally {
			release(cursor, sqLiteDatabase);
		}
		return null;
	}
	
	public void setVisible(long id, int visible) {
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			ContentValues values=new ContentValues();
			values.put(VISIBLE, visible);
			sqLiteDatabase.update(SORT_TABLE, values, _ID+"=?", new String[]{String.valueOf(id)});
		}catch(SQLiteException e) {
		}finally {
			release(sqLiteDatabase);
		}
	}
	
	public void setVisible(String packageName, int visible, String name) {
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			ContentValues values=new ContentValues();
			values.put(VISIBLE, visible);
			sqLiteDatabase.update(SORT_TABLE, values, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName, name});
		}catch(SQLiteException e) {
		}finally {
			release(sqLiteDatabase);
		}
	}
	
	public void delete(String packageName) {
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			sqLiteDatabase.delete(SORT_TABLE, PACKAGE_NAME+"=?", new String[]{packageName});
		}catch(SQLiteException e) {
		}finally {
			release(sqLiteDatabase);
		}
	}
	
	public void clearDataByPackageName(String packageName, String title) {
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			ContentValues values=new ContentValues();
			values.put(OPEN_TIME, 0L);
//			values.put(OPEN_NUM, 0L);
			sqLiteDatabase.update(SORT_TABLE, values, PACKAGE_NAME+"=? and "+APP_NAME+"=?", new String[]{packageName, title});
		}catch(SQLiteException e) {
		}finally {
			release(sqLiteDatabase);
		}
	}
	
	/**
	 * 返回最近安装的20条数据
	 * @return
	 */
	public List<String[]> getPackageList() {
		Cursor cursor=null;
		try{
			sqLiteDatabase=dbHelper.getReadableDatabase();
			cursor=sqLiteDatabase.query(INSTALL_TABLE, null, null, null, null, null, "_ID desc", INSTALL_LIMIT);
			if(cursor.getCount()>0) {
				List<String[]> list=new ArrayList<String[]>();
				cursor.moveToFirst();
				while(!cursor.isAfterLast()) {
					String[] str=new String[2];
					str[0]=cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
					str[1]=cursor.getString(cursor.getColumnIndex(APP_NAME));
					list.add(str);
					cursor.moveToNext();
				}
				return list;
			}
		}catch(SQLiteException e) {
		}finally {
			release(cursor, sqLiteDatabase);
		}
		return null;
	}
	
	public void addInstallPackageName(String packageName, String name) {
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			ContentValues values=new ContentValues();
			values.put(PACKAGE_NAME, packageName);
			values.put(APP_NAME, name);
			sqLiteDatabase.insert(INSTALL_TABLE, null, values);
		}catch(SQLiteException e) {
		}finally {
			release(sqLiteDatabase);
		}
	}
	
	public void deleteInstallPackageName(String packageName) {
		try{
			sqLiteDatabase=dbHelper.getWritableDatabase();
			ContentValues values=new ContentValues();
			values.put(PACKAGE_NAME, packageName);
			sqLiteDatabase.delete(INSTALL_TABLE, PACKAGE_NAME+"=?", new String[]{packageName});
		}catch(SQLiteException e) {
		}finally {
			release(sqLiteDatabase);
		}
	}
	
	private void release(Cursor cursor) {
		if(cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
	}
	
	private void release(SQLiteDatabase sqLiteDatabase) {
		if(sqLiteDatabase!=null && sqLiteDatabase.isOpen()) {
			sqLiteDatabase.close();
		}
	}
	
	private void release(Cursor cursor, SQLiteDatabase sqLiteDatabase) {
		release(cursor);
		release(sqLiteDatabase);
	}
}
