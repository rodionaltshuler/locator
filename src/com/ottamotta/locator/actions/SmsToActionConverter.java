package com.ottamotta.locator.actions;

import android.content.Context;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

/**
 */
public class SmsToActionConverter {

    public static final String LOCATION_REQUEST = "wru?";

    private static int SIGNIFICANT_PHONE_NUM_DIGITS = 10;

    public static final String TAG = "Locator::SmsToActionConverter";

    private Context mContext = LocatorApplication.getInstance();

    @Inject
    private ContactsModel contactsModel;

    private static SmsToActionConverter instance;

    private SmsToActionConverter() {
        LocatorInjector.inject(this);
    }

    public static synchronized SmsToActionConverter getInstance() {

        if (null == instance) {
            instance = new SmsToActionConverter();
        }
        return instance;
    }


    public Action getActionFromSms(String phoneNumber, String body, long time) {

        //here we get contact
        TrustedContact contact = contactsModel.getContactByPhoneNumber(phoneNumber);
        Log.d(LocatorApplication.TAG, "getting action from sms; contact id is " + contact.getId());
        Action action = new Action(phoneNumber, contact);
        action.setType(Action.TYPE_IN);

        Log.d(LocatorApplication.TAG, "Received SMS from number: " + phoneNumber);

        action.setContact(contact);
        action.setTime(time);
        String message = body;

        String[] parts = message.split(BaseLocatorActionExecutor.PARTS_DELIMETER);

        if (parts.length < BaseLocatorActionExecutor.SMS_PARTS) {
            Log.d(TAG, "Sms contains data parts to be request of our app");
        }

        try {
            Log.d(TAG, "Finding out is message contains request");
            action.setRequest(isRequest(parts[BaseLocatorActionExecutor.LOCATION_REQUEST_PART]));

            Log.d(TAG, "setting coords if exits...");
            LatLng[] locations = getLocations(parts[BaseLocatorActionExecutor.COORDS_PART]);
            if (locations[0] != null) {
                action.setLocation(locations[0]);
            }
            if (locations[1] != null) {
                action.setPrevLocation(locations[1]);
            }

            if (!action.isRequest() && null == action.getLocation()) {
                Log.d(TAG, message + " is NOT our message!");
                return null;
            }

            Log.d(TAG, message + " is our message!");

            Log.d(TAG, "setting address if exits...");
            setAddress(action, parts[BaseLocatorActionExecutor.ADDRESS_PART]);
            Log.d(TAG, "setting altitude if exits...");
            setAltitude(action, parts[BaseLocatorActionExecutor.ALTITUDE_PART]);
            Log.d(TAG, "setting user activity if exits...");
            setActivity(action, parts[BaseLocatorActionExecutor.ACTIVITY_PART]);
            Log.d(TAG, "setting prevTime id if exits...");
            setPrevTime(action, parts[BaseLocatorActionExecutor.TIME_BETWEEN_LOCATIONS_PART]);

        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "Index out of bounds");
        }

        return action;

    }


    private void setPrevTime(Action action, String timeoutBetweenLocationsSec) {
        if (timeoutBetweenLocationsSec.trim().length() > 0) {
            action.setPrevTime(action.getTime() - Long.valueOf(timeoutBetweenLocationsSec) * 1000);
        }
    }

    private void setActivity(Action action, String humanActivity) {
        if (humanActivity.trim().length() > 0) {
            action.setHumanActivity(Integer.valueOf(humanActivity));
        }
    }

    private void setAltitude(Action action, String altitude) {
        if (altitude.trim().length() > 0) {
            action.setAltitude(Integer.valueOf(altitude));
        }
    }

    public Action getActionFromSms(SmsMessage currentMessage) {
        return getActionFromSms(currentMessage.getDisplayOriginatingAddress(), currentMessage.getMessageBody(), currentMessage.getTimestampMillis());
    }

    private boolean isRequest(String requestPart) {
        return (requestPart != null && requestPart.contains(LOCATION_REQUEST));
    }


    private void setAddress(Action action, String address) {
        if (address.trim().length() > 0) {
            action.setAddress(address);
        }
    }


    private LatLng[] getLocations(String coords) {
        LatLng[] result = new LatLng[2];
        try {
            String[] latLon = coords.split(",");
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            result[0] = new LatLng(lat, lon);
            double prevLat = Double.parseDouble(latLon[2]);
            double prevLon = Double.parseDouble(latLon[3]);
            result[1] = new LatLng(prevLat, prevLon);
        } catch (Exception e) {

        }
        return result;
    }

    private LatLng getLocation(String coords) {

        try {
            String[] latLon = coords.split(",");
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            LatLng result = new LatLng(lat, lon);
            return result;
        } catch (Exception e) {

        }

        return null;
    }

}
