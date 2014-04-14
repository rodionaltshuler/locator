package com.ottamotta.locator.actions;

import android.content.Context;
import android.content.Intent;
import android.test.ApplicationTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

public class ActionTest extends ApplicationTestCase<LocatorApplication> {

    private Context mContext;

    @Inject
    private ContactsModel contactsProvider;

    @Inject
    private OrderExecutor orderExecutor;

    private static final String EXISTING_PHONE_NUM = "0504478616";
    private SmsSender mSmsSender;

    public ActionTest() {
        super(LocatorApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        LocatorInjector.inject(this);
        mSmsSender = SmsSender.getInstance();
        LocatorInjector.inject(this);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOutcomeRequestHandling() throws Exception {

        TrustedContact contact = contactsProvider.getContactByPhoneNumber(EXISTING_PHONE_NUM);
        Action outcomeRequest = new Action(contact.getMainPhoneNumber(), contact);
        outcomeRequest.setType(Action.TYPE_OUT);
        outcomeRequest.setContact(contact);
        outcomeRequest.setRequest(true);

        mSmsSender.setListenerForOneSend(new SmsSender.SendSmsOneShotListener() {
            @Override
            public boolean onSendSms(Action action, String smsBody) {
                return false;
            }
        });

        final Order order = orderExecutor.create(outcomeRequest); //order executing - sending sms

        SmsConfirmationReceiver receiver = new SmsConfirmationReceiver();

        Intent intentSentSms = new Intent();
        intentSentSms.setAction(SmsConfirmationReceiver.SENT_SMS_ACTION);
        intentSentSms.putExtra(SmsConfirmationReceiver.LOCATOR_ACTION_EXTRA, outcomeRequest);
        intentSentSms.putExtra(SmsConfirmationReceiver.ORDER_ID_EXTRA, outcomeRequest.getOrderId());
        intentSentSms.putExtra(SmsConfirmationReceiver.RESULT_CODE_EXTRA, SmsConfirmationReceiver.SENT_SMS_RESULT_CODE);

        Intent intentDelivered = new Intent();
        intentDelivered.setAction(SmsConfirmationReceiver.DELIVERED_SMS_ACTION);
        intentDelivered.putExtra(SmsConfirmationReceiver.LOCATOR_ACTION_EXTRA, outcomeRequest);
        intentDelivered.putExtra(SmsConfirmationReceiver.ORDER_ID_EXTRA, outcomeRequest.getOrderId());

        receiver.onReceive(getContext(), intentSentSms);
        receiver.onReceive(getContext(), intentDelivered);

        Action reply = outcomeRequest.reply(); //it's making on the other side
        reply.setLocation(new LatLng(50.1234, 37.2222));
        reply.setNeedLocation(false);
        reply.setType(Action.TYPE_IN);

        //now we receive this sms...
        ExecutorOutcomeShare outcomeShare = new ExecutorOutcomeShare(reply);
        String smsWeReceiveWithResponse = outcomeShare.getSmsText(reply);
        SmsToActionConverter.getInstance().getActionFromSms(reply.getContact().getMainPhoneNumber(), smsWeReceiveWithResponse, System.currentTimeMillis());
        orderExecutor.replaceRequestWithReplyAction(order, reply);

        Order orderTest = orderExecutor.getOrder(order.getId());
        Thread.sleep(2000);
        assertTrue("Order should have 4 history records, but has only " + orderTest.getHistory().size(), 4 == orderTest.getHistory().size());

    }

    public void testComposeOutcomeRequestSms() throws Exception {
        final long ORDER_ID = 22;

        TrustedContact contact = contactsProvider.getContactByPhoneNumber(EXISTING_PHONE_NUM);
        Action action = new Action(contact.getMainPhoneNumber(), contact);
        action.setRequest(true);
        action.setOrderId(ORDER_ID);
        ExecutorOutcomeRequest request = new ExecutorOutcomeRequest(action);

        String smsText = request.getSmsText(action);
        String smsExpected = BaseLocatorActionExecutor.LOCATION_REQUEST + ";;;;;" + ORDER_ID;

        assertTrue("smsText fact {" + smsText + "} and expected {" + smsExpected + "} don't match", smsText.equals(smsExpected));

    }

    public void testUnknownContact() throws Exception {
        final String UNKNOWN_PHONE = "012345678";
        SmsToActionConverter converter = SmsToActionConverter.getInstance();
        Action action = converter.getActionFromSms(UNKNOWN_PHONE, "wru?", System.currentTimeMillis());
        assert action.getContact() != null;
    }
}
