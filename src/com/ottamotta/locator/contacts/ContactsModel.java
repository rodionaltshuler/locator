package com.ottamotta.locator.contacts;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public interface ContactsModel {
    String CONTACTS_KEY = "Contacts";

    public void addContact(TrustedContact contact);

    public void addContactTrusted(TrustedContact contact);

    public void addNotTrusted (TrustedContact contact);

    public void removeContact(TrustedContact contact);

    public void makeContactNotTrusted(TrustedContact contact);

    public void addContactTrusted(Uri contactUri);

    public TrustedContact getContactByPhoneNumber(String phoneNumber);

    public Set<TrustedContact> getTrustedContacts();

    public TrustedContact getRequestedContact(String phoneNum);

    @Deprecated
    public Bitmap getPhoto(TrustedContact contact);

    public TrustedContact getContactById(String contactId);

    void updateContacts();

    public static class ContactsChangedEvent {
        public Set<TrustedContact> mContacts;

        public ContactsChangedEvent(Collection<TrustedContact> contacts) {
            mContacts = new TreeSet<>();
            mContacts.addAll(contacts);
        }
    }

    public static class ContactsLoadedEvent {
        public Set<TrustedContact> trustedContacts;
        public ContactsLoadedEvent(Set<TrustedContact> trustedContacts) {
            this.trustedContacts = trustedContacts;
        }
    }
}
