/*
 * Created on Oct 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Next Labs Inc.,
 * San Mateo CA, Ownership remains with Next Labs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * This interface represents a regular expression definition in the configuration
 * file.
 * 
 * @author amorga
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/IRegularExpressionConfigurationDO.java#1 $
 */

public interface IRegularExpressionConfigurationDO {

    /**
     * Returns the name of this regexp
     * 
     * @return name
     */
    public String getName();

    /**
     * Returns the value of this regexp
     * 
     * @return value
     */
    public String getValue();
}
