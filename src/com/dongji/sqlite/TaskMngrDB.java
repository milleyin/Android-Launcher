package com.dongji.sqlite;

import java.util.ArrayList;
import java.util.List;

import com.dongji.enity.NetrafficRecord;
import com.dongji.enity.PowerbootRecord;
import com.dongji.enity.ShowPermissionRecord;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskMngrDB {

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "task_mngr_db.db";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("create table if not exists NETRAFFIC_RECORD(_id INTEGER primary key autoincrement, name TEXT, value INTEGER)");
			db.execSQL("create table if not exists POWERBOOT_RECORD(_id INTEGER primary key autoincrement, name TEXT, state INTEGER, uid INTEGER)");
			db.execSQL("create table if not exists SHOW_PERMISSION(_id INTEGER primary key autoincrement, name TEXT, state INTEGER)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class Netraffic_Service {
		
		private DatabaseHelper dbHelper;
		private String TBNAME = "NETRAFFIC_RECORD";
		
		public Netraffic_Service(Context context) {
			dbHelper = new TaskMngrDB.DatabaseHelper(context);
		}
		
		public void add(NetrafficRecord record) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", record.getPkg_name());
			values.put("value", record.getFlow_value());
			db.insert(TBNAME, null, values);
			db.close();
		}
		
		public void del(String name) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(TBNAME, "name = ?", new String[]{name});
			db.close();
		}
		
		public void update(NetrafficRecord record) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", record.getPkg_name());
			values.put("value", record.getFlow_value());
			db.update(TBNAME, values, "name = ?", new String[]{record.getPkg_name()});
			db.close();
		}
		
		public long select(String name) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "name=?", new String[]{name}, null, null, null);
			long value = 0;
			if (cursor.moveToNext()) {
				value = cursor.getLong(cursor.getColumnIndex("value"));
			}
			cursor.close();
			db.close();
			return value;
		}
		
		public List<NetrafficRecord> getAll() {
			List<NetrafficRecord> data = new ArrayList<NetrafficRecord>();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, null, null, null, null, null);
			while (cursor.moveToNext()) {
				NetrafficRecord record = new NetrafficRecord();
				record.setPkg_name(cursor.getString(cursor.getColumnIndex("name")));
				record.setFlow_value(cursor.getLong(cursor.getColumnIndex("value")));
				data.add(record);
			}
			cursor.close();
			db.close();
			return data;
		}
		
		public int delAll() {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			return db.delete(TBNAME, null, null);
		}
		
		public void addAll(List<NetrafficRecord> data) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			for (NetrafficRecord record : data) {
				values.put("name", record.getPkg_name());
				values.put("value", record.getFlow_value());
				db.insert(TBNAME, null, values);
			}
			db.close();
		}
	}
	
	public static class Powerboot_DB {
		private DatabaseHelper dbHelper;
		private static final String TBNAME = "POWERBOOT_RECORD";
		
		public Powerboot_DB(Context context) {
			dbHelper = new TaskMngrDB.DatabaseHelper(context);
		}
		
		public void add(PowerbootRecord record) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", record.getPkgName());
			values.put("state", record.getState());
			values.put("uid", record.getUid());
			db.insert(TBNAME, null, values);
			db.close();
		}
		
		public void del(String name) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(TBNAME, "name = ?", new String[]{name});
			db.close();
		}
		
		public void update(PowerbootRecord record) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", record.getPkgName());
			values.put("state", record.getState());
			values.put("uid", record.getUid());
			db.update(TBNAME, values, "name = ?", new String[]{record.getPkgName()});
			db.close();
		}
		
		public int select(String name) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "name=?", new String[]{name}, null, null, null);
			int state = 0;
			if (cursor.moveToNext()) {
				state = cursor.getInt(cursor.getColumnIndex("state"));
			}
			cursor.close();
			db.close();
			return state;
		}
		
		public boolean isExist(String name) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "name=?", new String[]{name}, null, null, null);
			boolean exist = false;
			if (cursor.getCount() > 0) {
				exist = true;
			}
			cursor.close();
			db.close();
			return exist;
		}
		
		public List<PowerbootRecord> getForbiddenList(int state) {
			List<PowerbootRecord> data = new ArrayList<PowerbootRecord>();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "state=?", new String[]{state + ""}, null, null, null);
			while (cursor.moveToNext()) {
				PowerbootRecord record = new PowerbootRecord();
				record.setPkgName(cursor.getString(cursor.getColumnIndex("name")));
				record.setState(cursor.getInt(cursor.getColumnIndex("state")));
				record.setUid(cursor.getInt(cursor.getColumnIndex("uid")));
				data.add(record);
			}
			cursor.close();
			db.close();
			return data;
		}
		
		public List<PowerbootRecord> getAll() {
			List<PowerbootRecord> data = new ArrayList<PowerbootRecord>();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, null, null, null, null, null);
			while (cursor.moveToNext()) {
				PowerbootRecord record = new PowerbootRecord();
				record.setPkgName(cursor.getString(cursor.getColumnIndex("name")));
				record.setState(cursor.getInt(cursor.getColumnIndex("state")));
				record.setUid(cursor.getInt(cursor.getColumnIndex("uid")));
				data.add(record);
			}
			cursor.close();
			db.close();
			return data;
		}
		
		public int delAll() {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			return db.delete(TBNAME, null, null);
		}
		
		public void addAll(List<PowerbootRecord> data) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			for (PowerbootRecord record : data) {
				values.put("name", record.getPkgName());
				values.put("state", record.getState());
				values.put("uid", record.getUid());
				db.insert(TBNAME, null, values);
			}
			db.close();
		}
	}
	
	public static class Permission_DB {
		private DatabaseHelper dbHelper;
		private static final String TBNAME = "SHOW_PERMISSION";
		
		public Permission_DB(Context context) {
			dbHelper = new TaskMngrDB.DatabaseHelper(context);
		}
		
		public void add(ShowPermissionRecord record) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", record.getPkgName());
			values.put("state", record.getState());
			db.insert(TBNAME, null, values);
			db.close();
		}
		
		public void del(String name) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(TBNAME, "name = ?", new String[]{name});
			db.close();
		}
		
		public void update(ShowPermissionRecord record) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", record.getPkgName());
			values.put("state", record.getState());
			db.update(TBNAME, values, "name = ?", new String[]{record.getPkgName()});
			db.close();
		}
		
		public int select(String name) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "name=?", new String[]{name}, null, null, null);
			int state = 0;
			if (cursor.moveToNext()) {
				state = cursor.getInt(cursor.getColumnIndex("state"));
			}
			cursor.close();
			db.close();
			return state;
		}
		
		public boolean isExist(String name) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "name=?", new String[]{name}, null, null, null);
			boolean exist = false;
			if (cursor.getCount() > 0) {
				exist = true;
			}
			cursor.close();
			db.close();
			return exist;
		}
		
		public List<ShowPermissionRecord> getForbiddenList(int state) {
			List<ShowPermissionRecord> data = new ArrayList<ShowPermissionRecord>();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, "state=?", new String[]{state + ""}, null, null, null);
			while (cursor.moveToNext()) {
				ShowPermissionRecord record = new ShowPermissionRecord();
				record.setPkgName(cursor.getString(cursor.getColumnIndex("name")));
				record.setState(cursor.getInt(cursor.getColumnIndex("state")));
				data.add(record);
			}
			cursor.close();
			db.close();
			return data;
		}
		
		public List<ShowPermissionRecord> getAll() {
			List<ShowPermissionRecord> data = new ArrayList<ShowPermissionRecord>();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(TBNAME, null, null, null, null, null, null);
			while (cursor.moveToNext()) {
				ShowPermissionRecord record = new ShowPermissionRecord();
				record.setPkgName(cursor.getString(cursor.getColumnIndex("name")));
				record.setState(cursor.getInt(cursor.getColumnIndex("state")));
				data.add(record);
			}
			cursor.close();
			db.close();
			return data;
		}
		
		public int delAll() {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			return db.delete(TBNAME, null, null);
		}
		
		public void addAll(List<ShowPermissionRecord> data) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			for (ShowPermissionRecord record : data) {
				values.put("name", record.getPkgName());
				values.put("state", record.getState());
				db.insert(TBNAME, null, values);
			}
			db.close();
		}
	}
}
