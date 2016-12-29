package com.abby.redditgo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.abby.redditgo.MainApplication;
import com.abby.redditgo.di.ApplicationComponent;

/**
 * Created by gsshop on 2016. 12. 7..
 */

public abstract class BaseActivity extends AppCompatActivity {
    public ApplicationComponent getComponent() {
        return ((MainApplication) getApplication()).getComponent();
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectActivity(getComponent());
    }

    protected abstract void injectActivity(ApplicationComponent component);
}
