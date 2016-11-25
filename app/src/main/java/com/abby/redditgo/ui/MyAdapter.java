package com.abby.redditgo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.bumptech.glide.Glide;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.orhanobut.logger.Logger;
import com.squareup.phrase.Phrase;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Submission> mDataset;
    private final Context mContext;

    public MyAdapter(Context context) {
        mContext = context;
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

        @BindView(R.id.textView_score)
        TextView mScoreText;

        @BindView(R.id.textView_comments)
        TextView mCommentsText;

        @BindView(R.id.button_share)
        ImageButton mShareButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public void setDataset(List<Submission> submissions) {
        mDataset = new ArrayList<>();
        mDataset.addAll(submissions);
    }

    public void addAllDataset(List<Submission> submissions) {
        mDataset.addAll(submissions);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
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

        final Submission item = mDataset.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("hint: " + item.getPostHint() + ", link: " + item.getUrl());
                Intent intent;
                switch (item.getPostHint()) {
                    case IMAGE:
                        ImageDialogFragment dialog = ImageDialogFragment.newInstance(item.getUrl());
                        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), ImageDialogFragment.class.getSimpleName());
                        break;
                    case VIDEO:
                        String url = item.getUrl();
                        YouTubeInitializationResult result = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(mContext);
                        if (result == YouTubeInitializationResult.SUCCESS && url.contains("youtu")) {
                            intent = YouTubeIntents.createPlayVideoIntentWithOptions(mContext, "", true, false);
                            intent.setData(Uri.parse(url));
                            mContext.startActivity(intent);
                            break;
                        }
                    case LINK:
                    default:
                        intent = new Intent(mContext, DetailActivity.class);
                        intent.setData(Uri.parse(item.getUrl()));
                        mContext.startActivity(intent);
                }
            }
        });

        holder.mTitleText.setText(item.getTitle());

        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - item.getCreated().getTime());
        holder.mSubmitText.setText(Phrase.from(mContext, R.string.submit).put("hour", String.valueOf(hours)).put("author", item.getAuthor()).put("subreddit", item.getSubredditName()).format());

        holder.mScoreText.setText(String.valueOf(item.getScore()));
        holder.mCommentsText.setText(String.valueOf(item.getCommentCount()));

        holder.mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "This is the text shared.</p>");
                mContext.startActivity(Intent.createChooser(sharingIntent, "Share"));
            }
        });


        String thumbnail = item.getThumbnail();
        if (thumbnail == null) {
            holder.mIconImage.setVisibility(View.GONE);
            switch (item.getPostHint()) {
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
            switch (item.getPostHint()) {
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


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset != null) {
            return mDataset.size();
        }

        return 0;
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = mContext.getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }
}

