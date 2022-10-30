/**
 * Created on Jan 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync;

import com.novell.ldap.LDAPControl;

/**
 * Deleted control for Active Directory
 * 
 * When tracking changes made in Active Directory, in order to retrieve deleted objects, 
 * we have to use Delete Control. 
 *
 * @see http://forum.java.sun.com/thread.jspa?threadID=578338&tstart=200 
 * 
 * @author Andy Tian 
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/dirsync/DeletedControl.java#1 $
 */

public class DeletedControl extends LDAPControl {

    /*
     * Default control detects all objects that have been deleted in the Active Directory
     */
    private static final String OID = "1.2.840.113556.1.4.417";

    private static final boolean IS_CRITICAL = true; // Always set the criticality to TRUE

    /**
     * Constructor
     */
    public DeletedControl() {
        super(OID, IS_CRITICAL, null);
    }

    public String toString() {
        return OID; 
    }

}
