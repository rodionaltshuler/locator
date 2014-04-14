package com.ottamotta.locator.contacts;

import com.ottamotta.locator.actions.LocatorMenuItem;

import java.util.List;

public interface ContactMenuProvider {
    List<LocatorMenuItem> getMenuItems(TrustedContact contact);
}
