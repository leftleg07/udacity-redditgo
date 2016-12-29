package com.abby.redditgo.event;

/**
 * Created by gsshop on 2016. 12. 14..
 */
public class CommentErrorEvent {
    public final String errorMessage;

    public CommentErrorEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
