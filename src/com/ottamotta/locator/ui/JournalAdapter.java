package com.ottamotta.locator.ui;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.actions.LocatorMenuItem;
import com.ottamotta.locator.actions.Order;
import com.ottamotta.locator.actions.OrderCreatedEvent;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.actions.OrderExecutorImpl;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.ui.dialogs.ContextMenuDialogFragment;
import com.ottamotta.locator.roboguice.LocatorInjector;
import com.ottamotta.locator.utils.LocatorTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

class JournalAdapter extends ArrayAdapter<Order> {

    private LongSparseArray<Order> records;

    private Context context;

    @Inject
    private OrderExecutor orderExecutor;

    @Inject
    private EventBus bus;

    private TrustedContact contact;
    private long recentlyAddedOrderId = -1;

    public JournalAdapter(Context context, TrustedContact contact) {
        super(context, 0);
        LocatorInjector.inject(this);
        this.context = context;
        this.contact = contact;
        bus.register(this);
        initOrders();
    }

    @Override
    public Order getItem(int position) {
        return records.valueAt(position);
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Order order = getItem(position);

        View root;
        root = LayoutInflater.from(context).inflate(getLayoutResourceId(order.getStartAction().getType()), parent, false);

        LocatorTime locatorTime = new LocatorTime(order.getStartAction().getTime());

        TextView time = (TextView) root.findViewById(R.id.time);
        final TextView status = (TextView) root.findViewById(R.id.status);

        final TextView currentStatus = (TextView) root.findViewById(R.id.current_status);

        String currentStatusText = getCurrentStatus(order);


        if (null != currentStatus && order.getStartAction().getType() == Action.TYPE_OUT) {
            //Action.TYPE_IN has only one status so it won't be shown
            root.findViewById(R.id.current_status_layout).setVisibility(View.VISIBLE);
            currentStatus.setText(currentStatusText);
        }

        String timeFormatted = locatorTime.getTimeElapsedFormatted();
        time.setText(timeFormatted);

        final String orderDesc = getOrderDescription(order);
        status.setText(orderDesc);

        ImageView photo = (ImageView) root.findViewById(R.id.photo);
        setPhoto(photo, order);

        ImageView highlightArea = (ImageView) root.findViewById(R.id.context_menu_button);
        setupContextMenu(order, highlightArea);
        return root;
    }

    private void setupContextMenu(final Order order, ImageView menuButton) {
        final List<LocatorMenuItem> menuItems = new ArrayList<>();
        orderExecutor.fillContextMenu(order, menuItems);
        switch (menuItems.size()) {
            case 0:
                menuButton.setVisibility(View.GONE);
                break;
            case 1:
                final LocatorMenuItem menuItem = menuItems.get(0);
                menuButton.setImageResource(menuItem.getImageResourceId());
                menuButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuItem.run();
                    }
                });
                break;
            default:
                menuButton.setImageResource(R.drawable.ic_action_overflow);
                menuButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showContextMenu(menuItems);
                    }
                });
        }

    }

    private void showContextMenu(List<LocatorMenuItem> menuItems) {
        if (menuItems.size() > 0) {
            DialogFragment fragment = new ContextMenuDialogFragment(menuItems, contact);
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "contextMenu");
        }
    }

    private void setPhoto(ImageView imageView, Order order) {
        if (null == imageView) return;
        imageView.setImageDrawable(order.getContact().getPhotoDrawable(context));
    }

    private int getLayoutResourceId(int type) {
        return type == Action.TYPE_OUT ? R.layout.journal_order_out : R.layout.journal_order_in;
    }

    private String getOrderDescription(Order order) {
        String statusMessage = order.getHistory().get(0).comment;
        return statusMessage;
    }

    private void initOrders() {
        records = getOrders();
    }

    private LongSparseArray<Order> getOrders() {
        LongSparseArray<Order> journal = new LongSparseArray<>();
        List<Order> savedOrders = orderExecutor.getOrders();
        for (Order order : savedOrders) {
            if (null == contact || order.getContact().equals(contact)) {
                journal.put(order.getId(), order);
            }
        }
        return journal;
    }

    public void onEvent(OrderExecutorImpl.OrderStatusChangedEvent event) {
        initOrders();
        notifyDataSetChanged();
    }

    public void onEvent(OrderCreatedEvent event) {
        recentlyAddedOrderId = event.order.getId();
        initOrders();
        notifyDataSetChanged();
    }

    public void onEvent(OrderExecutor.OrdersChangedEvent event) {
        initOrders();
        notifyDataSetChanged();
    }

    private String getCurrentStatus(Order order) {

        if (order.getHistory().size() < 2)
            return null; //don't show current status if it's the same as initial

        Order.HistoryRecord lastStatus = order.getHistory().get(order.getHistory().size() - 1);
        String status = "(" + lastStatus.comment + ")";
        return status;
    }

}
