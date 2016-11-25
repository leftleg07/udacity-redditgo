package com.abby.redditgo.event;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;

import java.util.List;

/**
 * Created by gsshop on 2016. 11. 1..
 */

public class SubmissionEvent {
    public List<Submission> submissions;
    public Sorting mSorting;
    public int mPage;

    public SubmissionEvent(List<Submission> submissions, Sorting sorting, int page) {
        this.submissions = submissions;
        this.mSorting = sorting;
        this.mPage = page;
    }
}
