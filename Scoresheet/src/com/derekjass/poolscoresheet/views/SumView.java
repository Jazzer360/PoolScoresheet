package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import com.derekjass.poolscoresheet.R;

import android.content.Context;
import android.content.res.TypedArray;
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

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.PlayerScoreView,
				0, 0);

		try {
			sumRule = a.getInteger(R.styleable.SumView_sumRule, 1);
		} finally {
			a.recycle();
		}

		watchedViews = new HashSet<IntegerView>();
	}

	@Override
	public void onValueChanged(IntegerView v) {
		if (!v.hasValue() && sumRule == NO_SUM_WHEN_MISSING) {
			clearValue();
			return;
		}
		int sum = 0;
		for (IntegerView view : watchedViews) {
			if (!view.hasValue() && sumRule == NO_SUM_WHEN_MISSING) {
				clearValue();
				return;
			}
			sum += view.getValue();
		}
		
		if (sumRule == ALWAYS_SUM) {
			setValue(sum);
		} else {
			setHint(String.valueOf(sum));
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
	
	public void setCircled(boolean circled) {
		setBackgroundResource(circled ? BG_CIRCLED : BG);
	}
}
