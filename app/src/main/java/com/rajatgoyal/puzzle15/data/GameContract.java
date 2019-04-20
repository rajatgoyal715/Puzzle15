package com.rajatgoyal.puzzle15.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.rajatgoyal.puzzle15.BuildConfig;

/**
 * Created by rajat on 15/9/17.
 */

public class GameContract {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_HIGH_SCORES = "high_score";

    public static final class GameEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HIGH_SCORES).build();

        public static final String TABLE_NAME = "high_score";

        public static final String COLUMN_MOVES = "moves";
        public static final String COLUMN_TIME = "time";

    }
}
