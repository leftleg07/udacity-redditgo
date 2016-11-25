package com.abby.redditgo.ui;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.abby.redditgo.MainApplication;
import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.SubredditEvent;
import com.abby.redditgo.job.FetchSubmission;
import com.abby.redditgo.job.FetchSubreddit;
import com.abby.redditgo.job.JobId;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {
    private static final int SUBREDDIT_MENU_INDEX = 2;
    private static final String KEY_SUBREDDIT = "_key_subreddit";
    private static final String KEY_SORTING = "_key_sorting";
    private static final String KEY_TITLE = "_key_title";

    @Inject
    JobManager mJobManager;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    TextView mTitleView;
    TextView mSubTitleView;

    private String lastSubreddit = null;
    private Sorting lastSorting = Sorting.HOT;

    public ApplicationComponent getComponent() {
        return ((MainApplication) getApplication()).getComponent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainApplication) getApplication()).getComponent().inject(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTitleView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mSubTitleView = (TextView) mToolbar.findViewById(R.id.toolbar_subtitle);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        if(savedInstanceState == null) {
            mJobManager.addJobInBackground(new FetchSubreddit());
            fetchSubmission(null, Sorting.HOT);
            mTitleView.setText(R.string.side_front_page);
        } else {
            mJobManager.addJobInBackground(new FetchSubreddit());
            String subreddit = savedInstanceState.getString(KEY_SUBREDDIT);
            Sorting sorting = (Sorting) savedInstanceState.getSerializable(KEY_SORTING);
            String title = savedInstanceState.getString(KEY_TITLE);
            mTitleView.setText(title);
            fetchSubmission(subreddit, sorting);
        }
    }

    @OnClick(R.id.fab)
    public void onFABClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the mToolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.FETCH_SUBMISSION_ID);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SUBREDDIT, lastSubreddit);
        outState.putSerializable(KEY_SORTING, lastSorting);
        String title = (String) mTitleView.getText();
        outState.putString(KEY_TITLE, title);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubredditEvent(SubredditEvent event) {
        List<Subreddit> subreddits = event.subreddits;
        SubMenu subMenu = mNavigationView.getMenu().getItem(SUBREDDIT_MENU_INDEX).getSubMenu();

        for (final Subreddit subreddit : subreddits) {
            subMenu.add(subreddit.getDisplayName()).setIcon(R.drawable.ic_menu).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String title = String.valueOf(item.getTitle());
                    mTitleView.setText(title);
                    fetchSubmission(title, Sorting.HOT);
                    return false;
                }
            });
        }
    }

    public void fetchSubmission(String subreddit, Sorting sorting) {
        lastSubreddit = subreddit;
        lastSorting = sorting;
        mJobManager.cancelJobsInBackground(null, TagConstraint.ALL, JobId.FETCH_SUBMISSION_ID);
        mJobManager.addJobInBackground(new FetchSubmission(subreddit, sorting));
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
                        onSubTitleChange(R.string.filter_hot);
                        // Close the navigation drawer when an item is selected.
//                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

        mNavigationView.getMenu().getItem(SUBREDDIT_MENU_INDEX).getSubMenu().clear();
    }

    @Override
    public void onSubTitleChange(@StringRes int resid) {
        mSubTitleView.setText(resid);
        Sorting sorting = Sorting.HOT;
        switch (resid) {
            case R.string.filter_hot:
                sorting = Sorting.HOT;
                break;
            case R.string.filter_new:
                sorting = Sorting.NEW;
                break;
            case R.string.filter_rising:
                sorting = Sorting.RISING;
                break;
            case R.string.filter_controversial:
                sorting = Sorting.CONTROVERSIAL;
                break;
            case R.string.filter_top:
                sorting = Sorting.TOP;
                break;
        }

        fetchSubmission(lastSubreddit, sorting);
    }

    @Override
    public void refresh() {
        fetchSubmission(lastSubreddit, lastSorting);
    }
}
