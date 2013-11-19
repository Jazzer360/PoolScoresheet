package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.derekjass.poolscoresheet.R;

public class SumView extends IntegerView
implements SummableInteger.OnValueChangedListener {

	private static final int[] STATE_CIRCLED = {R.attr.isCircled};

	public static final int ALWAYS_SUM = 1;
	public static final int GRAY_SUM_WHEN_MISSING = 2;
	public static final int NO_SUM_WHEN_MISSING = 3;

	private boolean isCircled;
	private boolean hasSoftValue;
	private Set<SummableInteger> watchedViews;

	private final int sumRule;

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.SumView,
				0, 0);

		try {
			sumRule = a.getInt(R.styleable.SumView_sumRule, 1);
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
