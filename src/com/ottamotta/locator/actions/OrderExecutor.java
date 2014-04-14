package com.ottamotta.locator.actions;

import android.telephony.SmsMessage;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.contacts.TrustedContact;

import java.util.List;

public interface OrderExecutor {

    LatLng getLastSharedLocation();

    void retry(Action action);

    void addToTrustedAndDoAction(Action action);

    void addToTrusted(Action action);

    void rejectAddingToTrusted(Action action);

    void shareOnce(Action action);

    void setOrderStatus(Order order, int newStatus);

    /**
     * Use setOrderStatus(Order order, int newStatus) instead
     * @param order
     */
    @Deprecated
    void setOrderStatus(Order order, int newStatus, String comment, long time);

    void replaceRequestWithReplyAction(Order order, Action replyAction);

    void cancel(Action action);

    void fillContextMenu(Order order, List<LocatorMenuItem> menu);

    void requestLocation(TrustedContact contact);

    void shareLocation(TrustedContact contact);

    void createOrderFromIncomeSms(String senderPhoneNum, String smsBody, long time);

    void createOrderFromIncomeSms(SmsMessage currentMessage);

    void updateOrderSmsSent(Action action, int resultCode);

    void updateOrderSmsDelivered(Action action);

    void demoIncomeShare(TrustedContact item);

    void demoNotTrustedRequest();

    void demoShare(TrustedContact item);

    //refactored from dao
    Order create(Action startAction);

    int deleteOrder(long orderId);

    Order getOrder(long orderId);

    List<Order> getOrders();

    Order getLastOrderForContact(TrustedContact contact);

    public static class OrderStatusChangedEvent {
        public long orderId;
        public int newStatus;

        public OrderStatusChangedEvent(long orderId, int newStatus) {
            this.orderId = orderId;
            this.newStatus = newStatus;
        }
    }

    public class OrdersChangedEvent {
        //TODO pass unmodifiable set of orders here
    }

    public static class LocationFoundForRequestEvent {
        public Action action;

        public LocationFoundForRequestEvent(Action action) {
            this.action = action;
        }
    }

}
