package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.CommentReplyEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.models.Comment;
import net.dean.jraw.util.JrawUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentReplyCommentJob extends Job {

    private final String replyText;
    private final Comment replyTo;

    public CommentReplyCommentJob(Comment replyTo, String replyText) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));
        this.replyTo=replyTo;
        this.replyText = replyText;
    }

    @Override
    public void onRun() throws Throwable {
        String newCommentId = RedditApi.replyComment(replyTo, replyText);
        if(JrawUtils.isFullname("t1_" + newCommentId)) {
            EventBus.getDefault().post(new CommentReplyEvent(newCommentId, null));
        } else {
            EventBus.getDefault().post(new CommentReplyEvent(null, newCommentId));
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
