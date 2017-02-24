package com.abby.redditgo.ui.comment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abby.redditgo.R;
import com.abby.redditgo.event.CommentEvent;
import com.abby.redditgo.event.CommentRefreshEvent;
import com.abby.redditgo.event.LoginEvent;
import com.abby.redditgo.model.MyComment;
import com.abby.redditgo.model.MyContent;
import com.abby.redditgo.network.RedditApi;

import net.dean.jraw.models.CommentNode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_SUMISSION_ID = "_arg_param_sumission_id";
    private static final String ARG_PARAM_TITLE = "_arg_param_title";

    // TODO: Rename and change types of parameters
    private String mSubmissionId;
    private String mTitle;

    @BindView(R.id.recyclerView_comment)
    RecyclerView mRecyclerView;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private OnFragmentInteractionListener mListener;
    private CommentAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private String mLastCommentId = "";

    public CommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param submissionId Parameter 1.
     * @param title Parameter 2.
     * @return A new instance of fragment CommentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentFragment newInstance(String submissionId, String title) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_SUMISSION_ID, submissionId);
        args.putString(ARG_PARAM_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSubmissionId = getArguments().getString(ARG_PARAM_SUMISSION_ID);
            mTitle = getArguments().getString(ARG_PARAM_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_comment, container, false);
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

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CommentAdapter(getContext());
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
                        if(mListener != null) {
                            mListener.refresh();
                        }
                    }
                }
        );

        if(mListener != null) {
            mListener.refresh();
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

    private MyContent getDummyContent() {
        return new MyContent(mTitle);
    }


    private List<MyComment> comments;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentEvent(CommentEvent event) {
        showRefreshIndicator(false);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentRefreshEvent(CommentRefreshEvent event) {
        int position = mLayoutManager.findLastVisibleItemPosition();
        int compelete = mLayoutManager.findLastCompletelyVisibleItemPosition();
        if (compelete > 0) {
            position = compelete;
        }

        mLastCommentId = ((MyComment) mAdapter.getItemAt(position)).getComment().getId();
        showRefreshIndicator(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mListener != null) {
                    mListener.refresh();
                }
            }
        }, 2400);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (RedditApi.isAuthorized()) {
            onCommentRefreshEvent(null);
        }
    }

    public void showRefreshIndicator(boolean show) {
        mSwipeRefreshLayout.setRefreshing(show);
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
