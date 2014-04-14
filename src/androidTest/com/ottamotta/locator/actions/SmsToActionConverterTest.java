package com.ottamotta.locator.actions;

import android.content.Context;
import android.test.ApplicationTestCase;

import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

public class SmsToActionConverterTest extends ApplicationTestCase<LocatorApplication> {

    private static final String TRUSTED_CONTACT_SAMPLE_PHONENUMBER_LOCAL = "0504478616";

    private static final String TRUSTED_CONTACT_SAMPLE_PHONENUMBER_INTERNATIONAL = "+38" +
            TRUSTED_CONTACT_SAMPLE_PHONENUMBER_LOCAL;

    private Context mContext;
    private SmsToActionConverter mSmsToActionConverter;

    @Inject
    private ContactsModel contactsModel;

    public SmsToActionConverterTest() {
        super(LocatorApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //TODO add demo contacts?
        mContext = getContext();
        LocatorInjector.inject(this);
        mSmsToActionConverter = SmsToActionConverter.getInstance();
    }

    public void testIsAllowedNumber_TrustedContactWithSinglePhoneNumber_localFormat() throws Exception {
        String trustedPhoneNumber = TRUSTED_CONTACT_SAMPLE_PHONENUMBER_LOCAL;
        TrustedContact contact = contactsModel.getContactByPhoneNumber(trustedPhoneNumber);
        assertTrue("Phone number " + trustedPhoneNumber + " belongs to trusted contact", contact.isTrusted());
    }

    public void testIsAllowedNumber_TrustedContactWithSinglePhoneNumber_InternationalNumberFormat() throws Exception {
        String trustedPhoneNumber = TRUSTED_CONTACT_SAMPLE_PHONENUMBER_INTERNATIONAL;
        TrustedContact contact = contactsModel.getContactByPhoneNumber(trustedPhoneNumber);
        assertTrue("Phone number " + trustedPhoneNumber + " belongs to trusted contact", contact.isTrusted());
    }


    public void testIsAllowedNumber_NonTrustedContactWithSinglePhoneNumber() throws Exception {
        SmsToActionConverter sms = SmsToActionConverter.getInstance();
        String notTrustedPhoneNumber = "0505588313";
        TrustedContact contact = contactsModel.getContactByPhoneNumber(notTrustedPhoneNumber);
        assertTrue("Phone number " + notTrustedPhoneNumber + " belongs to non-trusted contact", !contact.isTrusted());
    }

    public void testIsAllowedNumber_TrustedContactWithMultiplyPhoneNumbers() throws Exception {
        SmsToActionConverter sms = SmsToActionConverter.getInstance();
        String[] phoneNumbers = new String[] {"0956904484", "0990288627"};
        boolean allTrusted = true;
        for (String number : phoneNumbers) {
            TrustedContact contact = contactsModel.getContactByPhoneNumber(number);
            if (!contact.isTrusted()){
                allTrusted = false;
                break;
            }
        }
        assertTrue("Failed test with multiply phone numbers of one contact", allTrusted);
    }

    public void testSmsConvertsRightToAction_LocationRequest() throws Exception {

        //SmsMessage sms = TestUtils.createFakeSms(context, TRUSTED_CONTACT_SAMPLE_PHONENUMBER_INTERNATIONAL, "wru?;;;;;");
        Action incomeAction = mSmsToActionConverter.getActionFromSms(TRUSTED_CONTACT_SAMPLE_PHONENUMBER_INTERNATIONAL,  "wru?;;;;;", System.currentTimeMillis());
        boolean correctness;

        TrustedContact contact = contactsModel.getContactByPhoneNumber(TRUSTED_CONTACT_SAMPLE_PHONENUMBER_INTERNATIONAL);

        assert(contact.isTrusted());

        correctness =
                (incomeAction.isRequest()) &&
                        (incomeAction.getContact() != null) &&
                        (incomeAction.isFromTrustedContact()) &&
                        (!incomeAction.isNeedLocation());

        assertTrue(correctness);
    }

    public void testSmsConvertsRightToAction_ReplyToLocationRequest() throws Exception {
        Action incomeAction = mSmsToActionConverter.getActionFromSms(TRUSTED_CONTACT_SAMPLE_PHONENUMBER_INTERNATIONAL, ";50.3412,37.12234;Inderpendence Sq. 1;1008;2;22", System.currentTimeMillis());
        boolean correctness;

        correctness =
                        (!incomeAction.isRequest()) &&
                        (incomeAction.getContact() != null) &&
                        (incomeAction.getLocation()!= null) &&
                        (incomeAction.getAddress().equals("Inderpendence Sq. 1")) &&
                        (incomeAction.getAltitude() == 1008) &&
                        (incomeAction.getHumanActivity() == 2);

        assertTrue(correctness);
    }

}

