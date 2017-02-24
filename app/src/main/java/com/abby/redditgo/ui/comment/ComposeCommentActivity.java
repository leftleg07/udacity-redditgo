package com.abby.redditgo.ui.comment;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.job.CommentReplyCommentJob;
import com.abby.redditgo.job.CommentReplySubmissionJob;
import com.abby.redditgo.ui.BaseActivity;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ComposeCommentActivity extends BaseActivity implements ComposeCommentFragment.OnFragmentInteractionListener {

    public static final String EXTRA_COMMENT_ID = "_extra_comment_id";
    public static final String EXTRA_SUBMISSION_ID = "_extra_submission_id";
    public static final String EXTRA_TITLE = "_extra_title";

    @Inject
    JobManager mJobManager;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String fullname = null;
    private String submissionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_comment);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        fullname = getIntent().getStringExtra(EXTRA_COMMENT_ID);
        submissionId = getIntent().getStringExtra(EXTRA_SUBMISSION_ID);

        if (savedInstanceState == null) {

            String title = getIntent().getStringExtra(EXTRA_TITLE);
            ComposeCommentFragment fragment = ComposeCommentFragment.newInstance(title);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, fragment).commit();
        }
    }

    @Override
    protected void injectActivity(ApplicationComponent component) {
        component.inject(this);

    }

    @Override
    public void activityFinish() {
        finish();
    }

    @Override
    public void reply(String replyText) {
        Job job = null;
        if (submissionId != null) {
            job = new CommentReplySubmissionJob(submissionId, replyText);
        } else {
            job = new CommentReplyCommentJob(fullname, replyText);
        }

        mJobManager.addJobInBackground(job);
    }

}
