package com.abby.redditgo.di;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.abby.redditgo.MainApplication;
import com.abby.redditgo.job.BaseJob;
import com.abby.redditgo.services.MyGcmJobService;
import com.abby.redditgo.services.MyJobService;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.di.DependencyInjector;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * dagger application module
 */
@Module
public class ApplicationModule {
    private final Context mApplicationContext;

    public ApplicationModule(Context applicationContext) {
        this.mApplicationContext = applicationContext;
    }

    @Provides
    @Singleton
    public Context proviceApplicationContext() {
        return mApplicationContext;
    }

    @Provides
    @Singleton // Application reference must come from AppModule.class
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
    }

    @Provides
    @Singleton
    public UUID provideUUID(ContentResolver contentResolver) {
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        UUID uuid = UUID.nameUUIDFromBytes(androidId.getBytes());
        return uuid;
    }

    /**
     * JobManager
     *
     * @return
     */
    @Singleton
    @Provides
    public JobManager provideJobManager() {
        Configuration.Builder builder = new Configuration.Builder(mApplicationContext)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {

                    }
                })
                .minConsumerCount(4)//always keep at least one consumer alive
                .maxConsumerCount(8)//up to 3 consumers at a time
                .loadFactor(8)//3 jobs per consumer
                .injector(new DependencyInjector() {
                    @Override
                    public void inject(Job job) {
                        ApplicationComponent component = ((MainApplication) mApplicationContext).getComponent();
                        if(job instanceof BaseJob) {
                            ((BaseJob)job).inject(component);
                        }
                    }
                })
                .consumerKeepAlive(120);//wait 2 minute
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(mApplicationContext,
                    MyJobService.class), true);
        } else {
            int enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mApplicationContext);
            if (enableGcm == ConnectionResult.SUCCESS) {
                builder.scheduler(GcmJobSchedulerService.createSchedulerFor(mApplicationContext,
                        MyGcmJobService.class), true);
            }
        }
        return new JobManager(builder.build());
    }

    @Singleton
    @Provides
    public ContentResolver provideContentResolver() {
        return mApplicationContext.getContentResolver();
    }
}
