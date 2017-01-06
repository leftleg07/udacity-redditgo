package com.abby.redditgo.ui;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.MockApplication;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.comment.CommentActivity;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static com.abby.redditgo.ui.comment.CommentActivity.EXTRA_SUBMISSION_ID;
import static net.dean.jraw.auth.AuthenticationState.NONE;

/**
 * Created by gsshop on 2016. 12. 21..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentScreenTest {
    @Rule
    public IntentsTestRule<CommentActivity> mActivityTestRule =
            new IntentsTestRule<>(CommentActivity.class, false, false);

    @Inject
    UUID mDeviceId;

    @Before
    public void setUp() {
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if (state == NONE) {
            RedditApi.anonymous(mDeviceId);
        }
    }

    @Test
    public void testCommentList() throws Exception {
        Intent intent = new Intent();
//        intent.putExtra(EXTRA_SUBMISSION_ID, "5jvho3");
//        intent.putExtra(EXTRA_SUBMISSION_ID, "5kdvhp");
//        intent.putExtra(EXTRA_SUBMISSION_ID, "5lt5o4");
//        intent.putExtra(EXTRA_SUBMISSION_ID, "5lwr91");
        intent.putExtra(EXTRA_SUBMISSION_ID, "5m2ivk");

        mActivityTestRule.launchActivity(intent);

        // wait for activity finished
        while (!mActivityTestRule.getActivity().isFinishing()) {
            TimeUnit.SECONDS.sleep(1);
        }

    }
}
