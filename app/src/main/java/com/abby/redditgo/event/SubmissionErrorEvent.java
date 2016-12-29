package com.abby.redditgo.event;

/**
 * Created by gsshop on 2016. 12. 14..
 */
public class SubmissionErrorEvent {
    public final String errorMessage;

    public SubmissionErrorEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
