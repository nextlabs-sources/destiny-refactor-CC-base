/*
 * Created on Feb 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * This exception is thrown when an invalid deployment Id is used.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/UnknownDeploymentIdException.java#1 $
 */

public class UnknownDeploymentIdException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     * 
     * @param msg
     *            message
     * @param attrs
     *            attributes
     */
    public UnknownDeploymentIdException(String deploymentID) {
        super();
        this.addNextPlaceholderValue(deploymentID);
    }

    /**
     * Constructor
     * 
     * @param msg
     *            message
     * @param attrs
     *            attributes
     * @param nested
     *            nested exception
     */
    public UnknownDeploymentIdException(String deploymentID, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(deploymentID);
    }
}