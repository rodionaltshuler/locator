package com.ottamotta.locator.manualLocation;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ottamotta.locator.R;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends ArrayAdapter<Address> {

    List<Address> mData = new ArrayList<>();
    Context mContext;

    public AddressAdapter(Context context, List<Address> data) {
        super(context, 0, data);
        mContext = context;

        if (data!=null)
        for (Address adr : data) {
            if (adr.getCountryName()!=null || adr.getLocality()!=null) {
                mData.add(adr);
            }
        }

    }

    @Override
    public Address getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public String getLocationName(int position) {
        String result;
        Address address = mData.get(position);
        if (address.getLocality()!=null) {
            result = address.getLocality();
        } else {
            result = address.getCountryName();
        }
        return result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(mContext).inflate(R.layout.adresses, parent, false);
        Address address = mData.get(position);

        TextView location = (TextView) convertView.findViewById(R.id.location);
        TextView subLocation = (TextView) convertView.findViewById(R.id.sub_location);

        location.setText(address.getAddressLine(0));
        if (address.getLocality()!=null) {
            subLocation.setText(address.getLocality());
        } else {
            subLocation.setText(address.getCountryName());
        }

        return convertView;
    }
}