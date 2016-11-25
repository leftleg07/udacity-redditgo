package com.abby.redditgo.services;

import android.support.annotation.NonNull;

import com.abby.redditgo.MainApplication;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;

import javax.inject.Inject;

/**
 * job service
 */
public class MyJobService extends FrameworkJobSchedulerService {
    @Inject JobManager jobManager;

    public MyJobService() {
        MainApplication.getInstance().getComponent().inject(this);
    }

    @NonNull
    @Override
    protected JobManager getJobManager() {
        return jobManager;
    }
}
