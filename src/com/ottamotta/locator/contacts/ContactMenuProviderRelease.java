package com.ottamotta.locator.contacts;

import android.content.Context;
import android.content.res.Resources;

import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.LocatorMenuItem;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.roboguice.LocatorInjector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ContactMenuProviderRelease implements ContactMenuProvider {

    @Inject
    protected Context context;

    @Override
    public List<LocatorMenuItem> getMenuItems(TrustedContact contact) {

        List<LocatorMenuItem> result = new ArrayList<>();
        Resources res = context.getResources();

        LocatorMenuItem requestLocationItem = new LocatorMenuItem(
                res.getString(R.string.request_location),
                0,
                new RequestLocationRunnable(contact)
        );
        requestLocationItem.setImageResourceId(R.drawable.ic_make_request);
        result.add(requestLocationItem);

        LocatorMenuItem shareLocationItem = new LocatorMenuItem(
                res.getString(R.string.share_location),
                1,
                new ShareLocationRunnable(contact)
        );
        shareLocationItem.setImageResourceId(R.drawable.ic_share_location);
        result.add(shareLocationItem);

        if (contact.isTrusted()) {
            LocatorMenuItem removeItem = new LocatorMenuItem(
                    res.getString(R.string.remove_from_trusted),
                    2,
                    new RemoveContactRunnable(contact)
            );
            result.add(removeItem);

/*            LocatorMenuItem makeNotTrustedItem = new LocatorMenuItem(
                    "Make not trusted",
                    3,
                    new MakeNotTrustedRunnable(contact)
            );
            result.add(makeNotTrustedItem);*/

        } else {
            LocatorMenuItem addToTrustedItem = new LocatorMenuItem(
                    res.getString(R.string.menu_item_add_to_trusted),
                    2,
                    new AddToTrustedRunnable(contact)
            );
            result.add(addToTrustedItem);
        }
        Collections.sort(result);
        return result;
    }

    protected static class RemoveContactRunnable implements Runnable {

        @Inject
        protected ContactsModel contactsModel;
        protected TrustedContact contact;

        public RemoveContactRunnable(TrustedContact contact) {
            this.contact = contact;
            LocatorInjector.inject(this);
        }

        @Override
        public void run() {
            contactsModel.removeContact(contact);
        }
    }

    protected static class MakeNotTrustedRunnable extends RemoveContactRunnable {

        public MakeNotTrustedRunnable(TrustedContact contact) {
            super(contact);
        }

        @Override
        public void run() {
            contactsModel.makeContactNotTrusted(contact);
        }
    }

    protected static class AddToTrustedRunnable extends RemoveContactRunnable {

        public AddToTrustedRunnable(TrustedContact contact) {
            super(contact);
        }

        @Override
        public void run() {
            contact.setTrusted(true);
            contactsModel.addContact(contact);
        }
    }

    protected static class RequestLocationRunnable implements Runnable {

        @Inject
        protected OrderExecutor orderExecutor;

        protected TrustedContact contact;

        public RequestLocationRunnable(TrustedContact contact) {
            this.contact = contact;
            LocatorInjector.inject(this);
        }

        @Override
        public void run() {
            orderExecutor.requestLocation(contact);
        }
    }

    protected static class ShareLocationRunnable extends RequestLocationRunnable {

        public ShareLocationRunnable(TrustedContact contact) {
            super(contact);
        }

        @Override
        public void run() {
            orderExecutor.shareLocation(contact);
        }
    }


}
