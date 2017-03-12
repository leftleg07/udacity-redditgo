package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.CommentErrorEvent;
import com.abby.redditgo.event.CommentRefreshEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * delete comment that the authenticated user posted.
 */
public class CommentDeleteJob extends Job {

    private final String fullname;

    public CommentDeleteJob(String fullname) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));
        this.fullname = fullname;
    }

    @Override
    public void onRun() throws Throwable {
        try {
            RedditApi.deleteComment(fullname);
            EventBus.getDefault().post(new CommentRefreshEvent());
        } catch (ApiException e) {
            EventBus.getDefault().post(new CommentErrorEvent(e.getMessage()));
        } catch (NetworkException e) {
            EventBus.getDefault().post(new CommentErrorEvent(e.getMessage()));
        }
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
}
