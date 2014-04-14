package com.ottamotta.locator.actions;

import com.ottamotta.locator.R;
import com.ottamotta.locator.roboguice.LocatorInjector;

import javax.inject.Inject;

public class ActionMenu {

    static class LocatorMenuItemShowOnMap extends LocatorMenuItem {
        public LocatorMenuItemShowOnMap(final Action action) {
            super(BaseLocatorActionExecutor.CONTEXT_MENU_ITEM_TITLES.get(BaseLocatorActionExecutor.MENU_ITEM_SHOW_ON_MAP),
                BaseLocatorActionExecutor.MENU_ITEM_SHOW_ON_MAP, new Runnable() {
                    @Override
                    public void run() {
                        action.getExecutor().showOnMap();
                    }
                });
            setImageResourceId(R.drawable.ic_map);
        }
    }

    static class LocatorMenuItemCancel extends LocatorMenuItem {
        @Inject
        OrderExecutor orderExecutor;
        Action action;

        public LocatorMenuItemCancel(final Action action) {
            super(BaseLocatorActionExecutor.CONTEXT_MENU_ITEM_TITLES.get(BaseLocatorActionExecutor.MENU_ITEM_CANCEL), BaseLocatorActionExecutor.MENU_ITEM_CANCEL, null);
            LocatorInjector.inject(this);
            this.action = action;
        }
        @Override
        public void run() {
            orderExecutor.cancel(action);
        }
    }

    static class LocatorMenuItemAddToTrusted extends LocatorMenuItem {
        @Inject
        OrderExecutor orderExecutor;
        Action action;

        public LocatorMenuItemAddToTrusted(Action action) {
            super(BaseLocatorActionExecutor.CONTEXT_MENU_ITEM_TITLES.get(BaseLocatorActionExecutor.MENU_ITEM_ADD_TO_TRUSTED), BaseLocatorActionExecutor.MENU_ITEM_ADD_TO_TRUSTED, null);
            LocatorInjector.inject(this);
            this.action = action;
        }

        @Override
        public void run() {
            orderExecutor.addToTrusted(action);
        }
    }

    static class NavigateWithCompass extends LocatorMenuItem {

        Action action;

        public NavigateWithCompass(Action action) {
            super(BaseLocatorActionExecutor.CONTEXT_MENU_ITEM_TITLES.get(BaseLocatorActionExecutor.MENU_ITEM_NAVIGATE_WITH_COMPASS), BaseLocatorActionExecutor.MENU_ITEM_NAVIGATE_WITH_COMPASS, null);
            this.action = action;
            setImageResourceId(R.drawable.ic_compass);
        }

        @Override
        public void run() {
            action.getExecutor().showCompass();
        }

    }
}
