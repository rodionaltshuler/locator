package com.ottamotta.locator.contacts;

import android.net.Uri;

import java.util.List;

public interface ContactsDao {

    public String add(TrustedContact contact);
    public String addNotTrusted(TrustedContact contact);
    public void add(long contactId);
    public void add(String phoneNumber);
    public void add(Uri contactUri);

    public List<TrustedContact> getContacts();
    public TrustedContact getContactById(long contactId);
    public TrustedContact getContactById(String contactId);
    public TrustedContact getContactByIdFromContentResolver(String contactId);
    public TrustedContact getContactByPhoneNum(String phoneNumber);

    public void remove(TrustedContact contact);
    public void remove(long contactId);

    void updateContact(TrustedContact contact);
}
