package org.adw.launcher;

import java.util.List;

import org.adw.launcher.AllDrawer.OnPackageListener;

import com.dongji.adapter.TaskMngrAdapter;
import com.dongji.enity.InstalledAppInfo;
import com.dongji.enity.PowerbootRecord;
import com.dongji.launcher.R;
import com.dongji.sqlite.TaskMngrDB;
import com.dongji.tool.AndroidUtils;
import com.dongji.tool.LauncherUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

public class TaskMngrChannel implements OnPackageListener{
	
	public static final int LOADING_THIRD_CONTENT = 0;
	public static final int LOADING_SYSTEM_CONTENT = 1;
	public static final int LOADING_PERMANENT_CONTENT = 2;
	public static final int ONE_CLICK_CLOSE = 3;
	public static final int CONFIRM_CLOSE = 4;
	public static final int CANCLE_CLOSE = 5;
	public static final int UPDATE_APP_COUNT = 6;
	
	private ListView mListView;
	private Button mConfirmBtn, mCancleBtn;
	private RadioButton mNormalBtn, mSystemBtn;
	private Button mOneKeyFinishIV;
	private TextView mProcessCountTV;
	private LinearLayout mCtrl_layout;
	private MyHandler mHandler;
	
	private List<InstalledAppInfo> data;
	private TaskMngrDB.Netraffic_Service service;
	private TaskMngrDB.Powerboot_DB powerboot_DB;
	private TaskMngrAdapter adapter;
	private View mLoadingProgress;
	private ProgressBar mLoadingProgressbar;
	private TextView mLoadingTextview;
	private View mTaskMngrLayout, mListLayout;
	private View mSuperView;
	private Launcher mLauncher;
	
	public TaskMngrChannel(View mSuperView, Launcher mLauncher) {
		this.mSuperView = mSuperView;
		this.mLauncher = mLauncher;
	}
	
	public View initViews() {
		mTaskMngrLayout = LayoutInflater.from(mLauncher).inflate(R.layout.task_manager_layout, null);
		mListLayout = mTaskMngrLayout.findViewById(R.id.list_layout);
		mCtrl_layout = (LinearLayout) mTaskMngrLayout.findViewById(R.id.ctrl_layout);
		mProcessCountTV = (TextView) mTaskMngrLayout.findViewById(R.id.process_count);
		mListView = (ListView) mTaskMngrLayout.findViewById(R.id.app_list);
//		mListView.setFocusableInTouchMode(true);
		
		initProgressBar();
		initHandler();
		initSectionBtn();
		service = new TaskMngrDB.Netraffic_Service(mLauncher);
		powerboot_DB = new TaskMngrDB.Powerboot_DB(mLauncher);
//		registReceiver();
		
		return mTaskMngrLayout;
	}
	
	private void initProgressBar() {
		mLoadingProgress = mTaskMngrLayout.findViewById(R.id.loading_layout);
		mLoadingProgressbar = (ProgressBar) mLoadingProgress.findViewById(R.id.loading_progressbar);
		mLoadingTextview = (TextView) mLoadingProgress.findViewById(R.id.loading_textview);
	}
	
	private void initSectionBtn() {
		mSystemBtn = (RadioButton) mTaskMngrLayout.findViewById(R.id.system_apps);
		mNormalBtn = (RadioButton) mTaskMngrLayout.findViewById(R.id.third_apps);
		mOneKeyFinishIV = (Button) mTaskMngrLayout.findViewById(R.id.close_oneClick);
		mConfirmBtn = (Button) mTaskMngrLayout.findViewById(R.id.confirm_btn);
		mCancleBtn = (Button) mTaskMngrLayout.findViewById(R.id.cancle_btn);
		mSystemBtn.setOnClickListener(listener);
		mNormalBtn.setOnClickListener(listener);
		mOneKeyFinishIV.setOnClickListener(listener);
		mConfirmBtn.setOnClickListener(listener);
		mCancleBtn.setOnClickListener(listener);
	}
	
	OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.third_apps:
//				mSystemBtn.setTextColor(Color.parseColor("#33b5e5"));
//				mNormalBtn.setTextColor(Color.parseColor("#ffffff"));
//				mSystemBtn.setBackgroundResource(R.drawable.btn_bg_gray);
//				mNormalBtn.setBackgroundResource(R.drawable.btn_bg_blue);
//				mListLayout.setBackgroundResource(R.drawable.arrow_left_bg);
				
				mHandler.sendEmptyMessage(LOADING_THIRD_CONTENT);
				mListView.setVisibility(View.GONE);
				mCtrl_layout.setVisibility(View.GONE);
				mLoadingProgress.setVisibility(View.VISIBLE);
				break;
			case R.id.system_apps:
//				mSystemBtn.setTextColor(Color.parseColor("#ffffff"));
//				mNormalBtn.setTextColor(Color.parseColor("#33b5e5"));
//				mNormalBtn.setBackgroundResource(R.drawable.btn_bg_gray);
//				mSystemBtn.setBackgroundResource(R.drawable.btn_bg_blue);
//				mListLayout.setBackgroundResource(R.drawable.arrow_left_bg2);
				
				mHandler.sendEmptyMessage(LOADING_SYSTEM_CONTENT);
				mListView.setVisibility(View.GONE);
				mCtrl_layout.setVisibility(View.GONE);
				mLoadingProgress.setVisibility(View.VISIBLE);
				break;
			case R.id.close_oneClick:
				if (mCtrl_layout.getVisibility() == View.GONE) {
					mHandler.sendEmptyMessage(ONE_CLICK_CLOSE);
				}
				break;
			case R.id.confirm_btn:
				mHandler.sendEmptyMessage(CONFIRM_CLOSE);
				break;
			case R.id.cancle_btn:
				mHandler.sendEmptyMessage(CANCLE_CLOSE);
				break;
			default:
				break;
			}
		}
	};
	
	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("myHandler");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
		mHandler.sendEmptyMessage(LOADING_THIRD_CONTENT);
		mLoadingProgress.setVisibility(View.VISIBLE);
	}
	
	private void updateData(List<InstalledAppInfo> list) {
		if (list == null || list.size() == 0) {
			mListView.setVisibility(View.GONE);
			mLoadingProgressbar.setVisibility(View.GONE);
			mLoadingTextview.setText("无数据");
			return;
		}
		if (adapter == null) {
			adapter = new TaskMngrAdapter(mLauncher, list, service, mHandler, mListView);
			mListView.setAdapter(adapter);
		} else {
			adapter.setShowCheckBox(false);
			adapter.reset();
			adapter.addData(list);
		}
		mListView.setVisibility(View.VISIBLE);
		mLoadingProgress.setVisibility(View.GONE);
		mProcessCountTV.setText("(" + adapter.getCount() + ")");
//		mCtrl_layout.setVisibility(View.GONE);
	}
	
	private void initPowerbootState(List<InstalledAppInfo> list) {
		for (InstalledAppInfo installedAppInfo : list) {
			if (AndroidUtils.canPowerboot(mLauncher, installedAppInfo.getPkgName())) {
				if (!powerboot_DB.isExist(installedAppInfo.getPkgName())) {
					PowerbootRecord record = new PowerbootRecord();
					record.setPkgName(installedAppInfo.getPkgName());
					record.setState(1);
					record.setUid(installedAppInfo.getUid());
					powerboot_DB.add(record);
				}
			}
		}
	}
	
	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case LOADING_THIRD_CONTENT:
				data = LauncherUtils.getRunningApps(mLauncher, LauncherUtils.FILTER_THIRD_APP);
				LauncherUtils.orderWithName(data);
				initPowerbootState(data);
				mLauncher.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						
						updateData(data);
					}
				});
				break;
			case LOADING_SYSTEM_CONTENT:
				data = LauncherUtils.getRunningApps(mLauncher, LauncherUtils.FILTER_SYSTEM_APP);
				LauncherUtils.orderWithName(data);
				initPowerbootState(data);
				mLauncher.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						
						updateData(data);
					}
				});
				break;
			case ONE_CLICK_CLOSE:
				mLauncher.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mCtrl_layout.setVisibility(View.VISIBLE);
						adapter.initChosenList();
						adapter.refreshCheckBox(true);
					}
				});
				break;
			case CONFIRM_CLOSE:
				mLauncher.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						List<InstalledAppInfo> list = adapter.getChosenList();
						if (list.size() > 0) {
							LauncherUtils.clearChosenApps(mLauncher, list);
//							for (InstalledAppInfo appInfo : list) {
//								adapter.removeAppDataByPackageName(appInfo.getPkgName());
//							}
//							System.out.println(data.size() + "============>");
//							data.removeAll(list);
//							System.out.println(data.size() + "============<");
//							adapter.reset();
//							adapter.addData(data);
							adapter.removeChosenApps(list);
						}
//						adapter.reset();
//						adapter.notifyDataSetChanged();
						mCtrl_layout.setVisibility(View.GONE);
						adapter.refreshCheckBox(false);
						mHandler.sendEmptyMessage(UPDATE_APP_COUNT);
					}
				});
				break;
			case CANCLE_CLOSE:
				mLauncher.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mCtrl_layout.setVisibility(View.GONE);
						adapter.refreshCheckBox(false);
					}
				});
				break;
			case UPDATE_APP_COUNT:
				mLauncher.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mProcessCountTV.setText("(" + adapter.getCount() + ")");
					}
				});
				break;
			default:
				break;
			}
		}
		
	}
	
	@Override
	public void onPackageAdded(String packageName, boolean replacing) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPackageRemoved(String packageName, boolean replacing) {
		System.out.println("uninstall app ======>" + replacing);
		if (!replacing) {
			if (adapter != null && adapter.uninstalledPkgName != null) {
//				adapter.removeAppDataByPackageName(adapter.uninstalledPkgName);
				adapter.refreshUninstall();
				mHandler.sendEmptyMessage(UPDATE_APP_COUNT);
			}
		}
	}
}
