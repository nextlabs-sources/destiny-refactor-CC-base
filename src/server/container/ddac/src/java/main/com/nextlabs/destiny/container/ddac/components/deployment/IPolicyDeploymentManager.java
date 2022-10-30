/*
 * Created on Aug 16, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/components/deployment/IPolicyDeploymentManager.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.components.deployment;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.ddac.DDACAgentServiceImpl;
import com.nextlabs.destiny.container.ddac.configuration.DDACConfiguration;

public interface IPolicyDeploymentManager {
    String COMP_NAME = "PolicyDeploymentManager";

    PropertyKey<DDACConfiguration> DDAC_CONFIGURATION = new PropertyKey<DDACConfiguration>("ddacconfig");
    PropertyKey<DDACAgentServiceImpl> DDAC_AGENT_SERVICE = new PropertyKey<DDACAgentServiceImpl>("agentservice");
    PropertyKey<IHibernateRepository> MGMT_DATA_SOURCE = new PropertyKey<IHibernateRepository>("mgmtrepository");
    PropertyKey<String> DDAC_COMPONENT_NAME = new PropertyKey<String>("ddaccomponentname");
}
