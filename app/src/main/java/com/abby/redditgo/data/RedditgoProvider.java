package com.abby.redditgo.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by gsshop on 2017. 1. 16..
 */
@ContentProvider(authority = RedditgoContract.AUTHORITY, database = RedditgoDatabase.class)
public final class RedditgoProvider {

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + RedditgoContract.AUTHORITY);

    interface Path {
        String FRONT_PAGE = "front_page";
        String ALL = "all";
        String SUBMISSION = "submission";

        String SUBREDDIT = "subreddit";

    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    /**
     * front page table
     */
    @TableEndpoint(table = RedditgoDatabase.FRONT_PAGE)
    public static class FrontPage {
        @ContentUri(
                path = Path.FRONT_PAGE,
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE)
        public static final Uri CONTENT_URI = buildUri(Path.FRONT_PAGE);

        @InexactContentUri(
                path = Path.FRONT_PAGE + "/*/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.FRONT_PAGE,
                whereColumn = {SubmissionColumn.SORTING, SubmissionColumn.ID},
                pathSegment = {1, 2})
        public static Uri withId(String sorting, String id) {
            return buildUri(Path.FRONT_PAGE, sorting, id);
        }

        @InexactContentUri(
                path = Path.FRONT_PAGE + "/*",
                name = "SORTING",
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE,
                whereColumn = {SubmissionColumn.SORTING},
                pathSegment = 1,
                defaultSort = SubmissionColumn.RANK + " ASC" )
        public static Uri withSorting(String sorting) {
            return buildUri(Path.FRONT_PAGE, sorting);
        }
    }


    /**
     * all hot
     */
    @TableEndpoint(table = RedditgoDatabase.ALL)
    public static class All {
        @ContentUri(
                path = Path.ALL,
                type = "vnd.android.cursor.dir/" + Path.ALL)
        public static final Uri CONTENT_URI = buildUri(Path.ALL);

        @InexactContentUri(
                path = Path.ALL + "/*/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.ALL,
                whereColumn = {SubmissionColumn.SORTING, SubmissionColumn.ID},
                pathSegment = {1, 2})
        public static Uri withId(String sorting, String id) {
            return buildUri(Path.ALL, sorting, id);
        }

        @InexactContentUri(
                path = Path.ALL + "/*",
                name = "SORTING",
                type = "vnd.android.cursor.dir/" + Path.ALL,
                whereColumn = {SubmissionColumn.SORTING},
                pathSegment = 1,
                defaultSort = SubmissionColumn.RANK + " ASC" )
        public static Uri withSorting(String sorting) {
            return buildUri(Path.ALL, sorting);
        }
    }


    /**
     * submission hot
     */
    @TableEndpoint(table = RedditgoDatabase.SUBMISSION)
    public static class Submission {
        @ContentUri(
                path = Path.SUBMISSION,
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION)
        public static final Uri CONTENT_URI = buildUri(Path.SUBMISSION);

        @InexactContentUri(
                path = Path.SUBMISSION + "/*/*/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION,
                whereColumn = {SubmissionColumn.SUBREDDIT, SubmissionColumn.SORTING, SubmissionColumn.ID},
                pathSegment = {1, 2, 3})
        public static Uri withId(String subreddit, String sorting, String id) {
            return buildUri(Path.SUBMISSION, subreddit, sorting, id);
        }

        @InexactContentUri(
                path = Path.SUBREDDIT + "/*/*",
                name = "SUBREDDIT",
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION,
                whereColumn = {SubmissionColumn.SUBREDDIT, SubmissionColumn.SORTING},
                pathSegment = {1, 2},
                defaultSort = SubmissionColumn.RANK + " ASC" )
        public static Uri withSubredditAndSorting(String subreddit, String sorting) {
            return buildUri(Path.SUBREDDIT, subreddit, sorting);
        }
    }


    /**
     * subreddit
     */
    @TableEndpoint(table = RedditgoDatabase.SUBREDDIT)
    public static class Subreddit {
        @ContentUri(
                path = Path.SUBREDDIT,
                type = "vnd.android.cursor.dir/" + Path.SUBREDDIT)
        public static final Uri CONTENT_URI = buildUri(Path.SUBREDDIT);

        @InexactContentUri(
                path = Path.SUBREDDIT + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBREDDIT,
                whereColumn = SubredditColumn.ID,
                pathSegment = 1,
                defaultSort = SubredditColumn.DISPLAY_NAME + " ASC" )
        public static Uri withId(String id) { return buildUri(Path.SUBREDDIT, id); }

    }

}
