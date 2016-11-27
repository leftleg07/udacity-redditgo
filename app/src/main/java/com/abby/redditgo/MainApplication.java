package com.abby.redditgo;

import android.support.multidex.MultiDexApplication;

import com.abby.redditgo.di.ApplicationComponent;
import com.abby.redditgo.di.ApplicationModule;
import com.abby.redditgo.di.DaggerApplicationComponent;
import com.facebook.stetho.Stetho;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidRedditClient;
import net.dean.jraw.android.AndroidTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;

/**
 * Created by gsshop on 2016. 10. 11..
 */

public class MainApplication extends MultiDexApplication {

    private static MainApplication instance;
    public static MainApplication getInstance() {
        return instance;
    }

    private ApplicationComponent component;
    public ApplicationComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        component = DaggerApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();

        RedditClient reddit = new AndroidRedditClient(this);
        reddit.setLoggingMode(LoggingMode.ALWAYS);
        AuthenticationManager.get().init(reddit, new RefreshTokenHandler(new AndroidTokenStore(this), reddit));

    }

}
