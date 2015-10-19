package com.lt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;

public class PinnedHeaderExpandableListView extends ExpandableListView
		implements OnScrollListener {

	private View mHeaderView;
	private int mHeaderWidth;
	private int mHeaderHeight;

	private View mTouchTarget;

	private boolean mIsActionDown = false;
	private boolean mIsHeaderGroupClickable = true;

	private OnHeaderUpdateListener mHeaderUpdateListener;

	/**
	 * 头部更新接口
	 * 
	 * @author taoliu
	 *
	 */
	public interface OnHeaderUpdateListener {
		/**
		 * 获取头部
		 * 
		 * @return
		 */
		public View getPinnedHeader();

		/**
		 * 对头部进行更新
		 * 
		 * @param headerView
		 * @param firstVisibleGroupPos
		 */
		public void updatePinnedHeader(View headerView, int firstVisibleGroupPos);
	}

	public PinnedHeaderExpandableListView(Context context) {
		super(context);
		initView();
	}

	public PinnedHeaderExpandableListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setFadingEdgeLength(0);
		setOnScrollListener(this);
	}

	public void setOnHeaderUpdateListener(OnHeaderUpdateListener listener) {
		mHeaderUpdateListener = listener;
		if (listener == null) {
			mHeaderView = null;
			mHeaderWidth = mHeaderHeight = 0;
			return;
		}
		mHeaderView = listener.getPinnedHeader();
		int firstVisiblePos = getFirstVisiblePosition();
		int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));
		listener.updatePinnedHeader(mHeaderView, firstVisibleGroupPos);
		requestLayout();
		postInvalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mHeaderView == null) {
			return;
		}
		measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
		mHeaderWidth = mHeaderView.getMeasuredWidth();
		mHeaderHeight = mHeaderView.getMeasuredHeight();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mHeaderView == null) {
			return;
		}
		int top = mHeaderView.getTop();
		mHeaderView.layout(0, top, mHeaderWidth, mHeaderHeight + top);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeaderView != null) {
			drawChild(canvas, mHeaderView, getDrawingTime());
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		int pos = pointToPosition(x, y);
		if (mHeaderView != null && y >= mHeaderView.getTop()
				&& y <= mHeaderView.getBottom()) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				mTouchTarget = getTouchTarget(mHeaderView, x, y);
				mIsActionDown = true;
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				View touchTarget = getTouchTarget(mHeaderView, x, y);
				if (touchTarget == mTouchTarget && mTouchTarget.isClickable()) {
					mTouchTarget.performClick();
					invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
				} else if (mIsHeaderGroupClickable) {
					int groupPosition = getPackedPositionGroup(getExpandableListPosition(pos));
					if (groupPosition != INVALID_POSITION && mIsActionDown) {
						if (isGroupExpanded(groupPosition)) {
							collapseGroup(groupPosition);
						} else {
							expandGroup(groupPosition);
						}
					}
				}
				mIsActionDown = false;
			}
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 得到触摸到的view
	 * 
	 * @param view
	 * @param x
	 * @param y
	 * @return
	 */
	private View getTouchTarget(View view, int x, int y) {
		if (!(view instanceof ViewGroup)) {
			return view;
		}
		ViewGroup parent = (ViewGroup) view;
		int childrenCount = parent.getChildCount();
		final boolean custmOrder = isChildrenDrawingOrderEnabled();
		View target = null;
		for (int i = childrenCount - 1; i >= 0; i--) {
			final int childIndex = custmOrder ? getChildDrawingOrder(
					childrenCount, i) : i;
			final View child = parent.getChildAt(childIndex);
			if (isTouchPointInView(child, x, y)) {
				target = child;
				break;
			}
		}
		if (target == null) {
			target = parent;
		}
		return target;
	}

	/**
	 * 判断所触摸的点是否在view内
	 * 
	 * @param view
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchPointInView(View view, int x, int y) {
		if (view.isClickable() && y >= view.getTop() && y <= view.getBottom()
				&& x >= view.getLeft() && x <= view.getRight()) {
			return true;
		}
		return false;
	}

	/**
	 * 刷新头部
	 */
	protected void refreshHeader() {
		if (mHeaderView == null) {
			return;
		}
		int firstVisiblePos = getFirstVisiblePosition();
		int secondVisiblepos = firstVisiblePos + 1;
		int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));
		int secondVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(secondVisiblepos));

		if (secondVisibleGroupPos == firstVisibleGroupPos + 1) {
			View view = getChildAt(1);
			if (view == null) {
				return;
			}
			if (view.getTop() <= mHeaderHeight) {
				int top = mHeaderHeight - view.getTop();
				mHeaderView.layout(0, -top, mHeaderWidth, mHeaderHeight-top);
			} else {
				mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
			}
		} else {
			mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
		}

		if (mHeaderUpdateListener != null) {
			mHeaderUpdateListener.updatePinnedHeader(mHeaderView,
					firstVisibleGroupPos);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (totalItemCount > 0) {
			refreshHeader();
		}
	}

}
