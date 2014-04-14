package com.ottamotta.locator.roboguice;

import com.google.inject.AbstractModule;
import com.ottamotta.locator.actions.OrderExecutor;
import com.ottamotta.locator.actions.OrderExecutorImpl;
import com.ottamotta.locator.actions.OrdersDao;
import com.ottamotta.locator.actions.OrdersDaoImpl;
import com.ottamotta.locator.contacts.ContactMenuProvider;
import com.ottamotta.locator.contacts.ContactMenuProviderRelease;
import com.ottamotta.locator.contacts.ContactsDao;
import com.ottamotta.locator.contacts.ContactsDaoImpl;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.ContactsModelImpl;

import de.greenrobot.event.EventBus;

public class LocatorBindingsModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ContactMenuProvider.class).to(ContactMenuProviderRelease.class);
        //bind(ContactMenuProvider.class).to(ContactMenuProviderDebug.class);
        bind(ContactsDao.class).to(ContactsDaoImpl.class);
        bind(OrderExecutor.class).to(OrderExecutorImpl.class);
        bind(ContactsModel.class).to(ContactsModelImpl.class);
        bind(OrdersDao.class).to(OrdersDaoImpl.class);
        bind(EventBus.class).toInstance(EventBus.getDefault());
    }

}
