package com.ottamotta.locator.external;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.R;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExternalLocationShareOptionsProvider {

    @Inject
    Context context;

    public List<LocationShareOption> getOptions() {
        List<LocationShareOption> options = new ArrayList<>();

        LocationShareOption googleMapsOption = new LocationShareOption() {
            static final String PACKAGE_NAME = "com.google.android.apps.maps";

            @Override
            public Intent getShareIntent(final LatLng locationFrom, final LatLng locationTo) {
                String uri = "http://maps.google.com/maps?saddr=" + locationFrom.latitude + "," + locationFrom.longitude +
                    "&daddr=" + locationTo.latitude + "," + locationTo.longitude;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName(PACKAGE_NAME, "com.google.android.maps.MapsActivity");
                return intent;
            }

            @Override
            public String getAppName() {
                return context.getResources().getString(R.string.google_maps_app_name);
            }

            @Override
            public String getPackageName() {
                return PACKAGE_NAME;
            }

            @Override
            public int getId() {
                return PACKAGE_NAME.hashCode();
            }
        };

        options.add(googleMapsOption);

        LocationShareOption yandexMapsOption = new LocationShareOption() {
            static final String PACKAGE_NAME = "ru.yandex.yandexmaps";

            @Override
            public Intent getShareIntent(final LatLng locationFrom, final LatLng locationTo) {
                final Intent intent = new Intent("ru.yandex.yandexmaps.action.BUILD_ROUTE_ON_MAP");
                intent.setPackage(PACKAGE_NAME);
                intent.putExtra("lat_from", locationFrom.latitude);
                intent.putExtra("lon_from", locationFrom.longitude);
                intent.putExtra("lat_to", locationTo.latitude);
                intent.putExtra("lon_to", locationTo.longitude);
                return intent;
            }

            @Override
            public String getAppName() {
                return context.getResources().getString(R.string.yandex_maps_app_name);
            }

            @Override
            public String getPackageName() {
                return PACKAGE_NAME;
            }

            @Override
            public int getId() {
                return PACKAGE_NAME.hashCode();
            }
        };
        options.add(yandexMapsOption);

        LocationShareOption androzicOption = new LocationShareOption() {
            static final String PACKAGE_NAME = "com.androzic";

            @Override
            public Intent getShareIntent(final LatLng locationFrom, final LatLng locationTo) {
                Intent oziIntent = new Intent();
                oziIntent.setAction("com.google.android.radar.SHOW_RADAR");
                oziIntent.putExtra("latitude", (float) (locationTo.latitude));
                oziIntent.putExtra("longitude", (float) (locationFrom.longitude));
                return oziIntent;
            }

            @Override
            public String getAppName() {
                return context.getResources().getString(R.string.androzic_app_name);
            }

            @Override
            public String getPackageName() {
                return PACKAGE_NAME;
            }

            @Override
            public int getId() {
                return PACKAGE_NAME.hashCode();
            }
        };
        options.add(androzicOption);

        return options;
    }

    /*    public void showOziAndrozic() {
            //https://github.com/andreynovikov/Androzic/tree/master/src/com/androzic
            Intent oziIntent = new Intent();
            oziIntent.setAction("com.google.android.radar.SHOW_RADAR");
            oziIntent.putExtra("latitude", (float) (action.getLocation().latitude));
            oziIntent.putExtra("longitude", (float) (action.getLocation().longitude));
            if (!(context instanceof Activity)) oziIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(oziIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Androzic Navigator isn't installed", Toast.LENGTH_LONG).show();
            }
        }*/

}
