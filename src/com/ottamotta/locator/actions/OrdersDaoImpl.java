package com.ottamotta.locator.actions;

import android.content.Context;

import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrdersDaoImpl implements OrdersDao {

    @Inject
    Context context;

    private List<Order> orders;

    private OrdersDB ordersDB;

    @Inject
    private ContactsModel contactsModel;

    public OrdersDaoImpl() {
        orders = getEmptyList();
    }

    private List<Order> getEmptyList() {
        return new ArrayList<>();
    }

    @Override
    public Order create(Action startAction) {
        if (startAction.getTime() == 0) startAction.setTime(System.currentTimeMillis());
        Order order = new Order(startAction, new Order.HistoryRecord(Order.STATUS_BEGIN, Order.getInitialComment(startAction), startAction.getTime()));
        getOrdersDB().createOrder(order);
        orders.add(order);
        return order;
    }


    private OrdersDB getOrdersDB() {
        if (ordersDB == null) {
            ordersDB = new OrdersDB(context);
        }
        return ordersDB;
    }

    @Override
    public void updateOrder(Order order) {
        getOrdersDB().updateOrder(order);
        updateOrdersFromDb();
    }

    @Override
    public int deleteOrder(long orderId) {
        int ordersDeleted = getOrdersDB().deleteOrder(orderId);
        if (ordersDeleted > 0) updateOrdersFromDb();
        return ordersDeleted;
    }

    @Override
    public Order getOrder(long orderId) {
        for (Order o : orders) {
            if (o.getId() == orderId) return o;
        }
        return getOrdersDB().getSavedOrders(orderId); //TODO maybe this is not needed
    }


    private List<Order> getOrdersFromDb() {
        List<Order> orders = getOrdersDB().getSavedOrders();
        return orders;
    }

    @Override
    public void updateOrdersFromDb() {
        orders = getOrdersFromDb();
    }

    @Override
    public List<Order> getOrders() {
        if (orders == null || orders.size() == 0) {
            orders = getOrdersFromDb();
        }
        if (orders == null) return null;
        return Collections.unmodifiableList(orders);
    }

    @Override
    public Order getLastOrderForContact(TrustedContact contact) {

        List<Order> orders = getOrders();
        if (orders == null) return null;

        List<Order> ordersForThisContact = getEmptyList();
        for (Order order : this.orders) {
            if (order.getContact().equals(contact))
                ordersForThisContact.add(order);
        }

        if (ordersForThisContact.size() > 0) {
            Collections.sort(ordersForThisContact, Order.COMPARATOR_BY_TIME);
            Order lastOrder = ordersForThisContact.get(ordersForThisContact.size() - 1);
            return lastOrder;
        }

        return null;
    }


}
