package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class MatchListFragment extends ListFragment {

	public interface MatchSelectedListener {
		public void onMatchSelected(long id);
	}

	private MatchSelectedListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (MatchSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement MatchSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (getListView().getChoiceMode()) {
		case ListView.CHOICE_MODE_MULTIPLE:
			break;
		default:
			listener.onMatchSelected(id);
		}
	}
	
	public void setSelectionMode(boolean multiSelect) {
		getListView().setChoiceMode(multiSelect ?
				ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_SINGLE);
	}
	
	public long[] getSelection() {
		return getListView().getCheckedItemIds();
	}
}
