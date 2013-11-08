package com.derekjass.poolscoresheet.views;

import java.util.Set;

import android.content.Context;
import android.util.AttributeSet;

public class SumView extends BasicIntegerView
implements IntegerView.ValueChangedListener {

	private Set<IntegerView> watchedViews;

	public SumView(Context context, AttributeSet attrs) {
		super(context, attrs);


	}

	@Override
	public void onValueChanged(IntegerView v) {
		int sum = 0;
		for (IntegerView view : watchedViews) {
			if (!view.hasValue()) {
				clearValue();
				return;
			}
			sum += v.getValue();
		}
		setValue(sum);
	}

	@Override
	public void onListenerAttached(IntegerView subject) {
		watchedViews.add(subject);
	}

	@Override
	public void onListenerRemoved(IntegerView subject) {
		watchedViews.remove(subject);
	}
}
