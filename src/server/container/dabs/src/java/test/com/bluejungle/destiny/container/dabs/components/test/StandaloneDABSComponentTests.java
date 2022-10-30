/*
 * Created on Feb 15, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.dabs.components.deployment.PolicyDeploymentTest;
import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriterTest;

import org.apache.commons.logging.*;
	
/**
 * DABS component test which run outside of the servlet container
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/components/test/StandaloneDABSComponentTests.java#2 $
 */

public class StandaloneDABSComponentTests {

    /**
     * Returns the test suite
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DABS internal Components");
        suite.addTest(new TestSuite(HibernateLogWriterTest.class, "Hibernate Log Writer"));

		Log log = LogFactory.getLog(StandaloneDABSComponentTests.class.getName());
		log.fatal("!!!!!!!!111Before Policy Deployment Test!!!!!!!!!");
        suite.addTest(new TestSuite(PolicyDeploymentTest.class, "Policy Deployment Component"));
		log.fatal("!!!!!!!!After Policy Deployment Test!!!!!!!!!");
        return suite;
    }
}
