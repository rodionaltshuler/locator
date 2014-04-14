package com.ottamotta.locator.external;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.compass.FusedLocationProvider;

import javax.inject.Inject;

public class ExternalShareExecutor {

    @Inject
    private Context context;

    private FusedLocationProvider locationProvider;

    public interface Listener {
        void onAppNotInstalled(LocationShareOption option);
    }

    public void doAction(final LocationShareOption option, final LatLng locationTo, final Listener noAppInstalledListener) {

        final FusedLocationProvider.LocationListener locationListener = new FusedLocationProvider.LocationListener() {
            @Override
            public void onReceiveLocation(Location location) {
                Intent intent = option.getShareIntent(new LatLng(location.getLatitude(), location.getLongitude()), locationTo);
                locationProvider.stop();
                if (!(context instanceof Activity))
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(intent);
                }
                catch (ActivityNotFoundException e) {

                    if (noAppInstalledListener != null) {
                        noAppInstalledListener.onAppNotInstalled(option);
                    }
                }
            }

            @Override
            public void onGetLocationTimeout() {
                //TODO implement
            }
        };
        locationProvider = FusedLocationProvider.newInstance(context, locationListener);
        locationProvider.start();
    }

}
