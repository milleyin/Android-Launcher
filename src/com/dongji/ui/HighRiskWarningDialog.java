package com.dongji.ui;

import java.util.List;

import com.dongji.enity.HighRiskPermissionExpl;
import com.dongji.enity.HighRiskPermissionExpl.PermissionCls;
import com.dongji.launcher.R;
import com.dongji.tool.AndroidUtils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HighRiskWarningDialog extends Dialog {
	
	private List<HighRiskPermissionExpl.PermissionCls> permissonList;
	private String pkgName;
	
	private Context context;
	private View mContentView;
	private ImageView mCloseDialogBtn;
	private Button mUninstallBtn, mDetailBtn;
	private LinearLayout mAuthListLayout;

	public HighRiskWarningDialog(Context context,String packageName) {
		super(context, R.style.dialog);
		this.context = context;
		this.pkgName = packageName;
		permissonList = HighRiskPermissionExpl.getInstance().getPermissionExplain(AndroidUtils.getPermissionList(context, packageName));
		initViews();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCanceledOnTouchOutside(true);
		setContentView(mContentView);
	}

	private void initViews() {
		mContentView = getLayoutInflater().inflate(R.layout.widget_high_risk_dialog, null);
		mCloseDialogBtn = (ImageView) mContentView.findViewById(R.id.close_dialog);
		mUninstallBtn = (Button) mContentView.findViewById(R.id.dialog_uninstall);
		mDetailBtn = (Button) mContentView.findViewById(R.id.dialog_detail);
		mAuthListLayout = (LinearLayout) mContentView.findViewById(R.id.authority_list_layout);
		
		initAuthList(permissonList);
		
		closeOnClickListener();
		defaultNegativeOnClickListener();
		defaultPositiveOnClickListener();
	}
	
	/**
	 * 初始化权限列表
	 * @param list
	 */
	private void initAuthList(List<HighRiskPermissionExpl.PermissionCls> list) {
		if (list != null && list.size() > 0) {
			for (PermissionCls cls : list) {
				View mItemLayout = getLayoutInflater().inflate(R.layout.item_permission_list, null);
				TextView authorityName = (TextView) mItemLayout.findViewById(R.id.authority_name);
				TextView authorityExplain = (TextView) mItemLayout.findViewById(R.id.authority_explain);
				authorityName.setText(cls.getPermissionName());
				authorityExplain.setText(cls.getPermissionExp());
				mAuthListLayout.addView(mItemLayout);
			}
		}
	}
	
	private void closeOnClickListener() {
		mCloseDialogBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	private void defaultPositiveOnClickListener() {
		mUninstallBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dismiss();
			}
		});
	}
	
	private void defaultNegativeOnClickListener() {
		mDetailBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dismiss();
			}
		});
	}
	
	public HighRiskWarningDialog setPositionOnClickListener(View.OnClickListener listener) {
		if (listener != null) {
			mUninstallBtn.setOnClickListener(listener);
		}
		return this;
	}
	
	public HighRiskWarningDialog setNegativeOnClickListener(View.OnClickListener listener) {
		if (listener != null) {
			mDetailBtn.setOnClickListener(listener);
		}
		return this;
	}
	
	public HighRiskWarningDialog setPermissionList(String packageName) {
		pkgName = packageName;
		permissonList = HighRiskPermissionExpl.getInstance().getPermissionExplain(AndroidUtils.getPermissionList(context, packageName));
		initAuthList(permissonList);
		return this;
	}

}
