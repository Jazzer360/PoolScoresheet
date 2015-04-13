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

	private int mValue;
	private boolean mHasValue;
	private Set<OnValueChangedListener> mListeners;

	private final boolean mMustSum;

	public IntegerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.IntegerView,
				0, 0);

		try {
			mMustSum = a.getBoolean(R.styleable.IntegerView_mustSum, true);
		} finally {
			a.recycle();
		}

		mValue = 0;
		mHasValue = false;
		mListeners = new HashSet<OnValueChangedListener>();
	}

	@Override
	public void setValue(int value) {
		this.mValue = value;
		mHasValue = true;
		setText(String.valueOf(value));
		notifyListeners();
	}

	@Override
	public void setValue(CharSequence value) {
		this.mValue = stringToInt(value.toString());
		mHasValue = true;
		setText(value);
		notifyListeners();
	}

	@Override
	public void clearValue() {
		mValue = 0;
		mHasValue = false;
		setText("");
		notifyListeners();
	}

	@Override
	public int getValue() {
		return mValue;
	}

	@Override
	public String getValueAsString() {
		return getText().toString();
	}

	@Override
	public boolean hasValue() {
		return mHasValue;
	}

	@Override
	public boolean hasSoftValue() {
		return false;
	}

	@Override
	public boolean mustSum() {
		return mMustSum;
	}

	@Override
	public void addOnValueChangedListener(OnValueChangedListener li) {
		mListeners.add(li);
		li.onAttachListener(this);
	}

	@Override
	public void removeOnValueChangedListener(OnValueChangedListener li) {
		mListeners.remove(li);
		li.onAttachListener(this);
	}

	private void notifyListeners() {
		for (OnValueChangedListener listener : mListeners) {
			listener.onValueChanged(this);
		}
	}

	private static int stringToInt(String string) {
		return !TextUtils.isEmpty(string) ? Integer.valueOf(string) : 0;
	}
}
