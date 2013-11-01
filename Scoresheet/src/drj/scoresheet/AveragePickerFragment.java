package drj.scoresheet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AveragePickerFragment extends DialogFragment {

	private Button selected = null;

	private OnClickListener buttonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (selected != null) {
				selected.setPressed(false);
			}
			v.setPressed(true);
			selected = (Button) v;
		}
	};

	public interface Listener {
		public void onAveragePicked(DialogFragment dialog);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_average, null);
		setupListeners(v);
		TextView name = (TextView) v.findViewById(R.id.dialog_name);
		name.setText(getArguments().getString(ScoresheetActivity.NAME_KEY));

		builder.setTitle(R.string.assign_average)
		.setView(v);


		return builder.create();
	}

	private void setupListeners(View v) {
		LinearLayout buttons =
				(LinearLayout) v.findViewById(R.id.dialog_ave_buttons1);

		for (int i = 0; i < buttons.getChildCount(); i++) {
			buttons.getChildAt(0).setOnClickListener(buttonListener);
		}

		buttons = (LinearLayout) v.findViewById(R.id.dialog_ave_buttons2);

		for (int i = 0; i < buttons.getChildCount(); i++) {
			buttons.getChildAt(0).setOnClickListener(buttonListener);
		}
	}
}
