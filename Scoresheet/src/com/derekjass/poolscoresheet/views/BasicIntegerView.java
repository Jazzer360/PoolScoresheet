package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.derekjass.poolscoresheet.R;

public class BasicIntegerView extends TextView implements IntegerView {

	private final boolean alwaysHasValue;
	private Set<ValueChangedListener> listeners;

	public BasicIntegerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.BasicIntegerView,
				0, 0);

		try {
			alwaysHasValue = a.getBoolean(
					R.styleable.BasicIntegerView_alwaysHasValue, false);
		} finally {
			a.recycle();
		}

		setBackgroundResource(R.drawable.box_bg);
		listeners = new HashSet<ValueChangedListener>();
	}

	@Override
	public void setValue(int value) {
		setValue(String.valueOf(value));
	}

	@Override
	public void setValue(CharSequence value) {
		setText(value);
		notifyListeners();
	}

	@Override
	public void clearValue() {
		setText("");
		notifyListeners();
	}

	@Override
	public int getValue() {
		return stringToInt(getText().toString());
	}

	@Override
	public String getValueAsString() {
		return getText().toString();
	}

	@Override
	public boolean hasValue() {
		return alwaysHasValue || !TextUtils.isEmpty(getText());
	}

	public void addValueChangedListener(ValueChangedListener li) {
		listeners.add(li);
		li.onListenerAttached(this);
	}

	public void removeValueChangedListener(ValueChangedListener li) {
		listeners.remove(li);
		li.onListenerRemoved(this);
	}

	protected void notifyListeners() {
		for (ValueChangedListener listener : listeners) {
			listener.onValueChanged(this);
		}
	}

	public static int stringToInt(String string) {
		return !TextUtils.isEmpty(string) ? Integer.valueOf(string) : 0;
	}
}
