package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.common.collect.Lists;
import com.orhanobut.logger.Logger;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.TraversalMethod;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentFetchJob extends Job {

    private final String submissionId;
    private int lastCommentSize = 0;

    public CommentFetchJob(String submissionId) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().addTags(JobId.COMMENT_FETCH_ID));
        this.submissionId = submissionId;
    }

    @Override
    public void onRun() throws Throwable {
        CommentNode node = RedditApi.comments(submissionId);
        EventBus.getDefault().post(new CommentEvent(node.getChildren(), lastCommentSize));
        lastCommentSize = node.getImmediateSize();

        // Load this node's comments first
        while (!isCancelled() && node.hasMoreComments()) {
            RedditApi.moreComments(node);
            for (int i = lastCommentSize; !isCancelled() && i < node.getImmediateSize(); i++) {
                CommentNode parent = node.get(i);

                // Load the children's comments next
                for (CommentNode child : parent.walkTree(TraversalMethod.BREADTH_FIRST)) {
                    if(isCancelled()) {
                        return;
                    }
                    while (!isCancelled() && child.hasMoreComments()) {
                        RedditApi.moreComments(child);
                    }

                }
            }

            List<CommentNode> nodes = Lists.newArrayList(node.getChildren().listIterator(lastCommentSize));
            EventBus.getDefault().post(new CommentEvent(nodes, lastCommentSize));

            lastCommentSize = node.getImmediateSize();

        }
    }


    void moreChildrenComment(CommentNode parent) {
        for (CommentNode node : parent.walkTree(TraversalMethod.BREADTH_FIRST)) {
            // Travel breadth first so we can accurately compare depths
            while (node.hasMoreComments()) {
                RedditApi.moreComments(node);
            }
        }
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
}
