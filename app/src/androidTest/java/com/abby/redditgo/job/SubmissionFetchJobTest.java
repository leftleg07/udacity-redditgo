package com.abby.redditgo.job;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.data.RedditgoProvider;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import net.dean.jraw.paginators.Sorting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by gsshop on 2017. 1. 20..
 */
@RunWith(AndroidJUnit4.class)
public class SubmissionFetchJobTest {


    private CountDownLatch signal;

    @Inject
    ContentResolver mContentResolver;

    @Inject
    JobManager mJobManager;

    private ContentObserver mObserver;


    private static final Sorting[] SORTINGS = {Sorting.HOT, Sorting.NEW, Sorting.RISING, Sorting.CONTROVERSIAL, Sorting.TOP};

    @Before
    public void setUp() {
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);

        mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                signal.countDown();
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    @Test
    public void testFrontPage() throws Exception {
        for (Sorting sorting : SORTINGS) {
            signal = new CountDownLatch(1);
            Uri uri = RedditgoProvider.FrontPage.withSorting(sorting.name());
            mContentResolver.registerContentObserver(uri, false, mObserver);
            mJobManager.addJobInBackground(new SubmissionFetchJob(null, sorting));
            signal.await();
            mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.SUBMISSION_FETCH_ID);
            mContentResolver.unregisterContentObserver(mObserver);
            Cursor cursor = mContentResolver.query(uri, null, null, null, null);
            int count = cursor.getCount();
            assertThat(count).isGreaterThan(0);

        }
    }

    @Test
    public void testAll() throws Exception {
        for (Sorting sorting : SORTINGS) {
            signal = new CountDownLatch(1);
            Uri uri = RedditgoProvider.All.withSorting(sorting.name());
            mContentResolver.registerContentObserver(uri, false, mObserver);
            mJobManager.addJobInBackground(new SubmissionFetchJob("all", sorting));
            signal.await();
            mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.SUBMISSION_FETCH_ID);
            mContentResolver.unregisterContentObserver(mObserver);
            Cursor cursor = mContentResolver.query(uri, null, null, null, null);
            int count = cursor.getCount();
            assertThat(count).isGreaterThan(0);

        }
    }

    private static final String SUBMISSION = "funny";
    @Test
    public void testSubmission() throws Exception {
        for (Sorting sorting : SORTINGS) {
            signal = new CountDownLatch(1);
            Uri uri = RedditgoProvider.Submission.withSubredditAndSorting(SUBMISSION, sorting.name());
            mContentResolver.registerContentObserver(uri, false, mObserver);
            mJobManager.addJobInBackground(new SubmissionFetchJob(SUBMISSION, sorting));
            signal.await();
            mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.SUBMISSION_FETCH_ID);
            mContentResolver.unregisterContentObserver(mObserver);
            Cursor cursor = mContentResolver.query(uri, null, null, null, null);
            int count = cursor.getCount();
            assertThat(count).isGreaterThan(0);

        }
    }

}
