package com.ottamotta.locator.compass;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.actions.BaseLocatorActionExecutor;
import com.ottamotta.locator.external.ExternalShareExecutor;
import com.ottamotta.locator.external.ExternalShareOptionsMenu;
import com.ottamotta.locator.external.LocationShareOption;
import com.ottamotta.locator.external.RequestPackageInstallDialogFragment;
import com.ottamotta.locator.ui.LocatorContactsActivity;
import com.ottamotta.locator.manualLocation.ManualLocationListener;
import com.ottamotta.locator.roboguice.RoboSherlockActivity;
import com.ottamotta.locator.utils.LocationUtils;

import javax.inject.Inject;

public class CompassFusionActivity extends RoboSherlockActivity implements FusionSensorListener.AzimuthListener {

    public static final int SHOW_LOCATION_SETTINGS_TIMEOUT = 10 * 1000;
    private static final int TIMEOUT_PAUSE_LOCATION_UPDATES_WHEN_GOING_BACKGROUND = 60 * 1000;
    private Action action;

    @Inject
    private ExternalShareOptionsMenu externalShareOptionsMenu;

    private FusionSensorListener sensorListener;

    private boolean sensorListenerRegistered = false;

    private float currentDegree = 0f;
    private FusedLocationProvider fusedLocationProvider;

    private Location currentLocation;
    private View root;
    private TextView tvAzimuth;
    private View loader;
    private TextView tvDistance;
    private ImageView compassImage;
    private View settingsInfo;
    private FusedLocationProvider.LocationListener locationListener = new FusedLocationProvider.LocationListener() {
        @Override
        public void onReceiveLocation(Location location) {
            if (currentLocation == null) setupLocationKnown();
            currentLocation = location;
            float distance = getDistance(location, action.getLocation());
            tvDistance.setText(getDistanceFormatted(distance));
            tvAzimuth.setText(getAzimuthFormatted(getHeadingToTargetDegrees()));
            registerSensorListener();
        }

        @Override
        public void onGetLocationTimeout() {
            animateShow(settingsInfo);
            settingsInfo.setVisibility(View.VISIBLE);
            manualLocationListener.getLocationManually(this, getMessageWhenLocationNotFound());
        }
    };

    private String getMessageWhenLocationNotFound() {
        return "Trying to navigate with compass to " + action.getContact().getName() + ", but can't find current location. Could You point it manually?";
    }

    @Inject
    private ManualLocationListener manualLocationListener;

    private Handler handler = new Handler();
    private Runnable pauseLocationUpdates = new Runnable() {
        @Override
        public void run() {
            fusedLocationProvider.stop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);

        sensorListener = new FusionSensorListener(this, this);

        action = getIntent().getExtras().getParcelable(Action.EXTRA_ACTION);

        getSupportActionBar().setLogo(action.getContact().getPhotoDrawable(this));
        getSupportActionBar().setTitle(action.getContact().getName());

        root = findViewById(R.id.root);

        loader = findViewById(R.id.progress_bar);
        settingsInfo = findViewById(R.id.settings_layout);

        tvDistance = (TextView) findViewById(R.id.distance);
        tvAzimuth = (TextView) findViewById(R.id.azimuth);
        compassImage = (ImageView) findViewById(R.id.compassView);

        settingsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locationSettingsIntent);
            }
        });

        setupLocationUnknown();
        searchForLocation();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected ExternalShareExecutor.Listener noExternalAppInstalledListener = new ExternalShareExecutor.Listener() {
        @Override
        public void onAppNotInstalled(LocationShareOption option) {
            RequestPackageInstallDialogFragment f = RequestPackageInstallDialogFragment.newInstance(
                    getString(R.string.request_install_dialog_title),
                    getString(R.string.request_install_dialog_message, option.getAppName()),
                    getString(R.string.request_install_dialog_positive),
                    getString(R.string.request_install_dialog_negative),
                    option.getPackageName(), option.getAppName());
            f.show(getSupportFragmentManager(), "dialogTag");
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startContactsActivity();
                return true;
            case R.id.menu_item_map:
                action.getExecutor().showOnMap();
                return true;
            default:
                if (externalShareOptionsMenu.onOptionsMenuItemSelected(item.getItemId(), action.getLocation(), noExternalAppInstalledListener)) {
                    return true;
                }
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupLocationUnknown() {
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.disabled_bg));
        root.setBackgroundColor(getResources().getColor(R.color.disabled_background_color));
        compassImage.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);
        animateShow(loader);
    }

    private void animateShow(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view.startAnimation(animation);
    }

    private void setupLocationKnown() {
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.enabled_bg));
        root.setBackgroundColor(getResources().getColor(R.color.background_color));
        compassImage.setVisibility(View.VISIBLE);
        loader.setVisibility(View.INVISIBLE);
    }

    private void searchForLocation() {
        fusedLocationProvider = FusedLocationProvider.newInstance(this, locationListener, 0, SHOW_LOCATION_SETTINGS_TIMEOUT);
        fusedLocationProvider.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentLocation == null) {
            handler.postDelayed(pauseLocationUpdates, TIMEOUT_PAUSE_LOCATION_UPDATES_WHEN_GOING_BACKGROUND);
        } else {
            handler.post(pauseLocationUpdates);
        }
        unregisterSensorListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(pauseLocationUpdates);
        fusedLocationProvider.setListener(locationListener);
        fusedLocationProvider.start();
        registerSensorListener();
    }

    private String getAzimuthFormatted(float azimuth) {
        if (azimuth < 0) azimuth = azimuth + 360;
        return LocationUtils.getDirection(azimuth) + " " + (int) azimuth;
    }

    private String getDistanceFormatted(float distance) {
        if (distance > 1) {
            float distKm = distance / 1000;
            return String.format("%.2f", distKm) + " km";
        }
        return (int) distance + " m";
    }

    private float getDistance(Location from, LatLng to) {
        float[] distances = new float[1];
        Location.distanceBetween(
                from.getLatitude(),
                from.getLongitude(),
                to.latitude,
                to.longitude,
                distances);
        return distances[0];
    }


    private void registerSensorListener() {

        if (!sensorListenerRegistered && currentLocation != null) {
            sensorListener.start();
            sensorListenerRegistered = true;
        }
    }

    private void unregisterSensorListener() {
        if (sensorListenerRegistered) {
            sensorListener.stop();
            sensorListenerRegistered = false;
        }
    }

    @Override
    public void onChange(float[] values) {

        float degree = values[0] - getHeadingToTargetDegrees();

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation tvAzimuth
        ra.setFillAfter(true);

        // Start the animation
        if (currentLocation != null) compassImage.startAnimation(ra);
        currentDegree = -degree;

    }

    private float getHeadingToTargetDegrees() {
        if (currentLocation != null) {
            Location targetLocation = new Location(currentLocation);
            targetLocation.setLatitude(action.getLocation().latitude);
            targetLocation.setLongitude(action.getLocation().longitude);
            return currentLocation.bearingTo(targetLocation);
        }
        return 0;
    }

    private void startContactsActivity() {
        Intent i = new Intent(this, LocatorContactsActivity.class);
        if (action.getContact() != null) i.putExtra(BaseLocatorActionExecutor.EXTRA_CONTACT, action.getContact());
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.compass_options_menu, menu);
        externalShareOptionsMenu.fill(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }


}
