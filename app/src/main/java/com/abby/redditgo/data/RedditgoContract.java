package com.abby.redditgo.data;

/**
 * Created by gsshop on 2017. 1. 16..
 */

public interface RedditgoContract {
    String DATABASE_NAME = "redditgo.db";
    int DATABASE_VERSION = 1;

    String AUTHORITY = "com.abby.redditgo.provider";

    /**
     * submission table
     */
    String TABLE_NAME_SUBMISSION = "submission";

    /**
     * front_page table
     */
    String TABLE_NAME_FRONT_PAGE = "submission_front_page";

    /**
     * all table
     */
    String TABLE_NAME_ALL = "submission_all";

    /**
     * subreddit table
     */
    String TABLE_NAME_SUBREDDIT = "subreddit";
}
