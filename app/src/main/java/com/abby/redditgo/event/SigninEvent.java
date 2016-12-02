package com.abby.redditgo.event;

/**
 * Created by gsshop on 2016. 10. 31..
 */

public class SigninEvent {
    public final String username;

    public SigninEvent(String username) {
        this.username = username;
    }
}
