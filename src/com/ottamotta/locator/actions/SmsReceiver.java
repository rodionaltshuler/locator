package com.ottamotta.locator.actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

/**
 * Reads SMS and decides does it contain coords or not
 */

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Inject
    OrderExecutor orderExecutor;

    @Override
    public void onReceive(Context context, Intent intent) {

        LocatorInjector.inject(this);
        //intent.getAction() == Activity.RESULT_OK
        Log.d(LocatorApplication.TAG, "onReceive() sms");

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    if (currentMessage != null) {
                        SmsParsedRequest request = SmsParsedRequest.fromSms(currentMessage);
                        if (request.isOurMessage()) {
                            orderExecutor.createOrderFromIncomeSms(currentMessage);
                        }
                    }

                }
            }

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }

    }
}
