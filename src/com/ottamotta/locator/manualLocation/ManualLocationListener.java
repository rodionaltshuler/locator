package com.ottamotta.locator.manualLocation;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.ottamotta.locator.compass.FusedLocationProvider;

import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;

@Singleton
public class ManualLocationListener {

    public static final String MESSAGE_EXTRA = "cause";

    @Inject
    private EventBus bus;

    @Inject
    private Context context;

    private Stack<FusedLocationProvider.LocationListener> listeners = new Stack<>();

    private ManualLocationReceivedEvent lastEvent;

    /**
     *
     * @param listener to whom post location when it's known
     */
    public void getLocationManually(FusedLocationProvider.LocationListener listener, String message) {
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
        listeners.push(listener);
        if (lastEvent != null && !lastEvent.isExpired()) {
            onGetLocation(lastEvent.location);
        }
        else {
            Intent intent = new Intent(context, ManualLocationActivity.class);
            intent.putExtra(MESSAGE_EXTRA, message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void onEvent(ManualLocationReceivedEvent event) {
        lastEvent = event;
        onGetLocation(event.location);
    }

    private void onGetLocation(Location location) {
        notifyListeners(location);
        bus.unregister(this);
    }

    private void notifyListeners(Location location) {
        while (!listeners.isEmpty()) {
            FusedLocationProvider.LocationListener listener = listeners.pop();
            listener.onReceiveLocation(location);
        }
    }

}
