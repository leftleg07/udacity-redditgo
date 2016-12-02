package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.SubmissionEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import static net.dean.jraw.auth.AuthenticationState.NONE;

/**
 * Created by gsshop on 2016. 11. 1..
 */

public class FetchSubmissionJob extends BaseJob {
    private final String mSubreddit;
    private final Sorting mSorting;

    @Inject
    UUID deviceId;

    /**
     * subreddit is null, fetch front page
     * @param subreddit
     * @param sorting
     */
    public FetchSubmissionJob(String subreddit, Sorting sorting) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()).addTags(JobId.FETCH_SUBMISSION_ID));
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
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if(state == NONE) {
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
            if(isCancelled()) {
                break;
            }
            EventBus.getDefault().post(new SubmissionEvent(submissions, mSorting, paginator.getPageIndex()));
        }

    }

    @Override
    public void inject(ApplicationComponent component) {
        component.inject(this);
    }
}
