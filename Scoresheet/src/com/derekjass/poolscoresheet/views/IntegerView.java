package com.derekjass.poolscoresheet.views;

public interface IntegerView {
	public interface ValueChangedListener {
		public void onValueChanged(IntegerView view);
		public void onListenerAttached(IntegerView subject);
		public void onListenerRemoved(IntegerView subject);
	}
	public void setValue(int value);
	public void setValue(CharSequence value);
	public void clearValue();
	public int getValue();
	public String getValueAsString();
	public boolean hasValue();
}
