package com.derekjass.poolscoresheet;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class ScoresheetActivity extends Activity {

	private Uri mMatchUri;
	private ScoresheetFragment mScoresheet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		mMatchUri = getIntent().getData();

		if (mMatchUri == null) {
			mMatchUri = getContentResolver().insert(Matches.CONTENT_URI, null);
		}

		mScoresheet = (ScoresheetFragment) getFragmentManager()
				.findFragmentById(R.id.scoresheetFragment);

		mScoresheet.loadUri(mMatchUri);
	}
}
