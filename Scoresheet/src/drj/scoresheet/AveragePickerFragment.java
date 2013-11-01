package drj.scoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AveragePickerFragment extends DialogFragment {

	public interface AveragePickerListener {
		public void onAveragePicked(int viewId, CharSequence avg);
	}

	static final String NAME_KEY = "name";
	static final String VIEW_ID_KEY = "view_id";
	
	private int viewClickedId;
	private AveragePickerListener hostActivity;
	private OnClickListener buttonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			hostActivity.onAveragePicked(viewClickedId,
					((TextView) v).getText());
			dismiss();
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			hostActivity = (AveragePickerListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement AveragePickerListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_average, null);
		setupListeners(v);
		TextView name = (TextView) v.findViewById(R.id.dialog_name);
		name.setText(getArguments().getString(NAME_KEY));

		builder.setTitle(R.string.assign_average)
		.setView(v);

		viewClickedId = getArguments().getInt(VIEW_ID_KEY);

		return builder.create();
	}

	private void setupListeners(View v) {
		LinearLayout buttons =
				(LinearLayout) v.findViewById(R.id.dialog_ave_buttons1);

		for (int i = 0; i < buttons.getChildCount(); i++) {
			buttons.getChildAt(i).setOnClickListener(buttonListener);
		}

		buttons = (LinearLayout) v.findViewById(R.id.dialog_ave_buttons2);

		for (int i = 0; i < buttons.getChildCount(); i++) {
			buttons.getChildAt(i).setOnClickListener(buttonListener);
		}
	}
}
