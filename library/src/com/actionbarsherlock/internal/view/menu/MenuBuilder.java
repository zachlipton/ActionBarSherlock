/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
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
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * An implementation of the {@link android.view.Menu} interface for use in
 * inflating menu XML resources to be added to a third-party action bar.
 *
 * @author Jake Wharton <jakewharton@gmail.com>
 * @see <a href="http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/com/android/internal/view/menu/MenuBuilder.java">com.android.internal.view.menu.MenuBuilder</a>
 */
public class MenuBuilder implements Menu {
	private static final boolean DEBUG = true;
	
    private static final int DEFAULT_ITEM_ID = 0;
    private static final int DEFAULT_GROUP_ID = 0;
    private static final int DEFAULT_ORDER = 0;

    public static final int NUM_TYPES = 2;
    public static final int TYPE_ACTION_BAR = 0;
    public static final int TYPE_NATIVE = 1;

    static final int[] THEME_RES_FOR_TYPE = new int [] {
        R.styleable.SherlockTheme_actionButtonStyle,
        0,
    };
    static final int[] LAYOUT_RES_FOR_TYPE = new int[] {
        R.layout.abs__action_menu_layout,
        0,
    };
    static final int[] ITEM_LAYOUT_RES_FOR_TYPE = new int[] {
        R.layout.abs__action_menu_item_layout,
        0,
    };

    /**
     * This is the part of an order integer that the user can provide.
     * @hide
     */
    static final int USER_MASK = 0x0000ffff;

    /**
     * Bit shift of the user portion of the order integer.
     * @hide
     */
    static final int USER_SHIFT = 0;

    /**
     * This is the part of an order integer that supplies the category of the
     * item.
     * @hide
     */

    static final int CATEGORY_MASK = 0xffff0000;

    /**
     * Bit shift of the category portion of the order integer.
     * @hide
     */
    static final int CATEGORY_SHIFT = 16;

    private static final int[] CATEGORY_TO_ORDER = new int[] {
        1, /* No category */
        4, /* CONTAINER */
        5, /* SYSTEM */
        3, /* SECONDARY */
        2, /* ALTERNATIVE */
        0, /* SELECTED_ALTERNATIVE */
    };


    /** Context used for resolving any resources. */
    private final Context mContext;

    private MenuType[] mMenuTypes;
    
    /** Child {@link ActionBarMenuItem} items. */
    private final ArrayList<MenuItemImpl> mItems;
    private final ArrayList<MenuItemImpl> mActionItems;
    private final ArrayList<MenuItemImpl> mNonActionItems;
    private final ArrayList<MenuItemImpl> mVisibleItems;
    
    private boolean mIsActionItemsStale;
    private boolean mIsVisibleItemsStale;
    private boolean mPreventDispatchingItemsChanged = false;
    private boolean mReserveActionOverflow;

    private int mActionWidthLimit;
    private int mMaxActionItems;
    
    private ViewGroup mMeasureActionButtonParent;
    private SparseBooleanArray mActionButtonGroups;

    /** Menu callback that will receive various events. */
    private Callback mCallback;

    private boolean mShowActionItemText;



    /**
     * Create a new action bar menu.
     *
     * @param context Context used if resource resolution is required.
     */
    public MenuBuilder(Context context) {
    	mMenuTypes = new MenuType[NUM_TYPES];
        mContext = context;
        
        mItems = new ArrayList<MenuItemImpl>();
        mActionItems = new ArrayList<MenuItemImpl>();
        mNonActionItems = new ArrayList<MenuItemImpl>();
        mVisibleItems = new ArrayList<MenuItemImpl>();

        mActionButtonGroups = new SparseBooleanArray();
        
        mIsActionItemsStale = true;
        mIsVisibleItemsStale = true;
    }


    /**
     * Adds an item to the menu.  The other add methods funnel to this.
     *
     * @param itemId Unique item ID.
     * @param groupId Group ID.
     * @param order Order.
     * @param title Item title.
     * @return MenuItem instance.
     */
    private MenuItem addInternal(int itemId, int groupId, int order, CharSequence title) {
        final int ordering = getOrdering(order);
        final MenuItemImpl item = new MenuItemImpl(this, groupId, itemId, order, ordering, title, MenuItem.SHOW_AS_ACTION_NEVER);

        mItems.add(findInsertIndex(mItems, ordering), item);
        return item;
    }

    private static int findInsertIndex(ArrayList<MenuItemImpl> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItemImpl item = items.get(i);
            if (item.getOrdering() <= ordering) {
                return i + 1;
            }
        }

        return 0;
    }
    
    private void flagActionItems(boolean reserveActionOverflow) {
        if (reserveActionOverflow != mReserveActionOverflow) {
            mReserveActionOverflow = reserveActionOverflow;
            mIsActionItemsStale = true;
        }
        if (!mIsActionItemsStale) {
            return;
        }

        final ArrayList<MenuItemImpl> visibleItems = getVisibleItems();
        final int itemsSize = visibleItems.size();
        int maxActions = mMaxActionItems;
        int widthLimit = mActionWidthLimit;
        int querySpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        ViewGroup parent = getMeasureActionButtonParent();
        int requiredItems = 0;
        int requestedItems = 0;
        int firstActionWidth = 0;
        boolean hasOverflow = false;

        for (MenuItemImpl item : visibleItems) {
            final boolean canBeAction = mShowActionItemText || (item.getIcon() != null);
            if (canBeAction && item.requiresActionButton()) {
                requiredItems += 1;
            } else if (canBeAction && item.requestsActionButton()) {
                requestedItems += 1;
            } else {
                hasOverflow = true;
            }
        }

        if (reserveActionOverflow && hasOverflow && ((requiredItems + requestedItems) > maxActions)) {
            maxActions -= 1;
        }

        if (DEBUG) {
            Log.d("MenuBuilder", "visible item count = " + itemsSize);
            Log.d("MenuBuilder", "requiredItems = " + requiredItems);
            Log.d("MenuBuilder", "requestedItems = " + requestedItems);
            Log.d("MenuBuilder", "hasOverflow = " + hasOverflow);
            Log.d("MenuBuilder", "reserveOverflow = " + reserveActionOverflow);
            Log.d("MenuBuilder", "maxActions (global) = " + mMaxActionItems);
        }

        mActionButtonGroups.clear();
        for (int i = 0; i < itemsSize; i++) {
            if (DEBUG) {
                Log.d("MenuBuilder", "maxActions (local) = " + maxActions);
                Log.d("MenuBuilder", "widthLimit = " + widthLimit);
            }

            MenuItemImpl item = visibleItems.get(i);
            final int itemId = item.getItemId();
            final int groupId = item.getGroupId();
            final boolean inGroup = mActionButtonGroups.get(groupId);
            final boolean canBeAction = mShowActionItemText || (item.getIcon() != null);

            if (DEBUG) {
                Log.d("MenuBuilder", "ITEM: itemId = " + itemId + ", groupId = " + groupId + ", groupExists = " + inGroup);
            }

            if (canBeAction && item.requiresActionButton()) {
                if (DEBUG) {
                    Log.d("MenuBuilder", "ITEM: requires action button.");
                }

                View v = item.getActionView();
                if (v == null) {
                    v = (View)item.getItemView(MenuBuilder.TYPE_ACTION_BAR, parent);
                }

                v.measure(querySpec, querySpec);
                int measuredWidth = v.getMeasuredWidth();

                if (DEBUG) {
                    Log.d("MenuBuilder", "ITEM: view = " + v.toString());
                    Log.d("MenuBuilder", "ITEM: view width = " + measuredWidth);
                }

                widthLimit -= measuredWidth;

                if (firstActionWidth == 0) {
                    firstActionWidth = measuredWidth;
                }

                if (groupId != View.NO_ID) {
                    mActionButtonGroups.put(groupId, true);
                }
            } else if (canBeAction && item.requestsActionButton()) {
                if (DEBUG) {
                    Log.d("MenuBuilder", "ITEM: requests action button.");
                }

                boolean isAction = ((maxActions > 0) || inGroup) && (widthLimit > 0);
                maxActions -= 1;
                if (isAction) {
                    View v = item.getActionView();
                    if (v == null) {
                        v = (View)item.getItemView(MenuBuilder.TYPE_ACTION_BAR, parent);
                    }

                    v.measure(querySpec, querySpec);
                    int measuredWidth = v.getMeasuredWidth();

                    if (DEBUG) {
                        Log.d("MenuBuilder", "ITEM: view = " + v.toString());
                        Log.d("MenuBuilder", "ITEM: view width = " + measuredWidth);
                    }

                    widthLimit -= measuredWidth;

                    if (firstActionWidth == 0) {
                        firstActionWidth = measuredWidth;
                    }

                    if ((widthLimit + firstActionWidth) <= 0) {
                        isAction = false;
                    }
                }
                if (isAction) {
                    if (groupId != View.NO_ID) {
                        mActionButtonGroups.put(groupId, true);
                    }
                    item.setIsActionButton(true);
                    if (DEBUG) {
                        Log.d("MenuBuilder", "ITEM: isAction = true");
                    }
                }
            } else if (groupId != View.NO_ID) {
                if (inGroup) {
                    item.setIsActionButton(mActionButtonGroups.get(groupId));
                } else {
                    mActionButtonGroups.put(groupId, false);
                    for (int j = 0; j < i; j++) {
                        MenuItemImpl areYouMyGroupie = visibleItems.get(j);
                        if (areYouMyGroupie.getGroupId() == groupId) {
                            areYouMyGroupie.setIsActionButton(false);
                        }
                    }
                }
            }
        }

        mActionItems.clear();
        mNonActionItems.clear();
        for (MenuItemImpl item : visibleItems) {
            if (item.isActionButton()) {
                mActionItems.add(item);
            } else {
                mNonActionItems.add(item);
            }
        }

        mIsActionItemsStale = false;

        if (DEBUG) {
            Log.d("MenuBuilder", "item group count = " + mActionButtonGroups.size());
            Log.d("MenuBuilder", "action items count = " + mActionItems.size());
            Log.d("MenuBuilder", "non-action items count = " + mNonActionItems.size());
        }
    }

    public void setActionWidthLimit(int width) {
        mActionWidthLimit = width;
        mIsActionItemsStale = true;
    }

    void setMaxActionItems(int maxItems) {
        mMaxActionItems = maxItems;
        mIsActionItemsStale = true;
    }

    private ViewGroup getMeasureActionButtonParent() {
        if (mMeasureActionButtonParent == null) {
            mMeasureActionButtonParent = (ViewGroup)getMenuType(TYPE_ACTION_BAR).getInflater().inflate(LAYOUT_RES_FOR_TYPE[TYPE_ACTION_BAR], null, false);
        }
        return mMeasureActionButtonParent;
    }

    MenuType getMenuType(int menuType) {
        if (mMenuTypes[menuType] == null) {
            mMenuTypes[menuType] = new MenuType(menuType);
        }

        return mMenuTypes[menuType];
    }

    public View getMenuView(int menuType, ViewGroup parent) {
        return (View)getMenuType(menuType).getMenuView(parent);
    }

    void onItemActionRequestChanged(MenuItemImpl menuItem) {
        onItemsChanged(false);
    }

    void onItemVisibleChanged(MenuItemImpl paramMenuItemImpl) {
        onItemsChanged(false);
    }

    public MenuAdapter getOverflowMenuAdapter(int menuType) {
        return new OverflowMenuAdapter(menuType);
    }

    /**
     * Returns the ordering across all items. This will grab the category from
     * the upper bits, find out how to order the category with respect to other
     * categories, and combine it with the lower bits.
     *
     * @param categoryOrder The category order for a particular item (if it has
     *            not been or/add with a category, the default category is
     *            assumed).
     * @return An ordering integer that can be used to order this item across
     *         all the items (even from other categories).
     */
    private static int getOrdering(int categoryOrder) {
        final int index = (categoryOrder & CATEGORY_MASK) >> CATEGORY_SHIFT;

        if (index < 0 || index >= CATEGORY_TO_ORDER.length) {
            throw new IllegalArgumentException("order does not contain a valid category.");
        }

        return (CATEGORY_TO_ORDER[index] << CATEGORY_SHIFT) | (categoryOrder & USER_MASK);
    }

    private void onItemsChanged(boolean cleared) {
        if (!mPreventDispatchingItemsChanged) {
            if (!mIsVisibleItemsStale) {
                mIsVisibleItemsStale = true;
            }
            if (!mIsActionItemsStale) {
                mIsActionItemsStale = true;
            }

            for (int i = 0; i < NUM_TYPES; i++) {
                if ((mMenuTypes[i] != null) && mMenuTypes[i].hasMenuView()) {
                	mMenuTypes[i].mMenuView.get().updateChildren(cleared);
                }
            }
        }
    }

    ArrayList<MenuItemImpl> getVisibleItems() {
        if (mIsVisibleItemsStale) {
            mVisibleItems.clear();
            for (MenuItemImpl item : mItems) {
                if (item.isVisible()) {
                    mVisibleItems.add(item);
                }
            }

            mIsVisibleItemsStale = false;
            mIsActionItemsStale = true;
        }
        return mVisibleItems;
    }

    ArrayList<MenuItemImpl> getActionItems(boolean includeOverflow) {
        flagActionItems(includeOverflow);
        return mActionItems;
    }

    ArrayList<MenuItemImpl> getNonActionItems(boolean includeOverflow) {
        flagActionItems(includeOverflow);
        return mNonActionItems;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public Callback getCallback() {
        return mCallback;
    }

    public void setShowsActionItemText(boolean showsActionItemText) {
        mShowActionItemText = showsActionItemText;
    }

    /**
     * Gets the root menu (if this is a submenu, find its root menu).
     *
     * @return The root menu.
     */
    public MenuBuilder getRootMenu() {
        return this;
    }

    /**
     * Get a list of the items contained in this menu.
     *
     * @return List of {@link MenuItemImpl}s.
     */
    public final List<MenuItemImpl> getItems() {
        return mItems;
    }

    final MenuItemImpl remove(int index) {
        return mItems.remove(index);
    }

    final Context getContext() {
        return mContext;
    }

    void setExclusiveItemChecked(MenuItem item) {
        final int group = item.getGroupId();

        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl curItem = mItems.get(i);
            if (curItem.getGroupId() == group) {
                if (!curItem.isExclusiveCheckable()) continue;
                if (!curItem.isCheckable()) continue;

                // Check the item meant to be checked, uncheck the others (that are in the group)
                curItem.setCheckedInt(curItem == item);
            }
        }
    }

    // ** Menu Methods ** \\

    @Override
    public MenuItem add(int titleResourceId) {
        return addInternal(0, 0, 0, mContext.getResources().getString(titleResourceId));
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleResourceId) {
        return addInternal(itemId, groupId, order, mContext.getResources().getString(titleResourceId));
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return addInternal(itemId, groupId, order, title);
    }

    @Override
    public MenuItem add(CharSequence title) {
        return addInternal(0, 0, 0, title);
    }

    @Override
    public SubMenuBuilder addSubMenu(CharSequence title) {
        return addSubMenu(DEFAULT_GROUP_ID, DEFAULT_ITEM_ID, DEFAULT_ORDER, title);
    }

    @Override
    public SubMenuBuilder addSubMenu(int titleResourceId) {
        return addSubMenu(DEFAULT_GROUP_ID, DEFAULT_ITEM_ID, DEFAULT_ORDER, titleResourceId);
    }

    @Override
    public SubMenuBuilder addSubMenu(int groupId, int itemId, int order, int titleResourceId) {
        String title = mContext.getResources().getString(titleResourceId);
        return addSubMenu(groupId, itemId, order, title);
    }

    @Override
    public SubMenuBuilder addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        MenuItemImpl item = (MenuItemImpl)add(groupId, itemId, order, title);
        SubMenuBuilder subMenu = new SubMenuBuilder(mContext, this, item);
        item.setSubMenu(subMenu);
        return subMenu;
    }

    @Override
    public void clear() {
        mItems.clear();
    }

    @Override
    public void close() {
        close(true);
    }

    final void close(boolean something) {
        if (getCallback() != null) {
            getCallback().onCloseMenu(this, something);
        }
    }

    @Override
    public MenuItemImpl findItem(int itemId) {
        for (MenuItemImpl item : mItems) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    @Override
    public MenuItemImpl getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public boolean hasVisibleItems() {
        for (MenuItem item : mItems) {
            if (item.isVisible()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeItem(int itemId) {
        final int size = mItems.size();
        for (int i = 0; i < size; i++) {
            if (mItems.get(i).getItemId() == itemId) {
                mItems.remove(i);
                return;
            }
        }
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, android.view.MenuItem[] outSpecificItems) {
        PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> lri =
                pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        final int N = lri != null ? lri.size() : 0;

        if ((flags & FLAG_APPEND_TO_GROUP) == 0) {
            removeGroup(groupId);
        }

        for (int i=0; i<N; i++) {
            final ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(
                ri.specificIndex < 0 ? intent : specifics[ri.specificIndex]);
            rintent.setComponent(new ComponentName(
                    ri.activityInfo.applicationInfo.packageName,
                    ri.activityInfo.name));
            final MenuItem item = add(groupId, itemId, order, ri.loadLabel(pm))
                    .setIcon(ri.loadIcon(pm))
                    .setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }

        return N;
    }

    @Override
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean performIdentifierAction(int id, int flags) {
        throw new RuntimeException("Method not supported.");
    }

    public boolean performItemAction(MenuItem item, int flags) {
        final MenuItemImpl itemImpl = (MenuItemImpl)item;

        if ((itemImpl == null) || !itemImpl.isEnabled()) {
            return false;
        }

        boolean invoked = itemImpl.invoke();

        if (itemImpl.hasSubMenu()) {
            close(false);

            if (mCallback != null) {
                // Return true if the sub menu was invoked or the item was invoked previously
                invoked |= mCallback.onSubMenuSelected((SubMenuBuilder)item.getSubMenu());
            }
        } else {
            if ((flags & Menu.FLAG_PERFORM_NO_CLOSE) == 0) {
                close(true);
            }
        }

        return invoked;
    }

    @Override
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return false;
    }

    @Override
    public void removeGroup(int groupId) {
        final int size = mItems.size();
        for (int i = 0; i < size; i++) {
            if (mItems.get(i).getGroupId() == groupId) {
                mItems.remove(i);
            }
        }
    }

    @Override
    public void setGroupCheckable(int groupId, boolean checkable, boolean exclusive) {
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == groupId) {
                item.setExclusiveCheckable(exclusive);
                item.setCheckable(checkable);
            }
        }
    }

    @Override
    public void setGroupEnabled(int groupId, boolean enabled) {
        final int size = mItems.size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == groupId) {
                item.setEnabled(enabled);
            }
        }
    }

    @Override
    public void setGroupVisible(int groupId, boolean visible) {
        final int size = mItems.size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == groupId) {
                item.setVisible(visible);
            }
        }
    }

    @Override
    public void setQwertyMode(boolean isQwerty) {
        throw new RuntimeException("Method not supported.");
    }


    class OverflowMenuAdapter extends MenuAdapter {
        public OverflowMenuAdapter(int menuType) {
            super(menuType);
        }

        @Override
        public int getCount() {
            return getNonActionItems(true).size();
        }

        @Override
        public MenuItemImpl getItem(int index) {
            return getNonActionItems(true).get(index);
        }
    }

    public class MenuAdapter extends BaseAdapter {
        private int mMenuType;

        public MenuAdapter(int menuType) {
            mMenuType = menuType;
        }

        @Override
        public int getCount() {
            return getVisibleItems().size();
        }

        @Override
        public MenuItemImpl getItem(int index) {
            return getVisibleItems().get(index);
        }

        @Override
        public long getItemId(int itemId) {
            return itemId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return (View)((MenuItemImpl)getItem(position)).getItemView(mMenuType, parent);
        }
    }

    public interface ItemInvoker {
        boolean invokeItem(MenuItemImpl paramMenuItemImpl);
    }

    public interface Callback {
        void onCloseMenu(MenuBuilder paramMenuBuilder, boolean paramBoolean);
        void onCloseSubMenu(SubMenuBuilder paramSubMenuBuilder);
        boolean onMenuItemSelected(MenuBuilder paramMenuBuilder, MenuItem paramMenuItem);
        void onMenuModeChange(MenuBuilder paramMenuBuilder);
        boolean onSubMenuSelected(SubMenuBuilder paramSubMenuBuilder);
    }

    class MenuType {
        private LayoutInflater mInflater;
        private int mMenuType;
        private WeakReference<MenuView> mMenuView;

        MenuType(int menuType) {
            mMenuType = menuType;
        }


        LayoutInflater getInflater() {
            if (mInflater == null) {
                Context context = new ContextThemeWrapper(getContext(), MenuBuilder.THEME_RES_FOR_TYPE[mMenuType]);
                mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            return mInflater;
        }

        MenuView getMenuView(ViewGroup parent) {
            if (LAYOUT_RES_FOR_TYPE[mMenuType] == 0) {
                return null;
            }

            synchronized (this) {
                MenuView menuView = mMenuView != null ? mMenuView.get() : null;
                if (menuView == null) {
                    menuView = (MenuView)getInflater().inflate(LAYOUT_RES_FOR_TYPE[mMenuType], parent, false);
                    menuView.initialize(MenuBuilder.this, mMenuType);

                    // Cache the view
                    mMenuView = new WeakReference<MenuView>(menuView);
                }

                return menuView;
            }
        }

        boolean hasMenuView() {
            return (mMenuView != null) && (mMenuView.get() != null);
        }
    }
}