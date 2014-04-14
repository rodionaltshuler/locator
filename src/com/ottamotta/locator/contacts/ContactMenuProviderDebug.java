package com.ottamotta.locator.contacts;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.actions.Action;
import com.ottamotta.locator.actions.LocatorMenuItem;
import com.ottamotta.locator.actions.Order;
import com.ottamotta.locator.actions.OrderCreatedEvent;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.actions.OrdersDao;
import com.ottamotta.locator.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;

@Singleton
public class ContactMenuProviderDebug extends ContactMenuProviderRelease {

    @Inject
    private OrderExecutor orderExecutor;

    @Inject
    private OrdersDao ordersDao;

    @Inject
    private EventBus bus;

    @Override
    public List<LocatorMenuItem> getMenuItems(final TrustedContact contact) {
        List<LocatorMenuItem> result = super.getMenuItems(contact);
        LocatorMenuItem makeNotTrustedItem = new LocatorMenuItem(
                    "Make not trusted",
                    3,
                    new MakeNotTrustedRunnable(contact)
            );
        result.add(makeNotTrustedItem);

        LocatorMenuItem demoIncomeShare = new LocatorMenuItem(
                    "Demo income share",
                    4,
                    new Runnable() {
                        @Override
                        public void run() {
                            demoIncomeShare(contact);
                        }
                    }
        );
        result.add(demoIncomeShare);

        LocatorMenuItem demoOutcomeRequest = new LocatorMenuItem(
                "Demo outcome request",
                5,
                new Runnable() {
                    @Override
                    public void run() {
                        demoOutcomeRequest(contact);
                    }
                }
        );
        result.add(demoOutcomeRequest);

        return result;
    }

    private void demoRequestFromUnknownTrusted() {
        orderExecutor.createOrderFromIncomeSms("+380630000014", "wru?", System.currentTimeMillis());
    }

    private void demoIncomeShare(TrustedContact contact) {
        LatLng[] loc = DebugLocationsProvider.getRandomLocation();
        String locations = LocationUtils.getLocationFormattedNoWhitespace(loc[0]) + "," + LocationUtils.getLocationFormattedNoWhitespace(loc[1]);
        orderExecutor.createOrderFromIncomeSms(contact.getMainPhoneNumber(), ";" + locations + ";;;;60", System.currentTimeMillis());
    }

    private void demoOutcomeRequest(final TrustedContact contact) {

        Action action = Action.newRequestAction(contact);
        Order o = ordersDao.create(action);
        bus.post(new OrderCreatedEvent(o));
        orderExecutor.setOrderStatus(o, Order.STATUS_SMS_DELIVERED);

        //orderExecutor.requestLocation(contact);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                demoIncomeShare(contact);
            }
        }, 10 * 1000);

    }

    private static class DebugLocationsProvider {


        static final LatLng[] AUSTIN = new LatLng[] {
          new LatLng(30.331370, -97.836681),
          new LatLng(30.332953, -97.834058)
        };

        static final LatLng[] SEUL = new LatLng[] {
                new LatLng(30.331370, -97.836681),
                new LatLng(30.332953, -97.834058)
        };

        static final LatLng[] ALPES = new LatLng[] {
                new LatLng(45.921966, 7.046111),
                new LatLng(45.921988, 7.046041)
        };

        static final LatLng[] NEPAL = new LatLng[] {
                new LatLng(28.256182, 83.964821),
                new LatLng(28.257269, 83.963769)
        };

        static final LatLng[] KIEV = new LatLng[] {
                new LatLng(50.313768, 30.561151),
                new LatLng(50.294479, 30.571520)
        };

        static Random random = new Random();
        static final List<LatLng[]> locations = new ArrayList<>();

        static {
            locations.add(AUSTIN);
            locations.add(SEUL);
            locations.add(KIEV);
            locations.add(ALPES);
            locations.add(NEPAL);
        }

        public static LatLng[] getRandomLocation() {
            return  locations.get(random.nextInt(locations.size()));
        }


    }
}
