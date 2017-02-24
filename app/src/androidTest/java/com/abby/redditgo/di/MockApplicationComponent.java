package com.abby.redditgo.di;

import com.abby.redditgo.job.SigninJobTest;
import com.abby.redditgo.job.SubmissionFetchJobTest;
import com.abby.redditgo.job.SubredditFetchJobTest;
import com.abby.redditgo.network.RedditApiTest;
import com.abby.redditgo.ui.CommentScreenTest;

import javax.inject.Singleton;

import dagger.Component;

/**
 * google dagger component
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface MockApplicationComponent {
    void inject(RedditApiTest redditApiTest);
    void inject(CommentScreenTest commentScreenTest);
    void inject(SigninJobTest signinJobTest);
    void inject(SubredditFetchJobTest subredditFetchJobTest);
    void inject(SubmissionFetchJobTest frontPageFetchJobTest);
}
