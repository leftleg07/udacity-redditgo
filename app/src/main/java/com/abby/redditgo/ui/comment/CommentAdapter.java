package com.abby.redditgo.ui.comment;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.abby.redditgo.event.AttemptLoginEvent;
import com.abby.redditgo.job.CommentDeleteJob;
import com.abby.redditgo.job.CommentVoteJob;
import com.abby.redditgo.model.MyComment;
import com.abby.redditgo.model.MyContent;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.BaseActivity;
import com.birbit.android.jobqueue.JobManager;
import com.oissela.software.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.VoteDirection;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.abby.redditgo.ui.comment.ComposeCommentActivity.EXTRA_COMMENT_ID;
import static com.abby.redditgo.ui.comment.ComposeCommentActivity.EXTRA_TITLE;
import static net.dean.jraw.models.VoteDirection.DOWNVOTE;
import static net.dean.jraw.models.VoteDirection.UPVOTE;

/**
 * An example of how to extend MultiLevelExpIndListAdapter.
 * Some of this code is copied from https://developer.android.com/training/material/lists-cards.html
 */
public class CommentAdapter extends MultiLevelExpIndListAdapter {
    /**
     * View type of an item or group.
     */
    public static final int VIEW_TYPE_ITEM = 0;

    /**
     * View type of the content.
     */
    private static final int VIEW_TYPE_CONTENT = 1;


    private final Context mContext;

    @Inject
    JobManager mJobManager;

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView_comment_author)
        TextView authorTextView;

        @BindView(R.id.textView_comment)
        TextView commentTextView;

        @BindView(R.id.checkBox_comment_open)
        CheckBox openCheck;

        @BindView(R.id.textView_comment_score)
        TextView scoreText;

        @BindView(R.id.textView_comment_date)
        TextView dateText;

        @BindDimen(R.dimen.comment_space)
        int space;

        @BindView(R.id.button_comment_up)
        ImageButton upButton;

        @BindView(R.id.button_comment_down)
        ImageButton downButton;

        @BindView(R.id.button_comment_reply)
        ImageButton replyButton;

        @BindView(R.id.button_comment_remove)
        ImageButton removeButton;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setPadding(int multiple) {
            itemView.setPadding(space * multiple, space, space, space);
        }
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content_textview)
        TextView contentTextView;

        public ContentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public CommentAdapter(Context context) {
        super();
        mContext = context;
        ((BaseActivity) context).getComponent().inject(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                int resource = R.layout.recyclerview_item;
                v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                viewHolder = new CommentViewHolder(v);
                break;
            case VIEW_TYPE_CONTENT:
                resource = R.layout.recyclerview_content;
                v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                viewHolder = new ContentViewHolder(v);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                final CommentViewHolder holder = (CommentViewHolder) viewHolder;
                final MyComment myComment = (MyComment) getItemAt(position);
                final Comment comment = myComment.getComment();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleGroup(position);
                    }
                });

                holder.authorTextView.setText(comment.getAuthor());
                holder.commentTextView.setText(comment.getBody());

                holder.scoreText.setText(String.valueOf(comment.getScore()) + " score");

                Date createdDate = comment.getCreated();
                Date editDate = comment.getEditDate();

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                holder.dateText.setText(format.format(editDate != null ? editDate : createdDate));

                holder.setPadding(myComment.getIndentation());

                if (myComment.getChildren().size() > 0) {
                    holder.openCheck.setVisibility(View.VISIBLE);
                } else {
                    holder.openCheck.setVisibility(View.GONE);
                }

                if (myComment.isGroup()) {
                    holder.openCheck.setChecked(false);
                } else {
                    holder.openCheck.setChecked(true);
                }

                updateVote(comment.getVote(), holder);
                holder.upButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (RedditApi.isAuthorized()) {
                            VoteDirection vote;
                            if (comment.getVote() == UPVOTE) {
                                vote = VoteDirection.NO_VOTE;
                            } else {
                                vote = UPVOTE;
                            }
                            mJobManager.addJobInBackground(new CommentVoteJob(comment, vote));
                        } else {
                            EventBus.getDefault().post(new AttemptLoginEvent());
                        }
                    }
                });

                holder.downButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (RedditApi.isAuthorized()) {
                            VoteDirection vote;
                            if (comment.getVote() == DOWNVOTE) {
                                vote = VoteDirection.NO_VOTE;
                            } else {
                                vote = DOWNVOTE;
                            }
                            mJobManager.addJobInBackground(new CommentVoteJob(comment, vote));
                        } else {
                            EventBus.getDefault().post(new AttemptLoginEvent());
                        }
                    }
                });

                holder.replyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (RedditApi.isAuthorized()) {
                            Intent intent = new Intent(mContext, ComposeCommentActivity.class);
                            intent.putExtra(EXTRA_COMMENT_ID, comment.getFullName());
                            intent.putExtra(EXTRA_TITLE, comment.getBody());
                            ContextCompat.startActivity(mContext, intent, null);
                        } else {
                            EventBus.getDefault().post(new AttemptLoginEvent());
                        }
                    }
                });

                holder.removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (RedditApi.isAuthorized()) {
                            mJobManager.addJobInBackground(new CommentDeleteJob(comment.getFullName()));
                        } else {
                            EventBus.getDefault().post(new AttemptLoginEvent());
                        }
                    }
                });
                break;
            case VIEW_TYPE_CONTENT:
                ContentViewHolder contentVH = (ContentViewHolder) viewHolder;
                MyContent content = (MyContent) getItemAt(position);
                contentVH.contentTextView.setText(content.content);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
    }

    private void updateVote(VoteDirection vote, CommentViewHolder holder) {
        switch (vote) {
            case UPVOTE:
                holder.upButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_up_highlight));
                holder.downButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_down));
                break;
            case DOWNVOTE:
                holder.upButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_up));
                holder.downButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_down_highlight));
                break;
            default:
                holder.upButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_up));
                holder.downButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_down));

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_CONTENT;
        return VIEW_TYPE_ITEM;
    }
}
