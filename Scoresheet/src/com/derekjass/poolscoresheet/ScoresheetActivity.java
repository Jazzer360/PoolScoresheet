package com.derekjass.poolscoresheet;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class ScoresheetActivity extends Activity {

	private Uri matchUri;
	private ScoresheetFragment scoresheet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		matchUri = getIntent().getData();

		if (matchUri == null) {
			matchUri = getContentResolver().insert(Matches.CONTENT_URI, null);
		}

		scoresheet = (ScoresheetFragment) getFragmentManager()
				.findFragmentById(R.id.scoresheetFragment);

		scoresheet.loadUri(matchUri);
	}
}
