package com.abby.redditgo.network;

import com.abby.redditgo.BuildConfig;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by gsshop on 2016. 10. 11..
 */
public class RedditApi {
    private static final String TAG = RedditApi.class.getSimpleName();

    public static void anonymous(UUID deviceId) {
        String clientId = BuildConfig.REDDIT_INSTALLED_CLIENT_ID;

        String redirectUrl = BuildConfig.REDDIT_INSTALLED_REDIRECT_URI;
        Credentials credentials = Credentials.userlessApp(clientId, deviceId);
        RedditClient reddit = AuthenticationManager.get().getRedditClient();
        try {
            OAuthData data = reddit.getOAuthHelper().easyAuth(credentials);
            reddit.authenticate(data);
        } catch (OAuthException e) {
           Logger.e(e, "Could not authenticate");
        }
    }
    public static boolean isAuthorized() {
        RedditClient reddit = AuthenticationManager.get().getRedditClient();
        return reddit.isAuthenticated() && reddit.hasActiveUserContext();
    }

    public static String signIn(String username, String password) {
        String clientId = BuildConfig.REDDIT_SCRIPT_CLIENT_ID;
        String clientSecret = BuildConfig.REDDIT_SCRIPT_CLIENT_SECRET;

        Credentials credentials = Credentials.script(username, password, clientId, clientSecret);
        RedditClient reddit = AuthenticationManager.get().getRedditClient();
        try {
            OAuthData data = reddit.getOAuthHelper().easyAuth(credentials);
            reddit.authenticate(data);
            return AuthenticationManager.get().getRedditClient().getAuthenticatedUser();
        } catch (Exception e) {
            Logger.e(e, "Could not log in");
            return null;
        }
    }

    /**
     * fetch subreddits
     */
    public static List<Subreddit> fetchSubreddits() {
        RedditClient reddit = AuthenticationManager.get().getRedditClient();

        UserSubredditsPaginator paginator = new UserSubredditsPaginator(reddit, "subscriber");
        List<Subreddit> latestSubreddits = new ArrayList<>();
        while (paginator.hasNext()) {
            Listing<Subreddit> subreddits = paginator.next();
            for (Subreddit subreddit : subreddits) {
                if (!subreddit.isNsfw()) {
                    latestSubreddits.add(subreddit);
                }
            }
        }

        // sorting
        if(!latestSubreddits.isEmpty()) {
            Collections.sort(latestSubreddits, new Comparator<Subreddit>() {
                @Override
                public int compare(Subreddit o1, Subreddit o2) {
                    return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
                }
            });
        }

       return latestSubreddits;
    }


    /**
     * fetch subreddit's submissions
     */
    public static List<Submission> fetchSubmissions(String subreddit, Sorting sorting) {
        RedditClient reddit = AuthenticationManager.get().getRedditClient();
        SubredditPaginator paginator = new SubredditPaginator(reddit, subreddit);
        paginator.setSorting(sorting);
        List<Submission> latestSubmissions = new ArrayList<>();
        while (paginator.hasNext()) {
            Listing<Submission> submissions = paginator.next();
            for (Submission submission : submissions) {
                if (!submission.isNsfw()) {
                    latestSubmissions.add(submission);
                }
            }
        }

        return latestSubmissions;
    }

}
