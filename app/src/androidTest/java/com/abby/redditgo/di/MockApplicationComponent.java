package com.abby.redditgo.di;

import com.abby.redditgo.data.RedditApiTest;

import javax.inject.Singleton;

import dagger.Component;

/**
 * google dagger component
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface MockApplicationComponent {
    void inject(RedditApiTest redditApiTest);
}
