package com.derekjass.poolscoresheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

	private static final int GAME_SET_TAG_KEY = R.id.scoreSetTagKey;
	private static final int PLAYERS = 5;
	private static final int ROUNDS = 3;
	static SimpleDateFormat sdf = new SimpleDateFormat("M-d-yyyy", Locale.US);

	private View progress;
	private View noDataText;
	private View scoresheet;

	private long dateMs;
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

	private Uri matchUri;

	private AsyncTask<Cursor, Void, Void> loadTask;
	private boolean matchDataLoaded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		matchDataLoaded = false;
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
				setDate(System.currentTimeMillis());
			}
		}.execute(v);

		return v;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (matchDataLoaded)
			saveData();
	}

	public void loadUri(Uri matchUri) {
		this.matchUri = matchUri;

		if (matchUri == null) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					scoresheet.setVisibility(View.GONE);
					progress.setVisibility(View.GONE);
					noDataText.setVisibility(View.VISIBLE);
				};
			}.execute();
			return;
		}

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), matchUri,
				null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		matchDataLoaded = false;
		loadTask = new AsyncTask<Cursor, Void, Void>() {
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
				scoresheet.setVisibility(View.GONE);
				noDataText.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
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
				setDate(tDate);
				homeTeam.setText(tHomeTeam);
				awayTeam.setText(tAwayTeam);
				fillNames(homePlayers, tHomePlayers);
				fillNames(awayPlayers, tAwayPlayers);
				fillSummableIntegers(homeAves, tHomeAves);
				fillSummableIntegers(awayAves, tAwayAves);
				fillSummableIntegers(homeScores, tHomeScores);
				fillSummableIntegers(awayScores, tAwayScores);
				setEroFromBitmask(eroBitmask);

				matchDataLoaded = true;
				loadTask = null;

				progress.setVisibility(View.GONE);
				noDataText.setVisibility(View.GONE);
				scoresheet.setVisibility(View.VISIBLE);
			}
		}.execute(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loadTask != null) loadTask.cancel(false);
	}

	private View.OnClickListener scoreBoxClickListener =
			new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean homeClicked = getView().findViewById(R.id.homeTeamViews)
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
			dialog.setTargetFragment(ScoresheetFragment.this, 0);
			dialog.show(getFragmentManager(), "ScoringDialog");
		}
	};

	@Override
	public void onScorePicked(int winViewId, CharSequence winScore,
			int lossViewId, CharSequence lossScore, boolean ero) {
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

	private View.OnClickListener averageBoxClickListener =
			new OnClickListener() {
		@Override
		public void onClick(View v) {
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
			dialog.setTargetFragment(ScoresheetFragment.this, 0);
			dialog.show(getFragmentManager(), "AverageDialog");
		}
	};

	@Override
	public void onAveragePicked(int viewId, CharSequence avg) {
		IntegerView view = (IntegerView) getView().findViewById(viewId);
		view.setValue(avg);
	}

	@Override
	public void onAverageCleared(int viewId) {
		IntegerView view = (IntegerView) getView().findViewById(viewId);
		view.clearValue();
	}

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

	private View.OnClickListener dateBoxClickListener =
			new OnClickListener() {
		@Override
		public void onClick(View v) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(dateMs);
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
		setDate(c.getTimeInMillis());
	}

	private void saveData() {
		ContentValues cv = new ContentValues();

		cv.put(Matches.COLUMN_DATE, dateMs);
		cv.put(Matches.COLUMN_TEAM_HOME, homeTeam.getText().toString());
		cv.put(Matches.COLUMN_TEAM_AWAY, awayTeam.getText().toString());
		cv.put(Matches.COLUMN_PLAYER_AVES_HOME, getValuesString(homeAves));
		cv.put(Matches.COLUMN_PLAYER_AVES_AWAY, getValuesString(awayAves));
		cv.put(Matches.COLUMN_PLAYER_NAMES_HOME, getNamesString(homePlayers));
		cv.put(Matches.COLUMN_PLAYER_NAMES_AWAY, getNamesString(awayPlayers));
		cv.put(Matches.COLUMN_PLAYER_SCORES_HOME, getValuesString(homeScores));
		cv.put(Matches.COLUMN_PLAYER_SCORES_AWAY, getValuesString(awayScores));
		cv.put(Matches.COLUMN_ERO_BITMASK, getEroBitmask());

		int homeWins = 0;
		for (SumView view : homeTotals) {
			if (view.isCircled()) homeWins++;
		}
		if (homeFinalRound.get(PLAYERS + 2).isCircled()) homeWins++;
		cv.put(Matches.COLUMN_ROUND_WINS_HOME, homeWins);

		int awayWins = 0;
		for (SumView view : awayTotals) {
			if (view.isCircled()) awayWins++;
		}
		if (awayFinalRound.get(PLAYERS + 2).isCircled()) awayWins++;
		cv.put(Matches.COLUMN_ROUND_WINS_AWAY, awayWins);

		getActivity().getContentResolver().update(matchUri, cv, null, null);
	}

	private void setEroFromBitmask(long eroBitmask) {
		if (eroBitmask == 0) return;
		for (int i = 0; i < PLAYERS * ROUNDS; i++) {
			if (((1 << i) & eroBitmask) > 0) {
				if (homeScores.get(i).getValue() == 10) {
					homeScores.get(i).setEro(true);
				} else {
					awayScores.get(i).setEro(true);
				}
			}
		}
	}

	private long getEroBitmask() {
		long mask = 0;

		for (int i = 0; i < PLAYERS * ROUNDS; i++) {
			if (homeScores.get(i).isEro() || awayScores.get(i).isEro())
				mask |= 1 << i;
		}

		return mask;
	}

	private void setDate(long millis) {
		dateMs = millis;
		date.setText(sdf.format(new Date(millis)));
	}

	private void getViewReferences(View v) {
		initLists();

		progress = (View) v.findViewById(R.id.scoresheetProgressBar);
		noDataText = (View) v.findViewById(R.id.scoresheetNoDataText);
		scoresheet = (View) v.findViewById(R.id.scoresheet);

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

	@SuppressWarnings("unchecked")
	private void initTags() {
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

	private void initListeners() {
		date.setOnClickListener(dateBoxClickListener);

		for (int i = 0; i < PLAYERS; i++) {
			addViewToSum(homeAve, homeAves.get(i));
			addViewToSum(awayAve, awayAves.get(i));
			homeAves.get(i).setOnClickListener(averageBoxClickListener);
			awayAves.get(i).setOnClickListener(averageBoxClickListener);
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
			homeScores.get(i).setOnClickListener(scoreBoxClickListener);
			awayScores.get(i).setOnClickListener(scoreBoxClickListener);
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

	private static String getNamesString(List<EditText> nameViews) {
		StringBuilder sb = new StringBuilder();

		for (EditText view : nameViews) {
			sb.append(view.getText().toString().replaceAll(",", "\\,"));
			if (view != nameViews.get(nameViews.size() - 1))
				sb.append(",");
		}

		return sb.toString();
	}

	private static String getValuesString(
			List<? extends SummableInteger> intViews) {
		StringBuilder sb = new StringBuilder();

		for (SummableInteger view : intViews) {
			sb.append(view.getValueAsString());
			if (view != intViews.get(intViews.size() - 1))
				sb.append(",");
		}

		return sb.toString();
	}

	private static void fillNames(
			List<EditText> nameViews, String[] names) {
		if (names == null) return;
		for (int i = 0; i < names.length; i++) {
			nameViews.get(i).setText(names[i]);
		}
	}

	private static void fillSummableIntegers(
			List<? extends SummableInteger> intViews, String[] data) {
		if (data == null) return;
		for (int i = 0; i < data.length; i++) {
			if (!TextUtils.isEmpty(data[i]))
				intViews.get(i).setValue(data[i]);
		}
	}

	private static String[] splitString(String s) {
		if (s == null) return null;
		String[] result = s.split("(?<!\\\\),");
		for (String string : result) {
			string = string.replaceAll("\\\\,", ",");
		}
		return result;
	}

	private static int getWinCount(Set<SummableInteger> views) {
		int wins = 0;
		for (SummableInteger view : views) {
			if (view.getValue() == 10) {
				wins++;
			}
		}
		return wins;
	}

	private static void linkViewsByTags(View view1, View view2) {
		view1.setTag(view2);
		view2.setTag(view1);
	}

	private static void addViewToSum(SumView sum, SummableInteger value) {
		value.addOnValueChangedListener(sum);
	}
}
