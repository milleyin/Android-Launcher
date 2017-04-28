package com.dongji.desktopswitch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CustomPowerDBHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "custompower";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_CUSTOMPOWER = "table_custompower";

	/**
	 * =1��on;=2:off.
	 */
	public final static String FIELD_POWERNAME = "powername";
	public final static String FIELD_WIFI = "wifi";
	public final static String FIELD_FLYMODEL = "flymodel";
	public final static String FIELD_SYNC = "sync";
	public final static String FIELD_AUTOROTATION = "autoratation";

	public final static String FIELD_BRIGHT = "bright";
	public final static String FIELD_MUSIC = "music";
	public final static String FIELD_RING = "ring";
	public final static String FIELD_SLEEP = "sleep";

	public CustomPowerDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "Create table if not exists " + TABLE_CUSTOMPOWER + "("
				+ FIELD_POWERNAME + " varchar(300)," + FIELD_WIFI + " integer,"
				+ FIELD_FLYMODEL + " integer," + FIELD_SYNC + " integer,"
				+ FIELD_AUTOROTATION + " integer," + FIELD_BRIGHT + " integer,"
				+ FIELD_MUSIC + " integer," + FIELD_RING + " integer,"
				+ FIELD_SLEEP + " integer" + ");";
		db.execSQL(sql);
	}

	public SettingInt selectAllInt() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CUSTOMPOWER, null, null, null, null,
				null, null);
		SettingInt settingInt = null;
		if (cursor.moveToFirst()) {
			settingInt = getSettingIntFromCursor(cursor);
		}
		cursor.close();
		db.close();
		return settingInt;
	}

	public boolean selectIsHasData() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CUSTOMPOWER, null, null, null, null,
				null, null);
		if (cursor.moveToFirst()) {
			cursor.close();
			db.close();
			return true;
		}
		cursor.close();
		db.close();
		return false;
	}

	public int selectIntBySettingName(String settingName) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CUSTOMPOWER,
				new String[] { settingName }, null, null, null, null, null);
		int settingInt = -1;
		if (cursor.moveToFirst()) {
			settingInt = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return settingInt;
	}
	
	public String selectStringBySettingName(String settingName) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CUSTOMPOWER,
				new String[] { settingName }, null, null, null, null, null);
		String settingString="自定义模式";
		if (cursor.moveToFirst()) {
			settingString = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return settingString;
	}

	public synchronized int insertIntoSettingInt(SettingInt settingInt) {
		long id = -1;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(FIELD_AUTOROTATION, settingInt.autoratation);
			contentValues.put(FIELD_BRIGHT, settingInt.bright);
			contentValues.put(FIELD_FLYMODEL, settingInt.flymodel);
			contentValues.put(FIELD_MUSIC, settingInt.music);
			contentValues.put(FIELD_POWERNAME, settingInt.powername);
			contentValues.put(FIELD_RING, settingInt.ring);
			contentValues.put(FIELD_SLEEP, settingInt.sleep);
			contentValues.put(FIELD_SYNC, settingInt.sync);
			contentValues.put(FIELD_WIFI, settingInt.wifi);
			id = db.insert(TABLE_CUSTOMPOWER, null, contentValues);
			db.close();
		} catch (Exception e) {
		}
		return (int) id;
	}

	public synchronized void updateAllSettingInt(SettingInt settingInt) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(FIELD_AUTOROTATION, settingInt.autoratation);
			contentValues.put(FIELD_BRIGHT, settingInt.bright);
			contentValues.put(FIELD_FLYMODEL, settingInt.flymodel);
			contentValues.put(FIELD_MUSIC, settingInt.music);
			contentValues.put(FIELD_POWERNAME, settingInt.powername);
			contentValues.put(FIELD_RING, settingInt.ring);
			contentValues.put(FIELD_SLEEP, settingInt.sleep);
			contentValues.put(FIELD_SYNC, settingInt.sync);
			contentValues.put(FIELD_WIFI, settingInt.wifi);
			db.update(TABLE_CUSTOMPOWER, contentValues, null, null);
			db.close();
		} catch (Exception e) {
		}
	}

	public synchronized void updateSettingIntBySettingName(String settingName,
			int value) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(settingName, value);
			db.update(TABLE_CUSTOMPOWER, contentValues, null, null);
			db.close();
		} catch (Exception e) {
		}
	}

	public SettingInt getSettingIntFromCursor(Cursor cursor) {
		SettingInt settingInt = new SettingInt();
		settingInt.autoratation = cursor.getInt(cursor
				.getColumnIndex(FIELD_AUTOROTATION));
		settingInt.bright = cursor.getInt(cursor.getColumnIndex(FIELD_BRIGHT));
		settingInt.flymodel = cursor.getInt(cursor
				.getColumnIndex(FIELD_FLYMODEL));
		settingInt.music = cursor.getInt(cursor.getColumnIndex(FIELD_MUSIC));
		settingInt.powername = cursor.getString(cursor
				.getColumnIndex(FIELD_POWERNAME));
		settingInt.ring = cursor.getInt(cursor.getColumnIndex(FIELD_RING));
		settingInt.sleep = cursor.getInt(cursor.getColumnIndex(FIELD_SLEEP));
		settingInt.sync = cursor.getInt(cursor.getColumnIndex(FIELD_SYNC));
		settingInt.wifi = cursor.getInt(cursor.getColumnIndex(FIELD_WIFI));
		return settingInt;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = " DROP TABLE IF EXISTS " + TABLE_CUSTOMPOWER;
		db.execSQL(sql);
		onCreate(db);
	}

}
