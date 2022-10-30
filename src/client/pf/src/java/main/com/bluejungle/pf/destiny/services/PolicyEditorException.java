package com.bluejungle.pf.destiny.services;

import com.bluejungle.destiny.services.policy.types.PolicyServiceFault;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/destiny/services/PolicyEditorException.java#1 $
 */

/**
 * Represents an exception of the <code>PolicyEditor</code>.
 * Instances of this class are created only in response
 * to receiving faults from the web service.
 */
public class PolicyEditorException extends Exception {

    /**
     * Constructs an empty <code>PolicyEditorException</code>.
     */
    public PolicyEditorException() {
    }

    /**
     * Constructs a <code>PolicyEditorException</code> with the
     * specified message.
     * @param message The message of the <code>PolicyEditorException</code>.
     */
    public PolicyEditorException( String message ) {
        super( message );
    }

    /**
     * Constructs a <code>PolicyEditorException</code> with the
     * specified message and the cause.
     * @param message The message of the PolicyEditorException.
     * @param cause The cause of the <code>PolicyEditorException</code>.
     */
    public PolicyEditorException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs a <code>PolicyEditorException</code> with the
     * specified cause.
     * @param cause The cause of this <code>PolicyEditorException</code>.
     */
    public PolicyEditorException( Throwable cause ) {
        super( cause != null ? cause.getMessage() : null, cause );
    }

    /**
     * Constructs a <code>PolicyEditorException</code> with the
     * specified cause.
     * @param cause The cause of this <code>PolicyEditorException</code>.
     */
    public PolicyEditorException( PolicyServiceFault cause ) {
        super( cause.getCauseMessage(), cause );
    }

}
