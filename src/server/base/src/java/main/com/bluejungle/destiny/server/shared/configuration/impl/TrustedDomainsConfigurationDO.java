package com.bluejungle.destiny.server.shared.configuration.impl;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/TrustedDomainsConfigurationDO.java#1 $
 */

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.ITrustedDomainsConfigurationDO;

/**
 * This class implements the trusted domains configuration object.
 */
public class TrustedDomainsConfigurationDO implements ITrustedDomainsConfigurationDO {

    private List trustedDomains = new ArrayList();

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.ITrustedDomainsConfigurationDO#getTrustedDomains()
     */
    public String[] getTrustedDomains() {
        return (String[])trustedDomains.toArray(new String[trustedDomains.size()]);
    }

    /**
     * Adds a list of mutually trusted domains to the collection.
     * @param domains a comma-separated list to add to the collection.
     */
    public void addMutuallyTrusted(String domains) {
        trustedDomains.add(domains);
    }
}
