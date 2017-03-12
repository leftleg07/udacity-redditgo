package com.abby.redditgo.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abby.redditgo.event.AccountEvent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import net.dean.jraw.models.LoggedInAccount;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * get the current logged in account
 */

public class AccountJob extends Job {


    public AccountJob() {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(Priority.MID).requireNetwork().singleInstanceBy(UUID.randomUUID().toString()));
    }

    @Override
    public void onRun() throws Throwable {
        LoggedInAccount account = RedditApi.account();
        EventBus.getDefault().post(new AccountEvent(account));

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
