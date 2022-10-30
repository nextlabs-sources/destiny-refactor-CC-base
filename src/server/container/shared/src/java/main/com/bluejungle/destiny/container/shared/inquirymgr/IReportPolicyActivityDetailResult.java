/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This interface is implemented by all the policy activity detail results
 * returned by a report execution. Detail results are returned when the report
 * definition specifies that no grouping should be applied to the results.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportDetailResult.java#1 $
 */

public interface IReportPolicyActivityDetailResult extends IReportActivityDetailResult {

    /**
     * Returns the policy decision
     * 
     * @return the policy decision
     */
    public PolicyDecisionEnumType getPolicyDecision();

    /**
     * Returns the name of the policy
     * 
     * @return the name of the policy
     */
    public String getPolicyName();
}