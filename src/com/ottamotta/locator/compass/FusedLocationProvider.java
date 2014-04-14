package com.ottamotta.locator.compass;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class FusedLocationProvider implements GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    protected LocationListener locationListener;

    public interface LocationListener {
        public void onReceiveLocation(Location location);
        public void onGetLocationTimeout();
    }

    public static final String TAG = "Fused";

    protected LocationClient locationClient;
    protected LocationRequest locationRequest;
    protected int minDistanceToUpdate = 1;
    protected int numUpdates = 1;

    protected Context mContext;

    private int mInterval = 1000;

    private boolean flagStarted = false;

    private static final int DEFAULT_LOCATION_TIMEOUT_MS = 30000;
    private Handler handler = new Handler();

    private Runnable getLocationTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            notifyListenerTimeout();
        }
    };
    private int locationTimeout;

    public int getLocationTimeout() {
        return 0 == locationTimeout ? DEFAULT_LOCATION_TIMEOUT_MS : locationTimeout;
    }

    public void setLocationTimeout(int locationTimeout) {
        this.locationTimeout = locationTimeout;
    }

    protected void notifyListenerTimeout() {
        if (locationListener != null) {
            locationListener.onGetLocationTimeout();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(minDistanceToUpdate);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(mInterval);

        locationRequest.setSmallestDisplacement(minDistanceToUpdate);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(mInterval);

        if (numUpdates > 0) locationRequest.setNumUpdates(numUpdates);

        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onDisconnected() {
        //Log.d(TAG, "Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        notifyListenerTimeout();
    }

    public static FusedLocationProvider newInstance(Context context, LocationListener listener) {
        return new FusedLocationProvider(context, listener);
    }

    public static FusedLocationProvider newInstance(Context context, LocationListener listener, int numUpdates, int timeOut) {
        FusedLocationProvider provider = new FusedLocationProvider(context, listener);
        provider.numUpdates = numUpdates;
        provider.locationTimeout = timeOut;
        return provider;
    }


    public FusedLocationProvider(Context context, LocationListener listener) {
        mContext = context;
        if (listener != null)
            locationListener = listener;
    }

    private void setupTimeoutMessage() {
        handler.removeCallbacks(getLocationTimeoutRunnable);
        handler.postDelayed(getLocationTimeoutRunnable, getLocationTimeout());
    }

    public void start() {
        if (locationClient == null) {
            locationClient = new LocationClient(mContext, this, this);
            locationClient.connect();
        }
        else if (locationClient.isConnected()) {
            requestLocationUpdates();
        }
        else {
            locationClient.connect();
        }
    }

    protected void notifyListener(Location location) {
        if (locationListener != null)
            locationListener.onReceiveLocation(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        handler.removeCallbacks(getLocationTimeoutRunnable);
        notifyListener(location);
    }

    public void stop() {
        removeListener();
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        else {
            locationClient.unregisterConnectionCallbacks(this);
        }

    }

    public void setListener(LocationListener listener) {
        setupTimeoutMessage();
        locationListener = listener;
        start();
    }

    public void removeListener() {
        //locationListener = null;
    }

}
