package com.abby.redditgo;


import com.abby.redditgo.di.ApplicationModule;
import com.abby.redditgo.di.DaggerMockApplicationComponent;
import com.abby.redditgo.di.MockApplicationComponent;


/**
 * Mock Application
 */
public class MockApplication extends MainApplication {
    public MockApplicationComponent getMockComponent() {
        return mMockComponent;
    }

    private MockApplicationComponent mMockComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mMockComponent = DaggerMockApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();
    }
}
