package com.ottamotta.locator.contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;

@Singleton
public class ContactsModelImpl implements ContactsModel {

    @Inject
    private EventBus mBus;

    @Inject
    private ContactsDao contactsDao;

    @Inject
    private Context context;

    public ContactsModelImpl() {}

    @Override
    public void addContact(TrustedContact contact) {
        contactsDao.add(contact);
        mBus.post(new ContactsChangedEvent(contactsDao.getContacts()));
    }

    @Override
    public void addContactTrusted(TrustedContact contact) {
        contact.setTrusted(true);
        addContact(contact);
    }

    @Override
    public void addNotTrusted(TrustedContact contact) {
        contactsDao.add(contact);
        mBus.post(new ContactsChangedEvent(contactsDao.getContacts()));
    }

    @Override
    public void removeContact(TrustedContact contact) {
        contactsDao.remove(contact);
        mBus.post(new ContactsChangedEvent(contactsDao.getContacts()));
    }

    @Override
    public void makeContactNotTrusted(TrustedContact contact) {
        contact.setTrusted(false);
        contactsDao.updateContact(contact);
        mBus.post(new ContactsChangedEvent(contactsDao.getContacts()));
    }

    @Override
    public void updateContacts() {
        Set<TrustedContact> contacts = getTrustedContacts();
        for (TrustedContact contact : contacts) {
            TrustedContact newContact = contactsDao.getContactByIdFromContentResolver(contact.getId());
            if (newContact != null) {
                //keep contact in app data if relevant contact has been deleted in device contacts
                boolean trusted = contact.isTrusted();
                contactsDao.remove(contact);
                newContact.setTrusted(trusted);
                contactsDao.add(newContact);
            }
        }
        mBus.post(new ContactsChangedEvent(contactsDao.getContacts()));
    }

    @Override
    public void addContactTrusted(Uri contactUri) {
        contactsDao.add(contactUri);
        mBus.post(new ContactsChangedEvent(contactsDao.getContacts()));
    }

    /**
     * Returns contact from list of trusted, if not in trusted list - from address book, NULL if not in present
     *
     * @param phoneNumber
     * @return
     */
    @Override
    public TrustedContact getContactByPhoneNumber(String phoneNumber) {
        return contactsDao.getContactByPhoneNum(phoneNumber);
    }

    @Override
    public Set<TrustedContact> getTrustedContacts() {
        List<TrustedContact> contacts = contactsDao.getContacts();
        return new HashSet<>(contacts);
    }

    @Override
    public TrustedContact getRequestedContact(String phoneNum) {
        return contactsDao.getContactByPhoneNum(phoneNum);
    }

    @Override
    @Deprecated
    public Bitmap getPhoto(TrustedContact contact) {
        try {
            InputStream is = context.getContentResolver().openInputStream(contact.getPhotoUri());
            Bitmap result = BitmapFactory.decodeStream(is);
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                return contact.getPhotoEmptyBitmap(context);
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return contact.getPhotoEmptyBitmap(context);
    }

    /**
     *
     * @param contactId
     * @return contact or NULL if contact does not exists (deleted by device user)
     */
    @Override
    public TrustedContact getContactById(String contactId) {
        return contactsDao.getContactById(contactId);
    }

}
