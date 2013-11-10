package com.derekjass.poolscoresheet.views;

public interface IntegerView {
	public interface ValueChangedListener {
		public void onValueChanged(IntegerView subject);
		public void onAttachListener(IntegerView subject);
		public void onDetachListener(IntegerView subject);
	}
	public void setValue(int value);
	public void setValue(CharSequence value);
	public void clearValue();
	public int getValue();
	public String getValueAsString();
	public boolean hasValue();
	public boolean hasSoftValue();
	public boolean mustSum();
}
