package com.abby.redditgo.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.abby.redditgo.R;
import com.abby.redditgo.data.RedditgoProvider;
import com.abby.redditgo.event.RefreshShowEvent;
import com.abby.redditgo.event.SubmissionErrorEvent;
import com.abby.redditgo.event.SubmissionEvent;
import com.abby.redditgo.event.VoteEvent;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_POSITION = "_key_position";
    private static final String KEY_SUBREDDIT = "_key_subreddit";

    private static final int LOADER_ID_FRONT_PAGE_HOT = 0;
    private static final int LOADER_ID_FRONT_PAGE_NEW = 1;
    private static final int LOADER_ID_FRONT_PAGE_RISING = 2;
    private static final int LOADER_ID_FRONT_PAGE_CONTROVERSAL = 3;
    private static final int LOADER_ID_FRONT_PAGE_TOP = 4;
    private static final int LOADER_ID_ALL_HOT = 5;
    private static final int LOADER_ID_ALL_NEW = 6;
    private static final int LOADER_ID_ALL_RISING = 7;
    private static final int LOADER_ID_ALL_CONTROVERSAL = 8;
    private static final int LOADER_ID_ALL_TOP = 9;
    private static final int LOADER_ID_SUBMISSION_HOT = 10;
    private static final int LOADER_ID_SUBMISSION_NEW = 11;
    private static final int LOADER_ID_SUBMISSION_RISING = 12;
    private static final int LOADER_ID_SUBMISSION_CONTROVERSAL = 13;
    private static final int LOADER_ID_SUBMISSION_TOP = 14;


    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    private OnFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;

    private int mLastFilterId = R.id.menu_hot;
    private int mPosition = 0;
    private AAdapter mAdapter = null;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION);
        }
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an mAdapter (see also next example)
        mAdapter = new AAdapter(getContext(), null);
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

        getLoaderManager().initLoader(LOADER_ID_FRONT_PAGE_HOT, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filter_menu, menu);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int position = mLayoutManager.findFirstVisibleItemPosition();
        int compelete = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        if (compelete > 0) {
            position = compelete;
        }
        if (position < 0) {
            position = mPosition;
        }
        outState.putInt(KEY_POSITION, position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubmissionEvent(SubmissionEvent event) {
        if (true) return;

        if (event.mSorting == Sorting.HOT) {
            mLastFilterId = R.id.menu_hot;
        } else if (event.mSorting == Sorting.NEW) {
            mLastFilterId = R.id.menu_new;
        } else if (event.mSorting == Sorting.RISING) {
            mLastFilterId = R.id.menu_rising;
        } else if (event.mSorting == Sorting.CONTROVERSIAL) {
            mLastFilterId = R.id.menu_controversial;
        } else if (event.mSorting == Sorting.TOP) {
            mLastFilterId = R.id.menu_top;
        }

        SubmissionAdapter adapter = (SubmissionAdapter) mRecyclerView.getAdapter();
        if (event.mPage == 1) {
            mRecyclerView.scrollToPosition(0);
            adapter.setSubmissions(event.submissions);
        } else {
            adapter.addSubmissions(event.submissions);
        }

        mRecyclerView.getAdapter().notifyDataSetChanged();

        if (mPosition > 0 && mPosition < adapter.getItemCount()) {
            mRecyclerView.scrollToPosition(mPosition);
            mPosition = 0;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubmissionErrorEvent(SubmissionErrorEvent event) {
        new MaterialDialog.Builder(getContext())
                .content(event.errorMessage)
                .positiveText(android.R.string.ok)
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoteEvent(VoteEvent event) {
        SubmissionAdapter adapter = (SubmissionAdapter) mRecyclerView.getAdapter();
        List<Submission> submissions = adapter.getSubmissions();
        for (int i = 0; i < submissions.size(); i++) {
            if (submissions.get(i).getId().equals(event.submission.getId())) {
                submissions.set(i, event.submission);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshShowEvent(RefreshShowEvent event) {
        switch (event.sorting) {
            case HOT:
                mLastFilterId = R.id.menu_hot;
                if (event.subreddit == null) {
                    getLoaderManager().restartLoader(LOADER_ID_FRONT_PAGE_HOT, null, this);
                } else if (event.subreddit.equals("all")) {
                    getLoaderManager().restartLoader(LOADER_ID_ALL_HOT, null, this);
                } else {
                    Bundle args = new Bundle();
                    args.putString(KEY_SUBREDDIT, event.subreddit);
                    getLoaderManager().restartLoader(LOADER_ID_SUBMISSION_HOT, args, this);
                }
                break;
            case NEW:
                mLastFilterId = R.id.menu_new;
                if (event.subreddit == null) {
                    getLoaderManager().restartLoader(LOADER_ID_FRONT_PAGE_NEW, null, this);
                } else if (event.subreddit.equals("all")) {
                    getLoaderManager().restartLoader(LOADER_ID_ALL_NEW, null, this);
                } else {
                    Bundle args = new Bundle();
                    args.putString(KEY_SUBREDDIT, event.subreddit);
                    getLoaderManager().restartLoader(LOADER_ID_SUBMISSION_NEW, args, this);
                }
                break;
            case RISING:
                mLastFilterId = R.id.menu_rising;
                if (event.subreddit == null) {
                    getLoaderManager().restartLoader(LOADER_ID_FRONT_PAGE_RISING, null, this);
                } else if (event.subreddit.equals("all")) {
                    getLoaderManager().restartLoader(LOADER_ID_ALL_RISING, null, this);
                } else {
                    Bundle args = new Bundle();
                    args.putString(KEY_SUBREDDIT, event.subreddit);
                    getLoaderManager().restartLoader(LOADER_ID_SUBMISSION_RISING, args, this);
                }
                break;
            case CONTROVERSIAL:
                mLastFilterId = R.id.menu_controversial;
                if (event.subreddit == null) {
                    getLoaderManager().restartLoader(LOADER_ID_FRONT_PAGE_CONTROVERSAL, null, this);
                } else if (event.subreddit.equals("all")) {
                    getLoaderManager().restartLoader(LOADER_ID_ALL_CONTROVERSAL, null, this);
                } else {
                    Bundle args = new Bundle();
                    args.putString(KEY_SUBREDDIT, event.subreddit);
                    getLoaderManager().restartLoader(LOADER_ID_SUBMISSION_CONTROVERSAL, args, this);
                }
                break;
            case TOP:
                mLastFilterId = R.id.menu_top;
                if (event.subreddit == null) {
                    getLoaderManager().restartLoader(LOADER_ID_FRONT_PAGE_TOP, null, this);
                } else if (event.subreddit.equals("all")) {
                    getLoaderManager().restartLoader(LOADER_ID_ALL_TOP, null, this);
                } else {
                    Bundle args = new Bundle();
                    args.putString(KEY_SUBREDDIT, event.subreddit);
                    getLoaderManager().restartLoader(LOADER_ID_SUBMISSION_TOP, args, this);
                }
                break;
        }


        mSwipeRefreshLayout.setRefreshing(true);

    }

    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_submission, popup.getMenu());
        popup.getMenu().findItem(mLastFilterId).setChecked(true);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (mListener == null) {
                    return false;
                }
                item.setChecked(true);
                switch (item.getItemId()) {
                    case R.id.menu_hot:
                        mListener.onSubTitleChange(R.string.filter_hot);
                        break;
                    case R.id.menu_new:
                        mListener.onSubTitleChange(R.string.filter_new);
                        break;
                    case R.id.menu_rising:
                        mListener.onSubTitleChange(R.string.filter_rising);
                        break;
                    case R.id.menu_controversial:
                        mListener.onSubTitleChange(R.string.filter_controversial);
                        break;
                    case R.id.menu_top:
                        mListener.onSubTitleChange(R.string.filter_top);
                        break;
                }
                return true;
            }
        });

        popup.show();
    }

    private void updateOperation() {
        if (mListener == null) {
            return;
        }
        mListener.refresh();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String subreddit = null;
        if (args != null) {
            subreddit = args.getString(KEY_SUBREDDIT);
        }
        switch (id) {
            case LOADER_ID_FRONT_PAGE_HOT:
                return new CursorLoader(getActivity(), RedditgoProvider.FrontPageHot.CONTENT_URI, null, null, null, null);
            case LOADER_ID_FRONT_PAGE_NEW:
                return new CursorLoader(getActivity(), RedditgoProvider.FrontPageNew.CONTENT_URI, null, null, null, null);
            case LOADER_ID_FRONT_PAGE_RISING:
                return new CursorLoader(getActivity(), RedditgoProvider.FrontPageRising.CONTENT_URI, null, null, null, null);
            case LOADER_ID_FRONT_PAGE_CONTROVERSAL:
                return new CursorLoader(getActivity(), RedditgoProvider.FrontPageControversal.CONTENT_URI, null, null, null, null);
            case LOADER_ID_FRONT_PAGE_TOP:
                return new CursorLoader(getActivity(), RedditgoProvider.FrontPageTop.CONTENT_URI, null, null, null, null);
            case LOADER_ID_ALL_HOT:
                return new CursorLoader(getActivity(), RedditgoProvider.AllHot.CONTENT_URI, null, null, null, null);
            case LOADER_ID_ALL_NEW:
                return new CursorLoader(getActivity(), RedditgoProvider.AllNew.CONTENT_URI, null, null, null, null);
            case LOADER_ID_ALL_RISING:
                return new CursorLoader(getActivity(), RedditgoProvider.AllRising.CONTENT_URI, null, null, null, null);
            case LOADER_ID_ALL_CONTROVERSAL:
                return new CursorLoader(getActivity(), RedditgoProvider.AllControversal.CONTENT_URI, null, null, null, null);
            case LOADER_ID_ALL_TOP:
                return new CursorLoader(getActivity(), RedditgoProvider.AllTop.CONTENT_URI, null, null, null, null);
            case LOADER_ID_SUBMISSION_HOT:
                return new CursorLoader(getActivity(), RedditgoProvider.SubmissionHot.withSubreddit(subreddit), null, null, null, null);
            case LOADER_ID_SUBMISSION_NEW:
                return new CursorLoader(getActivity(), RedditgoProvider.SubmissionNew.withSubreddit(subreddit), null, null, null, null);
            case LOADER_ID_SUBMISSION_RISING:
                return new CursorLoader(getActivity(), RedditgoProvider.SubmissionRising.withSubreddit(subreddit), null, null, null, null);
            case LOADER_ID_SUBMISSION_CONTROVERSAL:
                return new CursorLoader(getActivity(), RedditgoProvider.SubmissionControversal.withSubreddit(subreddit), null, null, null, null);
            case LOADER_ID_SUBMISSION_TOP:
                return new CursorLoader(getActivity(), RedditgoProvider.SubmissionTop.withSubreddit(subreddit), null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        int count = data.getCount();
        if (count > 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSubTitleChange(@StringRes int resid);

        void refresh();
    }

}
