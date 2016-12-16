package com.abby.redditgo.event;

import net.dean.jraw.models.LoggedInAccount;

/**
 * Created by gsshop on 2016. 12. 5..
 */
public class AccountEvent {
    public final LoggedInAccount account;
    public AccountEvent(LoggedInAccount account) {
        this.account = account;
    }
}
