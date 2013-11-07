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
	}

	static final String HOME_PLAYER_KEY = "home_player";
	static final String AWAY_PLAYER_KEY = "away_player";
	static final String HOME_VIEW_ID_KEY = "home_view_id";
	static final String AWAY_VIEW_ID_KEY = "away_view_id";
	static final String HOME_SCORE_KEY = "home_score";
	static final String AWAY_SCORE_KEY = "away_score";
	static final String ERO_KEY = "ero";

	private int homeViewId;
	private int awayViewId;
	private ScoringListener hostActivity;

	private CheckBox eroBox;
	private Set<RadioButton> scoreButtons = new HashSet<RadioButton>();
	private Set<RadioButton> playerButtons = new HashSet<RadioButton>();
	private View.OnClickListener scoreButtonListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (RadioButton button : scoreButtons) {
				button.setChecked(false);
			}
			((RadioButton) v).setChecked(true);
		}
	};
	private View.OnClickListener playerButtonListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (RadioButton button : playerButtons) {
				button.setChecked(false);
			}
			((RadioButton) v).setChecked(true);
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			hostActivity = (ScoringListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement ScoringListener");
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
				for (RadioButton button : playerButtons) {
					if (button.isChecked()) {
						winnerSelected = true;
						winner = button;
						break;
					}
				}

				boolean scoreSelected = false;
				CharSequence score = "";
				for (RadioButton button : scoreButtons) {
					if (button.isChecked()) {
						scoreSelected = true;
						score = button.getText();
						break;
					}
				}

				if (!(winnerSelected && scoreSelected)) {
					hostActivity.onScorePicked(
							homeViewId, "", awayViewId, "", false);
					return;
				}

				boolean homeWins =
						(winner.getId() == R.id.homeRadio) ? true : false;

				if (homeWins) {
					hostActivity.onScorePicked(
							homeViewId, getActivity().getText(R.string.n10),
							awayViewId, score,
							eroBox.isChecked());
				} else {
					hostActivity.onScorePicked(
							awayViewId, getActivity().getText(R.string.n10),
							homeViewId, score,
							eroBox.isChecked());
				}
			}
		})
		.setNeutralButton(R.string.clear,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				hostActivity.onScorePicked(
						homeViewId, "", awayViewId, "", false);
			}
		})
		.setNegativeButton(android.R.string.cancel, null);

		homeViewId = getArguments().getInt(HOME_VIEW_ID_KEY);
		awayViewId = getArguments().getInt(AWAY_VIEW_ID_KEY);

		return builder.create();
	}

	private void setupView(View v) {
		String score = null;

		RadioButton homePlayer = (RadioButton) v.findViewById(R.id.homeRadio);
		homePlayer.setText(getArguments().getString(HOME_PLAYER_KEY));
		homePlayer.setOnClickListener(playerButtonListener);
		String homeScore = getArguments().getString(HOME_SCORE_KEY);
		if (homeScore != null &&
				homeScore.equals(getActivity().getString(R.string.n10))) {
			homePlayer.setChecked(true);
		} else {
			score = homeScore;
		}
		playerButtons.add(homePlayer);

		RadioButton awayPlayer = (RadioButton) v.findViewById(R.id.awayRadio);
		awayPlayer.setText(getArguments().getString(AWAY_PLAYER_KEY));
		awayPlayer.setOnClickListener(playerButtonListener);
		String awayScore = getArguments().getString(AWAY_SCORE_KEY);
		if (awayScore != null &&
				awayScore.equals(getActivity().getString(R.string.n10))) {
			awayPlayer.setChecked(true);
		} else {
			score = awayScore;
		}
		playerButtons.add(awayPlayer);

		ViewGroup buttonLayout =
				(ViewGroup) v.findViewById(R.id.dialog_buttons);

		for (int i = 0; i < buttonLayout.getChildCount(); i++) {
			RadioButton button = (RadioButton) buttonLayout.getChildAt(i);
			button.setOnClickListener(scoreButtonListener);
			if (button.getText().equals(score)) {
				button.setChecked(true);
			}
			scoreButtons.add(button);
		}

		eroBox = (CheckBox) v.findViewById(R.id.eroCheckbox);
		eroBox.setChecked(getArguments().getBoolean(ERO_KEY));
	}

}
