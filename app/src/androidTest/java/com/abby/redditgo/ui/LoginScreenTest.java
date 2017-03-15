package com.abby.redditgo.ui;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.BuildConfig;
import com.abby.redditgo.R;
import com.abby.redditgo.event.LoginEvent;
import com.abby.redditgo.ui.login.LoginActivity;
import com.google.common.truth.Truth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Detail screen test
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginScreenTest {

    CountDownLatch latch;

    private static final String USERNAME = BuildConfig.REDDIT_USERNAME;
    private static final String PASSWORD = BuildConfig.REDDIT_PASSWORD;


    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void setUp() throws Exception {
        EventBus.getDefault().register(this);
        latch = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        EventBus.getDefault().unregister(this);

    }

    @Test
    public void testFailLogin() throws Exception {

        onView(withId(R.id.username)).perform(clearText());
        onView(withId(R.id.username)).perform(typeText(USERNAME), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(clearText());
        onView(withId(R.id.password)).perform(typeText(PASSWORD+"1"), closeSoftKeyboard());

        onView(withId(R.id.sign_in_button)).perform(click());

        latch.await();
        Truth.assertThat(mActivityTestRule.getActivity().isFinishing()).isFalse();
    }


    @Test
    public void testLogin() throws Exception {

        onView(withId(R.id.username)).perform(clearText());
        onView(withId(R.id.username)).perform(typeText(USERNAME), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(clearText());
        onView(withId(R.id.password)).perform(typeText(PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.sign_in_button)).perform(click());

        // wait for activity finished
        latch.await();
        Truth.assertThat(mActivityTestRule.getActivity().isFinishing()).isTrue();

    }


    @Subscribe
    public void onLoginEvent(LoginEvent event) {
        latch.countDown();

    }

}
