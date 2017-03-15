package com.abby.redditgo.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abby.redditgo.R;
import com.abby.redditgo.data.RedditgoProvider;
import com.abby.redditgo.event.SubmissionErrorEvent;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.paginators.Sorting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_POSITION = "_key_position";
    private static final String KEY_SORTING = "_key_sorting";
    private static final String KEY_SUBREDDIT = "_key_subreddit";

    private static final int LOADER_ID_FRONT_PAGE = 0;
    private static final int LOADER_ID_ALL = 1;
    private static final int LOADER_ID_SUBMISSION = 2;


    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.recycler_view_empty)
    View mEmptyView;

    @BindInt(R.integer.column_count)
    int mColumnCount;

    private OnFragmentInteractionListener mListener;
    private GridLayoutManager mLayoutManager;

    private int mPosition = -1;
    private int mTotalCount = -1;
    private SubmissionAdapter mAdapter = null;
    private boolean mIsFirst = false;

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

        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(getContext(), mColumnCount);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an mAdapter (see also next example)
        mAdapter = new SubmissionAdapter(getContext(), null, mEmptyView);
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
                        if (mListener == null) {
                            return;
                        }
                        mListener.refresh();
                    }
                }
        );

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION);
        }
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
    public void onSubmissionErrorEvent(SubmissionErrorEvent event) {
        new MaterialDialog.Builder(getContext())
                .content(event.errorMessage)
                .positiveText(android.R.string.ok)
                .show();
    }

    public void fetchSubmission(String subreddit, Sorting sorting) {
        mAdapter.setSubreddit(subreddit);
        mIsFirst = true;
        mTotalCount = 0;
        mSwipeRefreshLayout.setRefreshing(true);

        Bundle args = new Bundle();
        args.putString(KEY_SUBREDDIT, subreddit);
        args.putString(KEY_SORTING, sorting.name());
        if (subreddit == null) {
            getLoaderManager().restartLoader(LOADER_ID_FRONT_PAGE, args, this);
        } else if (subreddit.equals("all")) {
            getLoaderManager().restartLoader(LOADER_ID_ALL, args, this);
        } else {
            getLoaderManager().restartLoader(LOADER_ID_SUBMISSION, args, this);
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String subreddit = null;
        String sorting = null;
        if (args != null) {
            sorting = args.getString(KEY_SORTING);
            subreddit = args.getString(KEY_SUBREDDIT);
        }

        Uri uri = null;
        switch (id) {
            case LOADER_ID_FRONT_PAGE:
                uri = RedditgoProvider.FrontPage.withSorting(sorting);
                break;
            case LOADER_ID_ALL:
                uri = RedditgoProvider.All.withSorting(sorting);
                break;
            case LOADER_ID_SUBMISSION:
                uri = RedditgoProvider.Submission.withSubredditAndSorting(subreddit, sorting);
                break;
        }

        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        int count = data.getCount();
        mTotalCount += count;
        if (count > 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (mPosition == 0 && mIsFirst) {
                mRecyclerView.scrollToPosition(mPosition);
            } else if (mPosition > 0 && mPosition < mTotalCount) {
                mRecyclerView.scrollToPosition(mPosition);
                mPosition = 0;
            }
            mIsFirst = false;
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
        void refresh();
    }

}
