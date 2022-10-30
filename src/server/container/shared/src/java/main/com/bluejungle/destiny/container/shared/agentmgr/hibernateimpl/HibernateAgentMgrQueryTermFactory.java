/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IConcreteAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IORCompositeAgentMgrQueryTerm;
import com.bluejungle.framework.search.RelationalOp;

import java.util.Set;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/HibernateAgentMgrQueryTermFactory.java#1 $
 */

public class HibernateAgentMgrQueryTermFactory implements IAgentMgrQueryTermFactory {

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory#getConcreteQueryTerm(com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType, com.bluejungle.framework.search.RelationalOp, java.lang.Object)
     */
    public IConcreteAgentMgrQueryTerm getConcreteQueryTerm(AgentMgrQueryFieldType queryField, RelationalOp operator, Object value) {
        if (queryField == null) {
            throw new NullPointerException("queryField cannot be null.");
        }
        
        if (operator == null) {
            throw new NullPointerException("operator cannot be null.");
        }
        
        if (value == null) {
            throw new NullPointerException("value cannot be null.");
        }
        
        return new HibernateConcreteAgentMgrQueryTerm(queryField, operator, value);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory#getORCompositeQueryTerm(Set)
     */
    public IORCompositeAgentMgrQueryTerm getORCompositeQueryTerm(Set queryTermsToOr) {
        if (queryTermsToOr == null) {
            throw new NullPointerException("queryTermsToOr cannot be null.");
        }
                
        return new HibernateORCompositeAgentMgrQueryTerm(queryTermsToOr);
    }

}
