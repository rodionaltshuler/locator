package com.ottamotta.locator.actions;

import android.test.ApplicationTestCase;

import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.contacts.ContactsDaoImpl;
import com.ottamotta.locator.contacts.TrustedContact;

import java.util.Random;

public class ContactsDaoImplTest extends ApplicationTestCase<LocatorApplication> {

    private static final String TAG = "Locator:Test";
    public static final String MY_CONTACT_ID = "222"; //TODO set contact id of some contact on your device
    public static final String MY_PHONE_NUM_LOCAL = "0504478616"; //TODO set phone of this contact

    ContactsDaoImpl dao;

    public ContactsDaoImplTest() {
        super(LocatorApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = new ContactsDaoImpl(getContext());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddAndRestoreContact() throws Exception {
        TrustedContact originalContact = new TrustedContact();
        TrustedContact target = null;
        final String ID = "3765";
        originalContact.setId(ID);
        originalContact.setName("John Smith");
        originalContact.setMainPhoneNumber("03");
        try {
            dao.add(originalContact);
            target = dao.getContactById(ID);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertTrue("Saved and restored conatcs are not equal", originalContact.equals(target));
    }

    public void testGetContactByPhoneNum() throws Exception {
        assertTrue(dao.getContactByPhoneNum("0504478616").getName().contains("Аня"));
    }

    public void testGetContactById() throws Exception {
        assertTrue(dao.getContactById(MY_CONTACT_ID) != null);
    }

    public void testSaveContactByPhoneNum() throws Exception {
        TrustedContact saved = null;
        TrustedContact original = dao.getContactByPhoneNum("0667374927");
        String correctName = original.getName();
        String originalId = original.getId();
        saved = dao.getContactById(originalId);
        assertTrue(saved.getName().contains(correctName));
    }

    public void testGetContactByIdPhoneCorrect() throws Exception {
        final String correctNum = MY_PHONE_NUM_LOCAL;
        final String correctContactId = MY_CONTACT_ID;
        TrustedContact contact = dao.getContactById(correctContactId);
        assertTrue("Contact name = {" + contact.getName() + "} has phone number " + contact.getMainPhoneNumber() +
            " instead of " + correctNum,
            dao.getContactById(correctContactId).getMainPhoneNumber().contains(correctNum));
    }

    private TrustedContact createSampleContact() {
        Random r = new Random();
        String id = String.valueOf(r.nextInt(10000));
        String name = "Contact #" + id;
        String phoneNum = "+" + r.nextInt(1000000000);
        TrustedContact sampleContact = new TrustedContact();
        sampleContact.setId(id);
        sampleContact.setName(name);
        sampleContact.setMainPhoneNumber(phoneNum);
        return sampleContact;
    }

}
