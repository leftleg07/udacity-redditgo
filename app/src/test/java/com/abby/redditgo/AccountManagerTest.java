package com.abby.redditgo;

import net.dean.jraw.AccountPreferencesEditor;
import net.dean.jraw.ApiException;
import net.dean.jraw.managers.CaptchaHelper;
import net.dean.jraw.util.JrawUtils;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.AccountPreferences;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.KarmaBreakdown;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.ImportantUserPaginator;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserContributionPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * This class tests methods that require authentication, such as voting, saving, hiding, and posting.
 */
public class AccountManagerTest extends RedditTest {
    private static final String SUBMISSION_ID = "4k5sog";
    private static final String COMMENT_ID = "d3cdmuh";
    private String newSubmssionId;
    private String newCommentId;

    @Test
    public void testPostLink() {
        try {
            long number = epochMillis();

            URL url = JrawUtils.newUrl("https://www." + number + ".com");

            Submission submission = account.submit(
                    new AccountManager.SubmissionBuilder(url, "jraw_testing2", "Link post test (epoch=" + number + ")"));

            assertTrue(!submission.isSelfPost());
            assertTrue(submission.getUrl().equals(url.toExternalForm()));
            validateModel(submission);
            this.newSubmssionId = submission.getId();
        } catch (NetworkException e) {
            handle(e);
        } catch (ApiException e) {
            handlePostingQuota(e);
        }
    }

    @Test
    public void testPostSelfPost() {
        try {
            long number = epochMillis();
            String content = reddit.getUserAgent();

            Submission submission = account.submit(
                    new AccountManager.SubmissionBuilder(content, "jraw_testing2", "Self post test (epoch=" + number + ")"));

            assertTrue(submission.isSelfPost());
            assertTrue(submission.getSelftext().equals(content));
            validateModel(submission);
        } catch (NetworkException e) {
            handle(e);
        } catch (ApiException e) {
            handlePostingQuota(e);
        }
    }

    @Test
    public void testEditUserText() {
        String newText = "This is a new piece of text.";

        UserContributionPaginator p = getPaginator("submitted");
        p.setLimit(Paginator.RECOMMENDED_MAX_LIMIT);

        Listing<Contribution> submissions = p.next();
        Submission toEdit = null;
        for (Contribution c : submissions) {
            Submission s = (Submission) c;
            if (s.isSelfPost()) {
                toEdit = s;
            }
        }

        if (toEdit == null) {
            throw new IllegalStateException("Could not find any recent img_self posts");
        }

        try {
            account.updateContribution(toEdit, newText);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test(expectedExceptions = {ApiException.class, SkipException.class})
    public void testPostWithInvalidCaptcha() throws ApiException {
        CaptchaHelper helper = new CaptchaHelper(reddit);
        try {
            if (!helper.isNecessary()) {
                throw new SkipException("No captcha needed, request will return successfully either way");
            }
            account.submit(
                    new AccountManager.SubmissionBuilder("content", "jraw_testing2", "title"), helper.getNew(), "invalid captcha attempt");
        } catch (NetworkException e) {
            handle(e);
        } catch (ApiException e) {
            if (isRateLimit(e)) {
                // Nothing we can really do about this
                handlePostingQuota(e);
            }
            if (e.getReason().equals("BAD_CAPTCHA")) {
                // What we want
                throw e;
            }
            // Some other reason
            handle(e);
        }
    }

    @Test
    public void testReplySubmission() {
        try {
            String replyText = "" + epochMillis();
            Submission submission = reddit.getSubmission(SUBMISSION_ID);

            // Reply to a reply
            this.newCommentId = account.reply(submission, replyText);
            // Since only the _ID is returned, test the fullname
            assertTrue(JrawUtils.isFullname("t1_" + newCommentId));
        } catch (ApiException e) {
            handlePostingQuota(e);
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testReplyComment() {
        try {
            CommentNode comments = reddit.getSubmission(SUBMISSION_ID).getComments();
            Comment replyTo = null;
            for (CommentNode c : comments.walkTree()) {
                if (c.getComment().getId().equals(COMMENT_ID)) {
                    replyTo = c.getComment();
                    break;
                }
            }

            assertNotNull(replyTo);
            assertNotNull(account.reply(replyTo, "" + epochMillis()));
        } catch (NetworkException e) {
            handle(e);
        } catch (ApiException e) {
            handlePostingQuota(e);
        }
    }

    @Test(dependsOnMethods = "testReplySubmission")
    public void testDeleteComment() {
        try {
            moderation.delete("t1_" + newCommentId);

            for (CommentNode c : reddit.getSubmission(SUBMISSION_ID).getComments().walkTree()) {
                if (c.getComment().getId().equals(newCommentId)) {
                    fail("Found the (supposedly) deleted reply");
                }
            }
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test(dependsOnMethods = "testPostLink")
    public void testDeletePost() {
        try {
            moderation.delete(newSubmssionId);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }

        try {
            reddit.getSubmission(newSubmssionId);
        } catch (NetworkException e) {
            if (e.getResponse().getStatusCode() != 404) {
                fail("Did not get a 404 when querying the deleted reply", e);
            }
        }
    }

    @Test
    public void testSendRepliesToInbox() throws ApiException {
        try {
            Submission s = (Submission) getPaginator("submitted").next().get(0);
            account.sendRepliesToInbox(s, true);
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testVote() {
        try {
            String submissionId = "4k2aq2";
            Submission submission = reddit.getSubmission(submissionId);

            // Figure out a new vote direction: up if there is no vote, no vote if upvoted
            VoteDirection newVoteDirection = submission.getVote() == VoteDirection.NO_VOTE ? VoteDirection.UPVOTE : VoteDirection.NO_VOTE;
            account.vote(submission, newVoteDirection);

            submission = reddit.getSubmission(submissionId);
            // Make sure the vote took effect
            assertEquals(submission.getVote(), newVoteDirection);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testSaveSubmission() {
        try {
            Submission submission = reddit.getSubmission("3tox6a");
            account.save(submission);

            UserContributionPaginator paginator = getPaginator("saved");
            List<Contribution> saved = paginator.next();

            for (Contribution c : saved) {
                Submission s = (Submission) c;
                if (s.getId().equals(submission.getId())) {
                    return;
                }
            }

            fail("Did not find saved reply");

        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test(dependsOnMethods = "testSaveSubmission")
    public void testUnsaveSubmission() {
        try {
            Submission submission = reddit.getSubmission("3tox6a");
            account.unsave(submission);

            UserContributionPaginator paginator = getPaginator("saved");
            List<Contribution> saved = paginator.next();

            // Fail if we find the reply in the list
            for (Contribution s : saved) {
                if (s.getId().equals(submission.getId())) {
                    fail("Found the reply after it was unsaved");
                }
            }
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testHideSubmission() {
        try {
            Submission submission = reddit.getSubmission("3tox6a");
            account.hide(true, submission);

            UserContributionPaginator paginator = getPaginator("hidden");
            List<Contribution> hidden = paginator.next();

            for (Contribution c : hidden) {
                Submission s = (Submission) c;
                if (s.getId().equals(submission.getId())) {
                    return;
                }
            }

            fail("Did not find the reply in the hidden posts");
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test(dependsOnMethods = "testHideSubmission")
    public void testUnhideSubmission() {
        try {
            Submission submission = reddit.getSubmission("3tox6a");
            account.hide(false, submission);

            UserContributionPaginator paginator = getPaginator("hidden");
            List<Contribution> hidden = paginator.next();

            for (Contribution c : hidden) {
                Submission s = (Submission) c;
                if (s.getId().equals(submission.getId())) {
                    fail("Found unhidden reply in hidden posts");
                }
            }
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testSetNsfw() {
        try {
            Submission s = (Submission) getPaginator("submitted").next().get(0);
            boolean newVal = !s.isNsfw();

            moderation.setNsfw(s, newVal);

            // Reload the reply's data
            s = reddit.getSubmission(s.getId());
            assertTrue(s.isNsfw() == newVal);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testSubscribe() {
        try {
            Subreddit subreddit = reddit.getSubreddit("programming");
            boolean isSubscribed = isSubscribed(subreddit.getDisplayName());
            boolean expected = !isSubscribed;

            if (isSubscribed) {
                account.unsubscribe(subreddit);
            } else {
                account.subscribe(subreddit);
            }
            boolean actual = isSubscribed(subreddit.getDisplayName());
            assertEquals(actual, expected);
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testSticky() throws NetworkException {
        String modOf = getModeratedSubreddit().getDisplayName();
        SubredditPaginator paginator = new SubredditPaginator(reddit, modOf);

        Submission submission = null;
        List<Listing<Submission>> listingList = paginator.accumulate(3);
        for (Listing<Submission> submissions : listingList) {
            if (submissions.get(0).isStickied()) {
                // There is already a stickied post
                submission = submissions.get(0);
            } else {
                // Find the first img_self post
                for (Submission s : submissions) {
                    if (s.isSelfPost()) {
                        submission = s;
                        break;
                    }
                }
            }
        }

        if (submission == null)
            throw new IllegalStateException("No img_self posts in " + modOf);

        boolean expected = !(submission.isStickied());
        try {
            moderation.setSticky(submission, expected);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testGetFlairChoices() {
        try {
            String subreddit = "pcmasterrace"; // Glorious!
            List<FlairTemplate> templates = account.getFlairChoices(subreddit);
            validateModels(templates);

            validateModel(account.getCurrentFlair(subreddit));
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testSetFlair() {
        try {
            String subreddit = "jraw_testing2";
            FlairTemplate template = account.getFlairChoices(subreddit).get(0);

            moderation.setFlair(subreddit, template, null);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testEnableFlair() {
        try {
            account.setFlairEnabled("jraw_testing2", true);
        } catch (NetworkException | ApiException e) {
            handle(e);
        }
    }

    @Test
    public void testGetPreferences() {
        try {
            String[] names = {"over_18", "research", "hide_from_robots"};
            AccountPreferences prefs = account.getPreferences(Arrays.asList(names));
            // Only request preferences should be returned
            for (String name : names)
                assertNotNull(prefs.data(name));

            // Anything else should be null
            assertNull(prefs.data("lang"));
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testUpdatePreferences() {
        try {
            AccountPreferences original = account.getPreferences("over_18");
            AccountPreferencesEditor prefs = new AccountPreferencesEditor(original);
            validateModel(account.updatePreferences(prefs));
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testGetTrophies() {
        try {
            validateModels(reddit.getTrophies());
            validateModels(reddit.getTrophies("spladug"));
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testKarmaBreakdown() {
        try {
            KarmaBreakdown breakdown = account.getKarmaBreakdown();
            validateModel(breakdown);

            for (String subreddit : breakdown.getSummaries().keySet()) {
                // Make sure the img_link and reply karma properties are not null
                breakdown.getCommentKarma(subreddit);
                breakdown.getLinkKarma(subreddit);
            }
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testGetFriend() {
        try {
            validateModel(account.getFriend(getFriend().getFullName()));
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testAddFriend() {
        try {
            validateModel(account.updateFriend("spladug"));
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testDeleteFriend() {
        try {
            account.deleteFriend(getFriend().getFullName());
        } catch (NetworkException e) {
            handle(e);
        }
    }

    private UserRecord getFriend() throws NetworkException {
        Listing<UserRecord> friends = new ImportantUserPaginator(reddit, "friends").next();
        if (friends.size() == 0) {
            account.updateFriend("jraw_test3");
            return getFriend();
        }

        return friends.get(0);
    }

    private boolean isSubscribed(String subreddit) {
        UserSubredditsPaginator paginator = new UserSubredditsPaginator(reddit, "subscriber");
        paginator.setLimit(Paginator.RECOMMENDED_MAX_LIMIT);

        // Try to find the subreddit in the list of subscribed subs
        while (paginator.hasNext()) {
            Listing<Subreddit> subscribed = paginator.next();
            for (Subreddit sub : subscribed) {
                if (sub.getDisplayName().equals(subreddit)) {
                    return true;
                }
            }
        }

        return false;
    }

    private UserContributionPaginator getPaginator(String where) {
        return new UserContributionPaginator(reddit, where, reddit.getAuthenticatedUser());
    }
}
