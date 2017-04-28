package com.dongji.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.TaskMngrChannel;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dongji.enity.HighRiskPermissionExpl;
import com.dongji.enity.InstalledAppInfo;
import com.dongji.enity.PowerbootRecord;
import com.dongji.enity.ShowPermissionRecord;
import com.dongji.launcher.R;
import com.dongji.sqlite.TaskMngrDB;
import com.dongji.sqlite.TaskMngrDB.Netraffic_Service;
import com.dongji.tool.AndroidUtils;
import com.dongji.tool.LauncherUtils;
import com.dongji.ui.HighRiskWarningDialog;
import com.dongji.ui.TaskMngrDialog;

public class TaskMngrAdapter extends BaseAdapter {
	
	public static final int KILL_RUNNING_PROCESS = 0;
	private static final int MENU_DISPLAY_CTRL = 1;
	private static final int MENU_DISPLAY_CTRL_LAST = 5;
	private static final int RAM_USAGE_PERSENT = 2;
	private static final int CALCULATE_CACHE_VALUE = 3;
	private static final int REFRESH_UNINSTALL = 4;

	private Context context;
	private List<InstalledAppInfo> data, chosenList;
	private TaskMngrDB.Netraffic_Service service;
	private TaskMngrDB.Powerboot_DB powerboot_DB;
	private TaskMngrDB.Permission_DB permission_DB;
	private TaskMngrDialog mDialog;
	private HighRiskWarningDialog warningDialog;
	private View menuLayout;
	private ListView lv;
	private int menuMarginBottom;
	
	private int location;
	private float allUsageSum;
	private boolean showCheckBox;
	public String uninstalledPkgName;
	
	private Handler handler, handler2;
	
	private int archo_temp = -5, po;

	public TaskMngrAdapter(Context context, List<InstalledAppInfo> data, TaskMngrDB.Netraffic_Service service, Handler handler, ListView lv) {
		super();
		this.context = context;
		this.data = data;
		this.service = service;
		this.handler2 = handler;
		this.lv = lv;
		this.powerboot_DB = new TaskMngrDB.Powerboot_DB(context);
		this.permission_DB = new TaskMngrDB.Permission_DB(context);
		initHandler();
//		allUsageSum = getAllRamUsage();
//		initChosenList();
	}

	public TaskMngrAdapter(Context context, Netraffic_Service service) {
		super();
		this.context = context;
		this.service = service;
		initHandler();
	}
	
	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("handler");
		handlerThread.start();
		handler = new MyHandler(handlerThread.getLooper());
		handler.sendEmptyMessage(RAM_USAGE_PERSENT);
	}
	
	public void initChosenList() {
		if (chosenList == null) {
			chosenList = new ArrayList<InstalledAppInfo>();
		}
		if (data.size() > 0) {
			chosenList.clear();
			for (InstalledAppInfo info : data) {
				chosenList.add(info);
			}
		}
	}
	
	private float getAllRamUsage() {
		return (AndroidUtils.getTotalMemory(context) - AndroidUtils.getAvailMemory(context))/1024;
	}
	
	public void clearChosenList() {
		if (chosenList != null) {
			chosenList.clear();
		}
	}

	public List<InstalledAppInfo> getChosenList() {
		// TODO Auto-generated method stub
		return chosenList;
	}
	
	public void reset() {
		if (data != null && data.size() > 0) {
			data.clear();
		}
	}
	
	public void addData(List<InstalledAppInfo> list) {
		if (list != null && list.size() > 0) {
			data = list;
//			initChosenList();
//			allUsageSum = getAllRamUsage();
			handler.sendEmptyMessage(RAM_USAGE_PERSENT);
			notifyDataSetChanged();
		}
	}
	
	public boolean isShowCheckBox() {
		return showCheckBox;
	}

	public void setShowCheckBox(boolean showCheckBox) {
		this.showCheckBox = showCheckBox;
	}

	public void refreshCheckBox(boolean flag) {
		showCheckBox = flag;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data == null ? 0 : data.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return data.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	/**
	 * 根据包名删除卸载列表对应的数据，并刷新列表
	 * @param packageName
	 */
	private void removeAppDataByPackageName(String packageName) {
		if(data!=null && data.size()>0) {
			for(int i=0;i<data.size();i++) {
				InstalledAppInfo info=data.get(i);
				if(packageName.equals(info.getPkgName())) {
					data.remove(i);
//					allUsageSum = getAllRamUsage();
					handler.sendEmptyMessage(RAM_USAGE_PERSENT);
					notifyDataSetChanged();
					break;
				}
			}
		}
	}
	
	public void refreshUninstall() {
		handler.sendEmptyMessage(REFRESH_UNINSTALL);
	}
	
	public void removeChosenApps(List<InstalledAppInfo> list) {
		if (data != null && data.size() > 0) {
			data.removeAll(list);
//			allUsageSum = getAllRamUsage();
			handler.sendEmptyMessage(RAM_USAGE_PERSENT);
			notifyDataSetChanged();
//			initChosenList();
		}
	}
	
	public String getCacheSize(String packageName) {
		String cacheLen = "0.00B";
		File cacheFile = new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + packageName + "/cache");
//		File cacheFile = new File()
		if (cacheFile.exists()) {
//			if (cacheFile.listFiles() != null) {
//				System.out.println("list length =====>" + cacheFile.listFiles().length);
//			}
//			cacheLen = sizeFormat(AndroidUtils.getFileSize(cacheFile));
			cacheLen = Formatter.formatFileSize(context, AndroidUtils.getFileSize2(cacheFile));
//			System.out.println("cache==========>" + cacheLen);
		}
		return cacheLen;
	}
	
	public void clearCache(String packageName) {
		File cacheFile = new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + packageName + "/cache/");
		if (cacheFile.length() > 0) {
			AndroidUtils.deleteFile(cacheFile);
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
//		if (convertView == null) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.task_manager_list_item, null);
		holder = new ViewHolder();
		holder.mAppIcon = (ImageView) convertView.findViewById(R.id.app_icon);
		holder.mAppWarning = (ImageView) convertView
				.findViewById(R.id.app_warning);
		holder.mAppName = (TextView) convertView.findViewById(R.id.app_name);
		holder.mFlowValue = (TextView) convertView
				.findViewById(R.id.flow_value);
		holder.mPowerboot = (TextView) convertView.findViewById(R.id.powerboot);
		holder.mCacheVal = (TextView) convertView
				.findViewById(R.id.cache_value);
		holder.mRAMUsageVal = (TextView) convertView
				.findViewById(R.id.ram_usage_value);
		holder.mRAMUsageProgressbar = (ProgressBar) convertView.findViewById(R.id.ram_usage_progressbar);
		holder.mClearBtn = (ImageView) convertView.findViewById(R.id.clear_btn);
		holder.mFinishBtn = (ImageView) convertView
				.findViewById(R.id.finish_btn);
		holder.mFinishCB = (CheckBox) convertView
				.findViewById(R.id.finish_checkbox);
		holder.mItemLayout = convertView
				.findViewById(R.id.task_mngr_item_layout);
		holder.mForbidBootBtn = (ImageView) convertView
				.findViewById(R.id.forbid_boot);
		holder.mDetailBtn = (ImageView) convertView.findViewById(R.id.show_detail);
		holder.mUninstallBtn = (ImageView) convertView
				.findViewById(R.id.uninstall_app);
//		convertView.setTag(holder);
//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
		
		convertView.setTag(position);
		convertView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
						menuDisplayCtrl(v);
						po = (Integer)v.getTag();
					break;

				default:
					break;
					
				}
				return false;
			}
		});

		final InstalledAppInfo appInfo = data.get(position);
		holder.mAppIcon.setImageDrawable(appInfo.getIcon());
		holder.mAppName.setText(appInfo.getName());
//		System.out.println("app name====>" + appInfo.getName() + ", pkg name=====>" + appInfo.getPkgName());
		String[] permissionList = AndroidUtils.getPermissionList(context, appInfo.getPkgName());
		if (HighRiskPermissionExpl.getInstance().hasHighRiskPermission(permissionList)) {
			if (permission_DB.select(appInfo.getPkgName()) == 1) {
				holder.mAppWarning.setVisibility(View.GONE);
			} else {
				holder.mAppWarning.setVisibility(View.VISIBLE);
			}
		} else {
			holder.mAppWarning.setVisibility(View.GONE);
		}

//		long appRxBytes = TrafficStats.getUidRxBytes(appInfo.getUid());
//		long appTxBytes = TrafficStats.getUidTxBytes(appInfo.getUid());
		holder.mFlowValue.setText(LauncherUtils.sizeFormat(service.select(appInfo.getPkgName()) + AndroidUtils.getRxAndTxBytes(appInfo.getUid())));

		holder.mAppWarning.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPermissionDialog(appInfo.getPkgName());
				holder.mAppWarning.setVisibility(View.GONE);
				permission_DB.add(new ShowPermissionRecord(appInfo.getPkgName(), 1));
			}
		});
		
		if (AndroidUtils.canPowerboot(context, appInfo.getPkgName())) {
			if (powerboot_DB.select(appInfo.getPkgName()) == 1) {
				holder.mPowerboot.setText("开机启动");
				holder.mForbidBootBtn.setImageResource(R.drawable.forbid_boot_icon_1);
			} else {
				holder.mPowerboot.setText("禁止开机启动");
				holder.mForbidBootBtn.setImageResource(R.drawable.forbid_boot_icon_2);
			}
			holder.mPowerboot.setVisibility(View.VISIBLE);
			holder.mForbidBootBtn.setVisibility(View.VISIBLE);
		} else {
			holder.mPowerboot.setVisibility(View.GONE);
			holder.mForbidBootBtn.setVisibility(View.GONE);
		}
		
//		final String cacheVal = getCacheSize(appInfo.getPkgName());
//		holder.mCacheVal.setText(cacheVal);
		final String cacheVal = Formatter.formatFileSize(context, appInfo.getCacheVal());
		holder.mCacheVal.setText(cacheVal);
		float percentVal = appInfo.getUsageMemory() * 100.f / allUsageSum;
//		holder.mRAMUsageVal.setText(LauncherUtils.sizeFormat(appInfo.getUsageMemory() * 1024) + "(" + String.format("%.2f", percentVal) + "%)");
		holder.mRAMUsageVal.setText(Formatter.formatFileSize(context, appInfo.getUsageMemory() * 1024) + "(" + String.format("%.2f", percentVal) + "%)");
		holder.mRAMUsageProgressbar.setProgress((int)percentVal);

		holder.mClearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDialog == null) {
					mDialog = new TaskMngrDialog(context);
				}
				mDialog.setMsg1("是否确定清除缓存数据?").showMsg2(true).setAppName(appInfo.getName()).setCacheValue(cacheVal).setConfirmListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						clearCache(appInfo.getPkgName());
						holder.mCacheVal.setText("0B");
						mDialog.dismiss();
					}
				}).setCancelListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
					}
				}).show();
			}
		});
		holder.mFinishBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDialog == null) {
					mDialog = new TaskMngrDialog(context);
				}
				mDialog.setMsg1("是否确定关闭程序?").showMsg2(false).setConfirmListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						LauncherUtils.stopApp(context, appInfo);
						data.remove(position);
						notifyDataSetChanged();
						handler2.sendEmptyMessage(TaskMngrChannel.UPDATE_APP_COUNT);
						handler.sendEmptyMessage(RAM_USAGE_PERSENT);
						location = position;
						mDialog.dismiss();
					}
				}).setCancelListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
					}
				}).show();
			}
		});

		if (showCheckBox) {
			if (chosenList.contains(appInfo)) {
				holder.mFinishCB.setChecked(true);
			} else {
				holder.mFinishCB.setChecked(false);
			}
			holder.mFinishCB.setVisibility(View.VISIBLE);
//			holder.mClearBtn.setVisibility(View.GONE);
			holder.mFinishBtn.setVisibility(View.GONE);
		} else {
			holder.mFinishCB.setVisibility(View.GONE);
//			holder.mClearBtn.setVisibility(View.VISIBLE);
			holder.mFinishBtn.setVisibility(View.VISIBLE);
		}
		holder.mFinishCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							chosenList.add(appInfo);
//							System.out.println(chosenList.size());
						} else {
							chosenList.remove(appInfo);
//							System.out.println(chosenList.size());
						}
					}
				});
		
		holder.mItemLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				menuDisplayCtrl(v);
			}
		});
		
		holder.mForbidBootBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((Activity)context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						forbidPowerBootDialog(holder, appInfo);
					}
				});
			}
		});
		holder.mDetailBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AndroidUtils.showInstalledAppDetails(context,
						appInfo.getPkgName());
			}
		});
		
		if (appInfo.getFlag() == 1) {
			holder.mUninstallBtn.setVisibility(View.GONE);
		}
		holder.mUninstallBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uninstalledPkgName = appInfo.getPkgName();
				AndroidUtils.uninstallApp(context, uninstalledPkgName);
			}
		});
		return convertView;
	}
	
	/**
	 * 菜单的显隐控制
	 * @param v
	 */
	private void menuDisplayCtrl(View v) {
		if (menuLayout != null && menuLayout != v.findViewById(R.id.menu_layout)) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 0);
			lp.setMargins(0, 0, 0, -menuLayout.getHeight());
			menuLayout.setLayoutParams(lp);
		}
		
		menuLayout = v.findViewById(R.id.menu_layout);
		final int height = menuLayout.getHeight();
		menuMarginBottom = ((LinearLayout.LayoutParams)menuLayout.getLayoutParams()).bottomMargin;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (menuMarginBottom < 0) {
					if (po == getCount() - 1) {
						menuMarginBottom =  archo_temp;
						handler.sendEmptyMessage(MENU_DISPLAY_CTRL_LAST);
					} else {
						while (menuMarginBottom < archo_temp) {
							menuMarginBottom += 5;
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							handler.sendEmptyMessage(MENU_DISPLAY_CTRL);
						}
						
					}
				} else {
					while (menuMarginBottom > -height) {
						menuMarginBottom -= 5;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(MENU_DISPLAY_CTRL);
					}
				}
			}
		}).start();
	}
	
	/*Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 0);
			switch (msg.what) {
			case KILL_RUNNING_PROCESS:
				handler2.sendEmptyMessage(TaskMngrChannel.UPDATE_APP_COUNT);
				data.remove(location);
				notifyDataSetChanged();
				break;
			case MENU_DISPLAY_CTRL:
				lp.setMargins(0, 0, 0, menuMarginBottom);
				menuLayout.setLayoutParams(lp);
				menuLayout.postInvalidate();
				break;
			default:
				break;
			}
		}
		
	};*/
	
	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 0);
			switch (msg.what) {
			case KILL_RUNNING_PROCESS:
				handler2.sendEmptyMessage(TaskMngrChannel.UPDATE_APP_COUNT);
				data.remove(location);
				((Activity)context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
//						allUsageSum = getAllRamUsage();
						handler.sendEmptyMessage(RAM_USAGE_PERSENT);
						notifyDataSetChanged();
					}
				});
				break;
			case MENU_DISPLAY_CTRL:
				((Activity)context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						lp.setMargins(0, 0, 0, menuMarginBottom);
						menuLayout.setLayoutParams(lp);
						menuLayout.postInvalidate();
					}
				});
				break;
			case MENU_DISPLAY_CTRL_LAST:
				((Activity)context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						lp.setMargins(0, 0, 0, menuMarginBottom);
						menuLayout.setLayoutParams(lp);
						menuLayout.postInvalidate();
						lv.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								lv.smoothScrollToPosition(po);
//								System.out.println("滑动--->");
							}
						}, 100);
					}
				});
				break;
			case RAM_USAGE_PERSENT:
				allUsageSum = getAllRamUsage();
				break;
			case CALCULATE_CACHE_VALUE:
				
				break;
				
			case REFRESH_UNINSTALL:
				((Activity)context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						removeAppDataByPackageName(uninstalledPkgName);
					}
				});
				break;
			default:
				break;
			}
		}
		
	}

	class ViewHolder {
		ImageView mAppIcon, mAppWarning, mForbidBootBtn, mDetailBtn, mUninstallBtn;
		TextView mAppName, mFlowValue, mPowerboot, mCacheVal, mRAMUsageVal;
		ImageView mClearBtn, mFinishBtn;
		CheckBox mFinishCB;
		View mItemLayout;
		ProgressBar mRAMUsageProgressbar;
	}
	
	private void showPermissionDialog(final String pkgName) {
		if (warningDialog == null) {
			warningDialog = new HighRiskWarningDialog(context, pkgName);
		} else {
			warningDialog.setPermissionList(pkgName);
		}
		warningDialog.setPositionOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				AndroidUtils.uninstallApp(context, pkgName);
				uninstalledPkgName = pkgName;
				AndroidUtils.uninstallApp(context, uninstalledPkgName);
				warningDialog.dismiss();
			}
		}).setNegativeOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AndroidUtils.showInstalledAppDetails(context,
						pkgName);
				warningDialog.dismiss();
			}
		});
		warningDialog.show();
	}

	private void forbidPowerBootDialog(final ViewHolder holder, final InstalledAppInfo appInfo) {
		if (mDialog == null) {
			mDialog = new TaskMngrDialog(context);
		}
		if (powerboot_DB.select(appInfo.getPkgName()) == 1) {
			
			mDialog.setMsg1("确定禁止开机启动?").showMsg2(false).setConfirmListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					PowerbootRecord record = new PowerbootRecord();
					record.setPkgName(appInfo.getPkgName());
					record.setState(0);
					record.setUid(appInfo.getUid());
					powerboot_DB.update(record);
					holder.mPowerboot.setText("禁止开机启动");
					holder.mForbidBootBtn.setImageResource(R.drawable.forbid_boot_icon_2);
					mDialog.dismiss();
				}
			}).setCancelListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					PowerbootRecord record = new PowerbootRecord();
//					record.setPkgName(appInfo.getPkgName());
//					record.setState(1);
//					powerboot_DB.update(record);
//					holder.mPowerboot.setText("开机启动");
//					holder.mForbidBootBtn.setImageResource(R.drawable.forbid_boot_icon_1);
					mDialog.dismiss();
				}
			}).show();
			
		} else {
			
			mDialog.setMsg1("确定使用开机启动?").showMsg2(false)
					.setConfirmListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							PowerbootRecord record = new PowerbootRecord();
//							record.setPkgName(appInfo.getPkgName());
//							record.setState(0);
//							powerboot_DB.update(record);
//							holder.mPowerboot.setText("禁止开机启动");
//							holder.mForbidBootBtn
//									.setImageResource(R.drawable.forbid_boot_icon_2);
							PowerbootRecord record = new PowerbootRecord();
							record.setPkgName(appInfo.getPkgName());
							record.setState(1);
							record.setUid(appInfo.getUid());
							powerboot_DB.update(record);
							holder.mPowerboot.setText("开机启动");
							holder.mForbidBootBtn.setImageResource(R.drawable.forbid_boot_icon_1);
							mDialog.dismiss();
						}
					}).setCancelListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							PowerbootRecord record = new PowerbootRecord();
//							record.setPkgName(appInfo.getPkgName());
//							record.setState(1);
//							powerboot_DB.update(record);
//							holder.mPowerboot.setText("开机启动");
//							holder.mForbidBootBtn
//									.setImageResource(R.drawable.forbid_boot_icon_1);
							mDialog.dismiss();
						}
					}).show();
			
		}
	}
	
	private String sizeFormat(long size) {
		if (size < 1024) {
			return String.valueOf(size) + "B";
		}else if (size > 1024 * 1024) {
			float size_mb = (float) size / 1024 / 1024f;
			return String.format("%.2f", size_mb) + "M";
		}else {
			float size_kb = (float) size / 1024f;
			return String.format("%.2f", size_kb) + "K";
//			return size_kb + "K";
		}
	}

}
