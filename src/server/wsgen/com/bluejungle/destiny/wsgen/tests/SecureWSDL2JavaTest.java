/*
 * Created on Dec 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.bluejungle.destiny.wsgen.SecureWsdl2javaAntTask;

import junit.framework.TestCase;

/**
 * This is the test class for the secure WSDL 2 Java utility
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/wsgen/com/bluejungle/destiny/wsgen/tests/SecureWSDL2JavaTest.java#1 $:
 */

public class SecureWSDL2JavaTest extends TestCase {

    private static final String WRITTEN = "\t<requestFlow>\r\n\t<handler type=\"java:com.bluejungle.destiny.server.security.CertificateChecker\">\r\n\t\t"
            + "<parameter name=\"trustedCerts\" value=\"Bar\">\r\n\t\t</parameter>\r\n\t</handler>\r\n\t</requestFlow>\r\n";

    /**
     * Constructor
     *  
     */
    public SecureWSDL2JavaTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public SecureWSDL2JavaTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the list of trusted callers can be properly set
     * from the Ant script.
     */
    public void testTrustedCallersList() {
        SecureWsdl2javaAntTask task = new SecureWsdl2javaAntTask();
        task.setTrustedCallers("Foo");
        assertEquals("Foo", SecureWsdl2javaAntTask.getTrustedCallers());
        task.setTrustedCallers(null);
    }

    /**
     * This test verifies that the custom WSDL writer inserts the right lines in
     * the WSDD file.
     */
    public void testCustomWSDLWriter() {
        SecureWsdl2javaAntTask task = new SecureWsdl2javaAntTask();
        task.setTrustedCallers("Bar");
        MockSecureDeployWriter mockDeployWriter = new MockSecureDeployWriter();
        Writer mockStringWriter = new StringWriter();
        PrintWriter mockPrintWriter = new PrintWriter(mockStringWriter);
        mockDeployWriter.testCustomWriting(mockPrintWriter);
        mockPrintWriter.flush();
        String output = mockStringWriter.toString();
        assertEquals(WRITTEN, output);
        task.setTrustedCallers(null);
    }

    /**
     * This test verifies that the task cannot take incompatible parameters
     */
    public void testParameterCompatibility() {
        MockSecureWSDL2JavaAntTask task = createWSDLToJavaTask();
        boolean exThrown = false;
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("No trusted callers and no access list is a valid setup", exThrown);

        //Set trusted callers
        task = createWSDLToJavaTask();
        exThrown = false;
        task.setTrustedCallers("ABC, BCD");
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("Setting trusted callers and no access list is a valid setup", exThrown);

        //Set access list
        task = createWSDLToJavaTask();
        exThrown = false;
        task.setAccessList("Cert1=*- API1");
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("Setting access list and no trusted callers is a valid setup", exThrown);

        //Set trusted callers and access list (invalid)
        task = createWSDLToJavaTask();
        exThrown = false;
        task.setTrustedCallers("ABC, BCD");
        task.setAccessList("Cert1=*- API1");
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertTrue("Setting trusted callers and access list is invalid", exThrown);
    }

    /**
     * This test verifies that the access list gets correctly parsed, and that
     * invalid access lists get rejected.
     */
    public void testAccessListParsing() {
        //Pass a simple access list
        MockSecureWSDL2JavaAntTask task = createWSDLToJavaTask();
        task.setAccessList("Cert1=*- API1");
        boolean exThrown = false;
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("Parsed access list is valid.", exThrown);
        Map acl = MockSecureWSDL2JavaAntTask.getAccessList();
        assertEquals(acl.get("Cert1"), "*- API1");

        //Now, pass a more complex access list
        task = createWSDLToJavaTask();
        task.setAccessList("Cert1=*- API1; Cert2=API2; Cert3=*- API3");
        exThrown = false;
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("Parsed access list is valid.", exThrown);
        acl = MockSecureWSDL2JavaAntTask.getAccessList();
        assertEquals(acl.get("Cert1"), "*- API1");
        assertEquals(acl.get("Cert2"), "API2");
        assertEquals(acl.get("Cert3"), "*- API3");

        //Pass a bad access list (format issue)
        task = createWSDLToJavaTask();
        task.setAccessList("Cert1=*- API1; Cert2; Cert3=*- API3");
        exThrown = false;
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertTrue("Invalid access list is detected.", exThrown);
    }

    /**
     * This test verifies that when an access list is given to the task, the
     * handler is then properly generated.
     */
    public void testAPIAuthAccessList() {

        final String output = "\t<requestFlow>\r\n\t<handler type=\"java:com.bluejungle.destiny.server.security.APIAuthChecker\">\r\n\t\t<parameter name=\"Cert2\" value=\"API2\">\r\n\t\t</parameter>"
                + "\r\n\t\t<parameter name=\"Cert1\" value=\"*- API1\">\r\n\t\t</parameter>\r\n\t</handler>\r\n\t</requestFlow>\r\n";
        MockSecureWSDL2JavaAntTask task = createWSDLToJavaTask();
        task.setAccessList("Cert1=*- API1; Cert2=API2");
        boolean exThrown = false;
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("Access list is valid", exThrown);
        MockSecureDeployWriter mockDeployWriter = new MockSecureDeployWriter();
        Writer mockStringWriter = new StringWriter();
        PrintWriter mockPrintWriter = new PrintWriter(mockStringWriter);
        mockDeployWriter.testCustomWriting(mockPrintWriter);
        mockPrintWriter.flush();
        String pwOutput = mockStringWriter.toString();
        assertEquals("APIAuth handler output is valid", output, pwOutput);
    }

    /**
     * This test verifies validation for the user auth and client application parameters
     */
    public void testUserAuth() {
        MockSecureWSDL2JavaAntTask task = createWSDLToJavaTask();
        task.testValidation();
        task.setUserAuthRequired("true");
        boolean exThrown = false;
        try {
            task.testValidation();
        } catch (BuildException ex) {
            exThrown = true;
        }
        assertFalse("Ensure if userAuthRequired without clientApplication set, exception is thrown", exThrown);
        task.setClientApplication("foo");
        task.testValidation();
    }
    
    /**
     * This utility function creates a fresh WSDL2Java task that can then be
     * customized for each test.
     */
    private MockSecureWSDL2JavaAntTask createWSDLToJavaTask() {
        MockSecureWSDL2JavaAntTask task = new MockSecureWSDL2JavaAntTask();
        task.setTrustedCallers(null);
        task.setAccessList(null);
        task.setURL("myFile.wsdl");
        task.setImplementationClassName("com.bluejungle.myClass");
        task.setFactory("com.bluejungle.destiny.wsgen.SecureServiceGenerator");
        return (task);
    }
}