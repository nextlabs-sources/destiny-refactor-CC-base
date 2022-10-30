package com.bluejungle.destiny.server.shared.configuration;

/*
 * All sources, binaries and HTML pages (C) Copyright 2007 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs Inc.
 * All rights reserved worldwide.
 *
 * @author amorgan
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/ICustomObligationConfigurationDO.java#1 $
 */

import java.util.List;

public interface ICustomObligationConfigurationDO {
    /**
     * Returns the display name of the obligation
     *
     * @return displayName
     */
    public String getDisplayName();

    /**
     * Returns where the obligation should be executed (PDP or PEP)
     *
     * @return runAt
     */
    public String getRunAt();

    /**
     * Returns if the obligation should be run at user level or system level
     *
     * @return runBy
     */
    public String getRunBy();

    /*
     * Return the invocation string (the exec path for PDP obligations or
     * the simple name for PEP ones)
     *
     * @return invocationString
     */
    public String getInvocationString();

    /*
     * Return the argument format for the obligation
     *
     * @return arguments
     */
    public ICustomObligationArgumentDO[] getArguments();
}
