package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.derekjass.poolscoresheet.R;

public class SumView extends IntegerView
implements SummableInteger.OnValueChangedListener {

	private static final int[] STATE_CIRCLED = {R.attr.isCircled};

	protected static final int ALWAYS_SUM = 1;
	protected static final int GRAY_SUM_WHEN_MISSING = 2;
	protected static final int NO_SUM_WHEN_MISSING = 3;

	protected static final int NO_ANIMATION = 1;
	protected static final int FLIP_ANIMATION = 2;

	private boolean mIsCircled;
	private boolean mHasSoftValue;
	private Set<SummableInteger> mWatchedViews;

	private final int mSumRule;
	private final int mAnimation;

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.SumView,
				0, 0);

		try {
			mSumRule = a.getInt(R.styleable.SumView_sumRule, 1);
			mAnimation = a.getInt(R.styleable.SumView_sumChangeAnimation, 1);
			setCircled(a.getBoolean(R.styleable.SumView_isCircled, false));
		} finally {
			a.recycle();
		}

		mHasSoftValue = false;
		mWatchedViews = new HashSet<SummableInteger>();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState =
				super.onCreateDrawableState(extraSpace + 1);
		if (mIsCircled) mergeDrawableStates(drawableState, STATE_CIRCLED);
		return drawableState;
	}

	@Override
	public void onValueChanged(SummableInteger v) {
		boolean setComplete = !v.mustSum() ||
				(v.hasValue() && !v.hasSoftValue());
		boolean atLeastOneHasValue = v.hasValue() || v.hasSoftValue();
		int sum = 0;

		for (SummableInteger view : mWatchedViews) {
			setComplete &= !view.mustSum() ||
					(view.hasValue() && !view.hasSoftValue());
			atLeastOneHasValue |= view.hasValue() || view.hasSoftValue();
			if (mSumRule == NO_SUM_WHEN_MISSING && !setComplete) {
				clearValue();
				return;
			}
			sum += view.getValue();
		}

		if (atLeastOneHasValue) {
			if (mSumRule == GRAY_SUM_WHEN_MISSING && !setComplete) {
				setTextColor(getResources().getColor(R.color.light_gray));
				mHasSoftValue = true;
				setValue(sum);
			} else {
				setTextColor(Color.BLACK);
				mHasSoftValue = false;
				setValue(sum);
			}
		} else {
			mHasSoftValue = false;
			clearValue();
		}
	}

	@Override
	public void setValue(final int value) {
		switch (mAnimation) {
		case FLIP_ANIMATION:
			animate().rotationX(90f).setDuration(175)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					SumView.super.setValue(value);
					animate().rotationX(0f).setDuration(175).setListener(null);
				}
			});
			return;
		default:
			super.setValue(value);
		}
	}

	@Override
	public boolean hasSoftValue() {
		return mHasSoftValue;
	}

	@Override
	public void onAttachListener(SummableInteger subject) {
		mWatchedViews.add(subject);
	}

	@Override
	public void onDetachListener(SummableInteger subject) {
		mWatchedViews.add(subject);
	}

	public int countAddendOccurrences(int value) {
		int occurrences = 0;
		for (SummableInteger view : mWatchedViews) {
			if (view instanceof SumView) {
				occurrences += ((SumView) view).countAddendOccurrences(value);
			} else if (view.getValue() == value) {
				occurrences++;
			}
		}
		return occurrences;
	}

	public void setCircled(boolean circled) {
		if (mIsCircled != circled) {
			mIsCircled = circled;
			refreshDrawableState();
		}
	}

	public boolean isCircled() {
		return mIsCircled;
	}
}
