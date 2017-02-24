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

import static com.abby.redditgo.job.SubmissionFetchJob.FetchSubreddit.ALL;
import static com.abby.redditgo.job.SubmissionFetchJob.FetchSubreddit.FRONT_PAGE;
import static com.abby.redditgo.job.SubmissionFetchJob.FetchSubreddit.OTHER;
import static net.dean.jraw.auth.AuthenticationState.NONE;

/**
 * Created by gsshop on 2016. 11. 1..
 */

public class SubmissionFetchJob extends BaseJob {

    public enum FetchSubreddit {
        FRONT_PAGE,
        ALL,
        OTHER,
    }

    ;


    private final FetchSubreddit mFetchSubreddit;
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

        if (subreddit == null) {
            this.mFetchSubreddit = FRONT_PAGE;
        } else if (subreddit.equals("all")) {
            this.mFetchSubreddit = ALL;
        } else {
            this.mFetchSubreddit = OTHER;
        }

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
            }
        } catch (NetworkException e) {
            EventBus.getDefault().post(new SubmissionErrorEvent(e.getMessage()));
        }

    }

    private int rank = 0;

    static class RankSubmission {
        final Submission submission;
        final int rank;

        RankSubmission(Submission submission, int rank) {
            this.submission = submission;
            this.rank = rank;
        }
    }

    private void updateSubmissionTable(List<Submission> submissions, int pageIndex) {
        final ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        // Build hash table of incoming entries
        final HashMap<String, RankSubmission> entryMap = new HashMap<>();
        for (Submission e : submissions) {
            entryMap.put(e.getId(), new RankSubmission(e, rank++));
        }

        Cursor cursor = mContentResolver.query(contentUri(), null, null, null, null);
        Preconditions.checkNotNull(cursor);

        Logger.i("Found " + cursor.getCount() + " local entries. Computing merge solution...");
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(SubmissionColumn.ID));
            RankSubmission match = entryMap.get(id);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(id);
                // update

                String title = cursor.getString(cursor.getColumnIndex(SubmissionColumn.TITLE));
                String vote = cursor.getString(cursor.getColumnIndex(SubmissionColumn.VOTE));
                int score = cursor.getInt(cursor.getColumnIndex(SubmissionColumn.SCORE));
                int count = cursor.getInt(cursor.getColumnIndex(SubmissionColumn.NUM_COMMENTS));
                int rank = cursor.getInt(cursor.getColumnIndex(SubmissionColumn.RANK));
                Uri existingUri = withId(id);
                Submission submission = match.submission;
                if (rank != match.rank || !title.equals(submission.getTitle()) || !vote.equals(submission.getVote().name()) || score != submission.getScore() || count != submission.getCommentCount()) {
                    Logger.i("Scheduling update: " + existingUri);
                    batchOperations.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(SubmissionColumn.RANK, match.rank)
                            .withValue(SubmissionColumn.POST_HINT, submission.getPostHint().name())
                            .withValue(SubmissionColumn.URL, submission.getUrl())
                            .withValue(SubmissionColumn.TITLE, submission.getTitle())
                            .withValue(SubmissionColumn.VOTE, submission.getVote().name())
                            .withValue(SubmissionColumn.CREATED_TIME, submission.getCreated().getTime())
                            .withValue(SubmissionColumn.AUTHOR, submission.getAuthor())
                            .withValue(SubmissionColumn.SUBREDDIT, submission.getSubredditName())
                            .withValue(SubmissionColumn.SCORE, submission.getScore())
                            .withValue(SubmissionColumn.NUM_COMMENTS, submission.getCommentCount())
                            .withValue(SubmissionColumn.THUMBNAIL, submission.getThumbnail())
                            .build());
                } else {
                    Logger.i("No action: " + existingUri);
                }
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

        for (RankSubmission entry : entryMap.values()) {
            Submission submission = entry.submission;
            Logger.i("Scheduling insert: entry_id=" + submission.getId());
            batchOperations.add(ContentProviderOperation.newInsert(contentUri())
                    .withValue(SubmissionColumn.ID, submission.getId())
                    .withValue(SubmissionColumn.SORTING, mSorting.name())
                    .withValue(SubmissionColumn.RANK, entry.rank)
                    .withValue(SubmissionColumn.POST_HINT, submission.getPostHint().name())
                    .withValue(SubmissionColumn.URL, submission.getUrl())
                    .withValue(SubmissionColumn.TITLE, submission.getTitle())
                    .withValue(SubmissionColumn.VOTE, submission.getVote().name())
                    .withValue(SubmissionColumn.CREATED_TIME, submission.getCreated().getTime())
                    .withValue(SubmissionColumn.AUTHOR, submission.getAuthor())
                    .withValue(SubmissionColumn.SUBREDDIT, submission.getSubredditName())
                    .withValue(SubmissionColumn.SCORE, submission.getScore())
                    .withValue(SubmissionColumn.NUM_COMMENTS, submission.getCommentCount())
                    .withValue(SubmissionColumn.THUMBNAIL, submission.getThumbnail())
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
        switch (mFetchSubreddit) {
            case FRONT_PAGE:
                return RedditgoProvider.FrontPage.withSorting(mSorting.name());
            case ALL:
                return RedditgoProvider.All.withSorting(mSorting.name());
            case OTHER:
                return RedditgoProvider.Submission.withSubredditAndSorting(mSubreddit, mSorting.name());
        }
        return null;
    }

    Uri withId(String id) {
        switch (mFetchSubreddit) {
            case FRONT_PAGE:
                return RedditgoProvider.FrontPage.withId(mSorting.name(), id);
            case ALL:
                return RedditgoProvider.All.withId(mSorting.name(), id);
            case OTHER:
                return RedditgoProvider.Submission.withId(mSubreddit, mSorting.name(), id);
        }
        return null;
    }

    @Override
    public void inject(ApplicationComponent component) {
        component.inject(this);
    }
}
