/*
 * Created on Feb 23, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * The DMS registration outcome class represents whether a registration request
 * was approved or not.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/DMSRegistrationResult.java#1 $
 */

public class DMSRegistrationResult {

    /**
     * Constants to be used to describe an outcome
     */
    public static final DMSRegistrationResult SUCCESS = new DMSRegistrationResult("Success");
    public static final DMSRegistrationResult FAILURE = new DMSRegistrationResult("Failure");
    public static final DMSRegistrationResult PENDING = new DMSRegistrationResult("Pending");

    private String name;

    /**
     * 
     * Constructor (private)
     * 
     * @param name
     *            name of the outcome
     */
    private DMSRegistrationResult(String name) {
        this.name = name;
    }

    /**
     * Returns the outcome name
     * 
     * @return the outcome name
     */
    public String getName() {
        return this.name;
    }
}
