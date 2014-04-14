package com.ottamotta.locator.actions;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.ottamotta.locator.R;
import com.ottamotta.locator.application.LocatorApplication;

public class SmsSender {

    public static final String TAG = "Locator::OrderExecutor";
    private static SmsSender instance;
    private SendSmsOneShotListener mListener;

    private final Context context = LocatorApplication.getInstance();
    private final SmsManager sms = SmsManager.getDefault();

    public interface SendSmsOneShotListener {
        public boolean onSendSms(Action action, String smsBody);
    }

    public static synchronized SmsSender getInstance() {
        if (null == instance) {
            instance = new SmsSender();
        }
        return instance;
    }

    public void retry(Action action) {
        action.doAction();
    }

    public void setListenerForOneSend(SendSmsOneShotListener listener) {
        mListener = listener;
    }

    public void sendSms(Action locatorAction, String smsMessage) {

        Log.d(TAG, "Sending SMS: " + smsMessage);

        if (null != mListener) {
            boolean continueSending = mListener.onSendSms(locatorAction, smsMessage);
            mListener = null;
            if (!continueSending) return;
        }

        Intent sentIntent = new Intent(SmsConfirmationReceiver.SENT_SMS_ACTION);
        sentIntent.putExtra(SmsConfirmationReceiver.LOCATOR_ACTION_EXTRA, locatorAction);
        Log.d(TAG, "Creating sentIntent for orderId = " + locatorAction.getOrderId());

        PendingIntent sentPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        0,
                        sentIntent,
                        PendingIntent.FLAG_ONE_SHOT
                );


        Intent deliveryIntent = new Intent(SmsConfirmationReceiver.DELIVERED_SMS_ACTION);
        deliveryIntent.putExtra(SmsConfirmationReceiver.LOCATOR_ACTION_EXTRA, locatorAction);
        Log.d(TAG, "Creating sentIntent for orderId = " + locatorAction.getOrderId());

        PendingIntent deliveryPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        0,
                        deliveryIntent,
                        PendingIntent.FLAG_ONE_SHOT
                );

        String phoneNumber = locatorAction.getPhoneNumToReply();
        short port = Short.decode(context.getString(R.string.sms_port));
        Log.d("SMS:" + LocatorApplication.TAG, "Sending sms: " + smsMessage);
        sms.sendDataMessage(phoneNumber, null, port, smsMessage.getBytes(), sentPendingIntent, deliveryPendingIntent);
        //sms.sendTextMessage(phoneNumber, null, smsMessage, sentPendingIntent, deliveryPendingIntent);

    }
}
