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
import android.widget.CheckBox;
import android.widget.RadioButton;

public class ScoringDialog extends DialogFragment {

	public interface ScoringListener {
		public void onScorePicked(int winViewId, CharSequence winScore,
				int lossViewId, CharSequence lossScore, boolean ero);
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
	private Set<RadioButton> mScoreButtons = new HashSet<RadioButton>();
	private Set<RadioButton> mPlayerButtons = new HashSet<RadioButton>();
	private View.OnClickListener mScoreButtonListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (RadioButton button : mScoreButtons) {
				button.setChecked(false);
			}
			((RadioButton) v).setChecked(true);
		}
	};
	private View.OnClickListener mPlayerButtonListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (RadioButton button : mPlayerButtons) {
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_scoring, null);
		setupView(v);


		builder.setTitle(R.string.select_winner)
		.setView(v)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RadioButton winner = null;
				boolean winnerSelected = false;
				for (RadioButton button : mPlayerButtons) {
					if (button.isChecked()) {
						winnerSelected = true;
						winner = button;
						break;
					}
				}

				boolean scoreSelected = false;
				CharSequence score = "";
				for (RadioButton button : mScoreButtons) {
					if (button.isChecked()) {
						scoreSelected = true;
						score = button.getText();
						break;
					}
				}

				if (!(winnerSelected && scoreSelected)) {
					mListener.onScoreCleared(mHomeViewId, mAwayViewId);
					return;
				}

				boolean homeWins =
						(winner.getId() == R.id.homeRadio) ? true : false;

				if (homeWins) {
					mListener.onScorePicked(
							mHomeViewId, getActivity().getText(R.string.n10),
							mAwayViewId, score,
							mEroBox.isChecked());
				} else {
					mListener.onScorePicked(
							mAwayViewId, getActivity().getText(R.string.n10),
							mHomeViewId, score,
							mEroBox.isChecked());
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

		mHomeViewId = getArguments().getInt(HOME_VIEW_ID_KEY);
		mAwayViewId = getArguments().getInt(AWAY_VIEW_ID_KEY);

		return builder.create();
	}

	private void setupView(View v) {
		String score = null;

		RadioButton homePlayer = (RadioButton) v.findViewById(R.id.homeRadio);
		homePlayer.setText(getArguments().getString(HOME_PLAYER_KEY));
		homePlayer.setOnClickListener(mPlayerButtonListener);
		String homeScore = getArguments().getString(HOME_SCORE_KEY);
		if (homeScore != null &&
				homeScore.equals(getActivity().getString(R.string.n10))) {
			homePlayer.setChecked(true);
		} else {
			score = homeScore;
		}
		mPlayerButtons.add(homePlayer);

		RadioButton awayPlayer = (RadioButton) v.findViewById(R.id.awayRadio);
		awayPlayer.setText(getArguments().getString(AWAY_PLAYER_KEY));
		awayPlayer.setOnClickListener(mPlayerButtonListener);
		String awayScore = getArguments().getString(AWAY_SCORE_KEY);
		if (awayScore != null &&
				awayScore.equals(getActivity().getString(R.string.n10))) {
			awayPlayer.setChecked(true);
		} else {
			score = awayScore;
		}
		mPlayerButtons.add(awayPlayer);

		ViewGroup buttonLayout =
				(ViewGroup) v.findViewById(R.id.dialog_buttons);

		for (int i = 0; i < buttonLayout.getChildCount(); i++) {
			RadioButton button = (RadioButton) buttonLayout.getChildAt(i);
			button.setOnClickListener(mScoreButtonListener);
			if (button.getText().equals(score)) {
				button.setChecked(true);
			}
			mScoreButtons.add(button);
		}

		mEroBox = (CheckBox) v.findViewById(R.id.eroCheckbox);
		mEroBox.setChecked(getArguments().getBoolean(ERO_KEY));
	}
}
