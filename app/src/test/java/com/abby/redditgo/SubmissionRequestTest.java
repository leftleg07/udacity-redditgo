package com.abby.redditgo;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests for SubmissionRequest and the responses it produces
 */
public class SubmissionRequestTest extends RedditTest {
    private static final String SUBMISSION_ID = "92dd8";
//    private static final String ID = "5m2ivk";
    private static final String FOCUS_COMMENT_ID = "c0b73aj";
    private SubmissionRequest.Builder request;
    private Submission s;

    private static final String SUBMISSION_WITH_NULL_THUMBNAIL = "4b85hi";

    @BeforeMethod
    public void setUp() {
        this.request = new SubmissionRequest.Builder(SUBMISSION_ID);
    }

    @Test
    public void testDepth() {
        // Change depth to only top-level comments
        request.depth(1);
        s = get();
        for (CommentNode c : s.getComments().walkTree()) {
            if (c.getChildren() == null) {
                continue;
            }
            assertEquals(c.getChildren().size(), 0);
        }
    }

    @Test
    public void testFocus() {
        // Change the focused reply
        request.focus(FOCUS_COMMENT_ID);
        s = get();
        // The top level reply should be the focused one
        assertEquals(s.getComments().get(0).getComment().getId(), FOCUS_COMMENT_ID);
    }

    @Test
    public void testContext() {
        // Change the context while keeping the focus
        request.focus(FOCUS_COMMENT_ID);
        request.context(3);
        s = get();
        // The top level reply should be a parent of the focused one
        assertNotEquals(s.getComments().get(0).getComment().getId(), FOCUS_COMMENT_ID);
    }

    @Test
    public void testSort() {
        request.sort(CommentSort.TOP);
        s = get();
        int prevScore = s.getComments().get(0).getComment().getScore();
        for (int i = 1; i < 5; i++) {
            Comment c = s.getComments().get(i).getComment();
            assertTrue(prevScore >= c.getScore());
            prevScore = c.getScore();
        }
    }

    @Test
    public void testLimit() {
        request.sort(CommentSort.TOP);
        CommentNode node = get().getComments();

        while(node.hasMoreComments()) {
            node.loadMoreComments(reddit);
        }

//        assertEquals(node.getTotalSize(), 1);
    }

    @Test
    public void testNullThumbnail() {
        try {
            Submission s = reddit.getSubmission(SUBMISSION_WITH_NULL_THUMBNAIL);
            Submission.ThumbnailType t = s.getThumbnailType();
            assertEquals(Submission.ThumbnailType.NONE, t);
        } catch (NetworkException e) {
            handle(e);
        }
    }

    private Submission get() {
        try {
            return reddit.getSubmission(request.build());
        } catch (NetworkException e) {
            handle(e);
            return null;
        }
    }
}
