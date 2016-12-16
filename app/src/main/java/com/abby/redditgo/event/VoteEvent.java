package com.abby.redditgo.event;

import net.dean.jraw.models.Submission;

/**
 * Created by gsshop on 2016. 12. 6..
 */
public class VoteEvent {
    public final Submission submission;

    public VoteEvent(Submission submission) {
        this.submission = submission;
    }
}
