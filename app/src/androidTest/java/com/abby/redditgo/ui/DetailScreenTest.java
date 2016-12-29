package com.abby.redditgo.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.abby.redditgo.ui.detail.DetailActivity;

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
    public IntentsTestRule<DetailActivity> mActivityTestRule =
            new IntentsTestRule<>(DetailActivity.class, false, false);

    @Test
    public void testActivity() throws Exception {

        Intent intent = new Intent();
        Uri uri = Uri.parse("https://inews.co.uk/essentials/news/uk/toy-store-quietens-retailers-face-calls-help-children-autism/");
        uri = Uri.parse("https://i.reddituploads.com/58986555f545487c9d449bd5d9326528?fit=max&amp;h=1536&amp;w=1536&amp;s=c15543d234ef9bbb27cb168b01afb87d");
        intent.setData(uri);

        mActivityTestRule.launchActivity(intent);

        // wait for activity finished
        while (!mActivityTestRule.getActivity().isFinishing()) {
            TimeUnit.SECONDS.sleep(1);
        }


    }
}
