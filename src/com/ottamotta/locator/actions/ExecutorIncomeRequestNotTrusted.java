package com.ottamotta.locator.actions;

import com.ottamotta.locator.R;

import java.util.Collections;
import java.util.List;

/**
 * Icnome
 * Request
 * from NOT trusted contact
 */
class ExecutorIncomeRequestNotTrusted extends BaseLocatorActionExecutor {

    @Override
    public void getMenuItems(final Action action, int status, List<LocatorMenuItem> menu) {
        switch (status) {
            case Order.STATUS_BEGIN:
                menu.add(new ActionMenu.LocatorMenuItemAddToTrusted(action));

                Runnable rejectRunnable = new Runnable() {
                    @Override
                    public void run() {
                        orderExecutor.deleteOrder(action.getOrderId());
                        //orderExecutor.rejectAddingToTrusted(action);
                    }
                };
                LocatorMenuItem rejectItem = new LocatorMenuItem(CONTEXT_MENU_ITEM_TITLES.get(MENU_ITEM_REJECT_ADDING_TO_TRUSTED), MENU_ITEM_REJECT_ADDING_TO_TRUSTED, rejectRunnable);
                menu.add(rejectItem);

                Runnable shareOnceRunnable = new Runnable() {
                    @Override
                    public void run() {
                        orderExecutor.shareOnce(action);
                    }
                };
                LocatorMenuItem shareOnceItem = new LocatorMenuItem(CONTEXT_MENU_ITEM_TITLES.get(MENU_ITEM_SHARE_LOCATION_TO_NON_TRUSTED_ONCE), MENU_ITEM_SHARE_LOCATION_TO_NON_TRUSTED_ONCE, shareOnceRunnable);
                menu.add(shareOnceItem);
                break;

            case Order.STATUS_SEARCHING_FOR_LOCATION:
                LocatorMenuItem cancelItem = new ActionMenu.LocatorMenuItemCancel(action);
                menu.add(cancelItem);
                break;
            case Order.STATUS_SMS_SENT:
            case Order.STATUS_SMS_DELIVERED:
            case Order.STATUS_CANCELED:
            case Order.STATUS_COMPLETED:
                break;
        }
        Collections.sort(menu);
    }

    @Override
    public String getInitialComment(Action startAction) {
        String comment = startAction.getContact().getName() + " " + context.getResources().getString(R.string.requested_your_location_not_family);
        return comment;
    }

    public ExecutorIncomeRequestNotTrusted(Action action) {
        super(action);
    }

    @Override
    public void doAction() {
        buildNotification(action);
    }

    @Override
    protected String getSmsText(final Action actionIn) {
        return null;
    }

    @Override
    protected void buildNotification(Action action) {

        LocatorNotification.Builder(context)
            .setContact(action.getContact())
            .setIntent(getOpenContactsIntent())
            .setTitle(context.getString(R.string.income_request))
            .setMessage(context.getString(R.string.add_contact_request, action.getContact().getName()))
            .buildAndNotify();
    }

}
