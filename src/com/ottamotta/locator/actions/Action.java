package com.ottamotta.locator.actions;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.utils.LocationUtils;
import com.ottamotta.locator.contacts.TrustedContact;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Action should be taken after SMS receive
 */
public class Action implements Parcelable, Cloneable {

    public static final String EXTRA_MARKER_SERIALIZED = "actionMarker";
    public static final String EXTRA_PREV_MARKER_SERIALIZED = "prevActionMarker";
    public static final String EXTRA_ACTION = "action";

    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm");  //used to display time on map

    public static final int TYPE_IN = 0;
    public static final int TYPE_OUT = 1;

    public static final Comparator<? super Action> COMPARATOR_MOST_RECENT = new Comparator<Action>() {
        @Override
        public int compare(Action one, Action two) {
            if (one.getTime() > two.getTime()) return -1;
            return 1;
        }
    };
    private static final double MIN_SIGNIFICANT_DISTANCE = 30;

    private long orderId;

    private String notificationText;
    private String notificationTitle;
    private long time;
    private int type; //IN or OUT
    private String address;

    private boolean isRequest;
    private boolean needLocation; //for out action, location null before execution
    private LatLng location;
    private LatLng prevLocation;
    private long prevTime;

    private TrustedContact contact;

    private PendingIntent pendingIntentForNotification;
    private long timeoutBetweenLocations;
    private int humanActivity;
    private int altitude;

    private String phoneNumToReply; //contact might have several phone num, we should reply to same number the request came from

    public Action clone() {

        Action clone = new Action();

        clone.orderId = orderId;
        clone.notificationText = notificationText;
        clone.notificationTitle = notificationTitle;
        clone.time = time;
        clone.type = type;
        clone.address = address;
        clone.isRequest = isRequest;
        clone.needLocation = needLocation;
        if (location != null) clone.location = new LatLng(location.latitude, location.longitude);
        clone.contact = contact;
        clone.pendingIntentForNotification = pendingIntentForNotification;
        clone.altitude = altitude;
        clone.humanActivity = humanActivity;
        clone.timeoutBetweenLocations = timeoutBetweenLocations;
        clone.phoneNumToReply = phoneNumToReply;
        clone.prevLocation = prevLocation;
        clone.prevTime = prevTime;
        return clone;
    }

    public static Action newRequestAction(TrustedContact contact) {
        Action action = new Action();
        action.setType(TYPE_OUT)
                .setRequest(true)
                .setContact(contact)
                .setPhoneNumToReply(contact.getMainPhoneNumber());
        return action;
    }

    public static Action newShareAction(TrustedContact contact) {
        Action action = new Action();
        action.setContact(contact)
                .setType(TYPE_IN)
                .setNeedLocation(true);
        return action;
    }


    public static Action builder() {
        return new Action();
    }

    private Action() {
    }

    public Action(String phoneNumToReply, TrustedContact contact) {
        this.phoneNumToReply = phoneNumToReply;
        this.contact = contact;
    }

    public Action(Parcel in) {
        orderId = in.readLong();
        notificationText = in.readString();
        notificationTitle = in.readString();
        time = in.readLong();
        type = in.readInt();
        address = in.readString();
        timeoutBetweenLocations = in.readLong();
        altitude = in.readInt();
        humanActivity = in.readInt();
        isRequest = in.readByte() != 0;
        needLocation = in.readByte() != 0;

        contact = in.readParcelable(((Object) this).getClass().getClassLoader());
        pendingIntentForNotification = in.readParcelable(((Object) this).getClass().getClassLoader());
        location = in.readParcelable(((Object) this).getClass().getClassLoader());
        phoneNumToReply = in.readString();
        prevLocation = in.readParcelable(((Object) this).getClass().getClassLoader());
        prevTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(orderId);
        dest.writeString(notificationText);
        dest.writeString(notificationTitle);
        dest.writeLong(time);
        dest.writeInt(type);
        dest.writeString(address);
        dest.writeLong(timeoutBetweenLocations);
        dest.writeInt(altitude);
        dest.writeInt(humanActivity);

        dest.writeByte((byte) (isRequest ? 1 : 0));
        dest.writeByte((byte) (needLocation ? 1 : 0));

        dest.writeParcelable(contact, flags);
        dest.writeParcelable(pendingIntentForNotification, flags);
        dest.writeParcelable(location, flags);

        dest.writeString(phoneNumToReply);
        dest.writeParcelable(prevLocation, flags);
        dest.writeLong(prevTime);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };


    public Action reply() {
        Action action = new Action();
        //мы отвечаем на income action созданную из смс, пришла нам
        if (timeoutBetweenLocations != 0) {
            action.timeoutBetweenLocations = timeoutBetweenLocations;
        }
        action.orderId = orderId;
        action.contact = this.contact;
        action.setRequest(false);
        action.setNeedLocation(true);
        action.time = System.currentTimeMillis();
        action.type = TYPE_OUT;
        action.phoneNumToReply = phoneNumToReply;
        return action;
    }

    public void doAction() {
        getExecutor().doAction();
    }

    public BaseLocatorActionExecutor getExecutor() {

        if (type == Action.TYPE_IN) {
            return getExecutorForIncomeAction();
        } else {
            return getExecutorForOutcomeAction();
        }

    }

    private BaseLocatorActionExecutor getExecutorForIncomeAction() {

        //REQUESTS
        if (isRequest() && isFromTrustedContact()) {
            return new ExecutorIncomeRequestTrusted(this);
        }

        if (isRequest() && !isFromTrustedContact()) {
            return new ExecutorIncomeRequestNotTrusted(this);
        }

        //RESPONSE
        if (getLocation() != null) {
            return new ExecutorIncomeShare(this);
        }


        return null;

    }

    private BaseLocatorActionExecutor getExecutorForOutcomeAction() {

        if (isRequest()) {
            return new ExecutorOutcomeRequest(this);

        }

        if (isNeedLocation()) {
            return new ExecutorOutcomeShare(this);
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
    }


    public int getType() {
        return type;
    }

    public Action setType(int type) {
        this.type = type;
        return this;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public LatLng getLocation() {
        return location;
    }

    public boolean isFromTrustedContact() {
        return contact.isTrusted();
    }

    public TrustedContact getContact() {
        return contact;
    }

    public Action setContact(TrustedContact contact) {
        this.contact = contact;
        return this;
    }

    public PendingIntent getPendingIntentForNotification() {
        return pendingIntentForNotification;
    }

    public Action setLocation(LatLng location) {
        this.location = location;
        return this;
    }

    public void setPendingIntentForNotification(PendingIntent pendingIntentForNotification) {
        this.pendingIntentForNotification = pendingIntentForNotification;
    }

    public long getTime() {
        return time;
    }

    public Action setTime(long time) {
        this.time = time;
        return this;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getLocationStringForSms() {
        if (prevLocation != null) {
            return LocationUtils.getLocationFormattedNoWhitespace(location) + "," +
                    LocationUtils.getLocationFormattedNoWhitespace(prevLocation);
        }
        return LocationUtils.getLocationFormattedNoWhitespace(location);
    }

    public String getAddress() {
        return address;
    }

    public Action setAddress(String address) {
        this.address = address;
        return this;
    }

    public Action setRequest(boolean request) {
        isRequest = request;
        return this;
    }

    public boolean isNeedLocation() {
        return needLocation;
    }

    public Action setNeedLocation(boolean needLocation) {
        this.needLocation = needLocation;
        return this;
    }

    public String getUpdateTimeFormatted() {
        return format.format(new Date(time));
    }

    public long getOrderId() {
        return orderId;
    }

    public Action setOrderId(long orderId) {
        this.orderId = orderId;
        return this;
    }

    public long getTimeoutBetweenLocations() {
        return timeoutBetweenLocations;
    }

    public int getHumanActivity() {
        return humanActivity;
    }

    public int getAltitude() {
        return altitude;
    }

    public Action setTimeoutBetweenLocation(Long originalOrderId) {
        this.timeoutBetweenLocations = originalOrderId;
        return this;
    }

    public Action setHumanActivity(int humanAcitivity) {
        this.humanActivity = humanAcitivity;
        return this;
    }

    public Action setAltitude(Integer altitude) {
        this.altitude = altitude;
        return this;
    }

    public String getPhoneNumToReply() {
        return phoneNumToReply;
    }

    public Action setPhoneNumToReply(String phoneNumToReply) {
        this.phoneNumToReply = phoneNumToReply;
        return this;
    }

    public LatLng getPrevLocation() {
        return prevLocation;
    }

    public Action setPrevLocation(LatLng prevLocation) {
        this.prevLocation = prevLocation;
        return this;
    }

    public long getPrevTime() {
        return prevTime;
    }

    public Action setPrevTime(long prevTime) {
        this.prevTime = prevTime;
        return this;
    }

    public long getDurationSec() {
        return (getTime() - getPrevTime())/1000;
    }

    public double getSpeedKmph() {
        double distance = getDistance();
        if (distance == 0) return 0;
        double speed = distance / getDurationSec() / 1000 * 3600;
        return speed;
    }

    public double getDistance() {
        if (getLocation() == null || getPrevLocation() == null) return 0;
        Location from = LocationUtils.createLocation(prevLocation.latitude, prevLocation.longitude);
        Location to = LocationUtils.createLocation(location.latitude, location.longitude);
        return from.distanceTo(to);
    }

    public String getSpeedFormatted() {
        NumberFormat format = new DecimalFormat(".##");
        return format.format(getSpeedKmph());
    }

    public boolean hasTwoCoords() {
        return prevTime > 0 && prevLocation != null && prevLocation.latitude != 0 && getDistance() >= MIN_SIGNIFICANT_DISTANCE;
    }
}
