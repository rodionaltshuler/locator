package com.ottamotta.locator.application;

import android.app.Application;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.ottamotta.locator.appRater.apprater.AppRater;
import com.ottamotta.locator.contacts.ObserverContactsChangeService;

public class LocatorApplication extends Application {

    public static final String TAG = "LocatorApplication";
    public static final String PROMO_SITE_URL = "www.smslocator.com";
    private static LocatorApplication instance;

    public LocatorApplication() {

    }

    public static LocatorApplication getInstance() {
        if (null == instance) {
            instance = new LocatorApplication();
        }
        return instance;
    }

    public static void rateNow(FragmentActivity activity) {
        AppRater.showRateDialog(activity);
    }

    public static void initRate(FragmentActivity context) {
        AppRater.app_launched(context);
        AppRater.setMarket(new LocatorMarket());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService(new Intent(this, ObserverContactsChangeService.class));
        //TODO use own theme; re-build https://github.com/codechimp-org/AppRater/blob/master/AppRater/src/org/codechimp/apprater/AppRater.java
    }

}
