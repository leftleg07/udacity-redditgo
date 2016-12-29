package com.abby.redditgo.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
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

import com.abby.redditgo.ui.BaseActivity;
import com.abby.redditgo.R;
import com.abby.redditgo.job.SubmissionVoteJob;
import com.abby.redditgo.network.RedditApi;
import com.abby.redditgo.ui.comment.CommentActivity;
import com.abby.redditgo.ui.detail.DetailActivity;
import com.abby.redditgo.ui.detail.ImageDialogFragment;
import com.abby.redditgo.ui.login.LoginActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;
import static com.abby.redditgo.ui.comment.CommentActivity.EXTRA_SUBMISSION_ID;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {
    private List<Submission> mSubmissions;

    @Inject
    JobManager mJobManager;

    private final Context mContext;

    public SubmissionAdapter(Context context) {
        ((BaseActivity) context).getComponent().inject(this);
        this.mContext = context;
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

    // Provide a suitable constructor (depends on the kind of dataset)
    public void setSubmissions(List<Submission> submissions) {
        mSubmissions = new ArrayList<>();
        mSubmissions.addAll(submissions);
    }

    public void addSubmissions(List<Submission> submissions) {
        mSubmissions.addAll(submissions);
    }

    public List<Submission> getSubmissions() {
        return mSubmissions;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SubmissionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        // title - 타이틀
        // thumbnail - 섬네일
        // permalink - 코멘트 주소
        // url - 링크
        // score - 점수
        // num_comments - 코멘트 갯수
        // author - 작성자.
        // subreddit - funny
        // link_flair_text -
        // selftext - 상세 내용.

        final Submission submission = mSubmissions.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("hint: " + submission.getPostHint() + ", link: " + submission.getUrl());
                Intent intent;
                switch (submission.getPostHint()) {
                    case IMAGE:
                        ImageDialogFragment dialog = ImageDialogFragment.newInstance(submission);
                        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), ImageDialogFragment.class.getSimpleName());
                        break;
                    case VIDEO:
                        String url = submission.getUrl();
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
                        intent.putExtra(DetailActivity.EXTRA_TITLE, submission.getTitle());
                        intent.setData(Uri.parse(submission.getUrl()));
                        ContextCompat.startActivity(mContext, intent, null);
                }
            }
        });

        holder.mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RedditApi.isAuthorized()) {
                    if (submission.getVote() == VoteDirection.UPVOTE) {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(submission, VoteDirection.NO_VOTE));
                    } else {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(submission, VoteDirection.UPVOTE));
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
                    if (submission.getVote() == VoteDirection.DOWNVOTE) {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(submission, VoteDirection.NO_VOTE));
                    } else {
                        mJobManager.addJobInBackground(new SubmissionVoteJob(submission, VoteDirection.DOWNVOTE));
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

        holder.mTitleText.setText(submission.getTitle());

        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - submission.getCreated().getTime());
        holder.mSubmitText.setText(Phrase.from(mContext, R.string.submit).put("hour", String.valueOf(hours)).put("author", submission.getAuthor()).put("subreddit", submission.getSubredditName()).format());

        holder.mScoreText.setText(String.valueOf(submission.getScore()));
        holder.mCommentsText.setText(String.valueOf(submission.getCommentCount()));
        holder.mCommnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra(EXTRA_SUBMISSION_ID, submission.getId());
                ContextCompat.startActivity(mContext, intent, null);
            }
        });

        holder.mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(EXTRA_SUBJECT, submission.getTitle());
                sharingIntent.putExtra(EXTRA_TEXT, submission.getUrl());
                ContextCompat.startActivity(mContext, Intent.createChooser(sharingIntent, "Share URL"), null);
            }
        });


        String thumbnail = submission.getThumbnail();
        if (thumbnail == null) {
            holder.mIconImage.setVisibility(View.GONE);
            switch (submission.getPostHint()) {
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
            switch (submission.getPostHint()) {
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

        switch (submission.getVote()) {
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mSubmissions != null) {
            return mSubmissions.size();
        }

        return 0;
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = mContext.getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }
}

