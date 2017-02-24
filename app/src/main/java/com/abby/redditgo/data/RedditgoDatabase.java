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
    @Table(SubmissionColumn.class) public static final String SUBMISSION = RedditgoContract.TABLE_NAME_SUBMISSION;

    /**
     * front page
     */
    @Table(SubmissionColumn.class) public static final String FRONT_PAGE = RedditgoContract.TABLE_NAME_FRONT_PAGE;


    /**
     * all
     */
    @Table(SubmissionColumn.class) public static final String ALL = RedditgoContract.TABLE_NAME_ALL;


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
//    public static final String EXEC_ON_CREATE = "SELECT * FROM " + FRONT_PAGE;
}
