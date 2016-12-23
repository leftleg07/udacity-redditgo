package com.abby.redditgo.di;

import com.abby.redditgo.data.RedditApiTest;
import com.abby.redditgo.ui.CommentScreenTest;

import javax.inject.Singleton;

import dagger.Component;

/**
 * google dagger component
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface MockApplicationComponent {
    void inject(RedditApiTest redditApiTest);
    void inject(CommentScreenTest commentScreenTest);
}
