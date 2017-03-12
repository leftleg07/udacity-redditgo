package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.CommentErrorEvent;
import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.TraversalMethod;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * get the submission comment
 */
public class CommentFetchJob extends Job {

    private final String submissionId;
    private final CommentSort sort;

    public CommentFetchJob(String submissionId, CommentSort sort) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().addTags(JobId.COMMENT_FETCH_ID));
        this.submissionId = submissionId;
        this.sort = sort;
    }

    @Override
    public void onRun() throws Throwable {
        try {
            CommentNode node = RedditApi.comments(submissionId, sort);

            EventBus.getDefault().post(new CommentEvent(node.getChildren(), true));

            RedditApi.loadFully(node);
            // Load this node's comments first
            while (!isCancelled() && node.hasMoreComments()) {
                List<CommentNode> nodes = RedditApi.moreComments(node);
                for (CommentNode parent : nodes) {
                    // Load the children's comments next
                    for (CommentNode child : parent.walkTree(TraversalMethod.BREADTH_FIRST)) {
                        if (isCancelled()) {
                            return;
                        }
                        while (!isCancelled() && child.hasMoreComments()) {
                            RedditApi.moreComments(child);
                        }

                    }
                }

                EventBus.getDefault().post(new CommentEvent(nodes, false));


            }
        } catch (NetworkException e) {
            EventBus.getDefault().post(new CommentErrorEvent(e.getMessage()));
            return;
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
