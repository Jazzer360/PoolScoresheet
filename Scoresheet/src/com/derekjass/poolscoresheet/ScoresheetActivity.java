package com.derekjass.poolscoresheet;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class ScoresheetActivity extends Activity {

	public static final String EXTRA_MATCH_URI =
			"com.derekjass.poolscoresheet.EXTRA_MATCH_URI";

	private Uri matchUri;
	private ScoresheetFragment scoresheet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		Bundle extras = getIntent().getExtras();

		if (extras == null) {
			matchUri = getContentResolver().insert(Matches.CONTENT_URI, null);
		} else {
			matchUri = extras.getParcelable(EXTRA_MATCH_URI);
		}

		scoresheet = (ScoresheetFragment) getFragmentManager()
				.findFragmentById(R.id.scoresheetFragment);

		scoresheet.loadUri(matchUri);
	}
}
