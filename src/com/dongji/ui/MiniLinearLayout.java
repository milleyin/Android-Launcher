package com.dongji.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MiniLinearLayout extends LinearLayout {
	
	OnTouchActionUpListener onTouchActionUpListener;
	
	public MiniLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MiniLinearLayout(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		if(ev.getAction() == MotionEvent.ACTION_UP)
		{
			if(onTouchActionUpListener !=null)
			{
				onTouchActionUpListener.onUp();
			}
		}
		return false;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_UP)
			{
				if(onTouchActionUpListener !=null)
				{
					onTouchActionUpListener.onUp();
				}
			}
		return super.onTouchEvent(ev);
	}
	
	public interface OnTouchActionUpListener {
		void onUp();
	}


	public void setOnTouchActionUpListener(
			OnTouchActionUpListener onTouchActionUpListener) {
		this.onTouchActionUpListener = onTouchActionUpListener;
	}
}
