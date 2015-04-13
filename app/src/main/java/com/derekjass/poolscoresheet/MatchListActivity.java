package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.derekjass.poolscoresheet.MatchListFragment.MatchListCallbacks;
import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class MatchListActivity extends Activity
implements MatchListCallbacks {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_matchlist);

		setTitle(R.string.saved_matches);
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
