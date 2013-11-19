package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class ScoresheetActivity extends Activity {

	private ScoresheetFragment scoresheet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		scoresheet = (ScoresheetFragment) getFragmentManager()
				.findFragmentById(R.id.scoresheetFragment);

		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this);

		if (prefs.getString("match_uri", null) == null) {
			Uri uri = getContentResolver().insert(Matches.CONTENT_URI, null);
			prefs.edit().putString("match_uri", uri.toString()).commit();
			scoresheet.loadUri(uri);
		} else {
			Uri uri = Uri.parse(prefs.getString("match_uri", null));
			scoresheet.loadUri(uri);
		}
	}
}
