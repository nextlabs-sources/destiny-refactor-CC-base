/*
 * Created on Nov 20, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/test/com/nextlabs/destiny/container/ddac/DDACTestSuite.java#1 $:
 */

package com.nextlabs.destiny.container.ddac;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.nextlabs.destiny.container.ddac.components.deployment.PowerShellDeployerTest;

public class DDACTestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("DDAC Component");
        suite.addTest(PowerShellDeployerTest.suite());

        return suite;
    }
}
