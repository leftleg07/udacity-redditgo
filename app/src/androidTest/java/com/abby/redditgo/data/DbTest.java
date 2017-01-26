package com.abby.redditgo.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orhanobut.logger.Logger;

import net.dean.jraw.models.Submission;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Tests for database
 */
@RunWith(AndroidJUnit4.class)
public class DbTest {
    private Context mContext;

    private static final String[] TABLE_NAMES = {
            RedditgoContract.TABLE_NAME_FRONT_PAGE_HOT,
            RedditgoContract.TABLE_NAME_FRONT_PAGE_NEW,
            RedditgoContract.TABLE_NAME_FRONT_PAGE_RISING,
            RedditgoContract.TABLE_NAME_FRONT_PAGE_CONTROVERSAL,
            RedditgoContract.TABLE_NAME_FRONT_PAGE_TOP,
            RedditgoContract.TABLE_NAME_ALL_HOT,
            RedditgoContract.TABLE_NAME_ALL_NEW,
            RedditgoContract.TABLE_NAME_ALL_RISING,
            RedditgoContract.TABLE_NAME_ALL_CONTROVERSAL,
            RedditgoContract.TABLE_NAME_ALL_TOP,
            RedditgoContract.TABLE_NAME_SUBMISSION_HOT,
            RedditgoContract.TABLE_NAME_SUBMISSION_NEW,
            RedditgoContract.TABLE_NAME_SUBMISSION_RISING,
            RedditgoContract.TABLE_NAME_SUBMISSION_CONTROVERSAL,
            RedditgoContract.TABLE_NAME_SUBMISSION_TOP
    };

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testPostHint() throws Exception {
        Submission.PostHint hint = Submission.PostHint.SELF;
        String name = hint.name();
        Submission.PostHint hint2 = Submission.PostHint.valueOf("VIDEO");
        if (hint == Submission.PostHint.valueOf(name)) {
            Logger.i("AAAAA");
        } else {
            Logger.i("BBBBB");
        }

    }

    @Test
    public void testDb() throws Exception {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        for (String name : TABLE_NAMES) {
            tableNameHashSet.add(name);
        }
        tableNameHashSet.add(RedditgoContract.TABLE_NAME_SUBREDDIT);

        SQLiteDatabase db = com.abby.redditgo.data.generated.RedditgoDatabase.getInstance(mContext).getWritableDatabase();
        assertThat(db.isOpen()).isTrue();

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertWithMessage("Error: This means that the database has not been created correctly").that(c.moveToFirst()).isTrue();

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        assertWithMessage("Error: Your database was created without the tables").that(tableNameHashSet.size()).isEqualTo(0);
        c.close();

        /**
         * submission table
         */
        for (String name : TABLE_NAMES) {
            // now, do our tables contain the correct columns?
            c = db.rawQuery("PRAGMA table_info(" + name + ")",
                    null);

            assertWithMessage("Error: This means that we were unable to query the database for table information.").that(c.moveToFirst()).isTrue();


            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> entryColumnHashSet = new HashSet<String>();
            entryColumnHashSet.add(SubmissionColumn.ID);
            entryColumnHashSet.add(SubmissionColumn.POST_HINT);
            entryColumnHashSet.add(SubmissionColumn.URL);
            entryColumnHashSet.add(SubmissionColumn.TITLE);
            entryColumnHashSet.add(SubmissionColumn.VOTE);
            entryColumnHashSet.add(SubmissionColumn.CREATED_TIME);
            entryColumnHashSet.add(SubmissionColumn.AUTHOR);
            entryColumnHashSet.add(SubmissionColumn.SUBREDDIT);
            entryColumnHashSet.add(SubmissionColumn.SCORE);
            entryColumnHashSet.add(SubmissionColumn.NUM_COMMENTS);
            entryColumnHashSet.add(SubmissionColumn.THUMBNAIL);

            int columnNameIndex = c.getColumnIndex("name");
            do {
                String columnName = c.getString(columnNameIndex);
                entryColumnHashSet.remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain all of the required location
            // entry columns
            assertWithMessage("Error: The database doesn't contain all of the required " + name + " entry columns").that(entryColumnHashSet.isEmpty()).isTrue();
            c.close();
        }

        /**
         * subreddit table
         */
        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + RedditgoContract.TABLE_NAME_SUBREDDIT + ")", null);

        assertWithMessage("Error: This means that we were unable to query the database for table information.").that(c.moveToFirst()).isTrue();


        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> entryColumnHashSet = new HashSet<String>();
        entryColumnHashSet.add(SubredditColumn.ID);
        entryColumnHashSet.add(SubredditColumn.DISPLAY_NAME);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            entryColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertWithMessage("Error: The database doesn't contain all of the required subreddit entry columns").that(entryColumnHashSet.isEmpty()).isTrue();
        c.close();

        db.close();
    }

}
