package com.actionbarsherlock.internal.view.menu;

import com.actionbarsherlock.R;
import android.content.Context;
import android.content.res.Configuration;
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
	private OverflowMenuButton mOverflowButton;
	private boolean mReserveOverflow;
	private int mWidthLimit;
	
	public ActionMenuView(Context context) {
		super(context);
	}
	
	public ActionMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMaxItems = getMaxActionButtons();
	}
	
	private boolean addItemView(boolean something, ActionMenuItemView itemView) {
		itemView.setItemInvoker(this);
		final boolean hasText = itemView.hasText();
		if (hasText && something) {
			addView(makeDividerView(), makeDividerLayoutParams());
		}
		addView(itemView);
		return hasText;
	}
	
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
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
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
	
	@Override
	protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
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

	@Override
	public int getWindowAnimations() {
		return 0;
	}

	@Override
	public void initialize(MenuBuilder menu, int menuType) {
		menu.setActionWidthLimit(mWidthLimit);
		menu.setMaxActionItems(mMaxItems);
		if (mMenu != menu) {
			mMenu = menu;
			updateChildren(true);
		}
	}
	
	@Override
	public boolean invokeItem(MenuItemImpl item) {
		return mMenu.performItemAction(item, 0);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		//TODO
	}
	
	public void setOverflowReserved(boolean reserved) {
		mReserveOverflow = reserved;
	}
	
	public boolean showOverflowMenu() {
		if (mOverflowButton != null) {
			//TODO dispatch openOptionsMenu() to activity
			return true;
		}
		return false;
	}

	@Override
	public void updateChildren(boolean cleared) {
		//TODO
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
