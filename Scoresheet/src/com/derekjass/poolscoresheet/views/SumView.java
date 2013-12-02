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

	private boolean isCircled;
	private boolean hasSoftValue;
	private Set<SummableInteger> watchedViews;

	private final int sumRule;
	private final int animation;

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.SumView,
				0, 0);

		try {
			sumRule = a.getInt(R.styleable.SumView_sumRule, 1);
			animation = a.getInt(R.styleable.SumView_sumChangeAnimation, 1);
			setCircled(a.getBoolean(R.styleable.SumView_isCircled, false));
		} finally {
			a.recycle();
		}

		hasSoftValue = false;
		watchedViews = new HashSet<SummableInteger>();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState =
				super.onCreateDrawableState(extraSpace + 1);
		if (isCircled) mergeDrawableStates(drawableState, STATE_CIRCLED);
		return drawableState;
	}

	@Override
	public void onValueChanged(SummableInteger v) {
		boolean setComplete = !v.mustSum() ||
				(v.hasValue() && !v.hasSoftValue());
		boolean atLeastOneHasValue = v.hasValue() || v.hasSoftValue();
		int sum = 0;

		for (SummableInteger view : watchedViews) {
			setComplete &= !view.mustSum() ||
					(view.hasValue() && !view.hasSoftValue());
			atLeastOneHasValue |= view.hasValue() || view.hasSoftValue();
			if (sumRule == NO_SUM_WHEN_MISSING && !setComplete) {
				clearValue();
				return;
			}
			sum += view.getValue();
		}

		if (atLeastOneHasValue) {
			if (sumRule == GRAY_SUM_WHEN_MISSING && !setComplete) {
				setTextColor(getResources().getColor(R.color.light_gray));
				hasSoftValue = true;
				setValue(sum);
			} else {
				setTextColor(Color.BLACK);
				hasSoftValue = false;
				setValue(sum);
			}
		} else {
			hasSoftValue = false;
			clearValue();
		}
	}

	@Override
	public void setValue(final int value) {
		switch (animation) {
		case FLIP_ANIMATION:
			animate().rotationX(90f)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					SumView.super.setValue(value);
					animate().rotationX(0f).setListener(null);
				}
			});
			return;
		default:
			super.setValue(value);
		}
	}

	@Override
	public boolean hasSoftValue() {
		return hasSoftValue;
	}

	@Override
	public void onAttachListener(SummableInteger subject) {
		watchedViews.add(subject);
	}

	@Override
	public void onDetachListener(SummableInteger subject) {
		watchedViews.add(subject);
	}

	public int countAddendOccurrences(int value) {
		int occurrences = 0;
		for (SummableInteger view : watchedViews) {
			if (view instanceof SumView) {
				occurrences += ((SumView) view).countAddendOccurrences(value);
			} else if (view.getValue() == value) {
				occurrences++;
			}
		}
		return occurrences;
	}

	public void setCircled(boolean circled) {
		if (isCircled != circled) {
			isCircled = circled;
			refreshDrawableState();
		}
	}

	public boolean isCircled() {
		return isCircled;
	}
}
