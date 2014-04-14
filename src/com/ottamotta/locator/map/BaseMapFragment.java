package com.ottamotta.locator.map;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.ottamotta.locator.R;
import com.ottamotta.locator.roboguice.RoboSherlockFragment;

public abstract class BaseMapFragment extends RoboSherlockFragment {

    protected final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected SupportMapFragment mapFragment;
    protected GoogleMap map;

    protected Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGooglePlayServicesAvailable();
    }

    protected abstract int getLayoutResourceId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        map = mapFragment.getMap();
        return view;
    }

    protected void checkGooglePlayServicesAvailable() {
        //Check whether google play services installed
        //http://stackoverflow.com/questions/19218961/android-google-map-v2-need-update-google-play-service-on-device
        Activity parentActivity = getActivity();
        if (parentActivity == null)
            return;

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(parentActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, parentActivity,
                    PLAY_SERVICES_RESOLUTION_REQUEST);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
                dialog.show();
            }
            else {
                finish();
            }
        }
    }

    private void finish() {
        if (getActivity() != null)
            getActivity().finish();
    }

}
