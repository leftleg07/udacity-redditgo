package com.abby.redditgo.network;

import com.abby.redditgo.BuildConfig;
import com.fasterxml.jackson.databind.node.NullNode;
import com.orhanobut.logger.Logger;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
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
    private static final RedditClient reddit;
    private static final AccountManager account;
    private static final ModerationManager moderation;

    static {
        reddit = AuthenticationManager.get().getRedditClient();
        account = new AccountManager(reddit);
        moderation = new ModerationManager(reddit);
    }

    public static void anonymous(UUID deviceId) {
        String clientId = BuildConfig.REDDIT_INSTALLED_CLIENT_ID;

        String redirectUrl = BuildConfig.REDDIT_INSTALLED_REDIRECT_URI;
        Credentials credentials = Credentials.userlessApp(clientId, deviceId);
        try {
            OAuthData data = reddit.getOAuthHelper().easyAuth(credentials);
            reddit.authenticate(data);
        } catch (OAuthException e) {
            Logger.e(e, "Could not authenticate");
        }
    }

    public static boolean isAuthorized() {
        return reddit.isAuthenticated() && reddit.hasActiveUserContext();
    }

    /**
     * sign in
     * @param username
     * @param password
     * @return
     */
    public static String signin(String username, String password) {
        String clientId = BuildConfig.REDDIT_SCRIPT_CLIENT_ID;
        String clientSecret = BuildConfig.REDDIT_SCRIPT_CLIENT_SECRET;

        Credentials credentials = Credentials.script(username, password, clientId, clientSecret);
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
        if (!latestSubreddits.isEmpty()) {
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

    /**
     * logged in account
     * @return
     */
    public static LoggedInAccount account() {
        return reddit.me();
    }

    public static Submission vote(String submissionId, VoteDirection voteDirection) throws ApiException {
        Submission submission = reddit.getSubmission(submissionId);
        account.vote(submission, voteDirection);
        return reddit.getSubmission(submission.getId());

    }

    /**
     * get the submission comment
     */
    public static CommentNode comments(String submissionId, CommentSort sort) throws NetworkException {
        SubmissionRequest request = new SubmissionRequest.Builder(submissionId).sort(sort).build();
        return reddit.getSubmission(request).getComments();
    }

    public static List<CommentNode> moreComments(CommentNode node) throws NetworkException {
        return node.loadMoreComments(reddit);
    }

    /**
     * Sends a reply to a Submission
     * @param submissionId
     * @param replyText
     * @return
     * @throws NetworkException
     * @throws ApiException
     */
    public static String replySubmission(String submissionId, String replyText) throws NetworkException, ApiException {
        Submission submission = reddit.getSubmission(submissionId);
        return new AccountManager(reddit).reply(submission, replyText);
    }


    /**
     * Sends a reply to a Comment
     * @param fullname
     * @param replyText
     * @return
     * @throws NetworkException
     * @throws ApiException
     */
    public static String replyComment(final String fullname, String replyText) throws NetworkException, ApiException {
        return account.reply(new Contribution(NullNode.getInstance()) {
            @Override
            public String getFullName() {
                return fullname;
            }
        }, replyText);
    }

    /**
     * delete comment
     * @param fullname
     * @throws NetworkException
     * @throws ApiException
     */
    public static void deleteComment(String fullname) throws NetworkException, ApiException {
        moderation.delete(fullname);
    }

    /**
     * Votes on a comment or submission
     * @param comment
     * @param voteDirection
     * @throws NetworkException
     * @throws ApiException
     */
    public static void vote(Comment comment, VoteDirection voteDirection) throws NetworkException,  ApiException {
        account.vote(comment, voteDirection);
    }

    public static void loadFully(CommentNode node) {
        node.loadFully(reddit);
    }


}
