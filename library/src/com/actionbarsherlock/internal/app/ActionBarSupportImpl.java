/*
 * Copyright (C) 2011 Jake Wharton <jakewharton@gmail.com>
 * Copyright (C) 2010 Johan Nilsson <http://markupartist.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.actionbarsherlock.internal.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ActionMode;
import android.support.v4.view.MenuInflater;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import com.actionbarsherlock.R;
import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import com.actionbarsherlock.internal.widget.ActionBarContainer;
import com.actionbarsherlock.internal.widget.ActionBarContextView;
import com.actionbarsherlock.internal.widget.ActionBarWatson;

public final class ActionBarSupportImpl extends ActionBar {
	/**
	 * Simple static method abstraction to get an instance of our implementing class.
	 * 
	 * @return {@link ActionBar}
	 */
	public static ActionBar createFor(FragmentActivity activity) {
		return new ActionBarSupportImpl(activity);
	}
	
	private static final int CONTEXT_DISPLAY_NORMAL = 0;
	private static final int CONTEXT_DISPLAY_SPLIT = 1;
	private static final DecelerateInterpolator sFadeOutInterpolator = new DecelerateInterpolator();
	
	private ActionMode mActionMode;
	private ActionBarWatson mActionView;
	private ActionBarContainer mContainerView;
	private FrameLayout mContentView;
	private Context mContext;
	private int mContextDisplayMode;
	private Animation mCurrentAnim;
	private LinearLayout mLowerContextView;
	private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners;
	private int mSavedTabPosition;
	private TabImpl mSelectedTab;
	private boolean mShowHideAnimationEnabled;
	private ArrayList<TabImpl> mTabs;
	private ActionBarContextView mUpperContextView;
	private boolean mIsActionItemTextEnabled;
	
	
	private ActionBarSupportImpl(FragmentActivity activity) {
		super(activity);
		
		mTabs = new ArrayList<TabImpl>();
		mSavedTabPosition = -1;
		mMenuVisibilityListeners = new ArrayList<OnMenuVisibilityListener>();
		//TODO
		
		mContentView = (FrameLayout)activity.findViewById(R.id.actionbarsherlock_content);
	}
	
	
	
	private long animateTo(int position) {
		show();
		//TODO
		return 0;
	}
	
	private void cleanupTabs() {
		if (mSelectedTab != null) {
			selectTab(null);
		}
		mTabs.clear();
		mActionView.removeAllTabs();
		mSavedTabPosition = -1;
	}
	
	private void configureTab(ActionBar.Tab tab, int position) {
		TabImpl tabImpl = (TabImpl)tab;
		if (tabImpl.getCallback() == null) {
			throw new IllegalStateException("Action Bar Tab must have a Callback");
		}
		tabImpl.setPosition(position);
		mTabs.add(position, tabImpl);
		final int count = mTabs.size();
		for (int i = 0; i < count; i++) {
			mTabs.get(i).setPosition(i);
		}
	}
	
	private void hideAllExcept(int position) {
		final int count = mContainerView.getChildCount();
		for (int i = 0; i < count; i++) {
			mContainerView.getChildAt(i).setVisibility((i == position) ? View.VISIBLE : View.GONE);
		}
	}
	
	private void init(View view) {
		mContext = view.getContext();
		mActionView = (ActionBarWatson)view.findViewById(0/*TODO*/);
		mUpperContextView = (ActionBarContextView)view.findViewById(0/*TODO*/);
		mLowerContextView = (LinearLayout)view.findViewById(0/*TODO*/);
		mContainerView = (ActionBarContainer)view.findViewById(0/*TODO*/);
		if ((mActionView == null) || (mUpperContextView == null) || (mContainerView == null)) {
			throw new IllegalStateException(/*TODO*/);
		}
		
		mActionView.setContextView(mUpperContextView);
		mContextDisplayMode = (mLowerContextView == null) ? CONTEXT_DISPLAY_NORMAL : CONTEXT_DISPLAY_SPLIT;
	}
	
	@Override
	public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
		mMenuVisibilityListeners.add(listener);
	}

	@Override
	public void addTab(Tab tab) {
		addTab(tab, mTabs.isEmpty());
	}

	@Override
	public void addTab(Tab tab, int position) {
		addTab(tab, position, mTabs.isEmpty());
	}

	@Override
	public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
		mActionView.addTab(tab, position, setSelected);
		configureTab(tab, position);
		if (setSelected) {
			selectTab(tab);
		}
	}

	@Override
	public void addTab(Tab tab, boolean setSelected) {
		mActionView.addTab(tab, setSelected);
		configureTab(tab, mTabs.size());
		if (setSelected) {
			selectTab(tab);
		}
	}
	
	public void dispatchMenuVisibilityChanged(boolean isVisible) {
		//Marshal to all listeners
		for (OnMenuVisibilityListener listener : mMenuVisibilityListeners) {
			listener.onMenuVisibilityChanged(isVisible);
		}
	}
	
	@Override
	public View getCustomView() {
		return mActionView.getCustomNavigationView();
	}
	
	@Override
	public int getDisplayOptions() {
		return mActionView.getDisplayOptions();
	}

	@Override
	public int getHeight() {
		return mActionView.getHeight();
	}

	@Override
	public int getNavigationItemCount() {
		switch (mActionView.getNavigationMode()) {
			default:
			case ActionBar.NAVIGATION_MODE_STANDARD:
				return 0;
			
			case ActionBar.NAVIGATION_MODE_LIST:
				SpinnerAdapter dropdownAdapter = mActionView.getDropdownAdapter();
				return (dropdownAdapter != null) ? dropdownAdapter.getCount() : 0;
			
			case ActionBar.NAVIGATION_MODE_TABS:
				return mTabs.size();
		}
	}

	@Override
	public int getNavigationMode() {
		return mActionView.getNavigationMode();
	}

	@Override
	public int getSelectedNavigationIndex() {
		switch (mActionView.getNavigationMode()) {
			default:
			case ActionBar.NAVIGATION_MODE_STANDARD:
				return -1;

			case ActionBar.NAVIGATION_MODE_LIST:
				return mActionView.getDropdownSelectedPosition();
				
			case ActionBar.NAVIGATION_MODE_TABS:
				return mSelectedTab.getPosition();
		}
	}

	@Override
	public ActionBar.Tab getSelectedTab() {
		return mSelectedTab;
	}

	@Override
	public CharSequence getSubtitle() {
		return mActionView.getSubtitle();
	}

	@Override
	public ActionBar.Tab getTabAt(int index) {
		return mTabs.get(index);
	}

	@Override
	public int getTabCount() {
		return mTabs.size();
	}

	@Override
	public CharSequence getTitle() {
		return mActionView.getTitle();
	}

	@Override
	public void hide() {
		//TODO
	}

	@Override
	public boolean isShowing() {
		return mContainerView.getVisibility() == View.VISIBLE;
	}
	
	@Override
	public ActionBar.Tab newTab() {
		return new TabImpl();
	}

	@Override
	public void removeAllTabs() {
		cleanupTabs();
	}

	@Override
	public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
		mMenuVisibilityListeners.remove(listener);
	}

	@Override
	public void removeTab(ActionBar.Tab tab) {
		removeTabAt(tab.getPosition());
	}

	@Override
	public void removeTabAt(int position) {
		//Get the selected tab position before any removal and reodering occurs
		int selectedPosition = (mSelectedTab != null) ? mSelectedTab.getPosition() : mSavedTabPosition;
		
		//Remove the tab
		mActionView.removeTabAt(position);
		mTabs.remove(position);
		
		//Update the positions of all tabs after the removed one
		final int count = mTabs.size();
		for (int i = position; i < count; i++) {
			mTabs.get(i).setPosition(i);
		}
		
		if ((selectedPosition == position) && !mTabs.isEmpty()) {
			//Select the previous (or first) tab
			selectTab(mTabs.get(Math.max(0, position - 1)));
		}
	}

	@Override
	public void selectTab(ActionBar.Tab tab) {
		//TODO
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		mContainerView.setBackgroundDrawable(d);
	}

	@Override
	public void setCustomView(int resId) {
		View view = LayoutInflater.from(mContext).inflate(resId, mActionView, false);
		setCustomView(view);
	}

	@Override
	public void setCustomView(View view) {
		mActionView.setCustomNavigationView(view);
	}
	
	@Override
	public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
		view.setLayoutParams(layoutParams);
		mActionView.setCustomNavigationView(view);
	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		setDisplayOptions(showHomeAsUp ? ActionBar.DISPLAY_HOME_AS_UP : 0, ActionBar.DISPLAY_HOME_AS_UP);
	}

	@Override
	public void setDisplayOptions(int options) {
		mActionView.setDisplayOptions(options);
	}

	@Override
	public void setDisplayOptions(int newOptions, int mask) {
		mActionView.setDisplayOptions((mActionView.getDisplayOptions() & ~mask) | newOptions);
	}

	@Override
	public void setDisplayShowCustomEnabled(boolean showCustom) {
		setDisplayOptions(showCustom ? ActionBar.DISPLAY_SHOW_CUSTOM : 0, ActionBar.DISPLAY_SHOW_CUSTOM);
	}

	@Override
	public void setDisplayShowHomeEnabled(boolean showHome) {
		setDisplayOptions(showHome ? ActionBar.DISPLAY_SHOW_HOME : 0, ActionBar.DISPLAY_SHOW_HOME);
	}

	@Override
	public void setDisplayShowTitleEnabled(boolean showTitle) {
		setDisplayOptions(showTitle ? ActionBar.DISPLAY_SHOW_TITLE : 0, ActionBar.DISPLAY_SHOW_TITLE);
	}

	@Override
	public void setDisplayUseLogoEnabled(boolean useLogo) {
		setDisplayOptions(useLogo ? ActionBar.DISPLAY_USE_LOGO : 0, ActionBar.DISPLAY_USE_LOGO);
	}

	@Override
	public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
		mActionView.setDropdownAdapter(adapter);
		mActionView.setCallback(callback);
	}

	@Override
	public void setNavigationMode(int mode) {
		int currentMode = mActionView.getNavigationMode();
		if (mode != currentMode) {
			if (currentMode == ActionBar.NAVIGATION_MODE_TABS) {
				mSavedTabPosition = getSelectedNavigationIndex();
				selectTab(null);
			}
			
			mActionView.setNavigationMode(mode);
			
			if (mode == ActionBar.NAVIGATION_MODE_TABS) {
				setSelectedNavigationItem(mSavedTabPosition);
				mSavedTabPosition = -1;
			}
		}
	}

	@Override
	public void setSelectedNavigationItem(int position) {
		switch (mActionView.getNavigationMode()) {
			default:
			case ActionBar.NAVIGATION_MODE_STANDARD:
				throw new IllegalStateException(/*TODO*/);
				
			case ActionBar.NAVIGATION_MODE_TABS:
				selectTab(mTabs.get(position));
				break;
				
			case ActionBar.NAVIGATION_MODE_LIST:
				mActionView.setDropdownSelectedPosition(position);
				break;
		}
	}
	
	public void setShowHideAnimationEnabled(boolean enabled) {
		mShowHideAnimationEnabled = enabled;
		if (!enabled && (mCurrentAnim != null)) {
			mCurrentAnim.cancel();
		}
	}

	@Override
	public void setSubtitle(int resId) {
		setSubtitle(mContext.getString(resId));
	}

	@Override
	public void setSubtitle(CharSequence subtitle) {
		mActionView.setSubtitle(subtitle);
	}
	@Override
	public void setTitle(int resId) {
		setTitle(mContext.getString(resId));
	}

	@Override
	public void setTitle(CharSequence title) {
		mActionView.setTitle(title);
	}

	@Override
	public void show() {
		//TODO
	}

	@Override
	protected ActionMode startActionMode(ActionMode.Callback callback) {
		if (mActionMode != null) {
			mActionMode.finish();
		}
		mUpperContextView.killMode();
		
		ActionModeImpl mode = new ActionModeImpl(callback);
		if (callback.onCreateActionMode(mode, mode.getMenu())) {
			mode.invalidate();
			mUpperContextView.initForMode(mode);
			animateTo(1);
			if (mLowerContextView != null) {
				mLowerContextView.setVisibility(View.VISIBLE);
			}
			mActionMode = mode;
			show();
			return mode;
		}
		return null;
	}
	
	
	
	public class TabImpl extends ActionBar.Tab {
		private ActionBar.TabListener mCallback;
		private View mCustomView;
		private Drawable mIcon;
		private int mPosition;
		private Object mTag;
		private CharSequence mText;
		
		public TabImpl() {}
		
		public ActionBar.TabListener getCallback() {
			return mCallback;
		}
		
		public View getCustomView() {
			return mCustomView;
		}
		
		public Drawable getIcon() {
			return mIcon;
		}
		
		public int getPosition() {
			return mPosition;
		}
		
		public Object getTag() {
			return mTag;
		}
		
		public CharSequence getText() {
			return mText;
		}
		
		public void select() {
			ActionBarSupportImpl.this.selectTab(this);
		}
		
		public ActionBar.Tab setCustomView(int layoutResourceId) {
			View view = LayoutInflater.from(ActionBarSupportImpl.this.mContext).inflate(layoutResourceId, null);
			return setCustomView(view);
		}
		
		public ActionBar.Tab setCustomView(View view) {
			mCustomView = view;
			return this;
		}
		
		public ActionBar.Tab setIcon(int resId) {
			Drawable icon = ActionBarSupportImpl.this.mContext.getResources().getDrawable(resId);
			return setIcon(icon);
		}
		
		public ActionBar.Tab setIcon(Drawable icon) {
			mIcon = icon;
			return this;
		}
		
		public void setPosition(int position) {
			mPosition = position;
		}
		
		public ActionBar.Tab setTabListener(ActionBar.TabListener callback) {
			mCallback = callback;
			return this;
		}
		
		public ActionBar.Tab setTag(Object tag) {
			mTag = tag;
			return this;
		}
		
		public ActionBar.Tab setText(int resId) {
			CharSequence text = ActionBarSupportImpl.this.mContext.getText(resId);
			return setText(text);
		}
	
		public ActionBar.Tab setText(CharSequence text) {
			mText = text;
			return this;
		}
	}
	
	
	public class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
		private ActionMode.Callback mCallback;
		private WeakReference<View> mCustomView;
		private MenuBuilder mMenu;
		
		public ActionModeImpl(ActionMode.Callback callback) {
			mCallback = callback;
			mMenu = new MenuBuilder(getActivity());
			mMenu.setDefaultShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			mMenu.setCallback(this);
		}
		
		@Override
		public void finish() {
			if (ActionBarSupportImpl.this.mActionMode != this) {
				mCallback.onDestroyActionMode(this);
				mCallback = null;
				ActionBarSupportImpl.this.animateTo(0);
				ActionBarSupportImpl.this.mUpperContextView.closeMode();
			}
		}

		@Override
		public View getCustomView() {
			return (mCustomView != null) ? mCustomView.get() : null;
		}

		@Override
		public android.view.Menu getMenu() {
			return mMenu;
		}

		@Override
		public MenuInflater getMenuInflater() {
			return new MenuInflater(getActivity());
		}

		@Override
		public CharSequence getSubtitle() {
			return ActionBarSupportImpl.this.mUpperContextView.getSubtitle();
		}

		@Override
		public CharSequence getTitle() {
			return ActionBarSupportImpl.this.mUpperContextView.getTitle();
		}

		@Override
		public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
			return (mCallback != null) ? mCallback.onActionItemClicked(this, item) : false;
		}

		@Override
		public void invalidate() {
			mCallback.onPrepareActionMode(this, mMenu);
		}

		@Override
		public void setCustomView(View view) {
			ActionBarSupportImpl.this.mUpperContextView.setCustomView(view);
			mCustomView = new WeakReference<View>(view);
		}

		@Override
		public void setSubtitle(int resId) {
			setSubtitle(getActivity().getResources().getString(resId));
		}

		@Override
		public void setSubtitle(CharSequence subtitle) {
			ActionBarSupportImpl.this.mUpperContextView.setSubtitle(subtitle);
		}

		@Override
		public void setTitle(int resId) {
			setTitle(getActivity().getResources().getString(resId));
		}

		@Override
		public void setTitle(CharSequence title) {
			ActionBarSupportImpl.this.mUpperContextView.setTitle(title);
		}
	}
	
	
	
	
	

	/* TODO: THIS WILL GO SOMEWHERE ELSE!
	@Override
	protected void onMenuInflated(Menu menu) {
		int maxItems = MAX_ACTION_BAR_ITEMS_PORTRAIT;
		if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			maxItems = MAX_ACTION_BAR_ITEMS_LANDSCAPE;
		}
		
		//Iterate and grab as many actions as we can up to maxItems honoring
		//their showAsAction values
		int ifItems = 0;
		final int count = menu.size();
		List<MenuItemImpl> keep = new ArrayList<MenuItemImpl>();
		for (int i = 0; i < count; i++) {
			MenuItemImpl item = (MenuItemImpl)menu.getItem(i);
			
			//Items without an icon or items without a title when the title
			//is enabled are forced into the normal options menu
			if (!mIsActionItemTextEnabled && (item.getIcon() == null)) {
				continue;
			} else if (mIsActionItemTextEnabled && ((item.getTitle() == null) || item.getTitle().equals(""))) {
				continue;
			}
			
			if ((item.getShowAsAction() & MenuItem.SHOW_AS_ACTION_ALWAYS) != 0) {
				//Show always therefore add to keep list
				keep.add(item);
				
				if ((keep.size() > maxItems) && (ifItems > 0)) {
					//If we have exceeded the max and there are "ifRoom" items
					//then iterate backwards to remove one and add it to the
					//head of the classic items list.
					for (int j = keep.size() - 1; j >= 0; j--) {
						if ((keep.get(j).getShowAsAction() & MenuItem.SHOW_AS_ACTION_IF_ROOM) != 0) {
							keep.remove(j);
							ifItems -= 1;
							break;
						}
					}
				}
			} else if (((item.getShowAsAction() & MenuItem.SHOW_AS_ACTION_IF_ROOM) != 0)
					&& (keep.size() < maxItems)) {
				//"ifRoom" items are added if we have not exceeded the max.
				keep.add(item);
				ifItems += 1;
			}
		}
		
		//Mark items that will be shown on the action bar as such so they do
		//not show up on the activity options menu
		mActionBar.removeAllItems();
		mActionBar.setIsActionItemTextEnabled(mIsActionItemTextEnabled);
		for (MenuItemImpl item : keep) {
			item.setIsShownOnActionBar(true);
			
			//Get a new item for this menu item
			ActionBarWatson.Item watsonItem = mActionBar.newItem();
			
			//Create and initialize a watson itemview wrapper
			WatsonItemViewWrapper watsonWrapper = new WatsonItemViewWrapper(watsonItem);
			watsonWrapper.initialize(item, MenuBuilder.TYPE_WATSON);
			
			//Associate the itemview with the item so changes will be reflected
			item.setItemView(MenuBuilder.TYPE_WATSON, watsonWrapper);
			
			//Add to the action bar for display
			mActionBar.addItem(watsonItem);
		}
	}
	*/

	public void forceActionItemText() {
		mIsActionItemTextEnabled = true;
	}
}
