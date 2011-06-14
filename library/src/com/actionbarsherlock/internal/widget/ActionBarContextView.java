package com.actionbarsherlock.internal.widget;

import com.actionbarsherlock.R;
import com.actionbarsherlock.internal.view.menu.ActionMenuView;
import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ActionMode;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActionBarContextView extends ViewGroup {
	private static final int ANIMATE_IDLE = 0;
	private static final int ANIMATE_IN = 1;
	private static final int ANIMATE_OUT = 2;
	
	private boolean mAnimateInOnLayout;
	private int mAnimationMode;
	private View mClose;
	private int mContentHeight;
	private Object mCurrentAnimation;
	private View mCustomView;
	private ActionMenuView mMenuView;
	private CharSequence mSubtitle;
	private int mSubtitleStyleRes;
	private TextView mSubtitleView;
	private CharSequence mTitle;
	private LinearLayout mTitleLayout;
	private int mTitleStyleRes;
	private TextView mTitleView;
	
	public ActionBarContextView(Context context) {
		this(context, null);
	}
	public ActionBarContextView(Context context, AttributeSet attrs) {
		this(context, attrs, R.style.Widget_Sherlock_ActionMode);
	}
	public ActionBarContextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SherlockActionMode, defStyle, 0);
		mTitleStyleRes = a.getResourceId(R.styleable.SherlockActionMode_titleTextStyle, 0);
		mSubtitleStyleRes = a.getResourceId(R.styleable.SherlockActionMode_subtitleTextStyle, 0);
		mContentHeight = a.getLayoutDimension(R.styleable.SherlockActionMode_height, 0);
		a.recycle();
	}
	
	private void finishAnimation() {
		if (mCurrentAnimation != null) {
			//TODO stop animation
			mCurrentAnimation = null;
		}
	}
	
	private void initTitle() {
		if (mTitleLayout == null) {
			LayoutInflater.from(getContext()).inflate(0/*TODO*/, this);
			
			mTitleLayout = (LinearLayout)getChildAt(getChildCount() - 1);
			mTitleView = (TextView)mTitleLayout.findViewById(0/*TODO*/);
			mSubtitleView = (TextView)mTitleLayout.findViewById(0/*TODO*/);
			
			if (mTitle != null) {
				mTitleView.setText(mTitle);
				if (mTitleStyleRes != 0) {
					mTitleView.setTextAppearance(getContext(), mTitleStyleRes);
				}
			}
			if (mSubtitle != null) {
				mSubtitleView.setText(mSubtitle);
				if (mSubtitleStyleRes != 0) {
					mSubtitleView.setTextAppearance(getContext(), mSubtitleStyleRes);
				}
				mSubtitleView.setVisibility(View.VISIBLE);
			}
			return;
		}
		
		mTitleView.setText(mTitle);
		mSubtitleView.setText(mSubtitle);
		mSubtitleView.setVisibility((mSubtitle != null) ? View.VISIBLE : View.GONE);
		
		if (mTitleLayout.getParent() == null) {
			addView(mTitleLayout);
		}
	}
	
	private Object makeInAnimation() {
		//TODO
		return null;
	}
	
	private Object makeOutAnimation() {
		//TODO
		return null;
	}
	
	private int measureChildView(View view, int size, int heightMeasureSpec, int padding) {
		final int i = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);
		view.measure(i, heightMeasureSpec);
		return size - view.getMeasuredWidth() - padding;
	}
	
	private int positionChild(View view, int left, int top, int totalHeight) {
		final int width = view.getMeasuredWidth();
		final int height = view.getMeasuredHeight();
		final int paddingTop = (totalHeight - height) / 2;
		final int offsetTop = top + paddingTop;
		final int right = left + width;
		final int bottom = offsetTop + height;
		view.layout(left, offsetTop, right, bottom);
		return width;
	}

	private int positionChildInverse(View view, int right, int top, int totalHeight) {
		final int width = view.getMeasuredWidth();
		final int height = view.getMeasuredHeight();
		final int paddingTop = (totalHeight - height) / 2;
		final int offsetTop = top + paddingTop;
		final int left = right - width;
		final int bottom = offsetTop + height;
		view.layout(left, offsetTop, right, bottom);
		return width;
	}
	
	public void closeMode() {
		if (mAnimationMode != ANIMATE_OUT) {
			if (mClose == null) {
				killMode();
				return;
			}
			finishAnimation();
			mAnimationMode = ANIMATE_OUT;
			mCurrentAnimation = makeOutAnimation();
			//TODO mCurrentAnimation.start();
		}
	}
	
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	}
	
	public CharSequence getSubtitle() {
		return mSubtitle;
	}
	
	public CharSequence getTitle() {
		return mTitle;
	}
	
	public void initForMode(ActionMode mode) {
		if (mClose == null) {
			mClose = LayoutInflater.from(getContext()).inflate(0/*TODO*/, this, false);
			addView(mClose);
		}
		
		mClose.findViewById(0/*TODO*/).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mMenuView = (ActionMenuView)((MenuBuilder)mode.getMenu()).getMenuView(3/*TODO*/, this);
		mMenuView.setOverflowReserved(true);
		mMenuView.updateChildren(false);
		addView(mMenuView);
		
		mAnimateInOnLayout = true;
		if (mClose.getParent() == null) {
			addView(mClose);
		}
	}
	
	public void killMode() {
		finishAnimation();
		removeAllViews();
		mCustomView = null;
		mMenuView = null;
		mAnimateInOnLayout = false;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	// TODO Auto-generated method stub

	}

	@Override
	protected void onMeasure(int p1, int p2) {
		
	}
	
	public void setCustomView(View customView) {
		if (mCustomView != null) {
			removeView(mCustomView);
		}
		mCustomView = customView;
		
		if (mTitleLayout != null) {
			removeView(mTitleLayout);
			mTitleLayout = null;
		}
		
		if (customView != null) {
			addView(customView);
		}
		
		requestLayout();
	}
}
