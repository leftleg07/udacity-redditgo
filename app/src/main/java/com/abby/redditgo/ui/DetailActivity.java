package com.abby.redditgo.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.abby.redditgo.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Uri uri = getIntent().getData();
            DetailFragment fragment = DetailFragment.newInstance(uri);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, fragment).commit();
        }
    }

}
