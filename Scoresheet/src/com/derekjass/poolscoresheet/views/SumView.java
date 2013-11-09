package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import com.derekjass.poolscoresheet.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

public class SumView extends BasicIntegerView
implements IntegerView.ValueChangedListener {

	public static final int ALWAYS_SUM = 1;
	public static final int GRAY_SUM_WHEN_MISSING = 2;
	public static final int NO_SUM_WHEN_MISSING = 3;

	private static final int BG = R.drawable.box_bg;
	private static final int BG_CIRCLED = R.drawable.round_winner_bg;

	private Set<IntegerView> watchedViews;
	private final int sumRule;
	private final boolean showZeroSum;
	private boolean setComplete;

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.SumView,
				0, 0);

		try {
			sumRule = a.getInt(R.styleable.SumView_sumRule, 1);
			showZeroSum = a.getBoolean(R.styleable.SumView_showZeroSum, true);
		} finally {
			a.recycle();
		}

		watchedViews = new HashSet<IntegerView>();
		setComplete = false;
	}

	@Override
	public void onValueChanged(IntegerView v) {
		setComplete = v.hasValue();
		boolean atLeastOneHasValue = v.hasValue();

		int sum = 0;

		for (IntegerView view : watchedViews) {
			setComplete &= view.hasValue();
			atLeastOneHasValue |= view.hasValue();
			if (sumRule == NO_SUM_WHEN_MISSING && !setComplete) {
				clearValue();
				return;
			}
			sum += view.getValue();
		}

		if (atLeastOneHasValue) {
			if (sum == 0 && !showZeroSum) {
				clearValue();
				return;
			}
			if (sumRule == GRAY_SUM_WHEN_MISSING && !setComplete) {
				setTextColor(getResources().getColor(R.color.light_gray));
			} else {
				setTextColor(Color.BLACK);
			}
			setValue(sum);
		} else {
			clearValue();
		}
	}

	@Override
	public void onListenerAttached(IntegerView subject) {
		watchedViews.add(subject);
	}

	@Override
	public void onListenerRemoved(IntegerView subject) {
		watchedViews.remove(subject);
	}

	@Override
	public boolean hasValue() {
		return super.hasValue() && setComplete;
	}

	public void setCircled(boolean circled) {
		setBackgroundResource(circled ? BG_CIRCLED : BG);
	}
}
