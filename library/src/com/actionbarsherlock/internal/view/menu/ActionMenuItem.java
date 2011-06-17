package com.actionbarsherlock.internal.view.menu;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.SubMenu;
import android.view.View;

public class ActionMenuItem implements MenuItem {
	private static final int CHECKABLE = MenuItemImpl.CHECKABLE;
	private static final int CHECKED = MenuItemImpl.CHECKED;
	private static final int ENABLED = MenuItemImpl.ENABLED;
	private static final int EXCLUSIVE = MenuItemImpl.EXCLUSIVE;
	private static final int HIDDEN = MenuItemImpl.HIDDEN;
	private static final int NO_ICON = 0;
	
	private final int mCategoryOrder;
	private MenuItem.OnMenuItemClickListener mClickListener;
	private Context mContext;
	private int mFlags = ENABLED;
	private final int mGroup;
	private Drawable mIconDrawable;
	private int mIconResId = NO_ICON;
	private final int mId;
	private Intent mIntent;
	private final int mOrdering;
	private char mShortcutAlphabeticChar;
	private char mShortcutNumericChar;
	private CharSequence mTitle;
	private CharSequence mTitleCondensed;

	public ActionMenuItem(Context context, int group, int id, int categoryOrder, int ordering, CharSequence title) {
		mContext = context;
		mId = id;
		mGroup = group;
		mCategoryOrder = categoryOrder;
		mOrdering = ordering;
		mTitle = title;
	}

	@Override
	public View getActionView() {
		return null;
	}

	@Override
	public char getAlphabeticShortcut() {
		return mShortcutAlphabeticChar;
	}

	@Override
	public int getGroupId() {
		return mGroup;
	}

	@Override
	public Drawable getIcon() {
		return mIconDrawable;
	}

	@Override
	public Intent getIntent() {
		return mIntent;
	}

	@Override
	public int getItemId() {
		return mId;
	}

	@Override
	public ContextMenu.ContextMenuInfo getMenuInfo() {
		return null;
	}

	@Override
	public char getNumericShortcut() {
		return mShortcutNumericChar;
	}

	@Override
	public int getOrder() {
		return mOrdering;
	}

	@Override
	public SubMenu getSubMenu() {
		return null;
	}

	@Override
	public CharSequence getTitle() {
		return mTitle;
	}

	@Override
	public CharSequence getTitleCondensed() {
		return mTitleCondensed;
	}

	@Override
	public boolean hasSubMenu() {
		return false;
	}

	public boolean invoke() {
		if ((mClickListener != null) && mClickListener.onMenuItemClick(this)) {
			return true;
		} else if (mIntent != null) {
			mContext.startActivity(mIntent);
			return true;
		}
		return false;
	}

	@Override
	public boolean isCheckable() {
		return (mFlags & 0x1) != 0;
	}

	@Override
	public boolean isChecked() {
		return (mFlags & 0x2) != 0;
	}

	@Override
	public boolean isEnabled() {
		return (mFlags & 0x10) != 0;
	}

	@Override
	public boolean isVisible() {
		return (mFlags & 0x8) == 0;
	}

	@Override
	public MenuItem setActionView(int layoutResId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MenuItem setActionView(View view) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MenuItem setAlphabeticShortcut(char shortcut) {
		mShortcutAlphabeticChar = shortcut;
		return this;
	}

	@Override
	public MenuItem setCheckable(boolean checkable) {
		int i = this.mFlags & 0xFFFFFFFE;
		if (checkable)
			;
		for (int j = 1;; j = 0) {
			int k = i | j;
			this.mFlags = k;
			return this;
		}
	}

	@Override
	public MenuItem setChecked(boolean checked) {
		int i = this.mFlags & 0xFFFFFFFD;
		if (checked)
			;
		for (int j = 2;; j = 0) {
			int k = i | j;
			this.mFlags = k;
			return this;
		}
	}

	@Override
	public MenuItem setEnabled(boolean enabled) {
		int i = this.mFlags & 0xFFFFFFEF;
		if (enabled)
			;
		for (int j = 16;; j = 0) {
			int k = i | j;
			this.mFlags = k;
			return this;
		}
	}

	public ActionMenuItem setExclusiveCheckable(boolean exclusive) {
		int i = this.mFlags & 0xFFFFFFFB;
		if (exclusive)
			;
		for (int j = 4;; j = 0) {
			int k = i | j;
			this.mFlags = k;
			return this;
		}
	}

	@Override
	public MenuItem setIcon(int resId) {
		mIconResId = resId;
		mIconDrawable = mContext.getResources().getDrawable(resId);
		return this;
	}

	@Override
	public MenuItem setIcon(Drawable icon) {
		mIconDrawable = icon;
		mIconResId = 0;
		return this;
	}

	@Override
	public MenuItem setIntent(Intent intent) {
		mIntent = intent;
		return this;
	}

	@Override
	public MenuItem setNumericShortcut(char shortcut) {
		mShortcutNumericChar = shortcut;
		return this;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener listener) {
		mClickListener = listener;
		return this;
	}

	@Override
	public android.view.MenuItem setOnMenuItemClickListener(android.view.MenuItem.OnMenuItemClickListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setShortcut(char numericShortcut, char alphabeticShortcut) {
		mShortcutNumericChar = numericShortcut;
		mShortcutAlphabeticChar = alphabeticShortcut;
		return this;
	}

	@Override
	public void setShowAsAction(int layoutResId) {}

	@Override
	public MenuItem setTitle(int resId) {
		this.mTitle = mContext.getResources().getString(resId);
		return this;
	}

	@Override
	public MenuItem setTitle(CharSequence title) {
		mTitle = title;
		return this;
	}

	@Override
	public MenuItem setTitleCondensed(CharSequence title) {
		mTitleCondensed = title;
		return this;
	}

	@Override
	public MenuItem setVisible(boolean visible) {
		int i = this.mFlags & 0x8;
		if (visible)
			;
		for (int j = 0;; j = 8) {
			int k = i | j;
			this.mFlags = k;
			return this;
		}
	}
}