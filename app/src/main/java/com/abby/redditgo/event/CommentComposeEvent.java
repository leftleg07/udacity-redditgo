package com.abby.redditgo.event;

/**
 * Created by gsshop on 2017. 2. 23..
 */
public class CommentComposeEvent {
    public final String errorMessage;

    public CommentComposeEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
