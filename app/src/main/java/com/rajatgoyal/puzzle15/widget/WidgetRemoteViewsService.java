package com.rajatgoyal.puzzle15.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by rajat on 15/9/17.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteViewsFactory(this.getApplicationContext());
    }
}
