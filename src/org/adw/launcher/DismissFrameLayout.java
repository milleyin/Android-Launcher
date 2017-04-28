package org.adw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class DismissFrameLayout extends FrameLayout {

	DismissListener dismissListener;
	
	public DismissFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(ev.getAction()==MotionEvent.ACTION_DOWN && dismissListener!=null )
		{
			dismissListener.dismiss();
		}
		return false;
	}
	interface DismissListener{
		void dismiss();
	}

	public void setDismissListener(DismissListener dismissListener) {
		this.dismissListener = dismissListener;
	}
}
