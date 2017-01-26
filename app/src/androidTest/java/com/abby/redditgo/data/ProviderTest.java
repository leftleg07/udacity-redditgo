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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Tests for provider
 */
@RunWith(AndroidJUnit4.class)
public class ProviderTest {
    private ContentResolver mContentResolver;
    private Context mContext;

    ContentValues createSubmssionEntryValues() {
        ContentValues entryValues = new ContentValues();
        entryValues.put(SubmissionColumn.ID, "aaa");
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
        mContentResolver.delete(RedditgoProvider.FrontPageHot.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.FrontPageNew.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.FrontPageRising.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.FrontPageControversal.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.FrontPageTop.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.AllHot.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.AllNew.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.AllRising.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.AllControversal.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.AllTop.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.SubmissionHot.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.SubmissionNew.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.SubmissionRising.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.SubmissionControversal.CONTENT_URI, null, null);
        mContentResolver.delete(RedditgoProvider.SubmissionTop.CONTENT_URI, null, null);
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

    /**
     * front page hot table
     */
    @Test
    public void testFrontPageHotTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.FrontPageHot.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: FrontPageHot Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.FrontPageHot.withId(testValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from FrontPageHot query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: FrontPageHot Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * front page new table
     */
    @Test
    public void testFrontPageNewTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.FrontPageNew.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: FrontPageNew Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.FrontPageNew.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from FrontPageNew query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: FrontPageNew Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * front page rising table
     */
    @Test
    public void testFrontPageRisingTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.FrontPageRising.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: FrontPageRising Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.FrontPageRising.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from FrontPageRising query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: FrontPageRising Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * front page controversal table
     */
    @Test
    public void testFrontPageControversalTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.FrontPageControversal.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: FrontPageControversal Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.FrontPageControversal.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from FrontPageControversal query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: FrontPageControversal Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * front page top table
     */
    @Test
    public void testFrontPageTopTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.FrontPageTop.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: FrontPageTop Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.FrontPageTop.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from FrontPageTop query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: FrontPageTop Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * all hot table
     */
    @Test
    public void testAllHotTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.AllHot.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: AllHot Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.AllHot.withId(testValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from AllHot query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: AllHot Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * all new table
     */
    @Test
    public void testAllNewTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.AllNew.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: AllNew Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.AllNew.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from AllNew query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: AllNew Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * all rising table
     */
    @Test
    public void testAllRisingTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.AllRising.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: AllRising Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.AllRising.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from AllRising query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: AllRising Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * all controversal table
     */
    @Test
    public void testAllControversalTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.AllControversal.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: AllControversal Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.AllControversal.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from AllControversal query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: AllControversal Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * all top table
     */
    @Test
    public void testAllTopTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.AllTop.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: AllTop Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.AllTop.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from AllTop query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: AllTop Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * all hot table
     */
    @Test
    public void testSubmissionHotTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.SubmissionHot.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: SubmissionHot Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.SubmissionHot.withId(testValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from SubmissionHot query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: SubmissionHot Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * submission new table
     */
    @Test
    public void testSubmissionNewTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.SubmissionNew.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: SubmissionNew Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.SubmissionNew.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from SubmissionNew query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: SubmissionNew Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * submission rising table
     */
    @Test
    public void testSubmissionRisingTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.SubmissionRising.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: SubmissionRising Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.SubmissionRising.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from SubmissionRising query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: SubmissionRising Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * submission controversal table
     */
    @Test
    public void testSubmissionControversalTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.SubmissionControversal.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: SubmissionControversal Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.SubmissionControversal.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from SubmissionControversal query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: SubmissionControversal Query Validation Failed", cursor, updateValues);
        cursor.close();
    }

    /**
     * submission top table
     */
    @Test
    public void testSubmissionTopTable() throws Exception {
        ContentValues testValues = createSubmssionEntryValues();

        Uri uri = mContentResolver.insert(RedditgoProvider.SubmissionTop.CONTENT_URI, testValues);
        long id = ContentUris.parseId(uri);

        assertWithMessage("Error: SubmissionTop Query Validation Failed").that(id).isGreaterThan(0L);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(SubmissionColumn.AUTHOR, "bbbbb");
        uri = RedditgoProvider.SubmissionTop.withId(updateValues.getAsString(SubmissionColumn.ID));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isGreaterThan(0);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from SubmissionTop query").that(cursor.moveToFirst()).isTrue();
        validateCurrentRecord("Error: SubmissionTop Query Validation Failed", cursor, updateValues);
        cursor.close();
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
