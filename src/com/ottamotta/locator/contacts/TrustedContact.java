package com.ottamotta.locator.contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.ottamotta.locator.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrustedContact implements Parcelable, Comparable<TrustedContact> {

    public static final int STATUS_ENABLED = 0;
    public static final int STATUS_DISABLED = -1;
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public TrustedContact createFromParcel(Parcel in) {
            return new TrustedContact(in);
        }

        @Override
        public TrustedContact[] newArray(int size) {
            return new TrustedContact[size];
        }
    };
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    public static final Comparator<? super TrustedContact> BY_TRUST = new Comparator<TrustedContact>() {
        @Override
        public int compare(TrustedContact lhs, TrustedContact rhs) {
            if (lhs.isTrusted() && !rhs.isTrusted()) {
                return -1;
            }
            else if (!lhs.isTrusted() && rhs.isTrusted()) {
                return 1;
            }
            return 0;
        }
    };

    private String id;
    private int status; //enabled or disabled
    private String name;
    private boolean trusted;
    private Uri photoUri;
    private String mainPhoneNumber;

    private Set<String> phoneNumbers;

    private long updateTime;

    public TrustedContact() {
        phoneNumbers = new HashSet<>();
    }

    public TrustedContact(String id) {
        this();
        this.status = STATUS_ENABLED;
        this.id = id;
    }

    /**
     * Use TrustedContact(String id, String name, Set<String> phoneNumbers, Uri photoUri)  instead
     * @param id
     * @param name
     * @param phoneNumber
     * @param photoUri
     */
    @Deprecated
    public TrustedContact(String id, String name, String phoneNumber, Uri photoUri) {
        this(id);
        this.name = name;
        this.photoUri = photoUri;
        this.phoneNumbers.add(phoneNumber);
    }

    public TrustedContact(String id, String name, Set<String> phoneNumbers, String mainPhoneNumber, Uri photoUri) {
        this(id);
        this.name = name;
        this.photoUri = photoUri;
        this.phoneNumbers = phoneNumbers;
        this.mainPhoneNumber = mainPhoneNumber;
    }

    public TrustedContact(Parcel in) {
        id = in.readString();
        status = in.readInt();
        trusted = in.readInt() == 1;
        name = in.readString();
        photoUri = in.readParcelable(((Object) this).getClass().getClassLoader());
        mainPhoneNumber = in.readString();
        List<String> phoneNumbersList = new ArrayList<>();
        in.readStringList(phoneNumbersList);
        phoneNumbers = new HashSet<>(phoneNumbersList);
        updateTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(status);
        dest.writeInt(trusted ? 1 : 0);
        dest.writeString(name);
        dest.writeParcelable(photoUri, flags);
        dest.writeString(mainPhoneNumber);
        dest.writeStringList(new ArrayList<>(phoneNumbers));
        dest.writeLong(updateTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TrustedContact))
            return false;

        TrustedContact contact = (TrustedContact) o;

        return id.equals(contact.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public String getMainPhoneNumber() {
        return mainPhoneNumber;
    }

    private String getPhoneNumberWithoutWhitespaces(String original) {
        return original.replace(" ", "");
        //return original.replaceAll("\\s", "");
    }

    public void setMainPhoneNumber(String phoneNumber) {
        mainPhoneNumber = getPhoneNumberWithoutWhitespaces(phoneNumber);
        if (!phoneNumber.contains(mainPhoneNumber)) {
            throw new RuntimeException("Contact has no such phone number set as main (" + mainPhoneNumber + ")");
        }
    }

    public void addPhoneNumber(String phoneNumber) {
        String newNumber = getPhoneNumberWithoutWhitespaces(phoneNumber);
        this.phoneNumbers.add(newNumber);
        if (mainPhoneNumber == null)
            mainPhoneNumber = newNumber;
    }

    public Set<String> getPhoneNumbers() {
        return Collections.unmodifiableSet(phoneNumbers);
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateTimeFormatted() {
        return format.format(new Date(updateTime));
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public Drawable getPhotoDrawable(Context context) {
        Drawable photo;
        if (photoUri == null)
            return getDefaultContactDrawable(context);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(photoUri);
            photo = Drawable.createFromStream(inputStream, photoUri.toString());
            if (photo == null) {
                //contact is not exists in contents
                photo = getDefaultContactDrawable(context);
            }
        }
        catch (FileNotFoundException e) {
            photo = getDefaultContactDrawable(context);
        }
        return photo;
    }

    public Bitmap getPhotoEmptyBitmap(Context context) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
            R.drawable.rodion);
        return icon;
    }

    public Bitmap getPhoto(Context context) {
        try {
            InputStream is = context.getContentResolver().openInputStream(getPhotoUri());
            Bitmap result = BitmapFactory.decodeStream(is);
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                return getPhotoEmptyBitmap(context);
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return getPhotoEmptyBitmap(context);
    }

    private Drawable getDefaultContactDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rodion);
    }

    @Override
    public int compareTo(TrustedContact another) {
        if (another != null && another.getName() != null) {
            return getName().compareTo(another.getName());
        }
        return 1;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
