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
import com.abby.redditgo.data.SubmissionColumn;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.SubmissionErrorEvent;
import com.abby.redditgo.event.SubmissionEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.internal.Preconditions;

import static net.dean.jraw.auth.AuthenticationState.NONE;

/**
 * Created by gsshop on 2016. 11. 1..
 */

public class SubmissionFetchJob extends BaseJob {
    private final String mSubreddit;
    private final Sorting mSorting;

    @Inject
    ContentResolver mContentResolver;

    @Inject
    UUID deviceId;


    /**
     * subreddit is null, fetch front page
     *
     * @param subreddit
     * @param sorting
     */
    public SubmissionFetchJob(String subreddit, Sorting sorting) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()).addTags(JobId.SUBMISSION_FETCH_ID));
        this.mSubreddit = subreddit;
        this.mSorting = sorting;
    }

    @Override
    public void onAdded() {

    }


    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.i("onCancel");

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    @Override
    public void onRun() throws Throwable {
        try {
            AuthenticationState state = AuthenticationManager.get().checkAuthState();
            if (state == NONE) {
                RedditApi.anonymous(deviceId);
            }

            RedditClient reddit = AuthenticationManager.get().getRedditClient();
            SubredditPaginator paginator = new SubredditPaginator(reddit, mSubreddit);
            paginator.setSorting(mSorting);
            while (paginator.hasNext()) {
                Listing<Submission> submissions = paginator.next();
                List<Submission> latestSubmissions = new ArrayList<>();
                for (Submission submission : submissions) {
                    if (!submission.isNsfw()) {
                        latestSubmissions.add(submission);
                    }
                }
                if (isCancelled()) {
                    break;
                }

                // front page, all
                updateSubmissionTable(submissions, paginator.getPageIndex());
                EventBus.getDefault().post(new SubmissionEvent(submissions, mSorting, paginator.getPageIndex()));
            }
        } catch (NetworkException e) {
            EventBus.getDefault().post(new SubmissionErrorEvent(e.getMessage()));
        }

    }

    private void updateSubmissionTable(List<Submission> submissions, int pageIndex) {
        final ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        // Build hash table of incoming entries
        final HashMap<String, Submission> entryMap = new HashMap<>();
        for (Submission e : submissions) {
            entryMap.put(e.getId(), e);
        }

        Cursor cursor = mContentResolver.query(contentUri(), null, null, null, null);
        Preconditions.checkNotNull(cursor);

        Logger.i("Found " + cursor.getCount() + " local entries. Computing merge solution...");
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(SubmissionColumn.ID));
            Submission match = entryMap.get(id);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(id);
                // update
                Uri existingUri = withId(id);
                Logger.i("Scheduling update: " + existingUri);
                batchOperations.add(ContentProviderOperation.newUpdate(existingUri)
                        .withValue(SubmissionColumn.POST_HINT, match.getPostHint().name())
                        .withValue(SubmissionColumn.URL, match.getUrl())
                        .withValue(SubmissionColumn.TITLE, match.getTitle())
                        .withValue(SubmissionColumn.VOTE, match.getVote().name())
                        .withValue(SubmissionColumn.CREATED_TIME, match.getCreated().getTime())
                        .withValue(SubmissionColumn.AUTHOR, match.getAuthor())
                        .withValue(SubmissionColumn.SUBREDDIT, match.getSubredditName())
                        .withValue(SubmissionColumn.SCORE, match.getScore())
                        .withValue(SubmissionColumn.NUM_COMMENTS, match.getCommentCount())
                        .build());
            } else {
                // delete
                Uri deleteUri = withId(id);
                Logger.i("Scheduling delete: " + deleteUri);
                if (pageIndex == 1) {
                    batchOperations.add(ContentProviderOperation.newDelete(deleteUri).build());
                }
            }
        }

        cursor.close();

        for (Submission entry : entryMap.values()) {
            Logger.i("Scheduling insert: entry_id=" + entry.getId());
            batchOperations.add(ContentProviderOperation.newInsert(contentUri())
                    .withValue(SubmissionColumn.ID, entry.getId())
                    .withValue(SubmissionColumn.POST_HINT, entry.getPostHint().name())
                    .withValue(SubmissionColumn.URL, entry.getUrl())
                    .withValue(SubmissionColumn.TITLE, entry.getTitle())
                    .withValue(SubmissionColumn.VOTE, entry.getVote().name())
                    .withValue(SubmissionColumn.CREATED_TIME, entry.getCreated().getTime())
                    .withValue(SubmissionColumn.AUTHOR, entry.getAuthor())
                    .withValue(SubmissionColumn.SUBREDDIT, entry.getSubredditName())
                    .withValue(SubmissionColumn.SCORE, entry.getScore())
                    .withValue(SubmissionColumn.NUM_COMMENTS, entry.getCommentCount())
                    .build());

        }

        Logger.i("Merge solution ready. Applying batch update");

        try {
            if (batchOperations.size() > 0) {
                mContentResolver.applyBatch(RedditgoContract.AUTHORITY, batchOperations);
                Uri uri = contentUri();
                mContentResolver.notifyChange(contentUri(), null, false);
            }
        } catch (RemoteException | OperationApplicationException e) {
            Logger.e("Error applying batch insert", e);
        }
    }

    Uri contentUri() {
        switch (mSorting) {
            case HOT:
                return mSubreddit == null ? RedditgoProvider.FrontPageHot.CONTENT_URI : (mSubreddit.equals("all") ? RedditgoProvider.AllHot.CONTENT_URI : RedditgoProvider.SubmissionHot.withSubreddit(mSubreddit));
            case NEW:
                return mSubreddit == null ? RedditgoProvider.FrontPageNew.CONTENT_URI : (mSubreddit.equals("all") ? RedditgoProvider.AllNew.CONTENT_URI : RedditgoProvider.SubmissionNew.withSubreddit(mSubreddit));
            case RISING:
                return mSubreddit == null ? RedditgoProvider.FrontPageRising.CONTENT_URI : (mSubreddit.equals("all") ? RedditgoProvider.AllRising.CONTENT_URI : RedditgoProvider.SubmissionRising.withSubreddit(mSubreddit));
            case CONTROVERSIAL:
                return mSubreddit == null ? RedditgoProvider.FrontPageControversal.CONTENT_URI : (mSubreddit.equals("all") ? RedditgoProvider.AllControversal.CONTENT_URI : RedditgoProvider.SubmissionControversal.withSubreddit(mSubreddit));
            case TOP:
                return mSubreddit == null ? RedditgoProvider.FrontPageTop.CONTENT_URI : (mSubreddit.equals("all") ? RedditgoProvider.AllTop.CONTENT_URI : RedditgoProvider.SubmissionTop.withSubreddit(mSubreddit));
        }
        return null;
    }

    Uri withId(String id) {
        switch (mSorting) {
            case HOT:
                return mSubreddit == null ? RedditgoProvider.FrontPageHot.withId(id) : (mSubreddit.equals("all") ? RedditgoProvider.AllHot.withId(id) : RedditgoProvider.SubmissionHot.withId(id));
            case NEW:
                return mSubreddit == null ? RedditgoProvider.FrontPageNew.withId(id) : (mSubreddit.equals("all") ? RedditgoProvider.AllNew.withId(id) : RedditgoProvider.SubmissionNew.withId(id));
            case RISING:
                return mSubreddit == null ? RedditgoProvider.FrontPageRising.withId(id) : (mSubreddit.equals("all") ? RedditgoProvider.AllRising.withId(id) : RedditgoProvider.SubmissionRising.withId(id));
            case CONTROVERSIAL:
                return mSubreddit == null ? RedditgoProvider.FrontPageControversal.withId(id) : (mSubreddit.equals("all") ? RedditgoProvider.AllControversal.withId(id) : RedditgoProvider.SubmissionControversal.withId(id));
            case TOP:
                return mSubreddit == null ? RedditgoProvider.FrontPageTop.withId(id) : (mSubreddit.equals("all") ? RedditgoProvider.AllTop.withId(id) : RedditgoProvider.SubmissionTop.withId(id));
        }
        return null;
    }

    @Override
    public void inject(ApplicationComponent component) {
        component.inject(this);
    }
}
