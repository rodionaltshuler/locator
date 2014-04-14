package com.ottamotta.locator.actions;

import android.util.Log;

import com.ottamotta.locator.R;

import java.util.List;

/**
 * Outcome
 * Request
 */
class ExecutorOutcomeRequest extends BaseLocatorActionExecutor {

    private SmsSender mSmsSender;

    public ExecutorOutcomeRequest(Action action) {
        super(action);
        mSmsSender = SmsSender.getInstance();
    }

    @Override
    public void doAction() {

        //send sms with request
       /* Order o = orderExecutor.getOrder(action.getOrderId());
        orderExecutor.setOrderStatus(o, Order.STATUS_BEGIN,
                Order.STATUS_COMMENTS.get(Order.STATUS_BEGIN),
                System.currentTimeMillis());*/

        Log.d(TAG, "doAction() for outcome request");
        mSmsSender.sendSms(action, getSmsText(action));
        //sendSMS(action.getContactById().getMainPhoneNumber(), getSmsText(action));

    }


    @Override
    public String getSmsText(final Action actionIn) {

        StringBuilder sms = new StringBuilder();

        //no request we will send in response?
        sms.append(LOCATION_REQUEST);
        sms.append(EMPTY_PART);

        sms.append(EMPTY_PART);

        sms.append(EMPTY_PART);

        //altitude
        sms.append(EMPTY_PART);

        //activity
        sms.append(EMPTY_PART);

        if (actionIn.getTimeoutBetweenLocations() != 0){
            sms.append(actionIn.getTimeoutBetweenLocations());
        }
        Log.d(TAG, "Sending SMS: " + sms.toString());

        return sms.toString();

    }

    @Override
    protected void buildNotification(Action actionOut) {
        //do nothing
    }

    @Override
    public void getMenuItems(Action action, int status, List<LocatorMenuItem> menu) {
        super.getMenuItems(action, status, menu);
    }

    @Override
    public String getInitialComment(Action startAction) {
        String comment = context.getResources().getString(R.string.you_requested_location, startAction.getContact().getName());
        return comment;
    }


}
