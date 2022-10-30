/*
 * Created on Nov 20, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/test/com/nextlabs/destiny/container/ddac/components/deployment/PowerShellDeployerTest.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.components.deployment;

import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;

import com.bluejungle.framework.test.OutputFileBasedTestSuite;

public class PowerShellDeployerTest extends TestCase  {
    private static OutputFileBasedTestSuite getOutputFileBasedTestSuite() {
        return new OutputFileBasedTestSuite(PowerShellDeployerTest.class);
    }

    public static Test suite() {
        return getOutputFileBasedTestSuite();
    }
}
