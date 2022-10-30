/*
 * Created on Feb 23, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;

/**
 * This interface exposes the status of the registration of a DCC component with
 * the management server.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/IDCCRegistrationStatus.java#1 $
 */

public interface IDCCRegistrationStatus {

    /**
     * Returns the application user's configuration
     * 
     * @return the application user's configuration
     */
    public IApplicationUserConfigurationDO getApplicationUserConfiguration();
    
    /**
     * Returns the message handler configuration
     * 
     * @return the message handler configuration
     */
    public IMessageHandlersConfigurationDO getMessageHandlersConfiguration();

    /**
     * Returns the custom obligations configuration
     * 
     * @return the custom obligations configuration
     */
    public ICustomObligationsConfigurationDO getCustomObligationsConfiguration();
    
    /**
     * Returns Action List configuration
     * 
     * @return Action List
     */
    public IActionListConfigDO getActionListConfig();
    
    /**
     * Returns the server component configuration
     * 
     * @return the server component configuration
     */
    public IDCCComponentConfigurationDO getComponentConfiguration();

    /**
     * Returns the data repositories configuration
     * 
     * @return the data repositories configuration
     */
    public RepositoryConfigurationList getRepositoryConfigurations();
    
    /**
     * Returns the result (i.e outcome) of the registration request
     * 
     * @return the outcome of the registration request
     */
    public DMSRegistrationResult getRegistrationResult();
}
