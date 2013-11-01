package drj.scoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ScoringFragment extends DialogFragment {

	public interface ScoringListener {
		public void onScorePicked(int homeViewId, CharSequence homeScore,
				int awayViewId, CharSequence awayScore, boolean ero);
	}
	
	static final String HOME_PLAYER_KEY = "home_player";
	static final String AWAY_PLAYER_KEY = "away_player";
	static final String HOME_VIEW_ID_KEY = "home_view_id";
	static final String AWAY_VIEW_ID_KEY = "away_view_id";
	
	private int homeViewId;
	private int awayViewId;
	private ScoringListener hostActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			hostActivity = (ScoringListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement ScoringListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_scoring, null);

		builder.setTitle(R.string.select_winner)
		.setView(v);

		viewClickedId = getArguments().getInt(VIEW_ID_KEY);

		return builder.create();
	}

}
