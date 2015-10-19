package com.lt.view;

import java.util.NoSuchElementException;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class StickyLayout extends LinearLayout {

	private View mHeader;
	private View mContent;

	private int mHeaderHeight;
	private int mOriginalHeaderHeight;

	private int mStatus = STATUS_EXPANDED;
	private static final int STATUS_EXPANDED = 1;
	private static final int STATUS_COLLAPSED = 2;

	private int mTouchSlop;

	private int mLastX = 0;
	private int mLastY = 0;

	private int mLastXIntercept = 0;
	private int mLastYIntercept = 0;

	private boolean mIsSticky = true;
	private boolean mInitDataSucceed = false;

	private OnGiveUpTouchEventListener mGiveUpTouchEventListener;

	public interface OnGiveUpTouchEventListener {
		public boolean giveUpTouchEvent(MotionEvent event);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public StickyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public StickyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StickyLayout(Context context) {
		super(context);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus && (mHeader == null || mContent == null)) {
			initData();
		}
	}

	private void initData() {
		int headerId = getResources().getIdentifier("sticky_header", "id",
				getContext().getPackageName());
		int contentId = getResources().getIdentifier("sticky_content", "id",
				getContext().getPackageName());
		if (headerId != 0 && contentId != 0) {
			mHeader = findViewById(headerId);
			mContent = findViewById(contentId);
			mOriginalHeaderHeight = mHeader.getMeasuredHeight();
			mHeaderHeight = mOriginalHeaderHeight;
			mTouchSlop = ViewConfiguration.get(getContext())
					.getScaledTouchSlop();
			if (mHeaderHeight > 0) {
				mInitDataSucceed = true;
			}
		} else {
			throw new NoSuchElementException(
					"Did your view with id \"sticky_header\" or \"sticky_content\" exists?");
		}
	}

	public void setOnGiveUpTouchEventListener(OnGiveUpTouchEventListener l) {
		mGiveUpTouchEventListener = l;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int intercepted = 0;
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastXIntercept = x;
			mLastY = y;
			mLastYIntercept = y;
			intercepted = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			int distanceX = x - mLastXIntercept;
			int distanceY = y - mLastYIntercept;
			if (y <= getHeaderHeight()) {
				intercepted = 0;
			} else if (Math.abs(distanceY) <= Math.abs(distanceX)) {
				intercepted = 0;
			} else if (mStatus == STATUS_EXPANDED && distanceY <= -mTouchSlop) {
				intercepted = 1;
			} else if (mGiveUpTouchEventListener != null) {
				if (mGiveUpTouchEventListener.giveUpTouchEvent(ev)
						&& distanceY >= mTouchSlop) {
					intercepted = 1;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			intercepted = 0;
			mLastXIntercept = mLastYIntercept = 0;
			break;

		default:
			break;
		}
		return intercepted != 0 && mIsSticky;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsSticky) {
			return true;
		}
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			int distanceY = y - mLastY;
			mHeaderHeight += distanceY;
			setHeaderHeight(mHeaderHeight);
			break;
		case MotionEvent.ACTION_UP:
			int toHeight = 0;
			if (mHeaderHeight <= mOriginalHeaderHeight * 0.5) {
				toHeight = 0;
				mStatus = STATUS_COLLAPSED;
			} else {
				toHeight = mOriginalHeaderHeight;
				mStatus = STATUS_EXPANDED;
			}
			
			this.smoothSetHeaderHeight(mHeaderHeight, toHeight, 500);
			break;
			
		default:
			break;
		}
		mLastX=x;
		mLastY=y;
		return true;
	}
	
	public void smoothSetHeaderHeight(final int from, final int to,
			long duration){
		smoothSetHeaderHeight(from, to, duration, false);
		
	}

	public void smoothSetHeaderHeight(final int from, final int to,
			long duration, final boolean modifyOriginalHeaderHeight) {
		final int frameCount=(int)(duration/1000f*30)+1;
		final float partation=(to-from)/(float)frameCount;
		new Thread("Thread#smoothSetHeaderHeight"){

			@Override
			public void run() {
				for(int i=0;i<frameCount;i++){
					final int height;
					if(i==frameCount-1){
						height=to;
					}else{
						height=(int)(from+partation*i);
					}
					post(new Runnable() {
						public void run() {
							setHeaderHeight(height);
						}
					});
					try{
						sleep(10);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
				
				if(modifyOriginalHeaderHeight){
					setOriginalHeaderHeight(to);
				}
			}
			
		}.start();
	}
	
	public void setOriginalHeaderHeight(int height){
		mOriginalHeaderHeight=height;
	}

	public void setHeaderHeight(int height) {
		if (!mInitDataSucceed) {
			initData();
		}

		if (height <= 0) {
			height = 0;
		} else if (height > mOriginalHeaderHeight) {
			height = mOriginalHeaderHeight;
		}

		if (height == 0) {
			mStatus = STATUS_COLLAPSED;
		} else {
			mStatus = STATUS_EXPANDED;
		}

		if (mHeader != null && mHeader.getLayoutParams() != null) {
			mHeader.getLayoutParams().height = height;
			mHeader.requestLayout();
			mHeaderHeight = height;
		} else {
			Log.e("lt", "null LayoutParams when setHeaderHeight");
		}
	}

	private int getHeaderHeight() {
		return mHeaderHeight;
	}

}
