/*
 * Created on Dec 22, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.epicenter.misc;

import com.bluejungle.pf.domain.epicenter.common.ISpec;

/**
 * IPolicyACL is a single access control list entry.  It consists of a type, which is
 * either a READ, EDIT, DEPLOY, or DELETE and a ISubjectSpec which describes the set
 * of subjects that this entry applies to.
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/misc/IPolicyACL.java#1 $:
 */

public interface IPolicyACL {
    
    int READ = 1;
    int EDIT = 2;
    int DEPLOY = 3;
    int DELETE = 4;

    /**
     * @return the type of this ACL, which is one of the pre-defined types
     */
    int getType();
    
    /**
     * @return a subject spec that describes a set of subjects that this ACL
     * covers 
     */
    ISpec getSubject();
}
