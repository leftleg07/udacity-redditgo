package com.abby.redditgo.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
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
import com.abby.redditgo.event.SubmissionEvent;
import com.abby.redditgo.event.VoteEvent;

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
public class MainFragment extends Fragment {

    private static final String KEY_POSITION = "_key_position";

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    private OnFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;

    private int mLastFilterId = R.id.menu_hot;
    private int mPosition = 0;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
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

        // specify an adapter (see also next example)
        SubmissionAdapter adapter = new SubmissionAdapter(getContext());
        mRecyclerView.setAdapter(adapter);

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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    /*
     * Listen for option item selections so that we receive a notification
     * when the user requests a refresh by selecting the refresh action bar item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Check if user triggered a refresh:
            case R.id.menu_refresh:
                // Signal SwipeRefreshLayout to start the progress indicator
                mSwipeRefreshLayout.setRefreshing(true);

                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                updateOperation();

                return true;
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

    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());
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
        mSwipeRefreshLayout.setRefreshing(false);
        if (mListener == null) {
            return;
        }
        mListener.refresh();
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
