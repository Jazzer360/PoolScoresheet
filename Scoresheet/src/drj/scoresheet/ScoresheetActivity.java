package drj.scoresheet;

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
import drj.scoresheet.AveragePickerDialog.AveragePickerListener;
import drj.scoresheet.ScoringDialog.ScoringListener;

public class ScoresheetActivity extends Activity
implements AveragePickerListener, ScoringListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		linkScoreBoxes();
		recalculateRoundTotals();
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

		View homeView = (View) (homeClicked ? v : v.getTag());
		View awayView = (View) (homeClicked ? v.getTag() : v);

		TextView homePlayer = (TextView) ((ViewGroup) homeView.getParent())
				.getChildAt(1);
		TextView awayPlayer = (TextView) ((ViewGroup) awayView.getParent())
				.getChildAt(1);

		Bundle args = new Bundle();
		if (!TextUtils.isEmpty(homePlayer.getText())) {
			args.putString(ScoringDialog.HOME_PLAYER_KEY,
					homePlayer.getText().toString());
		} else {
			args.putString(ScoringDialog.HOME_PLAYER_KEY,
					homePlayer.getHint().toString());
		}

		if (!TextUtils.isEmpty(awayPlayer.getText())) {
			args.putString(ScoringDialog.AWAY_PLAYER_KEY,
					awayPlayer.getText().toString());
		} else {
			args.putString(ScoringDialog.AWAY_PLAYER_KEY,
					awayPlayer.getHint().toString());
		}
		args.putInt(ScoringDialog.HOME_VIEW_ID_KEY, homeView.getId());
		args.putInt(ScoringDialog.AWAY_VIEW_ID_KEY, awayView.getId());

		ScoringDialog dialog = new ScoringDialog();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "ScoringDialog");
	}

	@Override
	public void onScorePicked(int homeViewId, CharSequence homeScore,
			int awayViewId, CharSequence awayScore, boolean ero) {
		TextView home = setViewToText(homeViewId, homeScore);
		TextView away = setViewToText(awayViewId, awayScore);

		if (ero) {
			if (stringToInt(homeScore.toString()) == 10) {
				home.setTypeface(null, Typeface.BOLD);
				away.setTypeface(null, Typeface.NORMAL);
			} else {
				away.setTypeface(null, Typeface.BOLD);
				home.setTypeface(null, Typeface.NORMAL);
			}
		} else {
			home.setTypeface(null, Typeface.NORMAL);
			away.setTypeface(null, Typeface.NORMAL);
		}
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
		recalculateRoundTotals();
	}

	private void recalculateRoundTotals() {
		recalculateAverages();
		recalculatePlayerTotals();

		setViewToInt(R.id.awayTotalGame1,
				getIntFromView(R.id.awayPlayer1Round1) +
				getIntFromView(R.id.awayPlayer2Round1) +
				getIntFromView(R.id.awayPlayer3Round1) +
				getIntFromView(R.id.awayPlayer4Round1) +
				getIntFromView(R.id.awayPlayer5Round1) +
				getIntFromView(R.id.awayAveGame1));

		setViewToInt(R.id.awayTotalGame2,
				getIntFromView(R.id.awayPlayer1Round2) +
				getIntFromView(R.id.awayPlayer2Round2) +
				getIntFromView(R.id.awayPlayer3Round2) +
				getIntFromView(R.id.awayPlayer4Round2) +
				getIntFromView(R.id.awayPlayer5Round2) +
				getIntFromView(R.id.awayAveGame2));

		setViewToInt(R.id.awayTotalGame3,
				getIntFromView(R.id.awayPlayer1Round3) +
				getIntFromView(R.id.awayPlayer2Round3) +
				getIntFromView(R.id.awayPlayer3Round3) +
				getIntFromView(R.id.awayPlayer4Round3) +
				getIntFromView(R.id.awayPlayer5Round3) +
				getIntFromView(R.id.awayAveGame3));

		setViewToInt(R.id.awayTotalGame4,
				getIntFromView(R.id.awayPlayer1Total) +
				getIntFromView(R.id.awayPlayer2Total) +
				getIntFromView(R.id.awayPlayer3Total) +
				getIntFromView(R.id.awayPlayer4Total) +
				getIntFromView(R.id.awayPlayer5Total) +
				getIntFromView(R.id.awayAveGame4));

		setViewToInt(R.id.homeTotalGame1,
				getIntFromView(R.id.homePlayer1Round1) +
				getIntFromView(R.id.homePlayer2Round1) +
				getIntFromView(R.id.homePlayer3Round1) +
				getIntFromView(R.id.homePlayer4Round1) +
				getIntFromView(R.id.homePlayer5Round1) +
				getIntFromView(R.id.homeAveGame1));

		setViewToInt(R.id.homeTotalGame2,
				getIntFromView(R.id.homePlayer1Round2) +
				getIntFromView(R.id.homePlayer2Round2) +
				getIntFromView(R.id.homePlayer3Round2) +
				getIntFromView(R.id.homePlayer4Round2) +
				getIntFromView(R.id.homePlayer5Round2) +
				getIntFromView(R.id.homeAveGame2));

		setViewToInt(R.id.homeTotalGame3,
				getIntFromView(R.id.homePlayer1Round3) +
				getIntFromView(R.id.homePlayer2Round3) +
				getIntFromView(R.id.homePlayer3Round3) +
				getIntFromView(R.id.homePlayer4Round3) +
				getIntFromView(R.id.homePlayer5Round3) +
				getIntFromView(R.id.homeAveGame3));

		setViewToInt(R.id.homeTotalGame4,
				getIntFromView(R.id.homePlayer1Total) +
				getIntFromView(R.id.homePlayer2Total) +
				getIntFromView(R.id.homePlayer3Total) +
				getIntFromView(R.id.homePlayer4Total) +
				getIntFromView(R.id.homePlayer5Total) +
				getIntFromView(R.id.homeAveGame4));
	}

	private void recalculatePlayerTotals() {
		setViewToInt(R.id.awayPlayer1Total,
				getIntFromView(R.id.awayPlayer1Round1) +
				getIntFromView(R.id.awayPlayer1Round2) +
				getIntFromView(R.id.awayPlayer1Round3));

		setViewToInt(R.id.awayPlayer2Total,
				getIntFromView(R.id.awayPlayer2Round1) +
				getIntFromView(R.id.awayPlayer2Round2) +
				getIntFromView(R.id.awayPlayer2Round3));

		setViewToInt(R.id.awayPlayer3Total,
				getIntFromView(R.id.awayPlayer3Round1) +
				getIntFromView(R.id.awayPlayer3Round2) +
				getIntFromView(R.id.awayPlayer3Round3));

		setViewToInt(R.id.awayPlayer4Total,
				getIntFromView(R.id.awayPlayer4Round1) +
				getIntFromView(R.id.awayPlayer4Round2) +
				getIntFromView(R.id.awayPlayer4Round3));

		setViewToInt(R.id.awayPlayer5Total,
				getIntFromView(R.id.awayPlayer5Round1) +
				getIntFromView(R.id.awayPlayer5Round2) +
				getIntFromView(R.id.awayPlayer5Round3));

		setViewToInt(R.id.homePlayer1Total,
				getIntFromView(R.id.homePlayer1Round1) +
				getIntFromView(R.id.homePlayer1Round2) +
				getIntFromView(R.id.homePlayer1Round3));

		setViewToInt(R.id.homePlayer2Total,
				getIntFromView(R.id.homePlayer2Round1) +
				getIntFromView(R.id.homePlayer2Round2) +
				getIntFromView(R.id.homePlayer2Round3));

		setViewToInt(R.id.homePlayer3Total,
				getIntFromView(R.id.homePlayer3Round1) +
				getIntFromView(R.id.homePlayer3Round2) +
				getIntFromView(R.id.homePlayer3Round3));

		setViewToInt(R.id.homePlayer4Total,
				getIntFromView(R.id.homePlayer4Round1) +
				getIntFromView(R.id.homePlayer4Round2) +
				getIntFromView(R.id.homePlayer4Round3));

		setViewToInt(R.id.homePlayer5Total,
				getIntFromView(R.id.homePlayer5Round1) +
				getIntFromView(R.id.homePlayer5Round2) +
				getIntFromView(R.id.homePlayer5Round3));
	}

	private void recalculateAverages() {
		int awayAveTotal = getIntFromView(R.id.awayPlayer1Ave) +
				getIntFromView(R.id.awayPlayer2Ave) +
				getIntFromView(R.id.awayPlayer3Ave) +
				getIntFromView(R.id.awayPlayer4Ave) +
				getIntFromView(R.id.awayPlayer5Ave);
		int homeAveTotal = getIntFromView(R.id.homePlayer1Ave) +
				getIntFromView(R.id.homePlayer2Ave) +
				getIntFromView(R.id.homePlayer3Ave) +
				getIntFromView(R.id.homePlayer4Ave) +
				getIntFromView(R.id.homePlayer5Ave);

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

	private int stringToInt(String string) {
		return !TextUtils.isEmpty(string) ? Integer.valueOf(string) : 0;
	}

	private void linkScoreBoxes(int homeResId, int awayResId) {
		View home = findViewById(homeResId);
		View away = findViewById(awayResId);

		home.setTag(away);
		away.setTag(home);
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
}
