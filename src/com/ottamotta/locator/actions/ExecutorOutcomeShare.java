package com.ottamotta.locator.actions;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.R;
import com.ottamotta.locator.compass.FusedLocationProvider;
import com.ottamotta.locator.map.MapFragmentActivity;

import java.util.List;

/**
 * Outcome
 * Share
 */
class ExecutorOutcomeShare extends BaseLocatorActionExecutor {

    private static final int SEARCH_LOCATION_TIMEOUT = 60 * 1000;
    private static final long TIMEOUT_BETWEEN_LOCATIONS = 60 * 1000;
    private SmsSender mSmsSender;

    private boolean locationDeterminedAutomatically = false;

    private Handler handler = new Handler();

    public ExecutorOutcomeShare(Action action) {
        super(action);
        mSmsSender = SmsSender.getInstance();
    }

    @Override
    public void doAction() {
        final Order order = orderExecutor.getOrder(action.getOrderId());
        orderExecutor.setOrderStatus(order, Order.STATUS_SEARCHING_FOR_LOCATION);
        getLocationAndSend(order);
    }

    private void getLocationAndSend(final Order order) {
        if (action.getLocation() != null) {
            //location already pointed manually
            sendLocation(order);
        }
        else {
            final FusedLocationProvider fusedLocationProvider = FusedLocationProvider.newInstance(context, null, 1, SEARCH_LOCATION_TIMEOUT);
            FusedLocationProvider.LocationListener locationListener = new FusedLocationProvider.LocationListener() {
                @Override
                public void onReceiveLocation(Location location) {
                    if (action.getLocation() == null) {
                        //location is not already pointed manually
                        locationDeterminedAutomatically = true;
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        action.setLocation(userLocation);
                        action.setTime(System.currentTimeMillis());
                        sendLocation(order);
                    }
                    fusedLocationProvider.stop();
                }
                @Override
                public void onGetLocationTimeout() {
                    locationDeterminedAutomatically = false;
                    orderExecutor.setOrderStatus(order, Order.STATUS_FAILED_TO_FIND_LOCATION);
                }
            };
            fusedLocationProvider.setListener(locationListener);
        }

    }

    private void sendLocation(final Order order) {
        if (locationDeterminedAutomatically && action.getPrevLocation() == null) {
            //location received first time
            action.setPrevLocation(action.getLocation());
            action.setPrevTime(System.currentTimeMillis());
            action.setLocation(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLocationAndSend(order);
                }
            }, TIMEOUT_BETWEEN_LOCATIONS);
        }
        else {
            //location received second time or pointed manually
            action.setTimeoutBetweenLocation((action.getTime() - action.getPrevTime())/1000);
            mSmsSender.sendSms(action, getSmsText(action));
            orderExecutor.setOrderStatus(order, Order.STATUS_LOCATION_FOUND);
        }
    }

    @Override
    public String getSmsText(final Action action) {

        StringBuilder sms = new StringBuilder();

        //no request we will send in response?
        sms.append(EMPTY_PART);

        sms.append(action.getLocationStringForSms() + PARTS_DELIMETER);

        //address
        if (action.getAddress() != null) {
            sms.append(action.getAddress() + PARTS_DELIMETER);
        }

        else {
            sms.append(EMPTY_PART);
        }

        //altitude
        sms.append(EMPTY_PART);

        //activity
        sms.append(EMPTY_PART);

        //original order id
        if (action.getPrevTime() != 0) {
            sms.append(action.getTimeoutBetweenLocations());
        }
        return sms.toString();
    }

    @Override
    @Deprecated
    //use BaseLocationActionExecutor.showNotificationStatusChanged
    protected void buildNotification(Action actionOut) {

        Intent mapIntent = new Intent(context, MapFragmentActivity.class);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapIntent.putExtra(Action.EXTRA_MARKER_SERIALIZED, actionOut);

        LocatorNotification.Builder(context)
            .setContact(actionOut.getContact())
            .setIntent(mapIntent)
            .setTitle(context.getString(R.string.location_shared))
            .setMessage(context.getString(R.string.location_shared_to_when, actionOut.getContact().getName(), actionOut.getTime()))
            .buildAndNotify();
    }

    @Override
    public void getMenuItems(Action action, int status, List<LocatorMenuItem> menu) {
        super.getMenuItems(action, status, menu);
    }

    @Override
    public String getInitialComment(Action startAction) {
        String comment = context.getResources().getString(R.string.you_shared_location_with) + " " + startAction.getContact().getName();
        return comment;
    }

}
