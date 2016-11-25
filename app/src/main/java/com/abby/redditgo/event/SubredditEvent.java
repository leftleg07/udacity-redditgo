package com.abby.redditgo.event;

import net.dean.jraw.models.Subreddit;

import java.util.List;

/**
 * Created by gsshop on 2016. 10. 31..
 */

public class SubredditEvent {
    public List<Subreddit> subreddits;

    public SubredditEvent(List<Subreddit> subreddits) {
        this.subreddits = subreddits;
    }
}
