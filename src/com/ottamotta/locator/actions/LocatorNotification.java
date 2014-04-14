package com.ottamotta.locator.actions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.ottamotta.locator.R;
import com.ottamotta.locator.contacts.TrustedContact;

public class LocatorNotification {

    private String title;
    private String message;
    private TrustedContact contact;
    private Intent intent;
    private PendingIntent pendingIntent;
    private Context context;
    private boolean isSilent;

    public static LocatorNotification Builder(Context context) {
        LocatorNotification notification = new LocatorNotification();
        notification.context = context;
        return notification;
    }

    public LocatorNotification setContact(TrustedContact contact) {
        this.contact = contact;
        return this;
    }

    public LocatorNotification setTitle(String title) {
        this.title = title;
        return this;
    }

    public LocatorNotification setMessage(String message) {
        this.message = message;
        return this;
    }

    public LocatorNotification setPendingIntent(PendingIntent intent) {
        this.pendingIntent = intent;
        return this;
    }

    public LocatorNotification setIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

    public LocatorNotification setSilent(boolean silent) {
        this.isSilent = silent;
        return this;
    }

    public void buildAndNotify() {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = Integer.valueOf(contact.getId());
        notificationManager.notify(notificationId, build());
    }


    public Notification build() {

        PendingIntent resultPendingIntent;
        if (null == pendingIntent) {
            resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            Integer.valueOf(contact.getId()),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
        } else {
            resultPendingIntent = pendingIntent;
        }

        Bitmap contactPhoto = contact.getPhoto(context);

        Notification notification =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.default_icon)
                        .setTicker(title + ":" + message)
                        .setLargeIcon(contactPhoto)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(resultPendingIntent)
                        .setUsesChronometer(true)
                        .setAutoCancel(false)
                        .build();

        if (isSilent) {
            notification.sound = null;
            notification.vibrate = new long[] {0};
        } else {
            notification.sound = getSound();
            notification.defaults = Notification.DEFAULT_VIBRATE;
        }
        return notification;
    }

    private Uri getSound() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }


}
