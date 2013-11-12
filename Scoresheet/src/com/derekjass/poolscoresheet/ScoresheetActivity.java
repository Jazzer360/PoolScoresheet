package com.derekjass.poolscoresheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.derekjass.poolscoresheet.AveragePickerDialog.AveragePickerListener;
import com.derekjass.poolscoresheet.ScoringDialog.ScoringListener;
import com.derekjass.poolscoresheet.views.IntegerView;
import com.derekjass.poolscoresheet.views.PlayerScoreView;
import com.derekjass.poolscoresheet.views.SumView;
import com.derekjass.poolscoresheet.views.SummableInteger;
import com.derekjass.poolscoresheet.views.SummableInteger.OnValueChangedListener;

public class ScoresheetActivity extends Activity
implements AveragePickerListener, ScoringListener {

	private static final int GAME_SET_TAG_KEY = R.id.scoreSetTagKey;
	private static final int PLAYERS = 5;
	private static final int ROUNDS = 3;
	private static final String DATE_KEY = "date_key";
	private static final String HOME_TEAM_KEY = "home_team_key";
	private static final String AWAY_TEAM_KEY = "away_team_key";
	private static final String HOME_PLAYERS_KEY = "home_players_key";
	private static final String AWAY_PLAYERS_KEY = "away_players_key";
	private static final String HOME_AVERAGES_KEY = "home_averages_key";
	private static final String AWAY_AVERAGES_KEY = "away_averages_key";
	private static final String HOME_SCORES_KEY = "home_scores_key";
	private static final String AWAY_SCORES_KEY = "away_scores_key";

	private TextView date;
	private EditText homeTeam;
	private EditText awayTeam;
	private List<EditText> homePlayers;
	private List<EditText> awayPlayers;
	private List<IntegerView> homeAves;
	private List<IntegerView> awayAves;
	private List<PlayerScoreView> homeScores;
	private List<PlayerScoreView> awayScores;
	private List<SumView> homeFinalRound;
	private List<SumView> awayFinalRound;
	private List<SumView> homeSubtotals;
	private List<SumView> awaySubtotals;
	private List<IntegerView> homeRoundAves;
	private List<IntegerView> awayRoundAves;
	private List<SumView> homeTotals;
	private List<SumView> awayTotals;
	private SumView homeAve;
	private SumView awayAve;

	private OnValueChangedListener avgChangedListener =
			new OnValueChangedListener() {
		@Override
		public void onValueChanged(SummableInteger subject) {
			IntegerView homeRoundAve = null;
			IntegerView awayRoundAve = null;

			int homeAvg = Math.max(awayAve.getValue() - homeAve.getValue(), 0);
			int awayAvg = Math.max(homeAve.getValue() - awayAve.getValue(), 0);

			for (int i = 0; i < ROUNDS; i++) {
				homeRoundAve = homeRoundAves.get(i);
				awayRoundAve = awayRoundAves.get(i);
				if (homeAvg > 0) {
					homeRoundAve.setValue(homeAvg);
					awayRoundAve.clearValue();
				} else if (awayAvg > 0) {
					awayRoundAve.setValue(awayAvg);
					homeRoundAve.clearValue();
				} else {
					homeRoundAve.clearValue();
					awayRoundAve.clearValue();
				}
			}
		}

		@Override
		public void onAttachListener(SummableInteger subject) {}
		@Override
		public void onDetachListener(SummableInteger subject) {}
	};
	private OnValueChangedListener totalChangedListener =
			new OnValueChangedListener() {
		@Override
		public void onValueChanged(SummableInteger subject) {
			SumView view1 = (SumView) subject;
			SumView view2 = (SumView) view1.getTag();

			if (!view1.hasValue() || view1.hasSoftValue() ||
					!view2.hasValue() || view2.hasSoftValue()) {
				view1.setCircled(false);
				view2.setCircled(false);
				return;
			} else {
				int total1 = view1.getValue();
				int total2 = view2.getValue();

				if (total1 > total2) {
					view1.setCircled(true);
					view2.setCircled(false);
				} else if (total2 > total1) {
					view2.setCircled(true);
					view1.setCircled(false);
				} else {
					@SuppressWarnings("unchecked")
					Set<SummableInteger> games1 = (Set<SummableInteger>) view1
					.getTag(GAME_SET_TAG_KEY);
					@SuppressWarnings("unchecked")
					Set<SummableInteger> games2 = (Set<SummableInteger>) view2
					.getTag(GAME_SET_TAG_KEY);

					int wins1 = getWinCount(games1);
					int wins2 = getWinCount(games2);

					if (wins1 > wins2) {
						view1.setCircled(true);
						view2.setCircled(false);
					} else {
						view2.setCircled(true);
						view1.setCircled(false);
					}
				}
			}
		}

		@Override
		public void onDetachListener(SummableInteger subject) {}
		@Override
		public void onAttachListener(SummableInteger subject) {}
	};
	private static int getWinCount(Set<SummableInteger> views) {
		int wins = 0;
		for (SummableInteger view : views) {
			if (view.getValue() == 10) {
				wins++;
			}
		}
		return wins;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progressbar);

		View layout = getLayoutInflater()
				.inflate(R.layout.activity_scoresheet, null);

		new AsyncTask<View, Void, View>() {
			@Override
			protected View doInBackground(View... v) {
				getViewReferences(v[0]);
				setupTags();
				setupListeners();
				setDate(new Date());
				return v[0];
			}

			@Override
			protected void onPostExecute(View v) {
				LinearLayout.LayoutParams params =
						new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
				v.setLayoutParams(params);
				((LinearLayout) v).setGravity(Gravity.CENTER);
				setContentView(v);
			}
		}.execute(layout);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(DATE_KEY, date.getText().toString());
		outState.putString(HOME_TEAM_KEY, homeTeam.getText().toString());
		outState.putString(AWAY_TEAM_KEY, awayTeam.getText().toString());

		ArrayList<String> homePlayers = new ArrayList<String>();
		for (EditText view : this.homePlayers) {
			if (!TextUtils.isEmpty(view.getText())) {
				homePlayers.add(view.getText().toString());
			} else {
				homePlayers.add(null);
			}
		}
		outState.putStringArrayList(HOME_PLAYERS_KEY, homePlayers);

		ArrayList<String> awayPlayers = new ArrayList<String>();
		for (EditText view : this.awayPlayers) {
			if (!TextUtils.isEmpty(view.getText())) {
				awayPlayers.add(view.getText().toString());
			} else {
				awayPlayers.add(view.getText().toString());
			}
		}
		outState.putStringArrayList(AWAY_PLAYERS_KEY, awayPlayers);

		ArrayList<Integer> homeAves = new ArrayList<Integer>();
		for (IntegerView view : this.homeAves) {
			if (view.hasValue()) {
				homeAves.add(view.getValue());
			} else {
				homeAves.add(null);
			}
		}
		outState.putIntegerArrayList(HOME_AVERAGES_KEY, homeAves);

		ArrayList<Integer> awayAves = new ArrayList<Integer>();
		for (IntegerView view : this.awayAves) {
			if (view.hasValue()) {
				awayAves.add(view.getValue());
			} else {
				awayAves.add(null);
			}
		}
		outState.putIntegerArrayList(AWAY_AVERAGES_KEY, awayAves);

		ArrayList<Integer> homeScores = new ArrayList<Integer>();
		for (PlayerScoreView view : this.homeScores) {
			if (view.hasValue()) {
				homeScores.add(view.getValue());
			} else {
				homeScores.add(null);
			}
		}
		outState.putIntegerArrayList(HOME_SCORES_KEY, homeScores);

		ArrayList<Integer> awayScores = new ArrayList<Integer>();
		for (PlayerScoreView view : this.awayScores) {
			if (view.hasValue()) {
				awayScores.add(view.getValue());
			} else {
				awayScores.add(null);
			}
		}
		outState.putIntegerArrayList(AWAY_SCORES_KEY, awayScores);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		new AsyncTask<Bundle, Void, Void>() {
			@Override
			protected Void doInBackground(Bundle... bundle) {
				String dateString = bundle[0].getString(DATE_KEY);
				if (dateString != null) {
					date.setText(dateString);
				}

				String homeTeamString = bundle[0].getString(HOME_TEAM_KEY);
				if (homeTeamString != null) {
					homeTeam.setText(homeTeamString);
				}

				String awayTeamString = bundle[0].getString(AWAY_TEAM_KEY);
				if (awayTeamString != null) {
					awayTeam.setText(awayTeamString);
				}

				ArrayList<String> hPlayers =
						bundle[0].getStringArrayList(HOME_PLAYERS_KEY);
				if (hPlayers != null) {
					for (int i = 0; i < hPlayers.size(); i++) {
						String name = hPlayers.get(i);
						if (name != null) {
							homePlayers.get(i).setText(name);
						}
					}
				}

				ArrayList<String> aPlayers =
						bundle[0].getStringArrayList(AWAY_PLAYERS_KEY);
				if (aPlayers != null) {
					for (int i = 0; i < aPlayers.size(); i++) {
						String name = aPlayers.get(i);
						if (name != null) {
							awayPlayers.get(i).setText(name);
						}
					}
				}

				ArrayList<Integer> hAves =
						bundle[0].getIntegerArrayList(HOME_AVERAGES_KEY);
				if (hAves != null) {
					for (int i = 0; i < hAves.size(); i++) {
						Integer ave = hAves.get(i);
						if (ave != null) {
							homeAves.get(i).setValue(ave);
						}
					}
				}

				ArrayList<Integer> aAves =
						bundle[0].getIntegerArrayList(AWAY_AVERAGES_KEY);
				if (aAves != null) {
					for (int i = 0; i < aAves.size(); i++) {
						Integer ave = aAves.get(i);
						if (ave != null) {
							awayAves.get(i).setValue(ave);
						}
					}
				}

				ArrayList<Integer> hScores =
						bundle[0].getIntegerArrayList(HOME_SCORES_KEY);
				if (hScores != null) {
					for (int i = 0; i < hScores.size(); i++) {
						Integer score = hScores.get(i);
						if (score != null) {
							homeScores.get(i).setValue(score);
						}
					}
				}

				ArrayList<Integer> aScores =
						bundle[0].getIntegerArrayList(AWAY_SCORES_KEY);
				if (aScores != null) {
					for (int i = 0; i < aScores.size(); i++) {
						Integer score = aScores.get(i);
						if (score != null) {
							awayScores.get(i).setValue(score);
						}
					}
				}
				return null;
			}
		}.execute(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Are you sure?")
		.setMessage("Continuing will clear the scoreboxes.")
		.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ScoresheetActivity.super.onBackPressed();
			}
		})
		.setNegativeButton("No", null)
		.show();
	}

	public void onScoreBoxClicked(View v) {
		boolean homeClicked = findViewById(R.id.homeTeamViews)
				.findViewById(v.getId()) != null;

		PlayerScoreView homeView =
				(PlayerScoreView) (homeClicked ? v : v.getTag());
		PlayerScoreView awayView =
				(PlayerScoreView) (homeClicked ? v.getTag() : v);
		String homeScore = homeView.getValueAsString();
		String awayScore = awayView.getValueAsString();

		TextView homePlayer = (TextView) ((ViewGroup) homeView.getParent())
				.getChildAt(1);
		TextView awayPlayer = (TextView) ((ViewGroup) awayView.getParent())
				.getChildAt(1);
		String homeName = homePlayer.getText().toString();
		String awayName = awayPlayer.getText().toString();

		Bundle args = new Bundle();
		if (!TextUtils.isEmpty(homeName)) {
			args.putString(ScoringDialog.HOME_PLAYER_KEY, homeName);
		} else {
			args.putString(ScoringDialog.HOME_PLAYER_KEY,
					homePlayer.getHint().toString());
		}
		if (!TextUtils.isEmpty(awayName)) {
			args.putString(ScoringDialog.AWAY_PLAYER_KEY, awayName);
		} else {
			args.putString(ScoringDialog.AWAY_PLAYER_KEY,
					awayPlayer.getHint().toString());
		}
		if (!TextUtils.isEmpty(homeScore)) {
			args.putString(ScoringDialog.HOME_SCORE_KEY, homeScore);
		}
		if (!TextUtils.isEmpty(awayScore)) {
			args.putString(ScoringDialog.AWAY_SCORE_KEY, awayScore);
		}
		args.putInt(ScoringDialog.HOME_VIEW_ID_KEY, homeView.getId());
		args.putInt(ScoringDialog.AWAY_VIEW_ID_KEY, awayView.getId());

		args.putBoolean(ScoringDialog.ERO_KEY,
				homeView.isEro() || awayView.isEro());

		ScoringDialog dialog = new ScoringDialog();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "ScoringDialog");
	}

	@Override
	public void onScorePicked(int winViewId, CharSequence winScore,
			int lossViewId, CharSequence lossScore, boolean ero) {
		PlayerScoreView winner = (PlayerScoreView) findViewById(winViewId);
		PlayerScoreView loser = (PlayerScoreView) findViewById(lossViewId);

		winner.setValue(winScore);
		winner.setEro(ero);
		loser.setValue(lossScore);
		loser.setEro(false);
	}



	@Override
	public void onScoreCleared(int viewId1, int viewId2) {
		PlayerScoreView view1 = (PlayerScoreView) findViewById(viewId1);
		PlayerScoreView view2 = (PlayerScoreView) findViewById(viewId2);

		view1.clearValue();
		view1.setEro(false);
		view2.clearValue();
		view2.setEro(false);
	}

	public void onAverageBoxClicked(View v) {
		SummableInteger avgView = (SummableInteger) v;
		TextView nameView = (TextView) ((ViewGroup) v.getParent())
				.getChildAt(1);
		String avg = avgView.getValueAsString();
		String name = nameView.getText().toString();

		Bundle args = new Bundle();
		args.putInt(AveragePickerDialog.VIEW_ID_KEY, v.getId());
		if (!TextUtils.isEmpty(name)) {
			args.putString(AveragePickerDialog.NAME_KEY, name);
		} else {
			args.putString(AveragePickerDialog.NAME_KEY,
					nameView.getHint().toString());
		}
		if (!TextUtils.isEmpty(avg)) {
			args.putString(AveragePickerDialog.AVG_KEY, avg);
		}

		AveragePickerDialog dialog = new AveragePickerDialog();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "AverageDialog");
	}

	@Override
	public void onAveragePicked(int viewId, CharSequence avg) {
		IntegerView view = (IntegerView) findViewById(viewId);
		view.setValue(avg);
	}

	@Override
	public void onAverageCleared(int viewId) {
		IntegerView view = (IntegerView) findViewById(viewId);
		view.clearValue();
	}

	private void setDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("M-d-yyyy", Locale.US);
		this.date.setText(sdf.format(date));
	}

	private void initLists() {
		homePlayers = new ArrayList<EditText>(PLAYERS);
		awayPlayers = new ArrayList<EditText>(PLAYERS);
		homeAves = new ArrayList<IntegerView>(PLAYERS);
		awayAves = new ArrayList<IntegerView>(PLAYERS);
		homeScores = new ArrayList<PlayerScoreView>(PLAYERS * ROUNDS);
		awayScores = new ArrayList<PlayerScoreView>(PLAYERS * ROUNDS);
		homeFinalRound = new ArrayList<SumView>(PLAYERS + 3);
		awayFinalRound = new ArrayList<SumView>(PLAYERS + 3);
		homeSubtotals = new ArrayList<SumView>(ROUNDS);
		awaySubtotals = new ArrayList<SumView>(ROUNDS);
		homeRoundAves = new ArrayList<IntegerView>(ROUNDS);
		awayRoundAves = new ArrayList<IntegerView>(ROUNDS);
		homeTotals = new ArrayList<SumView>(ROUNDS);
		awayTotals = new ArrayList<SumView>(ROUNDS);
	}

	private void setupListeners() {
		for (int i = 0; i < PLAYERS; i++) {
			addViewToSum(homeAve, homeAves.get(i));
			addViewToSum(awayAve, awayAves.get(i));
		}

		for (int i = 0; i < PLAYERS * ROUNDS; i++) {
			PlayerScoreView homeScore = homeScores.get(i);
			PlayerScoreView awayScore = awayScores.get(i);

			int round = i / PLAYERS;
			int game = i % PLAYERS;

			addViewToSum(homeSubtotals.get(round), homeScore);
			addViewToSum(homeFinalRound.get(game), homeScore);
			addViewToSum(awaySubtotals.get(round), awayScore);
			addViewToSum(awayFinalRound.get(game), awayScore);
		}

		final int r4Subtotal = PLAYERS + 0;
		final int r4AveTotal = PLAYERS + 1;
		final int r4Total = PLAYERS + 2;

		for (int i = 0; i < ROUNDS; i++) {
			addViewToSum(homeTotals.get(i), homeSubtotals.get(i));
			addViewToSum(homeTotals.get(i), homeRoundAves.get(i));
			addViewToSum(homeFinalRound.get(r4Subtotal), homeSubtotals.get(i));
			addViewToSum(homeFinalRound.get(r4AveTotal), homeRoundAves.get(i));
			addViewToSum(homeFinalRound.get(r4Total), homeTotals.get(i));

			addViewToSum(awayTotals.get(i), awaySubtotals.get(i));
			addViewToSum(awayTotals.get(i), awayRoundAves.get(i));
			addViewToSum(awayFinalRound.get(r4Subtotal), awaySubtotals.get(i));
			addViewToSum(awayFinalRound.get(r4AveTotal), awayRoundAves.get(i));
			addViewToSum(awayFinalRound.get(r4Total), awayTotals.get(i));

			homeTotals.get(i).addOnValueChangedListener(totalChangedListener);
			awayTotals.get(i).addOnValueChangedListener(totalChangedListener);
		}

		homeFinalRound.get(r4Total).addOnValueChangedListener(
				totalChangedListener);
		awayFinalRound.get(r4Total).addOnValueChangedListener(
				totalChangedListener);

		homeAve.addOnValueChangedListener(avgChangedListener);
		awayAve.addOnValueChangedListener(avgChangedListener);
	}

	private static void addViewToSum(SumView sum, SummableInteger value) {
		value.addOnValueChangedListener(sum);
	}

	@SuppressWarnings("unchecked")
	private void setupTags() {
		SumView homeR4 = homeFinalRound.get(PLAYERS + 2);
		SumView awayR4 = awayFinalRound.get(PLAYERS + 2);
		homeR4.setTag(awayR4);
		awayR4.setTag(homeR4);
		homeR4.setTag(GAME_SET_TAG_KEY, new HashSet<SummableInteger>());
		awayR4.setTag(GAME_SET_TAG_KEY, new HashSet<SummableInteger>());

		for (int i = 0; i < ROUNDS; i++) {
			SumView homeTotal = homeTotals.get(i);
			SumView awayTotal = awayTotals.get(i);

			homeTotal.setTag(awayTotal);
			awayTotal.setTag(homeTotal);
			homeTotal.setTag(GAME_SET_TAG_KEY, new HashSet<SummableInteger>());
			awayTotal.setTag(GAME_SET_TAG_KEY, new HashSet<SummableInteger>());
		}

		for (PlayerScoreView home : homeScores) {
			PlayerScoreView away = null;
			for (PlayerScoreView view : awayScores) {
				if (home.getRound() == view.getRound()
						&& home.getGame() == view.getGame()) {
					away = view;
					linkViewsByTags(home, view);
				}
			}
			SumView homeRoundTotal = homeTotals.get(home.getRound() - 1);
			SumView awayRoundTotal = awayTotals.get(away.getRound() - 1);

			((Set<SummableInteger>) homeRoundTotal.getTag(
					GAME_SET_TAG_KEY)).add(home);
			((Set<SummableInteger>) awayRoundTotal.getTag(
					GAME_SET_TAG_KEY)).add(away);
			((Set<SummableInteger>) homeR4.getTag(GAME_SET_TAG_KEY)).add(home);
			((Set<SummableInteger>) awayR4.getTag(GAME_SET_TAG_KEY)).add(away);
		}
	}

	private static void linkViewsByTags(View view1, View view2) {
		view1.setTag(view2);
		view2.setTag(view1);
	}

	private void getViewReferences(View v) {
		initLists();

		date = (TextView) v.findViewById(R.id.dateView);

		homeTeam = (EditText) v.findViewById(R.id.homeTeamName);
		awayTeam = (EditText) v.findViewById(R.id.awayTeamName);

		homePlayers.add((EditText) v.findViewById(R.id.homeP1Name));
		homePlayers.add((EditText) v.findViewById(R.id.homeP2Name));
		homePlayers.add((EditText) v.findViewById(R.id.homeP3Name));
		homePlayers.add((EditText) v.findViewById(R.id.homeP4Name));
		homePlayers.add((EditText) v.findViewById(R.id.homeP5Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayP1Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayP2Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayP3Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayP4Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayP5Name));

		homeAves.add((IntegerView) v.findViewById(R.id.homeP1Ave));
		homeAves.add((IntegerView) v.findViewById(R.id.homeP2Ave));
		homeAves.add((IntegerView) v.findViewById(R.id.homeP3Ave));
		homeAves.add((IntegerView) v.findViewById(R.id.homeP4Ave));
		homeAves.add((IntegerView) v.findViewById(R.id.homeP5Ave));
		awayAves.add((IntegerView) v.findViewById(R.id.awayP1Ave));
		awayAves.add((IntegerView) v.findViewById(R.id.awayP2Ave));
		awayAves.add((IntegerView) v.findViewById(R.id.awayP3Ave));
		awayAves.add((IntegerView) v.findViewById(R.id.awayP4Ave));
		awayAves.add((IntegerView) v.findViewById(R.id.awayP5Ave));

		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP1R1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP2R1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP3R1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP4R1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP5R1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP1R2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP2R2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP3R2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP4R2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP5R2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP1R3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP2R3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP3R3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP4R3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homeP5R3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP1R1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP2R1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP3R1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP4R1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP5R1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP1R2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP2R2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP3R2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP4R2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP5R2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP1R3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP2R3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP3R3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP4R3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayP5R3));

		homeFinalRound.add((SumView) v.findViewById(R.id.homeP1Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeP2Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeP3Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeP4Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeP5Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeSubtotalR4));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeAveR4));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeTotalR4));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayP1Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayP2Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayP3Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayP4Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayP5Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awaySubtotalR4));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayAveR4));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayTotalR4));

		homeSubtotals.add((SumView) v.findViewById(R.id.homeSubtotalR1));
		homeSubtotals.add((SumView) v.findViewById(R.id.homeSubtotalR2));
		homeSubtotals.add((SumView) v.findViewById(R.id.homeSubtotalR3));
		awaySubtotals.add((SumView) v.findViewById(R.id.awaySubtotalR1));
		awaySubtotals.add((SumView) v.findViewById(R.id.awaySubtotalR2));
		awaySubtotals.add((SumView) v.findViewById(R.id.awaySubtotalR3));

		homeRoundAves.add((IntegerView) v.findViewById(R.id.homeAveR1));
		homeRoundAves.add((IntegerView) v.findViewById(R.id.homeAveR2));
		homeRoundAves.add((IntegerView) v.findViewById(R.id.homeAveR3));
		awayRoundAves.add((IntegerView) v.findViewById(R.id.awayAveR1));
		awayRoundAves.add((IntegerView) v.findViewById(R.id.awayAveR2));
		awayRoundAves.add((IntegerView) v.findViewById(R.id.awayAveR3));

		homeTotals.add((SumView) v.findViewById(R.id.homeTotalR1));
		homeTotals.add((SumView) v.findViewById(R.id.homeTotalR2));
		homeTotals.add((SumView) v.findViewById(R.id.homeTotalR3));
		awayTotals.add((SumView) v.findViewById(R.id.awayTotalR1));
		awayTotals.add((SumView) v.findViewById(R.id.awayTotalR2));
		awayTotals.add((SumView) v.findViewById(R.id.awayTotalR3));

		homeAve = (SumView) v.findViewById(R.id.homeAve);
		awayAve = (SumView) v.findViewById(R.id.awayAve);
	}
}
