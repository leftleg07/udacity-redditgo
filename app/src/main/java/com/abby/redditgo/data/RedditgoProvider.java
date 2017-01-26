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
        String FRONT_PAGE_HOT = "front_page.hot";
        String FRONT_PAGE_NEW = "front_page.new";
        String FRONT_PAGE_RISING = "front_page.rising";
        String FRONT_PAGE_CONTROVERSAL = "front_page.controversal";
        String FRONT_PAGE_TOP = "front_page.top";

        String ALL_HOT = "all.hot";
        String ALL_NEW = "all.new";
        String ALL_RISING = "all.rising";
        String ALL_CONTROVERSAL = "all.controversal";
        String ALL_TOP = "all.top";

        String SUBMISSION_HOT = "submission.hot";
        String SUBMISSION_NEW = "submission.new";
        String SUBMISSION_RISING = "submission.rising";
        String SUBMISSION_CONTROVERSAL = "submission.controversal";
        String SUBMISSION_TOP = "submission.top";

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
     * front page hot
     */
    @TableEndpoint(table = RedditgoDatabase.FRONT_PAGE_HOT)
    public static class FrontPageHot {
        @ContentUri(
                path = Path.FRONT_PAGE_HOT,
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE_HOT)
        public static final Uri CONTENT_URI = buildUri(Path.FRONT_PAGE_HOT);

        @InexactContentUri(
                path = Path.FRONT_PAGE_HOT + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.FRONT_PAGE_HOT,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.FRONT_PAGE_HOT, id);
        }
    }

    /**
     * front page new
     */
    @TableEndpoint(table = RedditgoDatabase.FRONT_PAGE_NEW)
    public static class FrontPageNew {
        @ContentUri(
                path = Path.FRONT_PAGE_NEW,
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE_NEW)
        public static final Uri CONTENT_URI = buildUri(Path.FRONT_PAGE_NEW);

        @InexactContentUri(
                path = Path.FRONT_PAGE_NEW + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.FRONT_PAGE_NEW,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.FRONT_PAGE_NEW, id);
        }
    }

    /**
     * front page rising
     */
    @TableEndpoint(table = RedditgoDatabase.FRONT_PAGE_RISING)
    public static class FrontPageRising {
        @ContentUri(
                path = Path.FRONT_PAGE_RISING,
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE_RISING)
        public static final Uri CONTENT_URI = buildUri(Path.FRONT_PAGE_RISING);

        @InexactContentUri(
                path = Path.FRONT_PAGE_RISING + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.FRONT_PAGE_RISING,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.FRONT_PAGE_RISING, id);
        }
    }

    /**
     * front page controversal
     */
    @TableEndpoint(table = RedditgoDatabase.FRONT_PAGE_CONTROVERSAL)
    public static class FrontPageControversal {
        @ContentUri(
                path = Path.FRONT_PAGE_CONTROVERSAL,
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE_CONTROVERSAL)
        public static final Uri CONTENT_URI = buildUri(Path.FRONT_PAGE_CONTROVERSAL);

        @InexactContentUri(
                path = Path.FRONT_PAGE_CONTROVERSAL + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.FRONT_PAGE_CONTROVERSAL,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.FRONT_PAGE_CONTROVERSAL, id);
        }
    }

    /**
     * front page top
     */
    @TableEndpoint(table = RedditgoDatabase.FRONT_PAGE_TOP)
    public static class FrontPageTop {
        @ContentUri(
                path = Path.FRONT_PAGE_TOP,
                type = "vnd.android.cursor.dir/" + Path.FRONT_PAGE_TOP)
        public static final Uri CONTENT_URI = buildUri(Path.FRONT_PAGE_TOP);

        @InexactContentUri(
                path = Path.FRONT_PAGE_TOP + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.FRONT_PAGE_TOP,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.FRONT_PAGE_TOP, id);
        }
    }

    /**
     * all hot
     */
    @TableEndpoint(table = RedditgoDatabase.ALL_HOT)
    public static class AllHot {
        @ContentUri(
                path = Path.ALL_HOT,
                type = "vnd.android.cursor.dir/" + Path.ALL_HOT)
        public static final Uri CONTENT_URI = buildUri(Path.ALL_HOT);

        @InexactContentUri(
                path = Path.ALL_HOT + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.ALL_HOT,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.ALL_HOT, id);
        }
    }

    /**
     * all new
     */
    @TableEndpoint(table = RedditgoDatabase.ALL_NEW)
    public static class AllNew {
        @ContentUri(
                path = Path.ALL_NEW,
                type = "vnd.android.cursor.dir/" + Path.ALL_NEW)
        public static final Uri CONTENT_URI = buildUri(Path.ALL_NEW);

        @InexactContentUri(
                path = Path.ALL_NEW + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.ALL_NEW,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.ALL_NEW, id);
        }
    }

    /**
     * all rising
     */
    @TableEndpoint(table = RedditgoDatabase.ALL_RISING)
    public static class AllRising {
        @ContentUri(
                path = Path.ALL_RISING,
                type = "vnd.android.cursor.dir/" + Path.ALL_RISING)
        public static final Uri CONTENT_URI = buildUri(Path.ALL_RISING);

        @InexactContentUri(
                path = Path.ALL_RISING + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.ALL_RISING,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.ALL_RISING, id);
        }
    }

    /**
     * all controversal
     */
    @TableEndpoint(table = RedditgoDatabase.ALL_CONTROVERSAL)
    public static class AllControversal {
        @ContentUri(
                path = Path.ALL_CONTROVERSAL,
                type = "vnd.android.cursor.dir/" + Path.ALL_CONTROVERSAL)
        public static final Uri CONTENT_URI = buildUri(Path.ALL_CONTROVERSAL);

        @InexactContentUri(
                path = Path.ALL_CONTROVERSAL + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.ALL_CONTROVERSAL,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.ALL_CONTROVERSAL, id);
        }
    }

    /**
     * all top
     */
    @TableEndpoint(table = RedditgoDatabase.ALL_TOP)
    public static class AllTop {
        @ContentUri(
                path = Path.ALL_TOP,
                type = "vnd.android.cursor.dir/" + Path.ALL_TOP)
        public static final Uri CONTENT_URI = buildUri(Path.ALL_TOP);

        @InexactContentUri(
                path = Path.ALL_TOP + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.ALL_TOP,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.ALL_TOP, id);
        }
    }

    /**
     * submission hot
     */
    @TableEndpoint(table = RedditgoDatabase.SUBMISSION_HOT)
    public static class SubmissionHot {
        @ContentUri(
                path = Path.SUBMISSION_HOT,
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_HOT)
        public static final Uri CONTENT_URI = buildUri(Path.SUBMISSION_HOT);

        @InexactContentUri(
                path = Path.SUBMISSION_HOT + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION_HOT,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.SUBMISSION_HOT, id);
        }

        @InexactContentUri(
                path = Path.SUBMISSION_HOT + "/subreddit/*",
                name = "SUBREDDIT",
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_HOT,
                whereColumn = SubmissionColumn.SUBREDDIT,
                pathSegment = 1)
        public static Uri withSubreddit(String subreddit) {
            return buildUri(Path.SUBMISSION_HOT, subreddit);
        }
    }

    /**
     * Submission new
     */
    @TableEndpoint(table = RedditgoDatabase.SUBMISSION_NEW)
    public static class SubmissionNew {
        @ContentUri(
                path = Path.SUBMISSION_NEW,
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_NEW)
        public static final Uri CONTENT_URI = buildUri(Path.SUBMISSION_NEW);

        @InexactContentUri(
                path = Path.SUBMISSION_NEW + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION_NEW,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.SUBMISSION_NEW, id);
        }

        @InexactContentUri(
                path = Path.SUBMISSION_NEW + "/subreddit/*",
                name = "SUBREDDIT",
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_NEW,
                whereColumn = SubmissionColumn.SUBREDDIT,
                pathSegment = 1)
        public static Uri withSubreddit(String subreddit) {
            return buildUri(Path.SUBMISSION_NEW, subreddit);
        }
    }

    /**
     * submission rising
     */
    @TableEndpoint(table = RedditgoDatabase.SUBMISSION_RISING)
    public static class SubmissionRising {
        @ContentUri(
                path = Path.SUBMISSION_RISING,
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_RISING)
        public static final Uri CONTENT_URI = buildUri(Path.SUBMISSION_RISING);

        @InexactContentUri(
                path = Path.SUBMISSION_RISING + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION_RISING,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.SUBMISSION_RISING, id);
        }

        @InexactContentUri(
                path = Path.SUBMISSION_RISING + "/subreddit/*",
                name = "SUBREDDIT",
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_RISING,
                whereColumn = SubmissionColumn.SUBREDDIT,
                pathSegment = 1)
        public static Uri withSubreddit(String subreddit) {
            return buildUri(Path.SUBMISSION_RISING, subreddit);
        }
    }

    /**
     * submission controversal
     */
    @TableEndpoint(table = RedditgoDatabase.SUBMISSION_CONTROVERSAL)
    public static class SubmissionControversal {
        @ContentUri(
                path = Path.SUBMISSION_CONTROVERSAL,
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_CONTROVERSAL)
        public static final Uri CONTENT_URI = buildUri(Path.SUBMISSION_CONTROVERSAL);

        @InexactContentUri(
                path = Path.SUBMISSION_CONTROVERSAL + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION_CONTROVERSAL,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.SUBMISSION_CONTROVERSAL, id);
        }

        @InexactContentUri(
                path = Path.SUBMISSION_CONTROVERSAL + "/subreddit/*",
                name = "SUBREDDIT",
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_CONTROVERSAL,
                whereColumn = SubmissionColumn.SUBREDDIT,
                pathSegment = 1)
        public static Uri withSubreddit(String subreddit) {
            return buildUri(Path.SUBMISSION_CONTROVERSAL, subreddit);
        }
    }

    /**
     * submission top
     */
    @TableEndpoint(table = RedditgoDatabase.SUBMISSION_TOP)
    public static class SubmissionTop {
        @ContentUri(
                path = Path.SUBMISSION_TOP,
                type = "vnd.android.cursor.dir/" + Path.SUBMISSION_TOP)
        public static final Uri CONTENT_URI = buildUri(Path.SUBMISSION_TOP);

        @InexactContentUri(
                path = Path.SUBMISSION_TOP + "/*",
                name = "ID",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION_TOP,
                whereColumn = SubmissionColumn.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(Path.SUBMISSION_TOP, id);
        }

        @InexactContentUri(
                path = Path.SUBMISSION_TOP + "/subreddit/*",
                name = "SUBREDDIT",
                type = "vnd.android.cursor.item/" + Path.SUBMISSION_TOP,
                whereColumn = SubmissionColumn.SUBREDDIT,
                pathSegment = 1)
        public static Uri withSubreddit(String subreddit) {
            return buildUri(Path.SUBMISSION_TOP, subreddit);
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
                pathSegment = 1)
        public static Uri withId(String id) { return buildUri(Path.SUBREDDIT, id); }

    }

}
