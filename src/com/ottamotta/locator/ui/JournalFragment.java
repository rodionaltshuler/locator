package com.ottamotta.locator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.RoboSherlockFragment;

import javax.inject.Inject;

public class JournalFragment extends RoboSherlockFragment {

    public static final String ARG_TRUSTED_CONTACT = "trusted_contact";

    @Inject
    private OrderExecutor orderExecutor;

    private ListView listView;
    private JournalAdapter adapter;
    private TrustedContact contact;

    public static JournalFragment newInstance(TrustedContact contact) {
        JournalFragment fragment = new JournalFragment();
        if (contact != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_TRUSTED_CONTACT, contact);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_TRUSTED_CONTACT)) contact = getArguments().getParcelable(ARG_TRUSTED_CONTACT);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        root = inflater.inflate(R.layout.journal, null);

        View contactMenu = root.findViewById(R.id.contact_menu);

        TextView contactName = (TextView) contactMenu.findViewById(R.id.title);
        contactName.setText(contact.getName());

        ImageView contactPhoto = (ImageView) contactMenu.findViewById(R.id.photo);
        contactPhoto.setImageDrawable(contact.getPhotoDrawable(getActivity()));

        View requestButton = contactMenu.findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderExecutor.requestLocation(contact);
            }
        });

        View shareButton = contactMenu.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderExecutor.shareLocation(contact);
            }
        });

        listView = (ListView) root.findViewById(android.R.id.list);
        listView.setAdapter(getAdapter(contact));
        listView.setEmptyView(root.findViewById(android.R.id.empty));

        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private synchronized JournalAdapter getAdapter(TrustedContact contact) {
        if (adapter == null) {
            adapter = new JournalAdapter(getActivity(), contact);
        }
        return adapter;
    }

}
