package com.abby.redditgo.ui;

import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.ui.main.MainActivity;
import com.abby.redditgo.util.CustomTabActivityHelper;
import com.abby.redditgo.util.WebviewFallback;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Detail screen test
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DetailScreenTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testActivity() throws Exception {

        Uri uri = Uri.parse("https://inews.co.uk/essentials/news/uk/toy-store-quietens-retailers-face-calls-help-children-autism/");

        CustomTabActivityHelper.openCustomTab(mActivityTestRule.getActivity(), uri, "AAAA", new WebviewFallback());

        // wait for activity finished
        while (!mActivityTestRule.getActivity().isFinishing()) {
            TimeUnit.SECONDS.sleep(1);
        }


    }
}
