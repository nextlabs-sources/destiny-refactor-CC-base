/*
 * Created on May 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.util.List;
import java.util.Map;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.destiny.obligation.IDObligation;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/IPolicyEvaluator.java#1 $
 */

public interface IPolicyEvaluator {

    /**
     * Configuration parameters required for init
     */
    public static final PropertyKey<IAgentPolicyContainer> AGENT_POLICY_CONTAINER_CONFIG_PARAM = new PropertyKey<IAgentPolicyContainer>("AgentPolicyContainer");
    public static final PropertyKey<IControlManager> CONTROL_MANAGER_CONFIG_PARAM = new PropertyKey<IControlManager>("ControlManager");
    public static final PropertyKey<AgentResourceManager> RESOURCE_MANAGER_CONFIG_PARAM = new PropertyKey<AgentResourceManager>("ResourceManager");
    public static final PropertyKey<IDSubjectManager> SUBJECT_MANAGER_CONFIG_PARAM = new PropertyKey<IDSubjectManager>("SubjectManager");

    /**
     * Index of arguments for policy evaluation
     */
    public static final int OLD_ACTION_INDEX = 1;
    public static final int OLD_TIMESTAMP_INDEX = 2;
    public static final int OLD_IGNORE_OBLIGATIONS_INDEX = 3;
    public static final int OLD_LOG_LEVEL_INDEX = 4;
    public static final int OLD_APP_NAME_INDEX = 5;
    public static final int OLD_APP_PATH_INDEX = 6;
    public static final int OLD_APP_FINGERPRINT_INDEX = 7;
    public static final int OLD_USER_SID_INDEX = 8;
    public static final int OLD_USER_NAME_INDEX = 9;
    public static final int OLD_IP_ADDRESS_INDEX = 10;
    public static final int OLD_PROCESS_TOKEN_INDEX = 11;
    public static final int OLD_SOURCE_INDEX = 12;
    public static final int OLD_SOURCE_ATTRS_INDEX = 13;
    public static final int OLD_TARGET_INDEX = 14;
    public static final int OLD_TARGET_ATTRS_INDEX = 15;

    public static final int REQUEST_ID_INDEX = 0;
    public static final int REQUEST_SOCKET_ID_INDEX = 1;
    public static final int AGENT_TYPE_INDEX = 2;
    public static final int TIMESTAMP_INDEX = 3;
    public static final int IGNORE_OBLIGATIONS_INDEX = 4;
    public static final int LOG_LEVEL_INDEX = 5;
    public static final int PROCESS_TOKEN_INDEX = 6;
    public static final int DIMENSION_DEFS_INDEX = 7;


    /**
     * Queries the decision engine to check if the operation is allowed.
     * Packages the results into an array.
     * 
     * @param paramArray
     *            array of string input parameters. Should contain strings in
     *            the following order: resource, action, user, application, host
     * @param resultParamArray
     *            output parameters
     * @return true if call is successful, false otherwise.
     *  
     */
    public boolean queryDecisionEngine(Object[] paramArray, List resultParamArray);

    /**
     * Queries the decision engine to check if the operation is allowed.  The results are
     * packaged into an array
     *
     * @param context a collection of DynamicAttributes describing the evaluation context (from, to, user, etc)
     * @param processToken the process token, to be used for user impersonation
     * @param resultParamArray
     *            output parameters
     * @return true if call is successful, false otherwise
     */
    public boolean queryDecisionEngine(Map<String, DynamicAttributes> context, Long processToken, int loggingLevel, boolean ignoreObligations, List resultParamArray);
}
