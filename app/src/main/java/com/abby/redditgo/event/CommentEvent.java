package com.abby.redditgo.event;

import net.dean.jraw.models.CommentNode;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentEvent {
    public final CommentNode node;

    public CommentEvent(CommentNode node) {
        this.node = node;
    }
}
