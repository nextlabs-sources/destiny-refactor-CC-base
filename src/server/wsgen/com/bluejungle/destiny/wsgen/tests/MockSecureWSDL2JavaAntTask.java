/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen.tests;

import org.apache.tools.ant.BuildException;

import com.bluejungle.destiny.wsgen.SecureWsdl2javaAntTask;

/**
 * This is a dummy class extending the real class (to be tested), so that tests
 * internal to the class can be done properly.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/wsgen/com/bluejungle/destiny/wsgen/tests/MockSecureWSDL2JavaAntTask.java#1 $
 */

public class MockSecureWSDL2JavaAntTask extends SecureWsdl2javaAntTask {

    /**
     * This method validates the task arguments. It calls the internal
     * validation method.
     * 
     * @throws BuildException
     *             if the validation of parameters failed.
     */
    public void testValidation() throws BuildException {
        validate();
    }
}