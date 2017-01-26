package com.abby.redditgo.job;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.data.RedditgoProvider;
import com.abby.redditgo.event.SigninEvent;
import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by gsshop on 2017. 1. 19..
 */
@RunWith(AndroidJUnit4.class)
public class SubredditFetchJobTest {
    private CountDownLatch signal;

    @Inject
    ContentResolver mContentResolver;

    @Inject
    JobManager mJobManager;

    private ContentObserver observer;

    @Before
    public void setUp() {
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        EventBus.getDefault().register(this);
        signal = new CountDownLatch(1);
        observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                signal.countDown();
            }
        };
        mContentResolver.registerContentObserver(RedditgoProvider.Subreddit.CONTENT_URI, false, observer);
    }

    @After
    public void tearDown() throws Exception {
        EventBus.getDefault().unregister(this);
        mContentResolver.unregisterContentObserver(observer);
    }

    @Subscribe
    public void onSigninEvent(SigninEvent event) {
        mJobManager.addJobInBackground(new SubredditFetchJob());
    }

    @Test
    public void testSubredditFetchJob() throws Exception {
        mJobManager.addJobInBackground(new SigninJob("e07skim", "eskim3164"));
        signal.await();

        Cursor cursor = mContentResolver.query(RedditgoProvider.Subreddit.CONTENT_URI, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);

    }
}
