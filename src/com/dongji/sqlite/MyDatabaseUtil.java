package com.dongji.sqlite;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MyDatabaseUtil {

	private Context mContext;
	private SQLiteDatabase mSQLiteDatabase = null;
	private DatabaseHelper mDatabaseHelper = null;

	private static final String DB_NAME = "dongjiRemind.db";//数据库名称
    private static final int DB_VERSION =1;//版本
    private static final String TABLE_1="table_1";
  
    public static final String  KEY_ID ="key_id";
    public static final String  KEY_NAME ="key_name";
    public static final String  KEY_AGE ="key_age";
	//用户表start
    public static final String USERID="uid";
    public static final String USERNAME="username";
    public static final String PASSWORD="password";
    
    private static final String DEL1="del1";
    private static final String DEL2="del2";
    private static final String DEL3="del3";
    //用户表end
	//系统表start
    private static final String USER="user";
    
    //短信库表
    private static final String MESSAGE_LIBRARY="message_library";
    
    public static final String  MESSAGE_ID ="message_id";
    public static final String  MESSAGE_CONTENT ="message_content";//内容
    public static final String  MESSAGE_CATEGORY ="message_category";//分类
    public static final String  MESSAGE_DATE ="message_date";//时间
    public static final String  MESSAGE_DEL1 ="message_del1";//备用字段
    public static final String  MESSAGE_DEL2 ="message_del2";//备用字段
    public static final String  MESSAGE_DEL3 ="message_del3";//备用字段
    
    
    //短信收藏表Favorite
    private static final String MESSAGE_FAVORITE="message_favorite";
    public static final String  FAVORITE_ID ="favorite_id";//自增id
    public static final String  THREAD_ID ="thread_id";//会话id
    public static final String  CONTENT_ID ="content_id";//某条短信的具体id
    public static final String  FAVORITE_CONTENT ="favorite_content";//短信内容
    public static final String  CONTENT_TIME ="favorite_time";//短信时间
    public static final String  FAVORITE_SENDER ="favorite_send";//短信发送者
    public static final String  FAVORITE_NUMBER ="favorite_number";//电话号码
    public static final String  FAVORITE_DEL1 ="favorite_del1";//备用字段
    public static final String  FAVORITE_DEL2 ="favorite_del2";//备用字段
    public static final String  FAVORITE_DEL3 ="favorite_del3";//备用字段
    public static final String TIME = "_time";
    
    
    //提醒表 
    private static final String TABLE_REMIND="table_remind";
    
    public static final String REMIND_ID="remind_id";  //提醒id
    public static final String REMIND_CONTENT = "remind_content"; //内容
    public static final String REMIND_CONTACT = "remind_contact"; //联系人
//    public static final String REMIND_CONTACT_ID = "remind_contact_id";
    public static final String REMIND_PARTICIPANT = "remind_participants"; //参与人
    public static final String REMIND_START = "remind_start";
    public static final String REMIND_END = "remind_end";
    
    public static final String REMIND_TYPE = "remind_type";//提醒类型,提前多久提醒
    public static final String REMIND_NUM = "remind_num"; 
    public static final int REMIND_TYPE_MIN = 0;
    public static final int REMIND_TYPE_HOUR = 1;
    public static final int REMIND_TYPE_DAY = 2;
    public static final int REMIND_TYPE_WEEK = 3;
    
    public static final String REMIND_TIME = "remind_time"; //提醒次数
    
    public static final String REPEAT_TYPE = "repeat_type"; //重复类型
    public static final int REPEAT_TYPE_ONE = 0;
    public static final int REPEAT_TYPE_DAY = 1;
    public static final int REPEAT_TYPE_WEEK = 2;
    public static final int REPEAT_TYPE_MONTH = 3;
    public static final int REPEAT_TYPE_YEAR = 4;
    
    public static final String REPEAT_FREQ = "repeat_freq"; //重复频率
    public static final String REPEAT_CONDITION = "repeat_condition"; //重复条件
    
    public static final String REPEAT_START_TIME ="repeat_start_time";
    public static final String REPEAT_END_TIME ="repeat_end_time";
    public static final String HAS_REMIND_TIME = "has_remind_time"; //已经提醒了多少次
    
    public static final String TIME_FILTER = "time_filter";  //时间过滤： text类型   long,long,long ;  在此时间内的提醒将不被触发
    
    private static final String REMIND_DEL1="remind_del1";
    private static final String REMIND_DEL2="remind_del2";
    private static final String REMIND_DEL3="remind_del3";
    
    //桌面文件夹加密信息表
    
    public static final String ENCRYPTION_TABLE = "encryption_table";
    public static final String E_ID = "_id";
    public static final String FOLDER_ID = "f_id";
    public static final String FOLDER_PASSWORD = "pwd";
    public static final String ISDECIPHERING = "isdeciphering";
    
    private static final String CREATE_ENTRYPTION = "CREATE TABLE " + ENCRYPTION_TABLE + "("+ E_ID + " INTEGER PRIMARY KEY ,"  +FOLDER_ID+" TEXT ,"+FOLDER_PASSWORD+" TEXT,"+ISDECIPHERING+" INTEGER);";
    
    
    //提醒表
    //创建提醒表
   	private static final String CREATE_REMIND = "CREATE TABLE " + TABLE_REMIND + "("
   	+ REMIND_ID + " INTEGER PRIMARY KEY ,"  +REMIND_CONTENT+" TEXT ,"+REMIND_CONTACT+" TEXT , "+REMIND_PARTICIPANT+" TEXT , "
   	+REMIND_START+" LONG , "+REMIND_END+" LONG , "+REMIND_TYPE+" INTEGER , "+REMIND_NUM+" INTEGER ,"+REMIND_TIME+" INTEGER , "+REPEAT_TYPE+" INTEGER , "+REPEAT_FREQ+" INTEGER , "+REPEAT_CONDITION+" TEXT ,"+REPEAT_START_TIME+" LONG ,"+REPEAT_END_TIME+" LONG ,"+ HAS_REMIND_TIME +" INTEGER," + TIME_FILTER+ " TEXT,"+ REMIND_DEL1 +" CHAR,"+REMIND_DEL2+" CHAR,"+REMIND_DEL3+" CHAR);";
    
    
    //收藏表
	private static final String CREATE_MESSAGE_FAVORITE = "CREATE TABLE " + MESSAGE_FAVORITE + "("
	+ FAVORITE_ID + " INTEGER PRIMARY KEY ,"  +THREAD_ID+" CHAR ,"+CONTENT_ID+" CHAR , "+FAVORITE_CONTENT+" CHAR , "+CONTENT_TIME+" CHAR , "+FAVORITE_SENDER+" CHAR , "+FAVORITE_NUMBER+" CHAR , "+TIME+" LONG ,"+FAVORITE_DEL1+" CHAR , "+FAVORITE_DEL2+" CHAR , "+FAVORITE_DEL3+" CHAR);";
    
//	//用户表
//	private static final String CREATE_USER = "CREATE TABLE " + USER + "("
//	+ KEY_ID + " INTEGER PRIMARY KEY," + USERID + " CHAR," +USERNAME+" CHAR,"+PASSWORD+" CHAR);";
	
	
	
	//短信库表
	private static final String CREATE_MESSAGE_LIBRARY = "CREATE TABLE " + MESSAGE_LIBRARY + "("
	+ MESSAGE_ID + " INTEGER PRIMARY KEY ,"  +MESSAGE_CONTENT+" CHAR ,"+MESSAGE_CATEGORY+" CHAR , "+MESSAGE_DATE+" CHAR , "+MESSAGE_DEL1+" CHAR , "+MESSAGE_DEL2+" CHAR , "+MESSAGE_DEL3+" CHAR);";
	public MyDatabaseUtil(Context mContext)
	{
		this.mContext = mContext;
	}
	
	//打开数据库
	public void open() throws SQLException
	{
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}


	//关闭数据库
	public void close()
	{
		mDatabaseHelper.close();
	}
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("CREATE_MESSAGE_LIBRARY---->"+CREATE_MESSAGE_LIBRARY);
			db.execSQL(CREATE_MESSAGE_LIBRARY); //短信库表
			db.execSQL(CREATE_MESSAGE_FAVORITE); //短信收藏表
			db.execSQL(CREATE_REMIND);
			db.execSQL(CREATE_ENTRYPTION); //桌面文件加密信息表
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

	//添加加密信息
	public long insertEntryption(String folderId,String pwd){
		
		Cursor cursor = mSQLiteDatabase.query(ENCRYPTION_TABLE, null , FOLDER_ID +" = '" + folderId +"'", null , null, null, null);
		
		if ( cursor.getCount() > 0) {
			
			if (cursor.moveToFirst()){
				
				int id = cursor.getInt(cursor.getColumnIndex(E_ID));
				
				mSQLiteDatabase.delete(ENCRYPTION_TABLE, E_ID + " = " + id, null);
			}
		}
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(FOLDER_ID, folderId);
		contentValues.put(FOLDER_PASSWORD, pwd);
		contentValues.put(ISDECIPHERING, "0");
		
		return mSQLiteDatabase.insert(ENCRYPTION_TABLE, E_ID, contentValues);
		
	}
	
	//查询是否需要解密
	public boolean isDeciphering(String folderId)
	{
		int isdeg = 0;
		
		Cursor cursor = mSQLiteDatabase.query(ENCRYPTION_TABLE, null , FOLDER_ID +" = '" + folderId +"'", null , null, null, null);
		
		int count = cursor.getCount();
		if (cursor.moveToFirst()){
				
			isdeg = cursor.getInt(cursor.getColumnIndex(ISDECIPHERING));
				
		}
		
		cursor.close();
		
		if ( count > 0 ){
			
			if (isdeg == 0 )
				return true;
		}
		
		return false;
		
	}
	
	//查询输入的密码是否正确
	public int queryEntryption(String folderId,String pwd){
		
		Cursor cursor =  mSQLiteDatabase.query(ENCRYPTION_TABLE, null, FOLDER_ID +" = '" +folderId+ "' and " + FOLDER_PASSWORD +" = '"+pwd+"'", null, null, null, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	
	//解密
	public long updateEntryption(String folderId){
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(ISDECIPHERING, "1");

		return mSQLiteDatabase.update(ENCRYPTION_TABLE, contentValues, FOLDER_ID +" = '"+folderId+"'", null);
	}
	
	//新建新的提醒
	public long insertRemind(String content,String contact,String partnerIds,long start_time,long end_time,int remind_type, int remind_num , int remind_time ,int repeat_type , int repeat_freq,String repeat_condition,long repeat_start_time, long repeat_end_time)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(REMIND_CONTENT,tranSQLSting(content));
		
		
		initialValues.put(REMIND_CONTACT,contact);
		initialValues.put(REMIND_PARTICIPANT,partnerIds);
		
		initialValues.put(REMIND_START,start_time);
		initialValues.put(REMIND_END,end_time);
		
		initialValues.put(REMIND_TYPE,remind_type);
		initialValues.put(REMIND_NUM, remind_num);
		
		initialValues.put(REMIND_TIME, remind_time);
		
		initialValues.put(REPEAT_TYPE, repeat_type);
		initialValues.put(REPEAT_FREQ, repeat_freq);
		initialValues.put(REPEAT_CONDITION, repeat_condition);
		
		initialValues.put(REPEAT_START_TIME, repeat_start_time);
		initialValues.put(REPEAT_END_TIME, repeat_end_time);
		
		initialValues.put(HAS_REMIND_TIME, 0);
		initialValues.put(TIME_FILTER, "");
		
		return mSQLiteDatabase.insert(TABLE_REMIND, REMIND_ID, initialValues);
	}
	
	//更新提醒
	public long updateRemind(long id,String content,String contact,String partnerIds,long start_time,long end_time,int remind_type, int remind_num , int remind_time ,int repeat_type , int repeat_freq,String repeat_condition,long repeat_start_time, long repeat_end_time)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(REMIND_CONTENT,tranSQLSting(content));
		initialValues.put(REMIND_CONTACT,contact);
		initialValues.put(REMIND_PARTICIPANT,partnerIds);
		
		initialValues.put(REMIND_START,start_time);
		initialValues.put(REMIND_END,end_time);
		
		initialValues.put(REMIND_TYPE,remind_type);
		initialValues.put(REMIND_NUM, remind_num);
		
		initialValues.put(REMIND_TIME, remind_time);
		
		initialValues.put(REPEAT_TYPE, repeat_type);
		initialValues.put(REPEAT_FREQ, repeat_freq);
		initialValues.put(REPEAT_CONDITION, repeat_condition);
		
		initialValues.put(REPEAT_START_TIME, repeat_start_time);
		initialValues.put(REPEAT_END_TIME, repeat_end_time);
		
		initialValues.put(HAS_REMIND_TIME, 0);
		initialValues.put(TIME_FILTER, "");
		
		return mSQLiteDatabase.update(TABLE_REMIND, initialValues, REMIND_ID+"="+id, null);
	}
	
	//更新已经提醒的次数
	public long updateHasRemindNum(long id,int has_remind_time)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(HAS_REMIND_TIME, has_remind_time);
		
		return mSQLiteDatabase.update(TABLE_REMIND, initialValues, REMIND_ID+"="+id, null);
	}
	
	//查询全部提醒
	public Cursor queryAllRemind()
	{
		return mSQLiteDatabase.query(TABLE_REMIND, null , null, null , null, null, null);
	}
	
	//查询指定id的提醒
	public Cursor queryRemind(long id)
	{
		return mSQLiteDatabase.query(TABLE_REMIND, null , REMIND_ID +" = "+id, null , null, null, null);
	}
	
	public Cursor queryRemindByContactId(String contactId)
	{
		String key = "#"+contactId+"#";
		return mSQLiteDatabase.query(TABLE_REMIND, null , REMIND_CONTACT +" LIKE '%"+key+"%' OR " + REMIND_PARTICIPANT + " LIKE '%"+key+"%'", null , null, null, null);
	}
	
    //查询所有提醒    按时间开始时间 从近到远排列
    public Cursor queryRemindContact(){
    	Cursor mCursor = mSQLiteDatabase.query(TABLE_REMIND, new String[]{REMIND_CONTACT}, null, null, null, null, REMIND_START+" desc");
    	return mCursor;
    }
    
    //删除指定id的提醒
	public long delete(long id)
	{
		return mSQLiteDatabase.delete(TABLE_REMIND,  REMIND_ID +" = "+id, null);
	}
	
	//更新时间过滤
	public long updateRemindTimeFilter(long id , long filter_time)
	{
		Cursor c = queryRemind(id);
		String filter_str ;
		
		c.moveToNext();
		filter_str = c.getString(c.getColumnIndex(TIME_FILTER));
		c.close();
		
		filter_str = filter_str + filter_time+",";
		
		System.out.println("  filter_str  --->  " + filter_str);
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(TIME_FILTER,filter_str);
		
		return mSQLiteDatabase.update(TABLE_REMIND, initialValues, REMIND_ID+"="+id, null);
	}
    
	/**
	 * 转义符
	 *
	 */
	public String tranSQLSting(String str)
	{
		String [] parts=str.split("'");
		String str_tran ="";
		for(int i =0;i<parts.length;i++)
		{
			if(i==0)
			{
				str_tran+=parts[i];
			}else{
				str_tran+="''"+parts[i];
			}
		}
		return str_tran;
	}

}

