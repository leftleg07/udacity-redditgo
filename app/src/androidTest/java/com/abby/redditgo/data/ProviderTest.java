package com.abby.redditgo.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.dean.jraw.paginators.Sorting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import static com.abby.redditgo.data.SubmissionColumn.SORTING;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Tests for provider
 */
@RunWith(AndroidJUnit4.class)
public class ProviderTest {
    private ContentResolver mContentResolver;
    private Context mContext;

    ContentValues createSubmssionEntryValues(Sorting soring) {
        ContentValues entryValues = new ContentValues();
        entryValues.put(SubmissionColumn.ID, "aaa");
        entryValues.put(SORTING, soring.name());
        entryValues.put(SubmissionColumn.POST_HINT, "aaa");
        entryValues.put(SubmissionColumn.URL, "aaa");
        entryValues.put(SubmissionColumn.TITLE, "aaa");
        entryValues.put(SubmissionColumn.VOTE, 1);
        entryValues.put(SubmissionColumn.CREATED_TIME, 1);
        entryValues.put(SubmissionColumn.AUTHOR, "aaa");
        entryValues.put(SubmissionColumn.SUBREDDIT, "aaa");
        entryValues.put(SubmissionColumn.SCORE, 1);
        entryValues.put(SubmissionColumn.NUM_COMMENTS, 1);
        return entryValues;
    }

    ContentValues createSubredditEntryValues() {
        ContentValues entryValues = new ContentValues();
        entryValues.put(SubredditColumn.ID, "aaaa");
        entryValues.put(SubredditColumn.DISPLAY_NAME, "aaaa");
        return entryValues;
    }

    void deleteAllRecord() {
        mContentResolver.delete(RedditgoProvider.FrontPage.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.All.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.Submission.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.Subreddit.CONTENT_URI, null, null);
    }

    void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);

            assertWithMessage("Column '%s' not found. %s", columnName, error).that(idx).isNotEqualTo(-1);
            String expectedValue = entry.getValue().toString();
            String currentValue = valueCursor.getString(idx);
            assertWithMessage("Value '%s' did not match the expected value '%s'. %s", currentValue, expectedValue, error).that(currentValue).isEqualTo(expectedValue);
        }
    }

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
        mContentResolver = mContext.getContentResolver();
        deleteAllRecord();
    }

    @Test
    public void testProviderRegistry() throws Exception {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        String pkg = mContext.getPackageName();
        String cls = com.abby.redditgo.data.generated.RedditgoProvider.class.getName();
        ComponentName componentName = new ComponentName(pkg, cls);
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertWithMessage("Error: RedditgoProvider registered with authority: %s instead of authority: %s", providerInfo.authority, RedditgoContract.AUTHORITY).that(providerInfo.authority).isEqualTo(RedditgoContract.AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.

            assertWithMessage("Error: RedditgoProvider not registered at " + mContext.getPackageName()).that(false).isTrue();
        }
    }

    private static final Sorting[] SORTINGS = {Sorting.HOT, Sorting.NEW, Sorting.RISING, Sorting.CONTROVERSIAL, Sorting.TOP};

    /**
     * front page hot table
     */
    @Test
    public void testFrontPageTable() throws Exception {
        for(Sorting sorting: SORTINGS) {
            ContentValues testValues = createSubmssionEntryValues(sorting);

            Uri uri = mContentResolver.insert(RedditgoProvider.FrontPage.CONTENT_URI, testValues);
            long id = ContentUris.parseId(uri);

            assertWithMessage("Error: FrontPageHot Query Validation Failed").that(id).isGreaterThan(0L);

            ContentValues updateValues = new ContentValues(testValues);
            updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
            uri = RedditgoProvider.FrontPage.withId(updateValues.getAsString(SubmissionColumn.SORTING), testValues.getAsString(SubmissionColumn.ID));
            int count = mContentResolver.update(uri, updateValues, null, null);
            assertThat(count).isGreaterThan(0);

            Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

            assertWithMessage("Error: No Records returned from FrontPageHot query").that(cursor.moveToFirst()).isTrue();
            validateCurrentRecord("Error: FrontPageHot Query Validation Failed", cursor, updateValues);
            cursor.close();
        }
    }


    /**
     * all hot table
     */
    @Test
    public void testAllTable() throws Exception {
        for(Sorting sorting: SORTINGS) {
            ContentValues testValues = createSubmssionEntryValues(sorting);

            Uri uri = mContentResolver.insert(RedditgoProvider.All.CONTENT_URI, testValues);
            long id = ContentUris.parseId(uri);

            assertWithMessage("Error: All Query Validation Failed").that(id).isGreaterThan(0L);

            ContentValues updateValues = new ContentValues(testValues);
            updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
            uri = RedditgoProvider.All.withId(updateValues.getAsString(SubmissionColumn.SORTING), testValues.getAsString(SubmissionColumn.ID));
            int count = mContentResolver.update(uri, updateValues, null, null);
            assertThat(count).isGreaterThan(0);

            Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

            assertWithMessage("Error: No Records returned from All query").that(cursor.moveToFirst()).isTrue();
            validateCurrentRecord("Error: All Query Validation Failed", cursor, updateValues);
            cursor.close();
        }
    }


    /**
     * submission table
     */
    @Test
    public void testSubmissionTable() throws Exception {
        for(Sorting sorting: SORTINGS) {
            ContentValues testValues = createSubmssionEntryValues(sorting);

            Uri uri = mContentResolver.insert(RedditgoProvider.Submission.CONTENT_URI, testValues);
            long id = ContentUris.parseId(uri);

            assertWithMessage("Error: SubmissionHot Query Validation Failed").that(id).isGreaterThan(0L);

            ContentValues updateValues = new ContentValues(testValues);
            updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
            uri = RedditgoProvider.Submission.withId(updateValues.getAsString(SubmissionColumn.SUBREDDIT), updateValues.getAsString(SubmissionColumn.SORTING), testValues.getAsString(SubmissionColumn.ID));
            int count = mContentResolver.update(uri, updateValues, null, null);
            assertThat(count).isGreaterThan(0);

            Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

            assertWithMessage("Error: No Records returned from SubmissionHot query").that(cursor.moveToFirst()).isTrue();
            validateCurrentRecord("Error: SubmissionHot Query Validation Failed", cursor, updateValues);
            cursor.close();
        }
    }


    /**
     * subreddit table
     */
    @Test
    public void testSubredditTable() throws Exception {
        ContentValues testValues = createSubredditEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.Subreddit.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: Subreddit Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubredditColumn.DISPLAY_NAME, "bbbbb");
        uri = RedditgoProvider.Subreddit.withId(updateValues.getAsString(SubredditColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from FrontPageTop query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: FrontPageTop Query Validation Failed", cursor, updateValues);
        cursor.close();
    }
}
