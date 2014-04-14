package com.ottamotta.locator.ui;

import com.ottamotta.locator.contacts.TrustedContact;

public class TrustedContactSelectedEvent {

    public TrustedContact contact;

    public TrustedContactSelectedEvent(TrustedContact contact) {
        this.contact = contact;
    }
}
