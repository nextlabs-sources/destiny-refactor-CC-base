/*
 * Created on Jan 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * ProfileNotFoundException is thrown when an attempt is made to retrieve a
 * profile by a unique field that is not associated with an existing profile
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/ProfileNotFoundException.java#1 $
 */
public class ProfileNotFoundException extends SingleErrorBlueJungleException {

    /**
     * Construct a profile not found exception with the error name and map of
     * arguments
     * 
     * @param errorName
     *            The name of the specific error that occurred
     * @param arguments
     *            The arguments to replace into an internationalized message
     */
    public ProfileNotFoundException(String typeOfProfileBeingRetrieved, String retrievalField, String retrievalValue) {
        super();
        this.addNextPlaceholderValue(typeOfProfileBeingRetrieved);
        this.addNextPlaceholderValue(retrievalField);
        this.addNextPlaceholderValue(retrievalValue);
    }

    public ProfileNotFoundException(String typeOfProfileBeingRetrieved, String retrievalField, String retrievalValue, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(typeOfProfileBeingRetrieved);
        this.addNextPlaceholderValue(retrievalField);
        this.addNextPlaceholderValue(retrievalValue);
    }
}