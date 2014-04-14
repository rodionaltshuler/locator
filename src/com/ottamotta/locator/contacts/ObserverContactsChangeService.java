package com.ottamotta.locator.contacts;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;

import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

public class ObserverContactsChangeService extends Service {

    private Handler handler = new Handler();
    private ContactsChangeObserver observer;

    @Inject
    private ContactsModel contactsModel;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LocatorInjector.inject(this);
        observeChange();
    }

    private void observeChange() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        ContactsChangeObserver observer = new ContactsChangeObserver(handler);
        getApplicationContext().getContentResolver().registerContentObserver(uri, true, observer);
    }
}
