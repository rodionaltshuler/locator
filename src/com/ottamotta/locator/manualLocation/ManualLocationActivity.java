package com.ottamotta.locator.manualLocation;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.ui.BaseActivity;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class ManualLocationActivity extends BaseActivity {

    @Inject
    private OrderExecutor orderExecutor;

    @Inject
    private EventBus bus;

    AddressFragment addressFragment;
    MyLocationMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_location_activity);

        if (savedInstanceState == null) {
            getSupportActionBar().setTitle(getString(R.string.where_are_you));
            Action action = null;
            if (getIntent().getExtras().containsKey(Action.EXTRA_ACTION)) {
                action = getIntent().getExtras().getParcelable(Action.EXTRA_ACTION);
                getSupportActionBar().setLogo(action.getContact().getPhotoDrawable(this));
                if (!bus.isRegistered(orderExecutor)) bus.register(orderExecutor);
            }
            addressFragment = action == null ? new AddressFragment() : AddressFragment.newInstance(action);
            mapFragment = action == null ? new MyLocationMapFragment() : MyLocationMapFragment.newInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.top_container, addressFragment);
            ft.replace(R.id.bottom_container, mapFragment);
            ft.commit();

        }
    }
}
