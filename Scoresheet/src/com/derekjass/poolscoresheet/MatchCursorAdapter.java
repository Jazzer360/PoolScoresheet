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

	public MatchCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = LayoutInflater.from(context);
		View newView = li.inflate(R.layout.listitem_match, parent, false);
		bindView(newView, context, cursor);
		return newView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Date date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(
				Matches.COLUMN_DATE)));

		String homeTeam = cursor.getString(cursor.getColumnIndexOrThrow(
				Matches.COLUMN_TEAM_HOME));
		String awayTeam = cursor.getString(cursor.getColumnIndexOrThrow(
				Matches.COLUMN_TEAM_AWAY));

		int homeWins = cursor.getInt(cursor.getColumnIndexOrThrow(
				Matches.COLUMN_ROUND_WINS_HOME));
		int awayWins = cursor.getInt(cursor.getColumnIndexOrThrow(
				Matches.COLUMN_ROUND_WINS_AWAY));

		int progressVisibility =
				homeWins + awayWins == 4 ? View.INVISIBLE : View.VISIBLE;

		TextView dateView =
				(TextView) view.findViewById(R.id.listDate);
		TextView inProgressView =
				(TextView) view.findViewById(R.id.listMatchIncomplete);
		TextView homeTeamView =
				(TextView) view.findViewById(R.id.listHomeTeam);
		TextView awayTeamView =
				(TextView) view.findViewById(R.id.listAwayTeam);
		TextView homeWinsView =
				(TextView) view.findViewById(R.id.listHomeWins);
		TextView awayWinsView =
				(TextView) view.findViewById(R.id.listAwayWins);

		dateView.setText(ScoresheetFragment.sdf.format(date));
		inProgressView.setVisibility(progressVisibility);
		homeTeamView.setText(homeTeam);
		awayTeamView.setText(awayTeam);
		homeWinsView.setText("- " + String.valueOf(homeWins));
		awayWinsView.setText("- " + String.valueOf(awayWins));
	}

}
