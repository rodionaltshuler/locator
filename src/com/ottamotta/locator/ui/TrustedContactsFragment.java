package com.ottamotta.locator.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.widget.ShareActionProvider;
import com.google.inject.Inject;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.RoboSherlockFragment;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class TrustedContactsFragment extends RoboSherlockFragment {

    private static final int PICK_CONTACT = 1;

    @Inject
    private EventBus bus;

    @Inject
    private OrderExecutor orderExecutor;

    @Inject
    private ContactsModel contactsModel;

    private ListView listView;
    private ShareActionProvider shareActionProvider;
    private TrustedContactsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private synchronized TrustedContactsAdapter getAdapter() {
        if (adapter == null) {
            adapter = new TrustedContactsAdapter(getActivity());
        }
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        root = inflater.inflate(R.layout.contacts_fragment, null);
        listView = (ListView) root.findViewById(android.R.id.list);
        adapter = getAdapter();
        listView.setAdapter(adapter);
        try {
            bus.register(this);
        }
        catch (EventBusException e) {
            e.printStackTrace();
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

   private void demo(TrustedContact contact) {
        //demoNotTrustedRequest();
        demoIncomeShare(contact);

    }

    private void demoIncomeShare(TrustedContact item) {
        orderExecutor.demoIncomeShare(item);
    }

    private void demoNotTrustedRequest() {
        orderExecutor.demoNotTrustedRequest();
    }

    public void onEvent(NeedAddContactEvent event) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    adapter.addContact(contactData);
                }
        }

    }

}
