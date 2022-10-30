/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;

import net.sf.hibernate.expression.Criterion;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/IHibernateAgentMgrQueryTerm.java#1 $
 */

public interface IHibernateAgentMgrQueryTerm extends IAgentMgrQueryTerm {

    /**
     * Retrieve the Hibernate Criteron which will represent this query term
     * @return
     */
    Criterion getCriterion();
    
}
