package com.derekjass.poolscoresheet;

import com.derekjass.poolscoresheet.AveragePickerDialog.AveragePickerListener;
import com.derekjass.poolscoresheet.ScoringDialog.ScoringListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScoresheetActivity extends Activity
implements AveragePickerListener, ScoringListener {

	private static final int[][] homePlayerGames =
		{{
			R.id.homePlayer1Round1,
			R.id.homePlayer2Round1,
			R.id.homePlayer3Round1,
			R.id.homePlayer4Round1,
			R.id.homePlayer5Round1,
		}, {
			R.id.homePlayer1Round2,
			R.id.homePlayer2Round2,
			R.id.homePlayer3Round2,
			R.id.homePlayer4Round2,
			R.id.homePlayer5Round2,
		}, {
			R.id.homePlayer1Round3,
			R.id.homePlayer2Round3,
			R.id.homePlayer3Round3,
			R.id.homePlayer4Round3,
			R.id.homePlayer5Round3,
		}, {
			R.id.homePlayer1Total,
			R.id.homePlayer2Total,
			R.id.homePlayer3Total,
			R.id.homePlayer4Total,
			R.id.homePlayer5Total,
		}};
	private static final int[][] awayPlayerGames =
		{{
			R.id.awayPlayer1Round1,
			R.id.awayPlayer2Round1,
			R.id.awayPlayer3Round1,
			R.id.awayPlayer4Round1,
			R.id.awayPlayer5Round1,
		}, {
			R.id.awayPlayer1Round2,
			R.id.awayPlayer2Round2,
			R.id.awayPlayer3Round2,
			R.id.awayPlayer4Round2,
			R.id.awayPlayer5Round2,
		}, {
			R.id.awayPlayer1Round3,
			R.id.awayPlayer2Round3,
			R.id.awayPlayer3Round3,
			R.id.awayPlayer4Round3,
			R.id.awayPlayer5Round3,
		}, {
			R.id.awayPlayer1Total,
			R.id.awayPlayer2Total,
			R.id.awayPlayer3Total,
			R.id.awayPlayer4Total,
			R.id.awayPlayer5Total,
		}};
	private static final int[] homePlayerAves =
		{
		R.id.homePlayer1Ave,
		R.id.homePlayer2Ave,
		R.id.homePlayer3Ave,
		R.id.homePlayer4Ave,
		R.id.homePlayer5Ave
		};
	private static final int[] awayPlayerAves =
		{
		R.id.awayPlayer1Ave,
		R.id.awayPlayer2Ave,
		R.id.awayPlayer3Ave,
		R.id.awayPlayer4Ave,
		R.id.awayPlayer5Ave
		};
	private static final int[] homeTeamTotals =
		{
		R.id.homeTotalGame1,
		R.id.homeTotalGame2,
		R.id.homeTotalGame3,
		R.id.homeTotalGame4
		};
	private static final int[] awayTeamTotals =
		{
		R.id.awayTotalGame1,
		R.id.awayTotalGame2,
		R.id.awayTotalGame3,
		R.id.awayTotalGame4
		};
	private static final int[] homeTeamAves =
		{
		R.id.homeAveGame1,
		R.id.homeAveGame2,
		R.id.homeAveGame3,
		R.id.homeAveGame4
		};
	private static final int[] awayTeamAves =
		{
		R.id.awayAveGame1,
		R.id.awayAveGame2,
		R.id.awayAveGame3,
		R.id.awayAveGame4
		};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		linkScoreBoxes();
		recalculateAverages();
		recalculatePlayerTotals();
		recalculateRoundTotals();
	}

	@Override
	protected void onResume() {
		super.onResume();
		recalculateAverages();
		recalculatePlayerTotals();
		recalculateRoundTotals();
	}

	private void linkScoreBoxes() {
		linkScoreBoxes(R.id.homePlayer1Round1, R.id.awayPlayer1Round1);
		linkScoreBoxes(R.id.homePlayer2Round1, R.id.awayPlayer2Round1);
		linkScoreBoxes(R.id.homePlayer3Round1, R.id.awayPlayer3Round1);
		linkScoreBoxes(R.id.homePlayer4Round1, R.id.awayPlayer4Round1);
		linkScoreBoxes(R.id.homePlayer5Round1, R.id.awayPlayer5Round1);

		linkScoreBoxes(R.id.homePlayer1Round2, R.id.awayPlayer2Round2);
		linkScoreBoxes(R.id.homePlayer2Round2, R.id.awayPlayer4Round2);
		linkScoreBoxes(R.id.homePlayer3Round2, R.id.awayPlayer1Round2);
		linkScoreBoxes(R.id.homePlayer4Round2, R.id.awayPlayer5Round2);
		linkScoreBoxes(R.id.homePlayer5Round2, R.id.awayPlayer3Round2);

		linkScoreBoxes(R.id.homePlayer1Round3, R.id.awayPlayer3Round3);
		linkScoreBoxes(R.id.homePlayer2Round3, R.id.awayPlayer1Round3);
		linkScoreBoxes(R.id.homePlayer3Round3, R.id.awayPlayer5Round3);
		linkScoreBoxes(R.id.homePlayer4Round3, R.id.awayPlayer2Round3);
		linkScoreBoxes(R.id.homePlayer5Round3, R.id.awayPlayer4Round3);
	}

	private void linkScoreBoxes(int homeResId, int awayResId) {
		View home = findViewById(homeResId);
		View away = findViewById(awayResId);

		home.setTag(away);
		away.setTag(home);
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

		TextView homeView = (TextView) (homeClicked ? v : v.getTag());
		TextView awayView = (TextView) (homeClicked ? v.getTag() : v);
		String homeScore = homeView.getText().toString();
		String awayScore = awayView.getText().toString();

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
				viewHasBoldText(homeView) || viewHasBoldText(awayView));

		ScoringDialog dialog = new ScoringDialog();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "ScoringDialog");
	}

	@Override
	public void onScorePicked(int winViewId, CharSequence winScore,
			int lossViewId, CharSequence lossScore, boolean ero) {
		TextView winner = setViewToText(winViewId, winScore);
		TextView loser = setViewToText(lossViewId, lossScore);

		if (ero) {
			winner.setTypeface(null, Typeface.BOLD);
			loser.setTypeface(null, Typeface.NORMAL);
		} else {
			winner.setTypeface(null, Typeface.NORMAL);
			loser.setTypeface(null, Typeface.NORMAL);
		}

		recalculatePlayerTotals();
		recalculateRoundTotals();
	}

	private void recalculatePlayerTotals() {
		for (int playerIndex = 0; playerIndex < 5; playerIndex++) {
			if (!TextUtils.isEmpty(
					getTextFromView(homePlayerGames[2][playerIndex]))) {
				int playerTotal = sumPlayer(playerIndex, homePlayerGames);
				setViewToInt(homePlayerGames[3][playerIndex], playerTotal);
			}
			if (!TextUtils.isEmpty(
					getTextFromView(awayPlayerGames[2][playerIndex]))) {
				int playerTotal = sumPlayer(playerIndex, awayPlayerGames);
				setViewToInt(awayPlayerGames[3][playerIndex], playerTotal);
			}
		}
	}

	private int sumPlayer(int playerIndex, int[][] playerViews) {
		int total = 0;
		for (int roundIndex = 0; roundIndex < 3; roundIndex++) {
			total += getIntFromView(playerViews[roundIndex][playerIndex]);
		}
		return total;
	}

	public void onAverageBoxClicked(View v) {
		TextView avgView = (TextView) v;
		TextView nameView = (TextView) ((ViewGroup) v.getParent())
				.getChildAt(1);
		String avg = avgView.getText().toString();
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
		setViewToText(viewId, avg);
		recalculateAverages();
		recalculateRoundTotals();
	}

	private void recalculateAverages() {
		int homeAveTotal = 0;
		int awayAveTotal = 0;

		for (int playerIndex = 0; playerIndex < 5; playerIndex++) {
			homeAveTotal += getIntFromView(homePlayerAves[playerIndex]);
			awayAveTotal += getIntFromView(awayPlayerAves[playerIndex]);
		}

		int awayRoundBonus = Math.max(0, homeAveTotal - awayAveTotal);
		int homeRoundBonus = Math.max(0, awayAveTotal - homeAveTotal);

		setViewToInt(R.id.awayAveTotal, awayAveTotal);
		setViewToInt(R.id.homeAveTotal, homeAveTotal);

		setViewToInt(R.id.awayAveGame1, awayRoundBonus);
		setViewToInt(R.id.awayAveGame2, awayRoundBonus);
		setViewToInt(R.id.awayAveGame3, awayRoundBonus);
		setViewToInt(R.id.awayAveGame4, awayRoundBonus * 3);
		setViewToInt(R.id.homeAveGame1, homeRoundBonus);
		setViewToInt(R.id.homeAveGame2, homeRoundBonus);
		setViewToInt(R.id.homeAveGame3, homeRoundBonus);
		setViewToInt(R.id.homeAveGame4, homeRoundBonus * 3);
	}

	private void recalculateRoundTotals() {
		for (int roundIndex = 0; roundIndex < 4; roundIndex++) {
			if (!TextUtils.isEmpty(
					getTextFromView(homePlayerGames[roundIndex][4]))) {
				int homeTotal = 0;
				int awayTotal = 0;
				for (int playerIndex = 0; playerIndex < 5; playerIndex++) {
					homeTotal += getIntFromView(
							homePlayerGames[roundIndex][playerIndex]);
					awayTotal += getIntFromView(
							awayPlayerGames[roundIndex][playerIndex]);
				}

				homeTotal += getIntFromView(homeTeamAves[roundIndex]);
				awayTotal += getIntFromView(awayTeamAves[roundIndex]);

				setViewToInt(homeTeamTotals[roundIndex], homeTotal);
				setViewToInt(awayTeamTotals[roundIndex], awayTotal);

				if (roundIndex != 3) {
					if (homeTotal > awayTotal ||
							(homeTotal == awayTotal
							&& countHomeWins(roundIndex) > 2)) {
						circleScore(homeTeamTotals[roundIndex]);
						clearCircle(awayTeamTotals[roundIndex]);
					} else if (awayTotal > homeTotal ||
							(homeTotal == awayTotal
							&& countHomeWins(roundIndex) < 3)) {
						circleScore(awayTeamTotals[roundIndex]);
						clearCircle(homeTeamTotals[roundIndex]);
					}
				} else {
					if (homeTotal > awayTotal ||
							(homeTotal == awayTotal
							&& countHomeWins(roundIndex) > 7)) {
						circleScore(homeTeamTotals[roundIndex]);
						clearCircle(awayTeamTotals[roundIndex]);
					} else if (homeTotal > awayTotal ||
							(homeTotal == awayTotal
							&& countHomeWins(roundIndex) < 8)) {
						circleScore(awayTeamTotals[roundIndex]);
						clearCircle(homeTeamTotals[roundIndex]);
					}
				}
			} else {
				setViewToText(homeTeamTotals[roundIndex], "");
				setViewToText(awayTeamTotals[roundIndex], "");
				clearCircle(homeTeamTotals[roundIndex]);
				clearCircle(awayTeamTotals[roundIndex]);
			}
		}
	}

	private int countHomeWins(int roundIndex) {
		int sum = 0;
		if (roundIndex == 3) {
			for (int i = 0; i < 3; i++) {
				sum += countHomeWins(i);
			}
			return sum;
		} else {
			for (int playerIndex = 0; playerIndex < 5; playerIndex++) {
				sum += getIntFromView(homePlayerGames[roundIndex][playerIndex])
						== 10 ? 1 : 0;
			}
			return sum;
		}
	}

	private void circleScore(int viewId) {
		View v = findViewById(viewId);
		v.setBackgroundResource(R.drawable.round_winner_bg);
	}

	private void clearCircle(int viewId) {
		View v = findViewById(viewId);
		v.setBackgroundResource(R.drawable.box_bg);
	}

	private TextView setViewToInt(int viewId, int value) {
		TextView v = (TextView) findViewById(viewId);
		v.setText(String.valueOf(value));
		return v;
	}

	private TextView setViewToText(int viewId, CharSequence string) {
		TextView v = (TextView) findViewById(viewId);
		v.setText(string);
		return v;
	}

	private int getIntFromView(int viewId) {
		TextView view = (TextView) findViewById(viewId);
		return stringToInt(view.getText().toString());
	}

	private CharSequence getTextFromView(int viewId) {
		TextView view = (TextView) findViewById(viewId);
		return view.getText();
	}

	private int stringToInt(String string) {
		return !TextUtils.isEmpty(string) ? Integer.valueOf(string) : 0;
	}

	private boolean viewHasBoldText(TextView v) {
		Typeface tf = v.getTypeface();
		return (tf != null && (tf.getStyle() & Typeface.BOLD) != 0)
				? true : false;
	}
}
