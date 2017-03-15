package com.abby.redditgo.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;


public class RedditGoWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RedditGoViewsFactory(getApplication());
    }


}
