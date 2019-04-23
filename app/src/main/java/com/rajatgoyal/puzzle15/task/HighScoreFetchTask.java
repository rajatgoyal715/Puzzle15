package com.rajatgoyal.puzzle15.task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.rajatgoyal.puzzle15.data.GameContract;
import com.rajatgoyal.puzzle15.model.GamePlay;
import com.rajatgoyal.puzzle15.model.Time;

import java.util.ArrayList;

/**
 * Created by rajat on 15/9/17.
 */

public class HighScoreFetchTask extends AsyncTask<Void, Void, ArrayList<GamePlay>> {

    private Context context;
    private static final int LIMIT = 10;

    public HighScoreFetchTask(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<GamePlay> doInBackground(Void... params) {
        Uri uri = GameContract.GameEntry.CONTENT_URI;
        String sortOrder = GameContract.GameEntry.COLUMN_SCORE + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, sortOrder);
        if(cursor == null) return null;

        ArrayList<GamePlay> gamePlays = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int moves = cursor.getInt(cursor.getColumnIndex(GameContract.GameEntry.COLUMN_MOVES));
                int time_in_seconds = cursor.getInt(cursor.getColumnIndex(GameContract.GameEntry.COLUMN_TIME));

                gamePlays.add(new GamePlay(moves, new Time(time_in_seconds)));
            } while (cursor.moveToNext() && gamePlays.size() < LIMIT);
        }

        cursor.close();
        return gamePlays;
    }
}
