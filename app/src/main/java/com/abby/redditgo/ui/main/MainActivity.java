package com.abby.redditgo.ui.main;

import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.abby.redditgo.RedditgoAds;
import com.abby.redditgo.data.RedditgoProvider;
import com.abby.redditgo.data.SubmissionColumn;
import com.abby.redditgo.data.SubredditColumn;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.AccountEvent;
import com.abby.redditgo.event.LoginEvent;
import com.abby.redditgo.event.VoteEvent;
import com.abby.redditgo.job.AccountJob;
import com.abby.redditgo.job.JobId;
import com.abby.redditgo.job.SubmissionFetchJob;
import com.abby.redditgo.job.SubredditFetchJob;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.BaseActivity;
import com.abby.redditgo.ui.login.LoginActivity;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.google.android.gms.analytics.Tracker;
import com.orhanobut.logger.Logger;

import net.dean.jraw.paginators.Sorting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements MainFragment.OnFragmentInteractionListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int SUBREDDIT_MENU_INDEX = 2;
    private static final String KEY_TITLE = "_key_title";
    private static final String KEY_SORTING = "_key_sorting";
    private static final java.lang.String KEY_SUBREDDIT = "_key_subreddit";

    private static final int LOADER_ID_SUBREDDIT = 0;

    @Inject
    JobManager mJobManager;

    @Inject
    Tracker mTracker;

    @Inject
    ContentResolver mContentResolver;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.ad_view)
    View mAdView;

    Button mLoginButton;
    TextView mUserNameText;

    TextView mTitleView;
    TextView mSubTitleView;

    private String mLastSubreddit = null;
    private Sorting mLastSorting = Sorting.HOT;
    private int mLastFilterId = R.id.menu_hot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);

        mTitleView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mSubTitleView = (TextView) mToolbar.findViewById(R.id.toolbar_subtitle);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);

        View headerView = mNavigationView.getHeaderView(0);
        mLoginButton = (Button) headerView.findViewById(R.id.button_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextCompat.startActivity(MainActivity.this, new Intent(MainActivity.this, LoginActivity.class), null);
            }
        });
        mUserNameText = (TextView) headerView.findViewById(R.id.text_username);

        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        if (savedInstanceState == null) {
            mTitleView.setText(R.string.side_front_page);
            fetchSubmission(mLastSubreddit, mLastSorting);
        } else {
            String title = savedInstanceState.getString(KEY_TITLE);
            String sorting = savedInstanceState.getString(KEY_SORTING);
            mLastSubreddit = savedInstanceState.getString(KEY_SUBREDDIT);
            mLastSorting = Sorting.valueOf(sorting);
            mTitleView.setText(title);
            MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            if (fragment != null) {
                fragment.fetchSubmission(mLastSubreddit, mLastSorting);
            }
        }

        getLoaderManager().initLoader(LOADER_ID_SUBREDDIT, null, this);

        RedditgoAds.loadAds(mAdView);

    }

    @Override
    protected void injectActivity(ApplicationComponent component) {
        component.inject(this);
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
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the mToolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (mAdView != null) {
            RedditgoAds.pauseAds(mAdView);
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            RedditgoAds.resumeAds(mAdView);
        }
    }

    /**
     * Called before the activity is destroyed
     */

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            RedditgoAds.destoryAds(mAdView);
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.SUBMISSION_FETCH_ID);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String title = (String) mTitleView.getText();
        outState.putString(KEY_TITLE, title);
        outState.putString(KEY_SORTING, mLastSorting.name());
        outState.putString(KEY_SUBREDDIT, mLastSubreddit);
    }


    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_submission, popup.getMenu());
        popup.getMenu().findItem(mLastFilterId).setChecked(true);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                mLastFilterId = item.getItemId();
                switch (mLastFilterId) {
                    case R.id.menu_hot:
                        mLastSorting = Sorting.HOT;
                        break;
                    case R.id.menu_new:
                        mLastSorting = Sorting.NEW;
                        break;
                    case R.id.menu_rising:
                        mLastSorting = Sorting.RISING;
                        break;
                    case R.id.menu_controversial:
                        mLastSorting = Sorting.CONTROVERSIAL;
                        break;
                    case R.id.menu_top:
                        mLastSorting = Sorting.TOP;
                        break;
                }
                item.setChecked(true);
                refresh();
                return true;
            }
        });

        popup.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (RedditApi.isAuthorized()) {
            mLoginButton.setVisibility(View.GONE);
            mUserNameText.setText(event.username);
            mContentResolver.notifyChange(RedditgoProvider.Subreddit.CONTENT_URI, null, false);
            mJobManager.addJobInBackground(new SubredditFetchJob());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountEvent(AccountEvent event) {
        mUserNameText.setText(event.account.getFullName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoteEvent(VoteEvent event) throws RemoteException, OperationApplicationException {
        String id = event.submission.getId();
        String sorting = mLastSorting.name();

        Uri existingUri = null;
        if (mLastSubreddit == null) {
            existingUri = RedditgoProvider.FrontPage.withId(sorting, id);
        } else if (mLastSubreddit.equals("all")) {
            existingUri = RedditgoProvider.All.withId(sorting, id);
        } else {
            existingUri = RedditgoProvider.Submission.withId(mLastSubreddit, sorting, id);
        }

        final ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        batchOperations.add(ContentProviderOperation.newUpdate(existingUri)
                .withValue(SubmissionColumn.POST_HINT, event.submission.getPostHint().name())
                .withValue(SubmissionColumn.URL, event.submission.getUrl())
                .withValue(SubmissionColumn.TITLE, event.submission.getTitle())
                .withValue(SubmissionColumn.VOTE, event.submission.getVote().name())
                .withValue(SubmissionColumn.CREATED_TIME, event.submission.getCreated().getTime())
                .withValue(SubmissionColumn.AUTHOR, event.submission.getAuthor())
                .withValue(SubmissionColumn.SUBREDDIT, event.submission.getSubredditName())
                .withValue(SubmissionColumn.SCORE, event.submission.getScore())
                .withValue(SubmissionColumn.NUM_COMMENTS, event.submission.getCommentCount())
                .withValue(SubmissionColumn.THUMBNAIL, event.submission.getThumbnail())
                .build());

//        mContentResolver.applyBatch(RedditgoContract.AUTHORITY, batchOperations);
        ContentValues values = new ContentValues();
        values.put(SubmissionColumn.POST_HINT, event.submission.getPostHint().name());
        values.put(SubmissionColumn.URL, event.submission.getUrl());
        values.put(SubmissionColumn.TITLE, event.submission.getTitle());
        values.put(SubmissionColumn.VOTE, event.submission.getVote().name());
        values.put(SubmissionColumn.CREATED_TIME, event.submission.getCreated().getTime());
        values.put(SubmissionColumn.AUTHOR, event.submission.getAuthor());
        values.put(SubmissionColumn.SUBREDDIT, event.submission.getSubredditName());
        values.put(SubmissionColumn.SCORE, event.submission.getScore());
        values.put(SubmissionColumn.NUM_COMMENTS, event.submission.getCommentCount());
        values.put(SubmissionColumn.THUMBNAIL, event.submission.getThumbnail());

        int count = mContentResolver.update(existingUri, values, null, null);
        if (count > 0) {
            Uri contentUri = null;
            if (mLastSubreddit == null) {
                contentUri = RedditgoProvider.FrontPage.withSorting(sorting);
            } else if (mLastSubreddit.equals("all")) {
                contentUri = RedditgoProvider.All.withSorting(sorting);
            } else {
                contentUri = RedditgoProvider.Submission.withSubredditAndSorting(mLastSubreddit, sorting);
            }
            mContentResolver.notifyChange(contentUri, null, false);
        }

    }

    public void fetchSubmission(String subreddit, Sorting sorting) {
        mLastSubreddit = subreddit;
        mLastSorting = sorting;
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.SUBMISSION_FETCH_ID);
        mJobManager.addJobInBackground(new SubmissionFetchJob(subreddit, sorting));
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null) {
            fragment.fetchSubmission(mLastSubreddit, mLastSorting);
        }
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_menu_item_front_page:
                                // Do nothing, we're already on that screen
                                mTitleView.setText(R.string.side_front_page);
                                fetchSubmission(null, Sorting.HOT);
                                break;
                            case R.id.navigation_menu_item_all:
                                mTitleView.setText(R.string.side_all);
                                fetchSubmission("all", Sorting.HOT);
                                break;
                            default:
                                break;
                        }
                        mLastSorting = Sorting.HOT;
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

        mNavigationView.getMenu().getItem(SUBREDDIT_MENU_INDEX).getSubMenu().clear();
        if (RedditApi.isAuthorized()) {
            mLoginButton.setVisibility(View.GONE);
            mJobManager.addJobInBackground(new AccountJob());
            mJobManager.addJobInBackground(new SubredditFetchJob());
        }
    }


    @Override
    public void refresh() {
        fetchSubmission(mLastSubreddit, mLastSorting);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_SUBREDDIT) {
            return new CursorLoader(this, RedditgoProvider.Subreddit.CONTENT_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            switch (loader.getId()) {
                case LOADER_ID_SUBREDDIT:
                    Logger.i("count: " + data.getCount());
                    if (RedditApi.isAuthorized() && data.getCount() > 0) {
                        SubMenu subMenu = mNavigationView.getMenu().getItem(SUBREDDIT_MENU_INDEX).getSubMenu();
                        subMenu.clear();
                        while (data.moveToNext()) {
                            String displayName = data.getString(data.getColumnIndex(SubredditColumn.DISPLAY_NAME));
                            subMenu.add(displayName).setIcon(R.drawable.ic_menu).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    String title = String.valueOf(item.getTitle());
                                    mTitleView.setText(title);
                                    fetchSubmission(title, Sorting.HOT);
                                    return false;
                                }
                            });
                        }
                        break;
                    }
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
