package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;

public class ScoringDialog extends DialogFragment {

	public interface ScoringListener {
		public void onScorePicked(int winViewId, int winScore,
				int lossViewId, int lossScore, boolean ero);
		public void onScoreCleared(int viewId1, int viewId2);
	}

	public static final String HOME_PLAYER_KEY = "home_player";
	public static final String AWAY_PLAYER_KEY = "away_player";
	public static final String HOME_VIEW_ID_KEY = "home_view_id";
	public static final String AWAY_VIEW_ID_KEY = "away_view_id";
	public static final String HOME_SCORE_KEY = "home_score";
	public static final String AWAY_SCORE_KEY = "away_score";
	public static final String ERO_KEY = "ero";

	private int mHomeViewId;
	private int mAwayViewId;
	private ScoringListener mListener;

	private CheckBox mEroBox;
	private NumberPicker mScorePicker;
	private RadioButton mHomePlayer;
	private RadioButton mAwayPlayer;
	private View.OnClickListener mPlayerButtonListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			((CompoundButton) v).setChecked(true);
			((CompoundButton) v.getTag()).setChecked(false);
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (getTargetFragment() == null) {
			try {
				mListener = (ScoringListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString() +
						" must implement ScoringListener or" +
						" dialog must setTargetFragment()");
			}
		} else {
			try {
				mListener = (ScoringListener) getTargetFragment();
			} catch (ClassCastException e) {
				throw new ClassCastException(getTargetFragment().toString() +
						" must implement ScoringListener");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_scoring, null);
		setupView(v);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_winner)
		.setView(v)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mHomePlayer.isChecked()) {
					mListener.onScorePicked(mHomeViewId, 10,
							mAwayViewId, mScorePicker.getValue(),
							mEroBox.isChecked());
				} else if (mAwayPlayer.isChecked()) {
					mListener.onScorePicked(mAwayViewId, 10,
							mHomeViewId, mScorePicker.getValue(),
							mEroBox.isChecked());
				} else {
					mListener.onScoreCleared(mHomeViewId, mAwayViewId);
				}
			}
		})
		.setNeutralButton(R.string.clear,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onScoreCleared(mHomeViewId, mAwayViewId);
			}
		})
		.setNegativeButton(android.R.string.cancel, null);


		return builder.create();
	}

	private void setupView(View v) {
		mHomeViewId = getArguments().getInt(HOME_VIEW_ID_KEY);
		mAwayViewId = getArguments().getInt(AWAY_VIEW_ID_KEY);

		int homeScore = getArguments().getInt(HOME_SCORE_KEY, -1);
		int awayScore = getArguments().getInt(AWAY_SCORE_KEY, -1);

		mScorePicker = (NumberPicker) v.findViewById(R.id.scorePicker);
		mEroBox = (CheckBox) v.findViewById(R.id.eroCheckbox);
		mHomePlayer = (RadioButton) v.findViewById(R.id.homeRadio);
		mAwayPlayer = (RadioButton) v.findViewById(R.id.awayRadio);

		mScorePicker.setMaxValue(7);
		mScorePicker.setWrapSelectorWheel(false);

		mHomePlayer.setText(getArguments().getString(HOME_PLAYER_KEY));
		mAwayPlayer.setText(getArguments().getString(AWAY_PLAYER_KEY));
		mHomePlayer.setOnClickListener(mPlayerButtonListener);
		mAwayPlayer.setOnClickListener(mPlayerButtonListener);
		linkViewsByTag(mHomePlayer, mAwayPlayer);

		if (homeScore == 10) {
			mHomePlayer.setChecked(true);
			mAwayPlayer.setChecked(false);
			mScorePicker.setValue(awayScore);
		} else if (awayScore == 10) {
			mAwayPlayer.setChecked(true);
			mHomePlayer.setChecked(false);
			mScorePicker.setValue(homeScore);
		} else {
			mHomePlayer.setChecked(false);
			mAwayPlayer.setChecked(false);
			mScorePicker.setValue(7);
		}

		mEroBox.setChecked(getArguments().getBoolean(ERO_KEY));
	}

	private static void linkViewsByTag(View v1, View v2) {
		v1.setTag(v2);
		v2.setTag(v1);
	}
}
