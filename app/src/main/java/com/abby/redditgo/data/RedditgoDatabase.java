package com.abby.redditgo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.OnConfigure;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by gsshop on 2017. 1. 16..
 */
@Database(fileName = RedditgoContract.DATABASE_NAME, version = RedditgoContract.DATABASE_VERSION)
public final class RedditgoDatabase {

    /**
     * submission
     */
    @Table(SubmissionColumn.class) public static final String SUBMISSION_HOT = RedditgoContract.TABLE_NAME_SUBMISSION_HOT;
    @Table(SubmissionColumn.class) public static final String SUBMISSION_NEW = RedditgoContract.TABLE_NAME_SUBMISSION_NEW;
    @Table(SubmissionColumn.class) public static final String SUBMISSION_RISING = RedditgoContract.TABLE_NAME_SUBMISSION_RISING;
    @Table(SubmissionColumn.class) public static final String SUBMISSION_CONTROVERSAL = RedditgoContract.TABLE_NAME_SUBMISSION_CONTROVERSAL;
    @Table(SubmissionColumn.class) public static final String SUBMISSION_TOP = RedditgoContract.TABLE_NAME_SUBMISSION_TOP;

    /**
     * front page
     */
    @Table(SubmissionColumn.class) public static final String FRONT_PAGE_HOT = RedditgoContract.TABLE_NAME_FRONT_PAGE_HOT;
    @Table(SubmissionColumn.class) public static final String FRONT_PAGE_NEW = RedditgoContract.TABLE_NAME_FRONT_PAGE_NEW;
    @Table(SubmissionColumn.class) public static final String FRONT_PAGE_RISING = RedditgoContract.TABLE_NAME_FRONT_PAGE_RISING;
    @Table(SubmissionColumn.class) public static final String FRONT_PAGE_CONTROVERSAL = RedditgoContract.TABLE_NAME_FRONT_PAGE_CONTROVERSAL;
    @Table(SubmissionColumn.class) public static final String FRONT_PAGE_TOP = RedditgoContract.TABLE_NAME_FRONT_PAGE_TOP;

    /**
     * all
     */
    @Table(SubmissionColumn.class) public static final String ALL_HOT = RedditgoContract.TABLE_NAME_ALL_HOT;
    @Table(SubmissionColumn.class) public static final String ALL_NEW = RedditgoContract.TABLE_NAME_ALL_NEW;
    @Table(SubmissionColumn.class) public static final String ALL_RISING = RedditgoContract.TABLE_NAME_ALL_RISING;
    @Table(SubmissionColumn.class) public static final String ALL_CONTROVERSAL = RedditgoContract.TABLE_NAME_ALL_CONTROVERSAL;
    @Table(SubmissionColumn.class) public static final String ALL_TOP = RedditgoContract.TABLE_NAME_ALL_TOP;


    /**
     * subreddit
     */
    @Table(SubredditColumn.class) public static final String SUBREDDIT = RedditgoContract.TABLE_NAME_SUBREDDIT;

    @OnCreate
    public static void onCreate(Context context, SQLiteDatabase db) {
    }

    @OnUpgrade
    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion,
                                 int newVersion) {
    }

    @OnConfigure
    public static void onConfigure(SQLiteDatabase db) {
    }

//    @ExecOnCreate
//    public static final String EXEC_ON_CREATE = "SELECT * FROM " + FRONT_PAGE_HOT;
}
