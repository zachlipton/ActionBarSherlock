package com.actionbarsherlock.internal.view.animation;

import java.util.HashSet;
import android.view.animation.Animation;

public class AnimationGroup extends Animation {
	private final HashSet<Animation> mAnimations;
	private int mStarted;
	private Animation.AnimationListener mExternalListener;
	
	private final Animation.AnimationListener mListener = new Animation.AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			if (mStarted == 0) {
				mExternalListener.onAnimationStart(animation);
			}
			mStarted += 1;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			mExternalListener.onAnimationRepeat(animation);
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mStarted -= 1;
			if (mStarted == 0) {
				mExternalListener.onAnimationEnd(AnimationGroup.this);
			}
		}
	};
	
	public AnimationGroup() {
		mAnimations = new HashSet<Animation>();
		mStarted = 0;
		
		setDuration(1000);
		setRepeatCount(Animation.INFINITE);
	}
	
	public void addAnimation(Animation animation) {
		animation.setAnimationListener(mListener);
		mAnimations.add(animation);
	}
	public void removeAnimation(Animation animation) {
		mAnimations.remove(animation);
	}
	public void clearAnimations() {
		mAnimations.clear();
	}

	@Override
	public void cancel() {
		for (Animation animation : mAnimations) {
			animation.cancel();
		}
	}

	@Override
	public long getDuration() {
		long max = 0;
		for (Animation animation : mAnimations) {
			if (animation.getDuration() > max) {
				max = animation.getDuration();
			}
		}
		return max;
	}

	@Override
	public void reset() {
		for (Animation animation : mAnimations) {
			animation.reset();
		}
	}
	
	@Override
	public void setAnimationListener(Animation.AnimationListener listener) {
		mExternalListener = listener;
	}

	@Override
	public void start() {
		for (Animation animation : mAnimations) {
			animation.start();
		}
	}

	@Override
	public void startNow() {
		for (Animation animation : mAnimations) {
			animation.startNow();
		}
	}
}
