package com.abby.redditgo.ui.comment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.event.CommentErrorEvent;
import com.abby.redditgo.event.CommentRefreshEvent;
import com.abby.redditgo.job.CommentReplyCommentJob;
import com.abby.redditgo.ui.BaseActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentComposeActivity extends BaseActivity {

    public static final String EXTRA_COMMENT_FULLNAME = "_extra_comment_fullname_extra_comment_fullname";

    @Inject
    JobManager mJobManager;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.login_progress)
    ProgressBar progressView;

    @BindView(R.id.form_comment)
    View ComposeFormView;

    @BindView(R.id.new_comment_reply_to)
    TextView replyToTextView;


    @BindView(R.id.new_comment_reply)
    EditText replyEditText;

    @BindView(R.id.new_comment_submit)
    Button submitButton;

    private String commentFullname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_compose);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        commentFullname = getIntent().getStringExtra(EXTRA_COMMENT_FULLNAME);
    }

    @Override
    protected void injectActivity(ApplicationComponent component) {
        component.inject(this);

    }

    @OnClick(R.id.new_comment_submit)
    public void onSubmmit(View view) {
        showProgress(true);
        mJobManager.addJobInBackground(new CommentReplyCommentJob(commentFullname, "Bugaboo Boxer and the new luggage changing the way we travel"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onCommentRefreshEvent(CommentRefreshEvent event) {
        showProgress(false);
        finish();
    }

    @Subscribe
    public void onCommentErrorEvent(CommentErrorEvent event) {
        showProgress(false);
        new MaterialDialog.Builder(this)
                .title("reply to")
                .positiveText(android.R.string.ok)
                .content(event.errorMessage)
                .show();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            ComposeFormView.setVisibility(!show ? View.GONE : View.VISIBLE);
            ComposeFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ComposeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(!show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            ComposeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
}
