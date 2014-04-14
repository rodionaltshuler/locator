package com.ottamotta.locator.manualLocation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.ottamotta.locator.R;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;

public class MyLocationMapFragment extends RoboFragment {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final int WAIT_AFTER_CAMERA_CHANGE_TIMEOUT = 300;
    private static final long INITIAL_TIMEOUT = 2000;

    @Inject
    private EventBus bus;

    @Inject
    private Context context;

    private SupportMapFragment mapFragment;

    private GoogleMap map;

    private Handler handler = new Handler();


    public static MyLocationMapFragment newInstance() {
        MyLocationMapFragment fragment = new MyLocationMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        checkGooglePlayServicesAvailable();
        map = mapFragment.getMap();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                map.setOnCameraChangeListener(cameraChangeListener);
            }
        }, INITIAL_TIMEOUT);
        return view;
    }

    private GoogleMap.OnCameraChangeListener cameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            handler.removeCallbacks(sendLocationRunnable);
            handler.postDelayed(sendLocationRunnable, WAIT_AFTER_CAMERA_CHANGE_TIMEOUT);
        }
    };

    private Runnable sendLocationRunnable = new Runnable() {
        @Override
        public void run() {
            bus.post(new LocationOnMapChangeEvent(map.getCameraPosition().target));
        }
    };

    public void onEvent(ShowLocationOnMapEvent event) {
        if (mapFragment != null && map != null) {
            map.setOnCameraChangeListener(null);
            GoogleMap googleMap = mapFragment.getMap();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(event.location));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14), WAIT_AFTER_CAMERA_CHANGE_TIMEOUT / 2, null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    map.setOnCameraChangeListener(cameraChangeListener);
                }
            }, WAIT_AFTER_CAMERA_CHANGE_TIMEOUT / 2);
        }
    }

    private void checkGooglePlayServicesAvailable() {
        //Check whether google play services installed
        //http://stackoverflow.com/questions/19218961/android-google-map-v2-need-update-google-play-service-on-device
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode) && getActivity() != null) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                dialog.show();
            }
        }
    }

}
