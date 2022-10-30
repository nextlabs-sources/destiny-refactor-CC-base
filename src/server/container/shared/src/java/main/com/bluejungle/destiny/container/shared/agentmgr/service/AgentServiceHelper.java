/*
 * Created on Jan 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.service;

import com.bluejungle.destiny.container.shared.agentmgr.IActionType;
import com.bluejungle.destiny.container.shared.agentmgr.IActivityJournalingAuditLevel;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatus;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates;
import com.bluejungle.destiny.container.shared.profilemgr.service.ProfileServiceHelper;
import com.bluejungle.destiny.container.shared.utils.IDCCComponentURLLocator;
import com.bluejungle.destiny.container.shared.utils.defaultimpl.DCCComponentURLLocatorImpl;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.agent.types.AgentStartupConfiguration;
import com.bluejungle.destiny.services.agent.types.AgentUpdates;
import com.bluejungle.destiny.services.management.types.ActionTypeDTO;
import com.bluejungle.destiny.services.management.types.ActionTypeDTOList;
import com.bluejungle.destiny.services.management.types.ActivityJournalingAuditLevelDTO;
import com.bluejungle.destiny.services.management.types.AgentCount;
import com.bluejungle.destiny.services.management.types.AgentDTO;
import com.bluejungle.destiny.services.management.types.AgentPolicyAssemblyStatusDTO;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.AgentStatistics;
import com.bluejungle.destiny.services.management.types.AgentTypeDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.policy.PolicyEditorIF;
import com.bluejungle.destiny.services.policy.PolicyEditorLocator;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

import org.apache.axis.types.UnsignedShort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/agentmgr/service/AgentServiceHelper.java#1 $
 */

public class AgentServiceHelper {

    /**
     * Constants
     */
    private static final String POLICY_SERVICE_LOCATION_SERVLET_PATH = "/services/PolicyEditorIFPort";
    private static final AgentServiceHelper SINGLETONINSTANCE = new AgentServiceHelper();

    /*
     * Private variables:
     */
    private IDCCComponentURLLocator urlLocator;
    private ProfileServiceHelper profileSvcHelper;
    private PolicyEditorIF policyEditorService;
    private Log log;

    /**
     * Constructor
     * 
     */
    private AgentServiceHelper() {
        super();
        this.profileSvcHelper = ProfileServiceHelper.getInstance();

        // Initialize the DCC Component URL locator:
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        ComponentInfo<IDCCComponentURLLocator> urlLocatorInfo = 
            new ComponentInfo<IDCCComponentURLLocator>(
                IDCCComponentURLLocator.COMP_NAME, 
                DCCComponentURLLocatorImpl.class.getName(), 
                LifestyleType.SINGLETON_TYPE);
        this.urlLocator = manager.getComponent(urlLocatorInfo);

        this.log = LogFactory.getLog(AgentServiceHelper.class.getName());
    }

    /**
     * Returns a singleton instance
     * 
     * @return singleton
     */
    public static final AgentServiceHelper getInstance() {
        return SINGLETONINSTANCE;
    }

    /**
     * Retrieve an array of data objects extracted from an array of IAgentDO
     * objects - optimized for policy deployment status retrieval (we only
     * retrieve deployment status from DPS once and reuse for all agents in the
     * array).
     * 
     * @param agents
     * @return extract data array
     */
    public AgentDTO[] extractAgentArrayData(IAgentDO[] agents) {
        AgentDTO[] agentArray = null;
        if (agents != null) {
            agentArray = new AgentDTO[agents.length];
            for (int i = 0; i < agents.length; i++) {
                IAgentDO agentDO = agents[i];
                AgentDTO agentDTO = extractAgentData(agentDO);
                agentArray[i] = agentDTO;
            }
        }
        return agentArray;
    }

    /**
     * Converts the policy assembly status into a web service object
     * 
     * @return the converted policy assembly status
     */
    public AgentPolicyAssemblyStatusDTO extractPolicyAssemblyStatusData() {
        final AgentPolicyAssemblyStatusDTO wsResult = new AgentPolicyAssemblyStatusDTO();
        wsResult.setLastPolicyUpdate(getLatestPolicyDeploymentTimestamp());
        return wsResult;
    }

    /**
     * Converts the agent policy status into its web service type
     * 
     * @param policyStatus
     *            agent policy status to convert
     * @return the corresponding web service type
     */
    protected AgentPolicyAssemblyStatusDTO extractAgentPolicyAssemblyStatus(IAgentPolicyAssemblyStatus policyStatus) {
        final AgentPolicyAssemblyStatusDTO wsResult = new AgentPolicyAssemblyStatusDTO();
        if (policyStatus != null) {
            wsResult.setLastPolicyUpdate(policyStatus.getLastAcknowledgedDeploymentBundleTimestamp());
        }
        return wsResult;
    }

    /**
     * Retrieve the data contained by the IAgentDO object
     * 
     * @param agent
     *            The AgentDO containing the data to transfer
     * @return agent DTO
     */
    public AgentDTO extractAgentData(IAgentDO agent) {
        AgentDTO wsAgent = null;
        if (agent != null) {
            wsAgent = new AgentDTO();
            if (agent.getId() != null) {
                wsAgent.setId(BigInteger.valueOf(agent.getId().longValue()));
            }

            AgentTypeDTO agentType = extractAgentType(agent.getType());
            wsAgent.setType(agentType);
            wsAgent.setIsOnline(agent.isOnline());
            wsAgent.setHost(agent.getHost());
            wsAgent.setIsPushReady(agent.getIsPushReady());
            if (agent.hasPushPort()) {
                UnsignedShort pushPort = new UnsignedShort(agent.getPushPort().longValue());
                wsAgent.setPushPort(pushPort);
            }
            wsAgent.setPolicyAssemblyStatus(extractAgentPolicyAssemblyStatus(agent.getPolicyAssemblyStatus()));
            wsAgent.setLastHeartbeat(agent.getLastHeartbeat());
            wsAgent.setCommProfileName(agent.getCommProfile().getName());
            wsAgent.setCommProfileID(BigInteger.valueOf(agent.getCommProfile().getId().longValue()));
        }
        return wsAgent;
    }

    /**
     * Retrieve the data contained by an IAgentType
     * 
     * @param agentType
     *            the data to extract
     * @return the extracts AgentTypeDTO
     */
    public AgentTypeDTO extractAgentType(IAgentType agentType) {
        Set<IActionType> actionTypes = agentType.getActionTypes();            
        ActionTypeDTO[] actionTypeDTOs = new ActionTypeDTO[actionTypes.size()];
        Iterator<IActionType> actionTypesIterator = actionTypes.iterator();
        for (int j=0; actionTypesIterator.hasNext(); j++) {
            IActionType nextActionType = actionTypesIterator.next();
            IActivityJournalingAuditLevel nextAuditingLevel = nextActionType.getActivityJournalingAuditLevel();
            ActivityJournalingAuditLevelDTO auditingLevelDTO = new ActivityJournalingAuditLevelDTO(nextAuditingLevel.getId(), nextAuditingLevel.getTitle(), new UnsignedShort(nextAuditingLevel.getOrdinal()));
            actionTypeDTOs[j] = new ActionTypeDTO(nextActionType.getId(), nextActionType.getTitle(), auditingLevelDTO);
        }
        
        return new AgentTypeDTO(agentType.getId(), agentType.getTitle(), new ActionTypeDTOList(actionTypeDTOs));

    }

    /**
     * Retrieve the timestamp for the latest policy deployment.
     * 
     * @return last policy deployment timestamp
     */
    public Calendar getLatestPolicyDeploymentTimestamp() {
        Calendar timestamp = null;
        try {
            if (this.policyEditorService == null) {
                // Get the DPS server URL:
                String[] urls = this.urlLocator.getComponentURLs(ServerComponentType.DPS);
                String location = urls[0];
                location += POLICY_SERVICE_LOCATION_SERVLET_PATH;
                PolicyEditorLocator locator = new PolicyEditorLocator();
                locator.setPolicyEditorIFPortEndpointAddress(location);
                this.policyEditorService = locator.getPolicyEditorIFPort();
            }

            timestamp = this.policyEditorService.getLatestDeploymentTime();
        } catch (ServiceNotReadyFault e) {
            this.log.error("Could not obtain agent policy up-to-date status", e);
        } catch (UnauthorizedCallerFault e) {
            this.log.error("Could not obtain agent policy up-to-date status", e);
        } catch (RemoteException e) {
            this.log.error("Could not obtain agent policy up-to-date status", e);
        } catch (ServiceException e) {
            this.log.error("Could not obtain agent policy up-to-date status", e);
        }
        return timestamp;
    }

    /**
     * Converts statistics to a web service object
     * 
     * @param stats
     *            original statistics
     * @return an instance of the AgentStatitics object that will go over the
     *         web service.
     */
    public AgentStatistics extractAgentStatisticsData(IAgentStatistics stats) {

        if (stats == null) {
            return null;
        }

        AgentStatistics resultStats = new AgentStatistics();
        Map<IAgentType, Long> agentCounts = stats.getAgentCounts();
        AgentCount[] agentCountsArray = new AgentCount[agentCounts.size()];
        int i = 0;
        for (Map.Entry<IAgentType, Long> nextAgentCount : agentCounts.entrySet()) {
            IAgentType nextAgentType = nextAgentCount.getKey();
            agentCountsArray[i] = new AgentCount(nextAgentCount.getKey().getId(), nextAgentType.getTitle(), nextAgentCount.getValue());
            i++;
        }
        resultStats.setAgentCount(agentCountsArray);
        resultStats.setTotalAgentCount(stats.getTotalAgentCount());
        resultStats.setHeartbeatsInLastDayCount(stats.getHeartbeatsInLastDayCount());
        resultStats.setAgentsNotConnectedInLastDayCount(stats.getAgentsDisconnectedInLastDayCount());
        resultStats.setAgentsWithOutOfDatePolicies(stats.getAgentsWithOutOfDatePolicies());
        return resultStats;
    }

    /**
     * Retrieve the data contained by the IAgentUpdates object
     * 
     * @param updates
     * @return updates DTO
     */
    public AgentUpdates extractUpdatesData(IAgentUpdates updates) {
        AgentUpdates updatesData = null;

        if (updates != null) {
            updatesData = new AgentUpdates();
            if (updates.getAgentProfileUpdate() != null) {
                AgentProfileDTO agentProfileDTO = this.profileSvcHelper.getAgentProfileDTO(updates.getAgentProfileUpdate());
                updatesData.setAgentProfile(agentProfileDTO);
            }

            if (updates.getCommProfileUpdate() != null) {
                CommProfileDTO commProfileDTO = this.profileSvcHelper.getCommProfileDTO(updates.getCommProfileUpdate());
                updatesData.setCommProfile(commProfileDTO);
            }
        }

        // For now, we always assume that the server is NOT busy:
        updatesData.setServerBusy(Boolean.FALSE);
        return updatesData;
    }

    /**
     * Retrieves the data contained by the IAgentStartupConfiguration object
     * 
     * @param configuration
     * @return configuration DTO
     */
    public AgentStartupConfiguration extractStartupConfigurationData(IAgentStartupConfiguration configuration) {
        AgentStartupConfiguration configurationData = null;
        if (configuration != null) {
            configurationData = new AgentStartupConfiguration();
            if (configuration.getId() != null) {
                configurationData.setId(BigInteger.valueOf(configuration.getId().longValue()));
            }
            if (configuration.getRegistrationId() != null) {
                configurationData.setRegistrationId(BigInteger.valueOf(configuration.getRegistrationId().longValue()));
            }
            if (configuration.getAgentProfile() != null) {
                AgentProfileDTO agentProfileDTO = this.profileSvcHelper.getAgentProfileDTO(configuration.getAgentProfile());
                configurationData.setAgentProfile(agentProfileDTO);
            }
            if (configuration.getCommProfile() != null) {
                CommProfileDTO commProfileDTO = this.profileSvcHelper.getCommProfileDTO(configuration.getCommProfile());
                configurationData.setCommProfile(commProfileDTO);
            }
        }
        return configurationData;
    }
}