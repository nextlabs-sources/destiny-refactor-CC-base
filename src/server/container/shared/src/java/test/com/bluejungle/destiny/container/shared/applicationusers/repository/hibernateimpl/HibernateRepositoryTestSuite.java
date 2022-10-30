/*
 * Created on Jul 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.ApplicationUserComponentTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/HibernateRepositoryTestSuite.java#1 $
 */

public class HibernateRepositoryTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateRepositoryTestSuite.suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl");
        suite.addTestSuite(TestAccessDomainDO.class);
        suite.addTestSuite(TestHibernateApplicationUserRepository.class);
        suite.addTestSuite(TestInternalApplicationUserDO.class);
        suite.addTestSuite(TestBaseAccessGroupDO.class);
        suite.addTestSuite(TestLinkedAccessGroupDO.class);
        suite.addTestSuite(TestBaseApplicationUserDO.class);
        return suite;
    }

}
