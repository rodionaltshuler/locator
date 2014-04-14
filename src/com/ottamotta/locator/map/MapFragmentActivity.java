package com.ottamotta.locator.map;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.ui.BaseActivity;
import com.ottamotta.locator.roboguice.RoboSherlockFragment;
import com.ottamotta.locator.utils.LocatorTime;

public class MapFragmentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment_activity);
        Action action = getIntent().getExtras().getParcelable(Action.EXTRA_ACTION);

        ActionBar bar = getSupportActionBar();
        bar.setLogo(action.getContact().getPhotoDrawable(this));
        bar.setTitle(action.getContact().getName());
        bar.setSubtitle(new LocatorTime(action.getTime()).getTimeElapsedFormatted());
        RoboSherlockFragment mapFragment = IncomeShareMapFragment.newInstance(action);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mapFragment).commit();
    }

}
