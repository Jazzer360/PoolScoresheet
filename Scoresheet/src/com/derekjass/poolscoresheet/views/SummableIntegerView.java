package com.derekjass.poolscoresheet.views;

public interface SummableIntegerView {
	public interface OnValueChangedListener {
		public void onValueChanged(SummableIntegerView subject);
		public void onAttachListener(SummableIntegerView subject);
		public void onDetachListener(SummableIntegerView subject);
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
