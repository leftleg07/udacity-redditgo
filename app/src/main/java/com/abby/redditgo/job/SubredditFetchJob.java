package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.SubredditEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.models.Subreddit;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.UUID;

/**
 * Created by gsshop on 2016. 10. 31..
 */

public class SubredditFetchJob extends Job {

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
            EventBus.getDefault().post(new SubredditEvent(subreddits));
        }

    }

}
