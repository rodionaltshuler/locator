package com.ottamotta.locator.actions;

import com.ottamotta.locator.R;

import java.util.Collections;
import java.util.List;

/**
 * Icnome
 * Request
 * from trusted contact
 */
class ExecutorIncomeRequestTrusted extends BaseLocatorActionExecutor {

    public ExecutorIncomeRequestTrusted(Action action) {
        super(action);
    }

    @Override
    public void doAction() {
        final Action out = action.reply();
        orderExecutor.create(out);
        out.getExecutor().doAction();
    }

    @Override
    protected String getSmsText(final Action actionIn) {

        return null;

    }

    @Override
    protected void buildNotification(Action actionOut) {

        // do nothing

    }

    @Override
    public void getMenuItems(Action action, int status, List<LocatorMenuItem> menu) {
        super.getMenuItems(action, status, menu);
        switch (status) {
            case Order.STATUS_SEARCHING_FOR_LOCATION:
                menu.add(new ActionMenu.LocatorMenuItemCancel(action));
                break;
            case Order.STATUS_SMS_SENT:
            case Order.STATUS_SMS_DELIVERED:
            case Order.STATUS_SMS_FAILED_TO_SEND:
            case Order.STATUS_CANCELED:
                break;
            case Order.STATUS_COMPLETED:
                menu.add(new ActionMenu.LocatorMenuItemShowOnMap(action));
        }
        Collections.sort(menu);
    }

    @Override
    public String getInitialComment(Action startAction) {
        String comment = startAction.getContact().getName() + " " + context.getResources().getString(R.string.requested_your_location);
        return comment;

    }

}
