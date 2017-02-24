package com.abby.redditgo.event;

/**
 * Created by gsshop on 2016. 10. 31..
 */

public class LoginEvent {
    public final String username;

    public LoginEvent(String username) {
        this.username = username;
    }
}
