package com.ottamotta.locator.ui;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.LocatorMenuItem;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.contacts.ContactMenuProvider;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.LocatorInjector;
import com.ottamotta.locator.ui.dialogs.ContextMenuDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

class TrustedContactsAdapter extends ArrayAdapter<TrustedContact> {

    private static final String CONTACTS_KEY = "Contacts";

    private Context context;

    private List<TrustedContact> mContacts;
    private TrustedContact contactSelected;

    @Inject
    ContactMenuProvider contactMenuProvider;

    @Inject
    private  OrderExecutor orderExecutor;

    @Inject
    private EventBus bus;

    @Inject
    private ContactsModel contactsProvider;

    public TrustedContactsAdapter(Context context) {
        super(context, 0);
        LocatorInjector.inject(this);
        this.context = context;
        bus.register(this);
        initContacts();
    }

    @Override
    public TrustedContact getItem(int position) {
        return mContacts.get(position);
    }

    @Override
    public int getCount() {
        if (mContacts == null) {
            return 0;
        } else {
            return mContacts.size() + 1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == mContacts.size()) {
            return getAddContactView();
        } else {
            boolean firstInGroup = false;
            if (position == 0) {
                firstInGroup = true;
            } else if (mContacts.get(position).isTrusted() != mContacts.get(position - 1).isTrusted()) {
                firstInGroup = true;
            }
            return getContactView(mContacts.get(position), firstInGroup);
        }
    }

    private void initContacts() {
        onEvent(new ContactsModel.ContactsLoadedEvent(contactsProvider.getTrustedContacts()));
    }

    public void onEvent(ContactsModel.ContactsLoadedEvent event) {
        mContacts = new ArrayList<>();
        mContacts.addAll(event.trustedContacts);
        sortContactsByTrust(mContacts);
        notifyDataSetChanged();
    }

    private void sortContactsByTrust(List<TrustedContact> contacts) {
        Collections.sort(contacts, TrustedContact.BY_TRUST);
    }

    public void onEvent(ContactsModel.ContactsChangedEvent event) {
        initContacts();
    }

    /*public void onEvent(OrderExecutorImpl.OrderStatusChangedEvent event) {
        Order order = orderExecutor.getOrder(event.orderId);
        showMessageOrderChanged(order);
    }

    private void showMessageOrderChanged(Order order) {
        String message = order.getContact().getName() + ": "
                + order.getHistory().get(order.getHistory().size()-1).comment;

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }*/

    public void addContact(TrustedContact newContact) {
        if (!mContacts.contains(newContact)) {
            mContacts.add(newContact);
        }
        contactsProvider.addContact(newContact);
    }


    public void addContact(Uri contactUri) {
        contactsProvider.addContactTrusted(contactUri);
    }

    public void removeContact(TrustedContact contact) {
        contactsProvider.removeContact(contact);
    }

    private View getContactView(final TrustedContact contact, boolean firstInGroup) {
        int layoutResource = contact.isTrusted() ? R.layout.trusted_contact_row : R.layout.not_trusted_contact_row;
        final View convertView = LayoutInflater.from(context).inflate(layoutResource, null);
        ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
        TextView title = (TextView) convertView.findViewById(R.id.phone_number);
        View menuButton = convertView.findViewById(R.id.card_header_button_expand);

        title.setText(contact.getName());

        photo.setImageDrawable(contact.getPhotoDrawable(context));

        if (contact.equals(contactSelected)) {
            convertView.findViewById(R.id.highlight_area).setSelected(true);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactSelected = contact;
                notifyDataSetChanged();
                bus.post(new TrustedContactSelectedEvent(contact));
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<LocatorMenuItem> menuItems = contactMenuProvider.getMenuItems(contact);
                DialogFragment fragment = new ContextMenuDialogFragment(menuItems, contact);
                fragment.show(((FragmentActivity)context).getSupportFragmentManager(), "contextMenu");
            }
        });

        if (firstInGroup) {
            View header = convertView.findViewById(R.id.header);
            header.setVisibility(View.VISIBLE);
            TextView headerTitle = (TextView) header.findViewById(R.id.title);
            String text = contact.isTrusted() ? context.getResources().getString(R.string.family) : context.getResources().getString(R.string.not_trusted);
            headerTitle.setText(text);
        }
        return convertView;
    }

    private View getAddContactView() {
        View view = LayoutInflater.from(context).inflate(R.layout.trusted_contact_add_row, null);
        View addContactsButton =  view.findViewById(R.id.pick_contact);
        addContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new NeedAddContactEvent());
            }
        });
        return view;
    }
}
