/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.framework.search.RelationalOp;

import java.util.Set;

/**
 * An Agent Manager Query Term Factory is used to create Agent Query Terms
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentMgrQueryTermFactory.java#1 $
 */

public interface IAgentMgrQueryTermFactory {

    /**
     * Create a concrete agent query term
     * 
     * @param queryField
     * @param operator
     * @param value
     * @return
     */
    public IConcreteAgentMgrQueryTerm getConcreteQueryTerm(AgentMgrQueryFieldType queryField, RelationalOp operator, Object value);
    public IORCompositeAgentMgrQueryTerm getORCompositeQueryTerm(Set queryTermsToOr);
}
