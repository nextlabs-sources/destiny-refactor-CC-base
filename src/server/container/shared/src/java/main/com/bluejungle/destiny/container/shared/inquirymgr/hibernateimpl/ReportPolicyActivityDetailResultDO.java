/*
 * Created on Nov 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the policy activity detail result data object.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportPolicyActivityDetailResultDO.java#1 $
 */

public class ReportPolicyActivityDetailResultDO extends BaseReportActivityResultDO implements IReportPolicyActivityDetailResult {

    private PolicyDecisionEnumType policyDecision;
    private String policyName;

    /**
     * Constructor
     */
    public ReportPolicyActivityDetailResultDO() {
        super();
    }

    /**
     * Constructor. This constructor is used to take results from a policy
     * activity log entry.
     * 
     * @param newAction
     *            action type
     * @param newApplicationName
     *            application name
     * @param fromResInfo
     *            name of the "from resource"
     * @param newId
     *            record id
     * @param newHostIPAddress
     *            host IP address
     * @param newHostName
     *            name of the host
     * @param newPolicyDecision
     *            policy decision
     * @param newPolicyName
     *            name of the policy
     * @param newTimestamp
     *            timestamp
     * @param newToResourceName
     *            name of the "to resource"
     * @param newUserName
     *            name of the user
     */
    public ReportPolicyActivityDetailResultDO(ActionEnumType newAction, String newApplicationName, String newFromResName, String newHostIPAddress, String newHostName, Long newId, PolicyDecisionEnumType newPolicyDecision, String newPolicyName,
            Calendar newTimestamp, String newToResourceName, String newUserName, int loggingLevel) {
        super(newAction, newApplicationName, newFromResName, newHostIPAddress, newHostName, newId, newTimestamp, newToResourceName, newUserName, loggingLevel);
        setPolicyDecision(newPolicyDecision);
        setPolicyName(newPolicyName);
    }

    /**
     * Similar constructor, except that also takes a stored result id from the
     * stored result table. Constructor
     * 
     * @param action
     *            action type
     * @param fromResName
     *            name of the "from resource"
     * @param id
     *            id of the matching data row
     * @param storedResultId
     *            id of the stored result in the result table
     * @param hostIPAddress
     *            IP address of the host
     * @param hostName
     *            name of the host
     * @param policyDecision
     *            policy decision
     * @param policyName
     *            policy name
     * @param timestamp
     *            timestamp
     * @param toResName
     *            name of the "to resource" 
     * @param userName
     *            user name
     */
    public ReportPolicyActivityDetailResultDO(ActionEnumType action, String applicationName, String fromResName, String hostIPAddress, String hostName, Long id, PolicyDecisionEnumType policyDecision, String policyName, Long storedResultId,
            Calendar timestamp, String toResName, String userName, int loggingLevel) {
        this(action, applicationName, fromResName, hostIPAddress, hostName, id, policyDecision, policyName, timestamp, toResName, userName, loggingLevel);
        setStoredResultId(storedResultId);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getPolicyDecision()
     */
    public PolicyDecisionEnumType getPolicyDecision() {
        return this.policyDecision;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getPolicyName()
     */
    public String getPolicyName() {
        return this.policyName;
    }

    /**
     * Sets the policy decision
     * 
     * @param newDecision
     *            new decision to set
     */
    protected void setPolicyDecision(PolicyDecisionEnumType newDecision) {
        this.policyDecision = newDecision;
    }

    /**
     * Set the policy name
     * 
     * @param newName
     *            new policy name to set
     */
    protected void setPolicyName(String newName) {
        this.policyName = newName;
    }
}
