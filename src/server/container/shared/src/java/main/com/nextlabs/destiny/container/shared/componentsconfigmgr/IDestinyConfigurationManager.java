/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr;

import java.util.Set;

import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This represents the configuration manager interface that is used by DMS to
 * read configuration information for the different DCC components.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/IDestinyConfigurationManager.java#1 $
 */

public interface IDestinyConfigurationManager extends IInitializable, ILogEnabled, IDisposable, IConfigurable {

    public static final String COMP_NAME = "DestinyConfigurationManager";

    /**
     * Returns the authentication configuration object
     * 
     * @return auth configuration object
     */
    public IApplicationUserConfigurationDO getApplicationUserConfiguration();
    
    /**
     * Returns the message handlers configuration object
     * 
     * @return the message handlers configuration object
     */
    public IMessageHandlersConfigurationDO getMessageHandlersConfiguration();
    
    /**
     * Returns the custom obligation configuration
     * 
     * @return custom obligations object
     */
    public ICustomObligationsConfigurationDO getCustomObligationsConfiguration();

    /**
     * Returns Action List configuration
     * 
     * @return Action List object
     */
    public IActionListConfigDO getActionListConfig();
    
    /**
     * Returns the DCC component configuration object of the specified type
     * 
     * @param type
     *            of the dcc component
     * @return dcc component configuration object
     */
    public IDCCComponentConfigurationDO getDCCConfiguration(ServerComponentType type);

    /**
     * Returns a set of data source configuration objects
     * 
     * @return set of objects
     */
    public Set<? extends IRepositoryConfigurationDO> getRepositories();
}
