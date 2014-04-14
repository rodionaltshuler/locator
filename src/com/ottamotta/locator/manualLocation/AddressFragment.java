package com.ottamotta.locator.manualLocation;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.utils.LocationUtils;
import com.ottamotta.locator.roboguice.RoboSherlockFragment;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class AddressFragment extends RoboSherlockFragment {

    private static final int WAIT_BEFORE_REFRESH_TIMEOUT = 2000;

    @Inject
    private OrderExecutor orderExecutor;

    @Inject
    private EventBus mBus;

    @Inject
    Context context;

    private EditText mSearchView;
    private ListView mListView;

    private Handler handler = new Handler();

    private LatLng resultLocation;

    private Action action;


    public static AddressFragment newInstance(Action action) {
        Bundle args =  new Bundle();
        args.putParcelable(Action.EXTRA_ACTION, action);
        AddressFragment fragment = new AddressFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!mBus.isRegistered(this)) {
            mBus.register(this);
        }
        action = getArguments().getParcelable(Action.EXTRA_ACTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.address_fragment, container, false);
        mListView = (ListView) root.findViewById(android.R.id.list);
        mSearchView = (EditText) root.findViewById(R.id.input);
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshLocationList();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button sendResultButton = (Button) root.findViewById(R.id.send_button);
        sendResultButton.setOnClickListener(sendResultListener);

        final LatLng lastSharedLocation = orderExecutor.getLastSharedLocation();
        if (lastSharedLocation != null) {
            mSearchView.setText(LocationUtils.getLocationFormatted(lastSharedLocation));
            onLocationFound(lastSharedLocation);
            Log.d(LocatorApplication.TAG, "Last shared location: " + LocationUtils.getLocationFormatted(lastSharedLocation));
        }

        return root;
    }



    private Runnable refreshLocationsRunnable = new Runnable() {
        @Override
        public void run() {
            String query = mSearchView.getText().toString();
            if (isCoords(query)) {
                LatLng location = LocationUtils.parseLocation(query);
                if (location != null) onLocationFound(location);
            } else {
                AsyncTask task = new GetAdressTask(context, query, taskListener);
                task.execute();
            }
        }
    };

    private static final char[] coordSymbols = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            ',', '.', '-', ' '};

    private boolean isCoords(String query) {
        char[] src = query.toCharArray();
        for (char c : src) {
            boolean exists = false;
            for (char allowed : coordSymbols) {
                if (allowed == c) {
                    exists = true;
                    break;
                }
            }
            if (!exists) return false;
        }
        return true;
    }

    private void refreshLocationList() {
        handler.removeCallbacks(refreshLocationsRunnable);
        handler.postDelayed(refreshLocationsRunnable, WAIT_BEFORE_REFRESH_TIMEOUT);
    }

    private GetAdressTask.Listener taskListener = new GetAdressTask.Listener() {
        @Override
        public void onResult(List<Address> addresses) {
            if (getActivity() != null) {
                AddressAdapter mAdapter = new AddressAdapter(getActivity(), addresses);
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getCount() > 0) {
                    Address firstAddress = mAdapter.getItem(0);
                    onLocationFound(new LatLng(firstAddress.getLatitude(), firstAddress.getLongitude()));
                }
            }
        }

        @Override
        public void onError() {
            //TODO implement
            //was: getActivity().finish();
        }
    };

    private View.OnClickListener sendResultListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(context, "Location: " + LocationUtils.getLocationFormatted(resultLocation), Toast.LENGTH_LONG).show();
            getActivity().onBackPressed();
            if (action == null) {
                mBus.post(new ManualLocationReceivedEvent(LocationUtils.createLocation(resultLocation.latitude, resultLocation.longitude)));
            } else {
                action.setTime(System.currentTimeMillis());
                action.setLocation(resultLocation);
                mBus.post(new OrderExecutor.LocationFoundForRequestEvent(action));
            }
        }
    };


    private void onLocationFound(LatLng location) {
        resultLocation = location;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        mBus.post(new ShowLocationOnMapEvent(location));
    }

    public void onEvent(LocationOnMapChangeEvent event) {
        resultLocation = event.location;
        mSearchView.setText(LocationUtils.getLocationFormatted(event.location));
        handler.removeCallbacks(refreshLocationsRunnable);
    }

}
