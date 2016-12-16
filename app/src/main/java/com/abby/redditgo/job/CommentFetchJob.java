package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.orhanobut.logger.Logger;

import net.dean.jraw.models.CommentNode;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentFetchJob extends Job {

    private final String submissionId;

    public CommentFetchJob(String submissionId) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));
        this.submissionId = submissionId;
    }

    @Override
    public void onRun() throws Throwable {
        CommentNode node = RedditApi.comments(submissionId);
        RedditApi.loadFully(node);
//        while (comments.hasMoreComments()) {
//            RedditApi.moreComments(comments);
//        }

//        for (CommentNode child : node.walkTree()) {
//            if(!child.hasMoreComments()) {
//                break;
//            }
//                Logger.i("Child had more comments: " + child);
////            RedditApi.moreComments(child);
////            Logger.i("Child had more comments: " + child);
////            node = child;
//        }
        if(node.hasMoreComments()) {
            Logger.i("Root node had more comments: " + node);
        }

        EventBus.getDefault().post(new CommentEvent(node));
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
