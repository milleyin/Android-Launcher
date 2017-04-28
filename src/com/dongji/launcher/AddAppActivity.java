package com.dongji.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;


public class AddAppActivity extends Activity implements View.OnClickListener, OnLongClickListener {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_add_app);
	}

	@Override
	public boolean onLongClick(View v) {
		
		return false;
	}

	@Override
	public void onClick(View v) {
		
	}
}
