package com.abby.redditgo;

import android.app.Application;
import android.provider.Settings;

import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidRedditClient;
import net.dean.jraw.android.AndroidTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;

import java.util.UUID;

/**
 * Created by gsshop on 2016. 10. 11..
 */

public class MainApplication extends Application {
    private static UUID uuid;

    public static UUID getUuid() {
        if (uuid != null) {
            return uuid;
        }

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        RedditClient reddit = new AndroidRedditClient(this);
        reddit.setLoggingMode(LoggingMode.ALWAYS);
        AuthenticationManager.get().init(reddit, new RefreshTokenHandler(new AndroidTokenStore(this), reddit));
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Logger.i(androidId);

        uuid = UUID.nameUUIDFromBytes(androidId.getBytes());
    }

}
