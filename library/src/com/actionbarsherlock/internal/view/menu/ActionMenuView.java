package com.actionbarsherlock.internal.view.menu;

import java.util.List;
import com.actionbarsherlock.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ActionMenuView extends LinearLayout implements MenuView, MenuBuilder.ItemInvoker {
	private static final int DIVIDER_PADDING = 12;
	
	private Drawable mDivider;
	private float mDividerPadding;
	private int mMaxItems;
	private MenuBuilder mMenu;
	//private OverflowMenuButton mOverflowButton;
	//private MenuPopupHelper mOverflowPopup;
	//private OpenOverflowRunnable mPostedOpenRunnable;
	private boolean mReserveOverflow;
	//private final Runnable mShowOverflow;
	private int mWidthLimit;
	
	public ActionMenuView(Context context) {
		this(context, null);
	}
	public ActionMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//mShowOverflow = new Runnable() {
		//	@Override
		//	public void run() {
		//		showOverflowMenu();
		//	}
		//};
		
		mMaxItems = getMaxActionButtons();
		mReserveOverflow = false;//(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		mWidthLimit = getResources().getDisplayMetrics().widthPixels / 2;
		
		TypedArray a = context.obtainStyledAttributes(R.styleable.SherlockTheme);
		mDivider = a.getDrawable(R.styleable.SherlockTheme_dividerVertical);
		a.recycle();
		
		mDividerPadding = DIVIDER_PADDING * getResources().getDisplayMetrics().density;
		
		setBaselineAligned(false);
	}
	
	/* XXX UNUSED?
	private boolean addItemView(boolean something, ActionMenuItemView itemView) {
		itemView.setItemInvoker(this);
		final boolean hasText = itemView.hasText();
		if (hasText && something) {
			addView(makeDividerView(), makeDividerLayoutParams());
		}
		addView(itemView);
		return hasText;
	}
	*/
	
	private int getMaxActionButtons() {
		return getResources().getInteger(R.integer.max_action_buttons);
	}
	
	private static boolean isDivider(View view) {
		return (view != null) && (view.getId() == R.id.action_menu_divider);
	}
	
	private LinearLayout.LayoutParams makeActionViewLayoutParams(View view) {
		return generateLayoutParams(view.getLayoutParams());
	}
	
	private LinearLayout.LayoutParams makeDividerLayoutParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT
		);
		params.topMargin = params.bottomMargin = (int)mDividerPadding;
		return params;
	}
	
	private ImageView makeDividerView() {
		ImageView view = new ImageView(getContext());
		view.setImageDrawable(mDivider);
		view.setScaleType(ImageView.ScaleType.FIT_XY);
		view.setId(R.id.action_menu_divider);
		return view;
	}
	
	private boolean removeChildrenUntil(int startAt, View paramView, boolean removeDivider) {
		//TODO
		final int count = getChildCount();
		int index = startAt;
		while (index < count) {
			break;
		}
		
		return false;
	}
	
	@Override
	protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT
		);
		params.gravity = Gravity.CENTER_VERTICAL;
		return params;
	}
	
	@Override
	protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
		if (params instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams((LinearLayout.LayoutParams)params);
			if (newParams.gravity <= 0) {
				newParams.gravity = Gravity.CENTER_VERTICAL;
			}
			return newParams;
		}
		return generateDefaultLayoutParams();
	}
	
	public View getOverflowButton() {
		return null;//mOverflowButton;
	}

	@Override
	public int getWindowAnimations() {
		return 0;
	}
	
	public boolean hideOverflowMenu() {
		//TODO
		return false;
	}

	@Override
	public void initialize(MenuBuilder menu, int menuType) {
		if (mReserveOverflow) {
			//TODO
		}
		
		menu.setActionWidthLimit(mWidthLimit);
		menu.setMaxActionItems(mMaxItems);
		if (mMenu != menu) {
			mMenu = menu;
			updateChildren(true);
		} else {
			updateChildren(false);
		}
	}
	
	@Override
	public boolean invokeItem(MenuItemImpl item) {
		return mMenu.performItemAction(item, 0);
	}
	
	public boolean isOverflowMenuOpen() {
		//TODO
		return false;
	}
	
	public boolean isOverflowMenuShowing() {
		//TODO
		return false;
	}
	
	public boolean isOverflowReserved() {
		return mReserveOverflow;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		mReserveOverflow = false;//(newConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		mMaxItems = getMaxActionButtons();
		mWidthLimit = getResources().getDisplayMetrics().widthPixels / 2;
		
		if (mMenu != null) {
			mMenu.setMaxActionItems(mMaxItems);
			updateChildren(false);
		}
		
		//if ((mOverflowPopup != null) && mOverflowPopup.isShowing()) {
		//	mOverflowPopup.dismiss();
		//	post(mShowOverflow);
		//}
	}
	
	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		//if ((mOverflowPopup != null) && mOverflowPopup.isShowing()) {
		//	mOverflowPopup.dismiss();
		//}
		//removeCallbacks(mShowOverflow);
		//if (mPostedOpenRunnable != null) {
		//	removeCallbacks(mPostedOpenRunnable);
		//}
	}
	
	public void openOverflowMenu() {
		//TODO
	}
	
	public void setOverflowReserved(boolean reserved) {
		//TODO mReserveOverflow = reserved;
	}
	
	public boolean showOverflowMenu() {
		//if (mOverflowButton != null) {
		//	//TODO dispatch openOptionsMenu() to activity
		//	return true;
		//}
		return false;
	}

	@Override
	public void updateChildren(boolean cleared) {
		List<MenuItemImpl> menuItems = mMenu.getActionItems(mReserveOverflow);
		final int menuItemCount = menuItems.size();
		boolean hasNextDivider = false;
		int viewIndex = 0;
		int menuItemIndex = 0;
		while (menuItemIndex < menuItemCount) {
			final MenuItemImpl menuItem = menuItems.get(menuItemIndex);
			boolean hasDivider = false;
			
			if (hasNextDivider) {
				if (!isDivider(getChildAt(viewIndex))) {
					addView(makeDividerView(), viewIndex, makeDividerLayoutParams());
				}
				viewIndex += 1;
				hasDivider = true;
				hasNextDivider = false;
			}

			View menuItemView = null;
			if (menuItem.getActionView() != null) {
				menuItemView = menuItem.getActionView();
				menuItemView.setLayoutParams(makeActionViewLayoutParams(menuItemView));
			} else {
				ActionMenuItemView actionMenuItem = (ActionMenuItemView)menuItem.getItemView(MenuBuilder.TYPE_SHERLOCK, this);
				actionMenuItem.setItemInvoker(this);
				hasDivider = (menuItemIndex > 0) && !hasDivider && actionMenuItem.hasText() && (menuItem.getIcon() == null);
				hasNextDivider = actionMenuItem.hasText();
				menuItemView = actionMenuItem;
			}
			
			if (removeChildrenUntil(viewIndex, menuItemView, hasDivider)) {
				addView(makeDividerView(), viewIndex, makeDividerLayoutParams());
			}
			if (hasDivider) {
				viewIndex += 1;
			}
			if (getChildAt(viewIndex) != menuItemView) {
				addView(menuItemView, viewIndex);
			}
			viewIndex += 1;
			menuItemIndex += 1;
		}
		
		//if (mOverflowButton != null) {
		//	//TODO add divider and overflow
		//}
	}
	
	
	class OverflowMenuButton extends ImageButton {
		public OverflowMenuButton(Context context) {
			super(context, null, R.attr.actionOverflowButtonStyle);
			setClickable(true);
			setFocusable(true);
			setVisibility(View.VISIBLE);
			setEnabled(true);
		}
		
		public boolean performClick() {
			return super.performClick() ? true : ActionMenuView.this.showOverflowMenu();
		}
	}
}
