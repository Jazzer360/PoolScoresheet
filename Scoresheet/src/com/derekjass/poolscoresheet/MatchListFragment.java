package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class MatchListFragment extends ListFragment {

	public interface MatchListCallbacks {
		public void onMatchSelected(long id);
	}

	private MatchListCallbacks listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (MatchListCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement MatchSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				mode.setTitle(String.valueOf(
						getListView().getCheckedItemCount()) + " " +
						getString(R.string.selected));
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.deleteSelection:
					deleteSelectedItems();
					mode.finish();
					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.matchlist_context_menu, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.matchlist_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionNew:
			ContentResolver cr = getActivity().getContentResolver();
			cr.insert(Matches.CONTENT_URI, null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.onMatchSelected(id);
	}

	private void deleteSelectedItems() {
		ContentResolver cr = getActivity().getContentResolver();
		long[] ids = getListView().getCheckedItemIds();

		StringBuilder sb = new StringBuilder();
		sb.append(Matches._ID);
		sb.append(" IN (");
		for (long id : ids) {
			sb.append(id);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");

		cr.delete(Matches.CONTENT_URI, sb.toString(), null);
	}
}
