package com.abby.redditgo.ui.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.abby.redditgo.data.SubmissionColumn;
import com.abby.redditgo.job.SubmissionVoteJob;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.BaseActivity;
import com.abby.redditgo.ui.comment.CommentActivity;
import com.abby.redditgo.ui.detail.DetailActivity;
import com.abby.redditgo.ui.detail.ImageDialogFragment;
import com.abby.redditgo.ui.login.LoginActivity;
import com.abby.redditgo.util.CursorRecyclerViewAdapter;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;
import com.bumptech.glide.Glide;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.orhanobut.logger.Logger;
import com.squareup.phrase.Phrase;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;
import static com.abby.redditgo.ui.comment.CommentActivity.EXTRA_SUBMISSION_ID;
import static net.dean.jraw.models.VoteDirection.DOWNVOTE;
import static net.dean.jraw.models.VoteDirection.UPVOTE;

/**
 * Created by gsshop on 2017. 1. 20..
 */

public class AAdapter extends CursorRecyclerViewAdapter<AAdapter.ViewHolder> {

    @Inject
    JobManager mJobManager;

    private final Context mContext;

    public AAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        ((BaseActivity) context).getComponent().inject(this);
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final String id = cursor.getString(cursor.getColumnIndex(SubmissionColumn.ID));
        String temp = cursor.getString(cursor.getColumnIndex(SubmissionColumn.POST_HINT));
        final Submission.PostHint postHint = Submission.PostHint.valueOf(temp);
        final String url = cursor.getString(cursor.getColumnIndex(SubmissionColumn.URL));
        final String title = cursor.getString(cursor.getColumnIndex(SubmissionColumn.TITLE));
        temp = cursor.getString(cursor.getColumnIndex(SubmissionColumn.VOTE));
        final VoteDirection vote = VoteDirection.valueOf(temp);
        long createdTime = cursor.getLong(cursor.getColumnIndex(SubmissionColumn.CREATED_TIME));
        String author = cursor.getString(cursor.getColumnIndex(SubmissionColumn.AUTHOR));
        String subreddit = cursor.getString(cursor.getColumnIndex(SubmissionColumn.SUBREDDIT));
        long score = cursor.getLong(cursor.getColumnIndex(SubmissionColumn.SCORE));
        long numComments = cursor.getLong(cursor.getColumnIndex(SubmissionColumn.NUM_COMMENTS));
        String thumbnail = cursor.getString(cursor.getColumnIndex(SubmissionColumn.THUMBNAIL));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("hint: " + postHint + ", link: " + url);
                Intent intent;
                switch (postHint) {
                    case IMAGE:
                        ImageDialogFragment dialog = ImageDialogFragment.newInstance(title, url);
                        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), ImageDialogFragment.class.getSimpleName());
                        break;
                    case VIDEO:
                        YouTubeInitializationResult result = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(mContext);
                        if (result == YouTubeInitializationResult.SUCCESS && url.contains("youtu")) {
                            intent = YouTubeIntents.createPlayVideoIntentWithOptions(mContext, "", true, false);
                            intent.setData(Uri.parse(url));
                            ContextCompat.startActivity(mContext, intent, null);
                            break;
                        }
                    case LINK:
                    default:
                        intent = new Intent(mContext, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_TITLE, title);
                        intent.setData(Uri.parse(url));
                        ContextCompat.startActivity(mContext, intent, null);
                }
            }
        });

        holder.mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RedditApi.isAuthorized()) {
                    if (vote == UPVOTE) {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(id, VoteDirection.NO_VOTE));
                    } else {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(id, UPVOTE));
                    }
                } else {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.title_activity_login)
                            .positiveText(R.string.action_sign_in)
                            .negativeText(android.R.string.no)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ContextCompat.startActivity(mContext, new Intent(mContext, LoginActivity.class), null);
                                }
                            }).show();

                }
            }
        });

        holder.mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RedditApi.isAuthorized()) {
                    if (vote == DOWNVOTE) {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(id, VoteDirection.NO_VOTE));
                    } else {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(id, DOWNVOTE));
                    }
                } else {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.title_activity_login)
                            .positiveText(R.string.action_sign_in)
                            .negativeText(android.R.string.no)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ContextCompat.startActivity(mContext, new Intent(mContext, LoginActivity.class), null);
                                }
                            }).show();
                }
            }
        });

        holder.mTitleText.setText(title);

        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - createdTime);
        holder.mSubmitText.setText(Phrase.from(mContext, R.string.submit).put("hour", String.valueOf(hours)).put("author", author).put("subreddit", subreddit).format());

        holder.mScoreText.setText(String.valueOf(score));
        holder.mCommentsText.setText(String.valueOf(numComments));
        holder.mCommnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra(EXTRA_SUBMISSION_ID, id);
                ContextCompat.startActivity(mContext, intent, null);
            }
        });

        holder.mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(EXTRA_SUBJECT, title);
                sharingIntent.putExtra(EXTRA_TEXT, url);
                ContextCompat.startActivity(mContext, Intent.createChooser(sharingIntent, "Share URL"), null);
            }
        });


        if (thumbnail == null) {
            holder.mIconImage.setVisibility(View.GONE);
            switch (postHint) {
                case SELF:
                    holder.mThumbnailImage.setImageResource(R.drawable.img_self);
                    break;
                case VIDEO:
                    holder.mThumbnailImage.setImageResource(R.drawable.img_video);
                    break;
                case LINK:
                    holder.mThumbnailImage.setImageResource(R.drawable.img_link);
                    break;
                case UNKNOWN:
                    holder.mThumbnailImage.setImageResource(R.drawable.img_unknown);
                    break;
                case IMAGE:
                    holder.mThumbnailImage.setImageResource(R.drawable.img_no);
                    break;
            }
        } else {
            holder.mIconImage.setVisibility(View.VISIBLE);
            switch (postHint) {
                case SELF:
                    holder.mIconImage.setImageResource(R.drawable.ic_account_circle_white_48dp);
                    break;
                case VIDEO:
                    holder.mIconImage.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                    break;
                case LINK:
                    holder.mIconImage.setImageResource(R.drawable.ic_mouse_white_48dp);
                    break;
                case UNKNOWN:
                    holder.mIconImage.setImageResource(R.drawable.ic_sentiment_satisfied_white_48dp);
                    break;
                case IMAGE:
                    holder.mIconImage.setImageResource(R.drawable.ic_image_white_48dp);
                    break;
            }
            Glide.with(mContext).load(thumbnail).into(holder.mThumbnailImage);
        }

        switch (vote) {
            case UPVOTE:
                holder.mUpButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_up_highlight));
                holder.mDownButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_down));
                break;
            case DOWNVOTE:
                holder.mUpButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_up));
                holder.mDownButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_down_highlight));
                break;
            default:
                holder.mUpButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_up));
                holder.mDownButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_down));

        }

    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        @BindView(R.id.textView_title)
        TextView mTitleText;

        @BindView(R.id.textView_submit)
        TextView mSubmitText;

        @BindView(R.id.imageView_thumbnail)
        ImageView mThumbnailImage;

        @BindView(R.id.imageView_icon)
        ImageView mIconImage;

        @BindView(R.id.textView_comment_score)
        TextView mScoreText;

        @BindView(R.id.textView_comments)
        TextView mCommentsText;

        @BindView(R.id.button_share)
        ImageButton mShareButton;

        @BindView(R.id.button_up)
        ImageButton mUpButton;

        @BindView(R.id.button_down)
        ImageButton mDownButton;

        @BindView(R.id.button_comment)
        ImageButton mCommnetButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
