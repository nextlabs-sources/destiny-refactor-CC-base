/*
 * Created on Jul 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers;

import com.bluejungle.destiny.container.shared.applicationusers.auth.jaas.AuthMgrTestSuite;
import com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.TestLDAPAccessProvider;
import com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateRepositoryTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/ApplicationUserComponentTestSuite.java#2 $
 */

public class ApplicationUserComponentTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ApplicationUserComponentTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.container.shared.applicationusers");
        suite.addTest(AuthMgrTestSuite.suite());
        suite.addTest(new TestSuite(TestLDAPAccessProvider.class, "LDAP User Access Provider test"));
        //suite.addTest(new TestSuite(TestApplicationUserManagerWithLocalAuth.class, "User Management tests with Local Authentication"));
        //suite.addTest(new TestSuite(TestApplicationUserManagerWithHybridAuth.class, "User Management tests with Hybrid Authentication"));
        suite.addTest(HibernateRepositoryTestSuite.suite());
        return suite;
    }
}
