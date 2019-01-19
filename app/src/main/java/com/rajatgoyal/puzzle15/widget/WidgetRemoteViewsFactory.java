package com.rajatgoyal.puzzle15.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.model.Leaderboard;
import com.rajatgoyal.puzzle15.model.Time;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by rajat on 15/9/17.
 */

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private ArrayList<Leaderboard> leaderboards;
    private static final String TAG = "WIDGET";

    public WidgetRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        fetchLeaderboard();
    }

    public void setLeaderboards(ArrayList<Leaderboard> leaderboards) {
        this.leaderboards = leaderboards;
        onDataSetChanged();
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
		Timber.tag(TAG).d("getCount: " + (leaderboards == null));
        return leaderboards == null ? 0 : leaderboards.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.leaderboard_list_item);
        Leaderboard leaderboard = leaderboards.get(position);
        remoteViews.setTextViewText(R.id.username, leaderboard.getName());
        remoteViews.setTextViewText(R.id.moves, "Moves : " + leaderboard.getMoves());
        remoteViews.setTextViewText(R.id.time, "Time : " + new Time(leaderboard.getTime()).toString());
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void fetchLeaderboard() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("high_scores");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Leaderboard> list = new ArrayList<>();

                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                int i = 1;
                for (DataSnapshot snapshot : snapshots) {
                    if (i > 20) break;
                    String name = "";
                    int moves = 0, time = 0;
                    Iterable<DataSnapshot> items = snapshot.getChildren();
                    int j = 0;
                    for (DataSnapshot item : items) {
                        if (j == 0) moves = Integer.parseInt(item.getValue().toString());
                        else if (j == 1) name = item.getValue().toString();
                        else time = Integer.parseInt(item.getValue().toString());
                        j++;
                    }
                    list.add(new Leaderboard(name, moves, time));
                    i++;
                }

                setLeaderboards(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
