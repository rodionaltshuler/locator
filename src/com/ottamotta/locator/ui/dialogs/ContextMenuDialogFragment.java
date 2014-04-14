package com.ottamotta.locator.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.LocatorMenuItem;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.ui.ContextMenuAdapter;

import java.util.List;

public class ContextMenuDialogFragment extends SherlockDialogFragment implements AdapterView.OnItemClickListener {

    private List<LocatorMenuItem> items;
    private ContextMenuAdapter adapter;
    private TrustedContact contact;

    public ContextMenuDialogFragment(List<LocatorMenuItem> items, TrustedContact contact) {
        this.items = items;
        this.contact = contact;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.context_menu_dialog_fragment, null);
        ListView listView = (ListView) root.findViewById(android.R.id.list);
        adapter = new ContextMenuAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        ImageView photo = (ImageView) root.findViewById(R.id.contact_photo);
        photo.setImageDrawable(contact.getPhotoDrawable(getActivity()));
        TextView name = (TextView) root.findViewById(R.id.contact_name);
        name.setText(contact.getName() + "\n" + contact.getMainPhoneNumber());
        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.getItem(position).run();
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
