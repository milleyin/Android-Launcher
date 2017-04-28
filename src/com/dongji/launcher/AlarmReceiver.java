package com.dongji.launcher;

/* import class */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dongji.sqlite.MyDatabaseUtil;


/*AlarmReceiver */
public class AlarmReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(final Context context, Intent intent)
  {	  
		Intent alaramIntent = new Intent(context, RemindPopActivity.class);
		alaramIntent.putExtra(MyDatabaseUtil.REMIND_ID, intent.getLongExtra(MyDatabaseUtil.REMIND_ID, -1));
		alaramIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(alaramIntent);
  }
}