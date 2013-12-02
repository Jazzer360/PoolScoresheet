package com.derekjass.poolscoresheet;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class MatchCursorAdapter extends CursorAdapter {

	private LayoutInflater mInflater;

	private final int mDateCol;
	private final int mHomeTeamCol;
	private final int mAwayTeamCol;
	private final int mHomeWinsCol;
	private final int mAwayWinsCol;

	public MatchCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);

		mDateCol = c.getColumnIndexOrThrow(Matches.COLUMN_DATE);
		mHomeTeamCol = c.getColumnIndexOrThrow(Matches.COLUMN_TEAM_HOME);
		mAwayTeamCol = c.getColumnIndexOrThrow(Matches.COLUMN_TEAM_AWAY);
		mHomeWinsCol = c.getColumnIndexOrThrow(Matches.COLUMN_ROUND_WINS_HOME);
		mAwayWinsCol = c.getColumnIndexOrThrow(Matches.COLUMN_ROUND_WINS_AWAY);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		if (mInflater == null) mInflater = LayoutInflater.from(context);
		View newView = mInflater.inflate(
				R.layout.listitem_match, parent, false);

		ViewHolder views = new ViewHolder();
		views.dateView =
				(TextView) newView.findViewById(R.id.listDate);
		views.inProgressView =
				(TextView) newView.findViewById(R.id.listMatchIncomplete);
		views.homeTeamView =
				(TextView) newView.findViewById(R.id.listHomeTeam);
		views.awayTeamView =
				(TextView) newView.findViewById(R.id.listAwayTeam);
		views.homeWinsView =
				(TextView) newView.findViewById(R.id.listHomeWins);
		views.awayWinsView =
				(TextView) newView.findViewById(R.id.listAwayWins);

		newView.setTag(views);
		return newView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder views = (ViewHolder) view.getTag();

		Date date = new Date(cursor.getLong(mDateCol));

		String homeTeam = cursor.getString(mHomeTeamCol);
		String awayTeam = cursor.getString(mAwayTeamCol);

		int homeWins = cursor.getInt(mHomeWinsCol);
		int awayWins = cursor.getInt(mAwayWinsCol);

		int progressVisibility =
				homeWins + awayWins == 4 ? View.INVISIBLE : View.VISIBLE;

		views.dateView.setText(ScoresheetFragment.sSdf.format(date));
		views.inProgressView.setVisibility(progressVisibility);
		views.homeTeamView.setText(homeTeam);
		views.awayTeamView.setText(awayTeam);
		views.homeWinsView.setText("- " + String.valueOf(homeWins));
		views.awayWinsView.setText("- " + String.valueOf(awayWins));
	}

	static class ViewHolder {
		TextView dateView;
		TextView inProgressView;
		TextView homeTeamView;
		TextView awayTeamView;
		TextView homeWinsView;
		TextView awayWinsView;
	}
}
