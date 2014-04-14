package com.ottamotta.locator.map;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.external.ExternalShareExecutor;
import com.ottamotta.locator.external.ExternalShareOptionsMenu;
import com.ottamotta.locator.external.LocationShareOption;
import com.ottamotta.locator.external.RequestPackageInstallDialogFragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class IncomeShareMapFragment extends BaseMapFragment {

    private Action action;
    protected static final int MAP_LINE_WIDTH = 8;

    private GoogleMap.InfoWindowAdapter mapAdapter;

    @Inject
    private ExternalShareOptionsMenu externalShareOptionsMenu;

    @Inject
    private Context context;

    protected ExternalShareExecutor.Listener noExternalAppInstalledListener = new ExternalShareExecutor.Listener() {
        @Override
        public void onAppNotInstalled(LocationShareOption option) {
            RequestPackageInstallDialogFragment f = RequestPackageInstallDialogFragment.newInstance(
                    getString(R.string.request_install_dialog_title),
                    getString(R.string.request_install_dialog_message, option.getAppName()),
                    getString(R.string.request_install_dialog_positive),
                    getString(R.string.request_install_dialog_negative),
                    option.getPackageName(), option.getAppName());
            f.show(getFragmentManager(), "dialogTag");
        }
    };

    public static IncomeShareMapFragment newInstance(Action incomeShareAction) {
        Bundle args = new Bundle();
        args.putParcelable(Action.EXTRA_ACTION, incomeShareAction);
        IncomeShareMapFragment fragment = new IncomeShareMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        action = getArguments().getParcelable(Action.EXTRA_ACTION);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.income_share_map_fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View movingInfo = view.findViewById(R.id.moving_info);
        if (action.hasTwoCoords()) {
            movingInfo.setVisibility(View.VISIBLE);
            TextView speedLabel = (TextView) view.findViewById(R.id.speed_label);
            speedLabel.setText(getString(R.string.average_speed_for_time, action.getDurationSec()));
            TextView speed = (TextView) view.findViewById(R.id.speed);
            speed.setText(getString(R.string.kmph, action.getSpeedFormatted()));
        } else {
            movingInfo.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupMap();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_options_menu, menu);
        externalShareOptionsMenu.fill(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_compass:
                action.getExecutor().showCompass();
            default:
                return externalShareOptionsMenu.onOptionsMenuItemSelected(item.getItemId(), action.getLocation(), noExternalAppInstalledListener) || super.onOptionsItemSelected(item);
        }
    }

    private void setupMarkers() {

        ActionMarker marker;
        marker = new ActionMarker(action.getContact(), action.getLocation(), action.getTime());

        if (action.hasTwoCoords()) {
            addMarker(map, marker.getLocation(), action.getContact(), R.drawable.point_to, 2);
            ActionMarker markerPrev;
            markerPrev = new ActionMarker(action.getContact(), action.getPrevLocation(), action.getPrevTime());
            addMarker(map, markerPrev.getLocation(), action.getContact(), R.drawable.point_from, 1);

            PolylineOptions line =
                    new PolylineOptions().add(markerPrev.getLocation())
                            .add(marker.getLocation())
                            .width(MAP_LINE_WIDTH)
                            .color(getResources().getColor(R.color.background_color));
            map.addPolyline(line);
        } else {
            addMarker(map, marker.getLocation(), action.getContact(), R.drawable.pointer, 1);
        }

        map.moveCamera(CameraUpdateFactory.newLatLng(action.getLocation()));
        map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
    }

    private void addMarker(GoogleMap googleMap, LatLng latLng, TrustedContact contact, int icon, int counter) {
        String title = "(" + counter + ") " + contact.getName();
        MarkerOptions newMarker =
            new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(icon))
                .infoWindowAnchor(0.5f, 1f);

        googleMap.addMarker(newMarker);

    }

    protected void setupMap() {
        setupMarkers();
        map.setMyLocationEnabled(true);
    }

}
