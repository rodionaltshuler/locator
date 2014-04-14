package com.ottamotta.locator.actions;

import com.ottamotta.locator.R;

import java.util.Collections;
import java.util.List;

/**
 * Income
 * Share
 */
class ExecutorIncomeShare extends BaseLocatorActionExecutor {

    public ExecutorIncomeShare(Action action) {
        super(action);
    }

    @Override
    public void doAction() {
        buildNotification(action);
        showOnMap();
    }

    @Override
    protected String getSmsText(final Action actionIn) {
        return null;
    }

    @Override
    protected void buildNotification(Action actionOut) {
        LocatorNotification.Builder(context)
            .setContact(actionOut.getContact())
            .setIntent(getMapIntent())
            .setTitle(context.getString(R.string.location_shared))
            .setMessage(context.getString(R.string.location_shared_from_when, action.getContact().getName(), action.getUpdateTimeFormatted()))
            .buildAndNotify();
    }

    @Override
    public void getMenuItems(Action action, int status, List<LocatorMenuItem> menu) {
        super.getMenuItems(action, status, menu);
        switch (status) {
            case Order.STATUS_BEGIN:
            case Order.STATUS_COMPLETED:
                menu.add(new ActionMenu.LocatorMenuItemShowOnMap(action));
                menu.add(new ActionMenu.NavigateWithCompass(action));
                if (!action.getContact().isTrusted()) {
                    menu.add(new ActionMenu.LocatorMenuItemAddToTrusted(action));
                }
        }
        Collections.sort(menu);
    }

    @Override
    public String getInitialComment(Action startAction) {
        String comment = startAction.getContact().getName() + " " + context.getResources().getString(R.string.shared_location_with_you);
        return comment;

    }
}
