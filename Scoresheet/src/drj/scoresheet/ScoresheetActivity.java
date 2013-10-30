package drj.scoresheet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ScoresheetActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_scoresheet);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	public void onScoreBoxClicked(View v) {
		
	}
	
	public void onAverageBoxClicked(View v) {
		
	}
}
