package com.derekjass.poolscoresheet.provider;

import android.provider.BaseColumns;

public final class League {

	private League() {}

	public static final String AUTHORITY =
			"com.derekjass.poolscoresheet.provider";

	public static final class Match implements BaseColumns {

		private Match() {}

		public static final String TABLE_NAME = "match";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_HOME_TEAM = "hometeam";
		public static final String COLUMN_AWAY_TEAM = "awayteam";
		public static final String COLUMN_NAME_HOME_PLAYERS = "homeplayers";
		public static final String COLUMN_NAME_AWAY_PLAYERS = "awayplayers";
		public static final String COLUMN_AVE_HOME_PLAYERS = "homeaves";
		public static final String COLUMN_AVE_AWAY_PLAYERS = "awayaves";
		public static final String COLUMN_SCORES_HOME_PLAYERS = "homescores";
		public static final String COLUMN_SCORES_AWAY_PLAYERS = "awayscores";
		public static final String COLUMN_HOME_ERO_FLAGS = "homeeroflags";
		public static final String COLUMN_AWAY_ERO_FLAGS = "awayeroflags";
	}
}
