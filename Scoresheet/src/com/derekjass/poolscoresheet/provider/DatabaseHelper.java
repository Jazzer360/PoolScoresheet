package com.derekjass.poolscoresheet.provider;

import android.content.Context;
import static com.derekjass.poolscoresheet.provider.League.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "League.db";

	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_MATCHES =
			"CREATE TABLE " + Match.TABLE_NAME + " (" +
					League.Match._ID + " INTEGER PRIMARY KEY," +
					League.Match.COLUMN_NAME_DATE + " TEXT" + COMMA_SEP +
					League.Match.COLUMN + " TEXT" + COMMA_SEP +
					" )";

	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + League.Match.TABLE_NAME;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_MATCHES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
