package com.derekjass.poolscoresheet.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class LeagueContract {

	private LeagueContract() {}

	static final String AUTHORITY =
			"com.derekjass.poolscoresheet.provider";

	public static interface MatchesColumns {
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_TEAM_HOME = "hometeam";
		public static final String COLUMN_TEAM_AWAY = "awayteam";
		public static final String COLUMN_PLAYER_NAMES_HOME = "homeplayers";
		public static final String COLUMN_PLAYER_NAMES_AWAY = "awayplayers";
		public static final String COLUMN_PLAYER_AVES_HOME = "homeaves";
		public static final String COLUMN_PLAYER_AVES_AWAY = "awayaves";
		public static final String COLUMN_PLAYER_SCORES_HOME = "homescores";
		public static final String COLUMN_PLAYER_SCORES_AWAY = "awayscores";
		public static final String COLUMN_ERO_BITMASK = "erobitmask";
		public static final String COLUMN_ROUND_WINS_HOME = "homeroundwins";
		public static final String COLUMN_ROUND_WINS_AWAY = "awayroundwins";
	}

	public static final class Matches implements BaseColumns, MatchesColumns {

		private Matches() {}

		static final String TABLE_NAME = "match";
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/vnd.com.derekjass.provider." +
						TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/vnd.com.derekjass.provider." +
						TABLE_NAME;
		public static final Uri CONTENT_URI = Uri.parse(
				"content://" + AUTHORITY + "/" + TABLE_NAME);
	}
}
