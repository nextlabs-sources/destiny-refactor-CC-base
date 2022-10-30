/*
 * Created on Nov 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentQuerySpecImpl;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * HostsWithAgentsTransferTask is used to transfer information in the management
 * database regarding which hosts have agents to the Policy Framework database.
 * This data is used to create the "All Desktops with Agents" desktop component
 * in policy author
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dps/src/java/main/com/bluejungle/destiny/container/dps/HostsWithAgentsTransferTask.java#3 $
 */

public class HostsWithAgentsTransferTask implements IHasComponentInfo<HostsWithAgentsTransferTask>, IManagerEnabled {

    private static final ComponentInfo COMPONENT_INFO = new ComponentInfo(HostsWithAgentsTransferTask.class.getName(), HostsWithAgentsTransferTask.class.getName(), LifestyleType.SINGLETON_TYPE);

    private static final String ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME = "HOST/Computers With Enforcers";

    private static final String COMPONENT_PQL_PREFIX = "ID null STATUS APPROVED CREATOR \"0\" ACCESS_POLICY ACCESS_CONTROL PBAC FOR * ON * BY appuser.did = resource.dso.owner DO allow ALLOWED_ENTITIES COMPONENT \"" + ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME
            + "\" = (((";
    private static final String COMPONENT_PQL_SUFFIX = ") AND TRUE) AND (TRUE AND TRUE))";
    private static final String OR_CONJUNCTION = " OR ";
    private static final String EQUALS_OPERATOR = " = ";
    private static final String HOST_RELATION_LVALUE = "host.name ";
    private static final String ONE_HOST_WITH_AGENT_PREDICATE = " OR FALSE";
    private static final String NO_HOSTS_WITH_AGENTS_PREDICATE = "FALSE OR TRUE";

    private IComponentManager componentManager;

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo getComponentInfo() {
        return COMPONENT_INFO;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.componentManager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.componentManager = manager;
    }

    void run() throws PersistenceException, EntityManagementException, PQLException {
        StringBuffer hostRelationPQL = new StringBuffer();

        IAgentDO[] allComplianceAgents = getComplianceAgents();
        if (allComplianceAgents.length == 1) {
            StringBuffer hostRelationToAdd = buildHostRelation(allComplianceAgents[0]);
            hostRelationPQL.append(hostRelationToAdd);
            hostRelationPQL.append(ONE_HOST_WITH_AGENT_PREDICATE);
        } else if (allComplianceAgents.length > 0) {
            for (int i = 0; i < allComplianceAgents.length; i++) {
                if (i != 0) {
                    hostRelationPQL.append(OR_CONJUNCTION);
                }

                IAgentDO nextAgent = allComplianceAgents[i];
                StringBuffer nextHostRelation = buildHostRelation(nextAgent);
                hostRelationPQL.append(nextHostRelation);
            }
        } else {
            hostRelationPQL.append(NO_HOSTS_WITH_AGENTS_PREDICATE);
        }

        StringBuffer desktopComponentPQLString = new StringBuffer(COMPONENT_PQL_PREFIX);
        desktopComponentPQLString.append(hostRelationPQL);
        desktopComponentPQLString.append(COMPONENT_PQL_SUFFIX);

        updateComponent(desktopComponentPQLString.toString());
    }

    /**
     * Build a relation pql string for the specified agent
     * 
     * @param nextAgent
     *            the agent to represent with the condition
     * @return the relation pql for the specified agent
     */
    private StringBuffer buildHostRelation(IAgentDO nextAgent) {
        return new StringBuffer(HOST_RELATION_LVALUE).append(EQUALS_OPERATOR).append("\"").append(nextAgent.getHost()).append("\"");
    }

    /**
     * Retrieve the list of registered compliance agents
     * 
     * @return the request list of registered agents
     * @throws PersistenceException
     *             if agent retrieval fails
     */
    private IAgentDO[] getComplianceAgents() throws PersistenceException {
        IAgentManager agentMgr = this.getAgentManager();
        IAgentType agentType = agentMgr.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentMgrQueryTermFactory queryTermFactory = agentMgr.getAgentMgrQueryTermFactory();
        
        List agentQueryTerms = new ArrayList(1);
        agentQueryTerms.add(queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, agentType));
        agentQueryTerms.add(queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.REGISTERED, RelationalOp.EQUALS, Boolean.TRUE));

        IAgentMgrQuerySpec agentQuerySpec = new AgentQuerySpecImpl(agentQueryTerms, Collections.EMPTY_LIST, 0);
        IAgentQueryResults agentQueryResults = agentMgr.getAgents(agentQuerySpec);
        return agentQueryResults.getAgents();
    }

    /**
     * Returns an instance of the IAgentManager implementation class
     * (AgentManager) after obtaining it from the component manager.
     * 
     * @return IAgentManager
     */
    protected IAgentManager getAgentManager() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (!compMgr.isComponentRegistered(IAgentManager.COMP_NAME)) {
            throw new IllegalStateException("Agent Manager not initialized.");
        }

        return (IAgentManager) compMgr.getComponent(IAgentManager.COMP_NAME);
    }

    /**
     * Update the "All Desktops With Enforcer Agents" desktop component
     * 
     * @param componentPQL
     *            the pql to set on the component
     * @throws EntityManagementException
     * @throws PQLException
     */
    private void updateComponent(String componentPQL) throws EntityManagementException, PQLException {
        LifecycleManager lm = getManager().getComponent(LifecycleManager.COMP_INFO);
        DevelopmentEntity allHostsWithAgentsEntity = lm.getEntityForName(
                EntityType.COMPONENT
              , ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME
              , LifecycleManager.MAKE_EMPTY
        );
        allHostsWithAgentsEntity.setPql(componentPQL);
        lm.saveEntity(allHostsWithAgentsEntity, LifecycleManager.MAKE_EMPTY, null);
    }
}
