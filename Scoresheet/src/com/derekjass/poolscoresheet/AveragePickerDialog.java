package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

public class AveragePickerDialog extends DialogFragment {

	public interface AveragePickerListener {
		public void onAveragePicked(int viewId, int avg);
		public void onAverageCleared(int viewId);
	}

	public static final String NAME_KEY = "name";
	public static final String VIEW_ID_KEY = "view_id";
	public static final String AVG_KEY = "avg";

	private int mViewId;
	private AveragePickerListener mListener;

	private NumberPicker mAvePicker;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (getTargetFragment() == null) {
			try {
				mListener = (AveragePickerListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString() +
						" must implement AveragePickerListener or" +
						" dialog must setTargetFragment()");
			}
		} else {
			try {
				mListener = (AveragePickerListener) getTargetFragment();
			} catch (ClassCastException e) {
				throw new ClassCastException(getTargetFragment().toString() +
						" must implement AveragePickerListener");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mViewId = getArguments().getInt(VIEW_ID_KEY);

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_average,
				(ViewGroup) getView(), false);

		mAvePicker = (NumberPicker) v.findViewById(R.id.avePicker);
		mAvePicker.setMaxValue(10);
		mAvePicker.setWrapSelectorWheel(false);
		mAvePicker.setValue(getArguments().getInt(AVG_KEY, 7));

		TextView name = (TextView) v.findViewById(R.id.dialogName);
		name.setText(getArguments().getString(NAME_KEY));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.assign_average)
		.setView(v)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onAveragePicked(mViewId, mAvePicker.getValue());
			}
		})
		.setNeutralButton(R.string.clear,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onAverageCleared(mViewId);
			}
		})
		.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}
}
