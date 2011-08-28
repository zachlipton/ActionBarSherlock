package com.actionbarsherlock.internal.view.menu;

import java.lang.ref.WeakReference;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.actionbarsherlock.R;

public class ActionMenuItemView extends RelativeLayout implements MenuView.ItemView, View.OnClickListener {
    private ImageView mImageButton;
    private TextView mTextButton;
    private MenuBuilder.ItemInvoker mItemInvoker;
    private MenuItemImpl mItemData;
    private WeakReference<ImageView> mDivider;

    public ActionMenuItemView(Context context) {
        this(context, null);
    }
    public ActionMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.actionButtonStyle);
    }
    public ActionMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
    }

    public boolean hasText() {
        return mTextButton.getVisibility() != View.GONE;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mImageButton = (ImageView) findViewById(R.id.abs__item_icon);
        mImageButton.setOnClickListener(this);
        mTextButton = (TextView) findViewById(R.id.abs__item_text);
        mTextButton.setOnClickListener(this);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mImageButton.setEnabled(enabled);
        mTextButton.setEnabled(enabled);
    }

    public void setDivider(ImageView divider) {
        mDivider = new WeakReference<ImageView>(divider);
    }

    public void setVisible(boolean visible) {
        final int visibility = visible ? View.VISIBLE : View.GONE;
        if ((mDivider != null) && (mDivider.get() != null)) {
            mDivider.get().setVisibility(visibility);
        }
        setVisibility(visibility);
    }

    public void setIcon(Drawable icon) {
        mImageButton.setImageDrawable(icon);
    }

	public void setItemInvoker(MenuBuilder.ItemInvoker itemInvoker) {
		mItemInvoker = itemInvoker;
	}

    public void setTitle(CharSequence title) {
        mTextButton.setText(title);
    }

    @Override
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;
        setId(itemData.getItemId());
        setIcon(itemData.getIcon());
        setTitle(itemData.getTitle());
		if (itemData.isVisible()) {
			setVisibility(View.VISIBLE);
			setEnabled(itemData.isEnabled());
		}
    }

    @Override
    public MenuItemImpl getItemData() {
        return mItemData;
    }

    @Override
    public void setCheckable(boolean checkable) {
        // No-op
    }

    @Override
    public void setChecked(boolean checked) {
        // No-op
    }

    @Override
    public void setShortcut(boolean showShortcut, char shortcutKey) {
        // No-op
    }

    @Override
    public boolean prefersCondensedTitle() {
        return true;
    }

    @Override
    public boolean showsIcon() {
        return true;
    }

	@Override
	public void onClick(View v) {
		if (mItemInvoker != null) {
			mItemInvoker.invokeItem(mItemData);
		}
	}
}