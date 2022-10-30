package com.bluejungle.destiny.server.shared.configuration;

/*
 * All sources, binaries and HTML pages (C) Copyright 2007 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs Inc.
 * All rights reserved worldwide.
 *
 * @author amorgan
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/ICustomObligationsConfigurationDO.java#1 $
 */


public interface ICustomObligationsConfigurationDO {
    /**
     * Returns all the custom obligations
     */
    public ICustomObligationConfigurationDO[] getCustomObligations();

    public void addCustomObligation(ICustomObligationConfigurationDO obl);
}
