package com.ottamotta.locator.manualLocation;

import com.google.android.gms.maps.model.LatLng;

public class ShowLocationOnMapEvent {
    public LatLng location;

    public ShowLocationOnMapEvent(LatLng location) {
        this.location = location;
    }
}
