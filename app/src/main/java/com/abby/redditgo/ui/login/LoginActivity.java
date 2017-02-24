package com.abby.redditgo.ui.login;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.job.SigninJob;
import com.abby.redditgo.ui.BaseActivity;
import com.birbit.android.jobqueue.JobManager;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoginFragment.OnFragmentInteractionListener{

    @Inject
    JobManager mJobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
    public void login(String username, String password) {
        mJobManager.addJobInBackground(new SigninJob(username, password));
    }
}

