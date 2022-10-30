/*
 * Created on Jan 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;

/**
 * EvaluationQueryResponse contains the user's response to a policy
 * decision of type QUERY.  It contains an id of the policy that originated
 * the query, and the user's response.
 * 
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/EvaluationQueryResponse.java#1 $:
 */

public class EvaluationQueryResponse {
    
    private Long policyId;
    private String userResponse;
   
    
    
    /**
     * Constructor
     * @param policyId
     * @param userResponse
     */
    public EvaluationQueryResponse(Long policyId, String userResponse) {
        super();
        this.policyId = policyId;
        this.userResponse = userResponse;
    }
    
    
    /**
     * Returns the policyId.
     * @return the policyId.
     */
    public Long getPolicyId() {
        return this.policyId;
    }
    /**
     * Returns the userResponse.
     * @return the userResponse.
     */
    public String getUserResponse() {
        return this.userResponse;
    }
}
