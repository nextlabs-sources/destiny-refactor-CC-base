package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/CircularReferenceException.java#1 $
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * Represents a policy editor exception caused by a circular reference.
 */
public class CircularReferenceException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Represents the chain of references. */
    private List<DomainObjectDescriptor> refs;

    /**
     * Creates a new <code>CircularReferenceException</code>.
     * @param refs the chain of references.
     */
    public CircularReferenceException( List<DomainObjectDescriptor> refs ) {
        super( "Circular reference is detected." );
        if ( refs == null ) {
            throw new NullPointerException("refs");
        }
        this.refs = Collections.unmodifiableList( new ArrayList<DomainObjectDescriptor>( refs ) );
    }

    /**
     * Returns the chain of references.
     * @return the chain of references.
     */
    public List<DomainObjectDescriptor> getChainOfReferences() {
        return refs;
    }

}
