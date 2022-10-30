package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/DuplicateEntityException.java#1 $
 */

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.bluejungle.framework.utils.CollectionUtils;

/**
 * This exception is thrown when an attempt is made to save
 * an entity with a name duplicating one of the existing names.
 */
public class DuplicateEntityException extends EntityManagementException {

    private static final long serialVersionUID = 1L;
    
    /**
     * A <code>Collection</code> of the duplicated entity names
     * that caused the error.
     */
    private Collection<String> duplicatedNames;

    /**
     * Creates a new DuplicateEntityException.
     * @param duplicatedNames A <code>Collection</code> of the duplicated entity names
     * that caused the error. 
     */
    public DuplicateEntityException( Collection<String> duplicatedNames ) {
        this.duplicatedNames =
                Collections.unmodifiableCollection(new HashSet<String>(duplicatedNames));
    }

    /**
     * Returns a <code>Collection</code> of <code>String</code> objects
     * representing names of entities that caused this error.  
     * @return a <code>Collection</code> of <code>String</code> objects
     * representing names of entities that caused this error.
     */
    public Collection<String> getDuplicatedNames() {
        return duplicatedNames;
    }
    
    @Override
    public String getMessage() {
        return "duplicatedNames = " + CollectionUtils.asString(duplicatedNames, ", ");
    }
}
