package com.dongji.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dongji.launcher.R;

public class TaskMngrDialog extends Dialog {
	
	private Context context;
	private View mContentView, mMsgLayout2;
	private TextView mMsg1TV, mAppNameTV, mCacheValueTV;
	private Button mConfirmBtn, mCancelBtn;

	public TaskMngrDialog(Context context) {
		super(context, R.style.dialog);
		this.context = context;
		initView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCanceledOnTouchOutside(true);
		setContentView(mContentView);
	}

	private void initView() {
		mContentView = LayoutInflater.from(context).inflate(R.layout.layout_taskmngr_dialog, null);
		mMsg1TV = (TextView) mContentView.findViewById(R.id.dialog_msg_1);
		mAppNameTV = (TextView) mContentView.findViewById(R.id.app_name);
		mCacheValueTV = (TextView) mContentView.findViewById(R.id.cache_value);
		mConfirmBtn = (Button) mContentView.findViewById(R.id.dialog_confirm);
		mCancelBtn = (Button) mContentView.findViewById(R.id.dialog_cancel);
		mMsgLayout2 = mContentView.findViewById(R.id.dialog_msg2_layout);
		
		setDefaultOnClickListener();
	}
	
	public TaskMngrDialog setMsg1(String msg) {
		mMsg1TV.setText(msg);
		return this;
	}
	
	public TaskMngrDialog setMsg1(int id) {
		mMsg1TV.setText(id);
		return this;
	}
	
	public TaskMngrDialog setAppName(String appName) {
		mAppNameTV.setText(appName);
		return this;
	}
	
	public TaskMngrDialog setAppName(int id) {
		mAppNameTV.setText(id);
		return this;
	}
	
	public TaskMngrDialog setCacheValue(String cacheValue) {
		mCacheValueTV.setText(cacheValue);
		return this;
	}
	
	public TaskMngrDialog setCacheValue(int id) {
		mCacheValueTV.setText(id);
		return this;
	}
	
	public TaskMngrDialog showMsg2(boolean flag) {
		if (flag) {
			mMsgLayout2.setVisibility(View.VISIBLE);
		} else {
			mMsgLayout2.setVisibility(View.GONE);
		}
		return this;
	}
	
	private void setDefaultOnClickListener() {
		mConfirmBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public TaskMngrDialog setConfirmListener(View.OnClickListener listener) {
		if (listener != null) {
			mConfirmBtn.setOnClickListener(listener);
		}
		return this;
	}
	
	public TaskMngrDialog setCancelListener(View.OnClickListener listener) {
		if (listener != null) {
			mCancelBtn.setOnClickListener(listener);
		}
		return this;
	}
	
	/*private String getVersionName() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			System.out.println("version name=======>" + info.versionName);
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/
	
}
