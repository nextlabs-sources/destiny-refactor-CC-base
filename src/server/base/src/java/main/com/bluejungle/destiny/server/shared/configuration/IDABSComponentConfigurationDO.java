/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * Interface to represent DABS configuration
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/IDABSComponentConfigurationDO.java#1 $
 */

public interface IDABSComponentConfigurationDO extends IDCCComponentConfigurationDO {

    public ITrustedDomainsConfigurationDO getTrustedDomainsConfiguration();
    
    public IFileSystemLogConfigurationDO getFileSystemLogConfiguration();

    public IRegularExpressionConfigurationDO[] getRegularExpressions();
}
