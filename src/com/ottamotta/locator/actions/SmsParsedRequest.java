package com.ottamotta.locator.actions;

import android.telephony.SmsMessage;
import android.util.Log;

import com.ottamotta.locator.application.LocatorApplication;

import java.io.Serializable;

class SmsParsedRequest implements Serializable {

    public static final int REQUEST_TYPE_INCOMING = 0;
    public static final int REQUEST_TYPE_OUTCOMING = 1;

    public static final String LOCATION_REQUEST = "wru?";
    public static final String PARTS_DELIMETER = ";";

    private static final int LOCATION_REQUEST_PART = 0;
    private static final int COORDS_PART = 1;
    private static final int ADDRESS_PART = 2;
    private static final int ALTITUDE_PART = 3;
    private static final int ACTIVITY_PART = 4;

    private static final int SMS_PARTS = 5;

    public static final String TAG = "SmsReceiver";
    public static final String EMPTY_PART = " ;";

    private int type; //incoming or outcoming request
    private boolean isRequest;
    private double lat;
    private double lon;
    private String address;
    private double altitude;
    private int activity;
    private long time;
    private String phoneNumber;
    private boolean needRequestLocation;

    private boolean ourMessage = false; //is it message to be handled by our app?


    public static SmsParsedRequest fromSms(SmsMessage sms) {

        SmsParsedRequest smsRequest = new SmsParsedRequest(sms);
        return smsRequest;
    }

    /**
     * For demo purposes
     */
    public SmsParsedRequest(boolean makeRequest, double lat, double lon, String phoneNumber, long time) {
        this.lat = lat;
        this.lon = lon;
        this.phoneNumber = phoneNumber;
        this.time = time;
        this.isRequest = makeRequest;
    }

    public String getSmsToDispatch() {
        StringBuilder sms = new StringBuilder();

        if (isRequest() && needRequestLocation) {
            sms.append(LOCATION_REQUEST).append(PARTS_DELIMETER);
        } else {
            sms.append(EMPTY_PART);
        }

        if (lat != 0) {
            sms.append(lat + "," + lon + PARTS_DELIMETER);
        } else {
            sms.append(EMPTY_PART);
        }

        if (address != null) {
            sms.append(address + PARTS_DELIMETER);
        } else {
            sms.append(EMPTY_PART);
        }

        //altitude
        sms.append(EMPTY_PART);

        //activity
        sms.append(EMPTY_PART);

        return sms.toString();
    }


    private SmsParsedRequest(SmsMessage currentMessage) {

        type = REQUEST_TYPE_INCOMING; //we've received SMS, so it's incoming request


        phoneNumber = currentMessage.getDisplayOriginatingAddress();
        time = currentMessage.getTimestampMillis();

        String message;
        //message = currentMessage.getDisplayMessageBody(); //for text sms, not data sms
        message = new String(currentMessage.getUserData());

        String[] parts = message.split(PARTS_DELIMETER);

        currentMessage.getUserData();
        if (parts.length < SMS_PARTS) {
            Log.d(LocatorApplication.TAG, "Sms contains data parts to be request of our app");
        }


        try {
            Log.d(LocatorApplication.TAG, "Finding out is message contains request");
            setRequest(parts[LOCATION_REQUEST_PART]);
            Log.d(LocatorApplication.TAG, message + " is our message!");


            Log.d(LocatorApplication.TAG, "setting coords if exits...");
            setCoords(parts[COORDS_PART]);

            Log.d(LocatorApplication.TAG, "setting address if exits...");
            setAddress(parts[ADDRESS_PART]);
            setAltitude(parts[ALTITUDE_PART]);
            setActivity(parts[ACTIVITY_PART]);
        } catch (ArrayIndexOutOfBoundsException e) {
            //if it's not a message from our app, exception will be raised
            Log.d(LocatorApplication.TAG, "Index out of bounds");
        }

        if (isRequest || lat != 0) ourMessage = true;


    }

    private void setActivity(String activity) {

        try {
            this.activity = Integer.parseInt(activity);
        } catch (Exception e) {
        }

    }

    private void setAltitude(String altitude) {
        try {
            this.altitude = Double.parseDouble(altitude);
        } catch (Exception e) {
        }
    }


    private void setAddress(String address) {
        if (address.trim().length() > 0) {
            this.address = address;
        }
        //otherwise, address still null
    }


    private void setCoords(String coords) {

        try {
            String[] latLon = coords.split(",");
            lat = Double.parseDouble(latLon[0]);
            lon = Double.parseDouble(latLon[1]);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private void setRequest(String requestPart) {
        isRequest = requestPart != null && requestPart.contains(LOCATION_REQUEST);
    }

    public boolean isRequest() {
        return isRequest;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getAddress() {
        return address;
    }

    public double getAltitude() {
        return altitude;
    }

    public int getActivity() {
        return activity;
    }

    public long getTime() {
        return time;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isOurMessage() {
        return ourMessage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isNeedRequestLocation() {
        return needRequestLocation;
    }

    public void setNeedRequestLocation(boolean needRequestLocation) {
        this.needRequestLocation = needRequestLocation;
    }
}
