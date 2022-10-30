/*
 * Created on Aug 17, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/DDACAgentServiceImpl.java#1 $:
 */

package com.nextlabs.destiny.container.ddac;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentQuerySpecImpl;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionfactory.VersionFactory;
import com.nextlabs.destiny.container.ddac.configuration.DDACActiveDirectoryConfiguration;

public class DDACAgentServiceImpl {
    /**
     * Log object
     */
    private static final Log log = LogFactory.getLog(DDACAgentServiceImpl.class.getName());

    private static final IAgentStartupData DEFAULT_AGENT_STARTUP = new IAgentStartupData() {
        public Integer getPushPort() {
            return 0;
        }
    };

    private IAgentType activeDirectoryAgentType = null;
    private ActiveDirectoryNameIdMapping idMap = new ActiveDirectoryNameIdMapping();

    public DDACAgentServiceImpl() {
        initializeActiveDirectoryAgentType();
    }

    private void initializeActiveDirectoryAgentType() {
        activeDirectoryAgentType = getAgentManager().getAgentType(AgentTypeEnumType.ACTIVE_DIRECTORY.getName());
    }

    private void registerAgent(String agentName) throws PersistenceException {
        IAgentManager agentManager = getAgentManager();

        IAgentRegistrationData agentRegistration = createRegistrationData(agentName);

        // Check to see if this agent is already registered
        IAgentMgrQueryTermFactory termFactory = agentManager.getAgentMgrQueryTermFactory();
        AgentQuerySpecImpl agentByHostAndType = new AgentQuerySpecImpl();
        agentByHostAndType.addSearchSpecTerm(termFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.EQUALS, agentRegistration.getHost()));
        agentByHostAndType.addSearchSpecTerm(termFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, agentRegistration.getType()));
        agentByHostAndType.setLimit(1);
        IAgentQueryResults result = null;
        try {
            result = agentManager.getAgents(agentByHostAndType);
        } catch (InvalidQuerySpecException e) {
            // Should never happen here, the types are valid
            throw new PersistenceException(e);
        }

        Long id = null;

        if (result != null && result.getAgents() != null && result.getAgents().length > 0) {
            // Already registered
            id = ((AgentDO)result.getAgents()[0]).getId();
            idMap.put(agentName, id);
            log.info("Agent " + agentName + " already registered with id " + id);
        } else {
            // Register new agent
            id = agentManager.registerAgent(agentRegistration).getId();
            idMap.put(agentName, id);
            log.info("Registering agent " + agentName + " with id " + id);
        }

        try {
            agentManager.startupAgent(id, DEFAULT_AGENT_STARTUP);
        } catch (InvalidIDException iie) {
            log.error("Unable to start-up agent with id " + id);
        }
    }

    public void registerAgents(Collection<DDACActiveDirectoryConfiguration> cfgs) {
        if (cfgs == null) {
            return;
        }

        for (DDACActiveDirectoryConfiguration cfg : cfgs) {
            try {
                registerAgent(cfg.getHostName());
            } catch (PersistenceException pe) {
                log.error("Error registering " + cfg.getHostName(), pe);
            }
        }
    }

    public IAgentManager getAgentManager() {   
        return (IAgentManager) ComponentManagerFactory.getComponentManager().getComponent(IAgentManager.COMP_NAME);
    }

    private static final IVersion dummyActiveDirectoryVersion = VersionFactory.makeVersion(1, 0, 0, 0, 0);

    private IAgentRegistrationData createRegistrationData(final String hostName) {
        return new IAgentRegistrationData() {
            public String getHost() {
                return hostName;
            }

            public IAgentType getType() {
                return activeDirectoryAgentType;
            }

            public IVersion getVersion() {
                return dummyActiveDirectoryVersion;
            }
        };
    }

    private class ActiveDirectoryNameIdMapping {
        private Map<String, Long> map = new HashMap<String, Long>();

        synchronized void put(String hostName, Long id) {
            if (hostName == null) {
                throw new NullPointerException("hostName is null");
            }

            if (id == null) {
                throw new NullPointerException("id is null");
            }

            map.put(hostName, id);
        }

        synchronized Long get(String hostName) {
            if (hostName == null) {
                throw new NullPointerException("hostName is null");
            }

            return map.get(hostName);
        }

    }

    public Long getId(String hostName) {
        return idMap.get(hostName);
    }

}
