package com.abby.redditgo.di;

import com.abby.redditgo.job.SubmissionFetchJob;
import com.abby.redditgo.services.MyGcmJobService;
import com.abby.redditgo.services.MyJobService;
import com.abby.redditgo.ui.comment.CommentActivity;
import com.abby.redditgo.ui.comment.CommentAdapter;
import com.abby.redditgo.ui.comment.CommentComposeActivity;
import com.abby.redditgo.ui.login.LoginActivity;
import com.abby.redditgo.ui.main.MainActivity;
import com.abby.redditgo.ui.main.SubmissionAdapter;

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
    void inject(LoginActivity loginActivity);
    void inject(SubmissionFetchJob fetchSubmission);
    void inject(SubmissionAdapter adapter);
    void inject(CommentActivity commentActivity);
    void inject(CommentAdapter commentAdapter);
    void inject(CommentComposeActivity commentComposeActivity);
}
