package com.dongji.receiver;

import java.util.ArrayList;
import java.util.List;

import com.dongji.enity.InstalledAppInfo;
import com.dongji.enity.NetrafficRecord;
import com.dongji.enity.PowerbootRecord;
import com.dongji.sqlite.TaskMngrDB;
import com.dongji.sqlite.TaskMngrDB.Netraffic_Service;
import com.dongji.sqlite.TaskMngrDB.Powerboot_DB;
import com.dongji.tool.AndroidUtils;
import com.dongji.tool.LauncherUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SystemReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//			context.startService(new Intent(context, NetrafficService.class));
			System.out.println("******************开         机*****************");
			Powerboot_DB powerboot_DB = new TaskMngrDB.Powerboot_DB(context);
			List<PowerbootRecord> forbiddenList = powerboot_DB.getForbiddenList(0);
//			for (PowerbootRecord powerbootRecord : forbiddenList) {
//				System.out.println(powerbootRecord.getPkgName() + "---hahahahahhaha");
//			}
			LauncherUtils.stopPowerBootApps(context, forbiddenList);
		}
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			System.out.println("******************关         机*****************");
			Netraffic_Service recordDB = new TaskMngrDB.Netraffic_Service(context);
			List<InstalledAppInfo> appList = LauncherUtils.getInstalledApp(context, LauncherUtils.FILTER_ALL_APP);
			List<NetrafficRecord> historyList = recordDB.getAll();
			List<NetrafficRecord> newList = new ArrayList<NetrafficRecord>();
			recordDB.delAll();
			NetrafficRecord record;
			String pkgName;
			for (InstalledAppInfo installedAppInfo : appList) {
				long historyFlow = 0;
				record = new NetrafficRecord();
				pkgName = installedAppInfo.getPkgName();
				record.setPkg_name(pkgName);
				for (NetrafficRecord netrafficRecord : historyList) {
					if (pkgName.equals(netrafficRecord.getPkg_name())) {
						historyFlow = netrafficRecord.getFlow_value();
						break;
					}
				}
				record.setFlow_value(historyFlow + AndroidUtils.getRxAndTxBytes(installedAppInfo.getUid()));
				newList.add(record);
			}
			recordDB.addAll(newList);
		}
		/*if (intent.getAction().equals(Intent.ACTION_WALLPAPER_CHANGED)) {
			Toast.makeText(context, "壁纸已更换", Toast.LENGTH_SHORT).show();
		}*/
	}

}
