package com.abby.redditgo.services;

import android.support.annotation.NonNull;

import com.abby.redditgo.MainApplication;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;

import javax.inject.Inject;

/**
 * gcm task service
 */
public class MyGcmJobService extends GcmJobSchedulerService {
    @Inject
    JobManager jobManager;

    public MyGcmJobService() {
        MainApplication.getInstance().getComponent().inject(this);
    }

    @NonNull
    @Override
    protected JobManager getJobManager() {
        return jobManager;
    }
}
