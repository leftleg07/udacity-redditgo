package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.CommentComposeEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentReplyCommentJob extends Job {

    private final String replyText;
    private final String fullname;

    public CommentReplyCommentJob(String fullname, String replyText) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));
        this.fullname =fullname;
        this.replyText = replyText;
    }

    @Override
    public void onRun() throws Throwable {
        try {
            RedditApi.replyComment(fullname, replyText);
            EventBus.getDefault().post(new CommentComposeEvent(null));
        } catch (ApiException e) {
            EventBus.getDefault().post(new CommentComposeEvent(e.getMessage()));
        } catch (NetworkException e) {
            EventBus.getDefault().post(new CommentComposeEvent(e.getMessage()));
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
