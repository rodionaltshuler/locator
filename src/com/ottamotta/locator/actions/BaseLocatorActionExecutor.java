package com.ottamotta.locator.actions;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.SparseArray;

import com.ottamotta.locator.R;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.compass.CompassFusionActivity;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.ui.LocatorContactsActivity;
import com.ottamotta.locator.manualLocation.ManualLocationActivity;
import com.ottamotta.locator.manualLocation.ManualLocationListener;
import com.ottamotta.locator.map.MapFragmentActivity;
import com.ottamotta.locator.roboguice.LocatorInjector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Base class for handling income & outcome requests
 */
public abstract class BaseLocatorActionExecutor {

    public static final String LOCATION_REQUEST = "wru?";

    public static final String EXTRA_CONTACT = "extra_contact";

    public static final String PARTS_DELIMETER = ";";
    protected static final int LOCATION_REQUEST_PART = 0;

    protected static final int COORDS_PART = 1;
    protected static final int ADDRESS_PART = 2;
    protected static final int ALTITUDE_PART = 3;
    protected static final int ACTIVITY_PART = 4;
    protected static final int TIME_BETWEEN_LOCATIONS_PART = 5;
    //action statuses common for different executors

    protected static final int SMS_PARTS = 6;

    protected static final String TAG = "Locator::ActionExecutor";

    protected static final int MENU_ITEM_SHOW_ON_MAP = 1;
    protected static final int MENU_ITEM_NAVIGATE_WITH_COMPASS = 2;
    protected static final int MENU_ITEM_ADD_TO_TRUSTED = 3;
    protected static final int MENU_ITEM_REJECT_ADDING_TO_TRUSTED = 4;
    protected static final int MENU_ITEM_SHARE_LOCATION_TO_NON_TRUSTED_ONCE = 5;
    protected static final int MENU_ITEM_RETRY_SEND_SMS = 6;
    protected static final int MENU_ITEM_DELETE = 7;
    protected static final int MENU_ITEM_CANCEL = 98;

    protected static final SparseArray<String> CONTEXT_MENU_ITEM_TITLES = new SparseArray<>();

    static {
        Resources res = LocatorApplication.getInstance().getResources();
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_SHOW_ON_MAP, res.getString(R.string.menu_item_show_on_map));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_DELETE, res.getString(R.string.menu_item_delete));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_ADD_TO_TRUSTED, res.getString(R.string.menu_item_add_to_trusted));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_REJECT_ADDING_TO_TRUSTED, res.getString(R.string.menu_item_reject_adding_to_trusted));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_SHARE_LOCATION_TO_NON_TRUSTED_ONCE, res.getString(R.string.menu_item_share_location_to_non_trusted_once));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_RETRY_SEND_SMS, res.getString(R.string.menu_item_retry_send_sms));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_CANCEL, res.getString(R.string.menu_item_cancel));
        CONTEXT_MENU_ITEM_TITLES.put(MENU_ITEM_NAVIGATE_WITH_COMPASS, res.getString(R.string.navigate_with_compass));
    }

    protected static final String EMPTY_PART = ";";
    @Inject
    protected Context context; // = LocatorApplication.getInstance();
    @Inject
    protected ContactsModel contactsModel;
    @Inject
    protected OrderExecutor orderExecutor;
    protected Action action;
    @Inject
    protected EventBus mBus; // = EventBus.getDefault();
    private Intent showMapIntent;
    @Inject
    private ManualLocationListener manualLocationListener;

    public BaseLocatorActionExecutor(Action action) {
        this.action = action;
        LocatorInjector.inject(this);
    }

    public abstract void doAction();

    protected abstract String getSmsText(final Action action);

    protected abstract void buildNotification(final Action action);

    protected Intent getOpenContactsIntent() {
        Intent intent = new Intent(context, LocatorContactsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_CONTACT, action.getContact());
        return intent;
    }

    protected void showNotificationAutoAnswerOff() {

        LocatorNotification.Builder(context)
            .setContact(action.getContact())
            .setTitle(context.getResources().getString(R.string.ignored_request))
            .setMessage(context.getResources().getString(R.string.from) + " " + action.getContact().getName())
            .setIntent(getOpenContactsIntent())
            .buildAndNotify();
    }

    public void getMenuItems(final Action action, int status, List<LocatorMenuItem> menu) {
        if (null == menu) {
            menu = new ArrayList<>();
        }
        LocatorMenuItem itemDelete;
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                orderExecutor.deleteOrder(action.getOrderId());
            }
        };
        itemDelete = new LocatorMenuItem(CONTEXT_MENU_ITEM_TITLES.get(MENU_ITEM_DELETE), MENU_ITEM_DELETE, deleteRunnable);
        itemDelete.setImageResourceId(R.drawable.ic_delete);
        menu.add(itemDelete);

        if (Order.STATUS_SMS_FAILED_TO_SEND == status) {
            Runnable retryRunnabe = new Runnable() {
                @Override
                public void run() {
                    orderExecutor.retry(action);
                }
            };
            LocatorMenuItem itemRetry = new LocatorMenuItem(CONTEXT_MENU_ITEM_TITLES.get(MENU_ITEM_RETRY_SEND_SMS), MENU_ITEM_RETRY_SEND_SMS, retryRunnabe);
            menu.add(itemRetry);
        }
        Collections.sort(menu);
    }

    public abstract String getInitialComment(Action startAction);

    private boolean isSilentNotification (int newStatus) {
        return newStatus != Order.STATUS_FAILED_TO_FIND_LOCATION;
    }

    public void showNotificationStatusChanged(int newStatus, String comment) {

        String title = action.getContact().getName() + ": " + getInitialComment(action);

        LocatorNotification.Builder(context)
                .setContact(action.getContact())
                .setTitle(title)
                .setMessage(comment)
                //.setMessage(Order.STATUS_COMMENTS.get(newStatus))
                .setIntent(getIntentForStatus(newStatus))
                .setSilent(isSilentNotification(newStatus))
                .buildAndNotify();
    }

    protected Intent getIntentForStatus(int newStatus) {
        Intent intent;
        switch (newStatus) {
            case Order.STATUS_FAILED_TO_FIND_LOCATION:
                intent = getLocationManuallyIntent();
                break;
            default:
                intent = getOpenContactsIntent();
        }
        return intent;
    }

    private Intent getLocationManuallyIntent() {
        //TODO code duplication with ManualLocationListener
        Intent intent = new Intent(context, ManualLocationActivity.class);
        intent.putExtra(Action.EXTRA_ACTION, action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public void showOnMap() {
        if (action.getLocation() != null & action.getContact() != null) {
            context.startActivity(getMapIntent(true));
        }
    }

    public void showCompass() {
        if (action.getLocation() != null && action.getContact() != null) {
            context.startActivity(getMapIntent(false));
        }
    }

    protected Intent getMapIntent() {
        return getMapIntent(true);
    }

    protected Intent getMapIntent(boolean map) {
        //TODO refactor: enum instead of boolean
        Class activityClass = map ? MapFragmentActivity.class : CompassFusionActivity.class;
        if (showMapIntent == null) {
            showMapIntent = new Intent(context, activityClass);
            showMapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showMapIntent.putExtra(Action.EXTRA_ACTION, action);
        }
        return showMapIntent;
    }
}
