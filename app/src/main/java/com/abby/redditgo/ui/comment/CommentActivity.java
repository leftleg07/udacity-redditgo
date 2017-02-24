package com.abby.redditgo.ui.comment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.abby.redditgo.R;
import com.abby.redditgo.data.SubmissionColumn;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.AttemptLoginEvent;
import com.abby.redditgo.event.CommentErrorEvent;
import com.abby.redditgo.job.CommentFetchJob;
import com.abby.redditgo.job.JobId;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.BaseActivity;
import com.abby.redditgo.ui.login.LoginActivity;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import net.dean.jraw.models.CommentSort;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.abby.redditgo.ui.comment.ComposeCommentActivity.EXTRA_SUBMISSION_ID;
import static com.abby.redditgo.ui.comment.ComposeCommentActivity.EXTRA_TITLE;

public class CommentActivity extends BaseActivity implements CommentFragment.OnFragmentInteractionListener {

    private static final int REQUEST_COMPOSE_COMMENT = 0x01;
    @Inject
    JobManager mJobManager;

    @Inject
    ContentResolver mContentResolver;

    @BindView(R.id.fab)
    FloatingActionButton mFAB;

    private String mSubmissionId;
    private String mTitle;
    private int mLastFilterId = R.id.menu_hot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        Uri uri = getIntent().getData();
        Cursor cursor = mContentResolver.query(uri, null, null, null, null);
        cursor.moveToNext();
        mTitle = cursor.getString(cursor.getColumnIndex(SubmissionColumn.TITLE));
        mSubmissionId = cursor.getString(cursor.getColumnIndex(SubmissionColumn.ID));

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

        if (savedInstanceState == null) {
            CommentFragment fragment = CommentFragment.newInstance(mSubmissionId, mTitle);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, fragment).commit();
        }

    }

    @Override
    protected void injectActivity(ApplicationComponent component) {
        component.inject(this);
    }


    @Override
    protected void onDestroy() {
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.COMMENT_FETCH_ID);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            refresh();
        }
    }

    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_comment, popup.getMenu());
        popup.getMenu().findItem(mLastFilterId).setChecked(true);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                mLastFilterId = item.getItemId();
                item.setChecked(true);
                refresh();
                return true;
            }
        });

        popup.show();
    }


    @OnClick(R.id.fab)
    public void onFABClick(View view) {
        if (RedditApi.isAuthorized()) {
            Intent intent = new Intent(this, ComposeCommentActivity.class);
            intent.putExtra(EXTRA_SUBMISSION_ID, mSubmissionId);
            intent.putExtra(EXTRA_TITLE, mTitle);
            startActivityForResult(intent, REQUEST_COMPOSE_COMMENT);
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


    @Override
    public void refresh() {
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.COMMENT_FETCH_ID);
        CommentSort sort = null;
        switch (mLastFilterId) {
            case R.id.menu_top:
                sort = CommentSort.TOP;
                break;
            case R.id.menu_hot:
                sort = CommentSort.HOT;
                break;
            case R.id.menu_new:
                sort = CommentSort.NEW;
                break;
            case R.id.menu_controversial:
                sort = CommentSort.CONTROVERSIAL;
                break;
            case R.id.menu_old:
                sort = CommentSort.OLD;
                break;
            case R.id.menu_qa:
                sort = CommentSort.QA;
                break;
        }
        CommentFragment fragment = (CommentFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment != null) {
            fragment.showRefreshIndicator(true);
        }
        mJobManager.addJobInBackground(new CommentFetchJob(mSubmissionId, sort));
    }
}
