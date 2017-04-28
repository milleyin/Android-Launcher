package com.dongji.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dongji.launcher.R;

public class MyAnimationFrameLayout extends FrameLayout {

	int lastX;
	int lastY;
	
	int targetX;
	int targetY;
	
	int distanceX;
    int distanceY;
	
	boolean isAnimating  = false; //是否正在播放动画 
	Bitmap actor;
	Bitmap bg;
	
	ImageView img;
	
	AbsoluteLayout anima_layout;
	
	public MyAnimationFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		actor = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		
		img = new ImageView(context);
	}

	Handler handler = new Handler()
	{
		public void handleMessage(android.os.Message msg) {
			invalidate();
		};
	};
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		if(isAnimating)
		{
			return true;
		}
//		if(ev.getAction() == MotionEvent.ACTION_DOWN)
//		{
//			lastX = (int) ev.getX();
//			lastY = (int) ev.getX();
//		}
//		
//		System.out.println("  onInterceptTouchEvent  lastX  --->" + lastX +"lastY --->" + lastY);
		
		return false;
	}
	
	public void triggler(int sourceX,int sourceY,int tX,int tY)
	{
		this.lastX = sourceX;
		this.lastY = sourceY;
		
		setDrawingCacheEnabled(true);
		buildDrawingCache();
		
		bg = Bitmap.createBitmap(getDrawingCache());
		
		isAnimating = true;
		
		targetX = tX;
		targetY = tY;
		
		distanceX = targetX - lastX;
		distanceY = targetY - lastY;
		
		img.setImageBitmap(actor);
		
		AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT,AbsoluteLayout.LayoutParams.WRAP_CONTENT,lastX,lastY);
		anima_layout.addView(img);
		anima_layout.setBackgroundDrawable(new BitmapDrawable(bg));
		
		TranslateAnimation ta = new TranslateAnimation(lastX, targetX, lastY, targetY);
		ta.setDuration(3000);
		
		ta.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				anima_layout.removeAllViews();
				isAnimating = false;
			}
		});
		
		img.setAnimation(ta);
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				
//				while(isAnimating)
//				{
//					
//					boolean isFinshX = false;
//					boolean isFinshY = false;
//					
//					
//					if(distanceX>0 && lastX<targetX)
//					{
//						lastX +=distanceX/10;
//					}else if (distanceX<0 && lastX>targetX ){
//						lastX +=distanceX/10;
//					}else{
//						isFinshX = true;
//					}
//					
//					
//					if(distanceY>0 && lastY<targetY)
//					{
//						lastY +=distanceY/10;
//					}else if (distanceY<0 && lastX>targetY ){
//						lastY +=distanceY/10;
//					}else{
//						isFinshY = true;
//					}
//
//					System.out.println(" lastX  --->" + lastX +"lastY --->" + lastY);
//					
//					try {
//						Thread.sleep(40);
//					} catch (Exception e) {
//					}
//					
//					handler.sendEmptyMessage(1);
//					
//					if(isFinshX && isFinshY)
//					{
//						isAnimating = false;
//						System.out.println("  动画结束");
//					}
//					
//					
//				}
//				
//			}
//		}).start();
	}
	
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
//		if(!isAnimating)
//		{
			super.dispatchDraw(canvas);
//		}
	}

	public void setAnima_layout(AbsoluteLayout anima_layout) {
		this.anima_layout = anima_layout;
	}
	
}
