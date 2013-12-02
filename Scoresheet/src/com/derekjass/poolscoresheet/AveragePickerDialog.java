package com.derekjass.poolscoresheet;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

public class AveragePickerDialog extends DialogFragment {

	public interface AveragePickerListener {
		public void onAveragePicked(int viewId, CharSequence avg);
		public void onAverageCleared(int viewId);
	}

	public static final String NAME_KEY = "name";
	public static final String VIEW_ID_KEY = "view_id";
	public static final String AVG_KEY = "avg";

	private int mViewClickedId;
	private AveragePickerListener mListener;

	private Set<RadioButton> mButtons = new HashSet<RadioButton>();
	private View.OnClickListener mButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (RadioButton button : mButtons) {
				button.setChecked(false);
			}
			((RadioButton) v).setChecked(true);
		}
	};

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
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_average, null);
		TextView name = (TextView) v.findViewById(R.id.dialog_name);
		name.setText(getArguments().getString(NAME_KEY));

		setupButtons((ViewGroup) v);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.assign_average)
		.setView(v)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (RadioButton button : mButtons) {
					if (button.isChecked()) {
						mListener.onAveragePicked(mViewClickedId,
								button.getText());
						return;
					}
					mListener.onAveragePicked(mViewClickedId, "");
				}
			}
		})
		.setNeutralButton(R.string.clear,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onAverageCleared(mViewClickedId);
			}
		})
		.setNegativeButton(android.R.string.cancel, null);

		mViewClickedId = getArguments().getInt(VIEW_ID_KEY);

		return builder.create();
	}

	private void setupButtons(ViewGroup v) {
		for (int i = 0; i < v.getChildCount(); i++) {
			View childView = v.getChildAt(i);
			if (childView instanceof ViewGroup) {
				setupButtons((ViewGroup) childView);
			} else if (childView instanceof RadioButton) {
				RadioButton button = (RadioButton) childView;
				button.setOnClickListener(mButtonListener);
				mButtons.add(button);

				if (button.getText().equals(getArguments().getString(AVG_KEY)))
					button.setChecked(true);
			}
		}
	}
}
