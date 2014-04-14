package com.ottamotta.locator.actions;

import com.ottamotta.locator.contacts.TrustedContact;

import java.util.List;

public interface OrdersDao {

    Order create(Action startAction);
     /**
     * @return Deleted orders rows count
     */
    public int deleteOrder(long orderId);
    public Order getOrder(long orderId);

    void updateOrdersFromDb();

    public List<Order> getOrders();
    public Order getLastOrderForContact(TrustedContact contact);

    void updateOrder(Order order);
}
