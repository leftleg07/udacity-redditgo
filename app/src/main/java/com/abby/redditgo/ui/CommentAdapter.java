package com.abby.redditgo.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.abby.redditgo.model.MyComment;
import com.abby.redditgo.model.MyContent;
import com.oissela.software.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;

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

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView_author)
        TextView authorTextView;

        @BindView(R.id.textView_comment)
        TextView commentTextView;

        @BindView(R.id.checkBox_open)
        CheckBox openCheck;

        @BindView(R.id.textView_score)
        TextView scoreText;

        @BindView(R.id.textView_date)
        TextView dateText;

        @BindDimen(R.dimen.comment_space)
        int space;

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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                CommentViewHolder cvh = (CommentViewHolder) viewHolder;
                MyComment comment = (MyComment) getItemAt(position);

                cvh.authorTextView.setText(comment.submission.getAuthor());
                cvh.commentTextView.setText(comment.submission.getBody());

                cvh.scoreText.setText(String.valueOf(comment.submission.getScore()) + " score");

                Date createdDate = comment.submission.getCreated();
                Date editDate = comment.submission.getEditDate();

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                cvh.dateText.setText(format.format(editDate != null ? editDate : createdDate));

                cvh.setPadding(comment.getIndentation());

                if (comment.getChildren().size() > 0) {
                    cvh.openCheck.setVisibility(View.VISIBLE);
                } else {
                    cvh.openCheck.setVisibility(View.GONE);
                }

                if (comment.isGroup()) {
                    cvh.openCheck.setChecked(false);
                } else {
                    cvh.openCheck.setChecked(true);
                }
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

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_CONTENT;
        return VIEW_TYPE_ITEM;
    }
}