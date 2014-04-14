package com.ottamotta.locator.contacts;

import android.database.ContentObserver;
import android.os.Handler;

import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

public class ContactsChangeObserver extends ContentObserver {

    @Inject
    ContactsModel contactsModel;

    public ContactsChangeObserver(Handler handler) {
        super(handler);
        LocatorInjector.inject(this);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        contactsModel.updateContacts();
    }


}
