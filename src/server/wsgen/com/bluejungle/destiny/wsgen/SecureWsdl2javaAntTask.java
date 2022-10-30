/*
 * Created on Dec 16, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.axis.tools.ant.wsdl.Wsdl2javaAntTask;
import org.apache.tools.ant.BuildException;

/**
 * This simple ant tasks allows the wsdl2java tagto specify the list of callers
 * that will be considered trusted by a given web service. The generator class
 * will pick up these values and generate the appropriate configuration tags
 * accordingly.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/wsgen/com/bluejungle/destiny/wsgen/SecureWsdl2javaAntTask.java#1 $:
 */

public class SecureWsdl2javaAntTask extends Wsdl2javaAntTask {

    private static final String SEPARATOR = ";";
    private static final String EQUAL = "=";
    private static String trustedCallers;
    private static String accessList;
    private static Map parsedAccessList;
    private static String userAuthRequired;
    private static String clientApplication;

    /**
     * This function simply clears static members after the script execution
     * completes.
     * 
     * @throws BuildException
     *             if the execution fails
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        super.execute();
        parsedAccessList = null;
        trustedCallers = null;
        accessList = null;
        clientApplication = null;
    }

    /**
     * This function validates input arguments for the tasks. In our case, the
     * trusted callers cannot be used at the same time as the access list (they
     * are mutually exclusive). Also, if the access list value is not correct,
     * an exception should be thrown.
     * 
     * @throws BuildException
     *             if parameters are invalid.
     */
    protected void validate() throws BuildException {
        super.validate();

        //check mutual exclusion
        if (accessList != null && trustedCallers != null) {
            throw new BuildException("The parameters trustedCallers and accessList cannot be used at the same time. Please pick only one of the two.");
        }

        if (accessList != null) {
            parsedAccessList = new HashMap();
            parseAccessLists();
        }

        if (trustedCallers != null && trustedCallers.length() == 0) {
            throw new BuildException("No trusted callers specified");
        }
                
        if ((userAuthRequired != null) && (userAuthRequired.equalsIgnoreCase("true")) && (clientApplication == null)) {
            throw new BuildException("clientApplication parameter necessary when userAuthRequired is true.");
        }
    }

    /**
     * This function parses the access list given as a parameter and stores it
     * into a collection so that it can be processed later on by the WSDD file
     * writer.
     * 
     * @throws BuildException
     *             if the access list is not correctly written
     */
    private void parseAccessLists() throws BuildException {
        String rawAccessList = accessList;

        if (rawAccessList.length() == 0) {
            throw new BuildException("accessList cannot be empty (all callers will be denied)");
        }

        StringTokenizer accessLists = new StringTokenizer(rawAccessList, SEPARATOR);
        int nbAccessLists = accessLists.countTokens();
        while (accessLists.hasMoreTokens()) {
            processAccessList(accessLists.nextToken());
        }
    }

    /**
     * Processes a single access list and place it in the parsed structure.
     * 
     * @param accessList
     *            raw access list (format should be "aliasName=rule")
     * @throws BuildException
     *             if the format or parsing is incorrect
     */
    private void processAccessList(String accessList) throws BuildException {
        StringTokenizer parser = new StringTokenizer(accessList, EQUAL);
        if (parser.countTokens() != 2) {
            //The format of the string is incorrect
            throw new BuildException("Incorrect access list : " + accessList);
        }

        String aliasName = parser.nextToken().trim();
        String access = parser.nextToken().trim();
        parsedAccessList.put(aliasName, access);
    }

    /**
     * Returns the list of trusted callers aliases
     * 
     * @return the list of trusted callers aliases
     */
    public static String getTrustedCallers() {
        return trustedCallers;
    }

    /**
     * Sets the list of trusted callers
     * 
     * @param trustedCallers
     *            space separated list of caller names
     */
    public void setTrustedCallers(String callers) {
        trustedCallers = callers;
    }

    /**
     * Returns the parsed access list. The raw access list is not accessible.
     * 
     * @return the parsed access list.
     */
    public static Map getAccessList() {
        return parsedAccessList;
    }

    /**
     * Sets the access list
     * 
     * @param accessList
     *            The access list to set.
     */
    public void setAccessList(String accessList) {
        SecureWsdl2javaAntTask.accessList = accessList;
    }

    /**
     * Determine if the service requires user authorization
     * 
     * @return true if the service requires user authorization; false otherwise
     */
    public static String getUserAuthRequired() {
        return userAuthRequired;
    }

    /**
     * Set the user authentication requirements
     * 
     * @param userAuthRequired
     *            true if the service requires authentication; false otherwise
     */
    public void setUserAuthRequired(String userAuthRequired) {
        SecureWsdl2javaAntTask.userAuthRequired = userAuthRequired;
    }

    /**
     * Retrieve the clientApplication.
     * 
     * @return the clientApplication.
     */
    public static String getClientApplication() {
        return clientApplication;
    }

    /**
     * Set the clientApplication
     * 
     * @param clientApplication
     *            The clientApplication to set.
     */
    public static void setClientApplication(String clientApplication) {
        SecureWsdl2javaAntTask.clientApplication = clientApplication;
    }
}