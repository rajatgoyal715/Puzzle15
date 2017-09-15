package com.rajatgoyal.puzzle15.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by rajat on 15/9/17.
 */

public class GameContentProvider extends ContentProvider {

    public static final String TAG = "GameContentProvider";

    public static final int HIGH_SCORES = 100;
//    public static final int HIGH_SCORE_WITH_ID = 101;

    private GameDbHelper mGameDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(GameContract.AUTHORITY, GameContract.PATH_HIGH_SCORES, HIGH_SCORES);
//        uriMatcher.addURI(GameContract.AUTHORITY, GameContract.PATH_HIGH_SCORES + "/#", HIGH_SCORE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mGameDbHelper = new GameDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mGameDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case HIGH_SCORES:
                break;
//            case HIGH_SCORE_WITH_ID:
//                String game_id = uri.getPathSegments().get(1);
//                selection = "_id=" + game_id;
//                break;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }

        retCursor = db.query(GameContract.GameEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mGameDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case HIGH_SCORES:
                long id = db.insert(GameContract.GameEntry.TABLE_NAME, null, values);
                Log.d(TAG, "insert: " + id);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(uri, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
