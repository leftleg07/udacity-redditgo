package com.abby.redditgo.ui.comment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.AttemptLoginEvent;
import com.abby.redditgo.event.CommentErrorEvent;
import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.event.CommentRefreshEvent;
import com.abby.redditgo.event.SigninEvent;
import com.abby.redditgo.job.CommentFetchJob;
import com.abby.redditgo.job.CommentReplySubmissionJob;
import com.abby.redditgo.job.JobId;
import com.abby.redditgo.model.MyComment;
import com.abby.redditgo.model.MyContent;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.BaseActivity;
import com.abby.redditgo.ui.login.LoginActivity;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private CommentAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private String submissionId;
    private String mLastCommentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

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
        mJobManager.addJobInBackground(new CommentFetchJob(submissionId, CommentSort.HOT));

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CommentAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        /*
        * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
        * performs a swipe-to-refresh gesture.
        */
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        updateOperation();
                    }
                }
        );

    }

    private MyContent getDummyContent() {
        return new MyContent("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam sed odio scelerisque, condimentum neque non, venenatis neque. Mauris nec feugiat felis, id porta nibh. In hac habitasse platea dictumst. Phasellus egestas rutrum justo, sit amet pharetra nulla egestas non. Vivamus ultricies ligula id mauris viverra, mattis volutpat turpis hendrerit. In hac habitasse platea dictumst. Nulla congue, lorem eu placerat luctus, metus lacus convallis est, non porta tellus nisi ut neque. Pellentesque posuere gravida tincidunt. Maecenas aliquet, nulla id vestibulum elementum, enim leo mattis ipsum, in lobortis quam enim pretium justo.");
    }

    @Override
    protected void injectActivity(ApplicationComponent component) {
        component.inject(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    /*
     * Listen for option item selections so that we receive a notification
     * when the user requests a refresh by selecting the refresh action bar item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                return true;
        }

        // User didn't trigger a refresh, let the superclass handle this action
        return super.onOptionsItemSelected(item);

    }

    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_submission, popup.getMenu());
//        popup.getMenu().findItem(mLastFilterId).setChecked(true);
//
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                if (mListener == null) {
//                    return false;
//                }
//                onRefreshShowEvent(null);
//                item.setChecked(true);
//                switch (item.getItemId()) {
//                    case R.id.menu_hot:
//                        mListener.onSubTitleChange(R.string.filter_hot);
//                        break;
//                    case R.id.menu_new:
//                        mListener.onSubTitleChange(R.string.filter_new);
//                        break;
//                    case R.id.menu_rising:
//                        mListener.onSubTitleChange(R.string.filter_rising);
//                        break;
//                    case R.id.menu_controversial:
//                        mListener.onSubTitleChange(R.string.filter_controversial);
//                        break;
//                    case R.id.menu_top:
//                        mListener.onSubTitleChange(R.string.filter_top);
//                        break;
//                }
//                return true;
//            }
//        });

        popup.show();
    }

    private List<MyComment> comments;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentEvent(CommentEvent event) {
        comments = new ArrayList<>();
        for (CommentNode node : event.nodes) {
            MyComment comment = new MyComment(node.getComment(), node.getDepth());
            comments.add(comment);
            makeComments(node, (List<MyComment>) comment.getChildren());
        }

        if (event.first) {
            mAdapter.clear();
            MyContent content = getDummyContent();
            mAdapter.add(content);
        }

        int position = mAdapter.getItemCount();
        mAdapter.addAll(comments);
        mAdapter.notifyDataSetChanged();

        mRecyclerView.getAdapter().notifyDataSetChanged();
        for (; position < mAdapter.getItemCount(); position++) {
            MyComment comment = (MyComment) mAdapter.getItemAt(position);
            if (comment.getComment().getId().equals(mLastCommentId)) {
                mRecyclerView.scrollToPosition(position);
                mLastCommentId = "";
            }
        }
    }


    private void makeComments(CommentNode node, List<MyComment> subComments) {
        for (CommentNode childNode : node.getChildren()) {

            MyComment comment = new MyComment(childNode.getComment(), childNode.getDepth());
            this.comments.add(comment);
            subComments.add(comment);

            makeComments(childNode, (List<MyComment>) comment.getChildren());
        }
    }

    @OnClick(R.id.fab)
    public void onFABClick(View view) {
        if (RedditApi.isAuthorized()) {
            mJobManager.addJobInBackground(new CommentReplySubmissionJob(submissionId, "Chris Farley was a comedy genius. Such a shame that he left us too early."));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentRefreshEvent(CommentRefreshEvent event) {
        int position = mLayoutManager.findLastVisibleItemPosition();
        int compelete = mLayoutManager.findLastCompletelyVisibleItemPosition();
        if (compelete > 0) {
            position = compelete;
        }

        mLastCommentId = ((MyComment) mAdapter.getItemAt(position)).getComment().getId();
        mSwipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateOperation();
            }
        }, 2400);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentErrorEvent(CommentErrorEvent event) {
        Snackbar.make(mFAB, event.errorMessage, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAttemptLoginEvent(AttemptLoginEvent event) {
        Snackbar.make(mFAB, "Please sign in to do that.", Snackbar.LENGTH_LONG)
                .setAction("Sign in", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContextCompat.startActivity(CommentActivity.this, new Intent(CommentActivity.this, LoginActivity.class), null);
                    }
                }).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSigninEvent(SigninEvent event) {
        if (RedditApi.isAuthorized()) {
            onCommentRefreshEvent(null);
        }
    }

    private void updateOperation() {
        mSwipeRefreshLayout.setRefreshing(false);
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.COMMENT_FETCH_ID);
        mJobManager.addJobInBackground(new CommentFetchJob(submissionId, CommentSort.HOT));
    }
}
