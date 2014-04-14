package com.ottamotta.locator.roboguice;

import com.ottamotta.locator.application.LocatorApplication;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class LocatorInjector {

    private static RoboInjector injector;

    public static synchronized RoboInjector getInjector() {
        if (null == injector) {
            injector = RoboGuice.getInjector(LocatorApplication.getInstance());
        }
        return injector;
    }

    public static synchronized void inject(Object object) {
        if (null == injector) {
            injector = RoboGuice.getInjector(LocatorApplication.getInstance());
        }
        injector.injectMembersWithoutViews(object);
    }

}
