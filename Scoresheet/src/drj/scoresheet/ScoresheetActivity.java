package drj.scoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class ScoresheetActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Closing Activity")
		.setMessage("Are you sure you want to close this activity? " +
		"Doing so will clear the score boxes.")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ScoresheetActivity.super.onBackPressed();
			}

		})
		.setNegativeButton("No", null)
		.show();
	}

	public void onScoreBoxClicked(View v) {

	}

	public void onAverageBoxClicked(View v) {

	}
}
