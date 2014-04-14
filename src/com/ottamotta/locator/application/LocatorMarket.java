package com.ottamotta.locator.application;

import android.content.Context;
import android.net.Uri;

import com.ottamotta.locator.appRater.apprater.Market;

public class LocatorMarket implements Market {

    @Override
    public Uri getMarketURI(Context context) {
        return Uri.parse("https://play.google.com/store/apps/details?id=com.ottamotta.localapps");
    }

}
