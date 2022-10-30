/*
 * Created on Dec 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf;

import com.bluejungle.pf.destiny.formatter.TestDomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.TestLeafObjectSearchSpec;
import com.bluejungle.pf.destiny.parser.ParserSuite;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentDOSuite;
import com.bluejungle.pf.domain.destiny.environment.TestTimeAttribute;
import com.bluejungle.pf.domain.destiny.policy.PolicyDOSuite;
import com.bluejungle.pf.domain.destiny.resource.ResourceDOSuite;
import com.bluejungle.pf.domain.destiny.subject.TestSubjectManager;
import com.bluejungle.pf.engine.destiny.EvaluationEngineSuite;
import com.nextlabs.pf.destiny.formatter.xacml.TestXACMLDomainObjectFormatter;
import com.nextlabs.pf.destiny.formatter.dac.TestDACDomainObjectFormatter;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/CommonPFTestSuite.java#1 $
 */

public class CommonPFTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.pf");
        //$JUnit-BEGIN$

        suite.addTest(TestDomainObjectFormatter.suite());
        suite.addTest(TestXACMLDomainObjectFormatter.suite());
        suite.addTest(TestDACDomainObjectFormatter.suite());
        suite.addTest(new TestSuite(TestLeafObjectSearchSpec.class));
        suite.addTest(ParserSuite.suite());
        suite.addTest(DeploymentDOSuite.suite());
        suite.addTest(new TestSuite(TestTimeAttribute.class));
        suite.addTest(PolicyDOSuite.suite());
        suite.addTest(ResourceDOSuite.suite());
        suite.addTest(new TestSuite(TestSubjectManager.class));
        suite.addTest(EvaluationEngineSuite.suite());
        
        //$JUnit-END$
        return suite;
    }

}
