/*
 * Copyright (C) 2006 The Android Open Source Project
 *               2011 Jake Wharton
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

package com.actionbarsherlock.internal.view.menu;

import java.lang.ref.WeakReference;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * An implementation of the {@link android.view.MenuItem} interface for use in
 * inflating menu XML resources to be added to a third-party action bar.
 *
 * @author Jake Wharton <jakewharton@gmail.com>
 * @see <a href="http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/com/android/internal/view/menu/MenuItemImpl.java">com.android.internal.view.menu.MenuItemImpl</a>
 */
public final class MenuItemImpl implements MenuItem {
    private static final String TAG = "MenuItemImpl";

    private final MenuBuilder mMenu;

    private final int mItemId;
    private final int mGroupId;
    private final int mCategoryOrder;
    private final int mOrdering;

    private Intent mIntent;
    private CharSequence mTitle;
    private CharSequence mTitleCondensed;
    private char mNumericalShortcut;
    private char mAlphabeticalShortcut;
    private int mShowAsAction;
    private SubMenuBuilder mSubMenu;
    private Runnable mItemCallback;
    private OnMenuItemClickListener mClickListener;
    private Drawable mIcon;
    private int mIconRes = View.NO_ID;
    private View mActionView;
    private int mActionViewRes = View.NO_ID;

    int mFlags = ENABLED;
    static final int CHECKABLE = 0x01;
    static final int CHECKED   = 0x02;
    static final int EXCLUSIVE = 0x04;
    static final int HIDDEN    = 0x08;
    static final int ENABLED   = 0x10;
    static final int IS_ACTION = 0x20;

    private final WeakReference<MenuView.ItemView>[] mItemViews;

    private final DialogInterface.OnClickListener subMenuClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int index) {
            dialog.dismiss();
            mSubMenu.getItem(index).invoke();
        }
    };
    private final DialogInterface.OnMultiChoiceClickListener subMenuMultiClick = new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int index, boolean isChecked) {
            dialog.dismiss();
            mSubMenu.getItem(index).setChecked(isChecked);
        }
    };


    /**
     * Create a new action bar menu item.
     *
     * @param context Context used if resource resolution is required.
     * @param itemId A unique ID. Used in the activity callback.
     * @param groupId Group ID. Currently unused.
     * @param order Item order. Currently unused.
     * @param title Title of the item.
     */
    @SuppressWarnings("unchecked")
    public MenuItemImpl(MenuBuilder menu, int groupId, int itemId, int order, int ordering, CharSequence title, int showAsAction) {
        mMenu = menu;

        mItemId = itemId;
        mGroupId = groupId;
        mCategoryOrder = order;
        mOrdering = ordering;
        mTitle = title;
        mShowAsAction = showAsAction;

        mItemViews = new WeakReference[MenuBuilder.NUM_TYPES];
    }



    public boolean invoke() {
        if (hasSubMenu()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mMenu.getContext());
            builder.setTitle(getTitle());

            final boolean isExclusive = mSubMenu.getItem(0).isExclusiveCheckable();
            final boolean isCheckable = mSubMenu.getItem(0).isCheckable();
            final CharSequence[] titles = getSubMenuTitles();
            if (isExclusive) {
                builder.setSingleChoiceItems(titles, getSubMenuSelected(), subMenuClick);
            } else if (isCheckable) {
                builder.setMultiChoiceItems(titles, getSubMenuChecked(), subMenuMultiClick);
            } else {
                builder.setItems(titles, subMenuClick);
            }

            builder.show();
            return true;
        }

        if (mClickListener != null &&
            mClickListener.onMenuItemClick(this)) {
            return true;
        }

        MenuBuilder.Callback callback = mMenu.getRootMenu().getCallback();
        if (callback != null &&
            callback.onMenuItemSelected(mMenu.getRootMenu(), this)) {
            return true;
        }

        if (mItemCallback != null) {
            mItemCallback.run();
            return true;
        }

        if (mIntent != null) {
            try {
                mMenu.getContext().startActivity(mIntent);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Can't find activity to handle intent; ignoring", e);
            }
        }

        return false;
    }

    private CharSequence[] getSubMenuTitles() {
        final int count = mSubMenu.size();
        CharSequence[] list = new CharSequence[count];
        for (int i = 0; i < count; i++) {
            list[i] = mSubMenu.getItem(i).getTitle();
        }
        return list;
    }

    private int getSubMenuSelected() {
        final int count = mSubMenu.size();
        for (int i = 0; i < count; i++) {
            if (mSubMenu.getItem(i).isChecked()) {
                return i;
            }
        }
        return -1;
    }

    private boolean[] getSubMenuChecked() {
        final int count = mSubMenu.size();
        boolean[] checked = new boolean[count];
        for (int i = 0; i < count; i++) {
            checked[i] = mSubMenu.getItem(i).isChecked();
        }
        return checked;
    }

    private MenuView.ItemView createItemView(int menuType, ViewGroup parent) {
        MenuView.ItemView view;
    	final int itemLayoutResId = MenuBuilder.ITEM_LAYOUT_RES_FOR_TYPE[menuType];
    	if (itemLayoutResId != 0) {
            view = (MenuView.ItemView)getLayoutInflater(menuType).inflate(itemLayoutResId, parent, false);
    	} else {
    		switch (menuType) {
    			case MenuBuilder.TYPE_NATIVE:
                    view = new NativeItemView();
    				break;
    			
    			default:
    				throw new IllegalStateException("Menu type has no defined layout and is also unknown.");
    		}
    	}
        view.initialize(this, menuType);
        return view;
    }
    
    /**
     * Returns a LayoutInflater that is themed for the given menu type.
     * 
     * @param menuType The type of menu.
     * @return A LayoutInflater.
     */
    public LayoutInflater getLayoutInflater(int menuType) {
        return mMenu.getMenuType(menuType).getInflater();
    }


    public MenuView.ItemView getItemView(int menuType, ViewGroup parent) {
        if (!hasItemView(menuType)) {
            MenuView.ItemView view = createItemView(menuType, parent);
            mItemViews[menuType] = new WeakReference<MenuView.ItemView>(view);
        }
        return mItemViews[menuType].get();
    }

    private boolean hasItemView(int menuType) {
        return mItemViews[menuType] != null && mItemViews[menuType].get() != null;
    }

    public void setItemView(int type, MenuView.ItemView itemView) {
        mItemViews[type] = new WeakReference<MenuView.ItemView>(itemView);
    }

    /**
     * Get whether or not this item is being shown on the action bar.
     *
     * @return {@code true} if shown, {@code false} otherwise.
     */
    public boolean isShownOnActionBar() {
        return (mFlags & IS_ACTION) == IS_ACTION;
    }

    /**
     * Denote whether or not this menu item is being shown on the action bar.
     *
     * @param isShownOnActionBar {@code true} if shown or {@code false}.
     */
    public void setIsShownOnActionBar(boolean isShownOnActionBar) {
        mFlags = (mFlags & ~IS_ACTION) | (isShownOnActionBar ? IS_ACTION : 0);
    }

    @Override
    public Intent getIntent() {
        return this.mIntent;
    }

    @Override
    public int getItemId() {
        return this.mItemId;
    }

    @Override
    public CharSequence getTitle() {
        return this.mTitle;
    }

    @Override
    public boolean isEnabled() {
        return (mFlags & ENABLED) != 0;
    }

    @Override
    public boolean isVisible() {
        return (mFlags & HIDDEN) == 0;
    }

    public boolean isActionButton() {
        return ((mFlags & IS_ACTION) == IS_ACTION) || requiresActionButton();
    }

    public boolean requestsActionButton() {
        return (mShowAsAction & MenuItem.SHOW_AS_ACTION_IF_ROOM) == MenuItem.SHOW_AS_ACTION_IF_ROOM;
    }

    public boolean requiresActionButton() {
        return (mShowAsAction & MenuItem.SHOW_AS_ACTION_ALWAYS) == MenuItem.SHOW_AS_ACTION_ALWAYS;
    }

    @Override
    public MenuItem setEnabled(boolean enabled) {
        final boolean oldValue = isEnabled();
        mFlags = (mFlags & ~ENABLED) | (enabled ? ENABLED : 0);

        if (oldValue != enabled) {
            for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
                if (hasItemView(i)) {
                    mItemViews[i].get().setEnabled(enabled);
                }
            }
        }

        return this;
    }

    @Override
    public MenuItem setIcon(int iconResourceId) {
        mIcon = null;
        mIconRes = iconResourceId;

        if (mIconRes != View.NO_ID) {
            setIconOnViews(mMenu.getContext().getResources().getDrawable(mIconRes));
        }

        return this;
    }

    @Override
    public MenuItem setIntent(Intent intent) {
        mIntent = intent;
        return this;
    }

    public void setIsActionButton(boolean isAction) {
        mFlags = (mFlags & ~IS_ACTION) | (isAction ? IS_ACTION : 0);
    }

    @Override
    public MenuItem setTitle(CharSequence title) {
        mTitle = title;
        return this;
    }

    @Override
    public MenuItem setTitle(int titleResourceId) {
        mTitle = mMenu.getContext().getResources().getString(titleResourceId);
        return this;
    }

    @Override
    public MenuItem setVisible(boolean visible) {
        if (setVisibleInt(visible)) {
            mMenu.onItemVisibleChanged(this);
        }
        return this;
    }

    boolean setVisibleInt(boolean visible) {
        final boolean oldValue = isVisible();
        mFlags = (mFlags & ~HIDDEN) | (visible ? 0 : HIDDEN);
        return oldValue != visible;
    }

    @Override
    public boolean isChecked() {
        return (mFlags & CHECKED) == CHECKED;
    }

    @Override
    public MenuItem setChecked(boolean checked) {
        if ((mFlags & EXCLUSIVE) == EXCLUSIVE) {
            // Call the method on the Menu since it knows about the others in this
            // exclusive checkable group
            mMenu.setExclusiveItemChecked(this);
        } else {
            setCheckedInt(checked);
        }

        return this;
    }

    void setCheckedInt(boolean checked) {
        final boolean oldValue = isChecked();
        mFlags = (mFlags & ~CHECKED) | (checked ? CHECKED : 0);
        if (oldValue != checked) {
            for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
                if (hasItemView(i)) {
                    mItemViews[i].get().setChecked(checked);
                }
            }
        }
    }

    @Override
    public boolean isCheckable() {
        return (mFlags & CHECKABLE) == CHECKABLE;
    }

    @Override
    public MenuItem setCheckable(boolean checkable) {
        final boolean oldValue = isCheckable();
        mFlags = (mFlags & ~CHECKABLE) | (checkable ? CHECKABLE : 0);
        if (oldValue != checkable) {
            for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
                if (hasItemView(i)) {
                    mItemViews[i].get().setCheckable(checkable);
                }
            }
        }

        return this;
    }

    public void setExclusiveCheckable(boolean exclusive) {
        mFlags = (mFlags & ~EXCLUSIVE) | (exclusive ? EXCLUSIVE : 0);
    }

    public boolean isExclusiveCheckable() {
        return (mFlags & EXCLUSIVE) == EXCLUSIVE;
    }

    @Override
    public CharSequence getTitleCondensed() {
        return mTitleCondensed;
    }

    @Override
    public MenuItem setTitleCondensed(CharSequence title) {
        mTitleCondensed = title;
        return this;
    }

    @Override
    public int getGroupId() {
        return mGroupId;
    }

    @Override
    public int getOrder() {
        return mCategoryOrder;
    }

    public int getOrdering() {
        return mOrdering;
    }

    @Override
    public SubMenuBuilder getSubMenu() {
        return mSubMenu;
    }

    /**
     * Set the sub-menu of this item.
     *
     * @param subMenu Sub-menu instance.
     * @return This Item so additional setters can be called.
     */
    MenuItem setSubMenu(SubMenuBuilder subMenu) {
        mSubMenu = subMenu;
        return this;
    }

    @Override
    public boolean hasSubMenu() {
        return (mSubMenu != null) && (mSubMenu.size() > 0);
    }

    @Override
    public char getAlphabeticShortcut() {
        return mAlphabeticalShortcut;
    }

    @Override
    public char getNumericShortcut() {
        return mNumericalShortcut;
    }

    @Override
    public MenuItem setAlphabeticShortcut(char alphaChar) {
        mAlphabeticalShortcut = Character.toLowerCase(alphaChar);
        return this;
    }

    @Override
    public MenuItem setNumericShortcut(char numericChar) {
        mNumericalShortcut = numericChar;
        return this;
    }

    @Override
    public MenuItem setShortcut(char numericChar, char alphaChar) {
        setNumericShortcut(numericChar);
        setAlphabeticShortcut(alphaChar);
        return this;
    }

    @Override
    public void setShowAsAction(int actionEnum) {
        mShowAsAction = actionEnum;
    }

    public int getShowAsAction() {
        return mShowAsAction;
    }

    public boolean showsTextAsAction() {
        return (mShowAsAction & MenuItem.SHOW_AS_ACTION_WITH_TEXT) == MenuItem.SHOW_AS_ACTION_WITH_TEXT;
    }

    @Override
    public View getActionView() {
        if (mActionView != null) {
            return mActionView;
        }
        if (mActionViewRes != View.NO_ID) {
            return LayoutInflater.from(mMenu.getContext()).inflate(mActionViewRes, null, false);
        }
        return null;
    }

    @Override
    public Drawable getIcon() {
        if (mIcon != null) {
            return mIcon;
        }
        if (mIconRes != View.NO_ID) {
            return mMenu.getContext().getResources().getDrawable(mIconRes);
        }
        return null;
    }

    @Override
    public ContextMenuInfo getMenuInfo() {
        return null;
    }

    @Override
    public MenuItem setActionView(int resId) {
        ViewGroup viewGroup = (ViewGroup)mMenu.getMenuView(MenuBuilder.TYPE_NATIVE, null);
        View view = LayoutInflater.from(mMenu.getContext()).inflate(resId, viewGroup, false);
        return setActionView(view);
    }

    @Override
    public MenuItem setActionView(View view) {
        mActionView = view;
        mMenu.onItemActionRequestChanged(this);
        return this;
    }

    @Override
    public MenuItem setIcon(Drawable icon) {
        mIcon = icon;
        mIconRes = View.NO_ID;
        setIconOnViews(icon);
        return this;
    }

    void setIconOnViews(Drawable icon) {
        for (int i = MenuBuilder.NUM_TYPES - 1; i >= 0; i--) {
            if (hasItemView(i)) {
                mItemViews[i].get().setIcon(icon);
            }
        }
    }

    @Override
    public android.view.MenuItem setOnMenuItemClickListener(final android.view.MenuItem.OnMenuItemClickListener menuItemClickListener) {
        return this.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return menuItemClickListener.onMenuItemClick(new MenuItemWrapper(item));
            }
        });
    }

    @Override
    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        mClickListener = menuItemClickListener;
        return this;
    }

    /**
     * Returns the currently set menu click listener for this item.
     *
     * @return Click listener or {@code null}.
     */
    public OnMenuItemClickListener getOnMenuItemClickListener() {
        return mClickListener;
    }



    public static final class NativeItemView implements MenuView.ItemView {
    	private static android.view.MenuItem addTo(MenuItemImpl menuItem, android.view.Menu menu) {
            	android.view.MenuItem nativeMenuItem = menu.add(menuItem.mGroupId, menuItem.mItemId, menuItem.mCategoryOrder, menuItem.mTitle)
                    .setAlphabeticShortcut(menuItem.mAlphabeticalShortcut)
                    .setNumericShortcut(menuItem.mNumericalShortcut)
                    .setVisible(menuItem.isVisible())
                    .setIntent(menuItem.mIntent)
                    .setCheckable(menuItem.isCheckable())
                    .setChecked(menuItem.isChecked())
                    .setOnMenuItemClickListener(menuItem.mClickListener);

                if (menuItem.isExclusiveCheckable()) {
                    menu.setGroupCheckable(menuItem.mGroupId, true, true);
                }
                
                return nativeMenuItem;
    	}
    	
        private MenuItemImpl mItemData;
        private android.view.MenuItem mNativeMenuItem;


        public void attach(android.view.Menu menu) {
            if (!mItemData.hasSubMenu()) {
            	mNativeMenuItem = addTo(mItemData, menu);
            } else {
                android.view.SubMenu subMenu = menu.addSubMenu(mItemData.mGroupId, mItemData.mItemId, mItemData.mCategoryOrder, mItemData.mTitle);
                mNativeMenuItem = subMenu.getItem();
                if (mItemData.mIconRes != View.NO_ID) {
                    subMenu.setIcon(mItemData.mIconRes);
                } else {
                    subMenu.setIcon(mItemData.mIcon);
                }
                for (MenuItemImpl item : mItemData.mSubMenu.getItems()) {
                    addTo(item, subMenu);
                }

                if (mItemData.mSubMenu.getItem(0).isExclusiveCheckable()) {
                    int checked = mItemData.getSubMenuSelected();
                    if (checked != -1) {
                        subMenu.getItem(checked).setChecked(true);
                    }
                }
            }
        }

        @Override
        public MenuItemImpl getItemData() {
            return mItemData;
        }

        @Override
        public void initialize(MenuItemImpl itemData, int menuType) {
            mItemData = itemData;
            //Initialization is deferred until we attach to a native menu
        }

        @Override
        public boolean prefersCondensedTitle() {
            return true;
        }

        @Override
        public void setCheckable(boolean checkable) {
            if (mNativeMenuItem != null) {
                mNativeMenuItem.setCheckable(checkable);
            }
        }

        @Override
        public void setChecked(boolean checked) {
            if (mNativeMenuItem != null) {
                mNativeMenuItem.setChecked(checked);
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (mNativeMenuItem != null) {
                mNativeMenuItem.setEnabled(enabled);
            }
        }

        @Override
        public void setIcon(Drawable icon) {
            if (mNativeMenuItem != null) {
                mNativeMenuItem.setIcon(icon);
            }
        }

        @Override
        public void setShortcut(boolean showShortcut, char shortcutKey) {
            if (mNativeMenuItem != null) {
                mNativeMenuItem.setAlphabeticShortcut(shortcutKey);
            }
        }

        @Override
        public void setTitle(CharSequence title) {
            if (mNativeMenuItem != null) {
                mNativeMenuItem.setTitle(title);
            }
        }

        @Override
        public boolean showsIcon() {
            return true;
        }
    }
}