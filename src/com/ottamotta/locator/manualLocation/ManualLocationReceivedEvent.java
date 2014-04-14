package com.ottamotta.locator.manualLocation;

import android.location.Location;

public class ManualLocationReceivedEvent {

    private static long EXPIRATION = 60 * 1000;

    public Location location;
    public long time;

    public ManualLocationReceivedEvent(Location location) {
        this.location = location;
        time = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > time + EXPIRATION;
    }
}
