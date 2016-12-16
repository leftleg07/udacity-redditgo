package com.abby.redditgo.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.abby.redditgo.BaseActivity;
import com.abby.redditgo.R;
import com.abby.redditgo.di.ApplicationComponent;

public class DetailActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "_extra_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        toolbar.setTitle(title);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            Uri uri = getIntent().getData();
            DetailFragment fragment = DetailFragment.newInstance(uri);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, fragment).commit();
        }
    }

    @Override
    protected void injectActivity(ApplicationComponent component) {

    }

}
