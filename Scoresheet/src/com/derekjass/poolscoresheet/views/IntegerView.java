package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.derekjass.poolscoresheet.R;

public class IntegerView extends TextView implements SummableInteger {

	private int value;
	private boolean hasValue;
	private Set<OnValueChangedListener> listeners;

	private final boolean mustSum;

	public IntegerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.IntegerView,
				0, 0);

		try {
			mustSum = a.getBoolean(R.styleable.IntegerView_mustSum, true);
		} finally {
			a.recycle();
		}

		value = 0;
		hasValue = false;
		listeners = new HashSet<OnValueChangedListener>();
	}

	@Override
	public void setValue(int value) {
		this.value = value;
		hasValue = true;
		setText(String.valueOf(value));
		notifyListeners();
	}

	@Override
	public void setValue(CharSequence value) {
		this.value = stringToInt(value.toString());
		hasValue = true;
		setText(value);
		notifyListeners();
	}

	@Override
	public void clearValue() {
		value = 0;
		hasValue = false;
		setText("");
		notifyListeners();
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getValueAsString() {
		return getText().toString();
	}

	@Override
	public boolean hasValue() {
		return hasValue;
	}

	@Override
	public boolean hasSoftValue() {
		return false;
	}

	@Override
	public boolean mustSum() {
		return mustSum;
	}

	@Override
	public void addOnValueChangedListener(OnValueChangedListener li) {
		listeners.add(li);
		li.onAttachListener(this);
	}

	@Override
	public void removeOnValueChangedListener(OnValueChangedListener li) {
		listeners.remove(li);
		li.onAttachListener(this);
	}

	private void notifyListeners() {
		for (OnValueChangedListener listener : listeners) {
			listener.onValueChanged(this);
		}
	}

	private static int stringToInt(String string) {
		return !TextUtils.isEmpty(string) ? Integer.valueOf(string) : 0;
	}
}
