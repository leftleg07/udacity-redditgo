package com.abby.redditgo.event;

import net.dean.jraw.paginators.Sorting;

/**
 * Created by gsshop on 2017. 1. 4..
 */
public class RefreshShowEvent {
    public String subreddit;
    public Sorting sorting;

    public RefreshShowEvent(String subreddit, Sorting sorting) {
        this.subreddit = subreddit;
        this.sorting = sorting;
    }
}
