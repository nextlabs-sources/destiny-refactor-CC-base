/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IORCompositeAgentMgrQueryTerm;

import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Disjunction;
import net.sf.hibernate.expression.Expression;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/HibernateORCompositeAgentMgrQueryTerm.java#1 $
 */

class HibernateORCompositeAgentMgrQueryTerm implements IORCompositeAgentMgrQueryTerm, IHibernateAgentMgrQueryTerm {

    private Set concreteQueryTermsToOr;

    /**
     * Create an instance of HibernateORCompositeAgentMgrQueryTerm
     * 
     * @param queryTermsToOR
     */
    public HibernateORCompositeAgentMgrQueryTerm(Set queryTermsToOR) {
        if (queryTermsToOR == null) {
            throw new NullPointerException("queryTermsToOR cannot be null.");
        }

        this.concreteQueryTermsToOr = queryTermsToOR;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.IHibernateAgentMgrQueryTerm#getCriterion()
     */
    public Criterion getCriterion() {
        Disjunction criterionDisjunction = Expression.disjunction();

        Iterator concreteQueryTermsToOrIterator = this.concreteQueryTermsToOr.iterator();
        while (concreteQueryTermsToOrIterator.hasNext()) {
            IHibernateAgentMgrQueryTerm nextConcreteQueryTerm = (IHibernateAgentMgrQueryTerm) concreteQueryTermsToOrIterator.next();
            criterionDisjunction.add(nextConcreteQueryTerm.getCriterion());
        }

        return criterionDisjunction;
    }
}
