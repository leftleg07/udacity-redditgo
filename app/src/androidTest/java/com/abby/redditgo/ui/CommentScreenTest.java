package com.abby.redditgo.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.SwipeRefreshLayout;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.R;
import com.abby.redditgo.event.LoginEvent;
import com.abby.redditgo.job.SigninJob;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.comment.CommentActivity;
import com.birbit.android.jobqueue.JobManager;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.abby.redditgo.R.id.new_comment_reply;
import static net.dean.jraw.auth.AuthenticationState.NONE;

/**
 * Created by gsshop on 2016. 12. 21..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentScreenTest {
    private CountDownLatch signal;

    @Inject
    JobManager mJobManager;

    @Rule
    public IntentsTestRule<CommentActivity> mActivityTestRule =
            new IntentsTestRule<>(CommentActivity.class, false, false);

    @Inject
    UUID mDeviceId;

    private static final String USERNAME = "e07skim";
    private static final String PASSWORD = "eskim3164";
    private SwipeRefreshLayout mSwipeRefresh;

    @Before
    public void setUp() {
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if (state == NONE) {
            RedditApi.anonymous(mDeviceId);
        }
        EventBus.getDefault().register(this);
        signal = new CountDownLatch(1);

    }

    @After
    public void tearDown() throws Exception {
        TimeUnit.MILLISECONDS.sleep(1600);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onLogininEvent(LoginEvent event) {
        signal.countDown();
    }

    /**
     * comment 화면을 띄운다.
     *
     * @throws Exception
     */
    @Test
    public void testCommentList() throws Exception {
        Intent intent = new Intent();
        Uri data = Uri.parse("content://com.abby.redditgo.provider/front_page/HOT/5t4wv5");
        data = Uri.parse("content://com.abby.redditgo.provider/front_page/HOT/5vgj3r");
        intent.setData(data);

        mActivityTestRule.launchActivity(intent);


        // wait for activity finished
        while (!mActivityTestRule.getActivity().isFinishing()) {
            TimeUnit.SECONDS.sleep(1);
        }

    }


    /**
     * comment compose 창에서 navigation back button을 누른다.
     *
     * @throws Exception
     */

    @Test
    public void testNavigationBack() throws Exception {
        openActivity();
        login();
        mSwipeRefresh = (SwipeRefreshLayout)mActivityTestRule.getActivity().findViewById(R.id.swiperefresh);

        onView(withId(R.id.fab)).perform(click());

        onView(withContentDescription(R.string.navigation_description)).perform(click());

        onView(withId(R.id.fab)).check(matches(isDisplayed()));

    }

    /**
     * comment compose 창에서 hardware back button을 누른다.
     */
    @Test
    public void testBackButton() throws Exception {
        openActivity();
        login();
        mSwipeRefresh = (SwipeRefreshLayout)mActivityTestRule.getActivity().findViewById(R.id.swiperefresh);

        onView(withId(R.id.fab)).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.fab)).check(matches(isDisplayed()));

    }

    /**
     * comment compose 창에서 comment를 작성하여 submit 버튼을 누른다.
     */
    @Test
    public void testReplyComment() throws Exception {
        openActivity();
        login();

        onView(withId(R.id.fab)).perform(click());

        onView(withId(new_comment_reply)).perform(clearText());
        onView(withId(R.id.new_comment_reply)).perform(typeText("Bound by War in Iraq, Three Generals Reconvene in the Situation Room"));

        onView(withId(R.id.new_comment_submit)).perform(click());

        onView(withId(R.id.fab)).check(matches(isDisplayed()));

    }

    /**
     * comment compose 창에서 comment를 공백으로 하여 submit 버튼을 누른다.
     */
    @Test
    public void testEmptyComment() throws Exception {
        openActivity();
        login();

        onView(withId(R.id.fab)).perform(click());

        onView(withId(new_comment_reply)).perform(clearText());
        onView(withId(R.id.new_comment_submit)).perform(click());

        onView(withId(new_comment_reply)).check(matches(hasFocus()));

    }


    public void openActivity() {
        Intent intent = new Intent();
        Uri data = Uri.parse("content://com.abby.redditgo.provider/front_page/HOT/5t4wv5");
        data = Uri.parse("content://com.abby.redditgo.provider/front_page/HOT/5vgj3r");
        intent.setData(data);
        mActivityTestRule.launchActivity(intent);

    }

    public void login() throws InterruptedException {
        mJobManager.addJobInBackground(new SigninJob(USERNAME, PASSWORD));
        signal.await();
    }
}
