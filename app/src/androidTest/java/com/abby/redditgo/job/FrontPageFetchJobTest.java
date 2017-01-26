package com.abby.redditgo.job;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.data.RedditgoProvider;
import com.birbit.android.jobqueue.JobManager;

import net.dean.jraw.paginators.Sorting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

/**
 * Created by gsshop on 2017. 1. 20..
 */
@RunWith(AndroidJUnit4.class)
public class FrontPageFetchJobTest {
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
        signal = new CountDownLatch(1);
        observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                signal.countDown();
            }
        };
        mContentResolver.registerContentObserver(RedditgoProvider.FrontPageHot.CONTENT_URI, false, observer);
    }

    @After
    public void tearDown() throws Exception {
        mContentResolver.unregisterContentObserver(observer);
    }

    @Test
    public void testFrontPageHot() throws Exception {
        mJobManager.addJobInBackground(new SubmissionFetchJob(null, Sorting.HOT));
        signal.await();
    }
}
