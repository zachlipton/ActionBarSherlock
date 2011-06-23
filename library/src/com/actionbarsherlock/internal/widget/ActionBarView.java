package com.actionbarsherlock.internal.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus.NmeaListener;
import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.actionbarsherlock.R;
import com.actionbarsherlock.internal.view.menu.ActionMenuItem;
import com.actionbarsherlock.internal.view.menu.ActionMenuView;
import com.actionbarsherlock.internal.view.menu.MenuBuilder;

public final class ActionBarView extends ViewGroup {
	private static final int DEFAULT_DISPLAY_OPTIONS = ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE;
	private static final int ALL_DISPLAY_OPTIONS = ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM
			| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO;
	
	private ActionBar.OnNavigationListener mCallback;
	private final int mContentHeight;
	private ActionBarContextView mContextView;
	private View mCustomNavView;
	private int mDisplayOptions;
	private Drawable mDivider;
	private View mHomeAsUpView;
	private View mHomeLayout;
	private Drawable mIcon;
	private ImageView mIconView;
	private int mIndeterminateProgressStyle;
	private ProgressBar mIndeterminateProgressView;
	private int mItemPadding;
	private LinearLayout mListNavLayout;
	private Drawable mLogo;
	private ActionMenuItem mLogoNavItem;
	private ActionMenuView mMenuView;
	private final AdapterView.OnItemSelectedListener mNavItemSelectedListener;
	private int mNavigationMode;
	private MenuBuilder mOptionsMenu;
	private int mProgressBarPadding;
	private int mProgressStyle;
	private ProgressBar mProgressView;
	private boolean mShowMenu;
	private Spinner mSpinner;
	private SpinnerAdapter mSpinnerAdapter;
	private CharSequence mSubtitle;
	private int mSubtitleStyleRes;
	private TextView mSubtitleView;
	private View.OnClickListener mTabClickListener;
	private LinearLayout mTabLayout;
	private HorizontalScrollView mTabScrollView;
	private CharSequence mTitle;
	private LinearLayout mTitleLayout;
	private int mTitleStyleRes;
	private TextView mTitleView;
	private boolean mUserTitle;
	
	

	public ActionBarView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDisplayOptions = DEFAULT_DISPLAY_OPTIONS;
		
		mTabClickListener = null;
		mNavItemSelectedListener = new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mCallback != null) {
					mCallback.onNavigationItemSelected(arg2, arg3);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//No op
			}
		};
		
		setBackgroundResource(0);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SherlockActionBar);
		ApplicationInfo appInfo = context.getApplicationInfo();
		PackageManager pm = context.getPackageManager();
		
		mNavigationMode = a.getInt(R.styleable.SherlockActionBar_navigationMode, ActionBar.NAVIGATION_MODE_STANDARD);
		
		mTitle = a.getString(R.styleable.SherlockActionBar_title);
		mTitleStyleRes = a.getResourceId(R.styleable.SherlockActionBar_titleTextStyle, 0);
		
		mSubtitle = a.getString(R.styleable.SherlockActionBar_subtitle);
		mSubtitleStyleRes = a.getResourceId(R.styleable.SherlockActionBar_subtitleTextStyle, 0);
		
		mLogo = a.getDrawable(R.styleable.SherlockActionBar_logo);
		if ((mLogo == null) && (context instanceof Activity)) {
			//TODO load from manifest
		}
		
		mIcon = a.getDrawable(R.styleable.SherlockActionBar_icon);
		if ((mIcon == null) && (context instanceof Activity)) {
			//TODO load from manifest
		}
		
		int homeLayoutResId = a.getResourceId(R.styleable.SherlockActionBar_homeLayout, R.layout.action_bar_home);
		mHomeLayout = LayoutInflater.from(context).inflate(homeLayoutResId, this, false);
		mHomeAsUpView = mHomeLayout.findViewById(R.id.up);
		mIconView = (ImageView)mHomeLayout.findViewById(R.id.home);
		
		mProgressStyle = a.getResourceId(R.styleable.SherlockActionBar_progressBarStyle, 0);
		mIndeterminateProgressStyle = a.getResourceId(R.styleable.SherlockActionBar_indeterminateProgressStyle, 0);
		mProgressBarPadding = a.getDimensionPixelOffset(R.styleable.SherlockActionBar_progressBarPadding, 0);
		
		mItemPadding = a.getDimensionPixelOffset(R.styleable.SherlockActionBar_itemPadding, 0);
		
		setDisplayOptions(a.getInt(R.styleable.SherlockActionBar_displayOptions, 0));
		
		int customLayoutResId = a.getResourceId(R.styleable.SherlockActionBar_customNavigationLayout, 0);
		if (customLayoutResId != 0) {
			mCustomNavView = LayoutInflater.from(context).inflate(customLayoutResId, this, false);
			mNavigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
			setDisplayOptions(mDisplayOptions | ActionBar.DISPLAY_SHOW_CUSTOM);
		}
		
		mContentHeight = a.getLayoutDimension(R.styleable.SherlockActionBar_height, 0);
		
		mDivider = a.getDrawable(R.styleable.SherlockActionBar_divider);
		
		a.recycle();
		
		mLogoNavItem = new ActionMenuItem(context, 0, android.R.id.home, 0, 0, mTitle);
		mHomeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (context instanceof Activity) {
					((Activity)context).onMenuItemSelected(0, mLogoNavItem);
				}
			}
		});
		mHomeLayout.setClickable(true);
		mHomeLayout.setFocusable(true);
	}
	
	
	private TabView createTabView(ActionBar.Tab tab) {
		TabView tabView = new TabView(tab);
		tabView.setFocusable(true);
		
		if (mTabClickListener == null) {
			mTabClickListener = new TabClickListener();
		}
		tabView.setOnClickListener(mTabClickListener);
		
		return tabView;
	}
	
	private void ensureTabsExist() {
		if (mTabScrollView == null) {
			mTabScrollView = new HorizontalScrollView(getContext());
			mTabScrollView.setHorizontalFadingEdgeEnabled(true);
			
			mTabLayout = new LinearLayout(getContext(), null, R.styleable.SherlockTheme_actionBarTabBarStyle);
			mTabScrollView.addView(mTabLayout);
		}
	}
	
	private void initTitle() {
		mTitleLayout = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.action_bar_title_item, null);
		mTitleView = (TextView)mTitleLayout.findViewById(R.id.action_bar_title);
		mSubtitleView = (TextView)mTitleLayout.findViewById(R.id.action_bar_subtitle);
		
		if (mTitleStyleRes != 0) {
			mTitleView.setTextAppearance(getContext(), mTitleStyleRes);
		}
		if (mTitle != null) {
			mTitleView.setText(mTitle);
		}
		if (mSubtitleStyleRes != 0) {
			mSubtitleView.setTextAppearance(getContext(), mSubtitleStyleRes);
		}
		if (mSubtitle != null) {
			mSubtitleView.setText(mSubtitle);
		}
		addView(mTitleLayout);
	}
	
	private int measureChildView(View view, int p1, int p2, int p3) {
		int i = View.MeasureSpec.makeMeasureSpec(p1, View.MeasureSpec.AT_MOST);
		view.measure(i, p2);
		return p1 - view.getMeasuredWidth() - p3;
		//TODO optimize for readability
	}

	private int positionChild(View paramView, int paramInt1, int paramInt2, int paramInt3) {
		int measuredWidth = paramView.getMeasuredWidth();
		int measuredHeight = paramView.getMeasuredHeight();
		int k = (paramInt3 - measuredHeight) / 2;
		int m = paramInt2 + k;
		int n = paramInt1 + measuredWidth;
		int i1 = m + measuredHeight;
		paramView.layout(paramInt1, m, n, i1);
		return measuredWidth;
		//TODO optimize for readability
	}

	private int positionChildInverse(View paramView, int paramInt1, int paramInt2, int paramInt3) {
		int measuredWidth = paramView.getMeasuredWidth();
		int meausredHeight = paramView.getMeasuredHeight();
		int k = (paramInt3 - meausredHeight) / 2;
		int m = paramInt2 + k;
		int n = paramInt1 - measuredWidth;
		int i1 = m + meausredHeight;
		paramView.layout(n, m, paramInt1, i1);
		return measuredWidth;
		//TODO optimize for readability
	}
	
	private void setTitleImpl(CharSequence title) {
		mTitle = title;
		
		if (mTitleView != null) {
			mTitleView.setText(title);
		}
		
		boolean hasTitle = !TextUtils.isEmpty(mTitle) || !TextUtils.isEmpty(mSubtitle);
		mTitleLayout.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
		
		if (mLogoNavItem != null) {
			mLogoNavItem.setTitle(title);
		}
	}
	
	public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
		ensureTabsExist();
		TabView tabView = createTabView(tab);
		mTabLayout.addView(tabView, position);
		if (setSelected) {
			tabView.setSelected(true);
		}
	}
	
	public void addTab(ActionBar.Tab tab, boolean setSelected) {
		ensureTabsExist();
		TabView tabView = createTabView(tab);
		mTabLayout.addView(tabView);
		if (setSelected) {
			tabView.setSelected(true);
		}
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new ActionBar.LayoutParams(19);
	}

	public View getCustomNavigationView() {
		return this.mCustomNavView;
	}

	public int getDisplayOptions() {
		return this.mDisplayOptions;
	}

	public SpinnerAdapter getDropdownAdapter() {
		return this.mSpinnerAdapter;
	}

	public int getDropdownSelectedPosition() {
		return this.mSpinner.getSelectedItemPosition();
	}

	public int getNavigationMode() {
		return this.mNavigationMode;
	}

	public CharSequence getSubtitle() {
		return this.mSubtitle;
	}

	public CharSequence getTitle() {
		return this.mTitle;
	}

	public void initIndeterminateProgress() {
		mIndeterminateProgressView = new ProgressBar(getContext(), null, mIndeterminateProgressStyle);
		mIndeterminateProgressView.setId(R.id.progress_circular);
		addView(mIndeterminateProgressView);
	}

	public void initProgress() {
		mProgressView = new ProgressBar(getContext(), null, mProgressStyle);
		mProgressView.setId(R.id.progress_horizontal);
		mProgressView.setMax(10000);
		addView(mProgressView);
	}

	public boolean isOverflowMenuOpen() {
		return (mMenuView != null) ? mMenuView.isOverflowMenuOpen() : false;
	}

	public boolean isOverflowMenuShowing() {
		return (mMenuView != null) ? mMenuView.isOverflowMenuShowing() : false;
	}

	public boolean isOverflowReserved() {
		return (mMenuView != null) ? mMenuView.isOverflowReserved() : false;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		addView(mHomeLayout);
		if ((mCustomNavView != null) && ((mDisplayOptions & DEFAULT_DISPLAY_OPTIONS) != 0)) {
			ViewParent localViewParent = mCustomNavView.getParent();
			if (localViewParent != this) {
				if ((localViewParent instanceof ViewGroup)) {
					((ViewGroup)localViewParent).removeView(mCustomNavView);
				}
				addView(mCustomNavView);
			}
		}
	}

	@Override
	protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {

	}
	
	@Override
	protected void onMeasure(int paramInt1, int paramInt2) {
		
	}

	public void openOverflowMenu() {
		if (mMenuView != null) {
			mMenuView.openOverflowMenu();
		}
	}
	
	public void postShowOverflowMenu() {
		post(new Runnable() {
			@Override
			public void run() {
				showOverflowMenu();
			}
		});
	}
	
	public void removeAllTabs() {
		if (mTabLayout != null) {
			mTabLayout.removeAllViews();
		}
	}
	
	public void removeTabAt(int index) {
		if (mTabLayout != null) {
			mTabLayout.removeViewAt(index);
		}
	}
	
	public void setCallback(ActionBar.OnNavigationListener callback) {
		mCallback = callback;
	}
	
	public void setContextView(ActionBarContextView contextView) {
		mContextView = contextView;
	}
	
	public void setCustomNavigationView(View customView) {
		if ((mDisplayOptions & DEFAULT_DISPLAY_OPTIONS) != 0) {
			if (mCustomNavView != null) {
				removeView(mCustomNavView);
			}
			mCustomNavView = customView;
			if (mCustomNavView != null) {
				addView(mCustomNavView);
			}
		}
	}
	
	public void setDisplayOptions(int newDisplayOptions) {
		final int oldDisplayOptions = mDisplayOptions;
		final int changed = oldDisplayOptions ^ newDisplayOptions;
		mDisplayOptions = newDisplayOptions;
		
		if ((changed & ALL_DISPLAY_OPTIONS) != 0) {
			//TODO
		}
	}
	
	public void setDropdownAdapter(SpinnerAdapter spinnerAdapter) {
		mSpinnerAdapter = spinnerAdapter;
		if (mSpinner != null) {
			mSpinner.setAdapter(mSpinnerAdapter);
		}
	}
	
	public void setDropdownSelectedPosition(int position) {
		mSpinner.setSelection(position);
	}
	
	public void setMenu(Menu menu) {
		if (mOptionsMenu != menu) {
			mOptionsMenu = (MenuBuilder)menu;
			
			if (mMenuView != null) {
				removeView(mMenuView);
			}
			
			mMenuView = (ActionMenuView)mOptionsMenu.getMenuView(MenuBuilder.TYPE_SHERLOCK, null);
			mMenuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			addView(mMenuView);
		}
	}
	
	public void setNavigationMode(int navigationMode) {
		if (mNavigationMode == navigationMode) {
			return;
		}
		
		switch (mNavigationMode) {
			
		}
		
		mNavigationMode = navigationMode;
		requestLayout();
	}
	
	public void setSubtitle(CharSequence subtitle) {
		mSubtitle = subtitle;
		if (mSubtitleView != null) {
			mSubtitleView.setText(mSubtitle);
			if (mSubtitle != null) {
				mSubtitleView.setVisibility(View.VISIBLE);
			}
			boolean hasTitle = !TextUtils.isEmpty(mTitle) || !TextUtils.isEmpty(mSubtitle);
			mTitleLayout.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
		}
	}
	
	public void setTabSelected(int index) {
		ensureTabsExist();
		
		final int count = mTabLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			mTabLayout.getChildAt(i).setSelected(i == index);
		}
	}
	
	public void setTitle(CharSequence title) {
		mUserTitle = true;
		setTitleImpl(title);
	}
	
	public void setWindowTitle(CharSequence title) {
		if (!mUserTitle) {
			setTitleImpl(title);
		}
	}
	
	public boolean showOverflowMenu() {
		return (mMenuView != null) ? mMenuView.showOverflowMenu() : false;
	}
	
	
	class HomeView extends LinearLayout {
		private View mIconView;
		private View mUpView;
		
		public HomeView(Context context) {
			super(context);
		}
		public HomeView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		@Override
		protected void onFinishInflate() {
			mUpView = findViewById(R.id.up);
			mIconView = findViewById(R.id.home);
		}
	}
	
	class TabClickListener implements View.OnClickListener {
		private TabClickListener() {}
		
		@Override
		public void onClick(View view) {
			((ActionBarView.TabView)view).getTab().select();
			final int count = mTabLayout.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = mTabLayout.getChildAt(i);
				child.setSelected(child == view);
			}
		}
	}
	
	class TabView extends LinearLayout {
		private ActionBar.Tab mTab;
		
		public TabView(ActionBar.Tab tab) {
			super(null, null, R.attr.actionBarTabStyle);
			mTab = tab;
			
			if (mTab.getCustomView() != null) {
				addView(mTab.getCustomView());
				setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1.0f));
			} else {
				if (mTab.getIcon() != null) {
					ImageView imageView = new ImageView(getContext());
					imageView.setImageDrawable(mTab.getIcon());
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
					layoutParams.gravity = Gravity.CENTER_VERTICAL;
					imageView.setLayoutParams(layoutParams);
					addView(imageView);
				}
				if (mTab.getText() != null) {
					TextView textView = new TextView(getContext(), null, R.attr.actionBarTabTextStyle);
					textView.setText(mTab.getText());
					textView.setSingleLine();
					textView.setEllipsize(TextUtils.TruncateAt.END);
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
					layoutParams.gravity = Gravity.CENTER_VERTICAL;
					textView.setLayoutParams(layoutParams);
					addView(textView);
				}
			}
		}
		
		public ActionBar.Tab getTab() {
			return mTab;
		}
	}
}
