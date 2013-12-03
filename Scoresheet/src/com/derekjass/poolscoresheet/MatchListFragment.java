package com.derekjass.poolscoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class MatchListFragment extends ListFragment
implements LoaderCallbacks<Cursor> {

	public interface MatchListCallbacks {
		public void onMatchSelected(long id);
	}

	private MatchListCallbacks mListener;
	private MatchCursorAdapter mAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (MatchListCallbacks) activity;
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);

		setEmptyText(getString(R.string.no_matches));

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
			public boolean onActionItemClicked(final ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.deleteSelection:
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.delete_warning_title)
					.setMessage(R.string.delete_warning)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							deleteSelectedItems();
							mode.finish();
						}
					})
					.setNegativeButton(android.R.string.no, null)
					.show();

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
		mListener.onMatchSelected(id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Matches.CONTENT_URI,
				new String[]{
			Matches._ID,
			Matches.COLUMN_DATE,
			Matches.COLUMN_TEAM_HOME,
			Matches.COLUMN_TEAM_AWAY,
			Matches.COLUMN_ROUND_WINS_HOME,
			Matches.COLUMN_ROUND_WINS_AWAY},
			null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (mAdapter == null) {
			mAdapter = new MatchCursorAdapter(getActivity(), data, 0);
			setListAdapter(mAdapter);
		} else {
			mAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
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
