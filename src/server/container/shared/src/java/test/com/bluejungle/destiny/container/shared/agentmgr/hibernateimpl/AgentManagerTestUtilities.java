/*
 * Created on Apr 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentManagerTestUtilities.java#1 $
 */

public class AgentManagerTestUtilities {

    /**
     * Constructor
     *  
     */
    public AgentManagerTestUtilities() {
        super();
    }

    /**
     * Cleans up all agents for the tests
     *  
     */
    public static void cleanupAllAgents() throws Exception {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        Session session = dataSource.getSession();
        Transaction tx = session.beginTransaction();
        session.delete("from AgentDO");
        tx.commit();
        session.close();
    }
}