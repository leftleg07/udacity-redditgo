package com.abby.redditgo.job;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.event.LoginEvent;
import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

/**
 * Created by gsshop on 2017. 1. 19..
 */
@RunWith(AndroidJUnit4.class)
public class SigninJobTest {
    private CountDownLatch signal;

    @Inject
    JobManager mJobManager;

    @Before
    public void setUp() {
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        EventBus.getDefault().register(this);
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onSigninEvent(LoginEvent event) {
        signal.countDown();
    }

    @Test
    public void testSignin() throws Exception {
        mJobManager.addJobInBackground(new SigninJob("e07skim", "eskim3164"));
        signal.await();
    }
}
