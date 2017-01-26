package com.abby.redditgo.job;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.data.RedditgoContract;
import com.abby.redditgo.data.RedditgoProvider;
import com.abby.redditgo.data.SubredditColumn;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.orhanobut.logger.Logger;

import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.internal.Preconditions;

/**
 * Created by gsshop on 2016. 10. 31..
 */

public class SubredditFetchJob extends BaseJob {

    @Inject
    ContentResolver mContentResolver;

    public SubredditFetchJob() {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));

    }

    @Override
    public void onAdded() {

    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }


    @Override
    public void onRun() throws Throwable {
        if (RedditApi.isAuthorized()) {
            List<Subreddit> subreddits = RedditApi.fetchSubreddits();
            updateSubredditTable(subreddits);
        }

    }

    private void updateSubredditTable(List<Subreddit> subreddits) {
        final ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        // Build hash table of incoming entries
        final HashMap<String, Subreddit> entryMap = new HashMap<>();
        for (Subreddit e : subreddits) {
            entryMap.put(e.getId(), e);
        }

        Cursor cursor = mContentResolver.query(RedditgoProvider.Subreddit.CONTENT_URI, null, null, null, null);
        Preconditions.checkNotNull(cursor);

        Logger.i("Found " + cursor.getCount() + " local entries. Computing merge solution...");

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(SubredditColumn.ID));
            Subreddit match = entryMap.get(id);
            if (match != null) {
                entryMap.remove(id);
                String displayName = cursor.getString(cursor.getColumnIndex(SubredditColumn.DISPLAY_NAME));
                Uri existingUri = RedditgoProvider.Subreddit.withId(id);
                if (!displayName.equals(match.getDisplayName())) {
                    //updation
                    // Entry exists. Remove from entry map to prevent insert later.
                    // update
                    Logger.i("Scheduling update: " + existingUri);
                    batchOperations.add(ContentProviderOperation.newUpdate(existingUri).withValue(SubredditColumn.DISPLAY_NAME, match.getDisplayName()).build());

                } else {
                    Logger.i("No action: " + existingUri);
                }
            } else {
                // deletion
                Uri deleteUri = RedditgoProvider.Subreddit.withId(id);
                Logger.i("Scheduling delete: " + deleteUri);
                batchOperations.add(ContentProviderOperation.newDelete(deleteUri).build());
            }

        }

        cursor.close();

        // insertion
        for (Subreddit entry : entryMap.values()) {
            Logger.i("Scheduling insert: entry_id=" + entry.getId());
            batchOperations.add(ContentProviderOperation.newInsert(RedditgoProvider.Subreddit.CONTENT_URI)
                    .withValue(SubredditColumn.ID, entry.getId())
                    .withValue(SubredditColumn.DISPLAY_NAME, entry.getDisplayName())
                    .build());
        }

        Logger.i("Merge solution ready. Applying batch update");
        try {
            if (true || batchOperations.size() > 0) {
                mContentResolver.applyBatch(RedditgoContract.AUTHORITY, batchOperations);
                mContentResolver.notifyChange(RedditgoProvider.Subreddit.CONTENT_URI, null, false);
            }
        } catch (RemoteException | OperationApplicationException e) {
            Logger.e("Error applying batch insert", e);
        }
    }

    @Override
    public void inject(ApplicationComponent component) {
        component.inject(this);
    }
}
