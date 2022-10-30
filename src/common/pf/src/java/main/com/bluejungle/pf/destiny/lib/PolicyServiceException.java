package com.bluejungle.pf.destiny.lib;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/PolicyServiceException.java#1 $
 */

/**
 * This class represents exceptions thrown from the
 * Policy Services. These exceptions are converted
 * to PolicyServiceFaults on the way out.
 */

public class PolicyServiceException extends Exception {

    /**
     * Constructs an empty PolicyServiceException.
     */
    public PolicyServiceException() {
    }

    /**
     * Constructs an PolicyServiceException with the
     * specified message.
     * @param message The message of the PolicyServiceException.
     */
    public PolicyServiceException( String message ) {
        super( message );
    }

    /**
     * Constructs an PolicyServiceException with the
     * specified message and the cause.
     * @param message The message of the PolicyServiceException.
     * @param cause The cause of the PolicyServiceException.
     */
    public PolicyServiceException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs an PolicyServiceException with the
     * specified cause.
     * @param cause The cause of the PolicyServiceException.
     */
    public PolicyServiceException( Throwable cause ) {
        super( cause );
    }

}
