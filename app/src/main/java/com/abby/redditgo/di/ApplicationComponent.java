package com.abby.redditgo.di;

import com.abby.redditgo.job.FetchSubmission;
import com.abby.redditgo.services.MyGcmJobService;
import com.abby.redditgo.services.MyJobService;
import com.abby.redditgo.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * google dagger component
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(MyGcmJobService myGcmJobService);
    void inject(MyJobService myJobService);
    void inject(MainActivity mainActivity);

    void inject(FetchSubmission fetchSubmission);
}
