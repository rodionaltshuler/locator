package com.ottamotta.locator.contacts;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ottamotta.locator.R;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.utils.LocatorGson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ContactsDaoImpl implements ContactsDao {

    public static final String CONTACTS_PREFS_FILE = "LocatorContacts";

    private static int SIGNIFICANT_PHONE_NUM_DIGITS = 10;
    private Gson gson = LocatorGson.getGson();

    private Map<String,TrustedContact> trustedContacts;
    private Map<String,TrustedContact> phoneNumbers;

    private Context context;

    @Inject
    public ContactsDaoImpl(Context context) {
        this.context = context;
    }

    @Override
    public String add(TrustedContact contact) {
        save(contact);
        return contact.getId();
    }

    @Override
    public String addNotTrusted(TrustedContact contact) {
        save(contact);
        return contact.getId();
    }

    @Override
    public void add(long contactId) {
        TrustedContact contact = getContactById(String.valueOf(contactId));
        save(contact);
    }

    @Override
    public void add(String phoneNumber) {
        TrustedContact contact = getContactByPhoneNum(phoneNumber);
        save(contact);
    }

    @Override
    public void add(Uri contactUri) {
        TrustedContact contact = getContactByUri(contactUri);
        if (contact.getPhoneNumbers().size() > 0) {
            contact.setTrusted(true);
            save(contact);
        }
        else {
            Toast.makeText(context, contact.getName() + " " + context.getResources().getString(R.string.has_no_phone_numbers), Toast.LENGTH_LONG).show();
        }
    }

    private void save(TrustedContact contact) {
        saveToL1Cache(contact);
        saveToL2Cache(contact);
    }

    @Override
    public void updateContact(TrustedContact contact) {
        save(contact);
    }

    @Override
    public List<TrustedContact> getContacts() {
        List<TrustedContact> trustedContacts = new ArrayList<>();
        trustedContacts.addAll(getTrustedContactsMap().values());
        return trustedContacts;
    }

    @Override
    public TrustedContact getContactById(long contactId) {
        return getContactById(String.valueOf(contactId));
    }

    @Override
    public TrustedContact getContactById(String contactId) {
        Map<String,TrustedContact> trustedContacts = getTrustedContactsMap();
        if (trustedContacts.containsKey(contactId)) {
            return trustedContacts.get(contactId);
        }
        else {
            TrustedContact contact = getContactByIdFromContentResolver(contactId);
            return contact;
        }
    }

    @Override
    public TrustedContact getContactByIdFromContentResolver(String contactId) {
        Uri contactUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId));
        return getContactByUri(contactUri);
    }

    private TrustedContact getContactByUri(Uri uri) {
        Cursor contactCursor = context.getContentResolver().query(uri, null, null, null, null);
        if (contactCursor.moveToFirst()) {
            String name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            Uri photoUri = getPhotoUri(uri);
            //String phoneNumber = getPhoneNumFromContentResolver(contactId);
            PhoneNumbers phoneNumbers = getPhoneNumbersFromContentResolver(contactId);
            TrustedContact contact = new TrustedContact(contactId, name, phoneNumbers.phoneNumbers, phoneNumbers.mainPhoneNumber, photoUri);
            contact.setTrusted(false);
            return contact;
        }
        return null;
    }

    @Override
    public TrustedContact getContactByPhoneNum(String phoneNum) {
        //FIXME possible error - trusted contacts is untrusted
        String phoneNumToSearch = getPhoneNumberToSearch(phoneNum);
        if (getPhoneNumbersMap().containsKey(phoneNumToSearch)) {
            return getPhoneNumbersMap().get(phoneNumToSearch);
        }
        else {
            TrustedContact contact = getNotTrustedContactByPhoneNum(phoneNum);
            return contact;
        }
    }

    private synchronized Map<String,TrustedContact> getPhoneNumbersMap() {
        if (null == phoneNumbers) {
            loadTrustedContactsFromL2Cache();
        }
        return phoneNumbers;
    }
    @Override
    public void remove(TrustedContact contact) {
        contact.setTrusted(false);
        getTrustedContactsMap().remove(contact.getId());
        for (String phoneNum : contact.getPhoneNumbers()) {
            getPhoneNumbersMap().remove(getPhoneNumberToSearch(phoneNum));
        }

        SharedPreferences.Editor editor = getPrefs().edit();
        editor.remove(contact.getId());
        editor.commit();
    }

    @Override
    public void remove(long contactId) {
        TrustedContact contact = getTrustedContactsMap().get(String.valueOf(contactId));
        if (contact != null)
            remove(contact);
    }

    private synchronized Map<String,TrustedContact> getTrustedContactsMap() {
        if (null == trustedContacts) {
            loadTrustedContactsFromL2Cache();
        }
        return trustedContacts;
    }

    private String getPhoneNumFromContentResolver(String contactId) {
        Cursor phonesForContact = context.getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
            new String[] { contactId }, null);

        String phoneNumber = null;
        while (phonesForContact.moveToNext()) {
            phoneNumber = phonesForContact.getString(phonesForContact.getColumnIndex(ContactsContract.Data.DATA1));
        }

        phonesForContact.close();

        return phoneNumber;
    }

    private PhoneNumbers getPhoneNumbersFromContentResolver(String contactId) {
        PhoneNumbers result = new PhoneNumbers();
        Cursor phonesForContact = context.getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
            new String[] { contactId }, null);

        Set<String> phones = new HashSet<>();
        while (phonesForContact.moveToNext()) {
            String nextPhone = withRemovedWhitespaces(phonesForContact.getString(phonesForContact.getColumnIndex(ContactsContract.Data.DATA1)));
            phones.add(nextPhone);
            if (null == result.mainPhoneNumber) {
                result.mainPhoneNumber = nextPhone;
            }
            else if (phonesForContact.getInt(phonesForContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) == 1) {
                result.mainPhoneNumber = nextPhone;
            }
        }
        phonesForContact.close();
        result.phoneNumbers = phones;
        return result;
    }

    private TrustedContact getNotTrustedContactByPhoneNum(String phoneNum) {
        String phoneNumToSearch = getPhoneNumberToSearch(phoneNum);

        Uri personUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumToSearch);

        Cursor cur = context.getContentResolver().query(personUri,
            null,
            null, null, null);

        if (cur.moveToFirst()) {
            String contactId = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            TrustedContact contact = new TrustedContact(contactId);
            contact.setName(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)));
            contact.setPhotoUri(getPhotoUri(contact.getId()));
            contact.setMainPhoneNumber(phoneNum);
            contact.setTrusted(false);
            return contact;
        }
        return getUnknownContact(phoneNum);
    }

    private Uri insertContact(String phone, String name) {

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.DISPLAY_NAME, name);

        Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        long contactId = getContactId(context, rawContactId);
        System.out.println("rawContactId = " + rawContactId);
        System.out.println("contactId = " + contactId);

        values.clear();
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        values.clear();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.Data.CONTENT_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        values.clear();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        Uri result = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        return result;
    }

    private long getContactId(Context context, long rawContactId) {
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[] { ContactsContract.RawContacts.CONTACT_ID }, ContactsContract.RawContacts._ID + "=" + rawContactId, null, null);
            if (cur.moveToFirst()) {
                return cur.getLong(cur.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cur != null) {
                cur.close();
            }
        }
        return -1l;
    }

    private TrustedContact getUnknownContact(String phoneNum) {
        //Uri contactCreatedUri =
        //trustedContact = contactCreatedUri == null ? null : getContactByUri(contactCreatedUri);
        //trustedContact.setTrusted(false);
        insertContact(phoneNum, context.getString(R.string.new_contact_name));
        TrustedContact trustedContact;
        trustedContact = getContactByPhoneNum(phoneNum);
        return trustedContact;
    }

    private Uri getPhotoUri(String contactId) {
        Uri contactUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId));
        Uri result = getPhotoUri(contactUri);
        return result;
    }

    private Uri getPhotoUri(Uri contactUri) {
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        return photoUri;
    }

    private Bitmap getPhoto(TrustedContact contact) {
        try {
            InputStream is = context.getContentResolver().openInputStream(contact.getPhotoUri());
            Bitmap result = BitmapFactory.decodeStream(is);
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadTrustedContactsFromL2Cache() {
        trustedContacts = getEmptyTrustedContactsMap();
        phoneNumbers = getEmptyPhoneNumbersMap();
        Map<String,String> contactJsons = (Map<String,String>) getPrefs().getAll();
        for (Map.Entry<String,String> contact : contactJsons.entrySet()) {
            Log.d(LocatorApplication.TAG, "processing contact:\n" + contact);
            try {
                TrustedContact next = getContactFromString(contact.getValue());
                saveToL1Cache(next);
            }
            catch (Exception e) {
                Log.d(LocatorApplication.TAG, "error processing contact, removing:\n" + contact.getKey());
                SharedPreferences.Editor editor = getPrefs().edit();
                editor.remove(contact.getKey());
                editor.commit();
            }
        }
    }

    private void saveToL1Cache(TrustedContact next) {
        getTrustedContactsMap().put(next.getId(), next);
        for (String number : next.getPhoneNumbers()) {
            phoneNumbers.put(getPhoneNumberToSearch(number), next);
            Log.d(LocatorApplication.TAG, "Putting phone number: " + getPhoneNumberToSearch(number) + "," + next.getName());
        }

    }

    private void saveToL2Cache(TrustedContact contact) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(contact.getId(), gson.toJson(contact));
        editor.commit();
    }

    private TrustedContact getContactFromString(String contactJson) {
        TrustedContact contact = gson.fromJson(contactJson, TrustedContact.class);
        return contact;
    }

    private SharedPreferences getPrefs() {
        return context.getSharedPreferences(CONTACTS_PREFS_FILE, Context.MODE_PRIVATE);
    }

    private Map<String,TrustedContact> getEmptyTrustedContactsMap() {
        return new HashMap<>();
    }

    private Map<String,TrustedContact> getEmptyPhoneNumbersMap() {
        return new HashMap<>();
    }

    String getPhoneNumberToSearch(String originalPhoneNum) {
        if (originalPhoneNum == null || originalPhoneNum.length() == 0) {
            return null;
        }
        originalPhoneNum = withRemovedWhitespaces(originalPhoneNum);

        int lastIndex = originalPhoneNum.length();
        int firstIndex = lastIndex - SIGNIFICANT_PHONE_NUM_DIGITS;
        if (firstIndex < 0)
            firstIndex = 0;

        String phoneNumToSearch = originalPhoneNum.substring(firstIndex, lastIndex);

        return phoneNumToSearch;
    }

    private String withRemovedWhitespaces(String source) {
        return source.replace(" ", "");
    }

    private static class PhoneNumbers {
        String mainPhoneNumber;
        Set<String> phoneNumbers;
    }
}
