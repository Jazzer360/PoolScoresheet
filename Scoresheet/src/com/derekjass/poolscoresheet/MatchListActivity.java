package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.derekjass.poolscoresheet.MatchListFragment.MatchSelectedListener;
import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class MatchListActivity extends Activity 
implements LoaderCallbacks<Cursor>, MatchSelectedListener {

	private MatchListFragment list;
	private MatchCursorAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_matchlist);

		list = (MatchListFragment) getFragmentManager()
				.findFragmentById(R.id.matchlistFragment);

		listAdapter = new MatchCursorAdapter(this, null, 0);
		list.setListAdapter(listAdapter);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_matchlist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionNew:
			startActivity(new Intent(this, ScoresheetActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Matches.CONTENT_URI,
				new String[]{
				Matches._ID,
				Matches.COLUMN_DATE,
				Matches.COLUMN_TEAM_HOME,
				Matches.COLUMN_TEAM_AWAY,
				Matches.COLUMN_ROUND_WINS_HOME,
				Matches.COLUMN_ROUND_WINS_AWAY},
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		listAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		listAdapter.swapCursor(null);
	}

	@Override
	public void onMatchSelected(long id) {
		Intent i = new Intent(this, ScoresheetActivity.class);

		Uri uri = Uri.withAppendedPath(Matches.CONTENT_URI,
				String.valueOf(id));

		i.setData(uri);
		startActivity(i);
	}
}
