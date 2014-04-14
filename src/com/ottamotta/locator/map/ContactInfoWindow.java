package com.ottamotta.locator.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.ottamotta.locator.R;
import com.ottamotta.locator.contacts.TrustedContact;

import java.util.Map;

class ContactInfoWindow implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;

    private final Map<String, TrustedContact> mContacts;

    public ContactInfoWindow(Context context, Map<String, TrustedContact> contacts) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.contact_info_window, null);
        mContacts = contacts;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TrustedContact mContact = mContacts.get(marker.getId());

        ImageView badge = (ImageView) mWindow.findViewById(R.id.badge);
        badge.setImageURI(mContact.getPhotoUri());

        TextView title = (TextView) mWindow.findViewById(R.id.title);
        //TODO implement A / B point showing
        //String titleText = "(" + (mapMarkers.size() + 1) + ") " + contact.getName();
        title.setText(mContact.getName());

        TextView snippet = (TextView) mWindow.findViewById(R.id.snippet);
        snippet.setText(mContact.getUpdateTimeFormatted());

        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
