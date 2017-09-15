package com.rajatgoyal.puzzle15.task;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.rajatgoyal.puzzle15.data.GameContract;
import com.rajatgoyal.puzzle15.model.HighScore;
import com.rajatgoyal.puzzle15.model.Time;

import java.util.ArrayList;

/**
 * Created by rajat on 15/9/17.
 */

public class HighScoreFetchTask extends AsyncTask<Void, Void, ArrayList<HighScore>>{

    private Context context;

    public HighScoreFetchTask(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<HighScore> doInBackground(Void... params) {
        Uri uri = GameContract.GameEntry.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        int count = cursor.getCount();

        if (count == 0) {
            return null;
        }

        ArrayList<HighScore> highScores = new ArrayList<>(cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                int moves = cursor.getInt(cursor.getColumnIndex(GameContract.GameEntry.COLUMN_MOVES));
                int time_in_seconds = cursor.getInt(cursor.getColumnIndex(GameContract.GameEntry.COLUMN_TIME));

                highScores.add(new HighScore(moves, new Time(time_in_seconds)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return highScores;
    }
}
