package com.ottamotta.locator.external;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;

public interface LocationShareOption {
    Intent getShareIntent(LatLng locationFrom, LatLng locationTo);
    String getAppName();
    String getPackageName();
    int getId();
}
