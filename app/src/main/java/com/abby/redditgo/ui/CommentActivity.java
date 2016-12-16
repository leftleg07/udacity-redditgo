package com.abby.redditgo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.abby.redditgo.BaseActivity;
import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.event.CommentReplyEvent;
import com.abby.redditgo.job.CommentFetchJob;
import com.abby.redditgo.job.CommentReplyJob;
import com.abby.redditgo.model.MyComment;
import com.abby.redditgo.model.MyContent;
import com.abby.redditgo.network.RedditApi;
import com.birbit.android.jobqueue.JobManager;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentActivity extends BaseActivity {

    public static final String EXTRA_SUBMISSION_ID = "_extra_submission_id";
    @Inject
    JobManager mJobManager;

    @BindView(R.id.recyclerView_comment)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton mFAB;

    private CommentAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private String submissionId;
    private List<MyComment> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submissionId = getIntent().getStringExtra(EXTRA_SUBMISSION_ID);
        mJobManager.addJobInBackground(new CommentFetchJob(submissionId));

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                mAdapter.toggleGroup(position);
            }
        }));

        mAdapter = new CommentAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

    }

    private MyContent getDummyContent() {
        return new MyContent("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam sed odio scelerisque, condimentum neque non, venenatis neque. Mauris nec feugiat felis, id porta nibh. In hac habitasse platea dictumst. Phasellus egestas rutrum justo, sit amet pharetra nulla egestas non. Vivamus ultricies ligula id mauris viverra, mattis volutpat turpis hendrerit. In hac habitasse platea dictumst. Nulla congue, lorem eu placerat luctus, metus lacus convallis est, non porta tellus nisi ut neque. Pellentesque posuere gravida tincidunt. Maecenas aliquet, nulla id vestibulum elementum, enim leo mattis ipsum, in lobortis quam enim pretium justo.");
    }

    @Override
    protected void injectActivity(ApplicationComponent component) {
        component.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }



    @Subscribe
    public void onCommentEvent(CommentEvent event) {
        comments = new ArrayList<MyComment>();
        CommentNode node = event.node;

        makeComments(node, comments);

        MyContent content = getDummyContent();
        mAdapter.clear();
        mAdapter.add(content);
        mAdapter.addAll(comments);
        mAdapter.notifyDataSetChanged();
//        mJobManager.addJobInBackground(new CommentMoreJob(node));

    }

    private void makeComments(CommentNode node, List<MyComment> subComments) {
        for (CommentNode child : node.getChildren()) {
            Comment comment = child.getComment();

            MyComment myComment = new MyComment(comment, child.getDepth());
            if (child.getDepth() > 1) {
                this.comments.add(myComment);
            }
            subComments.add(myComment);

            makeComments(child, (List<MyComment>) myComment.getChildren());
        }
    }

    @OnClick(R.id.fab)
    public void onFABClick(View view) {
        if(RedditApi.isAuthorized()) {
            mJobManager.addJobInBackground(new CommentReplyJob(submissionId, "Chris Farley was a comedy genius. Such a shame that he left us too early."));
        } else {
            Snackbar.make(mFAB, "Please sign in to do that.", Snackbar.LENGTH_LONG)
                    .setAction("Sign in", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ContextCompat.startActivity(CommentActivity.this, new Intent(CommentActivity.this, LoginActivity.class), null);
                        }
                    }).show();
        }
    }

    @Subscribe
    public void onCommentReplyEvent(CommentReplyEvent event) {
        if(event.newCommentId != null) {
            mJobManager.addJobInBackground(new CommentFetchJob(submissionId));
        } else {
            Snackbar.make(mFAB, event.errorMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
