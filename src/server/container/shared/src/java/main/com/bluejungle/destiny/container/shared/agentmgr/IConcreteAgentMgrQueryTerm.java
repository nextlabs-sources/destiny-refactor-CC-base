/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.framework.search.RelationalOp;

/**
 * A concrete AgentMgrQueryTerm is a leaf query criteria containing a concreate
 * query field, value and operator
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IConcreteAgentMgrQueryTerm.java#1 $
 */

public interface IConcreteAgentMgrQueryTerm extends IAgentMgrQueryTerm {

    /**
     * Returns the field name to query on
     * 
     * @return the field name (represented by a type) to query on
     */
    public AgentMgrQueryFieldType getFieldName();

    /**
     * Returns the operator for the query term
     * 
     * @return the operator for the query term
     */
    public RelationalOp getOperator();

    /**
     * Returns the expression
     * 
     * @return the expression
     */
    public Object getExpression();
}
