package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.derekjass.poolscoresheet.R;

public class SumView extends BasicIntegerView
implements IntegerView.ValueChangedListener {

	public static final int ALWAYS_SUM = 1;
	public static final int GRAY_SUM_WHEN_MISSING = 2;
	public static final int NO_SUM_WHEN_MISSING = 3;

	private static final int BG = R.drawable.box_bg;
	private static final int BG_CIRCLED = R.drawable.round_winner_bg;

	private boolean hasSoftValue;
	private Set<IntegerView> watchedViews;

	private final int sumRule;

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.SumView,
				0, 0);

		try {
			sumRule = a.getInt(R.styleable.SumView_sumRule, 1);
		} finally {
			a.recycle();
		}

		hasSoftValue = false;
		watchedViews = new HashSet<IntegerView>();
	}

	@Override
	public void onValueChanged(IntegerView v) {
		boolean setComplete = !v.mustSum() || v.hasValue() && !v.hasSoftValue();
		boolean atLeastOneHasValue = v.hasValue() || v.hasSoftValue();
		int sum = 0;

		for (IntegerView view : watchedViews) {
			setComplete &= !view.mustSum() || view.hasValue() && !view.hasSoftValue();
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
	public void onAttachListener(IntegerView subject) {
		watchedViews.add(subject);
	}

	@Override
	public void onDetachListener(IntegerView subject) {
		watchedViews.add(subject);
	}

	public void setCircled(boolean circled) {
		setBackgroundResource(circled ? BG_CIRCLED : BG);
	}
}
