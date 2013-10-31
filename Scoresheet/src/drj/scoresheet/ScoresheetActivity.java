package drj.scoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScoresheetActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoresheet);

		initializeLinks();
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
		TextView clickedBox = (TextView) v;
		clickedBox.setText("10");
		TextView otherBox = (TextView) clickedBox.getTag();
		otherBox.setText("7");
	}

	public void onAverageBoxClicked(View v) {
		Bundle args = new Bundle();
		ViewGroup parent = (ViewGroup) v.getParent();
		TextView name = (TextView) parent.getChildAt(1);
		args.putString("name", name.getText().toString());
		
		AveragePickerFragment dialog = new AveragePickerFragment();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "AverageDialog");
	}

	private void linkScoreBoxes(int homeResId, int awayResId) {
		View home = findViewById(homeResId);
		View away = findViewById(awayResId);

		home.setTag(away);
		away.setTag(home);
	}

	private void initializeLinks() {
		linkScoreBoxes(R.id.homePlayer1Round1, R.id.awayPlayer1Round1);
		linkScoreBoxes(R.id.homePlayer2Round1, R.id.awayPlayer2Round1);
		linkScoreBoxes(R.id.homePlayer3Round1, R.id.awayPlayer3Round1);
		linkScoreBoxes(R.id.homePlayer4Round1, R.id.awayPlayer4Round1);
		linkScoreBoxes(R.id.homePlayer5Round1, R.id.awayPlayer5Round1);
		linkScoreBoxes(R.id.homePlayer1Round2, R.id.awayPlayer2Round2);
		linkScoreBoxes(R.id.homePlayer2Round2, R.id.awayPlayer4Round2);
		linkScoreBoxes(R.id.homePlayer3Round2, R.id.awayPlayer1Round2);
		linkScoreBoxes(R.id.homePlayer4Round2, R.id.awayPlayer5Round2);
		linkScoreBoxes(R.id.homePlayer5Round2, R.id.awayPlayer3Round2);
		linkScoreBoxes(R.id.homePlayer1Round3, R.id.awayPlayer3Round3);
		linkScoreBoxes(R.id.homePlayer2Round3, R.id.awayPlayer1Round3);
		linkScoreBoxes(R.id.homePlayer3Round3, R.id.awayPlayer5Round3);
		linkScoreBoxes(R.id.homePlayer4Round3, R.id.awayPlayer2Round3);
		linkScoreBoxes(R.id.homePlayer5Round3, R.id.awayPlayer4Round3);
	}
}
