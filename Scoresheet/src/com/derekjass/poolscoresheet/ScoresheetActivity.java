package com.derekjass.poolscoresheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.derekjass.poolscoresheet.views.BasicIntegerView;
import com.derekjass.poolscoresheet.views.SummableIntegerView;
import com.derekjass.poolscoresheet.views.SummableIntegerView.OnValueChangedListener;
import com.derekjass.poolscoresheet.views.PlayerScoreView;
import com.derekjass.poolscoresheet.views.SumView;

public class ScoresheetActivity extends Activity
implements AveragePickerListener, ScoringListener {

	private TextView date;
	private List<EditText> homePlayers;
	private List<EditText> awayPlayers;
	private List<BasicIntegerView> homeAves;
	private List<BasicIntegerView> awayAves;
	private List<PlayerScoreView> homeScores;
	private List<PlayerScoreView> awayScores;
	private List<SumView> homeFinalRound;
	private List<SumView> awayFinalRound;
	private List<SumView> homeSubtotals;
	private List<SumView> awaySubtotals;
	private List<BasicIntegerView> homeRoundAves;
	private List<BasicIntegerView> awayRoundAves;
	private List<SumView> homeTotals;
	private List<SumView> awayTotals;
	private SumView homeAve;
	private SumView awayAve;

	private OnValueChangedListener avgChangedListener =
			new OnValueChangedListener() {
		@Override
		public void onValueChanged(SummableIntegerView view) {
			BasicIntegerView homeRoundAve = null;
			BasicIntegerView awayRoundAve = null;

			int homeAvg = Math.max(awayAve.getValue() - homeAve.getValue(), 0);
			int awayAvg = Math.max(homeAve.getValue() - awayAve.getValue(), 0);

			for (int i = 0; i < homeRoundAves.size(); i++) {
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
		public void onAttachListener(SummableIntegerView subject) {}
		@Override
		public void onDetachListener(SummableIntegerView subject) {}
	};

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
				setupListeners();
				setupLinks();
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
	public void onBackPressed() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Closing Activity")
		.setMessage("Are you sure you want to close this activity? " +
				"Doing so will clear the score boxes.")
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
		SummableIntegerView avgView = (SummableIntegerView) v;
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
		BasicIntegerView view = (BasicIntegerView) findViewById(viewId);
		view.setValue(avg);
	}

	@Override
	public void onAverageCleared(int viewId) {
		BasicIntegerView view = (BasicIntegerView) findViewById(viewId);
		view.clearValue();
	}

	private void setDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("M-d-yyyy", Locale.US);
		this.date.setText(sdf.format(date));
	}

	private void initLists() {
		homePlayers = new ArrayList<EditText>(5);
		awayPlayers = new ArrayList<EditText>(5);
		homeAves = new ArrayList<BasicIntegerView>(5);
		awayAves = new ArrayList<BasicIntegerView>(5);
		homeScores = new ArrayList<PlayerScoreView>(15);
		awayScores = new ArrayList<PlayerScoreView>(15);
		homeFinalRound = new ArrayList<SumView>(8);
		awayFinalRound = new ArrayList<SumView>(8);
		homeSubtotals = new ArrayList<SumView>(3);
		awaySubtotals = new ArrayList<SumView>(3);
		homeRoundAves = new ArrayList<BasicIntegerView>(3);
		awayRoundAves = new ArrayList<BasicIntegerView>(3);
		homeTotals = new ArrayList<SumView>(3);
		awayTotals = new ArrayList<SumView>(3);
	}

	private void setupListeners() {
		for (int i = 0; i < homeAves.size(); i++) {
			homeAves.get(i).addOnValueChangedListener(homeAve);
			awayAves.get(i).addOnValueChangedListener(awayAve);
		}

		for (int i = 0; i < homeScores.size(); i++) {
			PlayerScoreView home = homeScores.get(i);
			PlayerScoreView away = awayScores.get(i);

			home.addOnValueChangedListener(homeSubtotals.get(i / 5));
			home.addOnValueChangedListener(homeFinalRound.get(i % 5));
			away.addOnValueChangedListener(awaySubtotals.get(i / 5));
			away.addOnValueChangedListener(awayFinalRound.get(i % 5));
		}

		for (int i = 0; i < homeTotals.size(); i++) {
			homeSubtotals.get(i).addOnValueChangedListener(homeTotals.get(i));
			awaySubtotals.get(i).addOnValueChangedListener(awayTotals.get(i));
			homeRoundAves.get(i).addOnValueChangedListener(homeTotals.get(i));
			awayRoundAves.get(i).addOnValueChangedListener(awayTotals.get(i));

			homeSubtotals.get(i).addOnValueChangedListener(homeFinalRound.get(5));
			homeRoundAves.get(i).addOnValueChangedListener(homeFinalRound.get(6));
			homeTotals.get(i).addOnValueChangedListener(homeFinalRound.get(7));
			awaySubtotals.get(i).addOnValueChangedListener(awayFinalRound.get(5));
			awayRoundAves.get(i).addOnValueChangedListener(awayFinalRound.get(6));
			awayTotals.get(i).addOnValueChangedListener(awayFinalRound.get(7));
		}

		homeAve.addOnValueChangedListener(avgChangedListener);
		awayAve.addOnValueChangedListener(avgChangedListener);
	}

	private void setupLinks() {
		for (PlayerScoreView home : homeScores) {
			for (PlayerScoreView away : awayScores) {
				if (home.getRound() == away.getRound()
						&& home.getGame() == away.getGame()) {
					linkViews(home, away);
				}
			}
		}
	}

	private void linkViews(View view1, View view2) {
		view1.setTag(view2);
		view2.setTag(view1);
	}

	private void getViewReferences(View v) {
		initLists();

		date = (TextView) v.findViewById(R.id.dateView);

		homePlayers.add((EditText) v.findViewById(R.id.homePlayer1Name));
		homePlayers.add((EditText) v.findViewById(R.id.homePlayer2Name));
		homePlayers.add((EditText) v.findViewById(R.id.homePlayer3Name));
		homePlayers.add((EditText) v.findViewById(R.id.homePlayer4Name));
		homePlayers.add((EditText) v.findViewById(R.id.homePlayer5Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayPlayer1Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayPlayer2Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayPlayer3Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayPlayer4Name));
		awayPlayers.add((EditText) v.findViewById(R.id.awayPlayer5Name));

		homeAves.add((BasicIntegerView) v.findViewById(R.id.homePlayer1Ave));
		homeAves.add((BasicIntegerView) v.findViewById(R.id.homePlayer2Ave));
		homeAves.add((BasicIntegerView) v.findViewById(R.id.homePlayer3Ave));
		homeAves.add((BasicIntegerView) v.findViewById(R.id.homePlayer4Ave));
		homeAves.add((BasicIntegerView) v.findViewById(R.id.homePlayer5Ave));
		awayAves.add((BasicIntegerView) v.findViewById(R.id.awayPlayer1Ave));
		awayAves.add((BasicIntegerView) v.findViewById(R.id.awayPlayer2Ave));
		awayAves.add((BasicIntegerView) v.findViewById(R.id.awayPlayer3Ave));
		awayAves.add((BasicIntegerView) v.findViewById(R.id.awayPlayer4Ave));
		awayAves.add((BasicIntegerView) v.findViewById(R.id.awayPlayer5Ave));

		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer1Round1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer2Round1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer3Round1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer4Round1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer5Round1));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer1Round2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer2Round2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer3Round2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer4Round2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer5Round2));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer1Round3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer2Round3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer3Round3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer4Round3));
		homeScores.add((PlayerScoreView) v.findViewById(R.id.homePlayer5Round3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer1Round1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer2Round1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer3Round1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer4Round1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer5Round1));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer1Round2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer2Round2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer3Round2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer4Round2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer5Round2));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer1Round3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer2Round3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer3Round3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer4Round3));
		awayScores.add((PlayerScoreView) v.findViewById(R.id.awayPlayer5Round3));

		homeFinalRound.add((SumView) v.findViewById(R.id.homePlayer1Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homePlayer2Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homePlayer3Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homePlayer4Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homePlayer5Total));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeTeamSubtotalRound4));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeTeamAveRound4));
		homeFinalRound.add((SumView) v.findViewById(R.id.homeTeamTotalRound4));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayPlayer1Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayPlayer2Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayPlayer3Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayPlayer4Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayPlayer5Total));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayTeamSubtotalRound4));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayTeamAveRound4));
		awayFinalRound.add((SumView) v.findViewById(R.id.awayTeamTotalRound4));

		homeSubtotals.add((SumView) v.findViewById(R.id.homeTeamSubtotalRound1));
		homeSubtotals.add((SumView) v.findViewById(R.id.homeTeamSubtotalRound2));
		homeSubtotals.add((SumView) v.findViewById(R.id.homeTeamSubtotalRound3));
		awaySubtotals.add((SumView) v.findViewById(R.id.awayTeamSubtotalRound1));
		awaySubtotals.add((SumView) v.findViewById(R.id.awayTeamSubtotalRound2));
		awaySubtotals.add((SumView) v.findViewById(R.id.awayTeamSubtotalRound3));

		homeRoundAves.add((BasicIntegerView) v.findViewById(R.id.homeTeamAveRound1));
		homeRoundAves.add((BasicIntegerView) v.findViewById(R.id.homeTeamAveRound2));
		homeRoundAves.add((BasicIntegerView) v.findViewById(R.id.homeTeamAveRound3));
		awayRoundAves.add((BasicIntegerView) v.findViewById(R.id.awayTeamAveRound1));
		awayRoundAves.add((BasicIntegerView) v.findViewById(R.id.awayTeamAveRound2));
		awayRoundAves.add((BasicIntegerView) v.findViewById(R.id.awayTeamAveRound3));

		homeTotals.add((SumView) v.findViewById(R.id.homeTeamTotalRound1));
		homeTotals.add((SumView) v.findViewById(R.id.homeTeamTotalRound2));
		homeTotals.add((SumView) v.findViewById(R.id.homeTeamTotalRound3));
		awayTotals.add((SumView) v.findViewById(R.id.awayTeamTotalRound1));
		awayTotals.add((SumView) v.findViewById(R.id.awayTeamTotalRound2));
		awayTotals.add((SumView) v.findViewById(R.id.awayTeamTotalRound3));

		homeAve = (SumView) v.findViewById(R.id.homeTeamAve);
		awayAve = (SumView) v.findViewById(R.id.awayTeamAve);
	}
}
