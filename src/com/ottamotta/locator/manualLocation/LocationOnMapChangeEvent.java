package com.ottamotta.locator.manualLocation;

import com.google.android.gms.maps.model.LatLng;

public class LocationOnMapChangeEvent {
    public LatLng location;
    public LocationOnMapChangeEvent(LatLng newLocation) {
        this.location = newLocation;
    }
}
