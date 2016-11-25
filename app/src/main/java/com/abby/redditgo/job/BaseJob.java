package com.abby.redditgo.job;

import com.abby.redditgo.di.ApplicationComponent;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;

/**
 * Created by gsshop on 2016. 11. 4..
 */

public abstract class BaseJob extends Job {

    protected BaseJob(Params params) {
        super(params);
    }

    public abstract void inject(ApplicationComponent component);

}
