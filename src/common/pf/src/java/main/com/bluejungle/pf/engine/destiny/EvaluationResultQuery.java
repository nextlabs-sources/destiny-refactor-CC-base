/*
 * Created on Dec 6, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.pf.engine.destiny;


/**
 * EvaluationQueryResult is used to provide additional information in
 * case of query or confirm policy decision.  Each query/confirmation
 * has id of the policy.  This id should be used in the callback to 
 * inform the policy engine about the user's decision and provide user's
 * input.
 * 
 * Whether this is a confirmation or query is indicated in the effectName,
 * which is either EvaluationResult.QUERY, or EvaluationResult.CONFIRM.
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/EvaluationResultQuery.java#1 $
 */

public class EvaluationResultQuery {

    private String message;
    private String effectName;
    private long policyId;
    
    /**
     * Constructor
     * @param message message to be presented to the user
     * @param effectName either CONFIRM OR QUERY
     * @param policyId
     */
    public EvaluationResultQuery(String message, String effectType, long policyId) {
        super();
        this.message = message;
        this.effectName = effectType;
        this.policyId = policyId;
    }
    /**
     * @return message to display to the user
     */
    public String getMessage() {
        return this.message;
    }
    
    /**
     * @return an id of this query, unique per JVM instance
     */
    public long getPolicyId() {
        return this.policyId;
    }
    
    /**
     * Returns the effectName.
     * @return the effectName.
     */
    public String getEffectName() {
        return this.effectName;
    }
}
