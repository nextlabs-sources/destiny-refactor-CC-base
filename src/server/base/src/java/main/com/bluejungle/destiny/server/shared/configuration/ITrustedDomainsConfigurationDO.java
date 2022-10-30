package com.bluejungle.destiny.server.shared.configuration;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/ITrustedDomainsConfigurationDO.java#1 $
 */

/**
 * This interface defines the contract for trusted domain configuration.
 */
public interface ITrustedDomainsConfigurationDO {

    /**
     * Returns an array of strings representing trusted domains.
     * @return an array of strings representing trusted domains.
     */
    public String[] getTrustedDomains();

}
