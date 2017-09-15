package com.rajatgoyal.puzzle15.widget;

import android.content.ContentValues;
import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rajatgoyal.puzzle15.R;

/**
 * Created by rajat on 15/9/17.
 */

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;

    public WidgetRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        remoteViews.setTextViewText(R.id.username, "rajatgoyal715");
        remoteViews.setTextViewText(R.id.moves, "Moves : 50");
        remoteViews.setTextViewText(R.id.time, "Time : 00:00:50");
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
}
