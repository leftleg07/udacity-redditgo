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
    String TABLE_NAME_SUBMISSION_HOT = "submission_hot";
    String TABLE_NAME_SUBMISSION_NEW = "submission_new";
    String TABLE_NAME_SUBMISSION_RISING = "submission_rising";
    String TABLE_NAME_SUBMISSION_CONTROVERSAL = "submission_controversal";
    String TABLE_NAME_SUBMISSION_TOP = "submission_top";

    /**
     * front_page table
     */
    String TABLE_NAME_FRONT_PAGE_HOT = "front_page_hot";
    String TABLE_NAME_FRONT_PAGE_NEW = "front_page_new";
    String TABLE_NAME_FRONT_PAGE_RISING = "front_page_rising";
    String TABLE_NAME_FRONT_PAGE_CONTROVERSAL = "front_page_controversal";
    String TABLE_NAME_FRONT_PAGE_TOP = "front_page_top";

    /**
     * all table
     */
    String TABLE_NAME_ALL_HOT = "all_hot";
    String TABLE_NAME_ALL_NEW = "all_new";
    String TABLE_NAME_ALL_RISING = "all_rising";
    String TABLE_NAME_ALL_CONTROVERSAL = "all_controversal";
    String TABLE_NAME_ALL_TOP = "all_top";

    /**
     * subreddit table
     */
    String TABLE_NAME_SUBREDDIT = "subreddit";
}
