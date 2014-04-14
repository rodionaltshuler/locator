package com.ottamotta.locator.map;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.contacts.TrustedContact;

/**
 * Holds info to display action on map
 */
public class ActionMarker implements Parcelable {

    private TrustedContact contact;
    private LatLng location;
    private long time;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public ActionMarker createFromParcel(Parcel in) {
            return new ActionMarker(in);
        }

        @Override
        public ActionMarker[] newArray(int size) {
            return new ActionMarker[size];
        }
    };

    public ActionMarker(TrustedContact contact, LatLng location, long time) {
        this.contact = contact;
        this.location = location;
        this.time = time;
    }

    public ActionMarker(Parcel in) {

        contact = in.readParcelable(((Object) this).getClass().getClassLoader());
        location = in.readParcelable(((Object) this).getClass().getClassLoader());
        time = in.readLong();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(contact, 0);
        dest.writeParcelable(location, 0);
        dest.writeLong(time);
    }

    public TrustedContact getContact() {
        return contact;
    }

    public LatLng getLocation() {
        return location;
    }

    public long getTime() {
        return time;
    }
}
