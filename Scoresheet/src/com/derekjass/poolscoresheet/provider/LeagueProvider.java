package com.derekjass.poolscoresheet.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.derekjass.poolscoresheet.provider.LeagueContract.Matches;

public class LeagueProvider extends ContentProvider {

	private static final class DatabaseHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "League.db";

		private static final String SQL_CREATE_MATCHES =
				"CREATE TABLE " + Matches.TABLE_NAME + " (" +
						Matches._ID + " INTEGER PRIMARY KEY," +
						Matches.COLUMN_DATE + " INTEGER," +
						Matches.COLUMN_TEAM_HOME + " TEXT," +
						Matches.COLUMN_TEAM_AWAY + " TEXT," +
						Matches.COLUMN_PLAYER_AVES_HOME + " TEXT," +
						Matches.COLUMN_PLAYER_AVES_AWAY + " TEXT," +
						Matches.COLUMN_PLAYER_NAMES_HOME + " TEXT," +
						Matches.COLUMN_PLAYER_NAMES_AWAY + " TEXT," +
						Matches.COLUMN_PLAYER_SCORES_HOME + " TEXT," +
						Matches.COLUMN_PLAYER_SCORES_AWAY + " TEXT," +
						Matches.COLUMN_ERO_BITMASK + " INTEGER," +
						Matches.COLUMN_ROUND_WINS_HOME + " INTEGER," +
						Matches.COLUMN_ROUND_WINS_AWAY + " INTEGER)";

		private DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_MATCHES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
		}

	}

	private DatabaseHelper dbHelper;
	private static UriMatcher uriMatcher;

	private static final int URI_MATCHES = 1;
	private static final int URI_MATCH_ID = 2;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(LeagueContract.AUTHORITY,
				Matches.TABLE_NAME, URI_MATCHES);
		uriMatcher.addURI(LeagueContract.AUTHORITY,
				Matches.TABLE_NAME + "/#", URI_MATCH_ID);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String table = null;
		String where = null;
		String orderBy = TextUtils.isEmpty(sortOrder) ?
				Matches.COLUMN_DATE + " DESC" : sortOrder;

		switch (uriMatcher.match(uri)) {
		case URI_MATCHES:
			table = Matches.TABLE_NAME;
			where = selection;
			break;
		case URI_MATCH_ID:
			table = Matches.TABLE_NAME;
			where = Matches._ID + "=" + uri.getPathSegments().get(1);
			if (selection != null)
				where += " AND " + selection;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(table,
				projection,
				where,
				selectionArgs,
				null,
				null,
				orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case URI_MATCHES:
			return Matches.CONTENT_TYPE;
		case URI_MATCH_ID:
			return Matches.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		String table = null;

		switch (uriMatcher.match(uri)) {
		case URI_MATCHES:
			table = Matches.TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values = (initialValues != null ?
				new ContentValues(initialValues) : new ContentValues());

		if (!values.containsKey(Matches.COLUMN_DATE))
			values.put(Matches.COLUMN_DATE, System.currentTimeMillis());

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(table, null, values);

		if (rowId > 0) {
			Uri matchUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(matchUri, null);
			return matchUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = null;
		String where = null;

		switch (uriMatcher.match(uri)) {
		case URI_MATCHES:
			table = Matches.TABLE_NAME;
			where = selection;
			break;
		case URI_MATCH_ID:
			table = Matches.TABLE_NAME;
			where = Matches._ID + "=" + uri.getPathSegments().get(1);
			if (selection != null)
				where += selection;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsDeleted = db.delete(table, where, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String table = null;
		String where = null;

		switch (uriMatcher.match(uri)) {
		case URI_MATCHES:
			table = Matches.TABLE_NAME;
			where = selection;
			break;
		case URI_MATCH_ID:
			table = Matches.TABLE_NAME;
			where = Matches._ID + "=" + uri.getPathSegments().get(1);
			if (selection != null)
				where += " AND " + selection;
			break;
		default:
			throw new IllegalArgumentException("Uknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsUpdated = db.update(table, values, where, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return rowsUpdated;
	}

}
