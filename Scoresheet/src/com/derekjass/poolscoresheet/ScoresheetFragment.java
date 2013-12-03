package com.derekjass.poolscoresheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.derekjass.poolscoresheet.AveragePickerDialog.AveragePickerListener;
import com.derekjass.poolscoresheet.ScoringDialog.ScoringListener;
import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;
import com.derekjass.poolscoresheet.views.IntegerView;
import com.derekjass.poolscoresheet.views.PlayerScoreView;
import com.derekjass.poolscoresheet.views.SumView;
import com.derekjass.poolscoresheet.views.SummableInteger;
import com.derekjass.poolscoresheet.views.SummableInteger.OnValueChangedListener;

public class ScoresheetFragment extends Fragment
implements AveragePickerListener, ScoringListener, LoaderCallbacks<Cursor>,
OnDateSetListener {

	private static final int PLAYER_TAG_KEY = R.id.playerTagKey;
	private static final int PLAYERS = 5;
	private static final int ROUNDS = 3;
	static SimpleDateFormat sSdf = new SimpleDateFormat("M-d-yyyy", Locale.US);

	private View mProgress;
	private View mNoDataText;
	private View mScoresheet;

	private long mDateMs;
	private TextView mDate;
	private EditText mHomeTeam;
	private EditText mAwayTeam;
	private List<EditText> mHomePlayers;
	private List<EditText> mAwayPlayers;
	private List<IntegerView> mHomeAves;
	private List<IntegerView> mAwayAves;
	private List<PlayerScoreView> mHomeScores;
	private List<PlayerScoreView> mAwayScores;
	private List<SumView> mHomeFinalRound;
	private List<SumView> mAwayFinalRound;
	private List<SumView> mHomeSubtotals;
	private List<SumView> mAwaySubtotals;
	private List<IntegerView> mHomeRoundAves;
	private List<IntegerView> mAwayRoundAves;
	private List<SumView> mHomeTotals;
	private List<SumView> mAwayTotals;
	private SumView mHomeAve;
	private SumView mAwayAve;

	private Uri mMatchUri;

	private AsyncTask<Cursor, Void, Void> mLoadTask;
	private boolean mMatchDataLoaded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMatchDataLoaded = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(
				R.layout.fragment_scoresheet, container, false);

		new AsyncTask<View, Void, Void>() {
			@Override
			protected Void doInBackground(View... params) {
				getViewReferences(params[0]);
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				initTags();
				initListeners();
				setDate(new Date(System.currentTimeMillis()));
			}
		}.execute(v);

		return v;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mMatchDataLoaded)
			saveData();
	}

	public void loadUri(Uri matchUri) {
		this.mMatchUri = matchUri;

		if (matchUri == null) {
			mMatchDataLoaded = false;

			if (mLoadTask != null) mLoadTask.cancel(false);
			mLoadTask = null;

			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					hide(mScoresheet);
					hide(mProgress);
					show(mNoDataText);
				};
			}.execute();
		} else {
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), mMatchUri,
				null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mMatchDataLoaded = false;
		mLoadTask = new AsyncTask<Cursor, Void, Void>() {
			private long tDate;
			private String tHomeTeam;
			private String tAwayTeam;
			private String[] tHomePlayers;
			private String[] tAwayPlayers;
			private String[] tHomeAves;
			private String[] tAwayAves;
			private String[] tHomeScores;
			private String[] tAwayScores;
			private long eroBitmask;

			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
				hide(mScoresheet);
				hide(mNoDataText);
				show(mProgress);
			}

			@Override
			protected Void doInBackground(Cursor... cursor) {
				publishProgress();

				Cursor c = cursor[0];
				c.moveToFirst();
				tDate = c.getLong(c.getColumnIndexOrThrow(
						Matches.COLUMN_DATE));
				tHomeTeam = c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_TEAM_HOME));
				tAwayTeam = c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_TEAM_AWAY));
				tHomePlayers = splitString(c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_PLAYER_NAMES_HOME)));
				tAwayPlayers = splitString(c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_PLAYER_NAMES_AWAY)));
				tHomeAves = splitString(c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_PLAYER_AVES_HOME)));
				tAwayAves = splitString(c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_PLAYER_AVES_AWAY)));
				tHomeScores = splitString(c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_PLAYER_SCORES_HOME)));
				tAwayScores = splitString(c.getString(c.getColumnIndexOrThrow(
						Matches.COLUMN_PLAYER_SCORES_AWAY)));
				eroBitmask = c.getLong(c.getColumnIndexOrThrow(
						Matches.COLUMN_ERO_BITMASK));
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				setDate(new Date(tDate));
				mHomeTeam.setText(tHomeTeam);
				mAwayTeam.setText(tAwayTeam);
				fillNames(mHomePlayers, tHomePlayers);
				fillNames(mAwayPlayers, tAwayPlayers);
				fillValues(mHomeAves, tHomeAves);
				fillValues(mAwayAves, tAwayAves);
				fillValues(mHomeScores, tHomeScores);
				fillValues(mAwayScores, tAwayScores);
				setEroFromBitmask(eroBitmask);

				mMatchDataLoaded = true;
				mLoadTask = null;

				hide(mProgress);
				hide(mNoDataText);
				show(mScoresheet);
			}
		}.execute(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		loadUri(null);
	}

	private View.OnClickListener mScoreBoxClickListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean homeClicked = getView().findViewById(R.id.homeTeamViews)
					.findViewById(v.getId()) != null;

			PlayerScoreView homeView =
					(PlayerScoreView) (homeClicked ? v : v.getTag());
			PlayerScoreView awayView =
					(PlayerScoreView) (homeClicked ? v.getTag() : v);

			TextView homePlayer = (TextView) homeView.getTag(PLAYER_TAG_KEY);
			TextView awayPlayer = (TextView) awayView.getTag(PLAYER_TAG_KEY);
			String homeName = homePlayer.getText().toString();
			String awayName = awayPlayer.getText().toString();

			Bundle args = new Bundle();
			args.putString(ScoringDialog.HOME_PLAYER_KEY,
					!TextUtils.isEmpty(homeName) ?
							homeName : homePlayer.getHint().toString());
			args.putString(ScoringDialog.AWAY_PLAYER_KEY,
					!TextUtils.isEmpty(awayName) ?
							awayName : awayPlayer.getHint().toString());
			if (homeView.hasValue()) {
				args.putInt(ScoringDialog.HOME_SCORE_KEY, homeView.getValue());
			}
			if (awayView.hasValue()) {
				args.putInt(ScoringDialog.AWAY_SCORE_KEY, awayView.getValue());
			}
			args.putInt(ScoringDialog.HOME_VIEW_ID_KEY, homeView.getId());
			args.putInt(ScoringDialog.AWAY_VIEW_ID_KEY, awayView.getId());

			args.putBoolean(ScoringDialog.ERO_KEY,
					homeView.isEro() || awayView.isEro());

			ScoringDialog dialog = new ScoringDialog();
			dialog.setArguments(args);
			dialog.setTargetFragment(ScoresheetFragment.this, 0);
			dialog.show(getFragmentManager(), "ScoringDialog");
		}
	};

	@Override
	public void onScorePicked(int winViewId, int winScore,
			int lossViewId, int lossScore, boolean ero) {
		PlayerScoreView winner = (PlayerScoreView) getView()
				.findViewById(winViewId);
		PlayerScoreView loser = (PlayerScoreView) getView()
				.findViewById(lossViewId);

		winner.setValue(winScore);
		winner.setEro(ero);
		loser.setValue(lossScore);
		loser.setEro(false);
	}

	@Override
	public void onScoreCleared(int viewId1, int viewId2) {
		PlayerScoreView view1 = (PlayerScoreView) getView()
				.findViewById(viewId1);
		PlayerScoreView view2 = (PlayerScoreView) getView()
				.findViewById(viewId2);

		view1.clearValue();
		view1.setEro(false);
		view2.clearValue();
		view2.setEro(false);
	}

	private OnValueChangedListener mTotalChangedListener =
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
					int wins1 = view1.countAddendOccurrences(10);
					int wins2 = view2.countAddendOccurrences(10);

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

	private View.OnClickListener mAverageBoxClickListener =
			new OnClickListener() {
		@Override
		public void onClick(View v) {
			SummableInteger avgView = (SummableInteger) v;
			TextView nameView = (TextView) v.getTag(PLAYER_TAG_KEY);
			String name = nameView.getText().toString();

			Bundle args = new Bundle();
			args.putInt(AveragePickerDialog.VIEW_ID_KEY, v.getId());
			args.putString(AveragePickerDialog.NAME_KEY,
					!TextUtils.isEmpty(name) ?
							name : nameView.getHint().toString());
			if (avgView.hasValue()) {
				args.putInt(AveragePickerDialog.AVG_KEY, avgView.getValue());
			}

			AveragePickerDialog dialog = new AveragePickerDialog();
			dialog.setArguments(args);
			dialog.setTargetFragment(ScoresheetFragment.this, 0);
			dialog.show(getFragmentManager(), "AverageDialog");
		}
	};

	@Override
	public void onAveragePicked(int viewId, int avg) {
		SummableInteger view = (SummableInteger) getView().findViewById(viewId);
		view.setValue(avg);
	}

	@Override
	public void onAverageCleared(int viewId) {
		SummableInteger view = (SummableInteger) getView().findViewById(viewId);
		view.clearValue();
	}

	private OnValueChangedListener mAvgChangedListener =
			new OnValueChangedListener() {
		@Override
		public void onValueChanged(SummableInteger subject) {
			int homeAvg = Math.max(
					mAwayAve.getValue() - mHomeAve.getValue(), 0);
			int awayAvg = Math.max(
					mHomeAve.getValue() - mAwayAve.getValue(), 0);

			for (int i = 0; i < ROUNDS; i++) {
				SummableInteger homeRoundAve = mHomeRoundAves.get(i);
				SummableInteger awayRoundAve = mAwayRoundAves.get(i);
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

	private View.OnClickListener mDateBoxClickListener =
			new OnClickListener() {
		@Override
		public void onClick(View v) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(mDateMs);
			DatePickerDialog dialog = new DatePickerDialog(
					getActivity(), ScoresheetFragment.this,
					c.get(Calendar.YEAR),
					c.get(Calendar.MONTH),
					c.get(Calendar.DATE));
			dialog.show();
		}
	};

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, monthOfYear);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		setDate(c.getTime());
	}

	private void saveData() {
		ContentValues cv = new ContentValues();

		cv.put(Matches.COLUMN_DATE, mDateMs);
		cv.put(Matches.COLUMN_TEAM_HOME, mHomeTeam.getText().toString());
		cv.put(Matches.COLUMN_TEAM_AWAY, mAwayTeam.getText().toString());
		cv.put(Matches.COLUMN_PLAYER_AVES_HOME, getValuesString(mHomeAves));
		cv.put(Matches.COLUMN_PLAYER_AVES_AWAY, getValuesString(mAwayAves));
		cv.put(Matches.COLUMN_PLAYER_NAMES_HOME, getNamesString(mHomePlayers));
		cv.put(Matches.COLUMN_PLAYER_NAMES_AWAY, getNamesString(mAwayPlayers));
		cv.put(Matches.COLUMN_PLAYER_SCORES_HOME, getValuesString(mHomeScores));
		cv.put(Matches.COLUMN_PLAYER_SCORES_AWAY, getValuesString(mAwayScores));
		cv.put(Matches.COLUMN_ERO_BITMASK, getEroBitmask());

		int homeWins = 0;
		for (SumView view : mHomeTotals) {
			if (view.isCircled()) homeWins++;
		}
		if (mHomeFinalRound.get(PLAYERS + 2).isCircled()) homeWins++;
		cv.put(Matches.COLUMN_ROUND_WINS_HOME, homeWins);

		int awayWins = 0;
		for (SumView view : mAwayTotals) {
			if (view.isCircled()) awayWins++;
		}
		if (mAwayFinalRound.get(PLAYERS + 2).isCircled()) awayWins++;
		cv.put(Matches.COLUMN_ROUND_WINS_AWAY, awayWins);

		getActivity().getContentResolver().update(mMatchUri, cv, null, null);
	}

	private void setEroFromBitmask(long eroBitmask) {
		if (eroBitmask == 0) return;
		for (int i = 0; i < PLAYERS * ROUNDS; i++) {
			if (((1 << i) & eroBitmask) > 0) {
				if (mHomeScores.get(i).getValue() == 10) {
					mHomeScores.get(i).setEro(true);
				} else {
					mAwayScores.get(i).setEro(true);
				}
			}
		}
	}

	private long getEroBitmask() {
		long mask = 0;

		for (int i = 0; i < PLAYERS * ROUNDS; i++) {
			if (mHomeScores.get(i).isEro() || mAwayScores.get(i).isEro())
				mask |= 1 << i;
		}

		return mask;
	}

	private void setDate(Date newDate) {
		mDateMs = newDate.getTime();
		mDate.setText(sSdf.format(newDate));
	}

	private void getViewReferences(View v) {
		initLists();

		mProgress = (View) v.findViewById(R.id.scoresheetProgressBar);
		mNoDataText = (View) v.findViewById(R.id.scoresheetNoDataText);
		mScoresheet = (View) v.findViewById(R.id.scoresheet);

		mDate = (TextView) v.findViewById(R.id.dateView);

		mHomeTeam = (EditText) v.findViewById(R.id.homeTeamName);
		mAwayTeam = (EditText) v.findViewById(R.id.awayTeamName);

		mHomePlayers.add((EditText) v.findViewById(R.id.homeP1Name));
		mHomePlayers.add((EditText) v.findViewById(R.id.homeP2Name));
		mHomePlayers.add((EditText) v.findViewById(R.id.homeP3Name));
		mHomePlayers.add((EditText) v.findViewById(R.id.homeP4Name));
		mHomePlayers.add((EditText) v.findViewById(R.id.homeP5Name));
		mAwayPlayers.add((EditText) v.findViewById(R.id.awayP1Name));
		mAwayPlayers.add((EditText) v.findViewById(R.id.awayP2Name));
		mAwayPlayers.add((EditText) v.findViewById(R.id.awayP3Name));
		mAwayPlayers.add((EditText) v.findViewById(R.id.awayP4Name));
		mAwayPlayers.add((EditText) v.findViewById(R.id.awayP5Name));

		mHomeAves.add((IntegerView) v.findViewById(R.id.homeP1Ave));
		mHomeAves.add((IntegerView) v.findViewById(R.id.homeP2Ave));
		mHomeAves.add((IntegerView) v.findViewById(R.id.homeP3Ave));
		mHomeAves.add((IntegerView) v.findViewById(R.id.homeP4Ave));
		mHomeAves.add((IntegerView) v.findViewById(R.id.homeP5Ave));
		mAwayAves.add((IntegerView) v.findViewById(R.id.awayP1Ave));
		mAwayAves.add((IntegerView) v.findViewById(R.id.awayP2Ave));
		mAwayAves.add((IntegerView) v.findViewById(R.id.awayP3Ave));
		mAwayAves.add((IntegerView) v.findViewById(R.id.awayP4Ave));
		mAwayAves.add((IntegerView) v.findViewById(R.id.awayP5Ave));

		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP1R1));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP2R1));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP3R1));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP4R1));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP5R1));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP1R2));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP2R2));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP3R2));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP4R2));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP5R2));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP1R3));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP2R3));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP3R3));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP4R3));
		mHomeScores.add((PlayerScoreView) v.findViewById(R.id.homeP5R3));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP1R1));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP2R1));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP3R1));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP4R1));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP5R1));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP1R2));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP2R2));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP3R2));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP4R2));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP5R2));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP1R3));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP2R3));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP3R3));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP4R3));
		mAwayScores.add((PlayerScoreView) v.findViewById(R.id.awayP5R3));

		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeP1Total));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeP2Total));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeP3Total));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeP4Total));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeP5Total));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeSubtotalR4));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeAveR4));
		mHomeFinalRound.add((SumView) v.findViewById(R.id.homeTotalR4));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayP1Total));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayP2Total));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayP3Total));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayP4Total));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayP5Total));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awaySubtotalR4));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayAveR4));
		mAwayFinalRound.add((SumView) v.findViewById(R.id.awayTotalR4));

		mHomeSubtotals.add((SumView) v.findViewById(R.id.homeSubtotalR1));
		mHomeSubtotals.add((SumView) v.findViewById(R.id.homeSubtotalR2));
		mHomeSubtotals.add((SumView) v.findViewById(R.id.homeSubtotalR3));
		mAwaySubtotals.add((SumView) v.findViewById(R.id.awaySubtotalR1));
		mAwaySubtotals.add((SumView) v.findViewById(R.id.awaySubtotalR2));
		mAwaySubtotals.add((SumView) v.findViewById(R.id.awaySubtotalR3));

		mHomeRoundAves.add((IntegerView) v.findViewById(R.id.homeAveR1));
		mHomeRoundAves.add((IntegerView) v.findViewById(R.id.homeAveR2));
		mHomeRoundAves.add((IntegerView) v.findViewById(R.id.homeAveR3));
		mAwayRoundAves.add((IntegerView) v.findViewById(R.id.awayAveR1));
		mAwayRoundAves.add((IntegerView) v.findViewById(R.id.awayAveR2));
		mAwayRoundAves.add((IntegerView) v.findViewById(R.id.awayAveR3));

		mHomeTotals.add((SumView) v.findViewById(R.id.homeTotalR1));
		mHomeTotals.add((SumView) v.findViewById(R.id.homeTotalR2));
		mHomeTotals.add((SumView) v.findViewById(R.id.homeTotalR3));
		mAwayTotals.add((SumView) v.findViewById(R.id.awayTotalR1));
		mAwayTotals.add((SumView) v.findViewById(R.id.awayTotalR2));
		mAwayTotals.add((SumView) v.findViewById(R.id.awayTotalR3));

		mHomeAve = (SumView) v.findViewById(R.id.homeAve);
		mAwayAve = (SumView) v.findViewById(R.id.awayAve);
	}

	private void initLists() {
		mHomePlayers = new ArrayList<EditText>(PLAYERS);
		mAwayPlayers = new ArrayList<EditText>(PLAYERS);
		mHomeAves = new ArrayList<IntegerView>(PLAYERS);
		mAwayAves = new ArrayList<IntegerView>(PLAYERS);
		mHomeScores = new ArrayList<PlayerScoreView>(PLAYERS * ROUNDS);
		mAwayScores = new ArrayList<PlayerScoreView>(PLAYERS * ROUNDS);
		mHomeFinalRound = new ArrayList<SumView>(PLAYERS + 3);
		mAwayFinalRound = new ArrayList<SumView>(PLAYERS + 3);
		mHomeSubtotals = new ArrayList<SumView>(ROUNDS);
		mAwaySubtotals = new ArrayList<SumView>(ROUNDS);
		mHomeRoundAves = new ArrayList<IntegerView>(ROUNDS);
		mAwayRoundAves = new ArrayList<IntegerView>(ROUNDS);
		mHomeTotals = new ArrayList<SumView>(ROUNDS);
		mAwayTotals = new ArrayList<SumView>(ROUNDS);
	}

	private void initTags() {
		int r4total = PLAYERS + 2;
		linkViewsByTags(mHomeFinalRound.get(r4total),
				mAwayFinalRound.get(r4total));

		for (int i = 0; i < ROUNDS; i++) {
			linkViewsByTags(mHomeTotals.get(i), mAwayTotals.get(i));
		}

		for (int i = 0; i < PLAYERS; i++) {
			linkViewsByTags(mHomeAves.get(i), mHomePlayers.get(i),
					PLAYER_TAG_KEY);
			linkViewsByTags(mAwayAves.get(i), mAwayPlayers.get(i),
					PLAYER_TAG_KEY);
		}

		for (int i = 0; i < ROUNDS * PLAYERS; i++) {
			linkViewsByTags(mHomeScores.get(i), mHomePlayers.get(i % PLAYERS),
					PLAYER_TAG_KEY);
			linkViewsByTags(mAwayScores.get(i), mAwayPlayers.get(i % PLAYERS),
					PLAYER_TAG_KEY);
			PlayerScoreView home = mHomeScores.get(i);
			for (PlayerScoreView away : mAwayScores) {
				if (home.getRound() == away.getRound() &&
						home.getGame() == away.getGame()) {
					linkViewsByTags(home, away);
				}
			}
		}
	}

	private void initListeners() {
		mDate.setOnClickListener(mDateBoxClickListener);

		for (int i = 0; i < PLAYERS; i++) {
			addViewToSum(mHomeAve, mHomeAves.get(i));
			addViewToSum(mAwayAve, mAwayAves.get(i));
			mHomeAves.get(i).setOnClickListener(mAverageBoxClickListener);
			mAwayAves.get(i).setOnClickListener(mAverageBoxClickListener);
		}

		for (int i = 0; i < PLAYERS * ROUNDS; i++) {
			PlayerScoreView homeScore = mHomeScores.get(i);
			PlayerScoreView awayScore = mAwayScores.get(i);

			int round = i / PLAYERS;
			int game = i % PLAYERS;

			addViewToSum(mHomeSubtotals.get(round), homeScore);
			addViewToSum(mHomeFinalRound.get(game), homeScore);
			addViewToSum(mAwaySubtotals.get(round), awayScore);
			addViewToSum(mAwayFinalRound.get(game), awayScore);
			mHomeScores.get(i).setOnClickListener(mScoreBoxClickListener);
			mAwayScores.get(i).setOnClickListener(mScoreBoxClickListener);
		}

		final int r4Subtotal = PLAYERS + 0;
		final int r4AveTotal = PLAYERS + 1;
		final int r4Total = PLAYERS + 2;

		for (int i = 0; i < ROUNDS; i++) {
			addViewToSum(mHomeTotals.get(i), mHomeSubtotals.get(i));
			addViewToSum(mHomeTotals.get(i), mHomeRoundAves.get(i));
			addViewToSum(mHomeFinalRound.get(r4Subtotal),
					mHomeSubtotals.get(i));
			addViewToSum(mHomeFinalRound.get(r4AveTotal),
					mHomeRoundAves.get(i));
			addViewToSum(mHomeFinalRound.get(r4Total), mHomeTotals.get(i));

			addViewToSum(mAwayTotals.get(i), mAwaySubtotals.get(i));
			addViewToSum(mAwayTotals.get(i), mAwayRoundAves.get(i));
			addViewToSum(mAwayFinalRound.get(r4Subtotal),
					mAwaySubtotals.get(i));
			addViewToSum(mAwayFinalRound.get(r4AveTotal),
					mAwayRoundAves.get(i));
			addViewToSum(mAwayFinalRound.get(r4Total), mAwayTotals.get(i));

			mHomeTotals.get(i).addOnValueChangedListener(mTotalChangedListener);
			mAwayTotals.get(i).addOnValueChangedListener(mTotalChangedListener);
		}

		mHomeFinalRound.get(r4Total).addOnValueChangedListener(
				mTotalChangedListener);
		mAwayFinalRound.get(r4Total).addOnValueChangedListener(
				mTotalChangedListener);

		mHomeAve.addOnValueChangedListener(mAvgChangedListener);
		mAwayAve.addOnValueChangedListener(mAvgChangedListener);
	}

	private static void show(View v) {
		v.animate().setListener(null);
		if (v.getVisibility() != View.VISIBLE) {
			v.setAlpha(0f);
			v.setVisibility(View.VISIBLE);
		}
		v.animate().alpha(1f);
	}

	private static void hide(final View v) {
		if (v.getVisibility() == View.VISIBLE) {
			v.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					v.setVisibility(View.GONE);
				}
			});
		}
	}

	private static String getNamesString(List<EditText> nameViews) {
		StringBuilder sb = new StringBuilder();

		for (EditText view : nameViews) {
			sb.append(view.getText().toString().replaceAll(",", "\\\\,"));
			if (view != nameViews.get(nameViews.size() - 1)) sb.append(",");
		}

		return sb.toString();
	}

	private static String getValuesString(
			List<? extends SummableInteger> intViews) {
		StringBuilder sb = new StringBuilder();

		for (SummableInteger view : intViews) {
			sb.append(view.getValueAsString());
			if (view != intViews.get(intViews.size() - 1)) sb.append(",");
		}

		return sb.toString();
	}

	private static String[] splitString(String s) {
		if (s == null) return null;
		String[] result = s.split("(?<!\\\\),");
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].replaceAll("\\\\,", ",");
		}
		return result;
	}

	private static void fillNames(
			List<EditText> nameViews, String[] names) {
		if (names == null) return;
		for (int i = 0; i < names.length; i++) {
			nameViews.get(i).setText(names[i]);
		}
	}

	private static void fillValues(
			List<? extends SummableInteger> intViews, String[] data) {
		if (data == null) return;
		for (int i = 0; i < data.length; i++) {
			if (!TextUtils.isEmpty(data[i])) intViews.get(i).setValue(data[i]);
		}
	}

	private static void linkViewsByTags(View view1, View view2) {
		view1.setTag(view2);
		view2.setTag(view1);
	}

	private static void linkViewsByTags(View view1, View view2, int key) {
		view1.setTag(key, view2);
		view2.setTag(key, view1);
	}

	private static void addViewToSum(SumView sum, SummableInteger value) {
		value.addOnValueChangedListener(sum);
	}
}
