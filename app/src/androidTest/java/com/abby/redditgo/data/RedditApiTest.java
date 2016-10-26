package com.abby.redditgo.data;

import android.support.test.runner.AndroidJUnit4;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;
import static net.dean.jraw.auth.AuthenticationState.NONE;
import static net.dean.jraw.auth.AuthenticationState.READY;

/**
 * Created by gsshop on 2016. 10. 20..
 */
@RunWith(AndroidJUnit4.class)
public class RedditApiTest {
    private void login() {
        String username = "e07skim";
        String password = "eskim3164";
        String user = RedditApi.signIn(username, password);
        assertThat(user).isEqualTo(username);
    }
    
    @Test
    public void testSignIn() throws Exception {
        login();
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        assertThat(state).isEqualTo(READY);

    }

    @Test
    public void testFetchSubreddits() throws Exception {
        if (!RedditApi.isAuthorized()) {
            login();
        }

        RedditApi.fetchSubreddits();
    }

    @Test
    public void testFetchFrontPage() throws Exception {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if(state == NONE) {
            RedditApi.anonymous();
        }
        RedditApi.fetchFrontPage();

    }

    @Test
    public void testFetchAllSubmissions() throws Exception {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if(state == NONE) {
            RedditApi.anonymous();
        }
        RedditApi.fetchSubmissions("all");

    }
}
