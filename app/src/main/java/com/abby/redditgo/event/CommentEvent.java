package com.abby.redditgo.event;

import net.dean.jraw.models.CommentNode;

import java.util.List;

/**
 * Created by gsshop on 2016. 12. 7..
 */
public class CommentEvent {
    public final List<CommentNode> nodes;
    public final int index;

    public CommentEvent(List<CommentNode> nodes, int index) {
        this.nodes = nodes;
        this.index = index;
    }
}
