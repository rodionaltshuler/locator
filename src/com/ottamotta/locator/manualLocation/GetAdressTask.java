package com.ottamotta.locator.manualLocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetAdressTask extends AsyncTask {

    public static final String TAG = "Geocoder";
    List<Address> mAddresses;

    Context mContext;
    String mSearchString;
    Listener mListener;

    public interface Listener {
        public void onResult(List<Address> addresses);

        public void onError();
    }

    public GetAdressTask(Context context, String searchString, Listener listener) {

        mContext = context;
        mSearchString = searchString;
        mListener = listener;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        mListener.onResult(mAddresses);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mListener.onError();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Please reboot your device to enable Geo services.")
                .setTitle("Apps for Place")
                .setPositiveButton("Ok, I'll reboot", dialogClickListener)
                .show();
    }

    @Override
    protected Object doInBackground(Object... params) {

        try {
            Geocoder geocoder;
            geocoder = new Geocoder(mContext, new Locale("en"));
            mAddresses = geocoder.getFromLocationName(mSearchString, 10);


        } catch (IOException e) {
            e.printStackTrace();
            if (!Geocoder.isPresent() || e.getMessage().toLowerCase().contains("service not available")) {
                publishProgress("error");
            }

        }

        return null;
    }
}