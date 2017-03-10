package com.abby.redditgo;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by gsshop on 2017. 3. 2..
 */

public abstract class RedditgoAds {
    public static void initAds(Context context) {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713");
    }

    public static void loadAds(View view) {
        if(view instanceof AdView) {
            AdView adView = (AdView) view;
            // Create an ad request. Check your logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
        }
    }

    public static void pauseAds(View view) {
        if(view instanceof AdView) {
            AdView adView = (AdView) view;
            adView.pause();
        }
    }

    public static void resumeAds(View view) {
        if(view instanceof AdView) {
            AdView adView = (AdView) view;
            adView.resume();
        }
    }

    public static void destoryAds(View view) {
        if(view instanceof AdView) {
            AdView adView = (AdView) view;
            adView.destroy();
        }
    }


}
