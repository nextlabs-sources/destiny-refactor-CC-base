/*
 * Created on Feb 23, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.registration.DMSRegistrationResult;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;

/**
 * This is the DCC registration status implementation class. This class contains
 * information to allow a new server component instance to be created. Mostly,
 * it contains configuration information and the status of the registration
 * request.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/DCCRegistrationStatusImpl.java#1 $
 */

public class DCCRegistrationStatusImpl implements IDCCRegistrationStatus {

    private final IApplicationUserConfigurationDO appUserConfig;
    private final IMessageHandlersConfigurationDO messageHandlersConfig;
    private final ICustomObligationsConfigurationDO custOblConfig;
    private final IActionListConfigDO actionListConfig;
    private final IDCCComponentConfigurationDO compConfig;
    private final RepositoryConfigurationList dataRepositoryConfigs;
    private final DMSRegistrationResult registrationOutcome;

    /**
     * Constructor
     * 
     * @param appUserConfig
     *            application user configuration
     * @param compConfig
     *            server component configuration
     * @param registrationOutcome
     *            outcome of the registration request with management server
     * @param dataRepositoryConfigs
     *            data repository (i.e. DB) configuration
     * @param newActionListConfig
     *            Action List configuration     *            
     */
    public DCCRegistrationStatusImpl(IApplicationUserConfigurationDO appUserConfig,
                                     IMessageHandlersConfigurationDO messageHandlersConfig,
                                     ICustomObligationsConfigurationDO custOblConfig,
                                     IActionListConfigDO newActionListConfig,
                                     IDCCComponentConfigurationDO compConfig,
                                     RepositoryConfigurationList dataRepositoryConfigs,
                                     DMSRegistrationResult registrationOutcome
                                     ) {
        super();
        this.appUserConfig = appUserConfig;
        this.messageHandlersConfig = messageHandlersConfig;
        this.custOblConfig = custOblConfig;
        this.actionListConfig = newActionListConfig;
        this.compConfig = compConfig;
        this.dataRepositoryConfigs = dataRepositoryConfigs;
        this.registrationOutcome = registrationOutcome;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus#getApplicationUserConfiguration()
     */
    public IApplicationUserConfigurationDO getApplicationUserConfiguration() {
        return appUserConfig;
    }
    
    public IMessageHandlersConfigurationDO getMessageHandlersConfiguration(){
        return messageHandlersConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus#getCustomObligationsConfiguration()
     */
    public ICustomObligationsConfigurationDO getCustomObligationsConfiguration() {
        return custOblConfig;
    }
    
    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus#getActionListConfig()
     */
    public IActionListConfigDO getActionListConfig() {
        return actionListConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus#getComponentConfiguration()
     */
    public IDCCComponentConfigurationDO getComponentConfiguration() {
        return compConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus#getRepositoryConfigurations()
     */
    public RepositoryConfigurationList getRepositoryConfigurations() {
        return dataRepositoryConfigs;
    }
    
    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus#getRegistrationResult()
     */
    public DMSRegistrationResult getRegistrationResult() {
        return registrationOutcome;
    }
}
