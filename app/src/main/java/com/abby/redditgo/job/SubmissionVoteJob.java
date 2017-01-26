package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.SubmissionErrorEvent;
import com.abby.redditgo.event.VoteEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * Created by gsshop on 2016. 10. 31..
 */

public class SubmissionVoteJob extends Job {


    private final VoteDirection voteDirection;
    private final String submissionId;

    public SubmissionVoteJob(String submissionId, VoteDirection voteDirection) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));
        this.submissionId = submissionId;
        this.voteDirection = voteDirection;
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
        try {
            Submission newItem = RedditApi.vote(submissionId, voteDirection);
            EventBus.getDefault().post(new VoteEvent(newItem));
        } catch (NetworkException e) {
            EventBus.getDefault().post(new SubmissionErrorEvent(e.getMessage()));
        }

    }

}
