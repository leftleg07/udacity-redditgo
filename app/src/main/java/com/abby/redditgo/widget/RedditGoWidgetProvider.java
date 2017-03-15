package com.abby.redditgo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.abby.redditgo.R;
import com.abby.redditgo.ui.detail.DetailActivity;


/**
 * Created by bundee on 8/24/16.
 * Doc: https://developer.android.com/guide/topics/appwidgets/index.html#AppWidgetProvider
 */
public class RedditGoWidgetProvider extends AppWidgetProvider {


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            //Assign widget layout
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_redditgo);

            //Set adapter to listView
            views.setRemoteAdapter(R.id.widget_stock_list, new Intent(context, RedditGoWidgetService.class));

            //Use intent template. Recreate the stack that i want
            Intent clickIntentTemplate = new Intent(context, DetailActivity.class);

            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_stock_list, clickPendingIntentTemplate);

            //Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
