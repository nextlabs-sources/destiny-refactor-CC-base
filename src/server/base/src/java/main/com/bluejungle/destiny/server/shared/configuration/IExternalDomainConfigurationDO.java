/*
 * Created on Jul 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/IExternalDomainConfigurationDO.java#1 $
 */

public interface IExternalDomainConfigurationDO {

    public String getDomainName();

    public IAuthenticatorConfigurationDO getAuthenticatorConfiguration();

    public IUserAccessConfigurationDO getUserAccessConfiguration();
}
