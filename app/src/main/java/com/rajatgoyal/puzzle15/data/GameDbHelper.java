package com.rajatgoyal.puzzle15.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rajatgoyal.puzzle15.data.GameContract.GameEntry;

/**
 * Created by rajat on 15/9/17.
 */

public class GameDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "puzzle15_game.db";
    private static final int DATABASE_VERSION = 1;

    GameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_QUERY = " CREATE TABLE " + GameEntry.TABLE_NAME + " ( " +
                GameEntry._ID + " INTEGER PRIMARY KEY, " +
                GameEntry.COLUMN_SCORE + " INTEGER NOT NULL, " +
                GameEntry.COLUMN_MOVES + " INTEGER NOT NULL, " +
                GameEntry.COLUMN_TIME + " INTEGER NOT NULL " + ");";
        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GameEntry.TABLE_NAME);
        onCreate(db);
    }
}
