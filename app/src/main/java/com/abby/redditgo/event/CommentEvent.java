package com.abby.redditgo.event;

import net.dean.jraw.models.CommentNode;

import java.util.List;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentEvent {
    public final List<CommentNode> nodes;
    public final boolean first;

    public CommentEvent(List<CommentNode> nodes, boolean first) {
        this.nodes = nodes;
        this.first = first;
    }
}
