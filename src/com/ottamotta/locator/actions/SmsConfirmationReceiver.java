package com.ottamotta.locator.actions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseArray;

import com.ottamotta.locator.R;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

/**
 */
public class SmsConfirmationReceiver extends BroadcastReceiver {

    public static final String SENT_SMS_ACTION = "com.ottamotta.locator.SENT_SMS_ACTION";
    public static final String DELIVERED_SMS_ACTION = "com.ottamotta.locator.DELIVERED_SMS_ACTION";
    public static final int SENT_SMS_RESULT_CODE = 10;
    public static final String LOCATOR_ACTION_EXTRA = "LOCATOR_ACTION_EXTRA";
    public static final String ORDER_ID_EXTRA = "OrderIdExtra";

    private static final String TAG = "Locator_OrderExecutor";
    public static final String RESULT_CODE_EXTRA = "resultCodeExtra";

    private static SparseArray<String> resultCodes;
    private static final Resources res = LocatorApplication.getInstance().getResources();

    static {
        resultCodes = new SparseArray<>();
        resultCodes.put(Activity.RESULT_OK, res.getString(R.string.status_sent_successfully)); //"Sent successfully");
        resultCodes.put(SmsManager.RESULT_ERROR_GENERIC_FAILURE, res.getString(R.string.status_not_sent_generic_failure));
        resultCodes.put(SmsManager.RESULT_ERROR_RADIO_OFF, res.getString(R.string.status_not_sent_radio_is_off));
        resultCodes.put(SmsManager.RESULT_ERROR_NULL_PDU, res.getString(R.string.status_not_sent_empty_message));
        resultCodes.put(SmsManager.RESULT_ERROR_NO_SERVICE, res.getString(R.string.sms_no_service));
        resultCodes.put(SENT_SMS_RESULT_CODE, res.getString(R.string.status_sms_sent));
    }

    public static final String SMS_DELIVERED_MESSAGE = res.getString(R.string.status_sms_delivered);

    @Inject
    private OrderExecutor orderExecutor;

    public static String getCommentForResultCode(int resultCode) {
        String comment = resultCodes.get(resultCode);
        if (null == comment) return resultCodes.get(SmsManager.RESULT_ERROR_GENERIC_FAILURE);
        return comment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocatorInjector.inject(this);
        Action action = intent.getParcelableExtra(LOCATOR_ACTION_EXTRA); //for debug
        //long orderId = intent.getLongExtra(ORDER_ID_EXTRA, 0);
        long orderId = action.getOrderId();
        action.setOrderId(orderId);

        Log.d(TAG, "SmsConfirmationReceived.onReceive(); orderId = " + orderId);

        if (intent.getAction().equals(SENT_SMS_ACTION)) {
            int resultCode = getResultCode();
            orderExecutor.updateOrderSmsSent(action, resultCode);
            return;
        }

        if (intent.getAction().equals(DELIVERED_SMS_ACTION)) {
            orderExecutor.updateOrderSmsDelivered(action);
        }

    }
}
