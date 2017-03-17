package com.abby.redditgo.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.abby.redditgo.R;
import com.abby.redditgo.data.RedditgoProvider;
import com.abby.redditgo.data.SubmissionColumn;

import javax.inject.Inject;

public class RedditGoViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    protected static final String TAG = RedditGoViewsFactory.class.getSimpleName();

    private Cursor mCursor;

    private final Context mContext;

    @Inject
    public RedditGoViewsFactory(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {


    }

    @Override
    public void onDataSetChanged() {
        //If exist, get a fresh copy
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(RedditgoProvider.FrontPage.CONTENT_URI, null, null, null, null);

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Move to position first
        if (!mCursor.moveToPosition(position)) {
            return null;
        }

        //Doc: https://developer.android.com/reference/android/widget/RemoteViews.html
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget_submission);

        final String url = mCursor.getString(mCursor.getColumnIndex(SubmissionColumn.URL));
        final String title = mCursor.getString(mCursor.getColumnIndex(SubmissionColumn.TITLE));

        //Set Symbol
        views.setTextViewText(R.id.textView_title, title);

        Intent fillInIntent = new Intent();
        Uri uri = Uri.parse(url);
        fillInIntent.setData(uri);
        views.setOnClickFillInIntent(R.id.textView_title, fillInIntent); //R.id.stock_list_item is the container main container in list view item layout
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; //We only have 1 type
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null &&
                mCursor.moveToPosition(position)) {
            return mCursor.getLong(0); //Always first column
        }

        return position; //I think
    }

    @Override
    public boolean hasStableIds() {
        return true; //Since its from db and id used is row id
    }
}
