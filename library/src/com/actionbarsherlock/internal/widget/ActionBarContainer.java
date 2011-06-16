package com.actionbarsherlock.internal.widget;

import com.actionbarsherlock.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ActionBarContainer extends FrameLayout {
	private boolean mIsTransitioning;
	
	public ActionBarContainer(Context context) {
		this(context, null);
	}
	public ActionBarContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SherlockActionBar);
		setBackgroundDrawable(a.getDrawable(R.styleable.SherlockActionBar_background));
		a.recycle();
	}
	
}
