package com.ottamotta.locator.actions;

import android.content.res.Resources;
import android.util.SparseArray;

import com.ottamotta.locator.R;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.contacts.TrustedContact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * ORDER purpose - to hold info about the workflow currentStatus, from request/share begin to the end.
 * Each currentStatus could mean some actions available ("Repeat", for example?)
 * <p/>
 * When You request or share location, or someone sends you request/share, it's an ORDER.
 * <p/>
 * ORDER could contain one or more ACTIONS.
 * <p/>
 * EXAMPLE 1: 1 ORDER contains 2 ACTIONS
 * You received SMS request - it's an ORDER start. When other person received an SMS with your location
 * - it's an order end. This Order contains two Actions:
 * 1) Income Request Action (make a reply, show notification etc.)
 * 2) Outcome Share Action (send sms)
 * <p/>
 * EXAMPLE 2: 1 ORDER = 1 ACTION
 * You shared location. Only 1 ACTION will be made - Outcome Share Action.
 */
public class Order {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, HH:mm:ss");

    private static final EventBus mBus = EventBus.getDefault();

    public static final int STATUS_BEGIN = 0;

    public static final int STATUS_SEARCHING_FOR_LOCATION = 2;

    public static final int STATUS_SMS_SENT = 3;
    public static final int STATUS_SMS_DELIVERED = 4;
    public static final int STATUS_SMS_FAILED_TO_SEND = 5;

    public static final int STATUS_ADDING_TO_TRUSTED_REJECTED = 7;

    public static final int STATUS_FAILED_TO_FIND_LOCATION = 8;
    public static final int STATUS_LOCATION_FOUND = 9;

    public static final int STATUS_CANCELED = 98;
    public static final int STATUS_COMPLETED = 99;
    public static final int STATUS_DELETED = -1;

    public static final SparseArray<String> STATUS_COMMENTS = new SparseArray<>();

    static {
        Resources res = LocatorApplication.getInstance().getResources();
        STATUS_COMMENTS.put(STATUS_SEARCHING_FOR_LOCATION, res.getString(R.string.status_searching_for_location));
        STATUS_COMMENTS.put(STATUS_CANCELED, res.getString(R.string.status_canceled));
        STATUS_COMMENTS.put(STATUS_FAILED_TO_FIND_LOCATION, res.getString(R.string.failed_to_find_location));
        STATUS_COMMENTS.put(STATUS_LOCATION_FOUND, res.getString(R.string.location_found));
        STATUS_COMMENTS.put(STATUS_SMS_DELIVERED, res.getString(R.string.status_sms_delivered));
        STATUS_COMMENTS.put(STATUS_SMS_FAILED_TO_SEND, res.getString(R.string.status_not_sent_generic_failure));
        STATUS_COMMENTS.put(STATUS_SMS_SENT, res.getString(R.string.status_sent_successfully));
    }

    private long id;
    private Action startAction;
    private final List<HistoryRecord> history = new ArrayList<>();

    int currentStatus;

    public static final Comparator<Order> COMPARATOR_BY_TIME = new Comparator<Order>() {
        @Override
        public int compare(Order first, Order second) {
            HistoryRecord firstNewestRecord = first.getNewestRecord();
            HistoryRecord secondNewestRecord = second.getNewestRecord();
            if (firstNewestRecord.time > secondNewestRecord.time)
                return 1;
            if (firstNewestRecord.time < secondNewestRecord.time)
                return -1;
            return 0;
        }
    };

    public static final Comparator<Order> COMPARATOR_BY_CONTACT = new Comparator<Order>() {
        @Override
        public int compare(Order first, Order second) {
            return first.getContact().compareTo(second.getContact());
        }
    };

    public static class HistoryRecord {

        public static final int ORDER_ID_UNKNOWN = -1;
        public long id;
        public long orderId = ORDER_ID_UNKNOWN;

        public final int status;
        public final String comment;
        public final long time;

        public static final Comparator<HistoryRecord> COMPARATOR_BY_TIME = new Comparator<HistoryRecord>() {
            @Override
            public int compare(HistoryRecord first, HistoryRecord second) {
                if (first.time > second.time)
                    return 1;
                if (first.time < second.time)
                    return -1;
                return 0;
            }
        };

        public HistoryRecord(int status, String comment, long time) {
            this.status = status;
            this.comment = comment;
            this.time = time;
        }

        public HistoryRecord(long orderId, int status, String comment, long time) {
            this(status, comment, time);
            this.orderId = orderId;
        }

        public HistoryRecord(long id, long orderId, int status, String comment, long time) {
            this(orderId, status, comment, time);
            this.id = id;
        }
    }

    static String getInitialComment(Action startAction) {
        return startAction.getExecutor().getInitialComment(startAction); //getContactById().getName() + " " + startAction.getType() + " " + startAction.isNeedLocation();
    }

    public void startExecution() {
        startAction.doAction();
    }

    Order(Action startAction, HistoryRecord historyRecord) {
        this.startAction = startAction;
        this.currentStatus = historyRecord.status;
        history.add(historyRecord);
    }

    Order(Action startAction) {
        this.startAction = startAction;
        this.currentStatus = STATUS_BEGIN;
    }

    public Action getStartAction() {
        return startAction;
    }

    public void setStartAction(Action startAction) {
        this.startAction = startAction;
    }

    public List<HistoryRecord> getHistory() {
        return history;
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public TrustedContact getContact() {
        return startAction.getContact();
    }

    public String getStartTimeFormatted() {
        return startAction.getUpdateTimeFormatted();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        assert startAction != null;
        this.startAction.setOrderId(id);
        //this.startAction.setTimeoutBetweenLocation(id);
        for (HistoryRecord rec : getHistory())
            rec.orderId = id;
    }

    private HistoryRecord getNewestRecord() {
        assert history.size() >= 1;
        Collections.sort(history, HistoryRecord.COMPARATOR_BY_TIME);
        return history.get(history.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Order order = (Order) o;

        return (id == order.id);
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
