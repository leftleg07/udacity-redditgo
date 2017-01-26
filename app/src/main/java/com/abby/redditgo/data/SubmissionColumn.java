package com.abby.redditgo.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by gsshop on 2017. 1. 16..
 */

public interface SubmissionColumn {
    @PrimaryKey
    @DataType(TEXT)
    String ID = "_id";
    @DataType(TEXT)
    String POST_HINT = "post_hint";
    @DataType(TEXT)
    String URL = "url";
    @DataType(TEXT)
    String TITLE = "title";
    @DataType(TEXT)
    String VOTE = "likes";
    @DataType(INTEGER)
    String CREATED_TIME = "created_time";
    @DataType(TEXT)
    String AUTHOR = "author";
    @DataType(TEXT)
    String SUBREDDIT = "subreddit";
    @DataType(INTEGER)
    String SCORE = "score";
    @DataType(INTEGER)
    String NUM_COMMENTS = "num_comments";
    @DataType(TEXT)
    String THUMBNAIL = "thumbnail";
}
