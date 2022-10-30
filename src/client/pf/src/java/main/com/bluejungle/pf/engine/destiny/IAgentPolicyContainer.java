/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.engine.destiny;

import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;

import java.util.Map;
import java.util.Set;

/**
 * A single interface that should be used by the interceptor agent to talk to
 * the policy framework
 *
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/IAgentPolicyContainer.java#11 $
 */

public interface IAgentPolicyContainer {

    String CLASSNAME = IAgentPolicyContainer.class.getName();
    ComponentInfo<IAgentPolicyContainer> COMP_INFO = new ComponentInfo<IAgentPolicyContainer>(
    		CLASSNAME, 
    		AgentPolicyContainer.class, 
    		IAgentPolicyContainer.class, 
    		LifestyleType.SINGLETON_TYPE);

    PropertyKey<String> BASE_DIR_PROPERTY_NAME = new PropertyKey<String>("BaseDirectory");
    PropertyKey<AgentTypeEnumType> AGENT_TYPE_CONFIG_PARAM = new PropertyKey<AgentTypeEnumType>("AgentType");
    PropertyKey<Long> DISK_CACHE_FLUSH_FREQUENCY_CONFIG_PARAM = new PropertyKey<Long>("DiskCacheFlushFrequency");
    PropertyKey<String> DISK_CACHE_LOCATION_CONFIG_PARAM = new PropertyKey<String>("DiskCacheLocation");
    PropertyKey<Integer> DISK_CACHE_SIZE_CONFIG_PARAM = new PropertyKey<Integer>("DiskCacheSize");

    /**
     * Evaluate policies, executes obligations if executeObligations is true
     *
     * @param reqId unique request Id.
     * @param agentType identifies agent.
     * @param processToken process security token which may be needed to access resource properties.
     * @param context The evaluation context of the policy, which is a map of
     * attributes associated with individual contexts arranged by context name.
     * @param ts time when action is being done.
     * @param executeObligations indicates whether obligations should be executed or not.
     * @return the <code>EvaluationResult</code> for the activity.
     */

    EvaluationResult evaluate(
        long reqId
    ,   AgentTypeEnumType agentType
    ,   Long processToken
    ,   Map<String, DynamicAttributes> context
    ,   long ts
    ,   int level
    ,   boolean executeObligations
    );

    /**
     * Checks whether the application should be hooked or not.
     *
     * @param appName
     *            name of the application to be checked.
     * @return true if the application should not be hooked; returns false
     *         otehrwise.
     */
    boolean isApplicationIgnorable(String appName);
    
    /**
     * Checks if the application is trusted by name.
     *
     * @param appName name of the application to be checked.
     * @return true if the application is trusted, false otehrwise.
     */
    boolean isApplicationTrusted(String appName);

    /**
     * updates the container with a new deployment bundle
     *
     * @param bundle
     *            new deployment bundle
     * @throws InvalidBundleException
     *             if the bundle is not valid
     */
    void update(DeploymentBundleSignatureEnvelope bundle) throws InvalidBundleException;

    /**
     * Constructs a deployment request for a desktop agent that can be submitted
     * to the server
     *
     * @param users
     *            all users currently logged onto the system, cannot be null
     * @param hostName
     *            name of the agent host, cannot be null
     * @return deployment request
     */
    public DeploymentRequest getDeploymentRequestForUsers(Set<? extends ISystemUser> users, String hostName);

    /**
     * Constructs a deployment request for a file server agent that can be
     * submitted to the server
     *
     * @param hostName
     *            name of the agent host, cannot be null
     * @return deployment request
     */
    public DeploymentRequest getDeploymentRequestForSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectTypes, String hostName);

    /**
     * Constructs a deployment request for the specified agent that can be
     * submitted to the server
     *
     * @param hostName
     *            name of the agent host, cannot be null
     * @param agentType
     *            type of agent
     * @return deployment request
     */
    public DeploymentRequest getDeploymentRequestForSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectTypes, String hostName, AgentTypeEnum agentType);

    /**
     * @return the number of policies that are present in this policy container
     */
    public int getPolicyCount();

    /**
     * Load the currently deployed bundle. This is not done at startup to allow
     * the client to handle an invalid bundle
     *
     * @throws InvalidBundleException
     *             if the bundle is invalid
     */
    public void loadBundle() throws InvalidBundleException;

}
