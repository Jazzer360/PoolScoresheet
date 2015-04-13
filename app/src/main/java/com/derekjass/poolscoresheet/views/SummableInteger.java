package com.derekjass.poolscoresheet.views;

public interface SummableInteger {
	public interface OnValueChangedListener {
		public void onValueChanged(SummableInteger subject);
		public void onAttachListener(SummableInteger subject);
		public void onDetachListener(SummableInteger subject);
	}
	public void setValue(int value);
	public void setValue(CharSequence value);
	public void clearValue();
	public int getValue();
	public String getValueAsString();
	public boolean hasValue();
	public boolean hasSoftValue();
	public boolean mustSum();
	public void addOnValueChangedListener(OnValueChangedListener li);
	public void removeOnValueChangedListener(OnValueChangedListener li);
}
