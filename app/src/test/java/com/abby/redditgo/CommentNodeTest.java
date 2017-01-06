package com.abby.redditgo;

import com.google.common.collect.ImmutableMap;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.LocationHint;
import net.dean.jraw.models.TraversalMethod;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CommentNodeTest extends RedditTest {
    private final CommentNode simpleTree = null;
    private final int simpleTreeSize = 8;

//    public CommentNodeTest() {
//        super();
//        this.simpleTree = null;
////        this.simpleTree = reddit.getSubmission("2zsyu4").getComments();
//    }

    @Test
    public void testDepth() {
        Map<String, Integer> expectedDepths = ImmutableMap.<String, Integer>builder()
                .put("a", 1)
                .put("b", 2)
                .put("c", 2)
                .put("d", 2)
                .put("f", 3)
                .put("g", 3)
                .put("h", 3)
                .put("i", 3)
                .build();

        for (CommentNode n : simpleTree.walkTree()) {
            // Ensure the calculated depth matches the expected depth
            assertEquals(n.getDepth(), (int) expectedDepths.get(n.getComment().getBody()),
                    "Node not at expected depth (node=" + n.getComment().getBody() + ")");
        }
    }

    @Test
    public void testLoadMoreComments() {
        try {
//            CommentNode node = reddit.getSubmission("92dd8").getComments();
            SubmissionRequest.Builder request = new SubmissionRequest.Builder("5m40x3");
            request.sort(CommentSort.HOT);
            CommentNode node = reddit.getSubmission(request.build()).getComments();
            while(node.hasMoreComments()) {
                node.loadMoreComments(reddit);
            }
        } catch (NetworkException e) {
            handle(e);
        }
    }

    @Test
    public void testVisualize() {
        // Nothing we can really test here besides it if throws an exception
        simpleTree.visualize();
    }

    @Test
    public void testFindChild() {
        assertTrue(simpleTree.findChild("t1_cplzd5h").isPresent());
    }

    @Test
    public void testFindChildMoreMethods() {
        for (TraversalMethod method : TraversalMethod.values()) {
            assertTrue(simpleTree.findChild("t1_cplzd5h", LocationHint.of(method)).isPresent());
        }
    }

    @Test
    public void testTopLevel() {
        CommentNode node = simpleTree;
        // The first element in a pre-order traversal is logically a root node
        assertTrue(node.walkTree(TraversalMethod.PRE_ORDER).first().get().isTopLevel());

        // Likewise the first element in a post-order traversal should *not* be a root node
        assertFalse(node.walkTree(TraversalMethod.POST_ORDER).first().get().isTopLevel());
    }

    @Test
    public void testWalkPreorder() {
        testWalkTree(TraversalMethod.PRE_ORDER);
    }

    @Test
    public void testWalkPostorder() {
        testWalkTree(TraversalMethod.POST_ORDER);
    }
    @Test
    public void testWalkBreadthFirst() {
        testWalkTree(TraversalMethod.BREADTH_FIRST);
    }

    private void testWalkTree(TraversalMethod method) {
        // Test size from root
        assertEquals(calculateTotal(simpleTree, method), simpleTreeSize);

        // Test size from some other node
        CommentNode node = simpleTree.get(0).get(1); // Node 'c' in the tree
        int expected = 3 + 1; // Children size + parent node
        assertEquals(calculateTotal(node, method), expected);

    }

    private int calculateTotal(CommentNode root, TraversalMethod method) {
        return root.walkTree(method).size();
    }

    @Test
    public void testTotalSize() {
        assertEquals(simpleTree.getTotalSize(), simpleTreeSize);
    }

    @Test
    public void testImmediateSize() {
        // Test size of node 'a'
        assertEquals(simpleTree.get(0).getImmediateSize(), 3);
    }

    @Test
    public void testLoadFully() {
        CommentNode node = reddit.getSubmission("2kx1ly").getComments();
        node.loadFully(reddit);

        for (CommentNode child : node.walkTree()) {
            assertFalse(child.hasMoreComments(), "Child had more comments: " + child);
        }
        assertFalse(node.hasMoreComments(), "Root node had more comments: " + node);
    }

    @Test
    public void testContinueThread() {
        CommentNode node = reddit.getSubmission("2onit4").getComments();
        node.loadFully(reddit);
        CommentNode subject = null;
        for (CommentNode n : node.walkTree()) {
            if (n.isThreadContinuation()) {
                subject = n;
                break;
            }
        }

        if (subject == null)
            throw new IllegalStateException("Could not find a node that is a thread continuation");

        subject.loadMoreComments(reddit);
        assertFalse(subject.hasMoreComments());
        assertFalse(subject.isThreadContinuation());
        assertTrue(subject.getImmediateSize() > 0);
    }
}
