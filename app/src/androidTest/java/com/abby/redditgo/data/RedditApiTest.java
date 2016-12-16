package com.abby.redditgo.data;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.event.SubmissionEvent;
import com.abby.redditgo.event.SubredditEvent;
import com.abby.redditgo.job.CommentFetchJob;
import com.abby.redditgo.job.JobId;
import com.abby.redditgo.job.SubmissionFetchJob;
import com.abby.redditgo.job.SubredditFetchJob;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.google.common.truth.Truth;
import com.orhanobut.logger.Logger;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;
import static net.dean.jraw.auth.AuthenticationState.NONE;
import static net.dean.jraw.auth.AuthenticationState.READY;

/**
 * Created by gsshop on 2016. 10. 20..
 */
@RunWith(AndroidJUnit4.class)
public class RedditApiTest {
    private CountDownLatch mSignal;

    @Inject
    JobManager mJobManager;

    @Inject
    UUID mDeviceId;

    @Before
    public void setUp() {
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        EventBus.getDefault().register(this);
        mSignal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        mSignal.countDown();
        EventBus.getDefault().unregister(this);

    }

    private void login() {
        String username = "e07skim";
        String password = "eskim3164";
        String user = RedditApi.signIn(username, password);
        assertThat(user).isEqualTo(username);
    }
    
    @Test
    public void testSignIn() throws Exception {
        login();
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        assertThat(state).isEqualTo(READY);

    }

    @Test
    public void testFetchSubreddits() throws Exception {
        if (!RedditApi.isAuthorized()) {
            login();
        }

        mJobManager.addJobInBackground(new SubredditFetchJob());
        mSignal.await();
    }

    @Subscribe
    public void onSubredditEvent(SubredditEvent event) {
        for(Subreddit subreddit: event.subreddits) {
            Logger.i(subreddit.getDisplayName());
        }
        mSignal.countDown();
    }

    @Test
    public void testFetchFrontPage() throws Exception {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if(state == NONE) {
            RedditApi.anonymous(mDeviceId);
        }
        mJobManager.addJobInBackground(new SubmissionFetchJob(null, Sorting.HOT));
        mSignal.await();

    }

    @Subscribe
    public void onSubmissionEvent(SubmissionEvent event) {
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.FETCH_SUBMISSION_ID);
        for(Submission submission: event.submissions) {
            Logger.i(submission.getTitle());
        }
        mSignal.countDown();
    }

    @Test
    public void testFetchAllSubmissions() throws Exception {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if(state == NONE) {
            RedditApi.anonymous(mDeviceId);
        }
        mJobManager.addJobInBackground(new SubmissionFetchJob("all", Sorting.HOT));
        mSignal.await();

    }


    @Test
    public void testFetchComment() throws Exception {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if(state == NONE) {
            RedditApi.anonymous(mDeviceId);
        }
        String submissionId="5ilkvt";
        mJobManager.addJobInBackground(new CommentFetchJob(submissionId));
        mSignal.await();
    }

    @Subscribe
    public void onCommentEvent(CommentEvent event) {
        Truth.assertThat(event.node.getTotalSize() > 0);
    }
}
