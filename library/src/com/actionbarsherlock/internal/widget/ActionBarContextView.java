package com.actionbarsherlock.internal.widget;

import com.actionbarsherlock.R;
import com.actionbarsherlock.internal.view.animation.AnimationGroup;
import com.actionbarsherlock.internal.view.menu.ActionMenuView;
import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ActionMode;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActionBarContextView extends ViewGroup implements Animation.AnimationListener {
	private static final int ANIMATE_IDLE = 0;
	private static final int ANIMATE_IN = 1;
	private static final int ANIMATE_OUT = 2;
	
	private boolean mAnimateInOnLayout;
	private int mAnimationMode;
	private View mClose;
	private int mContentHeight;
	private Animation mCurrentAnimation;
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
		this(context, attrs, R.attr.actionModeStyle);
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
			mCurrentAnimation.cancel();
			mCurrentAnimation = null;
		}
	}
	
	private void initTitle() {
		if (mTitleLayout == null) {
			LayoutInflater.from(getContext()).inflate(R.layout.action_bar_title_item, this);
			
			mTitleLayout = (LinearLayout)getChildAt(getChildCount() - 1);
			mTitleView = (TextView)mTitleLayout.findViewById(R.id.action_bar_title);
			mSubtitleView = (TextView)mTitleLayout.findViewById(R.id.action_bar_subtitle);
			
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
	
	private Animation makeInAnimation() {
		final AnimationGroup group = new AnimationGroup();
		group.setAnimationListener(this);

		//mClose.setTranslationX(-mClose.getWidth());
		TranslateAnimation closeAnimation = new TranslateAnimation(-mClose.getWidth(), 0, 0, 0);
		closeAnimation.setDuration(200);
		closeAnimation.setInterpolator(new DecelerateInterpolator());
		mClose.setAnimation(closeAnimation);
		group.addAnimation(closeAnimation);
		
		if (mMenuView != null) {
			final int count = mMenuView.getChildCount();
			if (count > 0) {
				for (int i = count - 1, j = 0; i >= 0; i--, j++) {
					final View item = mMenuView.getChildAt(i);
					//item.setScaleY(0);
					ScaleAnimation scaleAnimation = new ScaleAnimation(0, 0, 0, 1.0f);
					scaleAnimation.setDuration(100);
					scaleAnimation.setStartOffset(j * 70);
					item.setAnimation(scaleAnimation);
					group.addAnimation(scaleAnimation);
				}
			}
		}
		
		return group;
	}
	
	private Animation makeOutAnimation() {
		final AnimationGroup group = new AnimationGroup();
		group.setAnimationListener(this);

		TranslateAnimation closeAnimation = new TranslateAnimation(0, -mClose.getWidth(), 0, 0);
		closeAnimation.setDuration(200);
		closeAnimation.setInterpolator(new DecelerateInterpolator());
		mClose.setAnimation(closeAnimation);
		group.addAnimation(closeAnimation);
		
		if (mMenuView != null) {
			final int count = mMenuView.getChildCount();
			for (int i = 0; i < count; i++) {
				ScaleAnimation scaleAnimation = new ScaleAnimation(0, 0, 1.0f, 0);
				scaleAnimation.setDuration(100);
				scaleAnimation.setStartOffset(i * 70);
				mMenuView.getChildAt(i).setAnimation(scaleAnimation);
				group.addAnimation(scaleAnimation);
			}
		}
		
		return group;
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
			mCurrentAnimation.start();
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
	
	public void initForMode(final ActionMode mode) {
		if (mClose == null) {
			mClose = LayoutInflater.from(getContext()).inflate(R.layout.action_mode_close_item, this, false);
			addView(mClose);
		}
		
		mClose.findViewById(R.id.action_mode_close_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mode.finish();
			}
		});
		
		mMenuView = (ActionMenuView)((MenuBuilder)mode.getMenu()).getMenuView(MenuBuilder.TYPE_SHERLOCK, this);
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
	public void onAnimationEnd(Animation animation) {
		if (mAnimationMode == ANIMATE_OUT) {
			killMode();
			mAnimationMode = ANIMATE_IDLE;
		}
	}
	
	@Override
	public void onAnimationRepeat(Animation animation) {
		//No op
	}
	
	@Override
	public void onAnimationStart(Animation animation) {
		//No op
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int childTop = getPaddingTop();
		final int childHeight = (bottom - top) - getPaddingTop() - getPaddingBottom();
		
		int childLeft = getPaddingLeft();
		
		if ((this.mClose != null) && (this.mClose.getVisibility() != 8)) {
			childLeft += positionChild(this.mClose, childLeft, childTop, childHeight);
			if (this.mAnimateInOnLayout) {
				this.mAnimationMode = ANIMATE_IN;
				this.mCurrentAnimation = makeInAnimation();
				this.mCurrentAnimation.start();
				this.mAnimateInOnLayout = false;
			}
		}
		if ((this.mTitleLayout != null) && (this.mCustomView == null)) {
			childLeft += positionChild(this.mTitleLayout, childLeft, childTop, childHeight);
		}
		if (this.mCustomView != null) {
			positionChild(this.mCustomView, childLeft, childTop, childHeight);
		}
		if (this.mMenuView != null) {
			final int childRight = (right - left) - getPaddingRight();
			positionChildInverse(this.mMenuView, childRight, childTop, childHeight);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY) {
			throw new IllegalStateException(/*TODO*/);
		}
		if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.UNSPECIFIED) {
			throw new IllegalStateException(/*TODO*/);
		}

		final int contentWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		final int heightPadding = getPaddingTop() + getPaddingBottom();
		if (mContentHeight > 0) {
			final int childWidth = contentWidth - getPaddingLeft() - getPaddingRight();
			final int childHeight = mContentHeight - heightPadding;
			final int childHeightSpec = View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.AT_MOST);
			
			if (mClose != null) {
				measureChildView(mClose, childWidth, childHeightSpec, 0);
			}
			if (mMenuView != null) {
				measureChildView(mMenuView, childWidth, childHeightSpec, 0);
			}
			if ((mTitleLayout != null) && (mCustomView == null)) {
				measureChildView(mTitleLayout, childWidth, childHeightSpec, 0);
			}
			if (mCustomView != null) {
				ViewGroup.LayoutParams customLayoutParams = this.mCustomView.getLayoutParams();
				int customWidthMode;
				int customWidth;
				int customHeightMode;
				int customHeight;
				
				if (customLayoutParams.width == 65534) {
					customWidthMode = View.MeasureSpec.AT_MOST;
				} else {
					customWidthMode = View.MeasureSpec.EXACTLY;
				}
				if (customLayoutParams.width < 0) {
					customWidth = childWidth;
				} else {
					customWidth = Math.min(customLayoutParams.width, childWidth);
				}
				if (customLayoutParams.height == 65534) {
					customHeightMode = View.MeasureSpec.AT_MOST;
				} else {
					customHeightMode = View.MeasureSpec.EXACTLY;
				}
				if (customLayoutParams.height < 0) {
					customHeight = Math.min(customLayoutParams.height, childHeight);
				} else {
					customHeight = heightPadding;
				}
				
				final int customWidthSpec = View.MeasureSpec.makeMeasureSpec(customWidth, customWidthMode);
				final int customHeightSpec = View.MeasureSpec.makeMeasureSpec(customHeight, customHeightMode);
				mCustomView.measure(customWidthSpec, customHeightSpec);
			}
		} else {
			int contentHeight = 0;
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				int i38 = getChildAt(i).getMeasuredHeight() + heightPadding;
				if (i38 > contentHeight) {
					contentHeight = i38;
				}
			}
			mContentHeight = View.MeasureSpec.getSize(heightMeasureSpec);
		}
		
		setMeasuredDimension(contentWidth, mContentHeight);
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
	
	public void setHeight(int height) {
		mContentHeight = height;
	}
	
	public void setSubtitle(CharSequence subtitle) {
		mSubtitle = subtitle;
		initTitle();
	}
	
	public void setTitle(CharSequence title) {
		mTitle = title;
		initTitle();
	}
}
