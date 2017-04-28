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


public class ScrollLayout extends ViewGroup{
	private static final String TAG = "ScrollLayout";  
    private Scroller mScroller;  
    private VelocityTracker mVelocityTracker;  
      
    private  int mCurScreen;  
    private int mDefaultScreen = 0;  
      
    private static final int TOUCH_STATE_REST = 0;  
    private static final int TOUCH_STATE_SCROLLING = 1;  
      
    private static final int SNAP_VELOCITY = 600;  
      
    private int mTouchState = TOUCH_STATE_REST;  
    private int mTouchSlop; 
    private int mTouchYSlop ;   
    private float mLastMotionX;  
    private float mLastMotionY;  
    private OnCurrentViewChangedListener mOnCurrentViewChangedListener;
    
    private OnScrollerFinish onScrollerFinish;
    boolean isFinish = false;
    
    public OnCurrentViewChangedListener getmOnCurrentViewChangedListener() {  
        return mOnCurrentViewChangedListener;  
    }  
  
    public void setmOnCurrentViewChangedListener(  
            OnCurrentViewChangedListener mOnCurrentViewChangedListener) {  
        this.mOnCurrentViewChangedListener = mOnCurrentViewChangedListener;  
    }  
  
    public interface OnCurrentViewChangedListener {  
  
        public void onCurrentViewChanged(View view, int currentview);  
    }  
      
    public void removeOnScrollerFinish()
    {
    	this.onScrollerFinish = null;
    	this.isFinish = false;
    }
    
    public void setOnScrollerFinish(OnScrollerFinish onScrollerFinish) {
		this.onScrollerFinish = onScrollerFinish;
	}
    public ScrollLayout(Context context, AttributeSet attrs) {  
        this(context, attrs, 0);  
    }  
  
    public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        mScroller = new Scroller(context);  
          
        mCurScreen = mDefaultScreen;  
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();  
    }  
  
    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b) {  
//        if (changed) {  
            int childLeft = 0;  
            final int childCount = getChildCount();  
              
            for (int i=0; i<childCount; i++) {  
                final View childView = getChildAt(i);  
                if (childView.getVisibility() != View.GONE) {  
                    final int childWidth = childView.getMeasuredWidth();  
                    try {
                        childView.layout(childLeft, 0, childLeft+childWidth, childView.getMeasuredHeight());  
					} catch (Exception e) {
						e.printStackTrace();
					}
                    childLeft += childWidth;  
                }  
            }  
//        }  
    }  
  
  
    @Override    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {     
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);     
    
        final int width = MeasureSpec.getSize(widthMeasureSpec);   
    
        final int count = getChildCount();    
        for (int i = 0; i < count; i++) {     
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);     
        }     
        scrollTo(mCurScreen * width, 0);           
    }    
      
    /** 
     * According to the position of current layout 
     * scroll to the destination page. 
     */  
    public void snapToDestination() {  
        final int screenWidth = getWidth();  
        final int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;  
        snapToScreen(destScreen);  
    }  
      
    public void snapToScreen(int whichScreen) {  
    	
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));  
        
        if (getScrollX() != (whichScreen*getWidth())) {  
              
            final int delta = whichScreen*getWidth()-getScrollX();  
            mScroller.startScroll(getScrollX(), 0, delta, 0, 500);  
            mCurScreen = whichScreen;  
              
            if (mOnCurrentViewChangedListener != null) {  
                mOnCurrentViewChangedListener.onCurrentViewChanged(this,mCurScreen);  
            }  
            
            invalidate();  
        	isFinish = false;
        }  
    }  
      
    public void setToScreen(int whichScreen) {  
    	
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));  
        mCurScreen = whichScreen;  
        
        scrollTo(whichScreen*getWidth(), 0);  
    }  
      
    public int getCurScreen() {  
        return mCurScreen;  
    }  
      
    @Override  
    public void computeScroll() {  
        if (mScroller.computeScrollOffset()) {  
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());  
            postInvalidate();  
        } else{
        	if(onScrollerFinish!= null && !isFinish)
        	{
        		onScrollerFinish.onScrollerFinish();
        		isFinish = true;
        	}
        } 
    }  
    
    
    public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	public interface OnScrollerFinish
    {
    	public void onScrollerFinish();
    }
}  
  

