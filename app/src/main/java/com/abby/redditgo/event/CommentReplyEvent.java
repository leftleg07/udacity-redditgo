package com.abby.redditgo.event;

/**
 * Created by gsshop on 2016. 12. 14..
 */
public class CommentReplyEvent {
    public final String newCommentId;
    public final String errorMessage;

    public CommentReplyEvent(String newCommentId, String errorMessage) {
        this.newCommentId = newCommentId;
        this.errorMessage = errorMessage;
    }
}
