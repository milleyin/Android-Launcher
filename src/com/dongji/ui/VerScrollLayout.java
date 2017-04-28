package com.dongji.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;


public class VerScrollLayout extends ViewGroup{
	private static final String TAG = "ScrollLayout";  
    private Scroller mScroller;  
    private VelocityTracker mVelocityTracker;  
      
    private  int mCurScreen;  
    private int mDefaultScreen = 0;  
      
    private static final int TOUCH_STATE_REST = 0;  
    private static final int TOUCH_STATE_SCROLLING = 1;  
      
    private static final int SNAP_VELOCITY = 600;  
      
    private int mTouchState = TOUCH_STATE_REST;  
    private int mTouchSlop = 120; 
    private int mTouchYSlop ;   //Y方向�?��滑动距离 
    private float mLastMotionX;  
    private float mLastMotionY;  
    private OnCurrentViewChangedListener mOnCurrentViewChangedListener;  
    
    private OnScrollerFinish onScrollerFinish;
    boolean isFinish = false;
    
    
    boolean isScrollerable = true; //是否可以左右滑动
    
    public OnCurrentViewChangedListener getmOnCurrentViewChangedListener() {  
        return mOnCurrentViewChangedListener;  
    }  
  
    public void setmOnCurrentViewChangedListener(  
            OnCurrentViewChangedListener mOnCurrentViewChangedListener) {  
        this.mOnCurrentViewChangedListener = mOnCurrentViewChangedListener;  
    }  
    
    public void setOnScrollerFinish(OnScrollerFinish onScrollerFinish) {
		this.onScrollerFinish = onScrollerFinish;
	}
    
    public void removeOnScrollerFinish()
    {
    	this.onScrollerFinish = null;
    	this.isFinish = false;
    }

	public interface OnCurrentViewChangedListener {  
  
        public void onCurrentViewChanged(View view, int currentview);  
    }  
      
    public VerScrollLayout(Context context, AttributeSet attrs) {  
        this(context, attrs, 0);  
    }  
  
    public VerScrollLayout(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        mScroller = new Scroller(context);  
          
        mCurScreen = mDefaultScreen;  
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();  
    }  
  
    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b) {  
            
//          if (changed) {  
            int childTop = 0;  
            final int childCount = getChildCount();  
              
            for (int i=0; i<childCount; i++) {  
                final View childView = getChildAt(i);  
                if (childView.getVisibility() != View.GONE) {  
                    final int childHeight = childView.getMeasuredHeight();  
                    try {
                        childView.layout(0, childTop, childView.getMeasuredWidth(), childTop+childHeight);  
					} catch (Exception e) {
						e.printStackTrace();
					}
                    childTop += childHeight;  
                }  
            }  
//        } 
    }  
  
  
    @Override    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
    	
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);     
    
        final int width = MeasureSpec.getSize(widthMeasureSpec);  
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);    
        
//        if (widthMode != MeasureSpec.EXACTLY) {     
//            throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");   
//        }     
    
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);    

//        if (heightMode != MeasureSpec.EXACTLY) {     
//            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");  
//        }     
    
        // The children are given the same width and height as the scrollLayout     
        final int count = getChildCount();    
        
        for (int i = 0; i < count; i++) {     
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);     
        }     
        scrollTo(0, mCurScreen * height);           
    }    
      
    
    /** 
     * According to the position of current layout 
     * scroll to the destination page. 
     */  
//    public void snapToDestination() {  
//        final int screenWidth = getWidth();  
//        final int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;  
//        snapToScreen(destScreen);  
//    }  
      
    
    public void snapToScreen(int whichScreen) {
    	
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));  
        
        if (getScrollY() != (whichScreen*getHeight())) {  
              
            final int delta = whichScreen*getHeight()-getScrollY(); 
            
            mScroller.startScroll(0, getScrollY(), 0, delta, 500);  
            mCurScreen = whichScreen;  
              
            if (mOnCurrentViewChangedListener != null) {  
                mOnCurrentViewChangedListener.onCurrentViewChanged(this,mCurScreen);  
            }  
            
            // Redraw the layout  
            invalidate();       
        }  
    }  
      
    public void setToScreen(int whichScreen) {  
    	
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));  
        mCurScreen = whichScreen;  
        scrollTo(0, whichScreen*getHeight());  
        
        if (mOnCurrentViewChangedListener != null) {  
            mOnCurrentViewChangedListener.onCurrentViewChanged(this,mCurScreen);  
        }  
        
    }  
      
    public int getCurScreen() {  
        return mCurScreen;  
    }  
      
    @Override  
    public void computeScroll() {  
        if (mScroller.computeScrollOffset()) {  
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());  
            postInvalidate();  
        }else{
        	if(onScrollerFinish!=null && !isFinish)
        	{
        		onScrollerFinish.onScrollerFinish();
        		isFinish = true;
        	}
        }
        
    }
    
    public interface OnScrollerFinish
    {
    	public void onScrollerFinish();
    }
}  
  

