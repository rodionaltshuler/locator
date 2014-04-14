package com.ottamotta.locator.external;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExternalShareOptionsMenu {

    @Inject
    ExternalLocationShareOptionsProvider externalOptionsProvider;

    @Inject
    ExternalShareExecutor externalShareExecutor;

    public void fill(Menu menu) {

        List<LocationShareOption> options = externalOptionsProvider.getOptions();
        for (LocationShareOption option : options) {
            MenuItem item = menu.add(0, option.getId(), Menu.NONE, option.getAppName());
        }
    }

    public boolean onOptionsMenuItemSelected(int itemId, LatLng locationTo, ExternalShareExecutor.Listener notInstalledListener) {
        for (LocationShareOption option : externalOptionsProvider.getOptions()) {
            if (option.getId() == itemId) {
                externalShareExecutor.doAction(option, locationTo, notInstalledListener);
                return true;
            }
        }
        return false;
    }

}
