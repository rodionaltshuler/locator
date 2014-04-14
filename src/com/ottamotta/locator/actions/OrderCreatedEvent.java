package com.ottamotta.locator.actions;

public class OrderCreatedEvent {
    public Order order;
    public OrderCreatedEvent(Order order) {
        this.order = order;
    }
}
